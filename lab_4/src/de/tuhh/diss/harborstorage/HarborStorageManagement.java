package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.HighBayStorage;

import de.tuhh.diss.harborstorage.sim.StorageException;
import de.tuhh.diss.harborstorage.sim.PhysicalHarborStorage;
import de.tuhh.diss.harborstorage.sim.PhysicalCrane;
import de.tuhh.diss.harborstorage.sim.StoragePlace;
import de.tuhh.diss.io.SimpleIO;

public class HarborStorageManagement implements HighBayStorage {

	private PhysicalCrane physicalCrane;
	private Slot[] slots;
	private Packet[] packets;
	private int packetCount;
	private CraneControl craneController;
	private int curMaxPacketId = 0;// id will start from 1

	/**
	 * Constructor
	 */
	public HarborStorageManagement() {
		PhysicalHarborStorage pp = new PhysicalHarborStorage();

		physicalCrane = pp.getCrane();
		craneController = new CraneControl(physicalCrane);

		Slot[] slots = Slot.copyStoragePlaceArr(pp.getStoragePlacesAsArray());

		packets = new Packet[slots.length];
		for (int i = 0; i < packets.length; i++) {// make sure all of them null.
			packets[i] = null;
		}
		packetCount = 0;
	}

	/**
	 * Store the packet
	 * 
	 * @param width
	 *            Width of packet
	 * @param height
	 * @param depth
	 * @param description
	 * @param weight
	 * @return packetID Created packet's ID. If failed, -1
	 */
	public int storePacket(int width, int height, int depth,
			String description, int weight) throws StorageException {
		if (width <= 0 || height <= 0 || depth <= 0 || weight <= 0) {
			throw new StorageException("Invalid Packet dimensions!!!");
		}
		if (packetCount >= slots.length) {
			// System.out.println("storage full");
			throw new StorageException("Storage is full");
		}

		Slot slot = findSuitableSlot(width, height, depth, weight);
		int packetId = -1;
		if (slot != null) {// we found a slot
			// create the packet
			// store the packet
			Packet packet = createPacket(width, height, depth, description,
					weight, slot.getNumber());
			int index = insertPacketToArray(packet);
			if (index >= 0) {
				craneController.storePacket(slot.getPositionX(),
						slot.getPositionY(), packets[index]);
				packetId = packet.getId();
				slots[getSlotArrayIdxByNumber(slot.getNumber())]
						.setContainedPacket(packetId);
				packetCount++;
				SimpleIO.println("Packet  stored in rack. The ID is "
						+ packetId);
			} else {

				throw new StorageException("Cannot store Packet in the array!");
				// ideally this cannot happen as packetCount track this.
			}
		} else {
			// System.out.println("no slot");
			throw new StorageException("No Suitable Slot is available");
		}
		return packetId;
	}

	/**
	 * A small method to keep track of packet ID's and assign new ID's. Always
	 * call this*
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param description
	 * @param weight
	 * @param slotNum
	 * @return The created packet
	 */
	private Packet createPacket(int width, int height, int depth,
			String description, int weight, int slotNum) {
		curMaxPacketId++;
		int id = curMaxPacketId;
		Packet packet = new Packet(width, height, depth, description, weight,
				id, slotNum);
		return packet;
	}

	/**
	 * Retrieve a packet by a given description. If descriptions are duplicated
	 * in multiple packets, the first match will be retrieved
	 * 
	 * @param description
	 *            packet description
	 */

	public void retrievePacket(String description) throws StorageException {
		boolean foundPacket = false;
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] != null
					&& packets[i].getDescription().equals(description)) {
				retrievePacketById(packets[i].getId());
				foundPacket = true;
				break;
			}
		}
		if (!foundPacket) {
			throw new StorageException(
					"No Package was found matching description");
		}
	}

	/**
	 * Overloaded** Retrieve a packet by ID. This is superior when description
	 * is not unique.
	 * 
	 * @param description
	 *            packet description
	 */

	public void retrievePacketById(int id) throws StorageException {
		boolean foundPacket = false;
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] != null && packets[i].getId() == id) {
				int index = getSlotArrayIdxByNumber(packets[i].getLocation());// location
																				// =
																				// slot
																				// number
				if (index >= 0) {
					craneController.retrievePacket(slots[index].getPositionX(),
							slots[index].getPositionY());
					packetCount--;// decrement packet count
					packets[i] = null;
					slots[index].setContainedPacket(-1);
					System.out.println("Packet Retrieved");
					foundPacket = true;
				} else {
					throw new StorageException(
							"Issue of finding Slot number of packet");
				}
				break;
			}
		}
		if (!foundPacket) {
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
			if (packets[i] != null && packets[i].getId() > 0) {
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
		System.out.println("System Ends.");
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
		Slot tempSlot = null;
		int minHeight = Integer.MAX_VALUE;
		int minWidth = Integer.MAX_VALUE;
		int minDepth = Integer.MAX_VALUE;
		int minLoadCapacity = Integer.MAX_VALUE;
		for (int i = 0; i < slots.length; i++) {
			if (slots[i].getContainedPacket() >= 0) {
				continue;
			}
			int slotWidth = slots[i].getWidth();
			int slotHeight = slots[i].getHeight();
			int slotDepth = slots[i].getDepth();
			int slotLoadCapacity = slots[i].getLoadCapacity();
			if (slotWidth >= dx && slotHeight >= dy && slotDepth >= dz
					&& slotLoadCapacity >= weight) {
				if (slotWidth <= minWidth && slotHeight <= minHeight
						&& slotDepth <= minDepth
						&& slotLoadCapacity <= minLoadCapacity) {
					tempSlot = slots[i];
				}
			}
		}
		return tempSlot;
	}

	/**
	 * Insert a packet to the packet Array in this class.
	 * 
	 * @param packet
	 * @return The Array index where this packet was stored
	 */
	private int insertPacketToArray(Packet packet) {
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
