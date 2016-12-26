package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.PhysicalCrane;
import de.tuhh.diss.harborstorage.sim.StorageElement;

public class CraneControl {

	private PhysicalCrane physicalCrane;

	public CraneControl(PhysicalCrane physicalCrane) {
		this.physicalCrane = physicalCrane;
	}

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

	private void moveToXY(int x, int y) {
		moveToX(x);
		moveToY(y);
	}

	private void moveToLoadPos() {
		moveToX(physicalCrane.getLoadingPosX());
		moveToY(physicalCrane.getLoadingPosY());
	}

	public void shutdown() {
		physicalCrane.shutdown();
	}

	public boolean moveToX(int x) {
		boolean success = false;
		int err = 0;// if err is positive, move
					// to right
		while (true) {
			err = x - physicalCrane.getPositionX();// if err is positive, move
													// to right
			if (err > 0) {
				physicalCrane.forward();
			} else if (err < 0) {
				physicalCrane.backward();
			} else {
				physicalCrane.stopX();
				if (x - physicalCrane.getPositionX() == 0) {
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

	public boolean moveToY(int y) {
		boolean success = false;
		int err = 0;// if err is positive, move
					// to right
		while (true) {
			err = y - physicalCrane.getPositionY();// if err is positive, move
													// to right
			if (err > 0) {
				physicalCrane.up();

			} else if (err < 0) {
				physicalCrane.down();
			} else {
				physicalCrane.stopY();
				if (y - physicalCrane.getPositionY() == 0) {// Verify again if
															// the crane is in
															// the actual place
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
