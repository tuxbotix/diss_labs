package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.HighBayStorage;
import de.tuhh.diss.harborstorage.sim.StorageException;
import de.tuhh.diss.harborstorage.sim.PhysicalHarborStorage;

public class HarborStorageManagement implements HighBayStorage {
	
	private PhysicalCrane physicalCrane;
	private Slot[] slots;
	private Packet[] packets;
        private int packetCount;
	
	public HarborStorageManagement(){
            slots = PhysicalHarborStorage.getFirstHarborStorage();
            physicalCrane =PhysicalHarborStorage.getCrane();
	}

	public int storePacket(int width, int height, int depth, String description, int weight) throws StorageException {
		return 0; // TODO: Replace this with your own code. 
		
	}
	
	public void retrievePacket(String description) throws StorageException {
            for(int i=0; i < 
		// TODO: Add your own code. 
	}
	
	public Packet[] getPackets() {
		return null; // TODO: Replace this with your own code. 
	}
	
	public void shutdown() {
		// TODO: Add your own code. 
	}
	
	private Packet getPacketById(int id){
            Packet packet = null;
            
            for(int i=0; i< packets.length ; i++){
                if(packets[i].getId() == id){
                    packet = packets[i];
                }
            }
            return packet;
	}
	
        private Slot getSlotByNumber(int number){
            Slot slot = null;
            
            for(int i=0; i< slots.length ; i++){
                if(slots[i].getNumber() == number){
                    slot = slots[i];
                }
            }
            return slot;
	}
}