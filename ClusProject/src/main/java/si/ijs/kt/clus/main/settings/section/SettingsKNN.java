
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsKNN extends SettingsBase {

    public SettingsKNN(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }

    /***********************************************************************
     * Section: KNN *
     ***********************************************************************/

    private INIFileSection m_SectionKNN;
    
    private final static int[] DEFAULT_K = new int[] {1, 3};
    private INIFileNominalOrIntOrVector m_k;
    
    public enum Distance {Euclidean, Chebyshev, Manhattan};
    private INIFileEnum<Distance> m_Distance;
    
    public enum SearchMethod {BrutForce, VPTree, KDTree};
    private INIFileEnum<SearchMethod> m_SearchMethod;	
    
    public final static String[] DISTANCE_WEIGHTS = new String[]{"constant", "1/d", "1-d"};
    public static final int DISTANCE_WEIGHTING_CONSTANT = 0;
    public static final int DISTANCE_WEIGHTING_INVERSE = 1; // d^{-1}
    public static final int DISTANCE_WEIGHTING_MINUS = 2;   // 1-d
    private INIFileNominalOrIntOrVector m_distanceWeight;
    
    private INIFileString m_attributeWeight; // parsed in KnnModel for its very diverse uses


    public int[] getKNNk() {
        return m_k.getIntVector();
    }

    public Distance getDistance() {
        return m_Distance.getChosenOption();
    }

    public int[] getKNNDistanceWeight() {
        return m_distanceWeight.getNominalVector();
    }

    public String getKNNAttrWeight() {
        return m_attributeWeight.getValue();
    }
    
    public void setKNNAttrWeight(String val) {
        m_attributeWeight.setValue(val);
    }

    public void setSectionKNNEnabled(boolean enable) {
        m_SectionKNN.setEnabled(enable);
    }


    public boolean isKNN() {
        return m_SectionKNN.isEnabled();
    }

    
    public SearchMethod getSearchMethod() {
    	return m_SearchMethod.getChosenOption();
    }

   


    @Override
    public INIFileSection create() {
        m_SectionKNN = new INIFileSection("kNN");
        m_SectionKNN.addNode(m_k = new INIFileNominalOrIntOrVector("K", NONELIST));
        m_k.setIntVector(DEFAULT_K);
        
        m_SectionKNN.addNode(m_Distance = new INIFileEnum<Distance>("Distance", Distance.class, Distance.Euclidean));
        //m_SectionKNN.addNode(m_searchMethod = new INIFileNominal("SearchMethod", SEARCH_METHODS, SEARCH_METHOD_BRUTE_FORCE));
        m_SectionKNN.addNode(m_SearchMethod =  new INIFileEnum<SearchMethod>("SearchMethod", SearchMethod.class, SearchMethod.BrutForce));
        m_SectionKNN.addNode(m_distanceWeight = new INIFileNominalOrIntOrVector("DistanceWeighting", DISTANCE_WEIGHTS));        
        m_distanceWeight.setNominal(DISTANCE_WEIGHTING_CONSTANT);
        
        m_SectionKNN.addNode(m_attributeWeight = new INIFileString("AttributeWeighting", "none"));
        m_SectionKNN.setEnabled(false);

        return m_SectionKNN;

    }

    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub
        
    }
}
