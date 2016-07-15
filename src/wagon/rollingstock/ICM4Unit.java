package wagon.rollingstock;

public class ICM4Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 4;
	}

	@Override
	public TrainType type() {
		return TrainType.ICM;
	}

	@Override
	public int getSeats1() {
		return 56;
	}

	@Override
	public int getSeats2() {
		return 211;
	}

	@Override
	public int getFoldableSeats() {
		return 32;
	}

	@Override
	public int getStandArea() {
		return 179;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 299;
	}

	@Override
	public int getNormA2() {
		return 373;
	}

	@Override
	public int getNormV2() {
		return 446;
	}
	
	@Override
	public String toString() {
		return "ICM4";
	}

}
