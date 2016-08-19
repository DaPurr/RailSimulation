package wagon.rollingstock;

import java.util.Iterator;

import com.google.common.collect.*;

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
