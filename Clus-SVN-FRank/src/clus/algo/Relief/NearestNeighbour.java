package clus.algo.Relief;

public class NearestNeighbour {
	int m_indexInDataSet = -1;
	double m_descriptiveDistance = Double.NaN;;
	double m_targetDistance = Double.NaN;;
	
	public NearestNeighbour(int index, double desDist, double tarDist){
		m_indexInDataSet = index;
		m_descriptiveDistance = desDist;
		m_targetDistance = tarDist;
	}
	
	public String toString(){
		return String.format("(%d; %.4f; %.4f)", m_indexInDataSet, m_descriptiveDistance, m_targetDistance);
	}
	
}
