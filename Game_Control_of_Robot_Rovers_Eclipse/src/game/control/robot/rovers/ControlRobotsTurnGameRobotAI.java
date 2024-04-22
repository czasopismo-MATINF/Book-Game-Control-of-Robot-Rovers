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

	protected void marker_new(String newMarker) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MARKER_NEW.messageFormat, newMarker), this);
	}

	protected void marker_overwrite(int markerId, String newMarker) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.MARKER_OVERWRITE.messageFormat, markerId, newMarker), this);
	}

	protected void charge_rover(int roverId, int energy) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.CHARGE_ROVER.messageFormat, roverId, energy), this);
	}

	protected void charging_station() {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.CHARGING_STATION.messageFormat), this);
	}

	protected void distribute_energy(String param) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.DISTRIBUTE_ENERGY.messageFormat, param), this);
	}

	protected int i = 0;

	public Boolean call() {

		/*
		 * SINGLE TURN LOGIC FOR YOUR ROBOTS
		 */

		Random random = new Random(System.currentTimeMillis());

		switch (random.nextInt() % 4) {

		case 0:
			this.charge_rover(random.nextInt() % 3, 10);
		case 1:
			this.charging_station();
			break;
		case 2:
			distribute_energy("full");
			break;
		case 3:
			distribute_energy("even");
			break;
		}

		return true;

	}

}
