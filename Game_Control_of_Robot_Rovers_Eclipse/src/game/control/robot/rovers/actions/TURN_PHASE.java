package game.control.robot.rovers.actions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import game.control.robot.rovers.board.*;
import game.control.robot.rovers.command.PromptCommand;
import game.control.robot.rovers.config.BoardConfig;
import game.control.robot.rovers.config.GameConfig;

import java.lang.FunctionalInterface;

//@FunctionalInterface
//interface F4<A, B, C, D> {
//	public void apply(A a, B b, C c, D d);
//}

@FunctionalInterface
interface F5<A, B, C, D, E> {
	public void apply(A a, B b, C c, D d, E e);
}

class WeatherEffects {

	public boolean operationSuccess(List<Blizzard> blizzards, int min, int max) {
		return true;
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
				&& commands.getOrDefault(robot.getId(), null)[phase] != null
				&& END_OF_TURN_COMMAND.valueOf(commands.getOrDefault(robot.getId(), null)[phase].underscoreCasedKeyWords) == command) {

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

	public void forEach(Planet planet, F5<Robot, PromptCommand, Planet, Area, GPSCoordinates> procedure) {

		this.entrySet().forEach(entry -> {
			
			GPSCoordinates coords = planet.robotGPSCoordinates(entry.getKey().getId());
			Area area = planet.getSurface()[coords.getX()][coords.getY()];
			
			procedure.apply(entry.getKey(), entry.getValue(), planet, area, coords);
			
		});

	}

}

enum ENERGY_COST_CALCULATOR {

	RANDOM((min, max) -> {
		return min;
	}), CONST((min, max) -> {
		return min;
	}), MIN((min, max) -> {
		return min;
	}), MAX((min, max) -> {
		return max;
	}), AVG((min, max) -> {
		return (min + max) / 2;
	});

	protected BoardConfig config = new GameConfig();

	private final BiFunction<Integer, Integer, Integer> calculate;

	private ENERGY_COST_CALCULATOR(BiFunction<Integer, Integer, Integer> calculate) {
		this.calculate = calculate;
	}

	public int calculate(BoardConfig.INT_CONFIG_ENTRY entry1, BoardConfig.INT_CONFIG_ENTRY entry2) {
		return this.calculate.apply(this.config.getIntValue(entry1), this.config.getIntValue(entry2));
	}

	public int calculate(BoardConfig.INT_CONFIG_ENTRY entry) {
		return this.calculate.apply(this.config.getIntValue(entry), this.config.getIntValue(entry));
	}

}

public enum TURN_PHASE {

	DROP_CARGO((planet, commands) -> {

		new RobotPromptCommandMap(planet.getAllRobots(), commands, 0, END_OF_TURN_COMMAND.DROP_CARGO).forEach(planet,
				(robot, promptCommand, p, area, coords) -> {

					int energyCost = ENERGY_COST_CALCULATOR.CONST
							.calculate(BoardConfig.INT_CONFIG_ENTRY.DROP_CARGO_ENERGY);

					if (energyCost <= robot.getTotalEnergy()) {

						robot.drainEnergy(energyCost);

						area.addBatteriesAndRocks(robot.getCargo().getBatteriesInCargo(), robot.getCargo().getRocks());
						robot.getCargo().releaseCargo();

					}

				});
		/*
		 * Map<Robot, PromptCommand> droppers = TURN_PHASE.filter(planet.getRobots(),
		 * commands, 0, END_OF_TURN_COMMAND.DROP_CARGO);
		 * 
		 * TURN_PHASE.forEach(droppers.keySet(), planet, commands, (cmd, robot, coords,
		 * area) -> {
		 * 
		 * int rocks = robot.getCargo().getRocks(); List<Battery> batteries =
		 * robot.getCargo().getBatteriesInCargo(); robot.getCargo().releaseCargo();
		 * area.addBatteriesAndRocks(batteries, rocks);
		 * 
		 * });
		 */
		return true;
	}), DROP_BATTERY((planet, commands) -> {

		new RobotPromptCommandMap(planet.getAllRobots(), commands, 1, END_OF_TURN_COMMAND.DROP_BATTERY).forEach(planet,
				(robot, promptCommand, p, area, coords) -> {

					int energyCost = ENERGY_COST_CALCULATOR.CONST
							.calculate(BoardConfig.INT_CONFIG_ENTRY.DROP_BATTERY_ENERGY);

					if (energyCost <= robot.getTotalEnergy()) {

						int slot = Integer.valueOf(promptCommand.argumentsArray[0]);

						if (slot >= 0 && slot < robot.getBatteries().length && robot.getBatteries()[slot] != null) {

							robot.drainEnergy(energyCost);

							Battery battery = robot.removeBattery(slot);
							area.getBatteries().add(battery);

						}

					}

				});
		/*
		 * planet.getAllAreas().stream().forEach(a -> {
		 * 
		 * Map<Robot, PromptCommand> droppers = TURN_PHASE.filter(a.getRobots(),
		 * commands, 1, END_OF_TURN_COMMAND.DROP_BATTERY);
		 * 
		 * TURN_PHASE.forEach(droppers.keySet(), planet, commands, (cmd, robot, coords,
		 * area) -> {
		 * 
		 * // wez baterie jesli jest i poloz na grunice zuzywajac energie
		 * 
		 * });
		 * 
		 * });
		 */
		return true;
	}), COLLECT_BATTERY((planet, commands) -> {

//		List<Robot> robots = new ArrayList(planet.getAllRobots());
//		Collections.shuffle(robots);
//		new RobotPromptCommandMap(robots, commands, 1, END_OF_TURN_COMMAND.COLLECT_BATTERY)
//		.forEach(planet, (robot, command, p, area, coords) -> {
//			
//		});
		/*
		 * planet.getAllAreas().stream().forEach(a -> {
		 * 
		 * Map<Robot, PromptCommand> collectors = TURN_PHASE.filter(a.getRobots(),
		 * commands, 1, END_OF_TURN_COMMAND.COLLECT_BATTERY);
		 * 
		 * TURN_PHASE.forEach(collectors.keySet(), planet, commands, (cmd, robot,
		 * coords, area) -> {
		 * 
		 * // wez id baterii sprawdz zamiec podnies zuzywajac energie });
		 * 
		 * });
		 */
		return true;
	}), COLLECT_ROCKS((planet, commands) -> {

//		planet.getAllAreas().forEach(area -> {
//			
//			RobotPromptCommandMap miners = new RobotPromptCommandMap(area.getRobots(),commands,1, END_OF_TURN_COMMAND.COLLECT_ROCKS);
//			
//			Map<Robot, Integer> collectedRocks = new HashMap<>();
//			
//			miners.keySet().forEach(robot -> {
//				collectedRocks.put(robot, 0);
//			});
//			
//			while(true) {
//				
//				if(area.getRocks() == 0) break;
//				
//				
//				
//			}
//			
//		});
		/*
		 * planet.getAllAreas().stream().forEach(a -> {
		 * 
		 * Map<Robot, PromptCommand> rockMiners = TURN_PHASE.filter(a.getRobots(),
		 * commands, 1, END_OF_TURN_COMMAND.COLLECT_ROCKS);
		 * 
		 * Map<Robot, Integer> collectedRocks = new HashMap<>();
		 * 
		 * rockMiners.keySet().forEach(r -> { collectedRocks.put(r, 0); });
		 * 
		 * // dodac zuzywanie energii // dodac obsluge zamieci w zaleznosci od jej sily
		 * 
		 * while (true) {
		 * 
		 * if (a.getRocks() == 0) { break; }
		 * 
		 * List<Robot> notYet = rockMiners.entrySet().stream().filter(e -> { if
		 * (Integer.valueOf(e.getValue().argumentsArray[0]) <=
		 * collectedRocks.getOrDefault(e.getKey(), 0)) return false; if
		 * (e.getKey().getCargo().isFull()) return false; return true; }).map(e ->
		 * e.getKey()).collect(Collectors.toList());
		 * 
		 * if (notYet.size() == 0) { break; }
		 * 
		 * Collections.shuffle(notYet); a.mineRocks(1);
		 * notYet.get(0).getCargo().addRocks(1); }
		 * 
		 * });
		 */
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