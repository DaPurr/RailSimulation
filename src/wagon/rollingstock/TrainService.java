package wagon.rollingstock;

import wagon.timetable.ComfortNorm;

/**
 * This class is used to model a train composition in the simulation. Most
 * importantly, it is necessary to know the composition capacity as that
 * influences passenger service
 * 
 * @author Nemanja Milovanovic
 *
 */

public class TrainService {
	
	private int id;
	private Composition composition;
	
	public TrainService(int id, Composition composition) {
		this.id = id;
		this.composition = composition;
	}

	public int id() {
		return id;
	}
	
	public Composition getComposition() {
		return composition;
	}
	
	public TrainService copy() {
		TrainService comp = new TrainService(
				id, 
				composition.copy());
		return comp;
	}

	public int getNrWagons() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getNrWagons();
		return count;
	}

	public TrainType type() {
		return TrainType.VIRM;
	}

	public int getAllSeats() {
		return getSeats1() + getSeats2() + getFoldableSeats();
	}

	public int getSeats1() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getSeats1();
		return count;
	}

	public int getSeats2() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getSeats2();
		return count;
	}

	public int getFoldableSeats() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getFoldableSeats();
		return count;
	}

	public int getStandArea() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getStandArea();
		return count;
	}

	public int getNormC1() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getNormC1();
		return count;
	}

	public int getNormC2() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getNormC2();
		return count;
	}

	public int getNormA2() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getNormA2();
		return count;
	}

	public int getNormV2() {
		int count = 0;
		for (RollingStockUnit unit : composition)
			count += unit.getNormV2();
		return count;
	}
	
	public int normCapacity1(ComfortNorm norm) {
		return getSeats1();
	}
	
	public int normCapacity2(ComfortNorm norm) {
		int count = 0;
		
		switch (norm) {
		case A:
			for (RollingStockUnit unit : composition)
				count += unit.getNormA2();
			return count;
		case C:
			for (RollingStockUnit unit : composition)
				count += unit.getNormC2();
			return count;
		case V:
			for (RollingStockUnit unit : composition)
				count += unit.getNormV2();
			return count;
		}
		
		throw new IllegalStateException("There cannot be another comfort norm.");
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TrainService)) 
			return false;
		TrainService o = (TrainService) other;
		return this.id == o.id;
	}
	
	@Override
	public int hashCode() {
		return 7*Integer.hashCode(id);
	}
	
	@Override
	public String toString() {
		return "[" + id + ": " + composition.toString() + "]";
	}
}