package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.HighBayStorage;

import de.tuhh.diss.harborstorage.sim.StorageException;
import de.tuhh.diss.harborstorage.sim.PhysicalHarborStorage;
import de.tuhh.diss.harborstorage.sim.PhysicalCrane;
import de.tuhh.diss.io.SimpleIO;

public class HarborStorageManagement implements HighBayStorage {

	private PhysicalCrane physicalCrane;
	private Slot[] slots;
	private Packet[] packets;
	private int packetCount;
	private CraneControl craneController;
	private int curMaxPacketId = 0;// Unique identification number (id) will
									// start from 1

	/**
	 * Constructor
	 */
	public HarborStorageManagement() {
		PhysicalHarborStorage pp = new PhysicalHarborStorage();

		// Get physical crane and storagePlace array
		physicalCrane = pp.getCrane();
		craneController = new CraneControl(physicalCrane);

		// using custom array copy that use the copy constructor for Slot
		// It is not assumed that slot array is in order of slotNumbers.
		slots = Slot.copyStoragePlaceArr(pp.getStoragePlacesAsArray());
		
		/**
		 * packet array have same size of slot array
		 * 
		 * When a packet is stored, it will be stored in first available cell
		 * (available = null) When retrieved the cell will be set to null. So
		 * packet Array index cells are not ordered by id/ time.
		 * 
		 * NOTE : Can use/ implement array sort, but no significant gain and
		 * useless performance overhead.
		 */
		
		packets = new Packet[slots.length];
		for (int i = 0; i < packets.length; i++) {// make sure all of them null.
			packets[i] = null;
		}
		packetCount = 0;// packet count
	}

	/**
	 * Store the packet
	 * 
	 * @param width
	 *            of the packet
	 * @param height
	 *            of the packet
	 * @param depth
	 *            of the packet
	 * @param description
	 *            of the packet
	 * @param weight
	 *            of the packet
	 * @return packetID Created packet's ID. If failed, -1
	 */
	public int storePacket(int width, int height, int depth,
			String description, int weight) throws StorageException {
		// validate dimensions.
		if (width <= 0 || height <= 0 || depth <= 0 || weight <= 0) {
			throw new StorageException("Invalid Packet dimensions!!!");
		}
		// validate enough slots available
		if (packetCount >= slots.length) {
			// System.out.println("storage full");
			throw new StorageException("Storage is full");
		}

		// find a slot. If null returned -> no slot found.
		Slot slot = findSuitableSlot(width, height, depth, weight);

		int packetId = -1;// default value. packet ID should never be negative.

		if (slot != null) {// we found a slot
			// create the packet
			// store the packet
			Packet packet = createPacket(width, height, depth, description,
					weight, slot.getNumber());
			// find available cell on packets[] and insert the packet.
			int index = insertPacketToArray(packet);

			if (index >= 0) {// if the packet was stored in the packet[]
				// call craneControl
				craneController.storePacket(slot.getPositionX(),
						slot.getPositionY(), packets[index]);
				// Store packetId
				packetId = packet.getId();
				// set containedPacket property of the corresponding slot.
				slots[getSlotArrayIdxByNumber(slot.getNumber())]
						.setContainedPacket(packetId);
				// increment packet count
				packetCount++;
				SimpleIO.println("Packet  stored in rack. The ID is "
						+ packetId);
			} else {// if this happen, something is badly wrong!!
				throw new StorageException("Cannot store Packet in the array!");
			}
		} else { // slot not found
			throw new StorageException("No Suitable Slot is available");
		}
		return packetId;// if successful, actual packetID, else -1
	}

	/**
	 * A small method to keep track of packet ID's and assign new ID's. Always
	 * call this to create and assign ID to a packet.
	 * 
	 * @param width of the packet
	 * @param height of the packet
	 * @param depth of the packet
	 * @param description of the packet
	 * @param weight of the packet
	 * @param slot Number of the packet
	 * @return the created packet
	 */
	private Packet createPacket(int width, int height, int depth, String description, int weight, int slotNum) {
		curMaxPacketId++;
		int id = curMaxPacketId;
		Packet packet = new Packet(width, height, depth, description, weight, id, slotNum);
		return packet;
	}

	/**
	 * Retrieve a packet by a given description. If descriptions are duplicated
	 * in multiple packets, the first match will be retrieved
	 * 
	 * @param description of the packet 
	 */

	public void retrievePacket(String description) throws StorageException {
		boolean foundPacket = false;
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] != null && packets[i].getDescription().equals(description)) {//compares the given description with the description of all the packets
				retrievePacketById(packets[i].getId());
				foundPacket = true;
				break;
			}
		}
		if (!foundPacket) {
			throw new StorageException("No Package was found matching description");//displays an error when the mentioned package could not be found
		}
	}

	/**
	 * Retrieve a packet by ID. This is useful when description is not unique.
	 * We prefer to use this as multiple packets can have same description. ID
	 * is somewhat like tracking number
	 * 
	 * @param description of the packet
	 */

	public void retrievePacketById(int id) throws StorageException {
		boolean foundPacket = false;// flag to check if a packet was found
		// loop
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] != null && packets[i].getId() == id) {// if found
				int index = getSlotArrayIdxByNumber(packets[i].getLocation());// location
																				// =
																				// slot
																				// number

				if (index >= 0) {// if the array index is valid
					// call cranecontrol
					craneController.retrievePacket(slots[index].getPositionX(),
							slots[index].getPositionY());

					packetCount--;// decrement packet count

					packets[i] = null;// set packet cell null IMPORTANT
					// set containedPacket to -1 this means slot is empty.
					slots[index].setContainedPacket(-1);
					SimpleIO.println("Packet Retrieved");
					foundPacket = true;

				} else { // if array index of slots[] is not valid.
					throw new StorageException(
							"Issue of finding Slot number of packet");
				}
				break;
			}
		}

		if (!foundPacket) {// if packet was not found
			throw new StorageException("No Package was with given Id");
		}
	}

	/**
	 * Get the packets
	 * 
	 * @return An array containing Packet objects. Filtered for Null's and
	 *         invalid ID's
	 */
	public Packet[] getPackets() {
		Packet[] packetsNew = new Packet[packetCount];
		int iter = 0;
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] != null && packets[i].getId() > 0) {// our policy is
																// packet ID > 0
				packetsNew[iter] = packets[i];
				iter++;
			}
		}
		return packetsNew;
	}

	/**
	 * Shutdown on app closure.
	 */
	public void shutdown() {
		SimpleIO.println("System Ends.");
		craneController.shutdown();
	}

	/**
	 * Find the best slot. NOTE Does not consider shortest distance from the
	 * crane.
	 * 
	 * @param dx
	 * @param dy
	 * @param dz
	 * @param weight
	 * @return A slot object
	 */
	private Slot findSuitableSlot(int dx, int dy, int dz, int weight) {
		// Solving for minimum value problem
		Slot tempSlot = null;
		int minHeight = Integer.MAX_VALUE;
		int minWidth = Integer.MAX_VALUE;
		int minDepth = Integer.MAX_VALUE;
		int minLoadCapacity = Integer.MAX_VALUE;
		//loop
		for (int i = 0; i < slots.length; i++) {
			// check if slot is vacant. -> containedPacket <0 = invalid packet id
			if (slots[i].getContainedPacket() >= 0) {
				continue;
			}
			
			int slotWidth = slots[i].getWidth();
			int slotHeight = slots[i].getHeight();
			int slotDepth = slots[i].getDepth();
			int slotLoadCapacity = slots[i].getLoadCapacity();
			
			/** 
			 * Combined checks
			 * 1. Matching slot dimensions (larger or equal than packet). first 4 checks
			 * 2. Smallest possible slot for the packet. 
			 * Order of the checks matter for performance :P
			 */
			
			if (slotWidth >= dx && slotHeight >= dy && slotDepth >= dz
					&& slotLoadCapacity >= weight
					&& slotWidth <= minWidth
					&& slotHeight <= minHeight && slotDepth <= minDepth
					&& slotLoadCapacity <= minLoadCapacity) {
				
				tempSlot = slots[i];
				
				// Update minimum values
				minWidth = slotWidth;
				minHeight = slotHeight;
				minDepth = slotDepth;
				minLoadCapacity = slotLoadCapacity;

			}
		}
		// if smallest slot found, tempSlot will be the smallest slot that fit the packet
		// else it'll be null
		return tempSlot;
	}

	/**
	 * Insert a packet to the packet Array in this class.
	 * 
	 * @param packet
	 * @return The Array index where this packet was stored
	 */
	private int insertPacketToArray(Packet packet) {
		// default value -1. which is invalid.
		int packetIndex = -1;
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] == null || packets[i].getId() <= 0) {
				packets[i] = packet;
				packetIndex = i;
				break;
			}
		}
		return packetIndex;
	}

	/**
	 * Get slot array index by slot number. Useful to refer and modify a slot.
	 * NOTE May be deprecated if the slot[] by PhysicalHarborStorage is in same
	 * order.
	 * 
	 * @param number
	 * @return Slot array's index
	 */
	private int getSlotArrayIdxByNumber(int number) {
		int index = -1;

		for (int i = 0; i < slots.length; i++) {
			if (slots[i].getNumber() == number) {
				index = i;
			}
		}
		return index;
	}
}
