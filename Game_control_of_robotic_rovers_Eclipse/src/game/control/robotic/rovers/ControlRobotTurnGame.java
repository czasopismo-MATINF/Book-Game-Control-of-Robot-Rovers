package game.control.robotic.rovers;

import game.control.robotic.rovers.prompt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import game.control.robotic.rovers.prompt.PromptCommand;
import game.control.robotic.rovers.prompt.PromptCommandAnnotation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.util.Map;
import java.util.TreeMap;

import game.control.robotic.rovers.board.*;
import game.control.robotic.rovers.board.BoardConfig.CONFIG_ENTRIES;

public class ControlRobotTurnGame {

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

	protected Planet planet = new Planet(ControlRobotTurnGame.ControlRobotTurnGameConfig.DEFAULT_PLANET_WIDTH,
			ControlRobotTurnGame.ControlRobotTurnGameConfig.DEFAULT_PLANET_HEIGHT);

	protected BoardConfig config = new ControlRobotTurnGameConfig();

	protected int currentRobotId;

	protected Map<Integer, PromptCommand[]> turnCommands = new TreeMap<>();

	protected String[][] turnCommandConfig = { { "dropCargo" }, { "dropBattery", "collectBattery", "collectRocks" },
			{ "markerNew", "markerOverwrite" },
			{ "chargeRover", "chargingStation", "distributeEnergy", "loadCargoToMotherShip", "move" },
			{ "enterMotherShip", "exitMotherShip" }, { "launch" } };

	public class CommandMethodNotFoundException extends Exception {
	}

	public class CommandMethodArgumentException extends Exception {
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

	@PromptCommandAnnotation
	public void testCommand(PromptCommand command, PromptPrinterInterface printer) {
		printer.println("Test Command run.");
		printer.println(command.arguments);
	}

	@PromptCommandAnnotation
	public void saveBoard(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);

		String fileName = command.argumentsArray[0];

		try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

			objectOutputStream.writeObject(this.planet);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@PromptCommandAnnotation
	public void loadBoard(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);

		String fileName = command.argumentsArray[0];

		try (FileInputStream fileInputStream = new FileInputStream(fileName);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

			this.planet = (Planet) objectInputStream.readObject();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@PromptCommandAnnotation
	public void planet(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		Integer width = Integer.valueOf(command.argumentsArray[0]);
		Integer height = Integer.valueOf(command.argumentsArray[1]);

		if (width <= 0 || height <= 0) {
			throw new CommandMethodArgumentException();
		}

		this.planet = new Planet(width, height);

	}

	@PromptCommandAnnotation
	public void addMotherShip(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		MotherShip motherShip = planet.extractMotherShip();
		if (motherShip == null)
			motherShip = new MotherShip(ControlRobotTurnGameConfig.MOTHER_SHIP_MAX_LOAD);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].setMotherShip(motherShip);

	}

	@PromptCommandAnnotation
	public void addRobot(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getRobots()
				.add(new Robot(ControlRobotTurnGameConfig.ROBOT_MAX_LOAD,
						ControlRobotTurnGameConfig.ROBOT_MAX_BATTERIES, this.config));

	}

	@PromptCommandAnnotation
	public void addRobots(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		for (int i = 0; i < Integer.valueOf(command.argumentsArray[2]); ++i) {
			planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getRobots()
					.add(new Robot(ControlRobotTurnGameConfig.ROBOT_MAX_LOAD,
							ControlRobotTurnGameConfig.ROBOT_MAX_BATTERIES, this.config));
		}

	}

	@PromptCommandAnnotation
	public void addBattery(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getBatteries().add(
				new Battery(ControlRobotTurnGameConfig.BATTERY_CAPACITY, ControlRobotTurnGameConfig.BATTERY_WEIGHT));

	}

	@PromptCommandAnnotation
	public void addChargingStation(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getChargingStations()
				.add(new ChargingStation(ControlRobotTurnGameConfig.CHARGING_STATION_ACCESS_POINTS));

	}

	@PromptCommandAnnotation
	public void addRocks(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		Integer rockWeight = Integer.valueOf(command.argumentsArray[2]);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].addRocks(rockWeight);

	}

	@PromptCommandAnnotation
	public void addChasm(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].setChasm(true);

	}

	@PromptCommandAnnotation
	public void addBlizzard(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		Integer volume = Integer.valueOf(command.argumentsArray[2]);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getBlizzards().add(new Blizzard(volume));

	}

	@PromptCommandAnnotation
	public void robot(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);

		this.currentRobotId = Integer.valueOf(command.argumentsArray[0]);
		this.turnCommands.putIfAbsent(this.currentRobotId, new PromptCommand[this.turnCommandConfig.length]);

		printer.println(this.buildRobotCommandStatus(this.currentRobotId));

	}

	protected void addTurnCommand(PromptCommand command) {

		for (int i = 0; i < this.turnCommandConfig.length; ++i) {
			for (int j = 0; j < this.turnCommandConfig[i].length; ++j) {
				if (this.turnCommandConfig[i][j].equals(command.camelCasedKeyWords)) {
					this.turnCommands.putIfAbsent(this.currentRobotId,
							new PromptCommand[this.turnCommandConfig.length]);
					this.turnCommands.getOrDefault(currentRobotId,
							new PromptCommand[this.turnCommandConfig.length])[i] = command;
				}
			}
		}

	}

	@PromptCommandAnnotation
	public void dropCargo(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void dropBattery(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void collectBattery(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);
		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void collectRocks(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void markerNew(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void markerOverwrite(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);
		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void chargeRover(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 3);
		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void chargingStation(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void distributeEnergy(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void loadCargoToMotherShip(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void move(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);
		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void enterMotherShip(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void exitMotherShip(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		this.addTurnCommand(command);

	}

	@PromptCommandAnnotation
	public void launch(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		this.addTurnCommand(command);

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

	@PromptCommandAnnotation
	public void turnStatus(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		this.turnCommands.entrySet().stream().forEach(e -> {
			printer.println(this.buildRobotCommandStatus(e.getKey()));
		});

	}

	@PromptCommandAnnotation
	public void turnCommit(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

	}

	public void runCommand(PromptCommand command, PromptPrinterInterface printer) {
		try {
			Method m = this.getClass().getMethod(command.camelCasedKeyWords, PromptCommand.class,
					PromptPrinterInterface.class);
			if (m != null && m.isAnnotationPresent(PromptCommandAnnotation.class)) {
				m.invoke(this, command, printer);
			} else {
				throw new CommandMethodNotFoundException();
			}

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			printer.println(e.getCause().toString());
			e.printStackTrace();
		} catch (CommandMethodNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
	}

}
