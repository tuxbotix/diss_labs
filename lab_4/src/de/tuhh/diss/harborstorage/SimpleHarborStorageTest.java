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

	private 
		static HarborStorageManagement hsm;
	
	private static boolean testTooLargePacket(){
		boolean refuse = false;
		int width=10;
		int height=10;
		int depth=10;
		int weight = 500;
		String description = "Really Large And Heavy Packet";
		
		SimpleIO.println("Test1: trying to put too large packet in rack");
		try{
			hsm.storePacket(width, height, depth, description, weight);
		}
		catch(StorageException e){
			SimpleIO.println("Package storage refused, package too large");
			refuse = true;
		}
		
		return refuse;
	}
	
	private static int producePacketOverflow(){
		final int NUM_PACKETS = 50;
		int storedPackets=0;
		
		int width=1;
		int height=1;
		int depth=1;
		int weight = 1;
		String description = "Small packet";
				
		SimpleIO.println("Test2: trying to put many small packets in rack");
		while(storedPackets<=NUM_PACKETS){
			try{
				hsm.storePacket(width, height, depth, description, weight);
			}
			catch(StorageException e){
				SimpleIO.println("Harbor storage full");
				break;
			}
			storedPackets++;
		}
		
		return storedPackets;
		
	}
	
	private static void startTestProcedure(){
		
		////////////// Test 1 ////////////		
		if(testTooLargePacket()){
			SimpleIO.println("Test successfull, too large packet was refused");
		}else{
			SimpleIO.println("Test failed, too large packet was stored");
		}
		//////////// Test 2 //////////////
		final int MAX_SLOTS_IN_RACK = 29;
		int storedPackets=producePacketOverflow();
		if(storedPackets==MAX_SLOTS_IN_RACK){
			SimpleIO.println("Test successfull, exactly 29 packets stored in rack");	
		}else{
			SimpleIO.println("Test failed, "+storedPackets+" packets stored. Expected 29.");
		}
	}
	
	public static void main(String[] args) {
		hsm = new HarborStorageManagement();
		startTestProcedure();
		hsm.shutdown();
	}

}
