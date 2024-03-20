package game.control.robotic.rovers;

import game.control.robotic.rovers.prompt.PromptCommand;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import game.control.robotic.rovers.board.*;
import game.control.robotic.rovers.command.GameCreateCommandAnnotation;
import game.control.robotic.rovers.command.GamePlayCommandAnnotation;
import game.control.robotic.rovers.command.GameStatusCommandAnnotation;

public class ControlRobotTurnGameBoardAndCommands {

	protected class ControlRobotTurnGameConfig implements BoardConfig {

		public static int ROBOT_MAX_LOAD = 10;
		public static int ROBOT_MAX_BATTERIES = 10;

		public static int BATTERY_CAPACITY = 1000;
		public static int BATTERY_WEIGHT = 1;

		public static int CHARGING_STATION_ACCESS_POINTS = 4;

		public static int MOTHER_SHIP_MAX_LOAD = 200;

		public static int DEFAULT_PLANET_WIDTH = 10;
		public static int DEFAULT_PLANET_HEIGHT = 10;

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

	protected BoardConfig config = new ControlRobotTurnGameConfig();

	protected Planet planet = new Planet(
			ControlRobotTurnGameBoardAndCommands.ControlRobotTurnGameConfig.DEFAULT_PLANET_WIDTH,
			ControlRobotTurnGameBoardAndCommands.ControlRobotTurnGameConfig.DEFAULT_PLANET_HEIGHT);

	public Planet getPlanet() {
		return this.planet;
	}

	public void setPlanet(Planet planet) {
		this.planet = planet;
	}

	protected Map<Integer, PromptCommand[]> turnCommands = new TreeMap<>();

	protected String[][] turnCommandConfig = { { "dropCargo" }, { "dropBattery", "collectBattery", "collectRocks" },
			{ "markerNew", "markerOverwrite" },
			{ "chargeRover", "chargingStation", "distributeEnergy", "loadCargoToMotherShip", "move" },
			{ "enterMotherShip", "exitMotherShip" }, { "launch" } };

	protected class CommandMethodArgumentException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	protected void validateNumberOfArguments(PromptCommand command, Integer numberOfArguments)
			throws CommandMethodArgumentException {
		if (command.argumentsArray.length < numberOfArguments) {
			throw new CommandMethodArgumentException();
		}
	}

	protected GPSCoordinates getCoords(PromptCommand command, Planet planet) {

		Integer longitude = Integer.valueOf(command.argumentsArray[0]);
		Integer latitude = Integer.valueOf(command.argumentsArray[1]);

		GPSCoordinates gpsCoords = new GPSCoordinates(longitude, latitude, planet.getWidth(), planet.getHeight());

		return gpsCoords;

	}

//	@PromptCommandAnnotation
//	public void saveBoard(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {
//
//		validateNumberOfArguments(command, 1);
//
//		String fileName = command.argumentsArray[0];
//
//		try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
//				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
//
//			objectOutputStream.writeObject(this.planet);
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	@PromptCommandAnnotation
//	public void loadBoard(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {
//
//		validateNumberOfArguments(command, 1);
//
//		String fileName = command.argumentsArray[0];
//
//		try (FileInputStream fileInputStream = new FileInputStream(fileName);
//				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
//
//			this.planet = (Planet) objectInputStream.readObject();
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	@GameCreateCommandAnnotation
	public void planet(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		Integer width = Integer.valueOf(command.argumentsArray[0]);
		Integer height = Integer.valueOf(command.argumentsArray[1]);

		if (width <= 0 || height <= 0) {
			throw new CommandMethodArgumentException();
		}

		this.planet = new Planet(width, height);

	}

	@GameCreateCommandAnnotation
	public void addMotherShip(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		MotherShip motherShip = planet.extractMotherShip();
		if (motherShip == null)
			motherShip = new MotherShip(ControlRobotTurnGameConfig.MOTHER_SHIP_MAX_LOAD);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].setMotherShip(motherShip);

	}

	@GameCreateCommandAnnotation
	public void addRobot(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getRobots()
				.add(new Robot(ControlRobotTurnGameConfig.ROBOT_MAX_LOAD,
						ControlRobotTurnGameConfig.ROBOT_MAX_BATTERIES, this.config));

	}

	@GameCreateCommandAnnotation
	public void addRobots(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		for (int i = 0; i < Integer.valueOf(command.argumentsArray[2]); ++i) {
			planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getRobots()
					.add(new Robot(ControlRobotTurnGameConfig.ROBOT_MAX_LOAD,
							ControlRobotTurnGameConfig.ROBOT_MAX_BATTERIES, this.config));
		}

	}

	@GameCreateCommandAnnotation
	public void addBattery(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getBatteries().add(
				new Battery(ControlRobotTurnGameConfig.BATTERY_CAPACITY, ControlRobotTurnGameConfig.BATTERY_WEIGHT));

	}

	@GameCreateCommandAnnotation
	public void addChargingStation(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getChargingStations()
				.add(new ChargingStation(ControlRobotTurnGameConfig.CHARGING_STATION_ACCESS_POINTS));

	}

	@GameCreateCommandAnnotation
	public void addRocks(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		Integer rockWeight = Integer.valueOf(command.argumentsArray[2]);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].addRocks(rockWeight);

	}

	@GameCreateCommandAnnotation
	public void addChasm(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].setChasm(true);

	}

	@GameCreateCommandAnnotation
	public void addBlizzard(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		Integer volume = Integer.valueOf(command.argumentsArray[2]);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getBlizzards().add(new Blizzard(volume));

	}

//	@PromptCommandAnnotation
//	public void robot(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {
//
//		validateNumberOfArguments(command, 1);
//
//		this.currentRobotId = Integer.valueOf(command.argumentsArray[0]);
//		this.turnCommands.putIfAbsent(this.currentRobotId, new PromptCommand[this.turnCommandConfig.length]);
//
//		printer.println(this.buildRobotCommandStatus(this.currentRobotId));
//
//	}

	protected void addTurnCommand(Integer robotId, PromptCommand command) {

		for (int i = 0; i < this.turnCommandConfig.length; ++i) {
			for (int j = 0; j < this.turnCommandConfig[i].length; ++j) {
				if (this.turnCommandConfig[i][j].equals(command.camelCasedKeyWords)) {
					this.turnCommands.putIfAbsent(robotId, new PromptCommand[this.turnCommandConfig.length]);
					this.turnCommands.getOrDefault(robotId,
							new PromptCommand[this.turnCommandConfig.length])[i] = command;
				}
			}
		}

	}

	@GamePlayCommandAnnotation
	public void dropCargo(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void dropBattery(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void collectBattery(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);
		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void collectRocks(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void markerNew(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void markerOverwrite(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);
		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void chargeRover(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);
		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void chargingStation(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void distributeEnergy(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void loadCargoToMotherShip(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void move(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void enterMotherShip(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void exitMotherShip(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		this.addTurnCommand(robotId, command);

	}

	@GamePlayCommandAnnotation
	public void launch(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		this.addTurnCommand(robotId, command);

	}

	protected String buildRobotCommandStatus(int robotId) {

		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(String.valueOf(robotId)).append(":");
		for (int i = 0; i < this.turnCommandConfig.length; ++i) {
			PromptCommand c = this.turnCommands.get(robotId)[i];
			if (c != null) {
				sBuilder.append(c.command);
			} else {
				sBuilder.append("...");
			}
			if (i + 1 < this.turnCommandConfig.length) {
				sBuilder.append(" > ");
			}
		}
		return sBuilder.toString();

	}

	@GameStatusCommandAnnotation
	public String turnStatus(PromptCommand command) throws CommandMethodArgumentException {

		return this.turnCommands.entrySet().stream().map(e -> this.buildRobotCommandStatus(e.getKey()))
				.collect(Collectors.joining("\n"));

	}

	public void turnCommit(PromptCommand command) throws CommandMethodArgumentException {

	}

}
