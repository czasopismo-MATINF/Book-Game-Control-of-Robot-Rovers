package game.control.robot.rovers.actions;

public enum END_OF_TURN_COMMAND {
	
	DROP_CARGO("dropCargo", "drop cargo", 0),
	
	DROP_BATTERY("dropBattery", "drop battery : %d", 1),
	COLLECT_BATTERY("collectBattery", "collect battery : %d %s %s", 3),
	COLLECT_ROCKS("collectRocks", "collect rocks : %d", 1),
	
	MARKER_NEW("markerNew", "marker new : %s", 1),
	MARKER_OVERWRITE("markerOverwrite", "marker overwrite : %d %s", 2),
	
	CHARGE_ROVER("chargeRover", "charge rover : %d energy %d", 3),
	CHARGING_STATION("chargingStation", "charging station", 0),
	DISTRIBUTE_ENERGY("distributeEnergy", "distribute energy : %s", 1),
	LOAD_CARGO_TO_MOTHER_SHIP("loadCargoToMotherShip", "load cargo to mother ship", 0),
	MOVE("move", "move : %c", 1),
	
	ENTER_MOTHER_SHIP("enterMotherShip", "enter mother ship", 0),
	EXIT_MOTHER_SHIP("exitMotherShip", "exit mother ship", 0),
	
	LAUNCH("launch", "launch", 0);
	
	public static final String MESSAGE_SEPARATOR = ":";
	
	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;

	private END_OF_TURN_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", END_OF_TURN_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
	}
	
}
