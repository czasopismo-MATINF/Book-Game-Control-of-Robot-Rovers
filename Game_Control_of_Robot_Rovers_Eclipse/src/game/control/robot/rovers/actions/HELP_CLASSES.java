package game.control.robot.rovers.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import game.control.robot.rovers.board.Area;
import game.control.robot.rovers.board.Battery;
import game.control.robot.rovers.board.Blizzard;
import game.control.robot.rovers.board.GPSCoordinates;
import game.control.robot.rovers.board.Planet;
import game.control.robot.rovers.board.Robot;
import game.control.robot.rovers.config.BoardConfig;
import game.control.robot.rovers.config.GameConfig;

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
	}), REACH_AREA((min, max, blizzards, weight, multiplier) -> {
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

class TO_STRING {

	protected static String toString(Area area, boolean filtered) {

		String nvMsg = "N/A: not visible\n";
		String naMsg = "N/A: blizzards\n";
		String rsMsg = "rocks: %d\n";
		String csMsg = "charging stations: %d\n";
		String bsMsg = "blizzards: %d\n";
		String cMsg = "chasm: %b\n";
		String msMsg = "mother-ship: %b\n";
		String mMsg = "markers: %s\n";
		String rMsg = "robots: %s\n";
		String bMsg = "battery: %d capacity: %d energy: %d weigth: %d\n";

		StringBuffer buffer = new StringBuffer();

		if (area == null) {
			buffer.append(nvMsg);
			return buffer.toString();
		}
		if (!filtered && WEATHER_EFFECTS.preventedBy(area.getBlizzards())) {
			buffer.append(naMsg);
			return buffer.toString();
		}

		buffer.append(String.format(rsMsg, area.getRocks()));
		buffer.append(String.format(csMsg, area.getChargingStations().stream().count()));
		buffer.append(String.format(bsMsg, area.getBlizzardVolume()));
		buffer.append(String.format(cMsg, area.hasChasm()));
		buffer.append(String.format(msMsg, area.hasMotherShip()));
		buffer.append(String.format(mMsg, area.getMarkers().stream().collect(Collectors.joining(" "))));
		buffer.append(String.format(rMsg,
				area.getRobots().stream().map(r -> String.valueOf(r.getId())).collect(Collectors.joining(" "))));

		for (Battery b : area.getBatteries()) {
			buffer.append(String.format(bMsg, b.getId(), b.getCapacity(), b.getEnergy(), b.getWeight()));
		}

		return buffer.toString();

	}

	public static String toString(Planet planet, Area area) {

		String naMsg = "N/A: blizzards\n";
		String cMsg = "C:\n%s\n";
		String eMsg = "E:\n%s\n";
		String sMsg = "S:\n%s\n";
		String wMsg = "W:\n%s\n";
		String nMsg = "N:\n%s\n";

		StringBuffer buffer = new StringBuffer();

		if (WEATHER_EFFECTS.preventedBy(area.getBlizzards())) {

			buffer.append(naMsg);

		} else {

			GPSCoordinates coords = planet.areaGPSCoordinates(area);
			if (coords == null) {
				return null;
			}

			buffer.append(String.format(cMsg, TO_STRING.toString(planet.getArea(coords), false)));
			buffer.append(String.format(eMsg, TO_STRING.toString(planet.getArea(coords.getE()), false)));
			buffer.append(String.format(sMsg, TO_STRING.toString(planet.getArea(coords.getS()), false)));
			buffer.append(String.format(wMsg, TO_STRING.toString(planet.getArea(coords.getW()), false)));
			buffer.append(String.format(nMsg, TO_STRING.toString(planet.getArea(coords.getN()), false)));

		}

		return buffer.toString();

	}

	public static String toString(Robot robot) {

		StringBuffer buffer = new StringBuffer();

		String rMsg = "ROBOT: id: %d\n";
		String bMsg = "BATTERY: id: %d capacity: %d energy: %d weight: %d\n";
		String nMsg = "BATTERY: empty slot\n";
		String cMsg = "CARGO: rocks: %d batteries: %d\n";

		buffer.append(String.format(rMsg, robot.getId()));

		buffer.append(Arrays.asList(robot.getBatteries()).stream().map(b -> {
			if (b != null) {
				return buffer.append(String.format(bMsg, b.getId(), b.getCapacity(), b.getEnergy(), b.getWeight()));
			} else {
				return buffer.append(String.format(nMsg));
			}
		}).collect(Collectors.joining()));

		buffer.append(String.format(cMsg, robot.getCargo().getRocks(), robot.getCargo().getBatteriesInCargo().size()));

		return buffer.toString();

	}

	public static String toString(GPSCoordinates coords) {

		String gpsMsg = "GPS: longitude: %d latitude: %d width: %d height: %d\n";

		return String.format(gpsMsg, coords.getLongitude(), coords.getLatitude(), coords.getWidth(),
				coords.getHeight());

	}

}

class WEATHER_EFFECTS {

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

	protected static String characters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890-=!@#$%^&*()_+/'][;.,?\"}{:><";

	protected static Character randomCharacter() {
		Random random = new Random(System.currentTimeMillis());
		return WEATHER_EFFECTS.characters.charAt(random.nextInt(0, WEATHER_EFFECTS.characters.length()));
	}

	public static Character transferCharacter(Character c, List<Blizzard> blizzards, int power) {
		Character ret = c;
		Random random = new Random(System.currentTimeMillis());
		boolean success = true;
		for (Blizzard b : blizzards) {
			if (random.nextInt(0, (int) ((1.0 / ((double) power)) * b.getVolume())) >= 1) {
				success = false;
			}
		}
		if (!success) {
			ret = WEATHER_EFFECTS.randomCharacter();
		}
		return ret;
	}

}

class RANDOM_EFFECTS {

	public static int getRandom(int min, int max) {

		Random random = new Random(System.currentTimeMillis());

		return random.nextInt(min, max + 1);

	}

	public static String mergeMarkers(String oldMarker, String newMarker, String groundMarker, int chars,
			List<Blizzard> blizzards) {

		StringBuffer sBuffer = new StringBuffer();

		int i = 0;
		for (; i < chars; ++i) {
			if (i < newMarker.length()) {
				sBuffer.append(newMarker.charAt(i));
			} else {
				if (i < oldMarker.length()) {
					sBuffer.append('.');
				} else {
					if (i < groundMarker.length()) {
						sBuffer.append(groundMarker.charAt(i));
					}
				}
			}
		}

		for (; i < groundMarker.length(); ++i) {
			sBuffer.append(groundMarker.charAt(i));
		}

		return sBuffer.toString();

	}

}
