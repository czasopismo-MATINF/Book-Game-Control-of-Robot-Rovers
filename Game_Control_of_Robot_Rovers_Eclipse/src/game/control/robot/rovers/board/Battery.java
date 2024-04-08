package game.control.robot.rovers.board;

import java.io.Serializable;

public class Battery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Integer MAX_BATTERY_ID = 0;

	private int id;
	private int capacity;
	private int energy;
	private int weight;

	public Battery(int capacity, int weight) {
		super();
		this.id = ++Battery.MAX_BATTERY_ID;
		this.capacity = capacity;
		this.energy = capacity;
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public int getEnergy() {
		return energy;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getWeight() {
		return weight;
	}

	private int charge(int energy) {
		if (this.energy + energy <= this.capacity) {
			this.energy += energy;
			return energy;
		} else {
			Integer e = this.energy;
			this.energy = this.capacity;
			return this.capacity - e;
		}
	}

	private int drain(int energy) {
		if (this.energy <= energy) {
			this.energy -= energy;
			return energy;
		} else {
			Integer e = this.energy;
			this.energy = 0;
			return e;
		}
	}

}
