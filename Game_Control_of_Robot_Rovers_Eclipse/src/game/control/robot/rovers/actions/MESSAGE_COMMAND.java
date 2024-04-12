package game.control.robot.rovers.actions;

import java.util.Map;
import java.util.function.BiFunction;

import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.command.PromptCommand;

public enum MESSAGE_COMMAND {

	SEND_GPS_MESSAGE("sendGpsMessage", "send gps message : %d %d %s", 3, (e1, e2) -> {

		return null;
	}), SEND_MESSAGE("sendMessage", "send message : %c %s", 2, (e1, e2) -> {

		return null;
	}), LOOK_AROUND("lookAround", "look around", 0, (e1, e2) -> {

		return null;
	}), CHECK_SELF("checkSelf", "check self", 0, (e1, e2) -> {

		return null;
	}), CHECK_GPS("checkGps", "check gps", 0, (e1, e2) -> {

		return null;
	});

	public enum MODE {
		PROMPT, CONCURRENT;
	};

	public static final String MESSAGE_SEPARATOR = ":";

	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;
	public final BiFunction<Map.Entry<Planet, Integer>, Map.Entry<PromptCommand, MODE>, String> action;

	private MESSAGE_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			BiFunction<Map.Entry<Planet, Integer>, Map.Entry<PromptCommand, MODE>, String> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", MESSAGE_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
