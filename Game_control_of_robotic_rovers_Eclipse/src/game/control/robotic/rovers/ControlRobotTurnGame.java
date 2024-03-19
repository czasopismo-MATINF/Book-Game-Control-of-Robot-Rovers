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

import game.control.robotic.rovers.board.*;

public class ControlRobotTurnGame {

	private Planet planet;

	private BoardConfig config = new ControlRobotTurnGameConfig();

	public class CommandMethodNotFoundException extends Exception {
	}

	public class CommandMethodArgumentException extends Exception {
	}

	void validateNumberOfArguments(PromptCommand command, Integer numberOfArguments)
			throws CommandMethodArgumentException {
		if (command.argumentsArray.length < numberOfArguments) {
			throw new CommandMethodArgumentException();
		}
	}

	GPSCoordinates getCoords(PromptCommand command, Planet planet) {

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
	public void addBattery(PromptCommand command, PromptPrinterInterface printer)
			throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 2);

		GPSCoordinates gpsCoords = this.getCoords(command, planet);

		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getBatteries().add(
				new Battery(ControlRobotTurnGameConfig.BATTERY_CAPACITY, ControlRobotTurnGameConfig.BATTERY_WEIGHT));

	}

	@PromptCommandAnnotation
	public void addChargingStation(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {
		
		validateNumberOfArguments(command, 2);
		
		GPSCoordinates gpsCoords = this.getCoords(command, planet);
		
		planet.getSurface()[gpsCoords.getX()][gpsCoords.getY()].getChargingStations().add(
				new ChargingStation(ControlRobotTurnGameConfig.CHARGING_STATION_ACCESS_POINTS)
		);
		
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
