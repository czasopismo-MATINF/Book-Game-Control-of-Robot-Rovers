package game.control.robot.rovers.actions;

import java.util.Map;

import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.command.PromptCommand;

public enum TURN_COMMIT_COMMAND {

	TURN_COMMIT("turnCommit", "turn commit", 0, (p, EOTPC, cmds) -> {
		
		// play END_OF_TURN_PHASE_CONFIG
		for (int i = 0; i < EOTPC.length; ++i) {
			for (int j = 0; j < EOTPC[i].length; ++j) {
				// play TURN_PHASE
				TURN_PHASE phase = EOTPC[i][j];
				phase.getTurnPhaseAction().apply(p, cmds);
			}
		}

		return null;

	});
	
	@FunctionalInterface
	public static interface F3<A, B, C, D> {
		public D apply(A a, B b, C c);
	}

	public static final String MESSAGE_SEPARATOR = ":";

	public final String camelCasedName;
	public final String messageFormat;
	public final int numberOfArguments;
	public final F3<Planet, TURN_PHASE[][], Map<Integer, PromptCommand[]>, String> action;

	private TURN_COMMIT_COMMAND(String camelCasedName, String messageFormat, int numberOfArguments,
			F3<Planet, TURN_PHASE[][], Map<Integer, PromptCommand[]>, String> action) {
		this.camelCasedName = camelCasedName;
		this.messageFormat = messageFormat.replaceFirst(":", TURN_COMMIT_COMMAND.MESSAGE_SEPARATOR);
		this.numberOfArguments = numberOfArguments;
		this.action = action;
	}

}
