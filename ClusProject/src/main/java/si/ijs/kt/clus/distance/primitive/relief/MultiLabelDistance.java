package si.ijs.kt.clus.distance.primitive.relief;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.main.settings.section.SettingsRelief.MultilabelDistance;
import si.ijs.kt.clus.util.jeans.math.MathUtil;

public class MultiLabelDistance {
		
	private MultilabelDistance m_DistanceType;
	private NominalAttrType[] m_Labels;
	private double[] m_LabelProbabilities;
	
	public static int m_RelevantLabelIndex = 0;
	
	public MultiLabelDistance(MultilabelDistance type, ClusAttrType[] attrs, double[] labelProbabilities){
		m_DistanceType = type;
		m_Labels = new NominalAttrType[attrs.length];
		for(int i = 0; i < attrs.length; i++){
			m_Labels[i] = (NominalAttrType) attrs[i];
		}
		m_LabelProbabilities = labelProbabilities;
	}
	
	/**
	 * Computes the distance between the sets of labels that belong to tuple one and two.
	 * The missing values are handled in Relief style, hence the definition of the distances
	 * may not follow the definition of addExample from the corresponding clus.errors class.
	 * @param t1
	 * @param t2
	 * @return
	 */
	public double calculateDist(DataTuple t1, DataTuple t2){
		double dist;
		switch(m_DistanceType){
		case HammingLoss:
			dist = hamming_loss(t1, t2);
			break;
		case MLAccuracy:
			dist = mlAccuracy(t1, t2);
			break;
		case MLFOne:
			dist = F1(t1, t2);
			break;
		case SubsetAccuracy:
			dist = subsetAccuracy(t1, t2);
			break;
		default:
			throw new RuntimeException("Unknown distance type");
		}
		return dist;
	}
	
    private double correctedAttributeValue(int attrInd, NominalAttrType attr, DataTuple t) {
    	return attr.isMissing(t) ? m_LabelProbabilities[attrInd] : 1.0 - attr.getNominal(t); // P(value) = 1/2 : index of value x in {1, 0} is 1 - x ...
    }
    
    private double areValuesEqualProbability(int attrInd, NominalAttrType attr, DataTuple t1, DataTuple t2) {
    	double p = m_LabelProbabilities[attrInd];
		double q = 1 - p;
    	if(attr.isMissing(t1) && attr.isMissing(t2)) {			
			return p * p + q * q;   		
    	} else if(attr.isMissing(t1)) {
    		return attr.getNominal(t2) == m_RelevantLabelIndex ? p : q; 
    	} else if(attr.isMissing(t2)) {
    		return attr.getNominal(t1) == m_RelevantLabelIndex ? p : q; 
    	} else {
    		return attr.getNominal(t1) == attr.getNominal(t2) ? 1.0 : 0.0;
    	}
    }

	/**
	 * Computes the Hamming loss distance between the label sets of data tuples t1 and t2, i.e.
	 * |labels(t1) symmetric labels(t2)| / |labels(t1) union labels(t2)|
	 * @param t1
	 * @param t2
	 * @return
	 */
    private double hamming_loss(DataTuple t1, DataTuple t2) {
    	NominalAttrType attr;
    	double dist = 0.0;
        for (int i = 0; i < m_Labels.length; i++) {
            attr = m_Labels[i];
            dist += 1.0 - areValuesEqualProbability(i, attr, t1, t2); // different
        }
        return dist / m_Labels.length;
    }
	
    /**
     * Computes |labels(t1) intersection labels(t2)| / |labels(t1) union labels(t2)|.
     * @param t1
     * @param t2
     * @return
     */
    private double mlAccuracy(DataTuple t1, DataTuple t2){
    	NominalAttrType attr;
    	double cap = 0.0;  // size of intersection
    	double cup = 0.0;  // size of union
    	for(int i = 0; i < m_Labels.length; i++){
    		attr = m_Labels[i];
    		double value1 = correctedAttributeValue(i, attr, t1);
    		double value2 = correctedAttributeValue(i, attr, t2);
            
            cap += value1 * value2;  // P(both present)
            cup += value1 + value2 - value1 * value2;  // P(at least one present)
    	}
    	double similarity = cup > MathUtil.C1E_9 ? cap / cup : 1.0;
    	return 1 - similarity;
    }
    
    
    /**
     * Computes 2 |labels(t1) intersection labels(t2)| / (|labels(t1)| + |labels(t2)|).
     * @param t1
     * @param t2
     * @return
     */
    private double F1(DataTuple t1, DataTuple t2) {
    	NominalAttrType attr;
    	double cap = 0.0;  // size of intersection
    	double first = 0.0, second = 0.0;  // size of first and second label set
    	for(int i = 0; i < m_Labels.length; i++){
    		attr = m_Labels[i];
    		double value1 = correctedAttributeValue(i, attr, t1);
    		double value2 = correctedAttributeValue(i, attr, t2);
            
            cap += value1 * value2;
            first += value1;
            second += value2;
    	}
    	double similarity = first + second > MathUtil.C1E_9 ? 2.0 * cap / (first + second) : 1.0;
    	return 1 - similarity;
    }
    
    /**
     * Computes the probability whether the label sets of two data tuples are the same.
     * If there are no missing values, either 0 or 1 is returned. 
     * @param t1
     * @param t2
     * @return
     */
    public double subsetAccuracy(DataTuple t1, DataTuple t2) {
    	NominalAttrType attr;
    	double pEqualSets = 1.0;
    	for(int i = 0; i < m_Labels.length; i++){
    		attr = m_Labels[i];       
    		double factor = areValuesEqualProbability(i, attr, t1, t2);
    		if (factor < MathUtil.C1E_9) {
    			pEqualSets = 0.0;
    			break;
    		}
    		pEqualSets *= factor;
    	}
    	return 1.0 - pEqualSets;
    }
    
    
    @Override
    public String toString() {
    	String fullClassName = this.getClass().toString();
    	int lastIndex = fullClassName.lastIndexOf(".") + 1;
    	return String.format("%s(%s)", fullClassName.substring(lastIndex), m_DistanceType.toString());
    }
    
    public String distanceName(){
        return m_DistanceType.toString();
    }
}
