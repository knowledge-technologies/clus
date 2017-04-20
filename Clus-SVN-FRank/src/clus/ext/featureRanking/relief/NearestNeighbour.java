
package clus.ext.featureRanking.relief;

import java.util.Arrays;

public class NearestNeighbour {

    private int m_indexInDataSet = -1;
    private double m_descriptiveDistance = Double.NaN;
    private double m_targetDistance = Double.NaN;
    private double[] m_perTargetDistances;

    public NearestNeighbour(int index, double desDist, double tarDist) {
        m_indexInDataSet = index;
        m_descriptiveDistance = desDist;
        m_targetDistance = tarDist;
    }
    
    public NearestNeighbour(int index, double desDist, double tarDist, double[] perTarDist) {
        m_indexInDataSet = index;
        m_descriptiveDistance = desDist;
        m_targetDistance = tarDist;
        m_perTargetDistances = new double[perTarDist.length];
        m_perTargetDistances = Arrays.copyOf(perTarDist, perTarDist.length);
    }


    public String toString() {
        return String.format("(%d; %.4f; %.4f)", m_indexInDataSet, m_descriptiveDistance, m_targetDistance);
    }
    
    public int getIndexInDataset(){
    	return m_indexInDataSet;
    }
    
    public double getTargetDistance(){
    	return m_targetDistance;
    }
    
    public double getPerTargetDistance(int i){
    	return m_perTargetDistances[i];
    }

}
