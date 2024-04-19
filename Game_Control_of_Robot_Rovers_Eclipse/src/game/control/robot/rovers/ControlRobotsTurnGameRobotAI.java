package game.control.robot.rovers;

import java.util.concurrent.Callable;

import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;

import game.control.robot.rovers.config.*;

import java.util.Random;

public class ControlRobotsTurnGameRobotAI implements Callable<Boolean> {

	protected BoardConfig config = new GameConfig();

	protected ControlRobotTurnGameConcurrentShell gameShell;

	public ControlRobotsTurnGameRobotAI(ControlRobotTurnGameConcurrentShell gameShell) {
		super();
		this.gameShell = gameShell;
	}

	protected void move(Character direction) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, direction), this);
	}

	protected void drop_cargo() {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.DROP_CARGO.messageFormat), this);
	}

	protected void drop_battery(int slot) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.DROP_BATTERY.messageFormat, slot), this);
	}

	protected void collect_battery_to_cargo(int batteryNumber) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.COLLECT_BATTERY.messageFormat, batteryNumber, "to", "cargo"), this);
	}

	protected void collect_battery_to_slot(int batteryNumber) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.COLLECT_BATTERY.messageFormat, batteryNumber, "to", "slot"), this);
	}

	protected void collect_rocks(int rocks) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.COLLECT_ROCKS.messageFormat, rocks), this);
	}

	protected int i = 0;

	public Boolean call() {

		/*
		 * SINGLE TURN LOGIC FOR YOUR ROBOTS
		 */

		Random random = new Random(System.currentTimeMillis());
		switch (random.nextInt() % 6) {
		case 0: {
			this.drop_battery(random.nextInt() % 15);
		}
			break;
		case 1: {
			this.drop_cargo();
		}
			break;
		case 2: {
			switch (random.nextInt() % 4) {
			case 0: {
				this.move('E');
			}
				break;
			case 1: {
				this.move('S');
			}
				break;
			case 2: {
				this.move('W');
			}
				break;
			case 3: {
				this.move('N');
			}
				break;
			}
		}
			break;
		case 3: {
			this.collect_battery_to_cargo(random.nextInt(2));
		}
			break;
		case 4: {
			this.collect_battery_to_slot(random.nextInt(2));
		}
		case 5: {
			this.collect_rocks(5);
		}

		}

		return true;

	}

}
