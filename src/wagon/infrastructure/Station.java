package wagon.infrastructure;

/**
 * This class models the necessary characteristics of a railway station.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class Station {
	
	private String name;
	
	/**
	 * Creates the <code>Station</code> object to represent a real-life railway station
	 * 
	 * @param	name	name of the station
	 */
	public Station(String name) {
		this.name = name;
	}
	
	/**
	 * @return	station name
	 */
	public String name() {
		return name;
	}
	
	@Override
	public String toString() {
		return name();
	}
}
