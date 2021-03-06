package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StorageException;
import de.tuhh.diss.io.SimpleIO;

public class HarborStorageApp {

 // max input attempts for a given input - to avoid infinite loop.
	private static final int INPUT_ATTEMPTS = 5;
	private static HarborStorageManagement hsm;

	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		hsm = new HarborStorageManagement();// creates HarborStorageManagement instance
		SimpleIO.println("\nWelcome to TUHH/DISS Harbor Storage Management");
		mainMenu();
	}

	/**
	 * Main menu. Called when main menu is to be displayed.
	 */
	public static void mainMenu() {
		SimpleIO.println();
		SimpleIO.println("*** Main Menu ***");
		SimpleIO.println("0: Quit Program");
		SimpleIO.println("1: Store a packet in the highbaystorage");
		SimpleIO.println("2: Retrieve a packet by description from the highbaystorage. Retrieves first match");
		SimpleIO.println("3: List and Retrieve packet from selection from the highbaystorage");
		SimpleIO.println();
		int choice = readZeroOrPositiveIntInLoop("Your choice:"); 
		SimpleIO.println();
		choice(choice);
	}

	/**
	 * Called when a choice is given. Choice 1. Store a packet Choice 2.
	 * Retrieve a packet by description Choice 3. Retrieve a packet by ID
	 * 
	 * @param choice
	 *            int that should be in range of 0-3, otherwise will go back to
	 *            main menu.
	 */
	public static void choice(int choice) {
		switch (choice) {
		case 0:// when choice is zero, the system is shutdown
			hsm.shutdown();
			break;

		case 1: {//when the choice is 1 ,the packet is stored
			storePacketCase(); 
			break;
		}
		case 2: {
			getPacketCase(true);//when the choice is 2, the packet is retrieved by its description
			break;
		}
		case 3: {
			getPacketCase(false);//when the choice is 2, the packet is retrieved by its entry number
			break;
		}
		default: { // when a number other than the given choice is entered,it displays an error and returns to the main menu
			SimpleIO.println("Invalid choice.");
			mainMenu();
			break;
		}
		}
	}

	/**
	 * Stores the packet.Validates its dimensions and stores it.
	 */
	private static void storePacketCase() {
		SimpleIO.println("*** Store a packet ***");
		String description = readNonEmptyStringInLoop("Description :");//description of the new packet and it cannot be a empty string
		int width = readGreaterThanZeroIntInLoop("Width :");//width of the packet and the number should be greater than zero
		int height = readGreaterThanZeroIntInLoop("Height : ");//height of the packet and the number should be greater than zero
		int depth = readGreaterThanZeroIntInLoop("Depth : ");//depth of the packet and the number should be greater than zero
		int weight = readGreaterThanZeroIntInLoop("Weight : ");//weight of the packet and the number should be greater than zero

		SimpleIO.println("You entered a packet \"" + description
				+ "\" of size " + width + "x" + height + "x" + depth
				+ " and weight " + weight); //displays information about the packet given by the user
		String choose = readNonEmptyStringInLoop("Shall we store the packet?(y/n)");// Get string -> response of user after printing "shall we store..."
		// message.
		if (choose.equalsIgnoreCase("y")) {// Allow "Y" or "y", other choices will skip storage and revert to main
			// menu.
			try {
				hsm.storePacket(width, height, depth, description, weight);
			} catch (StorageException e) {
				SimpleIO.println(e.getMessage());
			}
		}
		// After everything is done (Yes or no), goes back to main menu
		mainMenu();
	}

	/**
	 * Retrieve a packet by description or ID. If the boolean is true, it'll
	 * print and ask for description, Else it'll print and ask for ID. Kept both
	 * cases in same function as there are similar stuff in both.
	 * 
	 * @param isGetByDesc
	 *            Check if it is get by description or ID.
	 */
	private static void getPacketCase(boolean isGetByDesc) {
		Packet packets[] = hsm.getPackets();// Get currently stored packets
		if (packets.length > 0) {// if there is at least one packet
			SimpleIO.println("Available packets:");

			// print packet list
			for (int i = 0; i < packets.length; i++) {
				SimpleIO.println((i + 1) + ": Packet \""
						+ packets[i].getDescription() + "\" ID: "
						+ packets[i].getId() + " Size: "
						+ packets[i].getWidth() + "x" + packets[i].getHeight()
						+ "x" + packets[i].getDepth() + " Weight: "
						+ packets[i].getWeight());
			}
			// if get packet by description
			if (isGetByDesc) {
				SimpleIO.println();
				String description = readNonEmptyStringInLoop("*** Enter description of packet to be retrieved (0=Abort) ***\n");
				if (!description.equals("0")) { // if description is equal to 0, then abort
					try {
						hsm.retrievePacket(description);
					} catch (StorageException e) {
						SimpleIO.println(e.getMessage());
					}
				} else {
					SimpleIO.println("Operation aborted by user.");
				}
			} else {// get packet by ID
				SimpleIO.println();
				int entryNum = readZeroOrPositiveIntInLoop("*** Enter packet ID from the list (0=Abort) ***\n");

				if (entryNum != 0) {// if not zero, proceed, else abort
					try {
						hsm.retrievePacketById(entryNum);
					} catch (StorageException e) {
						SimpleIO.println(e.getMessage());
					}
				} else {
					SimpleIO.println("Operation aborted by user.");
				}
			}
		} else {
			SimpleIO.println("No packets stored in the system.");
		}
		mainMenu();
	}

	/**
	 * NOTE: This and readZeroOrPositiveIntInLoop could be cooked into a single
	 * method However, to make the meaning clean to user, it was avoided.
	 * 
	 * Print the input message and ask user to give a greater than zero int See
	 * readNonEmptyStringInLoop() for comments on logic Try upto INPUT_ATTEMPTS
	 * limit and exit to main menu on failure.
	 * 
	 * @param requestMessage
	 *            - message to be printed before asking for input. ex: "width: "
	 * @return user input after verification.
	 */
	private static int readGreaterThanZeroIntInLoop(String requestMessage) {
		int intValue = 0;
		boolean fail = false;
		int attempts = 0;
		do {
			attempts++;// count attempts
			SimpleIO.print(requestMessage);// print the given message, then read the input.
			try {
				intValue = SimpleIO.readInt();
				if (intValue > 0) {// check if readed value is greater than zero
					break;
				} else {
					fail = true;
					SimpleIO.println("Invalid value entered. Please enter a number greater than zero");
				}
			} catch (RuntimeException e) {
				SimpleIO.println("Invalid value entered. Please enter a valid number");
				fail = true;
			}
		} while (fail && attempts < INPUT_ATTEMPTS);
		if (attempts >= INPUT_ATTEMPTS) {
			SimpleIO.println(INPUT_ATTEMPTS
					+ " Attempts failed. Returning to main menu.");
			mainMenu();
		}

		return intValue;
	}

	/**
	 * Print the input message and ask user to give a zero or positive int See
	 * readNonEmptyStringInLoop() for comments Try upto INPUT_ATTEMPTS limit and
	 * exit to main menu on failure.
	 * 
	 * @param requestMessage
	 *            - message to be printed before asking for input. ex: "width: "
	 * @return user input after verification.
	 */
	private static int readZeroOrPositiveIntInLoop(String requestMessage) {
		int intValue = 0;
		boolean fail = false;// a flag to keep the loop running.
		int attempts = 0;
		do {
			attempts++;// to count the number of  attempts
			SimpleIO.print(requestMessage);// print the given message and then read the input.
			try {
				intValue = SimpleIO.readInt();
				if (intValue < 0) {
					fail = true;
					SimpleIO.println("Invalid value entered. Please enter a positive number");
				} else {
					break;
				}
			} catch (RuntimeException e) {
				SimpleIO.println("Invalid value entered. Please enter a valid number");
				fail = true;
			}
		} while (fail && attempts < INPUT_ATTEMPTS);
		if (attempts >= INPUT_ATTEMPTS) {
			SimpleIO.println(INPUT_ATTEMPTS
					+ " Attempts failed. Returning to main menu.");
			mainMenu();
		}
		return intValue;
	}

	/**
	 * Print the input message and ask user to give a non-empty string. Try upto
	 * INPUT_ATTEMPTS limit and exit to main menu on failure.
	 * 
	 * @param requestMessage
	 *            - message to be printed before asking for input. ex: "width: "
	 * @return user input after verification.
	 */
	private static String readNonEmptyStringInLoop(String requestMessage) {
		String strValue = "";
		boolean fail = true;// a flag to keep the loop running.
		int attempts = 0;// attempt counter
		// do while - try once and repeat if fail
		do {
			attempts++;// count attempts
			SimpleIO.print(requestMessage);// print the given message, then read the input.
			try {
				strValue = SimpleIO.readString();// read input
				if (strValue.isEmpty()) {// check if empty
					fail = true;
					SimpleIO.println("No data entered. Please enter a valid value");
				} else {// if not empty, break the loop which will return the
						// data.
					fail = false;
					break;
				}
			} catch (RuntimeException e) {// catch runtime errors -> wrong
											// values, etc.
				SimpleIO.println("No data entered. Please enter a valid value");
				fail = true;
			}
		} while (fail && attempts < INPUT_ATTEMPTS);

		if (fail) {// upon exit of loop, if fail = true, then loop exited with
					// limit count exceeding.
			SimpleIO.println(INPUT_ATTEMPTS + " Attempts failed.");
			mainMenu();
		}
		return strValue;
	}
}