package game.control.robotic.rovers.board;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;

public class MotherShip implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<Robot> robots = new ArrayList<>();
	private Cargo cargo;
	private Integer maxLoad;
	
	public List<Robot> getRobots() {
		return robots;
	}
	
}
