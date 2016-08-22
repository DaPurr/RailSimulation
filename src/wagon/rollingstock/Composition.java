package wagon.rollingstock;

import java.util.Iterator;

import com.google.common.collect.*;

import wagon.timetable.ComfortNorm;

public class Composition implements Iterable<RollingStockUnit> {

	private Multiset<RollingStockUnit> units;
	
	public Composition() {
		units = LinkedHashMultiset.create();
	}
	
	public Composition(Iterable<RollingStockUnit> units) {
		this.units = LinkedHashMultiset.create();
		for (RollingStockUnit unit : units)
			add(unit);
	}
	
	public void add(RollingStockUnit unit) {
		units.add(unit);
	}
	
	public int getNrUnits() {
		return units.size();
	}
	
	public Composition copy() {
		Composition comp = new Composition();
		comp.units = LinkedHashMultiset.create(units);
		return comp;
	}
	
	public static Composition toComposition(String comp) {
		Composition composition = new Composition();
		String[] parts = comp.split("-");
		for (String part : parts) {
			RollingStockUnit unit = RollingStockUnit
					.toUnit(
							TrainType.valueOf(part.substring(0, part.length()-1)), 
							Integer.valueOf(part.substring(part.length()-1, part.length()))
							);
			composition.add(unit);
		}
		return composition;
	}
	
	public int getAllSeats() {
		return getSeats1() + getSeats2();
	}

	public int getSeats1() {
		int count = 0;
		for (RollingStockUnit unit : units)
			count += unit.getSeats1();
		return count;
	}

	public int getSeats2() {
		int count = 0;
		for (RollingStockUnit unit : units)
			count += unit.getSeats2();
		return count;
	}

	public int getFoldableSeats() {
		int count = 0;
		for (RollingStockUnit unit : units)
			count += unit.getFoldableSeats();
		return count;
	}

	public int getStandArea() {
		int count = 0;
		for (RollingStockUnit unit : units)
			count += unit.getStandArea();
		return count;
	}

	public int getNormC1() {
		int count = 0;
		for (RollingStockUnit unit : units)
			count += unit.getNormC1();
		return count;
	}

	public int getNormC2() {
		int count = 0;
		for (RollingStockUnit unit : units)
			count += unit.getNormC2();
		return count;
	}

	public int getNormA2() {
		int count = 0;
		for (RollingStockUnit unit : units)
			count += unit.getNormA2();
		return count;
	}

	public int getNormV2() {
		int count = 0;
		for (RollingStockUnit unit : units)
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
			for (RollingStockUnit unit : units)
				count += unit.getNormA2();
			return count;
		case C:
			for (RollingStockUnit unit : units)
				count += unit.getNormC2();
			return count;
		case V:
			for (RollingStockUnit unit : units)
				count += unit.getNormV2();
			return count;
		}
		
		throw new IllegalStateException("There cannot be another comfort norm.");
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Composition))
			return false;
		Composition other = (Composition) o;
		return this.units.equals(other.units);
	}
	
	@Override
	public int hashCode() {
		return units.hashCode();
	}
	
	@Override
	public String toString() {
		String s = "";
		int count = 0;
		for (RollingStockUnit unit : units) {
			if (count != 0)
				s += "-";
			s += unit.toString();
			count++;
		}
		return s;
	}

	@Override
	public Iterator<RollingStockUnit> iterator() {
		return units.iterator();
	}
}
