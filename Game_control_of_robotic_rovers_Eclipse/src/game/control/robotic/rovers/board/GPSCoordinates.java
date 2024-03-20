package game.control.robotic.rovers.board;

import java.io.Serializable;

public class GPSCoordinates implements Serializable {

	int x;
	int y;

	public GPSCoordinates(int longitude, int latitude, int width, int height) {
		super();

		this.x = longitude % width;
		if( this.x < 0 ) this.x = width + this.x;

		if ((height % 2) == 0) {
			if (latitude >= 0) {
				this.y = (height / 2) - (latitude % ((height / 2) + 1));
			} else {
				this.y = (height / 2) - (latitude % (height / 2));
			}
		} else {
			this.y = (height / 2) - (latitude % ((height / 2) + 1));
		}

	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
