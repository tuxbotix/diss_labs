package de.tuhh.diss.harborstorage;

import de.tuhh.diss.harborstorage.sim.StorageElement;

public class Packet implements StorageElement{
        private int id;
        private int width;
        private int height;
        private int depth;
        private String description;
        private int weight;
        private int location;// We decided to store Slot ID (=number) instead of a slot object as storing a slot object is duplication. (Java don't pass by reference or pointer :P )

	public Packet(int dx, int dy, int dz, String desc, int weight, int id){// it is the job of Harbour Storage Mgmt. to give unique ID's
            this.width = dx;
            this.height = dy;
            this.depth = dz;
            this.description = desc;
            this.weight = weight;
            this.id = id;
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

	public int getId() {
		return id;
	}
	
	public String getDescription() {
		return description; // TODO: Replace this with your own code. 
	}
	public int getWeight() {
		return weight; // TODO: Replace this with your own code. 
	}
	public int getLocation(){
            return location;
	}
	public void setLocation(int location){
            this.location=location;
	}
}
