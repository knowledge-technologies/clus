package si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.util.tuple.Triple;

public class FindNeighboursCallable implements Callable<Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>>> {
	
	/** Instance of the Relief ranking where this object will be used from. */
	private ClusReliefFeatureRanking m_Relief;
	
	/** Index of the tuple in the dataset where we look for the neighbours. */
	private int m_TupleIndex;
	
	/** List of {@code -1 <= index < m_NbAttributes} with the same meaning as in the {@link clus.ext.featureRanking.relief.ClusReliefFeatureRanking} class. */
	private ArrayList<Integer> m_TargetIndices;
	
	public FindNeighboursCallable(ClusReliefFeatureRanking relief, int tupleIndex, ArrayList<Integer> necessaryTargets) {
		m_Relief = relief;
		m_TupleIndex = tupleIndex;
		m_TargetIndices = necessaryTargets;
	}
	
	@Override
	public Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>> call() throws Exception {
		return new Triple<ArrayList<Integer>, Integer, HashMap<Integer, NearestNeighbour[][]>>(m_TargetIndices, m_TupleIndex, m_Relief.findNearestNeighbours(m_TupleIndex, m_TargetIndices));
	}

}
