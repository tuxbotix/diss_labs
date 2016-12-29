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
	private int containedPacket; // same reason as for Packet.location save
									// index of packets[] which will contain the
									// given packet

	/**
	 * Slot constructor.
	 * @param number
	 * @param positionX
	 * @param positionY
	 * @param width
	 * @param height
	 * @param depth
	 * @param loadCapacity
	 */
	public Slot(int number, int positionX, int positionY, int width,
			int height, int depth, int loadCapacity) {
		this.number = number;
		this.positionX = positionX;
		this.positionY = positionY;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.loadCapacity = loadCapacity;
		this.containedPacket = -1;// -1 = no packet here. packetID >= 0;
	}

	public int getNumber() {
		return number;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDepth() {
		return depth;
	}

	public int getLoadCapacity() {
		return loadCapacity;
	}

	public int getPositionX() {
		return positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public int getContainedPacket() {
		return containedPacket;
	}

	public void setContainedPacket(int containedPacket) {
		this.containedPacket = containedPacket;
	}
}