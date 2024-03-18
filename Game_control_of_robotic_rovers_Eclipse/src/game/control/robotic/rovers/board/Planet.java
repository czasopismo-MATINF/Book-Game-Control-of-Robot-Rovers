package game.control.robotic.rovers.board;

import java.io.Serializable;

public class Planet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer width;
	private Integer height;

	private Area[][] surface;

	public Planet(Integer width, Integer height) {
		super();
		this.width = width;
		this.height = height;
		this.surface = new Area[this.width][this.height];
		for(int i = 0; i < this.width; ++i) {
			for(int j = 0; j < this.height; ++j) {
				this.surface[i][j] = new Area();
			}
		}
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public Area[][] getSurface() {
		return surface;
	}

	public MotherShip extractMotherShip() {
		for(int i = 0; i < this.width; ++i) {
			for(int j = 0; j < this.height; ++j) {
				MotherShip motherShip = this.surface[i][j].getMotherShip();
				if(motherShip != null) {
					this.surface[i][j].setMotherShip(null);
					return motherShip;
				}
			}
		}
		return null;
	}
	
}
