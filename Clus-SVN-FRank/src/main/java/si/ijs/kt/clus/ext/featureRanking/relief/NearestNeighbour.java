
package si.ijs.kt.clus.ext.featureRanking.relief;

public class NearestNeighbour {

    private final int m_indexInDataSet;
    private double m_descriptiveDistance = Double.NaN;
    private double m_targetDistance = Double.NaN;

    public NearestNeighbour(int index, double desDist, double tarDist) {
        m_indexInDataSet = index;
        m_descriptiveDistance = desDist;
        m_targetDistance = tarDist;
    }
    
    public String toString() {
        //return String.format("(ind: %d; descr. dist: %.4f; tar. dist: %.4f)", m_indexInDataSet, m_descriptiveDistance, m_targetDistance);
    	return String.format("NN(%d)", m_indexInDataSet);
    }
    
    public int getIndexInDataset(){
    	return m_indexInDataSet;
    }
    
    public double getTargetDistance(){
    	return m_targetDistance;
    }
    
    public boolean equals(Object other){
    	if(other instanceof NearestNeighbour){
    		return this.m_indexInDataSet == ((NearestNeighbour) other).m_indexInDataSet;
    	}
    	return false;
    }

}
