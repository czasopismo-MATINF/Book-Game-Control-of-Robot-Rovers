package game.control.robotic.rovers.board;

public class Battery {

	private Integer id;
	private Integer capacity;
	private Integer energy;
	private Integer weight;

	public Battery(Integer id, Integer maxEnergy, Integer weight) {
		super();
		this.id = id;
		this.capacity = maxEnergy;
		this.energy = maxEnergy;
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
