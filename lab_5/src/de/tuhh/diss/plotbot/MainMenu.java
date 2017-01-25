package de.tuhh.diss.plotbot;

import de.tuhh.diss.plotbot.geometry.Anchor;
import de.tuhh.diss.plotbot.geometry.Plottable;
import de.tuhh.diss.plotbot.geometry.Rectangle;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.util.ButtonCounter;
import lejos.util.TextMenu;

public class MainMenu {

	private static final String[] ITEMS = { "Anchor", "Rectangle", "Line" };
	private static final String TITLE = "Choose Shape to draw:";
	private TextMenu menu;

	/**
	 * Creates a new MainMenu object.
	 */
	public MainMenu() {
		menu = new TextMenu(ITEMS, 1, TITLE);
	}

	public Plottable select() {
		int selection = -1;
		do {
			selection = menu.select();
		} while (selection < 0);

		while (Button.ENTER.isDown()) {

		}

		Plottable toDraw = null;
	
		switch (selection) {

		case 0: {// anchor
			getIntFromButtons("Width", 3);
			toDraw = new Anchor();
		}

		case 1: {
			getIntFromButtons("Width", 3);
			toDraw = new Rectangle();			
		}

		default: {

		}
		}
		return toDraw;
	}

	/**
	 * Display value from 0 to 250
	 * 
	 * @return
	 */
	private int getIntFromButtons(String message, int y) {
		int number = 0;
		LCD.drawString(message, 0, y);

		while (!Button.ENTER.isDown()) {
			if (Button.LEFT.isDown()) {
				number++;
				LCD.drawInt(number, 8, y);
			} else if (Button.LEFT.isDown() && number < 250) {
				number--;
				LCD.drawInt(number, 8, y);
			}
		}

		while (Button.ENTER.isDown()) {

		}

		return number;
	}
}
