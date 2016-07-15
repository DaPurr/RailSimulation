package wagon.rollingstock;

public class SGM2Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 2;
	}

	@Override
	public TrainType type() {
		return TrainType.SGM;
	}

	@Override
	public int getSeats1() {
		return 24;
	}

	@Override
	public int getSeats2() {
		return 80;
	}

	@Override
	public int getFoldableSeats() {
		return 38;
	}

	@Override
	public int getStandArea() {
		return 184;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 142;
	}

	@Override
	public int getNormA2() {
		return 215;
	}

	@Override
	public int getNormV2() {
		return 288;
	}
	
	@Override
	public String toString() {
		return "SGM2";
	}

}
