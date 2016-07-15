package wagon.rollingstock;

public class ICM3Unit extends RollingStockUnit {

	@Override
	public int getNrWagons() {
		return 3;
	}

	@Override
	public TrainType type() {
		return TrainType.ICM;
	}

	@Override
	public int getSeats1() {
		return 35;
	}

	@Override
	public int getSeats2() {
		return 163;
	}

	@Override
	public int getFoldableSeats() {
		return 30;
	}

	@Override
	public int getStandArea() {
		return 172;
	}

	@Override
	public int getNormC1() {
		return getSeats1();
	}

	@Override
	public int getNormC2() {
		return 228;
	}

	@Override
	public int getNormA2() {
		return 299;
	}

	@Override
	public int getNormV2() {
		return 370;
	}
	
	@Override
	public String toString() {
		return "ICM3";
	}

}
