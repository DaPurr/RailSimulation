package wagon.network.expanded;

import java.time.LocalDateTime;

import wagon.infrastructure.Station;

public class DepartureNode extends EventNode {

	public DepartureNode(Station station, LocalDateTime time) {
		super(station, time);
	}
	
}
