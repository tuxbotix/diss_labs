package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StorageElement;

public class Packet implements StorageElement {
	private int id;
	private int width;
	private int height;
	private int depth;
	private String description;
	private int weight;
	private int location;
	// We decided to store Slot ID (=number) instead of a slot object as storing
	// a slot object is duplication.
	
	/**
	 * Constructor of the Packet
	 * 
	 * @param dx width
	 * @param dy height
	 * @param dz depth
	 * @param desc description
	 * @param weight weight
	 * @param id ID
	 */
	
	public Packet(int dx, int dy, int dz, String desc, int weight, int id) {
		// it is the job of Harbour Storage Management to give unique ID's
		this.width = dx;
		this.height = dy;
		this.depth = dz;
		this.description = desc;
		this.weight = weight;
		this.id = id;
	}

	/**
	 * Constructor with facility to set location on creation
	 * @param dx
	 * @param dy
	 * @param dz
	 * @param desc
	 * @param weight
	 * @param id
	 * @param location
	 */
	public Packet(int dx, int dy, int dz, String desc, int weight, int id, int location) {
		this.width = dx;
		this.height = dy;
		this.depth = dz;
		this.description = desc;
		this.weight = weight;
		this.id = id;
		this.location = location;
	}

	/**
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDepth() {
		return depth;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public int getWeight() {
		return weight;
	}

	public int getLocation() {
		return location;
	}

	/**
	 * Set location of parcel. This will be slot number.
	 * @param location
	 */
	public void setLocation(int location) {
		this.location = location;
	}
}
