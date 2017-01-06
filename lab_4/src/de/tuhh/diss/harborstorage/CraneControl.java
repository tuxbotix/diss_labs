package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.PhysicalCrane;
import de.tuhh.diss.harborstorage.sim.StorageElement;
import de.tuhh.diss.io.SimpleIO;

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
				&& physicalCrane.getPositionY() == physicalCrane.getLoadingPosY()) { // To verify if the crane is at the loading position
			
			physicalCrane.loadElement(packet);
			moveToXY(x, y);
			physicalCrane.storeElement();
			moveToLoadPos();
		} else {// Ensures that the crane starts from the loading position
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
				&& physicalCrane.getPositionY() == physicalCrane.getLoadingPosY()) {// To verify if the crane is at the loading position
			
			moveToXY(x, y);
			physicalCrane.retrieveElement();
			moveToLoadPos();
			packet = physicalCrane.unloadElement();
		} else { // Ensures that the crane starts from the loading position
			moveToLoadPos();
			packet = retrievePacket(x, y);
		}
		return packet;
	}

	/**
	 * Move to a given XY coordinate.Note that this doesn't take the shortest route**
	 * @param x
	 * @param y
	 * @return true if both axis were moved successfully
	 */
	private boolean moveToXY(int x, int y) {
		boolean xSuccess = moveToX(x);
		boolean ySuccess = moveToY(y);
		return xSuccess && ySuccess;
	}

	/**
	 * Enables the crane to move to the loading position
	 */
	private void moveToLoadPos() {
		moveToX(physicalCrane.getLoadingPosX());
		moveToY(physicalCrane.getLoadingPosY());
	}

	/**
	 * Shuts down the crane
	 */
	public void shutdown() {
		physicalCrane.shutdown();
	}

	/**
	 * 
	 * @param x Position in x axis
	 * @return Success is when the crane reaches the given position and it doesn't stall
	 */
	public boolean moveToX(int x) {
		boolean success = false;
		int err = 0;
		while (true) {
			err = x - physicalCrane.getPositionX();
			if (err > 0) {// if err is positive, move the crane to the right in x-axis
				physicalCrane.forward();
			} else if (err < 0) {// if err is negative, move the crane to the left in x-axis
				physicalCrane.backward();
			} else {// if err is zero, then stop the crane
				physicalCrane.stopX();
				if (x - physicalCrane.getPositionX() == 0) {// Reverifies that the crane is in the actual place
					success = true;
					break;
				}
			}
			if (physicalCrane.isStalledX()) {
				break;
			}
		}
		return success;
	}

	/**
	 * 
	 * @param y position in Y axis
	 * @return Success is when the crane reaches the given position and it doesn't stall
	 */
	public boolean moveToY(int y) {
		boolean success = false;
		int err = 0;
		while (true) {
			err = y - physicalCrane.getPositionY();// if err is positive, move the crane to the right in y-axis
			if (err > 0) {
				physicalCrane.up();

			} else if (err < 0) {// if err is negative, move the crane to the left in y-axis
				physicalCrane.down();
			} else {// if err is zero, then stop the crane
				physicalCrane.stopY();
				if (y - physicalCrane.getPositionY() == 0) {// Reverifies that the crane is in the actual place
					success = true;
					break;
				}
			}
			if (physicalCrane.isStalledY()) {
				break;
			}
		}
		return success;
	}
}
