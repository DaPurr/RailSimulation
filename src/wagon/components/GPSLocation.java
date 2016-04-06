package wagon.components;

/**
 * 
 * The <code>GPSLocation</code> class provides the latitude and longitude of the location it represents.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class GPSLocation {
	
	private double latitude;
	private double longitude;

	/**
	 * Creates a new <code>GPSLocation</code> object with the coordinates represented in latitude and longitude.
	 * 
	 * This class is mainly needed for algorithms utilizing location-specific heuristics (A* search, for example).
	 * 
	 * @param latitude	latitude of location
	 * @param longitude	longitude of location
	 */
	public GPSLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Returns the latitude of the location represented by <code>GPSLocation</code>.
	 * 
	 * @return	latitude of location
	 */
	public double latitude() {
		return latitude;
	}
	
	/**
	 * Returns the latitude of the location represented by <code>GPSLocation</code>.
	 * 
	 * @return	longitude of location
	 */
	public double longitude() {
		return longitude;
	}
	
	/**
	 * Calculates distance between two <code>GPSLocation</code> objects. Uses ... to determine the distance between the two points.
	 * 
	 * @param	other	<code>GPSLocation</code> object representing other location
	 * @return	distance between the two GPS locations
	 */
	public double distance(GPSLocation other) {
		return 0;
	}
	
	@Override
	public String toString() {
		String s = "(lat=" + latitude + ", lon=" + longitude + ")";
		return s;
	}
}
