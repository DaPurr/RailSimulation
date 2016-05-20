package wagon.infrastructure;

/**
 * 
 * The <code>GPSLocation</code> class provides the latitude and longitude of the location 
 * it represents.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class GPSLocation {
	
	private double latitude;
	private double longitude;

	/**
	 * Creates a new <code>GPSLocation</code> object with the coordinates represented in 
	 * latitude and longitude.
	 * 
	 * This class is mainly needed for algorithms utilizing location-specific heuristics 
	 * (A* search, for example).
	 * 
	 * @param latitude	latitude of location
	 * @param longitude	longitude of location
	 */
	public GPSLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * @return	Returns the latitude of the location represented by <code>GPSLocation</code>.
	 */
	public double latitude() {
		return latitude;
	}
	
	/**
	 * @return	Returns the latitude of the location represented by 
	 * <code>GPSLocation</code>.
	 */
	public double longitude() {
		return longitude;
	}
	
	/**
	 * Calculates distance between two <code>GPSLocation</code> objects. Uses ... 
	 * to determine the distance between the two points.
	 * 
	 * @param	other	<code>GPSLocation</code> object representing other location
	 * @return	distance between the two GPS locations
	 */
	public double distance(GPSLocation other) {
		double phi1 = Math.toRadians(this.latitude);
		double phi2 = Math.toRadians(other.latitude);
		double lambda1 = Math.toRadians(this.longitude);
		double lambda2 = Math.toRadians(other.longitude);
		double delta_phi = phi2 - phi1;
		double delta_lambda = lambda2 - lambda1;
		double earthRadius = 6371008.8;
		
		double a = Math.sin(delta_phi/2)*Math.sin(delta_phi/2) + 
				Math.cos(phi1) * Math.cos(phi2) *
				Math.sin(delta_lambda/2)*Math.sin(delta_lambda/2);
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return earthRadius * c;
	}
	
	@Override
	public String toString() {
		String s = "(lat=" + latitude + ", lon=" + longitude + ")";
		return s;
	}
}
