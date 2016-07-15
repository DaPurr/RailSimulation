package wagon.rollingstock;

public class SLT6Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 6;
	}

	@Override
	public TrainType type() {
		return TrainType.SLT;
	}

	@Override
	public int getSeats1() {
		return 56;
	}

	@Override
	public int getSeats2() {
		return 224;
	}

	@Override
	public int getFoldableSeats() {
		return 58;
	}

	@Override
	public int getStandArea() {
		return 288;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 322;
	}

	@Override
	public int getNormA2() {
		return 437;
	}

	@Override
	public int getNormV2() {
		return 552;
	}
	
	@Override
	public String toString() {
		return "SLT6";
	}

}
