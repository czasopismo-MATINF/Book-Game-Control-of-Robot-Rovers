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
		this.surface = new Area[width][height];
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

}
