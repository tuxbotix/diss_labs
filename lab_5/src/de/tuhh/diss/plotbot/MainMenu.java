package de.tuhh.diss.plotbot;

import de.tuhh.diss.plotbot.geometry.*;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.util.ButtonCounter;
import lejos.util.TextMenu;

public class MainMenu {

	private static final String[] ITEMS = { "Anchor", "Rectangle", "Diamond",
			"line" };
	private static final String TITLE = "Choose Shape to draw:";
	private TextMenu menu;

	private final int Y_UPPER_BOUND = 230;
	private final int RECT_X_SHIFT = 20;
	private final int ANCHOR_X_SHIFT = -20;

	/**
	 * Creates a new MainMenu object.
	 */
	public MainMenu() {
		menu = new TextMenu(ITEMS, 1, TITLE);
	}

	/**
	 * Select a menu item
	 * 
	 * @return
	 */
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
			int width = getIntFromButtons("Width", 3);
			toDraw = new Anchor(width,ANCHOR_X_SHIFT);
			break;
		}

		case 1: {
			int width = getIntFromButtons("Width", 3);
			int height = getIntFromButtons("Height", 3);
			toDraw = new Rectangle(new Coord(RECT_X_SHIFT, Y_UPPER_BOUND
					- height), new Coord(RECT_X_SHIFT + width, Y_UPPER_BOUND));
		}
		
		case 2: {
			int width = getIntFromButtons("Width", 3);
			int height = getIntFromButtons("Height", 3);
			toDraw = new Diamond(new Coord(RECT_X_SHIFT, Y_UPPER_BOUND
					- height), new Coord(RECT_X_SHIFT + width, Y_UPPER_BOUND));
		}
		
		case 3:{
			toDraw = new Line(new Coord(-20, 60), new Coord(20, 20));
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
		LCD.clear(y);
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
