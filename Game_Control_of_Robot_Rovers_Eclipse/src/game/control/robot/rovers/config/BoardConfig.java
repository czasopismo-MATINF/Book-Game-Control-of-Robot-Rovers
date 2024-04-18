package game.control.robot.rovers.config;

public interface BoardConfig {

	enum INT_CONFIG_ENTRY {
		
		ROBOT_CARGO_MAX_LOAD(3),
		ROBOT_MAX_BATTERIES(10),
		BATTERY_CAPACITY(1000),
		BATTERY_WEIGHT(1),
		CHARGING_STATION_ACCESS_POINTS(4),
		MOTHER_SHIP_MAX_LOAD(200),
		DEFAULT_PLANET_WIDTH(10),
		DEFAULT_PLANET_HEIGHT(10),
		
		DROP_CARGO_ENERGY(10),
		DROP_BATTERY_ENERGY(10),
		
		ROBOT_MOVE_AREA_MIN(0),
		ROBOT_MOVE_AREA_MAX(50),
		ROBOT_MOVE_WEIGHT_MULTIPLIER(5),
		
		COLLECT_BATTERY_CONST_ENERGY(25),
		COLLECT_BATTERY_WEIGHT_MULTIPLIER(5);
		
		public int defaultValue;
		
		private INT_CONFIG_ENTRY(int defaultValue) {
			this.defaultValue = defaultValue;
		}
	}
	
	enum STRING_CONFIG_ENTRY {
		
		MESSAGE_SEPARATOR("\n");
		
		public String defaultValue;
		
		private STRING_CONFIG_ENTRY(String defaultValue) {
			this.defaultValue = defaultValue;
		}
		
	}

	public int getIntValue(INT_CONFIG_ENTRY entry);
	public String getStringValue(STRING_CONFIG_ENTRY entry);

}
