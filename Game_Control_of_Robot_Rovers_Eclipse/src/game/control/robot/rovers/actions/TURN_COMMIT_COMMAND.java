package game.control.robot.rovers.actions;

import java.util.Map;
import java.util.function.BiFunction;

import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.command.PromptCommand;

public enum TURN_COMMIT_COMMAND {

	TURN_COMMIT("turnCommit", "turn commit", 0, (p, e2) -> {

		var END_OF_TURN_PHASE_CONFIG = e2.getKey();
		var endOfTurnRobotCommands = e2.getValue();
		
		// play END_OF_TURN_PHASE_CONFIG
		for (int i = 0; i < END_OF_TURN_PHASE_CONFIG.length; ++i) {
			for (int j = 0; j < END_OF_TURN_PHASE_CONFIG[i].length; ++j) {
				// play TURN_PHASE
				TURN_PHASE phase = END_OF_TURN_PHASE_CONFIG[i][j];
				phase.getTurnPhaseAction().apply(p, endOfTurnRobotCommands);
			}
		}

		return null;

	});

	public static final String MESSAGE_SEPARATOR = ":";

	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;
	public final BiFunction<Planet, Map.Entry<TURN_PHASE[][], Map<Integer, PromptCommand[]>>, String> action;

	private TURN_COMMIT_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			BiFunction<Planet, Map.Entry<TURN_PHASE[][], Map<Integer, PromptCommand[]>>, String> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", TURN_COMMIT_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
