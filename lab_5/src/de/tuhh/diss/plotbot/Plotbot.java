package de.tuhh.diss.plotbot;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;

public class Plotbot {
	private static boolean isCalibrated = false;

	public static void main(String[] args) {
		// Some example code to check if the build process works
//		LCD.drawString("Compiled successfully", 0, 0);
//		LCD.drawString("Good Luck!", 0, 1);
//		Button.ESCAPE.waitForPressAndRelease();
		
		PlotbotControl control= new PlotbotControl();

		if (isCalibrated) {
			// Call your MainMenu from here after you deleted the example code
			MainMenu myMainMenu = new MainMenu();
			Plottable s = myMainMenu.select();

			// TODO: Call methods to plot the selected shape. Be careful to not
			// draw anything, if the robot angles are not yet calibrated!
		} else {
			// TODO Calibration start
			
			isCalibrated= control.calibrationRoutine();
			MainMenu myMainMenu = new MainMenu();
			Plottable s = myMainMenu.select();
		}
//		LCD.drawString("Compiled successfully", 0, 0);
//		LCD.drawString("Good Luck!", 0, 1);
		Button.ESCAPE.waitForPressAndRelease();
	}

}
