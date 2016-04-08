package wagon.components.network.expanded;

import java.time.LocalTime;

import wagon.components.infrastructure.Station;

public class DepartureNode extends EventNode {

	public DepartureNode(Station station, LocalTime time) {
		super(station, time);
	}
	
}
