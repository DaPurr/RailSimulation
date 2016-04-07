package wagon.components.infrastructure;

/**
 * This class models the necessary characteristics of a railway station.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class Station {
	
	private String name;
	private int id;
	
	/**
	 * Creates the <code>Station</code> object to represent a real-life railway station
	 * 
	 * @param	name	name of the station
	 * @param	id		unique station id
	 */
	public Station(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	/**
	 * 
	 * @return	station name
	 */
	public String name() {
		return name;
	}
	
	/**
	 * 
	 * @return	station id
	 */
	public int id() {
		return id;
	}
}
