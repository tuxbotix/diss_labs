package de.tuhh.diss.plotbot.lowerLayer;

import de.tuhh.diss.plotbot.geometry.Coord;
import lejos.nxt.LCD;

/**
 * Contains all constants, forward/ inverse kinematic related calculations.
 * Modify this class and PlotbotControl class when changing robot/ configuration
 * 
 * Some of the functionality Coord class supposed to have is ported to this
 * class. Reason : Seperation of geometric primitives, operations from robot's
 * kinematics
 * 
 * @author darshana
 * 
 */
public class Robot {
	public static final int LIGHT_THRESHOLD = 500;// robot 3 600
	public static final int WHEEL_GEAR_RATIO = 5;
	public static final int ARM_GEAR_RATIO = 84;
	public static final int JOINT_TO_LIGHT_SENSOR = 105;
	public static final int JOINT_TO_PEN = 80;
	public static final int WHEEL_DIAMETER = 56;

	// At worst case, 19deg per mm at arm rotation limits.(for motor)
	// 10 deg per mm for wheel motors;
	// So; setting speed for wheel motors to be half of arm motors will kind of
	// equalize things.
	public static final float ARM_MOTOR_MAX_SPEED = 200;
	public static final float ARM_MOTOR_CAL_SPEED = 300;
	public static final float WHEEL_MOTOR_MAX_SPEED = ARM_MOTOR_MAX_SPEED - 100;
	public static final float WHEEL_MOTOR_CAL_SPEED = ARM_MOTOR_CAL_SPEED / 2;
	public static final int ARM_MOTOR_CAL_ACC = 3000;
	public static final int WHEEL_MOTOR_ACC = 4000;
	
	public static final int PEN_DOWN_ROTATION = -420;// rotation needed to lower the pen from top.

	public static final int TIMER_FREQ = 30; // in Hertz
	private static final int Y_AXIS_MIN = 0;
	private static final int Y_AXIS_MAX = 250;// 250mm

	public static final double DISTANCE_INCREMENT = 3;
	public static final double DEAD_BAND = 1;

	// maximum accuracy enforced to avoid oscillations and other issues.

	/**
	 * Convert arm angle to Motor angle
	 * 
	 * @param angle
	 *            of arm. Use double for precision
	 * @return angle for motor in degrees, type int (motor accept int type)
	 */
	public static int armAngleToMotorAngle(double angle) {
		return (int) -angle * ARM_GEAR_RATIO;
	}

	/**
	 * Convert motor angle to arm angle
	 * 
	 * @param Motor
	 *            angle
	 * @return Arm angle
	 */
	public static double motorAngleToArmAngle(double angle) {
		return -angle / ARM_GEAR_RATIO;
	}

	/**
	 * Convert distance to motor angle (for wheels)
	 * 
	 * @param angle
	 * @return
	 */
	public static int distanceToWheelMotorAngle(double distance) {
		return (int) (distance * 360 * WHEEL_GEAR_RATIO / (Math.PI * WHEEL_DIAMETER));
	}

	/**
	 * Convert motor angle to distance in mm (for wheels)
	 * 
	 * @param angle
	 * @return
	 */
	public static double motorAngleToWheelDistance(double angle) {
		return (angle / WHEEL_GEAR_RATIO) / 360 * Math.PI * WHEEL_DIAMETER;
	}

	/**
	 * Forward kinematics -> convert motor positions to pen's position in global
	 * coordinates
	 * 
	 * 1. Get Motor positions for both wheel and arm motors. (Both were reset to
	 * zero on initial position = 0,0
	 * 
	 * 2. convert motor angles to arm angle and wheel travel
	 * 
	 * 3. Trigonometry!
	 * 
	 * 4. Distance travelled by wheels is given relative to initial joint
	 * position (0,-80 on global coordinates) so we convert to global
	 * coordinates by subtracting the arm length.
	 * 
	 * @param jointY
	 *            Distance motor travelled since calibration (motor position
	 *            reset on calibration)
	 * @param armAngle
	 * @return
	 */

	public static Coord calculateForwardKinematics(double jointY,
			double armAngle) {

		double x = Math
				.sin(Math.toRadians(Robot.motorAngleToArmAngle(armAngle)))
				* JOINT_TO_PEN;
		double y = Math
				.cos(Math.toRadians(Robot.motorAngleToArmAngle(armAngle)))
				* JOINT_TO_PEN
				+ Robot.motorAngleToWheelDistance(jointY)
				- JOINT_TO_PEN;
		return new Coord(x, y);
	}

	/**
	 * 
	 * @param coord
	 * @return
	 */
	public static double[] calculateInverseKinematics(Coord coord) {
		double motorPos[] = new double[2];// 0 = wheel motor, 1 = arm motor

		// 1. Circle to line intersection (line lies on y axis; x=0)
		// (x - coord_x)^2 + (y - coord_y)^2 = R^2; R = JOINT_TO_PEN
		// x=0;
		// coord_x^2 + (y - coord_y)^2 = R^2
		// y^2 - 2y*coord_y + coord_x^2 + coord_y^2 - R^2 =0
		// Use ( B +- sqrt(B^2 - 4AC) )/ 2 to solve for y
		// B = -2*coord_y
		// C = coord_y^2 + coord_x^2 - R^2
		//
		// if B^2 - 4AC <0, no intersection.

// 		if (coord.x() != 0) {// small optimization
                double b = -2 * coord.y();
                double c = Math.pow(coord.y(), 2) + Math.pow(coord.x(), 2)
                                - Math.pow(JOINT_TO_PEN, 2);

                double discriminant = Math.pow(b, 2) - 4 * c;

                // if the circle don't intersect or become tangent, we don't
                // continue further as the
                // arm cannot reach it!!
                if (discriminant <= 0) {
                        return null;
                }

                double x = 0;
                double y1 = (-b + Math.sqrt(discriminant)) / 2;

                double y2 = (-b - Math.sqrt(discriminant)) / 2;

                // get angle to from x axis to the line drawn from solution to
                // target point.
                double theta1 = Math.atan2(coord.y() - y1, coord.x());
                double theta2 = Math.atan2(coord.y() - y2, coord.x());

                theta1  = 90 - Math.toDegrees(theta1); // convert to degrees, shift to measuring from y axis
                theta2  = 90 - Math.toDegrees(theta2);
                                
                if (theta1 < theta2) {// use theta 1 -> first result
                        motorPos[0] = y1 + JOINT_TO_PEN;
                        motorPos[1] = theta1;
                } else {
                        motorPos[0] = y2 + JOINT_TO_PEN;
                        motorPos[1] = theta2;
                }

// 		System.out.println("sol:" + motorPos[0] + " " + motorPos[1]);

		motorPos[0] = Robot.distanceToWheelMotorAngle(motorPos[0]);
		motorPos[1] = Robot.armAngleToMotorAngle(motorPos[1]);
		// LCD.clear(7);
		// LCD.drawString("Kin."+(int)motorPos[0]+ " "+ (int)motorPos[1], 0, 7);
		return motorPos;
	}
}