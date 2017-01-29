package de.tuhh.diss.plotbot.lowerLayer;

import de.tuhh.diss.plotbot.geometry.Coord;
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
	private TouchSensor armLimitSwitch;
	private TouchSensor penLimitSwitch;
	private NXTRegulatedMotor wheelMotor;
	private NXTRegulatedMotor armMotor;
	private NXTRegulatedMotor penMotor;

	/**
	 * localization variables, limits
	 */
	private int swivelArmMaxHalfRange;// +- this angle (degrees); MOTOR ANGLE.
	private Coord globalLocation;
	private double armTheta;
	private double jointY;
	private int armBackslash;// raw motor angles
	private int wheelBackslash;// raw motor angles
	private int penRange;// range will be degrees to move pen up and down.
	/**
	 * timer and related variables
	 */
	private boolean lastArmDir;// true right
	private boolean lastWheelDir;// true forward
	private MainTimerHandler mainTimerHandler;

	/**
	 * defaults and constants
	 */
	private int armMotorDefaultSpeed;
	private int wheelMotorDefaultSpeed;

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
			mainTimer = new Timer((1000 / Robot.TIMER_FREQ), this);
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
		armLimitSwitch = new TouchSensor(SensorPort.S1);
		penLimitSwitch = new TouchSensor(SensorPort.S2);

		globalLocation = new Coord(0, 0);
		swivelArmMaxHalfRange = 0;
		armBackslash = 0;
		armTheta = 0;
		jointY = 0;

		wheelMotorDefaultSpeed = armMotor.getSpeed();
		armMotorDefaultSpeed = armMotor.getSpeed();
	}

	/**
	 * This method is called by the timer and update the location of the robot.
	 */
	public void updateForwardKinematics() {
		int currentArm = armMotor.getPosition();
		int currentWheel = wheelMotor.getPosition();

		globalLocation = Robot.calculateForwardKinematics(currentWheel,
				currentArm);
		LCD.drawString("X :" + (int) globalLocation.x() + " Y :"
				+ (int) globalLocation.y(), 0, 5);
		LCD.drawString(currentWheel + " " + currentArm, 0, 6);
	}

	/**
	 * Get robot's current location
	 * 
	 * @return
	 */
	public Coord getRobotPosition() {
		updateForwardKinematics();
		return globalLocation;
	}

	/**
	 * move to 0,0 On calibration, motor tachometers are reset. 0 tach (arm
	 * motor)= center of arm 0 tach (wheel motor) = y=0 location (pen)
	 */
	public void moveToStart() {
		armMotor.rotateTo(0, true);
		wheelMotor.rotateTo(0, true);
	}

	/**
	 * Goto a given coordinate. Wheel will move first.
	 * 
	 * @param coord
	 *            - destination
	 * @return success status. Fails if bounds exceed.
	 */

	public boolean moveTo(Coord coord) {
		return moveTo(coord, false);
	}

	/**
	 * Goto a given coordinate Both actuators will activate at same time.
	 * 
	 * @param coord
	 *            - destination
	 * @return success status. Fails if bounds exceed.
	 */

	public boolean moveTo(Coord coord, boolean immidiateReturn) {
		double values[] = Robot.calculateInverseKinematics(coord);
		if (values != null && Math.abs(values[1]) < swivelArmMaxHalfRange) {

			moveWheelToRaw(values[0], immidiateReturn);

			return moveArmToRaw(values[1], false);
		} else {
			return false;
		}
	}

	/**
	 * Move pen up or down NEVER Run this without calibration!!!
	 * 
	 * @param down
	 */

	public void movePen(boolean down) {// down = true, go down
		if (!down) {// up = go to zero.
			while (!penLimitSwitch.isPressed()) {
				penMotor.forward();
			}
			penMotor.stop();
		} else {
			LCD.drawString("pen" + (-420 - penMotor.getPosition()), 0, 1);
			// Uses motor stall condition to avoid accidents
			penMotor.rotate(-420 - penMotor.getPosition());
			penMotor.stop();
		}
	}

	/**
	 * Rotate arm in degrees (Arm angle**). Performs backslash compensation and
	 * limits. positive = counterclockwise negative = clockwise if counter
	 * clockwise, add backslash
	 */
	public boolean moveArm(double angle, boolean immidiateReturn) {
		if (Math.abs(Robot.armAngleToMotorAngle(angle) + armMotor.getPosition()) < swivelArmMaxHalfRange) {
			// if (angle - armMotor.getPosition() > 0) {// if positive, arm
			// rotates counterclockwise
			// armMotor.rotate((int) (angle + armBackslash));
			// } else {
			armMotor.rotate(Robot.armAngleToMotorAngle(angle), immidiateReturn);
			// }
			LCD.drawChar('S', 10, 7);
			return true;
		} else {
			LCD.drawChar('F', 10, 7);
			return false;// out of bounds!
		}
	}

	/**
	 * Rotate arm in degrees (Arm angle**). Performs backslash compensation and
	 * limits. positive = counterclockwise negative = clockwise if counter
	 * clockwise, add backslash
	 */
	public boolean moveArmToRaw(double angle, boolean immidiateReturn) {
		if (Math.abs(angle) < swivelArmMaxHalfRange) {

			boolean dir = (angle - armMotor.getPosition() < 0);
			if (lastArmDir != dir) {
				if (dir) {
					angle = angle + armBackslash;
				}
			}
			lastArmDir = dir;
			armMotor.rotateTo((int) angle, immidiateReturn);
			// }
			LCD.drawChar('S', 10, 7);
			return true;
		} else {
			LCD.drawChar('F', 10, 7);
			return false;// out of bounds!
		}
	}

	/**
	 * Same as moveArm, immidiateReturn is false.
	 * 
	 * @param angle
	 * @return
	 */
	public boolean moveArm(double angle) {
		return moveArm(angle, false);
	}

	/**
	 * Specify in mm, positive = forward, negativ = backward
	 */
	public boolean moveWheel(double value, boolean immidiateReturn) {
		// TODO backslash compensation
		wheelMotor.rotate(Robot.distanceToWheelMotorAngle(value),
				immidiateReturn);
		return true;
	}

	/**
	 * Specify in motor angle (= raw), positive = forward, negativ = backward
	 */
	public boolean moveWheelToRaw(double value, boolean immidiateReturn) {
		// TODO backslash compensation
		boolean dir = (value - wheelMotor.getPosition() > 0);

		if (lastWheelDir != dir) {
			if (!dir) {
				value = value + wheelBackslash;
			}
		}
		lastWheelDir = dir;
		wheelMotor.rotateTo((int) value, immidiateReturn);
		return true;
	}

	/**
	 * Same as above, immidiateReturn variable is false by default
	 * 
	 * @param value
	 */
	public boolean moveWheel(double value) {
		return moveWheel(value, false);
	}

	public void stopMotion() {
		wheelMotor.stop();
		armMotor.stop();
	}

	/**
	 * reset kinematics wheel motor position is -80 relative to global
	 * coordinates. But 0 relative to the motor!
	 * 
	 */
	private void resetKinematics() {

		/**
		 * Tachometers reset
		 */
		wheelMotor.resetTachoCount();
		armMotor.resetTachoCount();

		globalLocation.setValue(0, 0);// global coords reset.
		double out[] = Robot.calculateInverseKinematics(globalLocation);
		// using this equation is reliable than manual setting
		// apply the inverse kinematics
		jointY = out[0];
		armTheta = out[1];
	}

	/**
	 * 
	 * @return
	 */
	public boolean calibrationRoutine() {
		LCD.drawString("Press Enter", 0, 0);
		LCD.drawString("to Start Calib.", 0, 1);
		Button.ENTER.waitForPressAndRelease();
		LCD.drawString("to Light Hi.", 0, 1);
		Button.ENTER.waitForPressAndRelease();
		lightSensor.calibrateHigh();
		LCD.drawString("LIGHT Sensor Lo", 0, 1);
		Button.ENTER.waitForPressAndRelease();
		lightSensor.calibrateLow();
		LCD.drawString("ready main-calib.", 0, 1);
		Button.ENTER.waitForPressAndRelease();

		calibratePen();
		movePen(false);

		calibrateArm();
		// LCD.clear();
		LCD.drawString("Going to initial Pos", 0, 1);
		LCD.clear();
		// Wheel gear backslash calculation
		wheelBackslash = (int) ((double) (getWheelBackslash()
				+ getWheelBackslash() + getWheelBackslash()) / 3);
		LCD.drawString("Backslash" + wheelBackslash, 0, 2);

		// move forward to align pen to 0.
		moveWheel(Robot.JOINT_TO_LIGHT_SENSOR - Robot.JOINT_TO_PEN);
		wheelMotor.stop();

		lastWheelDir = true;
		while (wheelMotor.isMoving() && armMotor.isMoving()) {

		}

		LCD.drawString(wheelMotor.getPosition() + " " + armMotor.getPosition(),
				0, 4);
		LCD.drawString("Calibration Done", 0, 1);

		Sound.beep();

		resetKinematics();
		updateForwardKinematics();

		// setup timer handler (listener)
		// mainTimerHandler = new MainTimerHandler();
		// mainTimerHandler.mainTimerStart();// start timer

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
		// armMotor.setAcceleration(ARM_MOTOR_CAL_ACC);
		armMotor.setSpeed(Robot.ARM_MOTOR_CAL_SPEED);// going slower
		// Get initial position. Assume this is center
		int swivelArmInitialPos = armMotor.getPosition();

		while (!armLimitSwitch.isPressed()) {
			armMotor.backward(); // replace with position(1)?
		}

		armMotor.stop();
		int halfRange = Math.abs(armMotor.getPosition() - swivelArmInitialPos);
		swivelArmMaxHalfRange = halfRange;

		LCD.drawString(
				"Max Range "
						+ (int) -Robot
								.motorAngleToArmAngle(swivelArmMaxHalfRange * 2),
				0, 0);

		/**
		 * Perform backslash calculation. Get value 3 times and average Since
		 * arm is "lagging" when going in anticlockwise dir, the angle must be
		 * added to the target angle. For clockwise, no backslash is added.
		 * 
		 * Use rotateArmToAngleCal() to rotate with backslash compensation
		 * added.
		 */

		armBackslash = (int) ((getArmBackslash() + getArmBackslash() + getArmBackslash()) / 3);

		// Goto center position.
		armMotor.rotate((halfRange - armBackslash));// -
													// Robot.armAngleToMotorAngle(armBackslash)
		lastArmDir = false;// turning to left is false, -> this rotation is to
							// left.
		return true;
	}

	/**
	 * Move to both extremes and find the center point which is pen down
	 * position.
	 */
	public void calibratePen() {
		// penMotor.setStallThreshold(5, 3);
		penMotor.setSpeed(200);

		// penMotor.rotate(10);
		while (!penLimitSwitch.isPressed()) {
			penMotor.forward();
		}
		// penMotor.rotate(-10);
		penMotor.resetTachoCount();// go forward for lowering pen.
	}

	/**
	 * Wheel/ y axis calibration. Main task: move pen to y=0 Secondary task:
	 * calculate backslash.
	 * 
	 */
	private double getWheelBackslash() {
		wheelMotor.setSpeed(Robot.WHEEL_MOTOR_CAL_SPEED);
		// drive until light sensor is hit.
		while (lightSensor.getNormalizedLightValue() > Robot.LIGHT_THRESHOLD) {
			LCD.drawString("L " + lightSensor.getNormalizedLightValue(), 0, 1);
			wheelMotor.backward();
		}

		wheelMotor.stop();
		int wheelBackslashInit = wheelMotor.getPosition();

		// Now bring the robot front so that the light sensor is at starting
		// edge.
		int previousValue = lightSensor.getNormalizedLightValue();

		// detect rising edge. (black to white transition
		while (!(previousValue < Robot.LIGHT_THRESHOLD && lightSensor
				.getNormalizedLightValue() > Robot.LIGHT_THRESHOLD)) {

			wheelMotor.forward();
			// LCD.drawInt(lightSensor.getNormalizedLightValue(), 4, 0, 1);
		}

		wheelMotor.stop();

		wheelBackslashInit = Math.abs(wheelMotor.getPosition()
				- wheelBackslashInit);

		return wheelBackslashInit;
	}

	//
	public void setWheelRawSpeed(double speed) {
		Robot.distanceToWheelMotorAngle(speed);
	}

	// mm/s
	public void setArmRawSpeed() {

	}

	public void resetActuatorSpeeds() {
		wheelMotor.setSpeed(Robot.WHEEL_MOTOR_MAX_SPEED);
		armMotor.setSpeed(Robot.ARM_MOTOR_MAX_SPEED);
	}

	public void setActuatorSpeeds(Coord start, Coord end) {

		double[] set1 = Robot.calculateInverseKinematics(start);
		double[] set2 = Robot.calculateInverseKinematics(end);

		double armRawRate = Math.abs(set2[1] - set1[1]);
		double wheelRawRate = Math.abs(set2[0] - set1[0]);

		if (armRawRate != 0) {// && wheelRawRate !=0
			double ratio = wheelRawRate / armRawRate;
			if (ratio < 1) {
				wheelMotor
						.setSpeed((float) (Robot.ARM_MOTOR_MAX_SPEED * ratio));
				armMotor.setSpeed((float) Robot.ARM_MOTOR_MAX_SPEED);
			} else {
				wheelMotor.setSpeed((float) (Robot.WHEEL_MOTOR_MAX_SPEED));
				armMotor.setSpeed((float) (Robot.WHEEL_MOTOR_MAX_SPEED * (1 / ratio)));
			}
		} else {// only wheel motor has to move
			wheelMotor.setSpeed(Robot.WHEEL_MOTOR_MAX_SPEED);
		}
	}

	/**
	 * Get arm backslash
	 * 
	 * @param position
	 * @return backslash in degrees
	 */
	private int getArmBackslash() {

		// Slower speeds.

		// armMotor.setAcceleration(ARM_MOTOR_CAL_ACC / 2);
		// armMotor.setSpeed(ARM_MOTOR_CAL_SPEED / 2);// going slower for
		// backslash

		while (!armLimitSwitch.isPressed()) {
			// LCD.drawString("Swinging the arm", 0,1);
			armMotor.backward(); // replace with position(1)?
		}
		armMotor.stop();

		int armBackslashInit = armMotor.getPosition();

		// TODO Better to do event listener
		while (armLimitSwitch.isPressed()) {
			// LCD.drawString("Swinging the arm", 0,1);
			armMotor.forward(); // replace with position(1)?
		}

		armBackslashInit = Math.abs(armBackslashInit - armMotor.getPosition());
		armMotor.stop();

		// normal speeds
		// armMotor.setAcceleration(4000);// defaults
		// armMotor.setSpeed(armMotorDefaultSpeed);// going slower for backslash

		return armBackslashInit;
	}
}
