package game.control.robot.rovers.actions;

import java.util.Map;

import game.control.robot.rovers.board.*;
import game.control.robot.rovers.ControlRobotsTurnGameRobotAI;
import game.control.robot.rovers.command.PromptCommand;
import game.control.robot.rovers.config.BoardConfig;

public enum MESSAGE_COMMAND {

	SEND_GPS_MESSAGE("sendGpsMessage", "send gps message : %d %d %s power %d", 5,
			(planet, currentRobotId, promptCommand, mode, robotAIs) -> {

				String rMsg = "robot: %d got message : %s\n";

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

				Area areaT = planet.getSurface()[targetCoordinates.getX()][targetCoordinates.getY()];

				String message = String.valueOf(promptCommand.argumentsArray[2]);

				StringBuffer messageThroughC = new StringBuffer();
				for (int i = 0; i < message.length(); ++i) {
					int drained = robot.drainEnergy(power);
					if (drained < power) {
						break;
					}
					messageThroughC
							.append(WEATHER_EFFECTS.transferCharacter(message.charAt(i), areaC.getBlizzards(), power));
				}
				message = messageThroughC.toString();

				StringBuffer output = new StringBuffer();

				if (areaT == null) {
					return output.toString();
				}

				for (Robot r : areaT.getRobots()) {

					StringBuffer messageToRobot = new StringBuffer();
					for (int i = 0; i < message.length(); ++i) {
						messageToRobot
								.append(WEATHER_EFFECTS.transferCharacter(message.charAt(i), areaT.getBlizzards(), 1));
					}

					if (mode == MODE.PROMPT) {
						output.append(String.format(rMsg, r.getId(), messageToRobot.toString()));
					} else if (mode == MODE.CONCURRENT) {
						ControlRobotsTurnGameRobotAI targetRobotAI = robotAIs.get(r.getId());
						if (targetRobotAI != null) {
							try {
								targetRobotAI.messageQueue.put(messageToRobot.toString());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				}

				return output.toString();
			}),
	SEND_MESSAGE("sendMessage", "send message : %c %s power %d", 4,
			(planet, currentRobotId, promptCommand, mode, robotAIs) -> {

				String rMsg = "robot: %d got message : %s\n";

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
							.append(WEATHER_EFFECTS.transferCharacter(message.charAt(i), areaC.getBlizzards(), power));
				}

				message = messageThroughC.toString();

				StringBuffer output = new StringBuffer();

				if (areaT == null) {
					return null;
				}

				for (Robot r : areaT.getRobots()) {

					if (robot == r) {
						continue;
					}

					StringBuffer messageToRobot = new StringBuffer();
					for (int i = 0; i < message.length(); ++i) {
						messageToRobot.append(
								WEATHER_EFFECTS.transferCharacter(message.charAt(i), areaT.getBlizzards(), power));
					}

					if (mode == MODE.PROMPT) {
						output.append(String.format(rMsg, r.getId(), messageToRobot.toString()));
					} else if (mode == MODE.CONCURRENT) {
						ControlRobotsTurnGameRobotAI targetRobotAI = robotAIs.get(r.getId());
						if (targetRobotAI != null) {
							try {
								targetRobotAI.messageQueue.put(messageToRobot.toString());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				}

				return output.toString();
			}),
	LOOK_AROUND("lookAround", "look around", 0, (planet, currentRobotId, promptCommand, mode, robotAIs) -> {

		String naMsg = "N/A: energy\n";
		
		if (currentRobotId == null) {
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

		int energyCost = ENERGY_COST_CALCULATOR.CONST.calculate(BoardConfig.INT_CONFIG_ENTRY.SCAN_ENERGY, null, 0, 0);
		if (energyCost > currentRobot.getTotalEnergy()) {
			return naMsg;
		}
		currentRobot.drainEnergy(energyCost);

		return TO_STRING.toString(planet, area);

	}), CHECK_SELF("checkSelf", "check self", 0, (planet, currentRobotId, promptCommand, mode, robotAIs) -> {

		if (currentRobotId == null) {
			return null;
		}

		Robot currentRobot = planet.getRobot(currentRobotId);
		if (currentRobot == null) {
			return null;
		}

		return TO_STRING.toString(currentRobot);

	}), CHECK_GPS("checkGps", "check gps", 0, (planet, currentRobotId, promptCommand, mode, robotAIs) -> {

		if (currentRobotId == null) {
			return null;
		}

		GPSCoordinates coords = planet.robotGPSCoordinates(currentRobotId);
		if (coords == null) {
			return null;
		}

		return TO_STRING.toString(coords);

	});

	public enum MODE {
		PROMPT, CONCURRENT;
	};

	public static final String MESSAGE_SEPARATOR = ":";

	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;

	public final HELPER_CLASSES.F5<Planet, Integer, PromptCommand, MODE, Map<Integer, ControlRobotsTurnGameRobotAI>, String> action;

	private MESSAGE_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			HELPER_CLASSES.F5<Planet, Integer, PromptCommand, MODE, Map<Integer, ControlRobotsTurnGameRobotAI>, String> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", MESSAGE_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
