package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.HighBayStorage;
import de.tuhh.diss.harborstorage.sim.StorageException;

public class HarborStorageManagement implements HighBayStorage {
	
	public int storePacket(int width, int height, int depth, String description, int weight) throws StorageException {
		return 0; // TODO: Replace this with your own code. 
	}
	
	public void retrievePacket(String description) throws StorageException {
		// TODO: Add your own code. 
	}
	
	public Packet[] getPackets() {
		return null; // TODO: Replace this with your own code. 
	}
	
	public void shutdown() {
		// TODO: Add your own code. 
	}
}
