package game.control.robotic.rovers.board;

import java.io.Serializable;

public class Blizzard implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int volume;

	public Blizzard(int volume) {
		super();
		this.volume = volume;
	}

	public int getVolume() {
		return volume;
	}

}
