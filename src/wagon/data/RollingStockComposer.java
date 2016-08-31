package wagon.data;

import wagon.rollingstock.TrainService;
import wagon.timetable.Trip;

public interface RollingStockComposer {

	public TrainService realizedComposition(TrainService comp, Trip trip);
}
