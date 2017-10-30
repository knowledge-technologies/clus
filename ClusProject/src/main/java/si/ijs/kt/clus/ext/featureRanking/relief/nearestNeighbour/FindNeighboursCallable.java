package si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour;

import java.util.concurrent.Callable;

import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.util.tuple.Triple;

public class FindNeighboursCallable implements Callable<Triple<Integer, Integer, NearestNeighbour[][]>> {
	
	/** Instance of the Relief ranking where this object will be used from. */
	private ClusReliefFeatureRanking m_Relief;
	
	/** Index of the tuple in the dataset where we look for the neighbours. */
	private int m_TupleIndex;
	
	/** {@code -1 <= index < m_NbAttributes} with the same meaning as in the {@link clus.ext.featureRanking.relief.ClusReliefFeatureRanking} class. */
	private int m_TargetIndex;
	
	///** tuplesNeighbours[target value][i]: index (in the dataset) of the i-th closest neighbour, for a given target value */
	//private int[][] tuplesNeighbours;
	
	public FindNeighboursCallable(ClusReliefFeatureRanking relief, int tupleIndex, int targetIndex) {
		m_Relief = relief;
		m_TupleIndex = tupleIndex;
		m_TargetIndex = targetIndex;
	}
	
	@Override
	public Triple<Integer, Integer, NearestNeighbour[][]> call() throws Exception {
		return new Triple<Integer, Integer, NearestNeighbour[][]>(m_TargetIndex, m_TupleIndex, m_Relief.findNearestNeighbours(m_TupleIndex, m_TargetIndex));
	}

}
