package game.control.robot.rovers.config;

public class GameConfig implements BoardConfig {

	@Override
	public int getIntValue(INT_CONFIG_ENTRY entry) {

		return entry.defaultValue;

	}

	@Override
	public String getStringValue(STRING_CONFIG_ENTRY entry) {
		
		return entry.defaultValue;
		
	}

}
