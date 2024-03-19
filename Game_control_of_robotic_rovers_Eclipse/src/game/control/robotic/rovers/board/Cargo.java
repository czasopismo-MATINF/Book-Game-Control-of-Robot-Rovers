package game.control.robotic.rovers.board;

import java.util.List;
import java.util.stream.Collectors;
import java.io.Serializable;
import java.util.ArrayList;

public class Cargo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Battery> batteriesInCargo = new ArrayList<>();
	private int rocks;

	public int load() {

		int batteryWeight = this.batteriesInCargo.stream().collect(Collectors.summingInt(b -> b.getWeight()));
		return batteryWeight + this.rocks;

	}

	public void addBattery(Battery battery) {

		this.batteriesInCargo.add(battery);

	}

	public void addRock(int rocks) {

		this.rocks += rocks;

	}

	public List<Battery> getBatteriesInCargo() {

		return batteriesInCargo;

	}

	public int getRocks() {

		return rocks;

	}

}
