package wagon.rollingstock;

public abstract class RollingStockUnit {
	
	public RollingStockUnit() {
	}

	/**
	 * @return	number of wagons in composition
	 */
	public abstract int getNrWagons();

	/**
	 * @return	the rolling stock type of this composition
	 */
	public abstract TrainType type();
	
	/**
	 * @return	all available capacity
	 */
	public int getAllSeats() {
		return getSeats1() + getSeats2();
	}

	/**
	 * @return	business class passenger seats
	 */
	public abstract int getSeats1();

	/**
	 * @return	economy class passenger seats
	 */
	public abstract int getSeats2();
	
	/**
	 * @return	economy class foldable seats
	 */
	public abstract int getFoldableSeats();
	
	/**
	 * @return	economy class stand area capacity
	 */
	public abstract int getStandArea();
	
	/**
	 * @return	business class 'C' norm
	 */
	public abstract int getNormC1();

	/**
	 * @return	economy class 'C' norm
	 */
	public abstract int getNormC2();
	
	/**
	 * @return	economy class 'A' norm
	 */
	public abstract int getNormA2();

	/**
	 * @return	economy class 'V' norm
	 */
	public abstract int getNormV2();
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RollingStockUnit))
			return false;
		RollingStockUnit o = (RollingStockUnit) other;
		return this.getNrWagons() == o.getNrWagons() &&
				this.type() == o.type();
	}
	
	@Override
	public int hashCode() {
		return 13*Integer.hashCode(getNrWagons()) + 
				23*type().hashCode();
	}
	
	public static RollingStockUnit toUnit(TrainType type, int nrUnits) {
		switch (type) {
		case ICM:
			if (nrUnits == 3)
				return new ICM3Unit();
			else if (nrUnits == 4)
				return new ICM4Unit();
		case VIRM:
			if (nrUnits == 4)
				return new VIRM4Unit();
			else if (nrUnits == 6)
				return new VIRM6Unit();
		case DDZ:
			if (nrUnits == 4)
				return new DDZ4Unit();
			else if (nrUnits == 6)
				return new DDZ6Unit();
		case DM90:
			if (nrUnits == 2)
				return new DM902Unit();
		case SGM:
			if (nrUnits == 2)
				return new SGM2Unit();
			else if (nrUnits == 3)
				return new SGM3Unit();
		case SLT:
			if (nrUnits == 4)
				return new SLT4Unit();
			else if (nrUnits == 6)
				return new SLT6Unit();
		case DDM:
			if (nrUnits == 4)
				return new DDM4Unit();
		}
		
		// no match
		throw new IllegalArgumentException("No rolling stock unit exists by combination: " + type.toString() + nrUnits);
	}
}
