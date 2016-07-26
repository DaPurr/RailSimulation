package sandbox;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.data.RealisationData;
import wagon.data.RollingStockComposerBasic;
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
					rdata);
			System.out.println(rcbasic.toString());
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
