package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StorageException;
import de.tuhh.diss.io.SimpleIO;

public class HarborStorageApp {
	private static HarborStorageManagement hsm;

	public static void main(String[] args) {
		hsm = new HarborStorageManagement();
		mainMenu();
	}

	public static void mainMenu() {
		System.out.println("Welcome to TUHH/DISS Harbor Storage Management");
		System.out.println("*** Main Menu ***");
		System.out.println("0: Quit Program");
		System.out.println("1: Store a packet in the highbaystorage");
		System.out.println("2: Retrieve a packet from the highbaystorage");
		System.out.print("Your choice:");
		int choice = SimpleIO.readInt();
		choice(choice);
	}

	public static void choice(int choice) {
		switch (choice) {
			case 0:
				hsm.shutdown();
				break;
	
			case 1: {
				System.out.println("*** Store a packet ***");
				System.out.print("Description :");
				String description = SimpleIO.readString();
				System.out.print("Width :");
				int width = SimpleIO.readInt();
				System.out.print("Height :");
				int height = SimpleIO.readInt();
				System.out.print("Depth :");
				int depth = SimpleIO.readInt();
				System.out.print("Weight :");
				int weight = SimpleIO.readInt();
				System.out.println("You entered a packet \"" + description + "\" of size " + width + "X" + height + "X"
						+ depth + " and weight " + weight);
				System.out.print("Shall we store the packet?(y/n)");
				String choose = SimpleIO.readString();
				if (choose.equalsIgnoreCase("y")) {
					try {
						hsm.storePacket(width, height, depth, description, weight);
					} catch (StorageException e) {
	
						e.printStackTrace();
					}
				}
				mainMenu();
				break;
			}
	
			case 2: {
				Packet packets[] = hsm.getPackets();
				System.out.println("Available packets:");
				int i = 0;
				for (i = 0; i <= packets.length; i++) {
					System.out.println(+i + ": Packet" + packets[i].getDescription() + "size:" + packets[i].getWidth() + "X"
							+ packets[i].getHeight() + "X" + packets[i].getDepth() + "weight:" + packets[i].getWeight());
				}
				System.out.print("*** Enter description of packet to be retrieved (0=Abort) ***");
				String description = SimpleIO.readString();
				try {
					hsm.retrievePacket(description);
				} catch (StorageException e) {
					e.printStackTrace();
				}
				mainMenu();
				break;
			}
			default: {
				mainMenu();
				break;
			}
		}
	}
}