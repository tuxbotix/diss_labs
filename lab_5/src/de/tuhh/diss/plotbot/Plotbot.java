package de.tuhh.diss.plotbot;

import de.tuhh.diss.plotbot.geometry.Coord;
import de.tuhh.diss.plotbot.geometry.Plottable;
import de.tuhh.diss.plotbot.lowerLayer.PlotbotControl;
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

		isCalibrated = PlotbotControl.getInstance().calibrationRoutine();

		if (isCalibrated) {
			// Call your MainMenu from here after you deleted the example code
			MainMenu myMainMenu = new MainMenu();
			Plottable s = myMainMenu.select();
//
			if (s != null) {
//	
				PlotbotControl.getInstance().movePen(true);
				s.plot(PlotbotControl.getInstance());
				PlotbotControl.getInstance().movePen(false);
//				LCD.clear();
//				LCD.drawString("Move start", 0, 0);
				
//				PlotbotControl.getInstance().movePen(true);
//				s.plot(PlotbotControl.getInstance());
//				PlotbotControl.getInstance().movePen(false);
//
//				boolean p =PlotbotControl.getInstance().moveTo(new Coord(20, 100));
//				LCD.clear(0);
//				LCD.drawString("Move"+p, 0, 0);
//
//				PlotbotControl.getInstance().movePen(true);
//				PlotbotControl.getInstance().movePen(false);
			}
			
		} else {
			LCD.drawString("Calibration Failure", 0, 0);
		}
		Button.ESCAPE.waitForPressAndRelease();
		
//		PlotbotControl.getInstance().moveTo(new Coord(0,0));
		
	}
}
