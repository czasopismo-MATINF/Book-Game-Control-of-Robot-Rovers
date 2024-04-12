package game.control.robot.rovers;

import java.util.concurrent.Callable;

import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;

import java.util.Random;

public class ControlRobotsTurnGameRobotAI implements Callable<Boolean> {

	ControlRobotTurnGameConcurrentShell gameShell;
	
	public ControlRobotsTurnGameRobotAI(ControlRobotTurnGameConcurrentShell gameShell) {
		super();
		this.gameShell = gameShell;
	}

	public Boolean call() {
		
		/*
		 * SINGLE TURN LOGIC FOR YOUR ROBOTS
		 */

		Random random = new Random();
		switch (random.nextInt() % 5) {
		case 0:
			this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'N'), this);
			return true;
		case 1:
			this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'E'), this);
			return true;
		case 2:
			this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'S'), this);
			return true;
		case 3:
			this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'W'), this);
		return true;
		case 4 :
		return true;
		}
		
		return true;
		
	}

}
