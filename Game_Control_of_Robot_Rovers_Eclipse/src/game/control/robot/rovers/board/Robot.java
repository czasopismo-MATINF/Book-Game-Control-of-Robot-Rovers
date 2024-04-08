package game.control.robot.rovers.board;

import java.io.Serializable;

public class Robot implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int MAX_ROBOT_ID = 0;

	private int id;
	private MaxLoadCargo cargo;
	private Battery[] batteries;

	public Robot(int maxLoad, int maxBatteries, BoardConfig boardConfig) {
		this.id = ++Robot.MAX_ROBOT_ID;
		this.cargo = new MaxLoadCargo(maxLoad);
		this.batteries = new Battery[maxBatteries];
		for (int i = 0; i < this.batteries.length; ++i) {
			this.batteries[i] = new Battery(boardConfig.getValue(BoardConfig.CONFIG_ENTRIES.BATTERY_CAPACITY),
					boardConfig.getValue(BoardConfig.CONFIG_ENTRIES.BATTERY_WEIGHT));
		}
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

	public int getId() {
		return id;
	}

}
