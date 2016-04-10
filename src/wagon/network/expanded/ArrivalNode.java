package wagon.network.expanded;

import java.time.LocalTime;

import wagon.infrastructure.Station;

public class ArrivalNode extends EventNode {

	public ArrivalNode(Station station, LocalTime time) {
		super(station, time);
	}
	
}
