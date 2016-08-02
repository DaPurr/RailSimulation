package sandbox;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.data.*;
import wagon.rollingstock.*;
import wagon.timetable.Timetable;

public class RealisationDataSandbox {

	public static void main(String[] args) {
		try {
			Timetable timetable = Timetable.importFromXML("data/materieelplan/processed/full_dataset_export.xml");
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
			
			RollingStockComposerBasic rcbasic = new RollingStockComposerBasic(
					timetable, 
					rdata, 
					0);
//			System.out.println(rcbasic.toString());
			Set<RollingStockUnit> units = new HashSet<>();
			units.add(new VIRM4Unit());
			Composition comp = new Composition(511, units);
			Composition realizedComp = rcbasic.realizedComposition(comp, null);
			System.out.println(realizedComp.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
