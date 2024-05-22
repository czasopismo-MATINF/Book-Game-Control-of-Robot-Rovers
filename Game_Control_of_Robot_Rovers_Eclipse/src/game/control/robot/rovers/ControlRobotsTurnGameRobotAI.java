package game.control.robot.rovers;

import java.util.concurrent.Callable;

import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;
import game.control.robot.rovers.actions.MESSAGE_COMMAND;
import game.control.robot.rovers.config.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	protected int getRobotId() {
		
		try {
			Pattern p = Pattern.compile("^ROBOT: id: (\\d+)", Pattern.MULTILINE);
			Matcher m  = p.matcher(this.check_self());
			m.find();
			return Integer.valueOf(m.group(1));
		} catch(IllegalStateException e) {
			return -1;
		}
		
	}

	public Boolean call() {

		return true;

	}

}
