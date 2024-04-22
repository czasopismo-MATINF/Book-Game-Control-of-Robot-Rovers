package game.control.robot.rovers.actions;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.board.GPSCoordinates;
import game.control.robot.rovers.board.Robot;
import game.control.robot.rovers.board.Area;
import game.control.robot.rovers.board.Battery;
import game.control.robot.rovers.command.PromptCommand;
import game.control.robot.rovers.config.BoardConfig;

class ToString {

	protected static String toString(Area area, boolean filtered) {

		StringBuffer buffer = new StringBuffer();

		if (area == null) {
			buffer.append("N/A: not visible\n");
			return buffer.toString();
		}
		if (!filtered) {
			if (WeatherEffects.preventedBy(area.getBlizzards())) {
				buffer.append("N/A: blizzards\n");
				return buffer.toString();
			}
		}

		buffer.append(String.format("rocks: %d charging stations: %d\n", area.getRocks(),
				area.getChargingStations().stream().count()));
		buffer.append(String.format("blizzards: %s\n",
				area.getBlizzards().stream().map(b -> String.valueOf(b.getVolume())).collect(Collectors.joining(" "))));
		buffer.append(String.format("chasm: %b\n", area.hasChasm()));
		buffer.append(String.format("mother-ship: %b\n", area.hasMotherShip()));
		buffer.append(String.format("markers: %s\n", area.getMarkers().stream().collect(Collectors.joining(" "))));

		buffer.append(String.format("robots: %s\n",
				area.getRobots().stream().map(r -> String.valueOf(r.getId())).collect(Collectors.joining(" "))));
		for (Battery b : area.getBatteries()) {
			buffer.append(String.format("battery: %d capacity: %d energy: %d weigth: %d\n", b.getId(), b.getCapacity(),
					b.getEnergy(), b.getWeight()));
		}

		return buffer.toString();

	}

	public static String toString(Planet planet, Area area) {

		StringBuffer buffer = new StringBuffer();

		if (WeatherEffects.preventedBy(area.getBlizzards())) {

			buffer.append("N/A: blizzards\n");

		} else {

			GPSCoordinates coords = planet.areaGPSCoordinates(area);
			if (coords == null) {
				return null;
			}

			buffer.append("C:\n");
			buffer.append(ToString.toString(planet.getArea(coords), false));
			buffer.append("E:\n");
			buffer.append(ToString.toString(planet.getArea(coords.getE()), false));
			buffer.append("S:\n");
			buffer.append(ToString.toString(planet.getArea(coords.getS()), false));
			buffer.append("W:\n");
			buffer.append(ToString.toString(planet.getArea(coords.getW()), false));
			buffer.append("N:\n");
			buffer.append(ToString.toString(planet.getArea(coords.getN()), false));

		}

		return buffer.toString();

	}

	public static String toString(Robot robot) {

		StringBuffer buffer = new StringBuffer();

		String rMsg = "ROBOT: id: %d\n";
		String bMsg = "BATTERY: id: %d capacity: %d energy: %d weight: %d\n";
		String cMsg = "CARGO: rocks: %d batteries: %d\n";

		buffer.append(String.format(rMsg, robot.getId()));

		for (int i = 0; i < robot.getBatteries().length; ++i) {
			Battery b = robot.getBatteries()[i];
			if (b != null) {
				buffer.append(String.format(bMsg, b.getId(), b.getCapacity(), b.getEnergy(), b.getWeight()));
			} else {
				buffer.append(String.format("BATTERY: empty slot\n"));
			}
		}
		buffer.append(String.format(cMsg, robot.getCargo().getRocks(), robot.getCargo().getBatteriesInCargo().size()));

		return buffer.toString();

	}

	public static String toString(GPSCoordinates coords) {

		return String.format("GPS: longitude: %d latitude: %d width: %d height: %d\n", coords.getLongitude(),
				coords.getLatitude(), coords.getWidth(), coords.getHeight());

	}

}

public enum MESSAGE_COMMAND {

	SEND_GPS_MESSAGE("sendGpsMessage", "send gps message : %d %d %s", 3, (e1, e2) -> {

		return null;
	}), SEND_MESSAGE("sendMessage", "send message : %c %s", 2, (e1, e2) -> {

		return null;
	}), LOOK_AROUND("lookAround", "look around", 0, (e1, e2) -> {

		Planet planet = e1.getKey();
		Integer currentRobotId = e1.getValue();
		if (currentRobotId == null || currentRobotId < 0) {
			return null;
		}

		Robot currentRobot = planet.getRobot(currentRobotId);
		if (currentRobot == null) {
			return null;
		}

		GPSCoordinates coords = planet.robotGPSCoordinates(currentRobotId);
		if (coords == null) {
			return null;
		}

		Area area = planet.getSurface()[coords.getX()][coords.getY()];

		int energyCost = ENERGY_COST_CALCULATOR.CONST.calculate(BoardConfig.INT_CONFIG_ENTRY.SCANING_ENERGY, null, 0,
				0);
		if (energyCost > currentRobot.getTotalEnergy()) {
			return "N/A: energy\n";
		}
		currentRobot.drainEnergy(energyCost);

		return ToString.toString(planet, area);

	}), CHECK_SELF("checkSelf", "check self", 0, (e1, e2) -> {

		Planet planet = e1.getKey();
		Integer currentRobotId = e1.getValue();
		if (currentRobotId == null || currentRobotId < 0) {
			return null;
		}

		Robot currentRobot = planet.getRobot(currentRobotId);
		if (currentRobot == null) {
			return null;
		}

		return ToString.toString(currentRobot);

	}), CHECK_GPS("checkGps", "check gps", 0, (e1, e2) -> {

		Planet planet = e1.getKey();
		Integer currentRobotId = e1.getValue();
		if (currentRobotId == null || currentRobotId < 0) {
			return null;
		}

		GPSCoordinates coords = planet.robotGPSCoordinates(currentRobotId);
		if (coords == null) {
			return null;
		}

		return ToString.toString(coords);

	});

	public enum MODE {
		PROMPT, CONCURRENT;
	};

	public static final String MESSAGE_SEPARATOR = ":";

	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;
	public final BiFunction<Map.Entry<Planet, Integer>, Map.Entry<PromptCommand, MODE>, String> action;

	private MESSAGE_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			BiFunction<Map.Entry<Planet, Integer>, Map.Entry<PromptCommand, MODE>, String> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", MESSAGE_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
