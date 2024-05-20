package game.control.robot.rovers.board;

import java.io.Serializable;

public class ChargingStation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int numberOfAccessPoints;

	public ChargingStation(int numberOfAccessPoints) {
		super();
		this.numberOfAccessPoints = numberOfAccessPoints;
	}

	public int getNumberOfAccessPoints() {
		return numberOfAccessPoints;
	}

}
