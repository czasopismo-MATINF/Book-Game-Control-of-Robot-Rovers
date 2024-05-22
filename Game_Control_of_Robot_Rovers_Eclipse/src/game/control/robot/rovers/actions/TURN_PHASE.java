package game.control.robot.rovers.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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
									BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
									BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(),
									robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
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

							if (WEATHER_EFFECTS.preventedBy(a.getBlizzards())) {
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
									BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
									BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(),
									robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
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

							if (WEATHER_EFFECTS.preventedBy(a.getBlizzards())) {
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

					if (WEATHER_EFFECTS.preventedBy(area.getBlizzards())) {
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
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(), robot.getTotalWeight(),
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
				int drained = robot.drainEnergy(energy);
				if (drained < energy) {
					return;
				}

				int groundMarkerPosition;
				if (WEATHER_EFFECTS.success(area.getBlizzards())) {
					area.getMarkers().add(new String(""));
					groundMarkerPosition = area.getMarkers().size() - 1;
				} else {
					groundMarkerPosition = RANDOM_EFFECTS.getRandom(0, area.getMarkers().size() - 1);
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

				String marker = RANDOM_EFFECTS.mergeMarkers(oldMarker, newMarker, groundMarker, numberOfChars,
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
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(), robot.getTotalWeight(),
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
				int drained = robot.drainEnergy(energy);
				if (drained < energy) {
					return;
				}

				int groundMarkerPosition;
				if (WEATHER_EFFECTS.success(area.getBlizzards())) {
					groundMarkerPosition = Integer.valueOf(promptCommand.argumentsArray[0]);
				} else {
					groundMarkerPosition = RANDOM_EFFECTS.getRandom(0, area.getMarkers().size() - 1);
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

				String marker = RANDOM_EFFECTS.mergeMarkers(oldMarker, newMarker, groundMarker, numberOfChars,
						area.getBlizzards());
				area.getMarkers().set(groundMarkerPosition, marker);

			});

		});

		return true;
	}), CHARGE_ROVER((planet, commands) -> {

		planet.getAllAreas().forEach(area -> {

			List<Robot> areaRobots = new ArrayList<>(area.getRobots());
			Collections.shuffle(areaRobots);
			RobotPromptCommandMap chargers = new RobotPromptCommandMap(areaRobots, commands, 3,
					END_OF_TURN_COMMAND.CHARGE_ROVER).filter(promptCommand -> {
						return String.valueOf(promptCommand.argumentsArray[1]).length() > 0
								&& String.valueOf(promptCommand.argumentsArray[1]).toUpperCase().equals("ENERGY")
								&& Integer.valueOf(promptCommand.argumentsArray[0]) >= 0
								&& Integer.valueOf(promptCommand.argumentsArray[2]) >= 0;
					});

			chargers.forEach(planet, (robot, promptCommand, p, a, coords) -> {

				int energyCost = ENERGY_COST_CALCULATOR.CONST
						.calculate(BoardConfig.INT_CONFIG_ENTRY.CHARGE_ROVER_CONNECTION_ENERGY, null, 0, 0);

				if (energyCost > robot.getTotalEnergy()) {
					return;
				}

				int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(), robot.getTotalWeight(),
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
				int drained = robot.drainEnergy(energy);
				if (drained < energy) {
					return;
				}

				int targetRobotId = Integer.valueOf(promptCommand.argumentsArray[0]);
				Robot targetRobot = a.getRobots().stream().filter(r -> r.getId() == targetRobotId).findAny()
						.orElse(null);
				if (targetRobot == null) {
					return;
				}

				if (WEATHER_EFFECTS.preventedBy(a.getBlizzards())) {
					return;
				}

				if (energyCost > robot.getTotalEnergy()) {
					return;
				}
				robot.drainEnergy(energyCost);

				int provEnergy = Integer.valueOf(promptCommand.argumentsArray[2]);
				int draiEnergy = robot.drainEnergy(provEnergy);
				int submEnergy = (int) (TURN_PHASE.getConfig()
						.getIntValue(BoardConfig.INT_CONFIG_ENTRY.CHARGE_ROVER_TRANSFER_ENERGY_PERCENT) * draiEnergy
						* 1.0 / 100);

				int tranEnergy = targetRobot.chargeEnergy(submEnergy);
				robot.chargeEnergy(tranEnergy);

			});

		});

		return true;
	}), CHARGING_STATION((planet, commands) -> {

		planet.getAllAreas().forEach(area -> {

			int numberOfAccessPoints = area.getChargingStations().stream()
					.collect(Collectors.summingInt(c -> c.getNumberOfAccessPoints()));
			List<Boolean> accessPoints = new ArrayList<>();
			for (int i = 0; i < numberOfAccessPoints; ++i) {
				accessPoints.add(true);
			}

			List<Robot> areaRobots = new ArrayList<>(area.getRobots());
			Collections.shuffle(areaRobots);
			RobotPromptCommandMap chargers = new RobotPromptCommandMap(areaRobots, commands, 3,
					END_OF_TURN_COMMAND.CHARGING_STATION);

			chargers.forEach(planet, (robot, promptCommand, p, a, coords) -> {

				int energyCost = ENERGY_COST_CALCULATOR.CONST
						.calculate(BoardConfig.INT_CONFIG_ENTRY.CHARGING_STATION_CONNECTION_ENERGY, null, 0, 0);

				if (energyCost > robot.getTotalEnergy()) {
					return;
				}

				int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(), robot.getTotalWeight(),
						BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
				int drained = robot.drainEnergy(energy);
				if (drained < energy) {
					return;
				}

				if (accessPoints.size() <= 0) {
					return;
				}

				if (WEATHER_EFFECTS.preventedBy(a.getBlizzards())) {
					return;
				}

				accessPoints.remove(0);
				robot.chargeFull();

			});

		});

		return true;
	}), DISTRIBUTE_ENERGY((planet, commands) -> {

		new RobotPromptCommandMap(planet.getAllRobots(), commands, 3, END_OF_TURN_COMMAND.DISTRIBUTE_ENERGY)
				.forEach(planet, (robot, promptCommand, p, area, coords) -> {

					if ("FULL".equals(promptCommand.argumentsArray[0].toUpperCase())) {

						int tranEnergy = (int) (TURN_PHASE.getConfig()
								.getIntValue(BoardConfig.INT_CONFIG_ENTRY.DISTRIBUTE_ENERGY_TRANSFER_PERCENT)
								* robot.getTotalEnergy() * 1.0 / 100);
						robot.drainAllEnergy();
						List<Battery> batteries = robot.getNotNullBatteries();
						for (int i = 0; i < batteries.size(); ++i) {
							if (tranEnergy <= 0) {
								break;
							}
							if (tranEnergy >= batteries.get(i).getCapacity()) {
								batteries.get(i).chargeFull();
								tranEnergy -= batteries.get(i).getEnergy();
							} else {
								batteries.get(i).charge(tranEnergy);
								tranEnergy = 0;
							}
						}

					} else if ("EVEN".equals(promptCommand.argumentsArray[0].toUpperCase())) {

						int tranEnergy = (int) (TURN_PHASE.getConfig()
								.getIntValue(BoardConfig.INT_CONFIG_ENTRY.DISTRIBUTE_ENERGY_TRANSFER_PERCENT)
								* robot.getTotalEnergy() * 1.0 / 100);
						robot.drainAllEnergy();
						for (int i = 0; i < tranEnergy; ++i) {
							robot.chargeEnergy(1);
						}

					}

				});

		return true;
	}), LOAD_CARGO_TO_MOTHER_SHIP((planet, commands) -> {

		planet.getAllAreas().forEach(area -> {

			new RobotPromptCommandMap(area.getRobots(), commands, 3, END_OF_TURN_COMMAND.LOAD_CARGO_TO_MOTHER_SHIP)
					.forEach(planet, (robot, promptCommand, p, a, coords) -> {

						int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
								BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
								BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(),
								robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
						int drained = robot.drainEnergy(energy);
						if (drained < energy) {
							return;
						}

						if (!area.hasMotherShip()) {
							return;
						}

						int energyCost = ENERGY_COST_CALCULATOR.CONST
								.calculate(BoardConfig.INT_CONFIG_ENTRY.LOAD_CARGO_TO_MOTHER_SHIP_ENERGY, null, 0, 0);
						if (energyCost > robot.getTotalEnergy()) {
							return;
						}
						robot.drainEnergy(energyCost);

						int rocks = robot.getCargo().getRocks();
						List<Battery> batteries = robot.getCargo().getBatteriesInCargo();

						MotherShip motherShip = area.getMotherShip();

						if (WEATHER_EFFECTS.preventedBy(a.getBlizzards())) {
							area.addBatteriesAndRocks(batteries, rocks);
						} else {
							motherShip.getCargo().addRocks(rocks);
							motherShip.getCargo().addBatteries(batteries);
						}

					});

		});

		return true;
	}), MOVE((planet, commands) -> {

		new RobotPromptCommandMap(planet.getAllRobots(), commands, 3, END_OF_TURN_COMMAND.MOVE).forEach(planet,
				(robot, promptCommand, p, a, coords) -> {

					if (String.valueOf(promptCommand.argumentsArray[0]).length() <= 0
							|| String.valueOf(promptCommand.argumentsArray[0]).length() > 1
							|| !"ESWN".contains(String.valueOf(promptCommand.argumentsArray[0]).toUpperCase())) {
						return;
					}

					int energy = ENERGY_COST_CALCULATOR.REACH_AREA.calculate(
							BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
							BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(), robot.getTotalWeight(),
							BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
					int drained = robot.drainEnergy(energy);
					if (drained < energy) {
						return;
					}

					p.moveRobot(robot.getId(), promptCommand.argumentsArray[0].toUpperCase());

				});

		return true;

	}), ENTER_MOTHER_SHIP((planet, commands) -> {

		planet.getAllAreas().forEach(area -> {

			new RobotPromptCommandMap(area.getRobots(), commands, 4, END_OF_TURN_COMMAND.ENTER_MOTHER_SHIP)
					.forEach(planet, (robot, promptCommand, p, a, coords) -> {

						int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
								BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
								BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(),
								robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
						int drained = robot.drainEnergy(energy);
						if (drained < energy) {
							return;
						}

						if (!area.hasMotherShip()) {
							return;
						}

						int energyCost = ENERGY_COST_CALCULATOR.CONST
								.calculate(BoardConfig.INT_CONFIG_ENTRY.ENTER_MOTHER_SHIP_ENERGY, null, 0, 0);
						if (energyCost > robot.getTotalEnergy()) {
							return;
						}
						robot.drainEnergy(energyCost);

						MotherShip motherShip = area.getMotherShip();

						if (!WEATHER_EFFECTS.preventedBy(a.getBlizzards())) {
							area.getRobots().remove(robot);
							motherShip.getRobots().add(robot);
						}

					});

		});

		return true;
	}), LAUNCH((planet, commands) -> {

		{

			MotherShip motherShip = planet.getMotherShip();

			if (motherShip != null) {

				new RobotPromptCommandMap(motherShip.getRobots(), commands, 5, END_OF_TURN_COMMAND.LAUNCH)
						.forEach(planet, (robot, promptCommand, p, a, coords) -> {

							int energyCost = ENERGY_COST_CALCULATOR.CONST.calculate(
									BoardConfig.INT_CONFIG_ENTRY.LAUNCH_MOTHER_SHIP_INSIDE_ENERGY, null, 0, 0);
							if (energyCost > robot.getTotalEnergy()) {
								return;
							}
							robot.drainEnergy(energyCost);

							motherShip.launch();

						});

			}

		}

		planet.getAllAreas().forEach(area -> {

			new RobotPromptCommandMap(area.getRobots(), commands, 5, END_OF_TURN_COMMAND.LAUNCH).forEach(planet,
					(robot, promptCommand, p, a, coords) -> {

						int energy = ENERGY_COST_CALCULATOR.REACH_DESTINATION_ON_AREA.calculate(
								BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MIN_ENERGY,
								BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_AREA_MAX_ENERGY, a.getBlizzards(),
								robot.getTotalWeight(), BoardConfig.INT_CONFIG_ENTRY.ROVER_MOVE_WEIGHT_MULTIPLIER);
						int drained = robot.drainEnergy(energy);
						if (drained < energy) {
							return;
						}

						if (!area.hasMotherShip()) {
							return;
						}

						int energyCost = ENERGY_COST_CALCULATOR.CONST
								.calculate(BoardConfig.INT_CONFIG_ENTRY.LAUNCH_MOTHER_SHIP_OUTSIDE_ENERGY, null, 0, 0);
						if (energyCost > robot.getTotalEnergy()) {
							return;
						}
						robot.drainEnergy(energyCost);

						MotherShip motherShip = area.getMotherShip();

						if (!WEATHER_EFFECTS.preventedBy(a.getBlizzards())) {
							motherShip.launch();
						}

					});

		});

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