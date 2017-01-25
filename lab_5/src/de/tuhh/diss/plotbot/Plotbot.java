package de.tuhh.diss.plotbot;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;

public class Plotbot {
	private static boolean isCalibrated = false;

	public static void main(String[] args) {
		// Some example code to check if the build process works
		// LCD.drawString("Compiled successfully", 0, 0);
		// LCD.drawString("Good Luck!", 0, 1);
		// Button.ESCAPE.waitForPressAndRelease();

		if (isCalibrated) {
			// Call your MainMenu from here after you deleted the example code
			MainMenu myMainMenu = new MainMenu();
			Plottable s = myMainMenu.select();

			// TODO: Call methods to plot the selected shape. Be careful to not
			// draw anything, if the robot angles are not yet calibrated!
		} else {
			// TODO Calibration start

			isCalibrated = PlotbotControl.getInstance().calibrationRoutine();
			MainMenu myMainMenu = new MainMenu();
			Plottable s = myMainMenu.select();
			if (s != null) {
				
				PlotbotControl.getInstance().movePen(true);
				PlotbotControl.getInstance().movePen(false);
				
				PlotbotControl.getInstance().moveTo(new Coord(40, 100));
				
				PlotbotControl.getInstance().movePen(true);
				PlotbotControl.getInstance().movePen(false);
			}
		}
		// LCD.drawString("Compiled successfully", 0, 0);
		// LCD.drawString("Good Luck!", 0, 1);
		Button.ESCAPE.waitForPressAndRelease();
	}

}
