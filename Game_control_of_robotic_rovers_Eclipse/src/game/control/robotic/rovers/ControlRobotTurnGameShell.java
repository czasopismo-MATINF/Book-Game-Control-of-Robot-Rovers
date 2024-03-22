package game.control.robotic.rovers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import game.control.robotic.rovers.command.GameCreateCommandAnnotation;
import game.control.robotic.rovers.command.GamePlayCommandAnnotation;
import game.control.robotic.rovers.command.GameStatusCommandAnnotation;
import game.control.robotic.rovers.command.PromptCommand;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import game.control.robotic.rovers.board.*;

public class ControlRobotTurnGameShell {

	protected ControlRobotTurnGameBoardAndCommands game = new ControlRobotTurnGameBoardAndCommands();

	protected int currentRobot = 0;

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

	public void saveBoard(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);

		String fileName = command.argumentsArray[0];

		try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

			objectOutputStream.writeObject(this.game.getPlanet());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void loadBoard(PromptCommand command) throws CommandMethodArgumentException {

		validateNumberOfArguments(command, 1);

		String fileName = command.argumentsArray[0];

		try (FileInputStream fileInputStream = new FileInputStream(fileName);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

			this.game.setPlanet((Planet) objectInputStream.readObject());

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

//	public void runCommand(PromptCommand command, PromptPrinterInterface printer) {
//
//		try {
//
//			switch (command.camelCasedKeyWords) {
//
//			case "robot": {
//				validateNumberOfArguments(command, 1);
//				this.currentRobot = Integer.valueOf(command.argumentsArray[0]);
//				return;
//			}
//
//			case "saveBoard": {
//				this.saveBoard(command, printer);
//				return;
//			}
//
//			case "loadBoard": {
//				this.loadBoard(command, printer);
//				return;
//			}
//
//			}
//
//			Method m;
//
//			try {
//				m = this.game.getClass().getMethod(command.camelCasedKeyWords, PromptCommand.class);
//			} catch (NoSuchMethodException e) {
//				// TODO Auto-generated catch block
//				// e.printStackTrace();
//				m = null;
//			}
//			if (m != null && m.isAnnotationPresent(GameCreateCommandAnnotation.class)) {
//
//				m.invoke(this.game, command);
//				return;
//
//			}
//			if (m != null && m.isAnnotationPresent(GameStatusCommandAnnotation.class)) {
//
//				String response = (String) m.invoke(this.game, command);
//				printer.println(response);
//				return;
//
//			}
//
//			try {
//				m = this.game.getClass().getMethod(command.camelCasedKeyWords, Integer.class, PromptCommand.class);
//			} catch (NoSuchMethodException e) {
//				// TODO Auto-generated catch block
//				// e.printStackTrace();
//				m = null;
//			}
//			if (m != null && m.isAnnotationPresent(GamePlayCommandAnnotation.class)) {
//
//				m.invoke(this.game, this.currentRobot, command);
//				return;
//
//			}
//			if (m != null && m.isAnnotationPresent(GameStatusCommandAnnotation.class)) {
//
//				String response = (String) m.invoke(this.game, this.currentRobot, command);
//				printer.println(response);
//				return;
//
//			}
//
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CommandMethodArgumentException e) {
//			if (e.getCause() != null) {
//				printer.println(e.getCause().toString());
//			}
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//
//		}
//
//	}
	
	public String runCommandThrowsExceptions(String commandLine, int currentRobot) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		PromptCommand command = new PromptCommand(commandLine);
		
		Method m = null;

		try {
			m = this.game.getClass().getMethod(command.camelCasedKeyWords, PromptCommand.class);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m != null && m.isAnnotationPresent(GameCreateCommandAnnotation.class)) {

			m.invoke(this.game, command);
			return null;

		}
		if (m != null && m.isAnnotationPresent(GameStatusCommandAnnotation.class)) {

			return (String) m.invoke(this.game, command);

		}

		m = null;

		try {
			m = this.game.getClass().getMethod(command.camelCasedKeyWords, Integer.class, PromptCommand.class);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m != null && m.isAnnotationPresent(GamePlayCommandAnnotation.class)) {

			m.invoke(this.game, this.currentRobot, command);
			return null;

		}
		if (m != null && m.isAnnotationPresent(GameStatusCommandAnnotation.class)) {

			return (String) m.invoke(this.game, this.currentRobot, command);

		}
		
		return null;

	}
	
	public String runCommand(String commandLine, int currentRobot) {
		
		try {
			return this.runCommandThrowsExceptions(commandLine, currentRobot);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public String runCommand(String commandLine) {

		PromptCommand command = new PromptCommand(commandLine);

		try {
		
		switch (command.camelCasedKeyWords) {

		case "robot": {
			validateNumberOfArguments(command, 1);
			this.currentRobot = Integer.valueOf(command.argumentsArray[0]);
			return null;
		}

		case "saveBoard": {
			this.saveBoard(command);
			return null;
		}

		case "loadBoard": {
			this.loadBoard(command);
			return null;
		}

		}
		
		} catch (CommandMethodArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return this.runCommand(commandLine, this.currentRobot);

	}

}
