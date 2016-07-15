package wagon.rollingstock;

public class SGM3Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 3;
	}

	@Override
	public TrainType type() {
		return TrainType.SGM;
	}

	@Override
	public int getSeats1() {
		return 36;
	}

	@Override
	public int getSeats2() {
		return 128;
	}

	@Override
	public int getFoldableSeats() {
		return 58;
	}

	@Override
	public int getStandArea() {
		return 304;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 222;
	}

	@Override
	public int getNormA2() {
		return 345;
	}

	@Override
	public int getNormV2() {
		return 468;
	}
	
	@Override
	public String toString() {
		return "SGM3";
	}

}
