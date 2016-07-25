package wagon.data;

import wagon.rollingstock.Composition;
import wagon.timetable.ScheduledTrip;

public interface RollingStockComposer {

	public Composition realizedComposition(Composition comp, int trainNumber, ScheduledTrip trip);
}
