package si.ijs.kt.clus.distance.primitive.relief;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.main.settings.section.SettingsMLC;
import si.ijs.kt.clus.util.jeans.math.MathUtil;

public class MultiLabelDistance {
	// If new types are introduced use the same values as in SettingsMLC class
	private static final int HAMMING_LOSS = SettingsMLC.MULTILABEL_MEASURES_HAMMINGLOSS;
	private static final int ACCURACY = SettingsMLC.MULTILABEL_MEASURES_MLACCURACY;
	private static final int F1 = SettingsMLC.MULTILABEL_MEASURES_MLFONE;
	private static final int SUBSET_ACCURACY = SettingsMLC.MULTILABEL_MEASURES_SUBSETACCURACY;
	
	private final int m_DistanceType;
	private NominalAttrType[] m_Labels;
	
	public MultiLabelDistance(int type, ClusAttrType[] attrs){
		m_DistanceType = type;
		m_Labels = new NominalAttrType[attrs.length];
		for(int i = 0; i < attrs.length; i++){
			m_Labels[i] = (NominalAttrType) attrs[i];
		}
		
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
		case HAMMING_LOSS:
			dist = hamming_loss(t1, t2);
			break;
		case ACCURACY:
			dist = accuracy(t1, t2);
			break;
		case F1:
			dist = F1(t1, t2);
			break;
		case SUBSET_ACCURACY:
			dist = subsetAccuracy(t1, t2);			
		default:
			throw new RuntimeException("Unknown distance type");
		}
		return dist;
	}
	
    private double correctedAttributeValue(NominalAttrType attr, DataTuple t) {
    	return attr.isMissing(t) ? 1.0 / attr.m_NbValues :  attr.getNominal(t);
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
            int value1 = attr.getNominal(t1);
            int value2 = attr.getNominal(t2);
            if(attr.isMissing(t1) || attr.isMissing(t2)){
            	dist +=  1.0 - 1.0 / attr.m_NbValues;  // mimics P(different values)
            }
            else{
            	dist += value1 == value2 ? 0.0 : 1.0;
            }
        }
        return dist / m_Labels.length;
    }
	
    /**
     * Computes |labels(t1) intersection labels(t2)| / |labels(t1) union labels(t2)|.
     * @param t1
     * @param t2
     * @return
     */
    private double accuracy(DataTuple t1, DataTuple t2){
    	NominalAttrType attr;
    	double cap = 0.0;  // size of intersection
    	double cup = 0.0;  // size of union
    	for(int i = 0; i < m_Labels.length; i++){
    		attr = m_Labels[i];
    		double value1 = correctedAttributeValue(attr, t1);
    		double value2 = correctedAttributeValue(attr, t2);
            
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
    		double value1 = correctedAttributeValue(attr, t1);
    		double value2 = correctedAttributeValue(attr, t2);
            
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
    		double factor = correctedAttributeValue(attr, t1) * correctedAttributeValue(attr, t2);
    		if (factor < MathUtil.C1E_9) {
    			return 0.0;
    		}
    		pEqualSets *= factor;
    	}
    	return pEqualSets;
    }
}
