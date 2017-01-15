package de.tuhh.diss.plotbot;

import lejos.nxt.Button;
import lejos.util.TextMenu;

public class MainMenu {

	private static final String[] ITEMS = {"Anchor"};	//add new text menu entries here
	private	static final String TITLE = "Choose Shape to draw:";
	private TextMenu menu;

	/**
	 * Creates a new MainMenu object.
	 */
	public MainMenu() {
		menu = new TextMenu(ITEMS, 1, TITLE);
	}
	public Plottable select(){
		int selection = -1;
		do {
			selection = menu.select();
		}while(selection < 0);

		while(Button.ENTER.isDown()) {
		}
		Plottable toDraw = null;
		if (selection == 0) {
			toDraw = new Anchor();
			//Think about what you have to do to start the drawing routine
		}
		return toDraw;
	}
}
