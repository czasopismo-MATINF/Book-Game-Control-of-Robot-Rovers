package game.control.robot.rovers;

import java.util.concurrent.Callable;

import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;
import game.control.robot.rovers.actions.MESSAGE_COMMAND;
import game.control.robot.rovers.config.*;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ControlRobotsTurnGameRobotAI implements Callable<Boolean> {

	protected BoardConfig config = new GameConfig();

	protected ControlRobotTurnGameConcurrentShell gameShell;

	public BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

	public ControlRobotsTurnGameRobotAI(ControlRobotTurnGameConcurrentShell gameShell) {
		super();
		this.gameShell = gameShell;
	}
	
	/**********/
	
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

	protected String load_cargo_to_mother_ship() {
		return this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.LOAD_CARGO_TO_MOTHER_SHIP.messageFormat),
				this);
	}	
	
	protected void move(Character direction) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, direction), this);
	}
	
	protected String enter_mother_ship() {
		return this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.ENTER_MOTHER_SHIP.messageFormat), this);
	}
	
	protected String launch() {
		return this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.LAUNCH.messageFormat), this);
	}
	
	/**********/
	
	protected String look_around() {
		return this.gameShell.runCommand(String.format(MESSAGE_COMMAND.LOOK_AROUND.messageFormat), this);
	}

	protected String check_self() {
		return this.gameShell.runCommand(String.format(MESSAGE_COMMAND.CHECK_SELF.messageFormat), this);
	}

	protected String check_gps() {
		return this.gameShell.runCommand(String.format(MESSAGE_COMMAND.CHECK_GPS.messageFormat), this);
	}

	protected void send_message(char direction, String message, int power) {
		this.gameShell.runCommand(String.format(MESSAGE_COMMAND.SEND_MESSAGE.messageFormat, direction, message, power),
				this);
	}

	protected void send_gps_message(int longitude, int latitude, String message, int power) {
		this.gameShell.runCommand(
				String.format(MESSAGE_COMMAND.SEND_GPS_MESSAGE.messageFormat, longitude, latitude, message, power),
				this);
	}
	
	/**********/

	public Boolean call() {

		/*
		 * SINGLE TURN LOGIC FOR YOUR ROBOTS
		 */

		Random random = new Random(System.currentTimeMillis());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String msg = this.messageQueue.poll();
		if (msg != null) {
			System.out.println(msg);
		}

		this.send_message('C', "hello", 30);
		this.send_gps_message(5, 0, "hello_gps", 10);

		this.move("ESWN".charAt(random.nextInt(0, 4)));

		return true;

	}

}
