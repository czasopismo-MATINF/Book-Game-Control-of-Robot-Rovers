package game.control.robotic.rovers.board;

public class GPSCoordinates {

	Integer x;
	Integer y;
	
	public GPSCoordinates(Integer longitude, Integer latitude, Integer width, Integer height) {
		super();
		
		this.x = longitude % width;
		
		if( ( height % 2 ) == 0 ) {
			if( latitude >= 0 ) {
				this.y = ( height / 2 ) - ( latitude % ( ( height / 2 ) + 1 ) );
			} else {
				this.y = ( height / 2 ) - ( latitude % ( height / 2 ) );
			}
		} else {
			this.y = ( height / 2 ) - ( latitude % ( ( height / 2 ) + 1 ) );
		}
		
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

}
