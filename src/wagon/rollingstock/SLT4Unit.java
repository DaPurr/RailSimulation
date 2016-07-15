package wagon.rollingstock;

import wagon.rollingstock.RollingStockUnit;

public class SLT4Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 4;
	}

	@Override
	public TrainType type() {
		return TrainType.SLT;
	}

	@Override
	public int getSeats1() {
		return 40;
	}

	@Override
	public int getSeats2() {
		return 144;
	}

	@Override
	public int getFoldableSeats() {
		return 32;
	}

	@Override
	public int getStandArea() {
		return 170;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 216;
	}

	@Override
	public int getNormA2() {
		return 285;
	}

	@Override
	public int getNormV2() {
		return 354;
	}
	
	@Override
	public String toString() {
		return "SLT4";
	}

}
