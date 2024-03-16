package game.control.robotic.rovers.board;

import java.io.Serializable;

public class Blizzard implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer volume;

	public Blizzard(Integer volume) {
		super();
		this.volume = volume;
	}

	public Integer getVolume() {
		return volume;
	}

}
