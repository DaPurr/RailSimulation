package wagon.network.expanded;

import java.time.LocalDateTime;

import wagon.infrastructure.Station;

public class ArrivalNode extends EventNode {

	public ArrivalNode(Station station, LocalDateTime time) {
		super(station, time);
	}
	
}
