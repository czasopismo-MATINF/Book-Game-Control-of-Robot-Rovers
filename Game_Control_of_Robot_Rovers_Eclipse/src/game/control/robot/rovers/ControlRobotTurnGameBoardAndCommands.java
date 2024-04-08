package game.control.robot.rovers;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import game.control.robot.rovers.board.*;
import game.control.robot.rovers.command.CommandMethodArgumentException;
import game.control.robot.rovers.command.GameCreateCommandAnnotation;
import game.control.robot.rovers.command.GamePlayCommandAnnotation;
import game.control.robot.rovers.command.GameStatusCommandAnnotation;
import game.control.robot.rovers.command.PromptCommand;

import java.util.concurrent.ConcurrentHashMap;

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

		public static String MESSAGE_SEPARATOR = "\n";

		public static int GPS_MESSAGE_ENERGY = 20;
		public static int LOCAL_MESSAGE_ENERGY = 5;

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

	protected Map<Integer, PromptCommand[]> turnCommands = new ConcurrentHashMap<>();
	
	enum TCP { //TURN COMMIT PHASES
		
		DROP_CARGO(0),
		DROP_COLLECT(1),
		MARKER(2),
		MOVE(3),
		ENTER_EXIT_MOTHER_SHIP(4),
		LAUNCH(5);
		
		private int phaseNumber;
		
		private TCP(int phaseNumber) {
			this.phaseNumber = phaseNumber;
		}
		
		public int gPN() {	// getPhaseNumber
			return this.phaseNumber;
		}
		
	}
	
	protected COMMAND[][] turnCommandConfig = {
		{COMMAND.DROP_CARGO},
		{COMMAND.DROP_BATTERY, COMMAND.COLLECT_BATTERY, COMMAND.COLLECT_ROCKS},
		{COMMAND.MARKER_NEW, COMMAND.MARKER_OVERWRITE},
		{COMMAND.CHARGE_ROVER, COMMAND.CHARGING_STATION, COMMAND.DISTRIBUTE_ENERGY, COMMAND.LOAD_CARGO_TO_MOTHER_SHIP, COMMAND.MOVE},
		{COMMAND.ENTER_MOTHER_SHIP, COMMAND.EXIT_MOTHER_SHIP},
		{COMMAND.LAUNCH}
	};

	public Planet getPlanet() {
		return this.planet;
	}

	public void setPlanet(Planet planet) {
		this.planet = planet;
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

	@GameStatusCommandAnnotation
	public String sendGpsMessage(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {
		return null;
	}

	@GameStatusCommandAnnotation
	public String sendMessage(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {
		return null;
	}

	@GameStatusCommandAnnotation
	public String lookAround(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {
		return null;
	}

	@GameStatusCommandAnnotation
	public String checkSelf(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {

		var robot = this.planet.getRobot(robotId);
		if (robot == null)
			return null;

		var sBuilder = new StringBuilder();

		sBuilder.append(String.format("cargo {load:%d}", robot.getCargo().load()));
		Arrays.asList(robot.getBatteries()).stream().forEach(b -> {
			sBuilder.append(ControlRobotTurnGameConfig.MESSAGE_SEPARATOR);
			sBuilder.append(String.format("battery {energy:%d;capacity:%d;weigth:%d}", b.getEnergy(), b.getCapacity(),
					b.getWeight()));
		});

		return sBuilder.toString();
	}
	

	@GameStatusCommandAnnotation
	public String checkGPS(Integer robotId, PromptCommand command) throws CommandMethodArgumentException {
		return null;
	}
	

	protected void addTurnCommand(Integer robotId, PromptCommand command) {

		for (int i = 0; i < this.turnCommandConfig.length; ++i) {
			for (int j = 0; j < this.turnCommandConfig[i].length; ++j) {
				if (this.turnCommandConfig[i][j].camelCasedName.equals(command.camelCasedKeyWords)) {
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
	

	protected void movePhase() {

		this.turnCommands.entrySet().stream().forEach(e -> {
			if(COMMAND.valueOf(e.getValue()[TCP.MOVE.gPN()].camelCasedKeyWords.toUpperCase()) == COMMAND.MOVE) {
				this.planet.moveRobot(e.getKey(), e.getValue()[TCP.MOVE.gPN()].argumentsArray[0]);
			}
		});
		
	}
	
	
	@GameStatusCommandAnnotation
	public synchronized String turnCommit(PromptCommand command) throws CommandMethodArgumentException {

		movePhase();
		
		return null;
	}

}
