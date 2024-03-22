package game.control.robotic.rovers;

import java.util.Map;
import java.util.concurrent.Callable;

public class RobotAI implements Callable<Boolean> {

	ControlRobotConcurrentTurnGameShell gameShell;

	enum MESSAGES {
		SEND_GPS_MESSAGE, SEND_MESSAGE, LOOK_AROUND, CHECK_SELF, CHECK_GPS,
		DROP_CARGO,
		DROP_BATTERY, COLLECT_BATTERY, COLLECT_ROCKS,
		MARKER_NEW, MARKER_OVERWRITE,
		CHARGE_ROVER, CHARGING_STATION, DISTRIBUTE_ENERGY, LOAD_CARGO_TO_MOTHER_SHIP, MOVE,
		ENTER_MOTHER_SHIP, EXIT_MOTHER_SHIP,
		LAUNCH
	}

	Map<MESSAGES, String> map = Map.of(
			MESSAGES.SEND_GPS_MESSAGE, "send gps message : %d %d %s",
			MESSAGES.SEND_MESSAGE, "send message : %c %s",
			MESSAGES.DROP_BATTERY, "drop battery : %d",
			
			MESSAGES.MOVE, "move : %c");

	public RobotAI(ControlRobotConcurrentTurnGameShell gameShell) {
		super();
		this.gameShell = gameShell;
	}

	public Boolean call() {
		
		/*
		 * SINGLE TURN LOGIC FOR YOUR ROBOTS
		 */
		
		this.gameShell.runCommand(String.format(this.map.get(MESSAGES.MOVE), 'E'), this);
		
		return true;
		
	}

}
