package game.control.robot.rovers;

import java.util.Map;

import game.control.robot.rovers.actions.CREATE_COMMAND;
import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;
import game.control.robot.rovers.actions.MESSAGE_COMMAND;
import game.control.robot.rovers.actions.STATUS_COMMAND;
import game.control.robot.rovers.actions.TURN_COMMIT_COMMAND;
import game.control.robot.rovers.actions.TURN_PHASE;
import game.control.robot.rovers.board.*;
import java.util.stream.Collectors;
import game.control.robot.rovers.command.PromptCommand;
import game.control.robot.rovers.config.BoardConfig;
import game.control.robot.rovers.config.GameConfig;

import java.util.concurrent.ConcurrentHashMap;

public class ControlRobotTurnGameBoardAndCommands {

	protected BoardConfig config = new GameConfig();

	/********************/

	protected Planet planet = new Planet(config.getIntValue(BoardConfig.INT_CONFIG_ENTRY.DEFAULT_PLANET_WIDTH),
			config.getIntValue(BoardConfig.INT_CONFIG_ENTRY.DEFAULT_PLANET_HEIGHT));

	public Planet getPlanet() {
		return this.planet;
	}

	public void setPlanet(Planet planet) {
		this.planet = planet;
	}

	/********************/

	protected Map<Integer, PromptCommand[]> endOfTurnRobotCommands = new ConcurrentHashMap<>();

	protected END_OF_TURN_COMMAND[][] END_OF_TURN_ROBOT_COMMANDS_CONFIG = { { END_OF_TURN_COMMAND.DROP_CARGO },
			{ END_OF_TURN_COMMAND.DROP_BATTERY, END_OF_TURN_COMMAND.COLLECT_BATTERY,
					END_OF_TURN_COMMAND.COLLECT_ROCKS },
			{ END_OF_TURN_COMMAND.MARKER_NEW, END_OF_TURN_COMMAND.MARKER_OVERWRITE },
			{ END_OF_TURN_COMMAND.CHARGE_ROVER, END_OF_TURN_COMMAND.CHARGING_STATION,
					END_OF_TURN_COMMAND.DISTRIBUTE_ENERGY, END_OF_TURN_COMMAND.MOVE,
					END_OF_TURN_COMMAND.LOAD_CARGO_TO_MOTHER_SHIP },
			{ END_OF_TURN_COMMAND.ENTER_MOTHER_SHIP, END_OF_TURN_COMMAND.EXIT_MOTHER_SHIP },
			{ END_OF_TURN_COMMAND.LAUNCH } };

	/********************/

	protected TURN_PHASE[][] END_OF_TURN_PHASE_CONFIG = { { TURN_PHASE.DROP_CARGO },
			{ TURN_PHASE.DROP_BATTERY, TURN_PHASE.COLLECT_BATTERY, TURN_PHASE.COLLECT_ROCKS },
			{ TURN_PHASE.MARKER_NEW, TURN_PHASE.MARKER_OVERWRITE },
			{ TURN_PHASE.CHARGE_ROVER, TURN_PHASE.CHARGING_STATION, TURN_PHASE.DISTRIBUTE_ENERGY,
					TURN_PHASE.LOAD_CARGO_TO_MOTHER_SHIP, TURN_PHASE.MOVE },
			{ TURN_PHASE.ENTER_MOTHER_SHIP }, { TURN_PHASE.LAUNCH } };

	/********************/

	protected void validateNumberOfArguments(PromptCommand command, Integer numberOfArguments)
			throws IllegalArgumentException {
		if (command.argumentsArray.length < numberOfArguments) {
			throw new IllegalArgumentException();
		}
	}

	/********************/

	protected void addTurnCommand(Integer robotId, PromptCommand command) throws IllegalArgumentException {

		this.validateNumberOfArguments(command,
				END_OF_TURN_COMMAND.valueOf(command.underscoreCasedKeyWords).numberOfArguments);

		for (int i = 0; i < this.END_OF_TURN_ROBOT_COMMANDS_CONFIG.length; ++i) {
			for (int j = 0; j < this.END_OF_TURN_ROBOT_COMMANDS_CONFIG[i].length; ++j) {
				if (this.END_OF_TURN_ROBOT_COMMANDS_CONFIG[i][j].camelCasedName.equals(command.camelCasedKeyWords)) {
					this.endOfTurnRobotCommands.putIfAbsent(robotId,
							new PromptCommand[this.END_OF_TURN_ROBOT_COMMANDS_CONFIG.length]);
					this.endOfTurnRobotCommands.getOrDefault(robotId,
							new PromptCommand[this.END_OF_TURN_ROBOT_COMMANDS_CONFIG.length])[i] = command;
				}
			}
		}

	}

	/********************/

	public String runMessageCommand(MESSAGE_COMMAND command, MESSAGE_COMMAND.MODE mode, PromptCommand promptCommand,
			int currentRobot) {

		Map.Entry<Planet, Integer> e1 = Map.entry(this.getPlanet(), currentRobot);
		Map.Entry<PromptCommand, MESSAGE_COMMAND.MODE> e2 = Map.entry(promptCommand, mode);
		return command.action.apply(e1, e2);

	}

	public void runEndOfTurnCommand(END_OF_TURN_COMMAND command, PromptCommand promptCommand, int currentRobot) {

		try {
			this.addTurnCommand(currentRobot, promptCommand);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

	}

	public String runStatusCommand(STATUS_COMMAND command, PromptCommand promptCommand) {

		Map.Entry<Planet, PromptCommand> e1 = Map.entry(this.getPlanet(), promptCommand);
		Map.Entry<END_OF_TURN_COMMAND[][], Map<Integer, PromptCommand[]>> e2 = Map
				.entry(this.END_OF_TURN_ROBOT_COMMANDS_CONFIG, this.endOfTurnRobotCommands);
		return command.action.apply(e1, e2);

	}

	public String runCreateCommand(CREATE_COMMAND command, PromptCommand promptCommand) {

		command.action.apply(this.planet, promptCommand);
		return null;

	}

	public String runTurnCommitCommand(TURN_COMMIT_COMMAND command, PromptCommand promptCommand) {

		Map.Entry<TURN_PHASE[][], Map<Integer, PromptCommand[]>> e2 = Map.entry(this.END_OF_TURN_PHASE_CONFIG,
				this.endOfTurnRobotCommands);
		var ret = command.action.apply(this.getPlanet(), e2);
		this.resetTurnCommitCommands();
		return this.printEndScreen();

	}

	protected void resetTurnCommitCommands() {
		this.endOfTurnRobotCommands = new ConcurrentHashMap<>();
	}

	protected String printEndScreen() {

		MotherShip motherShip = this.planet.getMotherShip();

		if (motherShip != null && motherShip.isLaunched()) {

			StringBuffer buffer = new StringBuffer("THE END:\n");

			buffer.append(
					String.format("rocks in the cargo of the Mother Ship: %d\n", motherShip.getCargo().getRocks()));
			buffer.append(String.format("batteries in the cargo of the Mother Ship: %d\n",
					motherShip.getCargo().getBatteriesInCargo().size()));
			buffer.append(String.format("rovers in the Mother Ship: %d\n", motherShip.getRobots().size()));
			buffer.append(String.format("rocks in the cargos of rovers: %d\n",
					motherShip.getRobots().stream().collect(Collectors.summingInt(r -> r.getCargo().getRocks()))));
			buffer.append(String.format("batteries in the cargos of rovers: %d\n", motherShip.getRobots().stream()
					.collect(Collectors.summingInt(r -> r.getCargo().getBatteriesInCargo().size()))));

			return buffer.toString();

		}

		return null;

	}

	/********************/

}
