package wagon.simulation;

import wagon.algorithms.Path;

/**
 * This class represents a certain passenger group, where 
 * passengers are aggregated into groups based on the <code>DefaultPath</code>s 
 * that they take.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class PassengerGroup {

	private Path path;
	private double size;
	
	/**
	 * Constructs a <code>PassengerGroup</code> object representing 
	 * <code>path</code>, of size <code>size</code>.
	 * 
	 * @param path	the path that this group will take
	 * @param size	group size
	 */
	public PassengerGroup(Path path, double size) {
		this.path = path;
		this.size = size;
	}
	
	/**
	 * Constructs a <code>PassengerGroup</code> object representing <code>path</code>, 
	 * that is initially empty.
	 * 
	 * @param path	the path this group will take
	 */
	public PassengerGroup(Path path) {
		this(path, 0.0);
	}
	
	/**
	 * Increments this group with 1.0.
	 */
	public void addPassenger() {
		size += 1.0;
	}
	
	/**
	 * Sets the grop size <code>size</code> equal to <code>newSize</code>.
	 * 
	 * @param newSize
	 */
	public void setSize(double newSize) {
		size = newSize;
	}
	
	/**
	 * @return group size
	 */
	public double size() {
		return size;
	}
	
	/**
	 * @return	<code>DefaultPath</code> corresponding to this passenger 
	 * 			group
	 */
	public Path getPath() {
		return new Path(path);
	}
	
	/**
	 * Set the current <code>DefaultPath</code> for this passenger 
	 * group.
	 * 
	 * @param path	<code>DefaultPath</code> to be traversed
	 */
	public void setPath(Path path) {
		this.path = new Path(path);
	}
}
