package wagon.rollingstock;

public class DDM4Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 4;
	}

	@Override
	public TrainType type() {
		return TrainType.DDM;
	}

	@Override
	public int getSeats1() {
		return 64;
	}

	@Override
	public int getSeats2() {
		return 448;
	}

	@Override
	public int getFoldableSeats() {
		return 77;
	}

	@Override
	public int getStandArea() {
		return 205;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 525;
	}

	@Override
	public int getNormA2() {
		return 589;
	}

	@Override
	public int getNormV2() {
		return 653;
	}
	
	@Override
	public String toString() {
		return "DDM4";
	}

}
