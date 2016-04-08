package wagon.components.network.expanded;

import java.time.LocalTime;

import wagon.components.infrastructure.Station;

public class ArrivalNode extends EventNode {

	public ArrivalNode(Station station, LocalTime time) {
		super(station, time);
	}
	
}
