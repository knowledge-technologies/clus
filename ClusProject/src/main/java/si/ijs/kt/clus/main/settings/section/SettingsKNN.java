
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsKNN extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsKNN(int position) {
        super(position);
    }

    /***********************************************************************
     * Section: KNN *
     ***********************************************************************/

    private INIFileSection m_SectionKNN;

    private final static int[] DEFAULT_K = new int[] { 1, 3 };
    private INIFileNominalOrIntOrVector m_k;

    private final static String[] DISTANCES = new String[] { "euclidean", "chebyshev", "manhattan" };
    public final static int DISTANCE_EUCLIDEAN = 0;
    public final static int DISTANCE_CHEBYSHEV = 1;
    public final static int DISTANCE_MANHATTAN = 2;
    private INIFileNominal m_distance;

    private final static String[] SEARCH_METHODS = new String[] { "BrutForce", "VP-tree", "KD-tree" };
    public final static int SEARCH_METHOD_BRUTE_FORCE = 0;
    public final static int SEARCH_METHOD_VP_TREE = 1;
    public final static int SEARCH_METHOD_KD_TREE = 2;
    private INIFileNominal m_searchMethod;

    public final static String[] DISTANCE_WEIGHTS = new String[] { "constant", "1/d", "1-d" };
    public static final int DISTANCE_WEIGHTING_CONSTANT = 0;
    public static final int DISTANCE_WEIGHTING_INVERSE = 1; // d^{-1}
    public static final int DISTANCE_WEIGHTING_MINUS = 2; // 1-d
    private INIFileNominalOrIntOrVector m_distanceWeight;

    private INIFileString m_attributeWeight; // parsed in KnnModel for its very diverse uses


    public int[] getKNNk() {
        return m_k.getIntVector();
    }


    public int getKNNDistance() {
        return m_distance.getValue();
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


    public int getKNNMethod() {
        return m_searchMethod.getValue();
    }


    @Override
    public INIFileSection create() {
        m_SectionKNN = new INIFileSection("kNN");
        m_SectionKNN.addNode(m_k = new INIFileNominalOrIntOrVector("K", NONELIST));
        m_k.setIntVector(DEFAULT_K);

        m_SectionKNN.addNode(m_distance = new INIFileNominal("Distance", DISTANCES, DISTANCE_EUCLIDEAN));
        m_SectionKNN.addNode(m_searchMethod = new INIFileNominal("SearchMethod", SEARCH_METHODS, SEARCH_METHOD_BRUTE_FORCE));
        m_SectionKNN.addNode(m_distanceWeight = new INIFileNominalOrIntOrVector("DistanceWeighting", DISTANCE_WEIGHTS));
        m_distanceWeight.setNominal(DISTANCE_WEIGHTING_CONSTANT);

        m_SectionKNN.addNode(m_attributeWeight = new INIFileString("AttributeWeighting", "none"));
        m_SectionKNN.setEnabled(false);

        return m_SectionKNN;

    }
}
