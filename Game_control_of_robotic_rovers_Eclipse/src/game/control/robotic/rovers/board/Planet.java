package game.control.robotic.rovers.board;

import java.io.Serializable;

public class Planet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int width;
	private int height;

	private Area[][] surface;

	public Planet(int width, int height) {
		super();
		this.width = width;
		this.height = height;
		this.surface = new Area[this.width][this.height];
		for (int i = 0; i < this.width; ++i) {
			for (int j = 0; j < this.height; ++j) {
				this.surface[i][j] = new Area();
			}
		}
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
	
	public Robot getRobot(int robotId) {
		for (var i = 0; i < this.width; ++i) {
			for (var j = 0; j < this.height; ++j) {
				for(var r : this.surface[i][j].getRobots()) {
					if( r.getId() == robotId ) {
						return r;
					}
				}
			}
		}
		return null;
	}

}
