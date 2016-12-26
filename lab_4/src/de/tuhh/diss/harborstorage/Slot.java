package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StoragePlace;

public class Slot implements StoragePlace {	
	
	private int number;
	private int positionX;
	private int positionY;
	private int width;
	private int height;
	private int depth;
	private int loadCapacity;
	private int containedPacket; //same reason as for Packet.location save index of packets[] which will contain the given packet
	
	public Slot(int number, int positionX, int positionY, int width, int height, int depth, int loadCapacity){
        this.number = number;
        this.positionX = positionX;
        this.positionY= positionY;
        this.width = width;
        this.height= height;
        this.depth=depth;
        this.loadCapacity = loadCapacity;
        this.containedPacket = -1;// -1 = no packet here. packetID >= 0;
	}
	
	public int getNumber() {
		return number; // TODO: Replace this with your own code. 
	}

	public int getWidth() {
		return width; // TODO: Replace this with your own code. 
	}

	public int getHeight() {
		return height; // TODO: Replace this with your own code. 
	}
	
	public int getDepth() {
		return depth; // TODO: Replace this with your own code. 
	}
	public int getLoadCapacity() {
		return loadCapacity; // TODO: Replace this with your own code. 
	}
	public int getPositionX() {
		return positionX; // TODO: Replace this with your own code. 
	}
	
	public int getPositionY() {
		return positionY; // TODO: Replace this with your own code. 
	}
	public int getContainedPacket(){
		return containedPacket;
	}
	public void setContainedPacket(int containedPacket){
		this.containedPacket = containedPacket;
	}
}
