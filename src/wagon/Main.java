package wagon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import wagon.algorithms.*;
import wagon.data.CiCoData;
import wagon.network.expanded.EventActivityNetwork;
import wagon.simulation.*;
import wagon.timetable.Timetable;

public class Main {

	public static void main(String[] args) {
		try {
//			Timetable sample = Timetable.importFromExcel("data/materieelplan/full_dataset.xlsx", 2);
//			Timetable sample = Timetable.importFromExcel("data/materieelplan/smaller_sample_schedule1.xlsx", 2);
//			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/smaller_sample_schedule1_export.xml");
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset_day2_export.xml");
//			sample.export("data/materieelplan/processed/full_dataset_day2_export.xml");
//			sample.export("data/materieelplan/processed/smaller_sample_schedule1_day2_export.xml");
			EventActivityNetwork network = EventActivityNetwork.createTransferNetwork(sample, 0);
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork5();
			
			BiCriterionDijkstra biDijkstra = new BiCriterionDijkstra(network, Criterion.DISTANCE, Criterion.TRANSFER);
//			BiCriterionDijkstra biDijkstra = new BiCriterionDijkstra(network, Criterion.TRANSFER, Criterion.DISTANCE);
			biDijkstra.lexicographicallyFirst("vss", "gn", LocalDateTime.parse("2016-04-11T10:00"));
			
			Options options = new Options("data/cico/ritten_20160315.csv", null, 2);
			
			CiCoData cicoData = CiCoData
					.importRawData(
							"data/cico/ritten_20160315.csv",
							"data/cico/omzettabel_stations.csv",
							options); // hardcoded
			cicoData.getJourneySummary();
			
//			cicoData.exportPassengers("data/cico/processed/ritten_20160209_processed.csv");
//			Collection<Passenger> selectedPassengers = cicoData.getPassengersWithJourney("rta", "ut");
//			Collection<Passenger> selectedPassengers = cicoData.getPassengersAtCheckInStation("rta");
//			List<Double> frequencies = CiCoData.drawPassengerArrivalRate(selectedPassengers, 10);
//			cicoData.exportEmpiricalArrivalRateOfCheckInStation(
//					"rta", 
//					10*60, 
//					"data/cico/frequencies/20160209/freqs_rta_10min.csv");
//			cicoData.exportEmpiricalArrivalRateOfJourney(
//					"rta",
//					"ut",
//					10*60, 
//					"data/cico/frequencies/20160209/freqs_rta_ut_5min.csv");
			
//			PiecewiseConstantProcess arrivals = new PiecewiseConstantProcess(selectedPassengers, 10*60, 0);
//			arrivals.exportDrawsFromProcess(10*60, "data/cico/test.csv");
//			arrivals.exportArrivalRate("data/cico/rates_piecewise_constant.csv");
//			System.out.println("p: " + arrivals.kolmogorovSmirnovTest());
			
//			SimModel sim = new SimModel(sample, 
//					network, 
//					options);
//			Report report = sim.start();
//			System.out.println(report.summary());
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (InvalidFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
