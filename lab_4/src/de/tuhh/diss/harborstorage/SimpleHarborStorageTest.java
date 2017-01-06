package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.HarborStorageManagement;
import de.tuhh.diss.harborstorage.sim.*;
import de.tuhh.diss.io.SimpleIO;

/*
 * This class performs two simple tests. The first one tries to store a packet that is too large and heavy for your rack.
 * The second test iteratively adds small packets to your rack until it is full.
 * 
 * Extend this file by adding more test cases to avoid extensive testing via the command line.
 */

public class SimpleHarborStorageTest {

	private static HarborStorageManagement hsm;

	private static boolean testTooLargePacket() {
		boolean refuse = false;
		int width = 10;
		int height = 10;
		int depth = 10;
		int weight = 500;
		String description = "Really Large And Heavy Packet";
		SimpleIO.println("Test1: trying to put too large packet in rack");
		try {
			hsm.storePacket(width, height, depth, description, weight);
		} catch (StorageException e) {
			SimpleIO.println("Package storage refused, package too large");
			refuse = true;
		}
		return refuse;
	}

	private static int producePacketOverflow() {
		final int NUM_PACKETS = 50;
		int storedPackets = 0;
		int width = 1;
		int height = 1;
		int depth = 1;
		int weight = 1;
		String description = "Small packet";
		SimpleIO.println("Test2: trying to put many small packets in rack");
		while (storedPackets <= NUM_PACKETS) {
			try {
				hsm.storePacket(width, height, depth, description, weight);
			} catch (StorageException e) {
				SimpleIO.println("Harbor storage full");
				break;
			}
			storedPackets++;
		}
		return storedPackets;
	}

	private static int randomProductDimension() {
		final double RANDOM_MAX = 10;// min = -max as well
		final int NUM_PACKETS = 500000;
		int storedPackets = 0;
		int successTestCount = 0;
		int actualPacketStored = 0;
		String description = "Random Sized Packets packet";
		SimpleIO.println("Test2: trying to put many random sized packets in rack");
		while (storedPackets < NUM_PACKETS) {

			int width = (int) (Math.random() * RANDOM_MAX * 2 - RANDOM_MAX);
			int height = (int) (Math.random() * RANDOM_MAX * 2 - RANDOM_MAX);
			int depth = (int)  (Math.random() * RANDOM_MAX * 2 - RANDOM_MAX);
			int weight = (int)  (Math.random() * RANDOM_MAX * 2 - RANDOM_MAX);

			boolean isDimsValid = (width > 0 && height > 0 && depth > 0 && weight > 0);

			boolean dimensionExceptionThrown = false;
			try {
				hsm.storePacket(width, height, depth, description, weight);
				actualPacketStored++;
			} catch (StorageException e) {
				if (e.getMessage().indexOf("Invalid Packet dimensions") >= 0) {
					dimensionExceptionThrown = true;
				} else if (e.getMessage().indexOf("Storage is full") >= 0) {
					 SimpleIO.println(e.getMessage());
					break;
				}
			}
			if (isDimsValid != dimensionExceptionThrown) {// if dims are valid,
															// no exception
															// should throw, and
															// other way around
				successTestCount++;
			} else {
				SimpleIO.println("Test failure");
			}
			storedPackets++;
		}
		SimpleIO.println("Random Packet Store " + successTestCount + "/"
				+ storedPackets + " tests success. " + actualPacketStored
				+ " packets stored in rack\n");
		return storedPackets;
	}

	private static void getAllPackets() {
		SimpleIO.println();
		Packet[] packets = hsm.getPackets();
		System.out.println("TEST 3, going to retrieve " + packets.length
				+ " packets");
		try {
			for (int i = 0; i < packets.length; i++) {
				hsm.retrievePacket(packets[i].getDescription());
			}
		} catch (StorageException e) {
			e.printStackTrace();
		}
	}

	private static void startTestProcedure() {

		// //////////// Test 1 ////////////
		if (testTooLargePacket()) {
			SimpleIO.println("Test successfull, too large packet was refused");
		} else {
			SimpleIO.println("Test failed, too large packet was stored");
		}
		// ////////// Test 2 //////////////
		final int MAX_SLOTS_IN_RACK = 29;
		int storedPackets = producePacketOverflow();
		if (storedPackets == MAX_SLOTS_IN_RACK) {
			SimpleIO.println("Test successfull, exactly 29 packets stored in rack");
		} else {
			SimpleIO.println("Test failed, " + storedPackets
					+ " packets stored. Expected 29.");
		}
		// / TEST 3
		getAllPackets();

		// TEST 4

		randomProductDimension();
		
		// TEST 5
		getAllPackets();
	}

	public static void main(String[] args) {
		hsm = new HarborStorageManagement();
		startTestProcedure();
		hsm.shutdown();
	}
}