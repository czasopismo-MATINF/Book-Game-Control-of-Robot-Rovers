package game.control.robotic.rovers.board;

import java.io.Serializable;
import game.control.robotic.rovers.CRTGConfig;

public class Robot implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Integer MAX_ROBOT_ID = 0;

	private Integer id;
	private MaxLoadCargo cargo;
	private Battery[] batteries;

	public Robot(Integer maxLoad, Integer maxBatteries) {
		this.id = ++Robot.MAX_ROBOT_ID;
		this.cargo = new MaxLoadCargo(maxLoad);
		this.batteries = new Battery[maxBatteries];
		for (int i = 0; i < this.batteries.length; ++i) {
			this.batteries[i] = new Battery(CRTGConfig.BATTERY_CAPACITY, CRTGConfig.BATTERY_WEIGHT);
		}
	}

	public boolean insertBattery(Battery battery, Integer slot) {
		if (slot < this.batteries.length && this.batteries[slot] == null) {
			this.batteries[slot] = battery;
			return true;
		}
		return false;
	}

	public Battery removeBattery(Integer slot) {
		if (slot < this.batteries.length && this.batteries[slot] != null) {
			Battery b = this.batteries[slot];
			this.batteries[slot] = null;
			return b;
		}
		return null;
	}

	public Integer getId() {
		return id;
	}

}
