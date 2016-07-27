package wagon.rollingstock;

import java.util.*;

import wagon.timetable.ComfortNorm;

/**
 * This class is used to model a train composition in the simulation. Most
 * importantly, it is necessary to know the composition capacity as that
 * influences passenger service
 * 
 * @author Nemanja Milovanovic
 *
 */

public class Composition {
	
	private int id;
	private Set<RollingStockUnit> wagons;
	
	public Composition(int id, Set<RollingStockUnit> wagons) {
		this.id = id;
		this.wagons = wagons;
	}

	public int id() {
		return id;
	}
	
	public Set<RollingStockUnit> getUnits() {
		return new HashSet<>(wagons);
	}
	
	public Composition copy() {
		Composition comp = new Composition(
				id, 
				new HashSet<>(wagons));
		return comp;
	}

	public int getNrWagons() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getNrWagons();
		return count;
	}

	public TrainType type() {
		return TrainType.VIRM;
	}

	public int getAllSeats() {
		return getSeats1() + getSeats2();
	}

	public int getSeats1() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getSeats1();
		return count;
	}

	public int getSeats2() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getSeats2();
		return count;
	}

	public int getFoldableSeats() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getFoldableSeats();
		return count;
	}

	public int getStandArea() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getStandArea();
		return count;
	}

	public int getNormC1() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getNormC1();
		return count;
	}

	public int getNormC2() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getNormC2();
		return count;
	}

	public int getNormA2() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
			count += unit.getNormA2();
		return count;
	}

	public int getNormV2() {
		int count = 0;
		for (RollingStockUnit unit : wagons)
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
			for (RollingStockUnit unit : wagons)
				count += unit.getNormA2();
			return count;
		case C:
			for (RollingStockUnit unit : wagons)
				count += unit.getNormC2();
			return count;
		case V:
			for (RollingStockUnit unit : wagons)
				count += unit.getNormV2();
			return count;
		}
		
		throw new IllegalStateException("There cannot be another comfort norm.");
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Composition)) 
			return false;
		Composition o = (Composition) other;
		return this.id == o.id && 
				this.wagons.equals(o.wagons);
	}
	
	@Override
	public int hashCode() {
		return 7*Integer.hashCode(id) + 13*wagons.hashCode();
	}
	
	@Override
	public String toString() {
		return "[" + id + ": " + wagons.toString() + "]";
	}
}