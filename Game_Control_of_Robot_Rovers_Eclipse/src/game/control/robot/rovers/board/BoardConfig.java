package game.control.robot.rovers.board;

public interface BoardConfig {

	enum CONFIG_ENTRIES {
		ROBOT_MAX_LOAD, ROBOT_MAX_BATTERIES, BATTERY_CAPACITY, BATTERY_WEIGHT, CHARGING_STATION_ACCESS_POINTS,
		MOTHER_SHIP_MAX_LOAD
	}

	public int getValue(CONFIG_ENTRIES entry);

}
