package wagon.simulation;

/**
 * This class represents a statistical counter. It is therefore basically a wrapper 
 * class for an integer value. It is designed to provide ease-of-access with use 
 * in combination of other classes in this package.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class Counter {

	private double count;
	private String name;
	
	/**
	 * Constructs a <code>Counter</code> with initial value of zero.
	 * 
	 * @param name	the name of this counter
	 */
	public Counter(String name) {
		this(name, 0);
	}
	
	/**
	 * Constructs a <code>Counter</code> with initial value <code>startValue</code>.
	 * 
	 * @param name			the name of this counter
	 * @param startValue	the start value
	 */
	public Counter(String name, double startValue) {
		count = startValue;
		this.name = name;
	}
	
	/**
	 * Increments the counter by <code>incr</code>.
	 * 
	 * @param incr	the increment
	 */
	public double increment(double incr) {
		count += incr;
		return count;
	}
	
	/**
	 * Increments the counter by 1.
	 */
	public double increment() {
		return increment(1);
	}
	
	/**
	 * @return	returns the current value of this counter.
	 */
	public double getValue() {
		return count;
	}
	
	/**
	 * @return	returns the name of this <code>Counter</code>
	 */
	public String name() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + ": " + count;
	}
}
