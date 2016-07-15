package wagon.rollingstock;

public class VIRM4Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 4;
	}

	@Override
	public TrainType type() {
		return TrainType.VIRM;
	}

	@Override
	public int getSeats1() {
		return 63;
	}

	@Override
	public int getSeats2() {
		return 326;
	}

	@Override
	public int getFoldableSeats() {
		return 16;
	}

	@Override
	public int getStandArea() {
		return 186;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 405;
	}

	@Override
	public int getNormA2() {
		return 490;
	}

	@Override
	public int getNormV2() {
		return 575;
	}
	
	@Override
	public String toString() {
		return "VIRM4";
	}

}
