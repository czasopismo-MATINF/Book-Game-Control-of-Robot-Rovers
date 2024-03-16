package game.control.robotic.rovers.board;

public class Robot {

	private Cargo cargo;
	private Battery[] batteries;

	public Robot(Integer maxLoad, Integer maxBatteries) {
		this.cargo = new Cargo(maxLoad);
		this.batteries = new Battery[maxBatteries];
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

}
