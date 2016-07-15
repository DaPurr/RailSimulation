package wagon.rollingstock;

public class DDZ4Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 4;
	}

	@Override
	public TrainType type() {
		return TrainType.DDZ;
	}

	@Override
	public int getSeats1() {
		return 67;
	}

	@Override
	public int getSeats2() {
		return 269;
	}

	@Override
	public int getFoldableSeats() {
		return 37;
	}

	@Override
	public int getStandArea() {
		return 207;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 373;
	}

	@Override
	public int getNormA2() {
		return 458;
	}

	@Override
	public int getNormV2() {
		return 543;
	}
	
	@Override
	public String toString() {
		return "DDZ4";
	}

}
