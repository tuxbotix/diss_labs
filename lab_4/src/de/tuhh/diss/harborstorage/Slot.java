package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StoragePlace;

public class Slot implements StoragePlace {	
	
	private int number;
	private int posX;
	private int posY;
	private int width;
	private int height;
	private int depth;
	private int loadCapacity;
	private int containedPacket; //same reason as for Packet.location save index of packets[] which will contain the given packet
	
	public Slot(int number, int posX, int posY, int width, int height, int depth, int loadCapacity){
            
	}
	
	public int getNumber() {
		return 0; // TODO: Replace this with your own code. 
	}

	public int getWidth() {
		return 0; // TODO: Replace this with your own code. 
	}

	public int getHeight() {
		return 0; // TODO: Replace this with your own code. 
	}
	
	public int getDepth() {
		return 0; // TODO: Replace this with your own code. 
	}
	public int getLoadCapacity() {
		return 0; // TODO: Replace this with your own code. 
	}
	public int getPositionX() {
		return 0; // TODO: Replace this with your own code. 
	}
	
	public int getPositionY() {
		return 0; // TODO: Replace this with your own code. 
	}

}
