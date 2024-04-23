package game.control.robot.rovers.actions;

import java.util.function.BiFunction;

import game.control.robot.rovers.board.*;
import game.control.robot.rovers.command.PromptCommand;
import game.control.robot.rovers.config.BoardConfig;
import game.control.robot.rovers.config.GameConfig;

public enum CREATE_COMMAND {

	RESET_PLANET("resetPlanet", "reset planet : %d %d", 2, (p, c) -> {

		int width = Integer.valueOf(c.argumentsArray[0]);
		int height = Integer.valueOf(c.argumentsArray[1]);
		p.resetPlanet(width, height);

		return true;
	}), ADD_MOTHER_SHIP("addMotherShip", "add mother ship : %d %d %s", 3, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		MotherShip motherShip = p.extractMotherShip();
		
		if(String.valueOf(c.argumentsArray[2]).equals("new")) {
			motherShip = new MotherShip(
					CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.MOTHER_SHIP_MAX_LOAD));
		}
		
		if (motherShip == null) {
			motherShip = new MotherShip(
					CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.MOTHER_SHIP_MAX_LOAD));
		}

		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].setMotherShip(motherShip);

		return true;
	}), ADD_ROBOT("addRobot", "add robot : %d %d", 2, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		Battery[] batteries = new Battery[CREATE_COMMAND.getConfig()
				.getIntValue(BoardConfig.INT_CONFIG_ENTRY.ROBOT_MAX_BATTERIES)];
		for (int i = 0; i < batteries.length; ++i) {
			batteries[i] = new Battery(
					CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.BATTERY_CAPACITY),
					CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.BATTERY_WEIGHT));
		}
		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getRobots().add(new Robot(
				CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.ROBOT_CARGO_MAX_LOAD), batteries));

		return true;
	}), ADD_ROBOTS("addRobots", "add robots : %d %d %d", 3, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		for (int i = 0; i < Integer.valueOf(c.argumentsArray[2]); ++i) {
			Battery[] batteries = new Battery[CREATE_COMMAND.getConfig()
					.getIntValue(BoardConfig.INT_CONFIG_ENTRY.ROBOT_MAX_BATTERIES)];
			for (int j = 0; j < batteries.length; ++j) {
				batteries[j] = new Battery(
						CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.BATTERY_CAPACITY),
						CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.BATTERY_WEIGHT));
			}
			p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getRobots().add(new Robot(
					CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.ROBOT_CARGO_MAX_LOAD), batteries));
		}

		return true;
	}), CLEAR_ROBOTS("clearRobots", "clear robots : %d %d", 2, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getRobots().clear();

		return true;
	}), ADD_BATTERY("addBattery", "add battery : %d %d", 2, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getBatteries()
				.add(new Battery(CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.BATTERY_CAPACITY),
						CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.BATTERY_WEIGHT)));

		return true;
	}), ADD_CHARGING_STATION("addChargingStation", "add charging station : %d %d", 2, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getChargingStations().add(new ChargingStation(
				CREATE_COMMAND.getConfig().getIntValue(BoardConfig.INT_CONFIG_ENTRY.CHARGING_STATION_ACCESS_POINTS)));

		return true;
	}), ADD_ROCKS("addRocks", "add rocks : %d %d %d", 3, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		Integer rockWeight = Integer.valueOf(c.argumentsArray[2]);

		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].addRocks(rockWeight);

		return true;
	}), ADD_CHASM("addChasm", "add chasm : %d %d", 2, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].setChasm(true);

		return true;
	}), ADD_BLIZZARD("addBlizzard", "add blizzard : %d %d", 3, (p, c) -> {

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		Integer volume = Integer.valueOf(c.argumentsArray[2]);

		p.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getBlizzards().add(new Blizzard(volume));

		return true;
	}), CLEAR_BLIZZARDS("clearBlizzards", "clear blizzards : %d %d", 2, (p, c) -> {
		
		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);
		
		Area area = p.getSurface()[gpsCoords.getX()][gpsCoords.getY()];
		
		area.getBlizzards().clear();
		
		return true;
	}), CLEAR_AREA("clearArea", "clear area : %d %d", 2, (p, c) -> {
	

		GPSCoordinates gpsCoords = CREATE_COMMAND.getCoords(c, p);

		Area area = p.getSurface()[gpsCoords.getX()][gpsCoords.getY()];

		area.setChasm(false);
		area.setRocks(0);
		area.getBatteries().clear();
		area.getChargingStations().clear();
		area.getBlizzards().clear();

		return true;
	});

	private static GPSCoordinates getCoords(PromptCommand command, Planet planet) {

		Integer longitude = Integer.valueOf(command.argumentsArray[0]);
		Integer latitude = Integer.valueOf(command.argumentsArray[1]);

		GPSCoordinates gpsCoords = new GPSCoordinates(longitude, latitude, planet.getWidth(), planet.getHeight());

		return gpsCoords;

	}

	protected static BoardConfig config = new GameConfig();

	protected static BoardConfig getConfig() {
		return config;
	}

	public static final String MESSAGE_SEPARATOR = ":";

	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;
	public final BiFunction<Planet, PromptCommand, Boolean> action;

	private CREATE_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			BiFunction<Planet, PromptCommand, Boolean> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", CREATE_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
