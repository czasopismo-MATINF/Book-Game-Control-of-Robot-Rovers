package game.control.robot.rovers;

import java.util.concurrent.Callable;

import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;

import game.control.robot.rovers.config.*;

//import java.util.Random;

public class ControlRobotsTurnGameRobotAI implements Callable<Boolean> {

	protected BoardConfig config = new GameConfig();

	protected ControlRobotTurnGameConcurrentShell gameShell;

	public ControlRobotsTurnGameRobotAI(ControlRobotTurnGameConcurrentShell gameShell) {
		super();
		this.gameShell = gameShell;
	}

	protected void move(Character direction) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, direction), this
		);
	}
	
	protected void drop_cargo() {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.DROP_CARGO.messageFormat), this
		);
	}

	protected void drop_battery(int slot) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.DROP_BATTERY.messageFormat, slot), this
		);
	}

	protected int i = 0;

	public Boolean call() {
		
		/*
		 * SINGLE TURN LOGIC FOR YOUR ROBOTS
		 */

//		Random random = new Random(System.currentTimeMillis());
//		switch (random.nextInt() % 5) {
//			case 0:
//				this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'N'), this);
//				return true;
//			case 1:
//				this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'E'), this);
//				return true;
//			case 2:
//				this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'S'), this);
//				return true;
//			case 3:
//				this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, 'W'), this);
//			return true;
//			case 4 :
//			return true;
//		}
		
		this.move('E');
		this.move('N');
		this.drop_cargo();
		this.drop_battery(i++);
		
		return true;
		
	}

}
