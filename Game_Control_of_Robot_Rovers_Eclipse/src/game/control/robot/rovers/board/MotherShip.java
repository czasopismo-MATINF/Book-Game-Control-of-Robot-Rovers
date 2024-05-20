package game.control.robot.rovers.board;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class MotherShip implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected List<Robot> robots = new ArrayList<>();
	protected Cargo cargo = new Cargo();
	protected int maxLoad;
	protected boolean launched = false;

	public MotherShip(int maxLoad) {
		super();
		this.maxLoad = maxLoad;
	}

	public List<Robot> getRobots() {
		return robots;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public int getMaxLoad() {
		return maxLoad;
	}

	public boolean isLaunched() {
		return this.launched;
	}

	public void launch() {
		this.launched = true;
	}

}
