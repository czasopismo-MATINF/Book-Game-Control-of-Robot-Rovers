package game.control.robot.rovers.config;

public interface BoardConfig {

	enum INT_CONFIG_ENTRY {
		
		ROBOT_MAX_LOAD(10),
		ROBOT_MAX_BATTERIES(10),
		BATTERY_CAPACITY(1000),
		BATTERY_WEIGHT(1),
		CHARGING_STATION_ACCESS_POINTS(4),
		MOTHER_SHIP_MAX_LOAD(200),
		DEFAULT_PLANET_WIDTH(10),
		DEFAULT_PLANET_HEIGHT(10),
		GPS_MESSAGE_ENERGY(20),
		LOCAL_MESSAGE_ENERGY(5);
		
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
