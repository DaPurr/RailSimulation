package wagon.components.rollingstock;

/**
 * This class is used to model a train composition in the simulation. Most
 * importantly, it is necessary to know the composition capacity as that
 * influences passenger service
 * 
 * @author Nemanja Milovanovic
 *
 */

public class Composition {

	private int nrWagons;
	private TrainType type;
	private int capacity1;
	private int capacity2;

	/**
	 * Constructs a <code>Composition</code> object reflecting a simplified 
	 * train composition.
	 * 
	 * @param type		the type of rolling stock
	 * @param nrWagons	number of trains in the composition
	 * @param capacity1	capacity for business class
	 * @param capacity2	capacity for economy class
	 */
	public Composition(TrainType type, int nrWagons, int capacity1, int capacity2) {
		this.type = type;
		this.nrWagons = nrWagons;
		this.capacity1 = capacity1;
		this.capacity2 = capacity2;
	}

	/**
	 * @return	number of wagons in composition
	 */
	public int getNrWagons() {
		return nrWagons;
	}

	/**
	 * @return	the rolling stock type of this composition
	 */
	public TrainType type() {
		return type;
	}

	/**
	 * @return	business class passenger capacity
	 */
	public int capacity1() {
		return capacity1;
	}

	/**
	 * @return	economy class passenger capacity
	 */
	public int capacity2() {
		return capacity2;
	}
}
