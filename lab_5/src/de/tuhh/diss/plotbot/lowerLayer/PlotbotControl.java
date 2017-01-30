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

	/**
	 * Singleton model. This static object will be returned when asked for.
	 */
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
	private Coord globalLocation; // Location of the pen in global coordinates.

	private int armBackslash;// raw motor angles
	private int wheelBackslash;// raw motor angles

	/**
	 * timer and related variables
	 */

	private MainTimerHandler mainTimerHandler;

	/**
	 * Defaults and constants. All in Robot class.
	 */

	/**
	 * Timer listener
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
		armLimitSwitch = new TouchSensor(SensorPort.S1);
		penLimitSwitch = new TouchSensor(SensorPort.S2);

		wheelMotor = Motor.C;
		armMotor = Motor.A;
		penMotor = Motor.B;

		globalLocation = new Coord(0, 0);
		swivelArmMaxHalfRange = 0;
		armBackslash = 0;
		wheelBackslash = 0;
	}

	/**
	 * ========================================================================
	 * =================================================================
	 * Kinematics/ Localisation
	 */

	/**
	 * This method is called by the timer and update the location of the robot.
	 */
	private void updateForwardKinematics() {
		int currentArm = armMotor.getPosition();
		int currentWheel = wheelMotor.getPosition();

		// Calculate forward kinematics and update.
		globalLocation = Robot.calculateForwardKinematics(currentWheel,
				currentArm - swivelArmMaxHalfRange);
		LCD.drawString("X :" + (int) globalLocation.x() + " Y :"
				+ (int) globalLocation.y(), 0, 5);
		LCD.drawString(currentWheel + " " + currentArm, 0, 6);
	}

	/**
	 * Reset kinematics.
	 * 
	 * Call when Pen is at 0,0 position (Joint at 0, -80)
	 * 
	 * Joint position is y = -80 relative to global coordinates. But 0 relative
	 * to the motor!
	 * 
	 */
	private void resetKinematics() {
		// Tachometers reset
		wheelMotor.resetTachoCount();
		// armMotor.resetTachoCount();
		// global coords reset.
		globalLocation.setValue(0, 0);
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
	 * ========================================================================
	 * Movements
	 */

	/**
	 * Move pen up or down NEVER Run this without calibration!!!
	 * 
	 * @param down
	 *            if true, go down
	 */

	public void movePen(boolean down) {// down = true, go down
		if (!down) {// up = go to zero.
			while (!penLimitSwitch.isPressed()) {
				penMotor.forward();
			}
			penMotor.stop();
		} else {
			LCD.drawString(
					"pen" + (Robot.PEN_DOWN_ROTATION - penMotor.getPosition()),
					0, 1);
			// Calculate and move the motor to absolute -420 deg. This'll avoid
			// breaks.
			penMotor.rotate(Robot.PEN_DOWN_ROTATION - penMotor.getPosition());
			penMotor.stop();
		}
	}

	/**
	 * move to 0,0 After Calibrated, 0,0 = pen at 0,0
	 * 
	 */
	public void moveToStart() {
		moveTo(new Coord(0, 0), true);
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
	 * Goto a given coordinate
	 * 
	 * @param coord
	 *            destination
	 * @param immediateReturn
	 *            If true, Both actuators will activate at same time.
	 * @return success status. Fails if bounds exceed.
	 */

	public boolean moveTo(Coord coord, boolean immediateReturn) {
		double values[] = Robot.calculateInverseKinematics(coord);

		LCD.drawString("INV :" + (int) values[0] + " Y :"
				+ (int) values[1], 0, 7);
		
		if (values != null && Math.abs(values[1]) < swivelArmMaxHalfRange) {
			moveWheelToRaw(values[0], immediateReturn);//
			return moveArmToRaw(values[1], false);
		} else {
			return false;
		}
	}

	/**
	 * 1. Rotate arm in degrees (Arm angle*). Center is zero deg., clockwise
	 * from zero is positive.
	 * 
	 * 2. Performs backslash compensation and limits.
	 * 
	 * If counterclockwise, add backslash. No need to track last direction due
	 * to use of absolute positioning (rotateTo)
	 * 
	 * @param angle
	 *            arm motor angle (degrees)
	 * @param immediateReturn
	 *            will exit this method immediately after commanding to hardware
	 * @return true if movement succeed
	 */
	public boolean moveArmToRaw(double angle, boolean immediateReturn) {
		if (Math.abs(angle) < swivelArmMaxHalfRange) {// bound check
			if (angle - armMotor.getPosition() < 0) {
				angle = angle + armBackslash;
			}
			armMotor.rotateTo((int) angle + swivelArmMaxHalfRange,
					immediateReturn);

			// LCD.drawChar('S', 10, 7);
			return true;
		} else {
			// LCD.drawChar('F', 10, 7);
			return false;// out of bounds!
		}
	}

	/**
	 * 
	 * 
	 * @param value
	 *            value in motor angles positive = forward, negative = backward
	 * @param immediateReturn
	 *            will exit this method immediately after commanding to hardware
	 * @return true if motion succeed
	 */
	public boolean moveWheelToRaw(double value, boolean immediateReturn) {
		if ((value - wheelMotor.getPosition() < 0)) {// if true, going backward
														// = add backslash.
			value = value + wheelBackslash;
		}
		wheelMotor.rotateTo((int) value, immediateReturn);
		return true;
	}

	/**
	 * ========================================================================
	 * Movements - Speed control
	 */

	/**
	 * Reset motor speeds
	 * 
	 */
	public void resetActuatorSpeeds() {
		wheelMotor.setSpeed(Robot.WHEEL_MOTOR_MAX_SPEED);
		armMotor.setSpeed(Robot.ARM_MOTOR_MAX_SPEED);
	}

	/**
	 * Set motor speeds by giving start and end coordinates This method attempt
	 * to match the slope of a line created by the two coordinate by matching
	 * "speeds" of the two motors. The resulting motion will give a smoother
	 * line.
	 * 
	 * @param start
	 *            Start coordinate
	 * @param end
	 *            End coordinate
	 */
	public void setActuatorSpeeds(Coord start, Coord end) {

		double[] set1 = Robot.calculateInverseKinematics(start);
		double[] set2 = Robot.calculateInverseKinematics(end);

		// Get the angle each motor has to turn to travel from current position
		// to target.
		double armRawRate = Math.abs(set2[1] - set1[1]);
		double wheelRawRate = Math.abs(set2[0] - set1[0]);

		// We are getting a ratio. Avoid division by zero
		if (armRawRate != 0) {
			double ratio = wheelRawRate / armRawRate;// speed ratio
			if (ratio < 1) {
				// Arm speed is fixed, wheel motor speed is reduced as needed
				wheelMotor
						.setSpeed((float) (Robot.ARM_MOTOR_MAX_SPEED * ratio));
				armMotor.setSpeed((float) Robot.ARM_MOTOR_MAX_SPEED);
			} else {
				// Wheel speed is fixed, arm motor speed is reduced as needed

				wheelMotor.setSpeed((float) (Robot.WHEEL_MOTOR_MAX_SPEED));
				armMotor.setSpeed((float) (Robot.WHEEL_MOTOR_MAX_SPEED * (1 / ratio)));
			}
		} else {// only wheel motor has to move
			wheelMotor.setSpeed(Robot.WHEEL_MOTOR_MAX_SPEED);
		}
	}

	/**
	 * Stop all motion
	 */
	public void stopMotion() {
		wheelMotor.stop();
		armMotor.stop();
	}

	/**
	 * ========================================================================
	 * Calibration
	 */

	/**
	 * Calibration routine. Call this first
	 * 
	 * @return true is all is well
	 */
	public boolean calibrationRoutine() {

		LCD.drawString("Press Enter", 0, 0);
		LCD.drawString("to Start Calib.", 0, 1);
		Button.ENTER.waitForPressAndRelease();

		// calibrate light HI, place at a white place
		LCD.drawString("to Light Hi.", 0, 1);
		Button.ENTER.waitForPressAndRelease();
		lightSensor.calibrateHigh();

		// calibrate light Low, place at a black place
		LCD.drawString("LIGHT Sensor Lo", 0, 1);
		Button.ENTER.waitForPressAndRelease();
		lightSensor.calibrateLow();

		// Place the robot in starting pos.
		LCD.drawString("ready main-calib.", 0, 1);
		Button.ENTER.waitForPressAndRelease();

		// Calibrate the pen
		calibratePen();
		// lift the pen
		movePen(false);

		// Calibrate the arm
		calibrateArm();

		LCD.drawString("Going to initial Pos", 0, 1);
		LCD.clear();

		// Wheel gear backslash calculation
		wheelBackslash = (getWheelBackslash() + getWheelBackslash() + getWheelBackslash()) / 3;
		LCD.drawString(
				"Backslash" + Robot.motorAngleToWheelDistance(wheelBackslash),
				0, 2);

		// move forward to align pen to y=0.
		wheelMotor.rotate(Robot
				.distanceToWheelMotorAngle(Robot.JOINT_TO_LIGHT_SENSOR
						- Robot.JOINT_TO_PEN));
		wheelMotor.stop();

		while (wheelMotor.isMoving() && armMotor.isMoving()) {

		}

		LCD.drawString(wheelMotor.getPosition() + " " + armMotor.getPosition(),
				0, 4);
		LCD.drawString("Calibration Done", 0, 1);

		Sound.beep();

		resetKinematics();
		Delay.msDelay(500);
		updateForwardKinematics();

		wheelMotor.setSpeed(Robot.WHEEL_MOTOR_MAX_SPEED);
		armMotor.setSpeed(Robot.ARM_MOTOR_MAX_SPEED);
		// setup timer handler (listener)
//		mainTimerHandler = new MainTimerHandler();
//		mainTimerHandler.mainTimerStart();// start timer

		return true;
	}

	/**
	 * Calibrate the arm
	 * 
	 * @return true if all is well
	 */
	private boolean calibrateArm() {

		LCD.clear();
		LCD.drawString("Starting arm calibration", 0, 0);
		LCD.drawString("Starting arm swing", 0, 1);

		/**
		 * Slower speeds
		 */
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

		armMotor.resetTachoCount();

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
		armMotor.rotateTo((halfRange));
		return true;
	}

	/**
	 * Move to top position and reset counter. Assume the
	 */
	public void calibratePen() {
		// penMotor.setStallThreshold(5, 3);
		penMotor.setSpeed(200);

		while (!penLimitSwitch.isPressed()) {
			penMotor.forward();
		}
		penMotor.resetTachoCount();// go forward for lowering pen.
	}

	/**
	 * Wheel/ y axis calibration. Main task: move pen to y=0 Secondary task:
	 * calculate backslash.
	 * 
	 * @return backslash in motor angle
	 */
	private int getWheelBackslash() {
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

		// detect rising edge. (black to white transition)
		while (!(previousValue < Robot.LIGHT_THRESHOLD && lightSensor
				.getNormalizedLightValue() > Robot.LIGHT_THRESHOLD)) {
			wheelMotor.forward();
		}
		wheelMotor.stop();

		wheelBackslashInit = Math.abs(wheelMotor.getPosition()
				- wheelBackslashInit);

		return wheelBackslashInit;
	}

	/**
	 * Get arm backslash. It is good to run this several times and average the
	 * value
	 * 
	 * @return backslash in motor angle (degrees)
	 */
	private int getArmBackslash() {

		// 1. Move until switch is pressed

		while (!armLimitSwitch.isPressed()) {
			armMotor.backward();
		}
		armMotor.stop();

		int armBackslashInit = armMotor.getPosition();

		// 2. Move until switch is un-pressed 3.
		while (armLimitSwitch.isPressed()) {
			armMotor.forward();
		}
		// Take the difference as backslash.
		armBackslashInit = Math.abs(armBackslashInit - armMotor.getPosition());
		armMotor.stop();

		return armBackslashInit;
	}
}
