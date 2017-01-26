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
		double[] output = Robot.calculateInverseKinematics(new Coord(0, 0));
		System.out.println("input 0,0 : output :" + output[0] + output[1]);
		
		output = Robot.calculateInverseKinematics(new Coord(40, 100));
		System.out.println("input 40,100 : output :" + output[0] + output[1]);
		
	}

}
