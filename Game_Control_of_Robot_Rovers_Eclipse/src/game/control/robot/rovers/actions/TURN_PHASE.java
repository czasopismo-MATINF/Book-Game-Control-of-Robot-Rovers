package game.control.robot.rovers.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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

	public static boolean success(List<Blizzard> blizzards) {

		Random random = new Random(System.currentTimeMillis());

		for (Blizzard blizzard : blizzards) {
			int rnd = random.nextInt(0, 101);
			if (rnd < blizzard.getVolume()) {
				return false;
			}
		}

		return true;

	}

}

class RandomEffects {

	public static int getRandom(int min, int max) {

		Random random = new Random(System.currentTimeMillis());

		return random.nextInt(min, max + 1);

	}

	public static String mergeMarkers(String oldMarker, String newMarker, String groundMarker, int chars,
			List<Blizzard> blizzards) {
		
		StringBuffer sBuffer = new StringBuffer();
		
		int i = 0;
		for(; i < chars; ++i) {
			if(i < newMarker.length()) {
				sBuffer.append(newMarker.charAt(i));
			} else {
				if(i < oldMarker.length()) {
					sBuffer.append('.');
				} else {
					if(i < groundMarker.length()) {
						sBuffer.append(groundMarker.charAt(i));
					}
				}
			}
		}
		
		for(; i < groundMarker.length(); ++i) {
			sBuffer.append(groundMarker.charAt(i));
		}
		
		return sBuffer.toString();
		
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
	}),

	CONST((min, max, blizzards, weight, multiplier) -> {
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
	}),

	DROP_BATTERY((planet, commands) -> {

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

		planet.getAllAreas().forEach(area -> {

			Map<Robot, Battery> robotCollectsBattery = new HashMap<>();

			new RobotPromptCommandMap(area.getRobots(), commands, 1, END_OF_TURN_COMMAND.COLLECT_BATTERY)
					.filter(cmd -> {
						return Integer.valueOf(cmd.argumentsArray[0]) >= 0
								&& Integer.valueOf(cmd.argumentsArray[0]) < area.getBatteries().size();
					}).forEach(planet, (robot, promptCommand, p, a, coord) -> {
						Integer batteryNumber = Integer.valueOf(promptCommand.argumentsArray[0]);
						robotCollectsBattery.put(robot, area.getBatteries().get(batteryNumber));
					});

			List<Robot> areaRobots = area.getRobots();
			Collections.shuffle(areaRobots);

			new RobotPromptCommandMap(areaRobots, commands, 1, END_OF_TURN_COMMAND.COLLECT_BATTERY).forEach(planet,
					(robot, promptCommand, p, a, coords) -> {

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
									BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MAX, a.getBlizzards(),
									robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_WEIGHT_MULTIPLIER);
							int drained = robot.drainEnergy(energy);
							if (drained < energy) {
								return;
							}

							if (batteryNumber >= a.getBatteries().size()) {
								return;
							}

							// Battery battery = area.getBatteries().get(batteryNumber);
							Battery battery = robotCollectsBattery.get(robot);
							if (battery == null) {
								return;
							}

							energy = ENERGY_COST_CALCULATOR.CONST_WEIGHT.calculate(
									BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_CONST_ENERGY, a.getBlizzards(),
									battery.getWeight(),
									BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_WEIGHT_MULTIPLIER);
							if (energy > robot.getTotalEnergy()) {
								return;
							}
							robot.drainEnergy(energy);

							if (WeatherEffects.preventedBy(a.getBlizzards())) {
								return;
							}

							a.getBatteries().remove(battery);
							robot.insertBattery(battery, freeSlot);

						} else if ("CARGO".equals(promptCommand.argumentsArray[2].toUpperCase())) {

							int batteryNumber = Integer.valueOf(promptCommand.argumentsArray[0]);
							if (batteryNumber < 0) {
								return;
							}

							int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
									BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MIN,
									BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MAX, a.getBlizzards(),
									robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_WEIGHT_MULTIPLIER);
							int drained = robot.drainEnergy(energy);
							if (drained < energy) {
								return;
							}

							if (batteryNumber >= a.getBatteries().size()) {
								return;
							}

							// Battery battery = area.getBatteries().get(batteryNumber);
							Battery battery = robotCollectsBattery.get(robot);
							if (battery == null) {
								return;
							}

							if (battery.getWeight() + robot.getCargo().load() > robot.getCargo().getMaxLoad()) {
								return;
							}

							energy = ENERGY_COST_CALCULATOR.CONST_WEIGHT.calculate(
									BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_CONST_ENERGY, area.getBlizzards(),
									battery.getWeight(),
									BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_WEIGHT_MULTIPLIER);
							if (energy > robot.getTotalEnergy()) {
								return;
							}
							robot.drainEnergy(energy);

							if (WeatherEffects.preventedBy(a.getBlizzards())) {
								return;
							}

							a.getBatteries().remove(battery);
							robot.getCargo().addBattery(battery);

						}
					});
		});

		return true;
	}), COLLECT_ROCKS((planet, commands) -> {

		planet.getAllAreas().stream().forEach(area -> {

			int ENERGY_PER_KG = ENERGY_COST_CALCULATOR.CONST
					.calculate(BoardConfig.INT_CONFIG_ENTRY.COLLECT_BATTERY_CONST_ENERGY, null, 0, 0);

			RobotPromptCommandMap areaMiners = new RobotPromptCommandMap(area.getRobots(), commands, 1,
					END_OF_TURN_COMMAND.COLLECT_ROCKS).filter(promptCommand -> {
						return Integer.valueOf(promptCommand.argumentsArray[0]) > 0;
					});
			Map<Robot, Integer> rockAmounts = new HashMap<>();
			Map<Robot, Integer> collectAttempts = new HashMap<>();
			areaMiners.forEach(planet, (robot, promptCommand, p, a, coords) -> {
				rockAmounts.put(robot, Integer.valueOf(promptCommand.argumentsArray[0]));
				collectAttempts.put(robot, 0);
			});

			while (true) {

				List<Robot> stillMining = areaMiners.keySet().stream().filter(r -> {

					return collectAttempts.get(r) < rockAmounts.get(r);

				}).collect(Collectors.toList());

				if (stillMining.size() == 0) {
					break;
				}

				Collections.shuffle(stillMining);

				for (Robot r : stillMining) {

					collectAttempts.put(r, collectAttempts.get(r) + 1);

					if (ENERGY_PER_KG > r.getTotalEnergy()) {
						continue;
					}
					if (r.getCargo().load() + 1 > r.getCargo().getMaxLoad()) {
						continue;
					}

					r.drainEnergy(ENERGY_PER_KG);

					if (WeatherEffects.preventedBy(area.getBlizzards())) {
						return;
					}

					int rocks = area.mineRocks(1);
					r.getCargo().addRocks(rocks);

				}

			}

		});

		return true;
	}), MARKER_NEW((planet, commands) -> {

		planet.getAllAreas().forEach(area -> {

			int ENERGY_PER_CHAR = ENERGY_COST_CALCULATOR.CONST
					.calculate(BoardConfig.INT_CONFIG_ENTRY.MARKER_ENERGY_PER_CHAR, null, 0, 0);

			List<Robot> areaRobots = new ArrayList<>(area.getRobots());
			Collections.shuffle(areaRobots);
			RobotPromptCommandMap writers = new RobotPromptCommandMap(areaRobots, commands, 2,
					END_OF_TURN_COMMAND.MARKER_NEW).filter(promptCommand -> {
						return String.valueOf(promptCommand.argumentsArray[0]).length() > 0;
					});

			writers.forEach(planet, (robot, promptCommand, p, a, coords) -> {

				int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
						BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MIN,
						BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MAX, a.getBlizzards(), robot.getTotalWeight(),
						BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_WEIGHT_MULTIPLIER);
				int drained = robot.drainEnergy(energy);
				if (drained < energy) {
					return;
				}

				int groundMarkerPosition;
				if (WeatherEffects.success(area.getBlizzards())) {
					area.getMarkers().add(new String(""));
					groundMarkerPosition = area.getMarkers().size() - 1;
				} else {
					groundMarkerPosition = RandomEffects.getRandom(0, area.getMarkers().size() - 1);
				}

				String oldMarker = new String("");
				String newMarker = String.valueOf(promptCommand.argumentsArray[0]);
				String groundMarker = area.getMarkers().get(groundMarkerPosition);

				int numberOfChars = (oldMarker.length() > newMarker.length() ? oldMarker.length() : newMarker.length());
				int energyCost = ENERGY_PER_CHAR * numberOfChars;
				int drainedEnergy = robot.drainEnergy(energyCost);
				if (ENERGY_PER_CHAR > 0) {
					numberOfChars = drainedEnergy / ENERGY_PER_CHAR;
				}

				String marker = RandomEffects.mergeMarkers(oldMarker, newMarker, groundMarker, numberOfChars,
						area.getBlizzards());
				area.getMarkers().set(groundMarkerPosition, marker);

			});

		});

		return true;
	}), MARKER_OVERWRITE((planet, commands) -> {

		planet.getAllAreas().forEach(area -> {

			int ENERGY_PER_CHAR = ENERGY_COST_CALCULATOR.CONST
					.calculate(BoardConfig.INT_CONFIG_ENTRY.MARKER_ENERGY_PER_CHAR, null, 0, 0);

			List<Robot> areaRobots = new ArrayList<>(area.getRobots());
			Collections.shuffle(areaRobots);
			RobotPromptCommandMap writers = new RobotPromptCommandMap(areaRobots, commands, 2,
					END_OF_TURN_COMMAND.MARKER_OVERWRITE).filter(promptCommand -> {
						return String.valueOf(promptCommand.argumentsArray[1]).length() > 0
								&& Integer.valueOf(promptCommand.argumentsArray[0]) >= 0
								&& Integer.valueOf(promptCommand.argumentsArray[0]) < area.getMarkers().size();
					});

			writers.forEach(planet, (robot, promptCommand, p, a, coords) -> {

				int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
						BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MIN,
						BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_AREA_MAX, a.getBlizzards(), robot.getTotalWeight(),
						BoardConfig.INT_CONFIG_ENTRY.ROBOT_MOVE_WEIGHT_MULTIPLIER);
				int drained = robot.drainEnergy(energy);
				if (drained < energy) {
					return;
				}

				int groundMarkerPosition;
				if (WeatherEffects.success(area.getBlizzards())) {
					groundMarkerPosition = Integer.valueOf(promptCommand.argumentsArray[0]);
				} else {
					groundMarkerPosition = RandomEffects.getRandom(0, area.getMarkers().size() - 1);
					if (groundMarkerPosition == area.getMarkers().size()) {
						area.getMarkers().add(new String(""));
					}
				}

				String oldMarker = area.getMarkers().get(Integer.valueOf(promptCommand.argumentsArray[0]));
				String newMarker = String.valueOf(promptCommand.argumentsArray[1]);
				String groundMarker = area.getMarkers().get(groundMarkerPosition);

				int numberOfChars = (oldMarker.length() > newMarker.length() ? oldMarker.length() : newMarker.length());
				int energyCost = ENERGY_PER_CHAR * numberOfChars;
				int drainedEnergy = robot.drainEnergy(energyCost);
				if (ENERGY_PER_CHAR > 0) {
					numberOfChars = drainedEnergy / ENERGY_PER_CHAR;
				}

				String marker = RandomEffects.mergeMarkers(oldMarker, newMarker, groundMarker, numberOfChars,
						area.getBlizzards());
				area.getMarkers().set(groundMarkerPosition, marker);

			});

		});

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

}