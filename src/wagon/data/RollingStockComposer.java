package wagon.data;

import wagon.rollingstock.TrainService;
import wagon.timetable.ScheduledTrip;

public interface RollingStockComposer {

	public TrainService realizedComposition(TrainService comp, ScheduledTrip trip);
}
