package de.tuhh.diss.plotbot;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.util.Delay;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * This Class is used for control of the plotting robot. A great amount of time
 * should spend for controlling the robot. Add a suitable constructor and add
 * further methods you need for driving the motors, evaluating the sensors etc.
 * 
 * Hint: Create an instance of PlotBotControl inside the class Plotbot and *
 * pass the PlotBotControl when calling the method plot of the selected shape.
 */
public class PlotbotControl {

	private static PlotbotControl control = null;

	private LightSensor lightSensor;
	private TouchSensor limitSwitch;
	private NXTRegulatedMotor wheelMotor;
	private NXTRegulatedMotor armMotor;
	private NXTRegulatedMotor penMotor;

	private int swivelArmMaxRange;

	private double globalXCoord;
	private double globalYCoord;
	private double armTheta;
	private double jointY;
	private int lastReadWheel;
	private int lastReadArm;

	private MainTimerHandler mainTimerHandler;

	private final int LIGHT_THRESHOLD = 500;
	private final int WHEEL_GEAR_RATIO = 5;
	private final int ARM_GEAR_RATIO = 84;
	private final int JOINT_TO_LIGHT_SENSOR = 105;
	private final int JOINT_TO_PEN = 80;
	private final int WHEEL_DIAMETER = 56;

	private final int TIMER_FREQ = 20; // in Hertz

	class MainTimerHandler implements TimerListener {
		private Timer mainTimer;

		public MainTimerHandler() {
			// timer interval in ms. 1000/ freq = delay(ms);
			mainTimer = new Timer((1000 / TIMER_FREQ), this);
		}

		public void mainTimerStart() {
			mainTimer.start();
		}

		public void mainTimerStop() {
			mainTimer.stop();
		}

		@Override
		public void timedOut() {
			PlotbotControl.getInstance().updateForwardKinematics();
		}
	}

	/**
	 * Get instance for singleton Reason : to avoid multiple control objects
	 * spawning and attempting to control the robot
	 * 
	 * @return
	 */
	public static PlotbotControl getInstance() {
		if (control == null) {
			control = new PlotbotControl();
		}
		return control;
	}

	/**
	 * Going for singleton model Reason : to avoid multiple control objects
	 * spawning and attempting to control the robot
	 */
	private PlotbotControl() {
		lightSensor = new LightSensor(SensorPort.S3, true);
		wheelMotor = Motor.C;
		armMotor = Motor.A;
		penMotor = Motor.B;
		limitSwitch = new TouchSensor(SensorPort.S1);
		swivelArmMaxRange = 0;

		globalXCoord = 0;
		globalYCoord = 0;
		armTheta = 0;
		jointY = 0;
		lastReadWheel = 0;
		lastReadArm = 0;
	}

	public boolean calibrationRoutine() {
		LCD.drawString("Press Enter", 0, 0);
		LCD.drawString("to Start Calib.", 0, 1);
		Button.ENTER.waitForPressAndRelease();

		return calibrateArm();
	}

	private boolean calibrateArm() {
		LCD.drawString("Starting arm calibration", 0, 0);
		LCD.clear(1);

		LCD.drawString("Starting arm swing", 0, 1);

		// armMotor.resetTachoCount();
		int swivelArmInitialPos = armMotor.getPosition();

		while (!limitSwitch.isPressed()) {
			// LCD.drawString("Swinging the arm", 0,1);
			armMotor.backward();
		}
		armMotor.stop();
		swivelArmMaxRange = Math.abs(armMotor.getPosition()
				- swivelArmInitialPos) * 2;

		LCD.drawString("Max Range " + swivelArmMaxRange, 0, 0);

		// int rotatePolarity = Math.abs(armMotor.getPosition() -
		// swivelArmInitialPos)/(armMotor.getPosition() - swivelArmInitialPos);
		armMotor.rotate((swivelArmMaxRange) / 2);

		// LCD.clear();
		LCD.drawString("Going to initial Pos", 0, 1);

		wheelMotor.backward();
		// LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);

		while (lightSensor.getNormalizedLightValue() > LIGHT_THRESHOLD) {
			LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		}

		Motor.C.stop();

		/**
		 * Arm swing calibration Assume the arm is straight on start
		 */

		LCD.clear();

		// //now bring the robot front so that thelight sensor is at starting
		// edge.
		wheelMotor.forward();
		// LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		int previousValue = lightSensor.getNormalizedLightValue();
		while (!(previousValue < LIGHT_THRESHOLD && lightSensor
				.getNormalizedLightValue() > LIGHT_THRESHOLD)) {
			LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		}

		wheelMotor.stop();
		moveWheel(JOINT_TO_LIGHT_SENSOR - JOINT_TO_PEN);
		wheelMotor.stop();

		while (wheelMotor.isMoving() && armMotor.isMoving()) {

		}

		resetKinematics();
		LCD.drawString(wheelMotor.getPosition() + " " + armMotor.getPosition(), 0, 4);
		LCD.drawString("Calibration Done", 0, 1);
		Sound.beep();
		updateForwardKinematics();

		// setup timer handler (listener)
		mainTimerHandler = new MainTimerHandler();
		mainTimerHandler.mainTimerStart();// start timer

		return true;
	}

	public void updateForwardKinematics() {
		int currentArm = armMotor.getPosition();
		int currentWheel = wheelMotor.getPosition();

		double armDelta = currentArm - lastReadArm;
		// TODO perform overflow check
		double wheelDelta = currentWheel - lastReadWheel;

		lastReadArm = currentArm;
		lastReadWheel = currentWheel;
		// TODO perform overflow check
		if (wheelDelta != 0) {
			jointY += (wheelDelta / WHEEL_GEAR_RATIO) / 360 * Math.PI
					* WHEEL_DIAMETER;
		}
		if (armDelta != 0) {
			armTheta += armDelta / ARM_GEAR_RATIO;
		}

		globalXCoord = Math.sin(Math.toRadians(armTheta)) * JOINT_TO_PEN;
		globalYCoord = Math.cos(Math.toRadians(armTheta)) * JOINT_TO_PEN
				+ jointY;

		LCD.drawString("X :" + globalXCoord + " Y :" + globalYCoord, 0, 5);
		LCD.drawString(armDelta + " " + wheelDelta, 0, 6);
	}

	public void moveToStart() {

	}

	/**
	 * 
	 * @param position
	 */
	private void getArmBackslash(int position) {

	}

	/**
	 * Specify in mm, positive = forward, negativ = backward
	 */
	private void moveWheel(double value) {
		int angle = (int) (value * 360 * WHEEL_GEAR_RATIO / (Math.PI * WHEEL_DIAMETER));
		wheelMotor.rotate(angle);
	}
	
	/**
	 * reset kinematics
	 */
	private void resetKinematics(){
		globalXCoord = 0;
		globalYCoord = 0;
		wheelMotor.resetTachoCount();
		armMotor.resetTachoCount();
		lastReadArm = 0;
		lastReadWheel = 0;
		jointY = -80;
		armTheta = 0;
	}
}
