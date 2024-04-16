package game.control.robot.rovers.board;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;

public class Planet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int width;
	protected int height;

	protected Area[][] surface;

	public void resetPlanet(int width, int height) {
		this.width = width;
		this.height = height;
		this.surface = new Area[this.width][this.height];
		for (int i = 0; i < this.width; ++i) {
			for (int j = 0; j < this.height; ++j) {
				this.surface[i][j] = new Area();
			}
		}
	}

	public Planet(int width, int height) {
		super();
		this.resetPlanet(width, height);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Area[][] getSurface() {
		return surface;
	}

	public MotherShip extractMotherShip() {
		for (int i = 0; i < this.width; ++i) {
			for (int j = 0; j < this.height; ++j) {
				MotherShip motherShip = this.surface[i][j].getMotherShip();
				if (motherShip != null) {
					this.surface[i][j].setMotherShip(null);
					return motherShip;
				}
			}
		}
		return null;
	}

	public MotherShip getMotherShip() {
		for (int i = 0; i < this.width; ++i) {
			for (int j = 0; j < this.height; ++j) {
				MotherShip motherShip = this.surface[i][j].getMotherShip();
				if (motherShip != null) {
					return motherShip;
				}
			}
		}
		return null;
	}

	public Robot getRobot(int robotId) {
		for (var i = 0; i < this.width; ++i) {
			for (var j = 0; j < this.height; ++j) {
				for (var r : this.surface[i][j].getRobots()) {
					if (r.getId() == robotId) {
						return r;
					}
				}
			}
		}
		return null;
	}

	public List<Robot> getAllRobots() {
		List<Robot> robots = new ArrayList<>();
		for (var i = 0; i < this.width; ++i) {
			for (var j = 0; j < this.height; ++j) {
				robots.addAll(this.surface[i][j].getRobots());
			}
		}
		return robots;
	}
	
	public List<Area> getAllAreas() {
		
		List<Area> areas = new ArrayList<>();
		for(int i = 0; i < this.getWidth(); ++i) {
			for(int j = 0; j < this.getHeight(); ++j) {
				areas.add(this.getSurface()[i][j]);
			}
		}
		return areas;
		
	}

	public GPSCoordinates robotGPSCoordinates(int robotId) {
		for (var i = 0; i < this.width; ++i) {
			for (var j = 0; j < this.height; ++j) {
				for (var r : this.surface[i][j].getRobots()) {
					if (r.getId() == robotId) {
						return new GPSCoordinates(i, j, this.width, this.height, GPSCoordinates.Mode.XYMODE);
					}
				}
			}
		}
		return null;
	}

	public void moveRobot(int robotId, String direction) {

		Robot robot;
		try {
			robot = this.getAllRobots().stream().filter(r -> r.getId() == robotId).findAny().get();
		} catch (NoSuchElementException e) {
			return;
		}
		GPSCoordinates gpsCoords = this.robotGPSCoordinates(robot.getId());

		switch (direction) {

		case "N", "n": {
			if (gpsCoords.getN() != null) {
				this.surface[gpsCoords.getX()][gpsCoords.getY()].getRobots().remove(robot);
				this.surface[gpsCoords.getN().getX()][gpsCoords.getN().getY()].getRobots().add(robot);
			}
		}
			return;
		case "E", "e": {
			this.surface[gpsCoords.getX()][gpsCoords.getY()].getRobots().remove(robot);
			this.surface[gpsCoords.getE().getX()][gpsCoords.getE().getY()].getRobots().add(robot);
		}
			return;
		case "S", "s": {
			if (gpsCoords.getS() != null) {
				this.surface[gpsCoords.getX()][gpsCoords.getY()].getRobots().remove(robot);
				this.surface[gpsCoords.getS().getX()][gpsCoords.getS().getY()].getRobots().add(robot);
			}
		}
			return;
		case "W", "w": {
			this.surface[gpsCoords.getX()][gpsCoords.getY()].getRobots().remove(robot);
			this.surface[gpsCoords.getW().getX()][gpsCoords.getW().getY()].getRobots().add(robot);
		}
			return;

		}

	}

}
