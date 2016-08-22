package sandbox;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.collect.*;

import wagon.data.*;
import wagon.rollingstock.*;
import wagon.timetable.Timetable;

public class RealisationDataSandbox {

	public static void main(String[] args) {
		try {
			Timetable timetable = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
			
			RollingStockComposerBasic rcbasic = new RollingStockComposerBasic(
					timetable, 
					rdata, 
					0);
			
			System.out.println(rcbasic.summary());
			
//			Multiset<RollingStockUnit> units = LinkedHashMultiset.create();
//			units.add(new VIRM4Unit());
//			TrainService comp = new TrainService(511, new Composition(units));
//			TrainService realizedComp = rcbasic.realizedComposition(comp, null);
//			System.out.println(realizedComp.toString());
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
