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

	/**
	 * hardware
	 */
	private LightSensor lightSensor;
	private TouchSensor limitSwitch;
	private NXTRegulatedMotor wheelMotor;
	private NXTRegulatedMotor armMotor;
	private NXTRegulatedMotor penMotor;

	/**
	 * localization variables, limits
	 */
	private int swivelArmMaxHalfRange;
	private double globalXCoord;
	private double globalYCoord;
	private double armTheta;
	private double jointY;
	private double armBackslash;// degrees
	private double wheelBackslash;// mm, distance travelled by wheel

	/**
	 * timer and related variables
	 */
	private int lastReadWheel;
	private int lastReadArm;
	private MainTimerHandler mainTimerHandler;

	/**
	 * defaults and constants
	 */
	private int armMotorDefaultSpeed;
	private int wheelMotorDefaultSpeed;

	private final int LIGHT_THRESHOLD = 500;
	private final int WHEEL_GEAR_RATIO = 5;
	private final int ARM_GEAR_RATIO = 84;
	private final int JOINT_TO_LIGHT_SENSOR = 105;
	private final int JOINT_TO_PEN = 80;
	private final int WHEEL_DIAMETER = 56;

	private final float ARM_MOTOR_CAL_SPEED = 0.8f;
	private final int ARM_MOTOR_CAL_ACC = 2000;
	private final int WHEEL_MOTOR_ACC = 2000;

	private final int TIMER_FREQ = 20; // in Hertz

	/**
	 * Timer listener
	 * 
	 * @author darshana
	 * 
	 */
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
		swivelArmMaxHalfRange = 0;
		armBackslash = 0;
		globalXCoord = 0;
		globalYCoord = 0;
		armTheta = 0;
		jointY = 0;
		lastReadWheel = 0;
		lastReadArm = 0;

		wheelMotorDefaultSpeed = armMotor.getSpeed();
		armMotorDefaultSpeed = armMotor.getSpeed();
	}

	/**
	 * 
	 * @return
	 */
	public boolean calibrationRoutine() {
		LCD.drawString("Press Enter", 0, 0);
		LCD.drawString("to Start Calib.", 0, 1);
		Button.ENTER.waitForPressAndRelease();

		calibrateArm();
		// LCD.clear();
		LCD.drawString("Going to initial Pos", 0, 1);
		LCD.clear();
		// Wheel gear backslash calculation
		wheelBackslash = (getWheelBackslash() + getWheelBackslash() + getWheelBackslash()) / 3;

		while (wheelMotor.isMoving() && armMotor.isMoving()) {

		}

		resetKinematics();
		
		LCD.drawString(
				wheelMotor.getPosition() + " " + armMotor.getPosition(), 0,
				4);
		LCD.drawString("Calibration Done", 0, 1);
		
		Sound.beep();
		
		updateForwardKinematics();

		// setup timer handler (listener)
		mainTimerHandler = new MainTimerHandler();
		mainTimerHandler.mainTimerStart();// start timer
		
		return true;

	}

	/**
	 * Calibrate the arm
	 * 
	 * @return
	 */
	private boolean calibrateArm() {
		LCD.clear();
		LCD.drawString("Starting arm calibration", 0, 0);
		LCD.drawString("Starting arm swing", 0, 1);

		/**
		 * Slower speeds
		 */
		armMotor.setAcceleration(ARM_MOTOR_CAL_ACC);
		armMotor.setSpeed(ARM_MOTOR_CAL_SPEED);// going slower
		// Get initial position. Assume this is center
		int swivelArmInitialPos = armMotor.getTachoCount();

		while (!limitSwitch.isPressed()) {
			armMotor.backward(); // replace with position(1)?
		}

		armMotor.stop();

		int swivelArmMaxHalfRange = Math.abs(armMotor.getTachoCount()
				- swivelArmInitialPos);

		LCD.drawString("Max Range " + swivelArmMaxHalfRange, 0, 0);

		/**
		 * Perform backslash calculation. Get value 3 times and average Since
		 * arm is "lagging" when going in anticlockwise dir, the angle must be
		 * added to the target angle. For clockwise, no backslash is added.
		 * 
		 * Use rotateArmToAngleCal() to rotate with backslash compensation
		 * added.
		 */

		armBackslash = (getArmBackslash() + getArmBackslash() + getArmBackslash()) / 3;

		// Goto center position.
		armMotor.rotate((swivelArmMaxHalfRange));

		return true;
	}

	public void updateForwardKinematics() {
		int currentArm = armMotor.getTachoCount();
		int currentWheel = wheelMotor.getTachoCount();

		double armDelta = currentArm - lastReadArm;
		// TODO perform overflow check
		double wheelDelta = currentWheel - lastReadWheel;

		lastReadArm = currentArm;
		lastReadWheel = currentWheel;
		// TODO perform overflow check
		if (wheelDelta != 0) {
			jointY += motorAngleToWheelDistance(wheelDelta);
		}
		if (armDelta != 0) {
			armTheta += motorAngleToArmAngle(armDelta);
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
	 * Wheel/ y axis calibration. Main task: move pen to y=0 Secondary task:
	 * calculate backslash.
	 * 
	 */
	private double getWheelBackslash() {

		// drive until light sensor is hit.
		while (lightSensor.getNormalizedLightValue() > LIGHT_THRESHOLD) {
			wheelMotor.backward();
		}

		wheelMotor.stop();
		double wheelBackslashInit = wheelMotor.getTachoCount();

		// Now bring the robot front so that the light sensor is at starting
		// edge.
		int previousValue = lightSensor.getNormalizedLightValue();

		// detect rising edge. (black to white transition
		while (!(previousValue < LIGHT_THRESHOLD && lightSensor
				.getNormalizedLightValue() > LIGHT_THRESHOLD)) {

			wheelMotor.forward();
			// LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		}

		wheelMotor.stop();

		wheelBackslashInit = motorAngleToWheelDistance(Math.abs(wheelMotor
				.getTachoCount() - wheelBackslashInit));

		// move forward to align pen to 0.
		moveWheel(JOINT_TO_LIGHT_SENSOR - JOINT_TO_PEN);
		wheelMotor.stop();

		return wheelBackslashInit;
	}

	/**
	 * 
	 * @param position
	 * @return backslash in degrees
	 */
	private double getArmBackslash() {

		// Slower speeds.

		armMotor.setAcceleration(ARM_MOTOR_CAL_ACC / 2);
		armMotor.setSpeed(ARM_MOTOR_CAL_SPEED / 2);// going slower for backslash

		while (!limitSwitch.isPressed()) {
			// LCD.drawString("Swinging the arm", 0,1);
			armMotor.backward(); // replace with position(1)?
		}
		armMotor.stop();

		int armBackslashInit = armMotor.getTachoCount();

		// TODO Better to do event listener
		while (limitSwitch.isPressed()) {
			// LCD.drawString("Swinging the arm", 0,1);
			armMotor.forward(); // replace with position(1)?
		}

		armBackslashInit = Math
				.abs(armBackslashInit - armMotor.getTachoCount());
		armMotor.stop();

		// normal speeds
		armMotor.setAcceleration(4000);// defaults
		armMotor.setSpeed(armMotorDefaultSpeed);// going slower for backslash

		return (double) armBackslashInit / ARM_GEAR_RATIO;
	}

	/**
	 * Rotate arm in degrees. Performs backslash compensation and limits.
	 * positive = counterclockwise negative = clockwise if counter clockwise,
	 * add backslash
	 */
	public boolean rotateArm(int angle) {
		if (Math.abs(armAngleToMotorAngle(angle) + armMotor.getPosition()) < swivelArmMaxHalfRange) {
//			if (angle - armMotor.getPosition() > 0) {// if positive, arm rotates
														// counterclockwise
//				armMotor.rotate((int) (angle + armBackslash));
//			} else {
				armMotor.rotate(angle);
//			}
			return true;
		} else {
			return false;// out of bounds!
		}
	}

	/**
	 * Specify in mm, positive = forward, negativ = backward
	 */
	public void moveWheel(double value) {
		// TODO backslash compensation
		wheelMotor.rotate(distanceToWheelMotorAngle(value));
	}

	public int armAngleToMotorAngle(double angle) {
		return (int) angle * ARM_GEAR_RATIO;
	}

	/**
	 * Convert motor angle to arm angle
	 * 
	 * @param
	 * @return
	 */
	public double motorAngleToArmAngle(double angle) {
		return angle / ARM_GEAR_RATIO;
	}

	/**
	 * Convert distance to motor angle (for wheels)
	 * 
	 * @param angle
	 * @return
	 */
	public int distanceToWheelMotorAngle(double angle) {
		return (int) (angle * 360 * WHEEL_GEAR_RATIO / (Math.PI * WHEEL_DIAMETER));
	}

	/**
	 * Convert motor angle to distance in mm (for wheels)
	 * 
	 * @param angle
	 * @return
	 */
	public double motorAngleToWheelDistance(double angle) {
		return (angle / WHEEL_GEAR_RATIO) / 360 * Math.PI * WHEEL_DIAMETER;
	}

	/**
	 * reset kinematics
	 */
	private void resetKinematics() {
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
