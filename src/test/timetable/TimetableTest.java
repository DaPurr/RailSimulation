package test.timetable;

import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import wagon.infrastructure.Station;
import wagon.rollingstock.Composition;
import wagon.rollingstock.TrainType;
import wagon.timetable.ScheduledDeparture;
import wagon.timetable.Timetable;

public class TimetableTest {
	
	@Test
	public void testAddStationStationListOfScheduledDeparture() {
		int nrTimetables = 10;
		int nrDeps = 4;
		Timetable[] expectedTimetables = new Timetable[nrTimetables];
		Timetable[] actualTimetables = new Timetable[nrTimetables];
		Station stationFrom = new Station("from", 1);
		Station stationTo = new Station("to", 2);
		Composition comp = new Composition(TrainType.VIRM, 6, 100, 20);
		
		for (int i = 0; i < nrTimetables; i++) {
			LocalTime time = LocalTime.parse("12:00:00");
			Timetable actualTimetable = new Timetable();
			Timetable expectedTimetable = new Timetable();
			List<ScheduledDeparture> listDeps = new ArrayList<>();
			for (int j = 0; j < nrDeps; j++) {
				time = time.plusMinutes(5);
				ScheduledDeparture departure = 
						new ScheduledDeparture(comp, time, stationTo);
				listDeps.add(departure);
			}
			expectedTimetable.addStation(stationFrom, listDeps);
			expectedTimetables[i] = actualTimetable;
			List<ScheduledDeparture> shuffledDeps = new ArrayList<>(listDeps);
			Collections.shuffle(shuffledDeps);
			actualTimetable.addStation(stationFrom, shuffledDeps);
			actualTimetables[i] = expectedTimetable;
		}
		
		for (int i = 0; i < nrTimetables; i++) {
			Timetable actual = actualTimetables[i];
			Timetable expected = expectedTimetables[i];
			assertEquals("Incorrect nr. of scheduled departures.", nrDeps, 
					actual.departuresByStation(stationFrom).size());
			assertEquals("Scheduled departures not sorted.", expected, actual);
		}
	}

	@Test
	public void testAddStationStationScheduledDeparture() {
		int nrTimetables = 10;
		int nrDeps = 4;
		Timetable[] expectedTimetables = new Timetable[nrTimetables];
		Timetable[] actualTimetables = new Timetable[nrTimetables];
		Station stationFrom = new Station("from", 1);
		Station stationTo = new Station("to", 2);
		Composition comp = new Composition(TrainType.VIRM, 6, 100, 20);
		
		for (int i = 0; i < nrTimetables; i++) {
			LocalTime time = LocalTime.parse("12:00:00");
			Timetable actualTimetable = new Timetable();
			Timetable expectedTimetable = new Timetable();
			List<ScheduledDeparture> listDeps = new ArrayList<>();
			for (int j = 0; j < nrDeps; j++) {
				time = time.plusMinutes(5);
				ScheduledDeparture departure = 
						new ScheduledDeparture(comp, time, stationTo);
				listDeps.add(departure);
			}
			for (ScheduledDeparture dep : listDeps)
				expectedTimetable.addStation(stationFrom, dep);
			expectedTimetables[i] = actualTimetable;
			List<ScheduledDeparture> shuffledDeps = new ArrayList<>(listDeps);
			Collections.shuffle(shuffledDeps);
			for (ScheduledDeparture dep : shuffledDeps)
				actualTimetable.addStation(stationFrom, dep);
			actualTimetables[i] = expectedTimetable;
		}
		
		for (int i = 0; i < nrTimetables; i++) {
			Timetable actual = actualTimetables[i];
			Timetable expected = expectedTimetables[i];
			assertEquals("Incorrect nr. of scheduled departures.", 
					nrDeps, actual.departuresByStation(stationFrom).size());
			assertEquals("Scheduled departures not sorted.", expected, actual);
		}
	}

	@Test
	public void testDeparturesByStation() {
		Station from1 = new Station("Nwk", 1);
		Station from2 = new Station("Cps", 2);
		Station from3 = new Station("Rta", 3);
		Station to1 = new Station("Cps", 2);
		Station to2 = new Station("Rta", 3);
		Station to3 = new Station("Rtd", 4);
		
		Composition comp1 = new Composition(TrainType.SLT, 3, 100, 20);
		
		LocalTime time1 = LocalTime.parse("12:00:00");
		ScheduledDeparture sd1 = new ScheduledDeparture(comp1, time1, to1);
		LocalTime time2 = LocalTime.parse("12:04:00");
		ScheduledDeparture sd2 = new ScheduledDeparture(comp1, time2, to2);
		LocalTime time3 = LocalTime.parse("12:09:00");
		ScheduledDeparture sd3 = new ScheduledDeparture(comp1, time3, to3);
		
		Timetable timetable = new Timetable();
		timetable.addStation(from1, sd1);
		timetable.addStation(from2, sd2);
		timetable.addStation(from3, sd3);
		
		List<ScheduledDeparture> list1 = new ArrayList<>();
		list1.add(sd1);
		List<ScheduledDeparture> list2 = new ArrayList<>();
		list2.add(sd2);
		List<ScheduledDeparture> list3 = new ArrayList<>();
		list3.add(sd3);
		
		assertEquals("Incorrect scheduled departures.", 
				list1, timetable.departuresByStation(from1));
		assertEquals("Incorrect scheduled departures.", 
				list2, timetable.departuresByStation(from2));
		assertEquals("Incorrect scheduled departures.", 
				list3, timetable.departuresByStation(from3));
	}

}
