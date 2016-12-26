package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.HighBayStorage;

import de.tuhh.diss.harborstorage.sim.StorageException;
import de.tuhh.diss.harborstorage.sim.PhysicalHarborStorage;
import de.tuhh.diss.harborstorage.sim.PhysicalCrane;
import de.tuhh.diss.harborstorage.sim.StoragePlace;

public class HarborStorageManagement implements HighBayStorage {

	private PhysicalCrane physicalCrane;
	private Slot[] slots;
	private Packet[] packets;
	private int packetCount;
	private CraneControl craneController;
	private int curMaxPacketId = 0;// id will start from 1

	public HarborStorageManagement() {
		PhysicalHarborStorage pp = new PhysicalHarborStorage();

		physicalCrane = pp.getCrane();
		craneController = new CraneControl(physicalCrane);
		// slots = (Slot[]) pp.getStoragePlacesAsArray();
		StoragePlace[] tempSlots = pp.getStoragePlacesAsArray();
		slots = new Slot[tempSlots.length];
		for (int i = 0; i < tempSlots.length; i++) {
			slots[i] = new Slot(tempSlots[i].getNumber(),
					tempSlots[i].getPositionX(), tempSlots[i].getPositionY(),
					tempSlots[i].getWidth(), tempSlots[i].getHeight(),
					tempSlots[i].getDepth(), tempSlots[i].getLoadCapacity());
		}
		packets = new Packet[tempSlots.length];
		for (int i = 0; i < packets.length; i++) {
			packets[i] = null;
		}
		packetCount = 0;
	}

	public int storePacket(int width, int height, int depth,
			String description, int weight) throws StorageException {
		if (packetCount >= slots.length) {
			System.out.println("storage full");
			throw new StorageException("Storage is full");
		}
		Slot slot = findSuitableSlot(width, height, depth, weight);
		int packetId = -1;
		if (slot != null) {// we found a slot
			// create the packet
			// store the packet
			Packet packet = createPacket(width, height, depth, description,
					weight);
			packet.setLocation(slot.getNumber());
			int index = insertPacketToArray(packet);
			if (index >= 0) {
				craneController.storePacket(slot.getPositionX(),
						slot.getPositionY(), packets[index]);
				packetId = packet.getId();
				slots[getSlotArrayIdxByNumber(slot.getNumber())].setContainedPacket(packetId);
				packetCount++;
				System.out.println("Packet inserted" + packetCount);
			} else {
				System.out.println("packet storage issue");
				throw new StorageException("Cannot store Packet in the array!"); // ideally
																					// this
																					// cannot
																					// happen
																					// as
																					// packetCount
																					// track
																					// this.
			}
		} else {
			System.out.println("no slot");
			throw new StorageException("No Suitable Slot is available");
		}
		return packetId;
	}

	private Packet createPacket(int width, int height, int depth,
			String description, int weight) {
		curMaxPacketId++;
		int id = curMaxPacketId;
		Packet packet = new Packet(width, height, depth, description, weight,
				id);
		return packet;
	}

	public void retrievePacket(String description) throws StorageException {
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] !=null && packets[i].getDescription().equals(description)) {
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
				}
			}
		}
	}

	public Packet[] getPackets() {
		Packet[] packetsNew = new Packet[packetCount];
		int iter = 0;
		for (int i = 0; i < packets.length; i++) {
			if (packets[i] != null || packets[i].getId() > 0) {
				packetsNew[iter] = packets[i];
				iter++;
			}
		}
		return packetsNew;
	}

	public void shutdown() {
		craneController.shutdown();
	}

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

	private Packet getPacketById(int id) {
		Packet packet = null;

		for (int i = 0; i < packets.length; i++) {
			if (packets[i].getId() == id) {
				packet = packets[i];
			}
		}
		return packet;
	}

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
