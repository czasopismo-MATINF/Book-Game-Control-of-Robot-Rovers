package game.control.robot.rovers.actions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.Random;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import game.control.robot.rovers.board.*;
import game.control.robot.rovers.command.PromptCommand;
import game.control.robot.rovers.config.BoardConfig;
import game.control.robot.rovers.config.GameConfig;

import java.lang.FunctionalInterface;

@FunctionalInterface
interface P4<A, B, C, D, E> {
	public void apply(A a, B b, C c, D d, E e);
}

@FunctionalInterface
interface F5<A, B, C, D, E, F> {
	public F apply(A a, B b, C c, D d, E e);
}

class WeatherEffects {

	public static boolean preventedBy(List<Blizzard> blizzards) {

		Random random = new Random(System.currentTimeMillis());

		for (Blizzard blizzard : blizzards) {
			int rnd = random.nextInt(0, 101);
			if (rnd < blizzard.getVolume()) {
				return true;
			}
		}

		return false;

	}

}

class RobotPromptCommandMap extends HashMap<Robot, PromptCommand> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int phase;
	protected Map<Integer, PromptCommand[]> commands;
	protected END_OF_TURN_COMMAND command;

	public RobotPromptCommandMap(Collection<Robot> robots, Map<Integer, PromptCommand[]> commands, int phase,
			END_OF_TURN_COMMAND command) {

		robots.forEach(robot -> {

			if (commands.getOrDefault(robot.getId(), null) != null
					&& commands.getOrDefault(robot.getId(), null)[phase] != null && END_OF_TURN_COMMAND.valueOf(
							commands.getOrDefault(robot.getId(), null)[phase].underscoreCasedKeyWords) == command) {

				this.put(robot, commands.getOrDefault(robot.getId(), null)[phase]);
			}

		});

		this.phase = phase;
		this.commands = commands;
		this.command = command;

	}

	public RobotPromptCommandMap filter(Predicate<PromptCommand> condition) {

		return new RobotPromptCommandMap(this.entrySet().stream().filter(e -> {
			return condition.test(e.getValue());
		}).map(e -> {
			return e.getKey();
		}).collect(Collectors.toList()), this.commands, this.phase, this.command);

	}

	public void forEach(Planet planet, P4<Robot, PromptCommand, Planet, Area, GPSCoordinates> procedure) {

		this.entrySet().forEach(entry -> {

			GPSCoordinates coords = planet.robotGPSCoordinates(entry.getKey().getId());
			Area area = planet.getSurface()[coords.getX()][coords.getY()];

			procedure.apply(entry.getKey(), entry.getValue(), planet, area, coords);

		});

	}

}

enum ENERGY_COST_CALCULATOR {

	RANDOM((min, max, blizzards, weight, multiplier) -> {
		Random random = new Random(System.currentTimeMillis());
		return random.nextInt(min, max + 1);
	}), CONST((min, max, blizzards, weight, multiplier) -> {
		return min;
	}), MIN((min, max, blizzards, weight, multiplier) -> {
		return min;
	}), MAX((min, max, blizzards, weight, multiplier) -> {
		return max;
	}), AVG((min, max, blizzards, weight, multiplier) -> {
		return (min + max) / 2;
	}), REACH_DESTINATION_ON_AREA((min, max, blizzards, weight, multiplier) -> {
		Random random = new Random(System.currentTimeMillis());
		int totalVolume = blizzards.stream().collect(Collectors.summingInt(b -> b.getVolume()));
		return random.nextInt(min, max + (int) (((double) max) * ((double) totalVolume) / 100)) + weight * multiplier;
	}), CONST_WEIGHT((min, max, blizzards, weight, multiplier) -> {
		return min + weight * multiplier;
	});

	protected BoardConfig config = new GameConfig();

	private final F5<Integer, Integer, List<Blizzard>, Integer, Integer, Integer> calculate;

	private ENERGY_COST_CALCULATOR(F5<Integer, Integer, List<Blizzard>, Integer, Integer, Integer> calculate) {
		this.calculate = calculate;
	}

	public int calculate(BoardConfig.INT_CONFIG_ENTRY entry1, BoardConfig.INT_CONFIG_ENTRY entry2,
			List<Blizzard> blizzards, Integer weight, Integer multiplier) {
		return this.calculate.apply(this.config.getIntValue(entry1), this.config.getIntValue(entry2), blizzards, weight,
				multiplier);
	}

	public int calculate(BoardConfig.INT_CONFIG_ENTRY entry1, List<Blizzard> blizzards, Integer weight,
			Integer multiplier) {
		return this.calculate.apply(this.config.getIntValue(entry1), this.config.getIntValue(entry1), blizzards, weight,
				multiplier);
	}

	public int calculate(BoardConfig.INT_CONFIG_ENTRY entry1, BoardConfig.INT_CONFIG_ENTRY entry2,
			List<Blizzard> blizzards, Integer weight, BoardConfig.INT_CONFIG_ENTRY entry3) {
		return this.calculate.apply(this.config.getIntValue(entry1), this.config.getIntValue(entry2), blizzards, weight,
				this.config.getIntValue(entry3));
	}

	public int calculate(BoardConfig.INT_CONFIG_ENTRY entry1, List<Blizzard> blizzards, Integer weight,
			BoardConfig.INT_CONFIG_ENTRY entry2) {
		return this.calculate.apply(this.config.getIntValue(entry1), this.config.getIntValue(entry1), blizzards, weight,
				this.config.getIntValue(entry2));
	}

}

public enum TURN_PHASE {

	DROP_CARGO((planet, commands) -> {

		new RobotPromptCommandMap(planet.getAllRobots(), commands, 0, END_OF_TURN_COMMAND.DROP_CARGO).forEach(planet,
				(robot, promptCommand, p, area, coords) -> {

					int energyCost = ENERGY_COST_CALCULATOR.CONST
							.calculate(BoardConfig.INT_CONFIG_ENTRY.DROP_CARGO_ENERGY, area.getBlizzards(), 0, 0);

					if (energyCost <= robot.getTotalEnergy()) {

						robot.drainEnergy(energyCost);

						area.addBatteriesAndRocks(robot.getCargo().getBatteriesInCargo(), robot.getCargo().getRocks());
						robot.getCargo().releaseCargo();

					}

				});

		return true;
	}), DROP_BATTERY((planet, commands) -> {

		new RobotPromptCommandMap(planet.getAllRobots(), commands, 1, END_OF_TURN_COMMAND.DROP_BATTERY).forEach(planet,
				(robot, promptCommand, p, area, coords) -> {

					int energyCost = ENERGY_COST_CALCULATOR.CONST
							.calculate(BoardConfig.INT_CONFIG_ENTRY.DROP_BATTERY_ENERGY, area.getBlizzards(), 0, 0);

					if (energyCost <= robot.getTotalEnergy()) {

						int slot = Integer.valueOf(promptCommand.argumentsArray[0]);

						if (slot >= 0 && slot < robot.getBatteries().length && robot.getBatteries()[slot] != null) {

							robot.drainEnergy(energyCost);

							Battery battery = robot.removeBattery(slot);
							area.getBatteries().add(battery);

						}

					}

				});

		return true;
	}), COLLECT_BATTERY((planet, commands) -> {

		new RobotPromptCommandMap(planet.getAllRobots(), commands, 1, END_OF_TURN_COMMAND.COLLECT_BATTERY)
				.forEach(planet, (robot, promptCommand, p, area, coords) -> {

					if ("SLOT".equals(promptCommand.argumentsArray[2].toUpperCase())) {

						int freeSlot = robot.getFreeSlot();
						if (freeSlot < 0) {
							return;
						}

						int batteryNumber = Integer.valueOf(promptCommand.argumentsArray[0]);
						if (batteryNumber < 0) {
							return;
						}

						int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
								BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MIN,
								BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MAX, area.getBlizzards(),
								robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_WEIGHT_MULTIPLIER);
						int drained = robot.drainEnergy(energy);
						if (drained < energy) {
							return;
						}
						
						if(batteryNumber >= area.getBatteries().size()) {
							return;
						}

						Battery battery = area.getBatteries().get(batteryNumber);

						energy = ENERGY_COST_CALCULATOR.CONST_WEIGHT.calculate(
								BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_CONST_ENERGY, area.getBlizzards(),
								battery.getWeight(), BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_WEIGHT_MULTIPLIER);
						if (energy > robot.getTotalEnergy()) {
							return;
						}
						robot.drainEnergy(energy);

						if (WeatherEffects.preventedBy(area.getBlizzards())) {
							return;
						}

						area.getBatteries().remove(battery);
						robot.insertBattery(battery, freeSlot);

					} else if ("CARGO".equals(promptCommand.argumentsArray[2].toUpperCase())) {

						int batteryNumber = Integer.valueOf(promptCommand.argumentsArray[0]);
						if (batteryNumber < 0) {
							return;
						}

						int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
								BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MIN,
								BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MAX, area.getBlizzards(),
								robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_WEIGHT_MULTIPLIER);
						int drained = robot.drainEnergy(energy);
						if (drained < energy) {
							return;
						}

						if(batteryNumber >= area.getBatteries().size()) {
							return;
						}
						
						Battery battery = area.getBatteries().get(batteryNumber);

						if (battery.getWeight() + robot.getCargo().load() > robot.getCargo().getMaxLoad()) {
							return;
						}

						energy = ENERGY_COST_CALCULATOR.CONST_WEIGHT.calculate(
								BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_CONST_ENERGY, area.getBlizzards(),
								battery.getWeight(), BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_WEIGHT_MULTIPLIER);
						if (energy > robot.getTotalEnergy()) {
							return;
						}
						robot.drainEnergy(energy);

						if (WeatherEffects.preventedBy(area.getBlizzards())) {
							return;
						}

						area.getBatteries().remove(battery);
						robot.getCargo().addBattery(battery);

					}
				});

		return true;
	}), COLLECT_ROCKS((planet, commands) -> {

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

	protected static BoardConfig config = new GameConfig();

	protected static BoardConfig getConfig() {
		return TURN_PHASE.config;
	}

	public final BiFunction<Planet, Map<Integer, PromptCommand[]>, Boolean> phaseAction;

	public BiFunction<Planet, Map<Integer, PromptCommand[]>, Boolean> getTurnPhaseAction() {
		return this.phaseAction;
	}

	private TURN_PHASE(BiFunction<Planet, Map<Integer, PromptCommand[]>, Boolean> phaseAction) {
		this.phaseAction = phaseAction;
	}
	/*
	 * protected static void forEach(Collection<Robot> robots, Planet planet,
	 * Map<Integer, PromptCommand[]> commands, F<PromptCommand[], Robot,
	 * GPSCoordinates, Area> function) {
	 * 
	 * robots.stream().forEach(r -> {
	 * 
	 * GPSCoordinates coords = planet.robotGPSCoordinates(r.getId()); Area area =
	 * planet.getSurface()[coords.getX()][coords.getY()]; PromptCommand[] command =
	 * commands.getOrDefault(r.getId(), null); function.apply(command, r, coords,
	 * area);
	 * 
	 * });
	 * 
	 * }
	 * 
	 * protected static Map<Robot, PromptCommand> filter(List<Robot> robots,
	 * Map<Integer, PromptCommand[]> commands, int phase, END_OF_TURN_COMMAND
	 * command) {
	 * 
	 * Map<Robot, PromptCommand> filtered = new HashMap<>();
	 * 
	 * robots.stream().filter(r -> {
	 * 
	 * PromptCommand[] cmds = commands.getOrDefault(r.getId(), null); if (cmds ==
	 * null) return false; PromptCommand cmd = cmds[phase]; if (cmd == null) return
	 * false; if (END_OF_TURN_COMMAND.valueOf(cmd.underscoreCasedKeyWords) ==
	 * command) { if (cmd.argumentsArray.length < command.numberOfArguments) return
	 * false; }
	 * 
	 * filtered.put(r, cmd);
	 * 
	 * return true;
	 * 
	 * }).collect(Collectors.toList());
	 * 
	 * return filtered;
	 * 
	 * }
	 */
}