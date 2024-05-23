package game.control.robot.rovers;

import java.util.concurrent.Callable;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;
import game.control.robot.rovers.actions.HELPER_CLASSES;
import game.control.robot.rovers.actions.MESSAGE_COMMAND;
import game.control.robot.rovers.config.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ControlRobotsTurnGameRobotAI implements Callable<Boolean> {

	protected BoardConfig config = new GameConfig();

	protected ControlRobotTurnGameConcurrentShell gameShell;

	public BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

	public ControlRobotsTurnGameRobotAI(ControlRobotTurnGameConcurrentShell gameShell) {
		super();
		this.gameShell = gameShell;
	}

	/**********/

	protected void drop_cargo() {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.DROP_CARGO.messageFormat), this);
	}

	protected void drop_battery(int slot) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.DROP_BATTERY.messageFormat, slot), this);
	}

	protected void collect_battery_to_cargo(int batteryNumber) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.COLLECT_BATTERY.messageFormat, batteryNumber, "to", "cargo"), this);
	}

	protected void collect_battery_to_slot(int batteryNumber) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.COLLECT_BATTERY.messageFormat, batteryNumber, "to", "slot"), this);
	}

	protected void collect_rocks(int rocks) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.COLLECT_ROCKS.messageFormat, rocks), this);
	}

	protected void marker_new(String newMarker) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MARKER_NEW.messageFormat, newMarker), this);
	}

	protected void marker_overwrite(int markerId, String newMarker) {
		this.gameShell.runCommand(
				String.format(END_OF_TURN_COMMAND.MARKER_OVERWRITE.messageFormat, markerId, newMarker), this);
	}

	protected void charge_rover(int roverId, int energy) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.CHARGE_ROVER.messageFormat, roverId, energy), this);
	}

	protected void charging_station() {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.CHARGING_STATION.messageFormat), this);
	}

	protected void distribute_energy(String param) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.DISTRIBUTE_ENERGY.messageFormat, param), this);
	}

	protected String load_cargo_to_mother_ship() {
		return this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.LOAD_CARGO_TO_MOTHER_SHIP.messageFormat),
				this);
	}

	protected void move(Character direction) {
		this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.MOVE.messageFormat, direction), this);
	}

	protected String enter_mother_ship() {
		return this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.ENTER_MOTHER_SHIP.messageFormat), this);
	}

	protected String launch() {
		return this.gameShell.runCommand(String.format(END_OF_TURN_COMMAND.LAUNCH.messageFormat), this);
	}

	/**********/

	protected String look_around() {
		return this.gameShell.runCommand(String.format(MESSAGE_COMMAND.LOOK_AROUND.messageFormat), this);
	}

	protected String check_self() {
		return this.gameShell.runCommand(String.format(MESSAGE_COMMAND.CHECK_SELF.messageFormat), this);
	}

	protected String check_gps() {
		return this.gameShell.runCommand(String.format(MESSAGE_COMMAND.CHECK_GPS.messageFormat), this);
	}

	protected void send_message(char direction, String message, int power) {
		this.gameShell.runCommand(String.format(MESSAGE_COMMAND.SEND_MESSAGE.messageFormat, direction, message, power),
				this);
	}

	protected void send_gps_message(int longitude, int latitude, String message, int power) {
		this.gameShell.runCommand(
				String.format(MESSAGE_COMMAND.SEND_GPS_MESSAGE.messageFormat, longitude, latitude, message, power),
				this);
	}

	/**********/

	static class AreaResponse {

		boolean notVisible;
		boolean notVisibleBlizzards;
		int rocks;
		int chargingStations;
		int blizzards;
		boolean chasm;
		boolean motherShip;
		List<String> markers = new ArrayList<>();
		List<Integer> robots = new ArrayList<>();
		List<BatteryResponse> batteries = new ArrayList<>();

	}

	static class LookAroundResponse {

		String nvMsg = "N/A: not visible";
		String naMsg = "N/A: blizzards";

		AreaResponse C = new AreaResponse();
		AreaResponse E = new AreaResponse();
		AreaResponse S = new AreaResponse();
		AreaResponse W = new AreaResponse();
		AreaResponse N = new AreaResponse();

		boolean notVisibleBlizzards;

		LookAroundResponse(String response) {

			if (naMsg.equals(response)) {
				this.notVisibleBlizzards = true;
				return;
			}

			AreaResponse area = null;
			for (String line : response.split("\n")) {

				switch (line) {
				case "C:":
					area = C;
					break;
				case "E:":
					area = E;
					break;
				case "S:":
					area = S;
					break;
				case "W:":
					area = W;
					break;
				case "N:":
					area = N;
					break;
				}

				if (area != null) {

					if (nvMsg.equals(line)) {
						area.notVisible = true;
						continue;
					}

					if (naMsg.equals(line)) {
						area.notVisibleBlizzards = true;
						continue;
					}

					if (line.startsWith("rocks")) {
						area.rocks = Integer.valueOf(line.split(" ")[1]);
						continue;
					}
					if (line.startsWith("charging stations")) {
						area.chargingStations = Integer.valueOf(line.split(" ")[2]);
						continue;
					}
					if (line.startsWith("blizzards")) {
						area.blizzards = Integer.valueOf(line.split(" ")[1]);
						continue;
					}
					if (line.startsWith("chasm")) {
						area.chasm = Boolean.valueOf(line.split(" ")[1]);
						continue;
					}
					if (line.startsWith("mother-ship")) {
						area.motherShip = Boolean.valueOf(line.split(" ")[1]);
						continue;
					}
					if (line.startsWith("markers:")) {
						String[] resp = line.split(" ");
						for (String marker : Arrays.asList(resp).subList(1, resp.length)) {
							area.markers.add(marker);
						}
						continue;
					}
					if (line.startsWith("robots:")) {
						String[] resp = line.split(" ");
						for (String robotId : Arrays.asList(resp).subList(1, resp.length)) {
							area.robots.add(Integer.valueOf(robotId));
						}
						continue;
					}
					if (line.startsWith("battery:")) {
						String[] resp = line.split(" ");
						area.batteries.add(new BatteryResponse(Integer.valueOf(resp[1]), Integer.valueOf(resp[3]),
								Integer.valueOf(resp[5]), Integer.valueOf(resp[7])));
						continue;
					}
				}
			}

		}
	}

	static class BatteryResponse {

		int id;
		int capacity;
		int energy;
		int weight;

		BatteryResponse(int id, int capacity, int energy, int weight) {
			this.id = id;
			this.capacity = capacity;
			this.energy = energy;
			this.weight = weight;
		}

	}

	static class SelfCheckResponse {

		int id;
		int rocksInCargo;
		int batteriesInCargo;
		BatteryResponse[] batteries = new BatteryResponse[BoardConfig.INT_CONFIG_ENTRY.ROVER_MAX_BATTERIES.defaultValue];
		List<BatteryResponse> notNullBatteries = new ArrayList<>();

		SelfCheckResponse(String response) {
			// int bc = 0;
			for (String line : response.split("\n")) {
				if (line.startsWith("robot")) {
					this.id = Integer.valueOf(line.split(" ")[2]);
					continue;
				}
				if (line.startsWith("rocks")) {
					this.rocksInCargo = Integer.valueOf(line.split(" ")[1]);
					continue;
				}
				if (line.startsWith("batteries")) {
					this.batteriesInCargo = Integer.valueOf(line.split(" ")[1]);
					continue;
				}
				if (line.startsWith("battery")) {
					if (line.equals("battery: empty slot")) {
						// TODO
						// this.batteries[bc++] = null;
					} else {
						String[] resp = line.split(" ");
						// this.batteries[bc++] = new BatteryResponse(Integer.valueOf(resp[2]),
						// Integer.valueOf(resp[4]),
						// Integer.valueOf(resp[6]), Integer.valueOf(resp[8]));
						this.notNullBatteries.add(new BatteryResponse(Integer.valueOf(resp[2]),
								Integer.valueOf(resp[4]), Integer.valueOf(resp[6]), Integer.valueOf(resp[8])));
					}
					continue;
				}
			}
		}

		int totalEnergy() {
			return this.notNullBatteries.stream().collect(Collectors.summingInt(b -> b.energy));
		}

		int totalCapacity() {
			return this.notNullBatteries.stream().collect(Collectors.summingInt(b -> b.capacity));
		}

	}

	static class GPSResponse {

		int longitude;
		int latitude;
		int width;
		int height;

		GPSResponse(String response) {
			String[] resp = response.split("\n")[0].split(" ");
			this.longitude = Integer.valueOf(resp[2]);
			this.latitude = Integer.valueOf(resp[4]);
			this.width = Integer.valueOf(resp[6]);
			this.height = Integer.valueOf(resp[8]);
		}

	}

	/**********/

	static class PlanetInfo {

		String MSInfo;
		int MSLongitude;
		int MSLatitude;

		void parse(String msg) {

			// TODO: validate message before parsing, discard invalid messages

			if (msg.startsWith("MS")) {
				if (msg.split("_").length >= 3) {
					this.MSInfo = msg;
					this.MSLongitude = Integer.valueOf(msg.split("_")[1]);
					this.MSLatitude = Integer.valueOf(msg.split("_")[2]);
				}
			}

		}
	}

	PlanetInfo info = new PlanetInfo();

	/**********/

	public Boolean call() {

		LookAroundResponse lar = new LookAroundResponse(this.look_around());
		SelfCheckResponse scr = new SelfCheckResponse(this.check_self());
		GPSResponse gpsr = new GPSResponse(this.check_gps());

		while (!this.messageQueue.isEmpty()) {
			this.info.parse(this.messageQueue.element());
			this.messageQueue.poll();
		}
		
		if(lar.C.chargingStations > 0 && scr.totalEnergy() < 0.5d * ((double) scr.totalCapacity())) {
			this.charging_station();
		}

		if (lar.C.motherShip && scr.rocksInCargo > 0) {
			this.marker_new("CZASOPISMO-MATINF-s0/2024");
			this.load_cargo_to_mother_ship();
			this.enter_mother_ship();
			this.launch();
			return true;
		}

		if (lar.C.motherShip && lar.C.rocks > 0) {
			this.collect_rocks(1);
			this.marker_new("CZASOPISMO-MATINF-s0/2024");
			this.load_cargo_to_mother_ship();
			this.enter_mother_ship();
			this.launch();
			return true;
		}

		if (lar.C.motherShip) {
			scr = new SelfCheckResponse(this.check_self());
			while (scr.totalEnergy() > 0) {
				this.send_gps_message(HELPER_CLASSES.RANDOM_EFFECTS.getRandom(0, gpsr.width),
						HELPER_CLASSES.RANDOM_EFFECTS.getRandom(-gpsr.height, gpsr.height),
						String.format("MS_%d_%d", gpsr.longitude, gpsr.latitude), 10);
				scr = new SelfCheckResponse(this.check_self());
			}
			return true;
		}

		if (lar.C.rocks > 0 && scr.rocksInCargo == 0) {
			this.collect_rocks(1);
		}

		if (this.info.MSInfo != null) {
			if (this.info.MSLongitude != gpsr.longitude) {
				this.move('W');
			} else {
				if (this.info.MSLatitude > gpsr.latitude) {
					this.move('N');
				} else if (this.info.MSLatitude < gpsr.latitude) {
					this.move('S');
				}
			}

		} else {
			switch (HELPER_CLASSES.RANDOM_EFFECTS.getRandom(0, 3)) {
			case 0: {
				if (lar.E.notVisibleBlizzards == false && lar.E.notVisible == false && lar.E.chasm == false) {
					this.move('E');
				}
			}
				break;
			case 1: {
				if (lar.S.notVisibleBlizzards == false && lar.S.notVisible == false && lar.S.chasm == false) {
					this.move('S');
				}
			}
				break;
			case 2: {
				if (lar.W.notVisibleBlizzards == false && lar.W.notVisible == false && lar.W.chasm == false) {
					this.move('W');
				}
			}
				break;
			case 3: {
				if (lar.N.notVisibleBlizzards == false && lar.N.notVisible == false && lar.N.chasm == false) {
					this.move('N');
				}
			}
				break;
			}
		}

		return true;

	}

}
