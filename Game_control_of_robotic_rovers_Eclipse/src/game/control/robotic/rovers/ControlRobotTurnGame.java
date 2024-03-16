package game.control.robotic.rovers;

import game.control.robotic.rovers.board.Planet;
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

public class ControlRobotTurnGame {

	private Planet planet;

	public class CommandMethodNotFoundException extends Exception {
	}

	public class CommandMethodArgumentException extends Exception {
	}

	@PromptCommandAnnotation
	public void testCommand(PromptCommand command, PromptPrinterInterface printer) {
		printer.println("Test Command run.");
		printer.println(command.arguments);
	}

	@PromptCommandAnnotation
	public void saveBoard(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

		if (command.argumentsArray.length < 1) {
			throw new CommandMethodArgumentException();
		}

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
	public void loadBoard(PromptCommand command, PromptPrinterInterface printer)
			throws ClassNotFoundException, CommandMethodArgumentException {

		if (command.argumentsArray.length < 1) {
			throw new CommandMethodArgumentException();
		}

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
		}
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
