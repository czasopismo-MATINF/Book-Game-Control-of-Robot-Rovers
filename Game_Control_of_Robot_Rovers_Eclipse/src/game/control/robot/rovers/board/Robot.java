package game.control.robot.rovers.board;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Robot implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int MAX_ROBOT_ID = 0;

	private int id;
	private MaxLoadCargo cargo;
	private Battery[] batteries;

	public Robot(int maxLoad, Battery[] batteries) {
		this.id = ++Robot.MAX_ROBOT_ID;
		this.cargo = new MaxLoadCargo(maxLoad);
		this.batteries = batteries;
	}

	public int getId() {
		return id;
	}
	
	public MaxLoadCargo getCargo() {
		return cargo;
	}

	public Battery[] getBatteries() {
		return batteries;
	}

	public boolean insertBattery(Battery battery, int slot) {
		if (slot < this.batteries.length && this.batteries[slot] == null) {
			this.batteries[slot] = battery;
			return true;
		}
		return false;
	}

	public Battery removeBattery(int slot) {
		if (slot < this.batteries.length && this.batteries[slot] != null) {
			Battery b = this.batteries[slot];
			this.batteries[slot] = null;
			return b;
		}
		return null;
	}
	
	public List<Battery> getChargedBatteries() {
		return Arrays.asList(this.getBatteries()).stream().filter(b -> b.getEnergy() > 0).collect(Collectors.toList());
	}

	public int getTotalEnergy() {
		return Arrays.asList(this.getBatteries()).stream().collect(Collectors.summingInt(b -> b.getEnergy()));
	}
	
	public int drainEnergy(int energy) {
		
		int total = 0;
		for(int i = 0; i < energy; ++i) {
			List<Battery> chargedBatteries = this.getChargedBatteries();
			if(chargedBatteries.size() == 0) {
				return total;
			}
			Collections.shuffle(chargedBatteries);
			chargedBatteries.get(0).drain(1);
			total += 1;
		}
		
		return total;
		
	}
	
	public boolean hasEnoughEnergy(int energy) {
		return this.getTotalEnergy() >= energy;
	}

}
