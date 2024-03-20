package game.control.robotic.rovers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import game.control.robotic.rovers.command.GameCreateCommandAnnotation;
import game.control.robotic.rovers.command.GamePlayCommandAnnotation;
import game.control.robotic.rovers.command.GameStatusCommandAnnotation;
import game.control.robotic.rovers.prompt.PromptCommand;
import game.control.robotic.rovers.prompt.PromptPrinterInterface;

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

	public void saveBoard(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

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

	public void loadBoard(PromptCommand command, PromptPrinterInterface printer) throws CommandMethodArgumentException {

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

	public void runCommand(PromptCommand command, PromptPrinterInterface printer) {

		try {

			switch (command.camelCasedKeyWords) {

			case "robot": {
				validateNumberOfArguments(command, 1);
				this.currentRobot = Integer.valueOf(command.argumentsArray[0]);
				return;
			}

			case "saveBoard": {
				this.saveBoard(command, printer);
				return;
			}

			case "loadBoard": {
				this.loadBoard(command, printer);
				return;
			}

			}

			Method m;
			boolean noMethodPass1 = false;
			boolean noMethodPass2 = false;

			try {

				m = this.game.getClass().getMethod(command.camelCasedKeyWords, PromptCommand.class);
				if (m != null && m.isAnnotationPresent(GameCreateCommandAnnotation.class)) {

					m.invoke(this.game, command);
					return;

				}
				if (m != null && m.isAnnotationPresent(GameStatusCommandAnnotation.class)) {

					String response = (String) m.invoke(this.game, command);
					printer.println(response);
					return;

				}

			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				noMethodPass1 = true;
			}

			try {

				m = this.game.getClass().getMethod(command.camelCasedKeyWords, Integer.class, PromptCommand.class);
				if (m != null && m.isAnnotationPresent(GamePlayCommandAnnotation.class)) {

					m.invoke(this.game, this.currentRobot, command);
					return;

				}

			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				noMethodPass2 = true;
			}

			if (noMethodPass1 && noMethodPass2) {
				printer.println("NO SUCH METHOD.");
			} else {
				printer.println("METHOD NOT INVOKED");
			}

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
			e.printStackTrace();
		} catch (CommandMethodArgumentException e) {
			if (e.getCause() != null) {
				printer.println(e.getCause().toString());
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

	}

}
