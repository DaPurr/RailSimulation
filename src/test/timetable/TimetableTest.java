package test.timetable;

import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.Test;

import wagon.infrastructure.Station;
import wagon.rollingstock.*;
import wagon.timetable.*;

public class TimetableTest {
	
	@Test
	public void testAddStationStationListOfScheduledDeparture() {
		int nrTimetables = 10;
		int nrDeps = 4;
		Timetable[] expectedTimetables = new Timetable[nrTimetables];
		Timetable[] actualTimetables = new Timetable[nrTimetables];
		Station stationFrom = new Station("from");
		Station stationTo = new Station("to");
		
		Set<RollingStockUnit> units1 = new HashSet<>();
		units1.add(new VIRM6Unit());
		Composition comp = new Composition(1, units1);
		
		for (int i = 0; i < nrTimetables; i++) {
			LocalTime time = LocalTime.parse("12:00:00");
			Timetable actualTimetable = new Timetable();
			Timetable expectedTimetable = new Timetable();
			List<ScheduledTrip> listDeps = new ArrayList<>();
			for (int j = 0; j < nrDeps; j++) {
				time = time.plusMinutes(5);
				ScheduledTrip departure = 
						new ScheduledTrip(comp, time, time.plusMinutes(5), stationFrom, stationTo, ComfortNorm.C, 2);
				listDeps.add(departure);
			}
			expectedTimetable.addStation(stationFrom, listDeps);
			expectedTimetables[i] = actualTimetable;
			List<ScheduledTrip> shuffledDeps = new ArrayList<>(listDeps);
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
		Station stationFrom = new Station("from");
		Station stationTo = new Station("to");

		Set<RollingStockUnit> units1 = new HashSet<>();
		units1.add(new VIRM6Unit());
		Composition comp = new Composition(1, units1);
		
		for (int i = 0; i < nrTimetables; i++) {
			LocalTime time = LocalTime.parse("12:00:00");
			Timetable actualTimetable = new Timetable();
			Timetable expectedTimetable = new Timetable();
			List<ScheduledTrip> listDeps = new ArrayList<>();
			for (int j = 0; j < nrDeps; j++) {
				time = time.plusMinutes(5);
				ScheduledTrip departure = 
						new ScheduledTrip(comp, time, time.plusMinutes(5), stationFrom, stationTo, ComfortNorm.C, 2);
				listDeps.add(departure);
			}
			for (ScheduledTrip dep : listDeps)
				expectedTimetable.addStation(stationFrom, dep);
			expectedTimetables[i] = actualTimetable;
			List<ScheduledTrip> shuffledDeps = new ArrayList<>(listDeps);
			Collections.shuffle(shuffledDeps);
			for (ScheduledTrip dep : shuffledDeps)
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
		Station from1 = new Station("Nwk");
		Station from2 = new Station("Cps");
		Station from3 = new Station("Rta");
		Station to1 = new Station("Cps");
		Station to2 = new Station("Rta");
		Station to3 = new Station("Rtd");
		
		Set<RollingStockUnit> units1 = new HashSet<>();
		units1.add(new SLT4Unit());
		Composition comp = new Composition(1, units1);
		
		LocalTime time1 = LocalTime.parse("12:00:00");
		ScheduledTrip sd1 = new ScheduledTrip(comp, time1, time1.plusMinutes(2), from1, to1, ComfortNorm.C, 2);
		LocalTime time2 = LocalTime.parse("12:04:00");
		ScheduledTrip sd2 = new ScheduledTrip(comp, time2, time2.plusMinutes(2), from2, to2, ComfortNorm.C, 2);
		LocalTime time3 = LocalTime.parse("12:09:00");
		ScheduledTrip sd3 = new ScheduledTrip(comp, time3, time3.plusMinutes(3), from3, to3, ComfortNorm.C, 2);
		
		Timetable timetable = new Timetable();
		timetable.addStation(from1, sd1);
		timetable.addStation(from2, sd2);
		timetable.addStation(from3, sd3);
		
		List<ScheduledTrip> list1 = new ArrayList<>();
		list1.add(sd1);
		List<ScheduledTrip> list2 = new ArrayList<>();
		list2.add(sd2);
		List<ScheduledTrip> list3 = new ArrayList<>();
		list3.add(sd3);
		
		assertEquals("Incorrect scheduled departures.", 
				list1, timetable.departuresByStation(from1));
		assertEquals("Incorrect scheduled departures.", 
				list2, timetable.departuresByStation(from2));
		assertEquals("Incorrect scheduled departures.", 
				list3, timetable.departuresByStation(from3));
	}
	
	@Test
	public void testGetRoute() {
		Station from1 = new Station("Nwk");
		Station from2 = new Station("Cps");
		Station from3 = new Station("Rta");
		Station to1 = new Station("Cps");
		Station to2 = new Station("Rta");
		Station to3 = new Station("Rtd");
		
		Set<RollingStockUnit> units1 = new HashSet<>();
		units1.add(new SLT4Unit());
		Composition comp1 = new Composition(1, units1);
		
		Set<RollingStockUnit> units2 = new HashSet<>();
		units2.add(new SGM3Unit());
		Composition comp2 = new Composition(1, units2);
		
		LocalTime time1 = LocalTime.parse("12:00:00");
		ScheduledTrip sd1 = new ScheduledTrip(comp1, time1, time1.plusMinutes(2), from1, to1, ComfortNorm.C, 2);
		LocalTime time2 = LocalTime.parse("12:04:00");
		ScheduledTrip sd2 = new ScheduledTrip(comp1, time2, time2.plusMinutes(2), from2, to2, ComfortNorm.C, 2);
		LocalTime time3 = LocalTime.parse("12:09:00");
		ScheduledTrip sd3 = new ScheduledTrip(comp1, time3, time3.plusMinutes(3), from3, to3, ComfortNorm.C, 2);
		
		LocalTime time4 = LocalTime.parse("15:00:00");
		ScheduledTrip sd4 = new ScheduledTrip(comp2, time4, time4.plusMinutes(2), from1, to1, ComfortNorm.C, 2);
		LocalTime time5 = LocalTime.parse("15:04:00");
		ScheduledTrip sd5 = new ScheduledTrip(comp2, time5, time5.plusMinutes(2), from2, to2, ComfortNorm.C, 2);
		LocalTime time6 = LocalTime.parse("15:09:00");
		ScheduledTrip sd6 = new ScheduledTrip(comp2, time6, time6.plusMinutes(3), from3, to3, ComfortNorm.C, 2);
		
		Timetable timetable = new Timetable();
		timetable.addStation(from1, sd1);
		timetable.addStation(from2, sd2);
		timetable.addStation(from3, sd3);
		timetable.addStation(from1, sd4);
		timetable.addStation(from3, sd6);
		timetable.addStation(from2, sd5);
		
		List<ScheduledTrip> list1 = new ArrayList<>();
		list1.add(sd1);
		list1.add(sd2);
		list1.add(sd3);
		
		List<ScheduledTrip> list2 = new ArrayList<>();
		list2.add(sd4);
		list2.add(sd5);
		list2.add(sd6);
		
		assertEquals("Incorrect route length.", list1.size(), timetable.getRoute(comp1).size());
		assertEquals("Incorrect route length.", list2.size(), timetable.getRoute(comp2).size());
		
		SortedSet<ScheduledTrip> route1 = timetable.getRoute(comp1);
		SortedSet<ScheduledTrip> route2 = timetable.getRoute(comp2);
		assertEquals("Incorrect route.", list1, route1);
		assertEquals("Incorrect route.", list2, route2);
	}

}
