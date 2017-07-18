
package si.ijs.kt.clus.main.settings;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsAttribute implements ISettings {

    /***********************************************************************
     * Section: Attribute *
     ***********************************************************************/

    protected INIFileString m_Target;
    protected INIFileString m_Clustering;
    protected INIFileString m_Descriptive;
    protected INIFileString m_Key;
    protected INIFileString m_Disabled;
    protected INIFileNominalOrDoubleOrVector m_Weights;
    protected INIFileNominalOrDoubleOrVector m_ClusteringWeights;
    protected INIFileBool m_ReduceMemoryNominal;
    protected INIFileString m_GIS;


    public String getGIS() {
        return m_GIS.getValue();
    }


    public void setGIS(String str) {
        m_GIS.setValue(str);
    }

    public boolean isNullGIS(){
        return StringUtils.unCaseCompare(m_GIS.getValue(), NONE);
    }

    public String getTarget() {
        return m_Target.getValue();
    }


    public void setTarget(String str) {
        m_Target.setValue(str);
    }


    public boolean isNullTarget() {
        return StringUtils.unCaseCompare(m_Target.getValue(), NONE);
    }


    public boolean isDefaultTarget() {
        return StringUtils.unCaseCompare(m_Target.getValue(), DEFAULT);
    }


    public String getClustering() {
        return m_Clustering.getValue();
    }


    public void setClustering(String str) {
        m_Clustering.setValue(str);
    }


    public String getDescriptive() {
        return m_Descriptive.getValue();
    }


    public void setDescriptive(String str) {
        m_Descriptive.setValue(str);
    }


    public String getKey() {
        return m_Key.getValue();
    }


    public String getDisabled() {
        return m_Disabled.getValue();
    }


    public void setDisabled(String str) {
        m_Disabled.setValue(str);
    }


    public INIFileNominalOrDoubleOrVector getNormalizationWeights() {
        return m_Weights;
    }


    public boolean hasNonTrivialWeights() {
        for (int i = 0; i < m_Weights.getVectorLength(); i++) {
            if (m_Weights.isNominal(i))
                return true;
            else if (m_Weights.getDouble(i) != 1.0)
                return true;
        }
        return false;
    }


    public INIFileNominalOrDoubleOrVector getClusteringWeights() {
        return m_ClusteringWeights;
    }


    public boolean getReduceMemoryNominalAttrs() {
        return m_ReduceMemoryNominal.getValue();
    }

    /***********************************************************************
     * Section: Attribute - Normalization *
     ***********************************************************************/

    public final static String[] NORMALIZATIONS = { "Normalize" };
    public final static int NORMALIZATION_DEFAULT = 0;

    /***********************************************************************
     * Section: Attribute - Target weights *
     ***********************************************************************/

    public final static String[] NUM_NOM_TAR_NTAR_WEIGHTS = { "TargetWeight", "NonTargetWeight", "NumericWeight", "NominalWeight" };

    public final static int TARGET_WEIGHT = 0;
    public final static int NON_TARGET_WEIGHT = 1;
    public final static int NUMERIC_WEIGHT = 2;
    public final static int NOMINAL_WEIGHT = 3;


    @Override
    public INIFileSection create() {
        INIFileSection attrs = new INIFileSection("Attributes");
        attrs.addNode(m_Target = new INIFileString("Target", DEFAULT));
        attrs.addNode(m_Clustering = new INIFileString("Clustering", DEFAULT));
        attrs.addNode(m_Descriptive = new INIFileString("Descriptive", DEFAULT));
        attrs.addNode(m_Key = new INIFileString("Key", NONE));
        attrs.addNode(m_Disabled = new INIFileString("Disable", NONE));
        attrs.addNode(m_Weights = new INIFileNominalOrDoubleOrVector("Weights", NORMALIZATIONS));
        m_Weights.setNominal(NORMALIZATION_DEFAULT);
        attrs.addNode(m_ClusteringWeights = new INIFileNominalOrDoubleOrVector("ClusteringWeights", EMPTY));
        m_ClusteringWeights.setDouble(1.0);
        m_ClusteringWeights.setArrayIndexNames(NUM_NOM_TAR_NTAR_WEIGHTS);
        attrs.addNode(m_ReduceMemoryNominal = new INIFileBool("ReduceMemoryNominalAttrs", false));
        attrs.addNode(m_GIS = new INIFileString("GIS", NONE));

        return attrs;
    }

}
