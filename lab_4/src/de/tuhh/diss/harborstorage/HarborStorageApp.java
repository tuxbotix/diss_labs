package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StorageException;
import de.tuhh.diss.io.SimpleIO;

public class HarborStorageApp {
	private static HarborStorageManagement hsm;

	public static void main(String[] args) {
		hsm = new HarborStorageManagement();
		System.out.println("\nWelcome to TUHH/DISS Harbor Storage Management");
		mainMenu();
	}

	public static void mainMenu() {
		SimpleIO.println();
		System.out.println("*** Main Menu ***");
		System.out.println("0: Quit Program");
		System.out.println("1: Store a packet in the highbaystorage");
		System.out
				.println("2: Retrieve a packet by description from the highbaystorage. Retrieves first match");
		System.out
				.println("3: List and Retrieve packet from selection from the highbaystorage");
		SimpleIO.println();
		int choice = readZeroOrPositiveIntInLoop("Your choice:");
		SimpleIO.println();
		choice(choice);

	}

	public static void choice(int choice) {
		switch (choice) {
		case 0:
			hsm.shutdown();
			break;

		case 1: {
			storePacketCase();
			break;
		}

		case 2: {
			getPacketCase(true);// get by description
			break;
		}
		case 3: {
			getPacketCase(false);// get by entry number
			break;
		}
		default: {
			mainMenu();
			break;
		}
		}
	}

	private static void storePacketCase() {
		System.out.println("*** Store a packet ***");
		String description = readNonEmptyStringInLoop("Description :");
		int width = readGreaterThanZeroIntInLoop("Width :");
		int height = readGreaterThanZeroIntInLoop("Height : ");
		int depth = readGreaterThanZeroIntInLoop("Depth : ");
		int weight = readGreaterThanZeroIntInLoop("Weight : ");

		SimpleIO.println("You entered a packet \"" + description
				+ "\" of size " + width + "x" + height + "x" + depth
				+ " and weight " + weight);
		// System.out.print("Shall we store the packet?(y/n)");
		String choose = readNonEmptyStringInLoop("Shall we store the packet?(y/n)");
		if (choose.equalsIgnoreCase("y")) {
			try {
				hsm.storePacket(width, height, depth, description, weight);
			} catch (StorageException e) {
				SimpleIO.println(e.getMessage());
				// e.printStackTrace();
			}
		}
		mainMenu();
	}

	private static void getPacketCase(boolean isGetByDesc) {
		Packet packets[] = hsm.getPackets();

		if (packets.length > 0) {// if there is at least one packet
			System.out.println("Available packets:");
			for (int i = 0; i < packets.length; i++) {
				System.out.println((i + 1) + ": Packet \""
						+ packets[i].getDescription() + "\" ID: "+packets[i].getId()+" Size: "
						+ packets[i].getWidth() + "x" + packets[i].getHeight()
						+ "x" + packets[i].getDepth() + " Weight: "
						+ packets[i].getWeight());
			}
			if (isGetByDesc) {
				// System.out.print("*** Enter description of packet to be retrieved (0=Abort) ***");
				SimpleIO.println();
				String description = readNonEmptyStringInLoop("*** Enter description of packet to be retrieved (0=Abort) ***\n");
				if (!description.equals("0")) {
					try {
						hsm.retrievePacket(description);
					} catch (StorageException e) {
						SimpleIO.println(e.getMessage());
					}
				}else{
					SimpleIO.println("Operation aborted by user.");
				}
			} else {
				SimpleIO.println();
				int entryNum = readZeroOrPositiveIntInLoop("*** Enter packet ID from the list (0=Abort) ***\n");
				if (entryNum != 0) {
					try {
						hsm.retrievePacketById(entryNum);
					} catch (StorageException e) {
						SimpleIO.println(e.getMessage());
					}
				}else{
					SimpleIO.println("Operation aborted by user.");
				}
			}
		} else {
			SimpleIO.println("No packets stored in the system.");
		}
		mainMenu();
	}

	private static int readGreaterThanZeroIntInLoop(String requestMessage) {
		int intValue = 0;
		boolean fail = false;
		do {
			SimpleIO.print(requestMessage);
			try {
				intValue = SimpleIO.readInt();
				if (intValue <= 0) {
					fail = true;
					SimpleIO.println("Invalid value entered. Please enter a number greater than zero");
				} else {
					break;
				}
			} catch (RuntimeException e) {
				SimpleIO.println("Invalid value entered. Please enter a valid number");
				fail = true;
			}
		} while (fail);

		return intValue;
	}
	
	private static int readZeroOrPositiveIntInLoop(String requestMessage) {
		int intValue = 0;
		boolean fail = false;
		do {
			SimpleIO.print(requestMessage);
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
		} while (fail);

		return intValue;
	}

	private static String readNonEmptyStringInLoop(String requestMessage) {
		String strValue = "";
		boolean fail = false;
		do {
			SimpleIO.print(requestMessage);
			try {
				strValue = SimpleIO.readString();
				if (strValue.equals("")) {
					fail = true;
					SimpleIO.println("No data entered. Please enter a valid value");
				} else {
					break;
				}
			} catch (RuntimeException e) {
				SimpleIO.println("No data entered. Please enter a valid value");
				fail = true;
			}
		} while (fail);

		return strValue;
	}
}