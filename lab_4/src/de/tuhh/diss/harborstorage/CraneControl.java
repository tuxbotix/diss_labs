package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.PhysicalCrane;
import de.tuhh.diss.harborstorage.sim.StorageElement;

public class CraneControl {

	private PhysicalCrane physicalCrane;

	/**
	 * Constructor
	 * @param physicalCrane
	 */
	public CraneControl(PhysicalCrane physicalCrane) {
		this.physicalCrane = physicalCrane;
	}

	/**
	 * Store a packet.
	 * @param x
	 * @param y
	 * @param packet
	 */
	public void storePacket(int x, int y, StorageElement packet) {

		physicalCrane.start();

		if (physicalCrane.getPositionX() == physicalCrane.getLoadingPosX()
				&& physicalCrane.getPositionY() == physicalCrane.getLoadingPosY()) {
			physicalCrane.loadElement(packet);
			moveToXY(x, y);
			physicalCrane.storeElement();
			moveToLoadPos();
		} else {// To verify if the crane is at the loading position
			moveToLoadPos();
			storePacket(x, y, packet);
		}

	}

	/**
	 * Get a packet by coordinates of its location
	 * @param x
	 * @param y
	 * @return The packet
	 */
	public StorageElement retrievePacket(int x, int y) {
		StorageElement packet = null;
		physicalCrane.start();

		if (physicalCrane.getPositionX() == physicalCrane.getLoadingPosX()
				&& physicalCrane.getPositionY() == physicalCrane.getLoadingPosY()) {
			moveToXY(x, y);
			physicalCrane.retrieveElement();
			moveToLoadPos();
			packet = physicalCrane.unloadElement();
		} else {// To verify if the crane is at the loading position
			moveToLoadPos();
			packet = retrievePacket(x, y);
		}
		return packet;
	}

	/**
	 * Move to a given XY coordinate. This don't take the shortest route**
	 * @param x
	 * @param y
	 */
	private void moveToXY(int x, int y) {
		moveToX(x);
		moveToY(y);
	}

	/**
	 * Move to loading position
	 */
	private void moveToLoadPos() {
		moveToX(physicalCrane.getLoadingPosX());
		moveToY(physicalCrane.getLoadingPosY());
	}

	/**
	 * Shutdown the crane
	 */
	public void shutdown() {
		physicalCrane.shutdown();
	}

	/**
	 * 
	 * @param x Position in x axis
	 * @return Success or not (if and only if the crane reach to given position and it is not stalled)
	 */
	public boolean moveToX(int x) {
		boolean stalled = false;
		int err = x - physicalCrane.getPositionX();// if err is positive, move to right
		
		while (err !=0 && !stalled) {// until error ==0;
			if (err > 0) {
				physicalCrane.forward();
			} else if (err < 0) {
				physicalCrane.backward();
			} else {
				physicalCrane.stopX();
			}
			stalled=physicalCrane.isStalledX();
			err = x - physicalCrane.getPositionX();// if err is positive, move
		}
		
		return (err ==0 && !stalled); // error !=0 and not stalled
	}

	/**
	 * 
	 * @param y position in Y axis
	 * @return Success or not (if and only if the crane reach to given position and it is not stalled)
	 */
	public boolean moveToY(int y) {
		boolean stalled = false;
		int err = y - physicalCrane.getPositionY();// if err is positive, move to right
		
		while (err !=0 && !stalled) {// until error ==0;
			if (err > 0) {
				physicalCrane.up();
			} else if (err < 0) {
				physicalCrane.down();
			} else {
				physicalCrane.stopY();
			}
			stalled=physicalCrane.isStalledY();
			err = y - physicalCrane.getPositionY();// if err is positive, move
		}
		
		return (err ==0 && !stalled); // error !=0 and not stalled
	}
}
