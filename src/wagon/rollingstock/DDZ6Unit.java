package wagon.rollingstock;

public class DDZ6Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 6;
	}

	@Override
	public TrainType type() {
		return TrainType.DDZ;
	}

	@Override
	public int getSeats1() {
		return 106;
	}

	@Override
	public int getSeats2() {
		return 439;
	}

	@Override
	public int getFoldableSeats() {
		return 62;
	}

	@Override
	public int getStandArea() {
		return 344;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 607;
	}

	@Override
	public int getNormA2() {
		return 748;
	}

	@Override
	public int getNormV2() {
		return 889;
	}
	
	@Override
	public String toString() {
		return "DDZ6";
	}

}
