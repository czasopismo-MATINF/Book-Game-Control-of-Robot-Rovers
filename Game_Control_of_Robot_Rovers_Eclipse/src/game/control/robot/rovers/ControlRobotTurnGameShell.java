package game.control.robot.rovers;

import game.control.robot.rovers.actions.CREATE_COMMAND;
import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;
import game.control.robot.rovers.actions.MESSAGE_COMMAND;
import game.control.robot.rovers.actions.STATUS_COMMAND;
import game.control.robot.rovers.actions.TURN_COMMIT_COMMAND;
import game.control.robot.rovers.board.*;
import game.control.robot.rovers.command.PromptCommand;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.util.Scanner;
import java.io.IOException;

public class ControlRobotTurnGameShell {

	protected ControlRobotTurnGameBoardAndCommands game = new ControlRobotTurnGameBoardAndCommands();

	protected int currentRobot = 0;

	protected void validateNumberOfArguments(PromptCommand command, Integer numberOfArguments)
			throws IllegalArgumentException {
		if (command.argumentsArray.length < numberOfArguments) {
			throw new IllegalArgumentException();
		}
	}

	protected String saveBoard(PromptCommand command) throws IllegalArgumentException {

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

		return null;

	}

	protected String loadBoard(PromptCommand command) throws IllegalArgumentException {

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

		return null;

	}

	protected String s(PromptCommand command) throws IllegalArgumentException {

		return this.saveBoard(new PromptCommand("save board " + PromptCommand.SPLIT_KEYWORDS_ARGUMENTS + " board.in"));

	}

	protected String l(PromptCommand command) throws IllegalArgumentException {

		return this.loadBoard(new PromptCommand("load board " + PromptCommand.SPLIT_KEYWORDS_ARGUMENTS + " board.in"));
	}

	protected String runScript(PromptCommand command) throws IllegalArgumentException {

		validateNumberOfArguments(command, 1);
		
		try(Scanner scanner = new Scanner(new File(command.argumentsArray[0]))) {
			
			while(scanner.hasNextLine()) {
				
				this.runCommand(scanner.nextLine());
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;

	}

	protected String rs(PromptCommand command) {
		
		return this.runScript(new PromptCommand("run script " + PromptCommand.SPLIT_KEYWORDS_ARGUMENTS + " create_example_planet.script"));
		
	}
	
	protected String robot(PromptCommand command) throws IllegalArgumentException {

		validateNumberOfArguments(command, 1);

		this.currentRobot = Integer.valueOf(command.argumentsArray[0]);

		return null;

	}

	protected String runCommand(String commandLine, int currentRobot) {

		PromptCommand promptCommand = new PromptCommand(commandLine);

		try {

			return this.game.runMessageCommand(MESSAGE_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords),
					MESSAGE_COMMAND.MODE.PROMPT, promptCommand, currentRobot, null);

		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
		}

		try {

			this.game.runEndOfTurnCommand(END_OF_TURN_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords),
					promptCommand, currentRobot);

		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
		}

		try {

			return this.game.runStatusCommand(STATUS_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords),
					promptCommand);

		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
		}

		try {

			CREATE_COMMAND command = CREATE_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords);
			this.validateNumberOfArguments(promptCommand, command.numberOfArguments);
			return this.game.runCreateCommand(command, promptCommand);

		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
		}

		try {

			return this.game.runTurnCommitCommand(TURN_COMMIT_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords),
					promptCommand);

		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
		}

		return null;

	}

	public String runCommand(String commandLine) {

		PromptCommand command = new PromptCommand(commandLine);

		try {

			return (String) this.getClass().getDeclaredMethod(command.camelCasedKeyWords, PromptCommand.class)
					.invoke(this, command);

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return this.runCommand(commandLine, this.currentRobot);

	}

}
