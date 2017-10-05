
package si.ijs.kt.clus.ext.featureRanking.relief;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NearestNeighbour {

    private final int m_indexInDataSet;
    private double m_descriptiveDistance = Double.NaN;
    private double m_targetDistance = Double.NaN;

    /**
     * Construct nearest neighbour from the three parameters that define it.
     * @param index
     * @param desDist
     * @param tarDist
     */
    public NearestNeighbour(int index, double desDist, double tarDist) {
        m_indexInDataSet = index;
        m_descriptiveDistance = desDist;
        m_targetDistance = tarDist;
    }
    
    /**
     * Construct nearest neighbour from a string in a file.
     * @param nnFileString e.g., NN(21;2.21;1.2E-1) or NN(0;2.22;0.21)
     */
    public NearestNeighbour(String nnFileString) {
    	Pattern nnPatern = Pattern.compile("NN[(]([^;]+);([^;]+);([^)]+)[)]");
    	Matcher nnParsed = nnPatern.matcher(nnFileString);
    	if(nnParsed.find()) {
    		m_indexInDataSet = Integer.parseInt(nnParsed.group(1));
    		m_descriptiveDistance = Double.parseDouble(nnParsed.group(2));
    		m_targetDistance = Double.parseDouble(nnParsed.group(3));
    	} else {
    		throw new RuntimeException(String.format("Nearest neighbour %s could not be parsed.", nnFileString));
    	}
    }
    
    public String toFileString() {
    	return String.format("NN(%d;%f;%f)", m_indexInDataSet, m_descriptiveDistance, m_targetDistance);
    }
    
    @Override
    public String toString() {
        //return String.format("(ind: %d; descr. dist: %.4f; tar. dist: %.4f)", m_indexInDataSet, m_descriptiveDistance, m_targetDistance);
    	return String.format("NN(%d)", m_indexInDataSet);
    }
    
    public int getIndexInDataset(){
    	return m_indexInDataSet;
    }
    
    public double getDescriptiveDidstance() {
    	return m_descriptiveDistance;
    }
    
    public double getTargetDistance(){
    	return m_targetDistance;
    }
    
    @Override
    public boolean equals(Object other){
    	if(other instanceof NearestNeighbour){
    		return this.m_indexInDataSet == ((NearestNeighbour) other).m_indexInDataSet;
    	}
    	return false;
    }
    
    
    
    
    

}
