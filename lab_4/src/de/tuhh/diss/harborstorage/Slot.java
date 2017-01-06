package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StoragePlace;

public class Slot implements StoragePlace {

	// attributes of Slot as defined in class diagram
	private int number;
	private int positionX; // position of slot in Cartesian coordinated. bottom
							// left point*
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
	 * 
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

	/**
	 * Copy constructor. Useful to copy a StoragePlace array to Slot. Perform a
	 * deep copy of StoragePlace to Slot (copy all attributes)
	 * 
	 * @param storagePlace
	 *            StoragePlace object
	 */
	public Slot(StoragePlace storagePlace) {
		this(storagePlace.getNumber(), storagePlace.getPositionX(),
				storagePlace.getPositionY(), storagePlace.getWidth(),
				storagePlace.getHeight(), storagePlace.getDepth(), storagePlace
						.getLoadCapacity());
	}

	/**
	 * Copy StoragePlaces array to a Slot array. Uses the copy constructor
	 * 
	 * @param storagePlaces
	 * @return Slot array which have deep copied storagePlace
	 */
	public static Slot[] copyStoragePlaceArr(StoragePlace[] storagePlaces) {
		Slot slots[] = new Slot[storagePlaces.length];
		for (int i = 0; i < storagePlaces.length; i++) {
			if (storagePlaces[i] != null) {// copy only not null stuff.
				slots[i] = new Slot(storagePlaces[i]);
			}
		}
		return slots;
	}

	/**
	 * Get Number
	 * 
	 * @return number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * get width
	 * 
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * get height
	 * 
	 * @return heigth
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * get depth
	 * 
	 * @return depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * get Load capacity
	 * 
	 * @return loadCapacity
	 */
	public int getLoadCapacity() {
		return loadCapacity;
	}

	/**
	 * Get position X
	 * 
	 * @return positionX
	 */
	public int getPositionX() {
		return positionX;
	}

	/**
	 * Get position Y
	 * 
	 * @return positionY
	 */
	public int getPositionY() {
		return positionY;
	}

	/**
	 * Get contained packet we set the packet ID
	 * 
	 * @return
	 */
	public int getContainedPacket() {
		return containedPacket;
	}

	/**
	 * Set contained packet value We set packet ID**
	 * 
	 * @param containedPacket
	 */
	public void setContainedPacket(int containedPacket) {
		this.containedPacket = containedPacket;
	}
}