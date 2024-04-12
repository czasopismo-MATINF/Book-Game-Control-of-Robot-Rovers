package game.control.robot.rovers.actions;

import java.util.Map;
import java.util.function.BiFunction;

import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.command.PromptCommand;

public enum TURN_PHASE { // TURN PHASE

	DROP_CARGO((p, c) -> { // planet, commands

		return true;
	}), DROP_BATTERY((p, c) -> {

		return true;
	}), COLLECT_BATTERY((p, c) -> {

		return true;
	}), COLLECT_ROCKS((p, c) -> {

		return true;
	}), MARKER_NEW((p, c) -> {

		return true;
	}), MARKER_OVERWRITE((p, c) -> {

		return true;
	}), CHARGE_ROVER((p, c) -> {

		return true;
	}), CHARGING_STATION((p, c) -> {

		return true;
	}), DISTRIBUTE_ENERGY((p, c) -> {

		return true;
	}), LOAD_CARGO_TO_MOTHER_SHIP((p, c) -> {

		return true;
	}), MOVE((p, c) -> {

		c.entrySet().stream().forEach(e -> {
			// if there is a command and the command is MOVE
			if (e.getValue()[3] != null && END_OF_TURN_COMMAND
					.valueOf(e.getValue()[3].underscoreCasedKeyWords) == END_OF_TURN_COMMAND.MOVE) {
				p.moveRobot(e.getKey(), e.getValue()[3].argumentsArray[0]);
			}
		});

		return true;

	}), ENTER_MOTHER_SHIP((p, c) -> {

		return true;
	}), EXIT_MOTHER_SHIP((p, c) -> {

		return true;
	}), LAUNCH((p, c) -> {

		return true;
	});

	BiFunction<Planet, Map<Integer, PromptCommand[]>, Boolean> phaseAction;

	public BiFunction<Planet, Map<Integer, PromptCommand[]>, Boolean> getTurnPhaseAction() {
		return this.phaseAction;
	}

	private TURN_PHASE(BiFunction<Planet, Map<Integer, PromptCommand[]>, Boolean> phaseAction) {
		this.phaseAction = phaseAction;
	}

}