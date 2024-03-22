package game.control.robotic.rovers;

enum COMMAND {
	
	SEND_GPS_MESSAGE("sendGpsMessage", "send gps message : %d %d %s"),
	SEND_MESSAGE("sendMessage", "send message : %c %s"),
	LOOK_AROUND("lookAround", "look around"),
	CHECK_SELF("checkSelf", "check self"),
	CHECK_GPS("checkGps", "check gps"),
	
	DROP_CARGO("dropCargo", "drop cargo"),
	
	DROP_BATTERY("dropBattery", "drop battery : %d"),
	COLLECT_BATTERY("collectBattery", "collect battery : %d %s %s"),
	COLLECT_ROCKS("collectRocks", "collect rocks : %d"),
	
	MARKER_NEW("markerNew", "marker new : %s"),
	MARKER_OVERWRITE("markerOverwrite", "marker overwrite : %d %s"),
	
	CHARGE_ROVER("chargeRover", "charge rover : %d"),
	CHARGING_STATION("chargingStation", "charging station"),
	DISTRIBUTE_ENERGY("distributeEnergy", "distribute energy : %s"),
	LOAD_CARGO_TO_MOTHER_SHIP("loadCargoToMotherShip", "load cargo to mother ship"),
	MOVE("move", "move : %c"),
	
	ENTER_MOTHER_SHIP("enterMotherShip", "enter mother ship"),
	EXIT_MOTHER_SHIP("exitMotherShip", "exit mother ship"),
	
	LAUNCH("launch", "launch");
	
	private static final String MESSAGE_SEPARATOR = ":";
	
	public final String camelCasedName;
	public final String messageFormat;

	private COMMAND(String camelCasedName, String messageFormat) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", COMMAND.MESSAGE_SEPARATOR);
	}
}