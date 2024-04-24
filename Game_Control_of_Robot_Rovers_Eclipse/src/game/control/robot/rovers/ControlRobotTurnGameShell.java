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

	protected void saveBoard(PromptCommand command) throws IllegalArgumentException {

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

	protected void loadBoard(PromptCommand command) throws IllegalArgumentException {

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

			case "s": {
				this.saveBoard(new PromptCommand("save board : board.in"));
				return null;
			}

			case "l": {
				this.loadBoard(new PromptCommand("load board : board.in"));
				return null;
			}

			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return this.runCommand(commandLine, this.currentRobot);

	}

}
