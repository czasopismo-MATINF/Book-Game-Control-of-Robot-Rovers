package game.control.robot.rovers.actions;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.board.GPSCoordinates;
import game.control.robot.rovers.board.Robot;
import game.control.robot.rovers.ControlRobotsTurnGameRobotAI;
import game.control.robot.rovers.board.Area;
import game.control.robot.rovers.board.Battery;
import game.control.robot.rovers.board.Blizzard;
import game.control.robot.rovers.command.PromptCommand;
import game.control.robot.rovers.config.BoardConfig;
import java.util.Random;

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

class WeatherEffects2 {

	protected static String characters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890-=!@#$%^&*()_+/'][;.,?\"}{:><";

	protected static Character randomCharacter() {
		Random random = new Random(System.currentTimeMillis());
		return WeatherEffects2.characters.charAt(random.nextInt(0, WeatherEffects2.characters.length()));
	}

	public static Character transferCharacter(Character c, List<Blizzard> blizzards, int power) {
		Character ret = c;
		Random random = new Random(System.currentTimeMillis());
		boolean success = true;
		for (Blizzard b : blizzards) {
			if (random.nextInt(0, (int) ((1.0 / ((double) power)) * b.getVolume())) >= 1) {
				success = false;
			}
		}
		if (!success) {
			ret = WeatherEffects2.randomCharacter();
		}
		return ret;
	}

}

public enum MESSAGE_COMMAND {

	SEND_GPS_MESSAGE("sendGpsMessage", "send gps message : %d %d %s power %d", 5,
			(planet, currentRobotId, promptCommand, mode, robotAIs) -> {
				Robot robot = planet.getRobot(currentRobotId);
				if (robot == null) {
					return null;
				}

				int longitude = Integer.valueOf(promptCommand.argumentsArray[0]);
				int latitude = Integer.valueOf(promptCommand.argumentsArray[1]);

				if (!"POWER".equals(String.valueOf(promptCommand.argumentsArray[3]).toUpperCase())) {
					return null;
				}

				int power = Integer.valueOf(promptCommand.argumentsArray[4]);
				if (power <= 0) {
					return null;
				}

				GPSCoordinates coords = planet.robotGPSCoordinates(currentRobotId);
				if (coords == null) {
					return null;
				}

				Area areaC = planet.getSurface()[coords.getX()][coords.getY()];

				GPSCoordinates targetCoordinates = new GPSCoordinates(longitude, latitude, planet.getWidth(),
						planet.getHeight());

				Area areaT = null;
				areaT = planet.getSurface()[targetCoordinates.getX()][targetCoordinates.getY()];

				String message = String.valueOf(promptCommand.argumentsArray[2]);

				StringBuffer messageThroughC = new StringBuffer();
				for (int i = 0; i < message.length(); ++i) {
					int drained = robot.drainEnergy(power);
					if (drained < power) {
						break;
					}
					messageThroughC
							.append(WeatherEffects2.transferCharacter(message.charAt(i), areaC.getBlizzards(), power));
				}

				message = messageThroughC.toString();

				StringBuffer output = new StringBuffer();
				if (areaT != null) {

					for (Robot r : areaT.getRobots()) {

						StringBuffer messageForRobot = new StringBuffer();
						for (int i = 0; i < message.length(); ++i) {
							messageForRobot.append(
									WeatherEffects2.transferCharacter(message.charAt(i), areaT.getBlizzards(), power));
						}

						if (mode == MODE.PROMPT) {
							output.append(String.format("robot: %d got message : %s\n", r.getId(),
									messageForRobot.toString()));

						} else if (mode == MODE.CONCURRENT) {
							ControlRobotsTurnGameRobotAI targetRobotAI = robotAIs.get(r.getId());
							if (targetRobotAI != null) {
								try {
									targetRobotAI.messageQueue.put(messageForRobot.toString());
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

					}

				}

				return output.toString();
			}),
	SEND_MESSAGE("sendMessage", "send message : %c %s power %d", 4,
			(planet, currentRobotId, promptCommand, mode, robotAIs) -> {

				Robot robot = planet.getRobot(currentRobotId);
				if (robot == null) {
					return null;
				}

				if (!"CESWN".contains(String.valueOf(promptCommand.argumentsArray[0]).toUpperCase())) {
					return null;
				}

				if (!"POWER".equals(String.valueOf(promptCommand.argumentsArray[2]).toUpperCase())) {
					return null;
				}

				int power = Integer.valueOf(promptCommand.argumentsArray[3]);
				if (power <= 0) {
					return null;
				}

				GPSCoordinates coords = planet.robotGPSCoordinates(currentRobotId);
				if (coords == null) {
					return null;
				}

				Area areaC = planet.getSurface()[coords.getX()][coords.getY()];

				GPSCoordinates targetCoordinates = null;
				switch (String.valueOf(promptCommand.argumentsArray[0]).toUpperCase()) {
				case "E":
					targetCoordinates = coords.getE();
					break;
				case "S":
					targetCoordinates = coords.getS();
					break;
				case "W":
					targetCoordinates = coords.getW();
					break;
				case "N":
					targetCoordinates = coords.getN();
					break;
				case "C":
					targetCoordinates = coords;
					break;
				}

				Area areaT = null;
				if (targetCoordinates != null) {
					areaT = planet.getSurface()[targetCoordinates.getX()][targetCoordinates.getY()];
				}

				String message = String.valueOf(promptCommand.argumentsArray[1]);

				StringBuffer messageThroughC = new StringBuffer();
				for (int i = 0; i < message.length(); ++i) {
					int drained = robot.drainEnergy(power);
					if (drained < power) {
						break;
					}
					messageThroughC
							.append(WeatherEffects2.transferCharacter(message.charAt(i), areaC.getBlizzards(), power));
				}

				message = messageThroughC.toString();

				StringBuffer output = new StringBuffer();
				if (areaT != null) {

					for (Robot r : areaT.getRobots()) {

						if (robot == r) {
							continue;
						}

						StringBuffer messageForRobot = new StringBuffer();
						for (int i = 0; i < message.length(); ++i) {
							messageForRobot.append(
									WeatherEffects2.transferCharacter(message.charAt(i), areaT.getBlizzards(), power));
						}

						if (mode == MODE.PROMPT) {
							output.append(String.format("robot: %d got message : %s\n", r.getId(),
									messageForRobot.toString()));

						} else if (mode == MODE.CONCURRENT) {
							ControlRobotsTurnGameRobotAI targetRobotAI = robotAIs.get(r.getId());
							if (targetRobotAI != null) {
								try {
									targetRobotAI.messageQueue.put(messageForRobot.toString());
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

					}

				}

				return output.toString();
			}),
	LOOK_AROUND("lookAround", "look around", 0, (planet, currentRobotId, promptCommand, mode, robotAIs) -> {

		Robot currentRobot = planet.getRobot(currentRobotId);
		if (currentRobot == null) {
			return null;
		}

		GPSCoordinates coords = planet.robotGPSCoordinates(currentRobotId);
		if (coords == null) {
			return null;
		}

		Area area = planet.getSurface()[coords.getX()][coords.getY()];

		int energyCost = ENERGY_COST_CALCULATOR.CONST.calculate(BoardConfig.INT_CONFIG_ENTRY.SCAN_ENERGY, null, 0,
				0);
		if (energyCost > currentRobot.getTotalEnergy()) {
			return "N/A: energy\n";
		}
		currentRobot.drainEnergy(energyCost);

		return ToString.toString(planet, area);

	}), CHECK_SELF("checkSelf", "check self", 0, (planet, currentRobotId, promptCommand, mode, robotAIs) -> {

		Robot currentRobot = planet.getRobot(currentRobotId);
		if (currentRobot == null) {
			return null;
		}

		return ToString.toString(currentRobot);

	}), CHECK_GPS("checkGps", "check gps", 0, (planet, currentRobotId, promptCommand, mode, robotAIs) -> {

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

	// public final BiFunction<Map.Entry<Planet, Integer>, Map.Entry<PromptCommand,
	// MODE>, String> action;
	@FunctionalInterface
	public interface F5<A, B, C, D, E, F> {
		public F apply(A a, B b, C c, D d, E e);
	}

	public final F5<Planet, Integer, PromptCommand, MODE, Map<Integer, ControlRobotsTurnGameRobotAI>, String> action;

	private MESSAGE_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			F5<Planet, Integer, PromptCommand, MODE, Map<Integer, ControlRobotsTurnGameRobotAI>, String> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", MESSAGE_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
