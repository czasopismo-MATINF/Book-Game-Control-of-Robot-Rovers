package game.control.robot.rovers.actions;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.command.PromptCommand;

public enum STATUS_COMMAND {

	TURN_STATUS("turnStatus", "turn status", 0, (e1, e2) -> {

		return e2.getValue().entrySet().stream()
				.map(e -> STATUS_COMMAND.buildRobotCommandStatus(e.getKey(), e2.getKey(), e2.getValue()))
				.collect(Collectors.joining("\n"));

	});

	public static final String MESSAGE_SEPARATOR = ":";

	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;
	public final BiFunction<Map.Entry<Planet, PromptCommand>, Map.Entry<END_OF_TURN_COMMAND[][], Map<Integer, PromptCommand[]>>, String> action;

	protected static String buildRobotCommandStatus(int robotId,
			END_OF_TURN_COMMAND[][] END_OF_TURN_ROBOT_COMMANDS_CONFIG,
			Map<Integer, PromptCommand[]> endOfTurnRobotCommands) {

		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(String.valueOf(robotId)).append(":");
		for (int i = 0; i < END_OF_TURN_ROBOT_COMMANDS_CONFIG.length; ++i) {
			PromptCommand c = endOfTurnRobotCommands.get(robotId)[i];
			if (c != null) {
				sBuilder.append(c.command);
			} else {
				sBuilder.append("...");
			}
			if (i + 1 < END_OF_TURN_ROBOT_COMMANDS_CONFIG.length) {
				sBuilder.append(" > ");
			}
		}
		return sBuilder.toString();

	}

	private STATUS_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			BiFunction<Map.Entry<Planet, PromptCommand>, Map.Entry<END_OF_TURN_COMMAND[][], Map<Integer, PromptCommand[]>>, String> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", STATUS_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
