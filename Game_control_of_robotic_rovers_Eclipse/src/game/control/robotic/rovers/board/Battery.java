package game.control.robotic.rovers.board;

import java.io.Serializable;

public class Battery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Integer MAX_BATTERY_ID = 0;

	private Integer id;
	private Integer capacity;
	private Integer energy;
	private Integer weight;

	public Battery(Integer capacity, Integer weight) {
		super();
		this.id = ++Battery.MAX_BATTERY_ID;
		this.capacity = capacity;
		this.energy = capacity;
		this.weight = weight;
	}

	public Integer getId() {
		return id;
	}

	public Integer getEnergy() {
		return energy;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public Integer getWeight() {
		return weight;
	}

	private Integer charge(Integer energy) {
		if (this.energy + energy <= this.capacity) {
			this.energy += energy;
			return energy;
		} else {
			Integer e = this.energy;
			this.energy = this.capacity;
			return this.capacity - e;
		}
	}

	private Integer drain(Integer energy) {
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
