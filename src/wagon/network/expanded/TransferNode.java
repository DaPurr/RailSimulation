package wagon.network.expanded;

import java.time.LocalDateTime;

import wagon.infrastructure.Station;
import wagon.network.Node;

public class TransferNode implements Node, Comparable<TransferNode> {
	
	private LocalDateTime time;
	private Station station;
	
	public TransferNode(LocalDateTime time, Station station) {
		this.time = time;
		this.station = station;
	}
	
	public Station getStation() {
		return station;
	}
	
	public LocalDateTime getTime() {
		return time;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TransferNode))
			return false;
		TransferNode other = (TransferNode) o;
		return this.station.equals(other.station) &&
				this.time.equals(other.time);
	}
	
	@Override
	public int compareTo(TransferNode o) {
		int res1 = this.station.name().compareTo(o.station.name());
		if (res1 != 0)
			return res1;
		return this.time.compareTo(o.time);
	}
	
	@Override
	public int hashCode() {
		return 7*station.hashCode() + 13*time.hashCode();
	}
	
	@Override
	public String toString() {
		return "Transfer: " + getStation().name() + "@" + getTime().toString();
	}

}
