package de.tuhh.diss.plotbot;

import de.tuhh.diss.plotbot.geometry.Coord;
import de.tuhh.diss.plotbot.geometry.Diamond;
import de.tuhh.diss.plotbot.geometry.Plottable;
import de.tuhh.diss.plotbot.lowerLayer.PlotbotControl;
import de.tuhh.diss.plotbot.lowerLayer.Robot;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.util.Delay;

public class Plotbot {
	private static boolean isCalibrated = false;

	public static void main(String[] args) {
		// Some example code to check if the build process works
		// LCD.drawString("Compiled successfully", 0, 0);
		// LCD.drawString("Good Luck!", 0, 1);
		// Button.ESCAPE.waitForPressAndRelease();

		isCalibrated = PlotbotControl.getInstance().calibrationRoutine();
		// PlotbotControl.getInstance().calibratePen();
		// PlotbotControl.getInstance().movePen(true);
		// PlotbotControl.getInstance().movePen(false);
		if (isCalibrated) {
			// Call your MainMenu from here after you deleted the example code
			MainMenu myMainMenu = new MainMenu();
			Plottable s = myMainMenu.select();
			//
			if (s != null) {
				//
				//
				// PlotbotControl.getInstance().moveArm(0);
				// Delay.msDelay(500);
				// LCD.drawString("test 2, 40d", 0,3);
				// PlotbotControl.getInstance().moveArmToRaw(Robot.armAngleToMotorAngle(55),
				// false);
				// LCD.drawString(Motor.A.getPosition()+"armpos", 0,3);
				// Delay.msDelay(2000);
				// LCD.drawString("test 3, -40d", 0,3);
				// PlotbotControl.getInstance().moveArmToRaw(Robot.armAngleToMotorAngle(-40),false);
				// LCD.drawString(Motor.A.getPosition()+"armpos", 0,3);
				//
				// Delay.msDelay(500);
				// LCD.drawString("test 4, 0,0", 0,3);
				// PlotbotControl.getInstance().movePen(true);
				//
				// PlotbotControl.getInstance().moveTo(new Coord(0,0));
				// Delay.msDelay(500);
				// LCD.drawString("test 5, 50,0", 0,3);
				// PlotbotControl.getInstance().moveTo(new Coord(50,0));
				// Delay.msDelay(500);
				// LCD.drawString("test 6, 0,0", 0,3);
				// PlotbotControl.getInstance().moveTo(new Coord(0,0));
				// Delay.msDelay(500);
				// LCD.drawString("test 7, 0,50", 0,3);
				// PlotbotControl.getInstance().moveTo(new Coord(0,50));
				//
				// Delay.msDelay(500);
				// LCD.drawString("test 8, 0,0", 0,3);
				// PlotbotControl.getInstance().moveTo(new Coord(0,0));
				//
				// PlotbotControl.getInstance().movePen(false);

				s.plot(PlotbotControl.getInstance());

				/**
				 * In order to plot both rectangle and diamond First diamond's
				 * plot will plot a diamond by default Then we check if s is an
				 * instance of Diamond. If so, it'll fetch the bounding box
				 * (Rectangle) and plot it.
				 * 
				 */
				if (s instanceof Diamond) {
					((Diamond) s).getBoundingBox().plot(
							PlotbotControl.getInstance());
				}
				// PlotbotControl.getInstance().movePen(false);
				// LCD.clear();
				// LCD.drawString("Move start", 0, 0);

				// PlotbotControl.getInstance().movePen(true);
				// s.plot(PlotbotControl.getInstance());
				// PlotbotControl.getInstance().movePen(false);
				//

				// boolean p =PlotbotControl.getInstance().moveTo(new Coord(20,
				// 20));
				// LCD.clear(0);
				// LCD.clear(1);
				// LCD.drawString("Move"+p, 0, 0);
				// p =PlotbotControl.getInstance().moveTo(new Coord(30, 100));
				// LCD.drawString("Move"+p, 0, 1);
				//
				//
				//
				// PlotbotControl.getInstance().movePen(true);
				// PlotbotControl.getInstance().movePen(false);
			}

		} else {
			LCD.drawString("Calibration Failure", 0, 0);
		}
		Button.ESCAPE.waitForPressAndRelease();

		PlotbotControl.getInstance().moveTo(new Coord(0, 0));

	}
}
