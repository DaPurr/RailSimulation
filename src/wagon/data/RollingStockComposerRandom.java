package wagon.data;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import wagon.rollingstock.*;
import wagon.timetable.Trip;

public class RollingStockComposerRandom implements RollingStockComposer {
	
	private RandomGenerator random;
	private RealisationData rdata;
	
	public RollingStockComposerRandom(RealisationData rdata, long seed) {
		random = new MersenneTwister(seed);
		this.rdata = rdata;
	}

	@Override
	public TrainService realizedComposition(TrainService comp, Trip trip) {
		Set<Composition> allCompositions = new HashSet<>();
		for (Entry<Integer, SortedSet<RealisationDataEntry>> entry : rdata.entrySet()) {
			for (RealisationDataEntry rEntry : entry.getValue()) {
				allCompositions.add(rEntry.getPlannedComposition().getComposition());
				allCompositions.add(rEntry.getRealizedComposition().getComposition());
			}
		}
		
		List<Composition> allCompsList = new ArrayList<>(allCompositions);
		
		int r = random.nextInt(allCompsList.size());
		TrainService service = new TrainService(comp.id(), allCompsList.get(r));
		return service;
	}

}
