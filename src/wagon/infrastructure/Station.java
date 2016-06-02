package wagon.infrastructure;

/**
 * This class models the necessary characteristics of a railway station.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class Station {
	
	private String name;
	private GPSLocation location;
	
	/**
	 * Creates the <code>Station</code> object to represent a real-life railway station
	 * 
	 * @param	name	name of the station
	 */
	public Station(String name, GPSLocation location) {
		this.name = name.toLowerCase();
		this.location = location;
	}
	
	public Station(String name) {
		this.name = name.toLowerCase();
		this.location = null;
	}
	
	/**
	 * @return	station name
	 */
	public String name() {
		return name;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += name;
		if (location != null)
			s += ": " + location.toString();
		return s;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Station))
			return false;
		Station o = (Station) other;
		return this.name.equalsIgnoreCase(o.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
