package test.timetable;

import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import wagon.components.infrastructure.Station;
import wagon.components.rollingstock.Composition;
import wagon.components.rollingstock.TrainType;
import wagon.components.timetable.ScheduledDeparture;
import wagon.components.timetable.Timetable;

public class TimetableTest {
	
	@Test
	public void testAddStationStationListOfScheduledDeparture() {
		Station stationFrom1 = new Station("test1", 26);
		Station stationTo1 = new Station("test2", 30);
		
		Composition comp1 = new Composition(TrainType.VIRM, 6, 100, 20);
		Composition comp2 = new Composition(TrainType.DDZ, 6, 100, 20);
		Composition comp3 = new Composition(TrainType.SLT, 6, 100, 20);
		Composition comp4 = new Composition(TrainType.SLT, 6, 100, 20);
		
		LocalTime time1 = LocalTime.parse("12:09");
		LocalTime time2 = LocalTime.parse("12:23");
		LocalTime time3 = LocalTime.parse("15:09");
		LocalTime time4 = LocalTime.parse("22:09");
		
		ScheduledDeparture sd1 = new ScheduledDeparture(comp1, time1, stationTo1);
		ScheduledDeparture sd2 = new ScheduledDeparture(comp2, time2, stationTo1);
		ScheduledDeparture sd3 = new ScheduledDeparture(comp3, time3, stationTo1);
		ScheduledDeparture sd4 = new ScheduledDeparture(comp4, time4, stationTo1);
		
		List<ScheduledDeparture> departures = new ArrayList<>();
		departures.add(sd1);
		departures.add(sd2);
		departures.add(sd3);
		departures.add(sd4);
		
		List<ScheduledDeparture> shuffledDepartures = new ArrayList<>(departures);
		Collections.shuffle(shuffledDepartures);
		
		Timetable tt1 = new Timetable();
		tt1.addStation(stationFrom1, shuffledDepartures);
		
		assertEquals("Departures not added.", 4, shuffledDepartures.size());
		assertEquals("Departures not sorted.", departures, tt1.departuresByStation(stationFrom1));
	}

	@Test
	public void testAddStationStationScheduledDeparture() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeparturesByStation() {
		fail("Not yet implemented");
	}

}
