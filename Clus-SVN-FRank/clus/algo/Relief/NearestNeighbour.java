package clus.algo.Relief;

public class NearestNeighbour {
	int m_indexInDataSet;
	double m_descriptiveDistance;
	double m_targetDistance;
	
	public NearestNeighbour(int index, double desDist, double tarDist){
		m_indexInDataSet = index;
		m_descriptiveDistance = desDist;
		m_targetDistance = tarDist;
	}

}
