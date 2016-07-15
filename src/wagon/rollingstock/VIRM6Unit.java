package wagon.rollingstock;

public class VIRM6Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 6;
	}

	@Override
	public TrainType type() {
		return TrainType.VIRM;
	}

	@Override
	public int getSeats1() {
		return 132;
	}

	@Override
	public int getSeats2() {
		return 438;
	}

	@Override
	public int getFoldableSeats() {
		return 26;
	}

	@Override
	public int getStandArea() {
		return 276;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 596;
	}

	@Override
	public int getNormA2() {
		return 721;
	}

	@Override
	public int getNormV2() {
		return 846;
	}
	
	@Override
	public String toString() {
		return "VIRM6";
	}

}
