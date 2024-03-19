package game.control.robotic.rovers;

import game.control.robotic.rovers.board.BoardConfig;

public class ControlRobotTurnGameConfig implements BoardConfig {

	public static int ROBOT_MAX_LOAD = 10;
	public static int ROBOT_MAX_BATTERIES = 10;

	public static int BATTERY_CAPACITY = 1000;
	public static int BATTERY_WEIGHT = 1;

	public static int CHARGING_STATION_ACCESS_POINTS = 4;

	public static int MOTHER_SHIP_MAX_LOAD = 200;

	@Override
	public int getValue(CONFIG_ENTRIES entry) {
		
		switch (entry) {
		case ROBOT_MAX_LOAD:
			return ControlRobotTurnGameConfig.ROBOT_MAX_LOAD;
		case ROBOT_MAX_BATTERIES:
			return ControlRobotTurnGameConfig.ROBOT_MAX_BATTERIES;
		case BATTERY_CAPACITY:
			return ControlRobotTurnGameConfig.BATTERY_CAPACITY;
		case BATTERY_WEIGHT:
			return ControlRobotTurnGameConfig.BATTERY_WEIGHT;
		case CHARGING_STATION_ACCESS_POINTS:
			return ControlRobotTurnGameConfig.CHARGING_STATION_ACCESS_POINTS;
		case MOTHER_SHIP_MAX_LOAD:
			return ControlRobotTurnGameConfig.MOTHER_SHIP_MAX_LOAD;
		default:
			return 0;
		}
		
	}

}
