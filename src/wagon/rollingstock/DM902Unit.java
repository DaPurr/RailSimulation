package wagon.rollingstock;

public class DM902Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 2;
	}

	@Override
	public TrainType type() {
		return TrainType.DM90;
	}

	@Override
	public int getSeats1() {
		return 12;
	}

	@Override
	public int getSeats2() {
		return 105;
	}

	@Override
	public int getFoldableSeats() {
		return 34;
	}

	@Override
	public int getStandArea() {
		return 134;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 151;
	}

	@Override
	public int getNormA2() {
		return 201;
	}

	@Override
	public int getNormV2() {
		return 251;
	}
	
	@Override
	public String toString() {
		return "DM902";
	}

}
