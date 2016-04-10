package wagon.network.expanded;

import java.time.LocalTime;

import wagon.infrastructure.Station;

public class DepartureNode extends EventNode {

	public DepartureNode(Station station, LocalTime time) {
		super(station, time);
	}
	
}
