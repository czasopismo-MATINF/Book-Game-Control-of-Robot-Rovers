package game.control.robot.rovers.board;

import java.io.Serializable;

public class GPSCoordinates implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	enum Mode {
		XYMODE
	}
	
	protected int x;
	protected int y;
	
	protected int longitude;
	protected int latitude;
	
	protected int width;
	protected int height;

	public GPSCoordinates(int longitude, int latitude, int width, int height) {
		super();
		
		this.longitude = longitude;
		this.latitude = latitude;
		
		this.width = width;
		this.height = height;

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
	
	public GPSCoordinates(int x, int y, int width, int height, Mode mode) {
		super();
		
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
		
		this.longitude = x;
		this.latitude = (height / 2) - y;
		
	}
	
	public GPSCoordinates getN() {
		if(this.y > 0) {
			return new GPSCoordinates(this.x, this.y - 1, this.width, this.height, Mode.XYMODE);
		}
		return null;
	}
	
	public GPSCoordinates getE() {
		return new GPSCoordinates(longitude + 1, latitude, width, height);
	}
	
	public GPSCoordinates getS() {
		if(this.y < this.height - 1) {
			return new GPSCoordinates(this.x, this.y + 1, this.width, this.height, Mode.XYMODE);
		}
		return null;
	}
	
	public GPSCoordinates getW() {
		return new GPSCoordinates(longitude - 1, latitude, width, height);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
