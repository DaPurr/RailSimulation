package wagon.simulation;

import wagon.infrastructure.Station;

public class Journey {
	public final Station origin;
	public final Station destination;
	
	public Journey(Station origin, Station destination) {
		this.origin = origin;
		this.destination = destination;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Journey))
			return false;
		Journey other = (Journey) o;
		return this.origin.equals(other.origin) &&
				this.destination.equals(other.destination);
	}
	
	@Override
	public int hashCode() {
		return 13*origin.hashCode() + 23*destination.hashCode();
	}
	
	@Override
	public String toString() {
		return "(" + origin.name() + ", " + destination.name() + ")";
	}
}