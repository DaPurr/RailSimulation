package wagon.rollingstock;

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
	private int seats1;
	private int seats2;
	private int foldable;
	private int standArea;
	private int id;
	
	private int normC1;
	private int normC2;
	private int normA2;
	private int normV2;
	
	private ComfortNorm norm;

	/**
	 * Constructs a <code>Composition</code> object reflecting a simplified 
	 * train composition.
	 * 
	 * @param type		the type of rolling stock
	 * @param nrWagons	number of trains in the composition
	 * @param seats1	number of seats for business class
	 * @param seats2	number of seats for economy class
	 * @param foldable	number of foldable seats in economy class
	 * @param standArea	stand area capacity for economy class
	 * @param normC1	norm 'C' capacity for business class
	 * @param normC2	norm 'C' capacity for economy class
	 * @param normA2	norm 'A' capacity for economy class
	 * @param normV2	norm 'V' capacity for economy class
	 * @param norm		the comfort norm for this trip
	 */
	public Composition(int id, TrainType type, int nrWagons, int seats1, int seats2,
			int foldable, int standArea, int normC1, int normC2, int normA2, 
			int normV2, ComfortNorm norm) {
		this.type = type;
		this.nrWagons = nrWagons;
		this.seats1 = seats1;
		this.seats2 = seats2;
		this.foldable = foldable;
		this.id = id;
		this.standArea = standArea;
		
		this.normC1 = normC1;
		this.normC2 = normC2;
		this.normA2 = normA2;
		this.normV2 = normV2;
		this.norm = norm;
	}
	
	/**
	 * @return	this composition's ID
	 */
	public int id() {
		return id;
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
	 * @return	all available capacity
	 */
	public int getAllSeats() {
		return seats1 + seats2;
	}

	/**
	 * @return	business class passenger seats
	 */
	public int getSeats1() {
		return seats1;
	}

	/**
	 * @return	economy class passenger seats
	 */
	public int getSeats2() {
		return seats2;
	}
	
	/**
	 * @return	economy class foldable seats
	 */
	public int getFoldableSeats2() {
		return foldable;
	}
	
	/**
	 * @return	economy class stand area capacity
	 */
	public int getStandArea2() {
		return standArea;
	}
	
	/**
	 * @return	business class 'C' norm
	 */
	public int getNormC1() {
		return normC1;
	}

	/**
	 * @return	economy class 'C' norm
	 */
	public int getNormC2() {
		return normC2;
	}
	
	/**
	 * @return	economy class 'A' norm
	 */
	public int getNormA2() {
		return normA2;
	}

	/**
	 * @return	economy class 'V' norm
	 */
	public int getNormV2() {
		return normV2;
	}
	
	public ComfortNorm getNorm() {
		return norm;
	}
	
	public int normCapacity() {
		switch (norm) {
		case A:
			return normA2 + seats1;
		case C:
			return normC1 + normC2;
		case V:
			return normV2 + seats1;
		}
		
		throw new IllegalStateException("There cannot be another comfort norm.");
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Composition))
			return false;
		Composition o = (Composition) other;
		return this.id == o.id;
	}
	
	@Override
	public int hashCode() {
		return Integer.hashCode(id);
	}
}
