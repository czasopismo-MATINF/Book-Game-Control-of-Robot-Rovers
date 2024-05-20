package game.control.robot.rovers.board;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.io.Serializable;

public class Cargo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected List<Battery> batteriesInCargo = new ArrayList<>();
	protected int rocks;

	public int load() {

		int batteryWeight = this.batteriesInCargo.stream().collect(Collectors.summingInt(b -> b.getWeight()));
		
		return batteryWeight + this.rocks;

	}

	public void addBattery(Battery battery) {

		this.batteriesInCargo.add(battery);

	}

	public void addRocks(int rocks) {

		this.rocks += rocks;

	}
	
	public void addBatteries(List<Battery> batteries) {
		
		this.batteriesInCargo.addAll(batteries);
	
	}

	public List<Battery> getBatteriesInCargo() {

		return batteriesInCargo;

	}

	public int getRocks() {

		return rocks;

	}

}
