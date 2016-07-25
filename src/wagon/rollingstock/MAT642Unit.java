package wagon.rollingstock;

public class MAT642Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 2;
	}

	@Override
	public TrainType type() {
		return TrainType.MAT64;
	}

	@Override
	public int getSeats1() {
		return 24;
	}

	@Override
	public int getSeats2() {
		return 110;
	}

	@Override
	public int getFoldableSeats() {
		return 23;
	}

	@Override
	public int getStandArea() {
		return 103;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 157;
	}

	@Override
	public int getNormA2() {
		return 197;
	}

	@Override
	public int getNormV2() {
		return 237;
	}

}
