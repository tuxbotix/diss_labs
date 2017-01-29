package de.tuhh.diss.plotbot;

import de.tuhh.diss.plotbot.geometry.Coord;
import de.tuhh.diss.plotbot.lowerLayer.Robot;

public class kinematicTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Coord coord1 = new Coord(0, 0);
		double[] output = Robot.calculateInverseKinematics(new Coord(40, 20));
		System.out.println("1. input 0,0 : output :" + output[0] +" "+ output[1]);
		
		Coord out = Robot.calculateForwardKinematics(output[0],output[1]);
		System.out.println("1. forward :" + out.x() +  " "+out.y());
		
		output = Robot.calculateInverseKinematics(new Coord(0, 20));
		System.out.println("2. input 0,0 : output :" + output[0] +" "+ output[1]);
		
		out = Robot.calculateForwardKinematics(output[0],output[1]);
		System.out.println("2. forward :" + out.x() +  " "+out.y());
		
		out = Robot.calculateForwardKinematics(0,Robot.armAngleToMotorAngle(60));
		System.out.println("2. forward :" + out.x() +  " "+out.y());
		
		double[] output1 = Robot.calculateInverseKinematics(new Coord(70, 0));
		
		output = Robot.calculateInverseKinematics(new Coord(71, 0));
		System.out.println("2. input 0,0 : output :" + (output[0] - output1[0])+" "+ (output[1] - output1[0])+" "+Robot.distanceToWheelMotorAngle(1));
		
		
		
// 		System.out.println(
// 		Line line = new Line (new Coord(0,0), new Coord(70,100));
// 		Coord intercept = line.getNormalInteceptingPoint(new Coord(8,5));
// 		System.out.println("intercept"+intercept.x()+" "+ intercept.y());

//                 line.plot();
	}

}
