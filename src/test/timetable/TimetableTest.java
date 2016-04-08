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
			assertEquals("Incorrect nr. of scheduled departures.", nrDeps, actual.size());
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
			assertEquals("Incorrect nr. of scheduled departures.", nrDeps, actual.size());
			assertEquals("Scheduled departures not sorted.", expected, actual);
		}
	}

	@Test
	public void testDeparturesByStation() {
		assertEquals("BOE", 1, 1);
	}

}
