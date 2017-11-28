
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.ext.hierarchical.ClassesValue;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsHMLC extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsHMLC(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }

    /***********************************************************************
     * Section: Hierarchical multi-label classification *
     ***********************************************************************/

    // Hierarchical multi-classification now supports both trees and DAGS
    // This was required because Gene Ontology terms are organized in a partial order
    public final static String[] HIERTYPES = { "Tree", "DAG" };

    public final static int HIERTYPE_TREE = 0;
    public final static int HIERTYPE_DAG = 1;

    public final static String[] HIERWEIGHT = { "ExpSumParentWeight", "ExpAvgParentWeight", "ExpMinParentWeight", "ExpMaxParentWeight", "NoWeight" };

    public final static int HIERWEIGHT_EXP_SUM_PARENT_WEIGHT = 0;
    public final static int HIERWEIGHT_EXP_AVG_PARENT_WEIGHT = 1;
    public final static int HIERWEIGHT_EXP_MIN_PARENT_WEIGHT = 2;
    public final static int HIERWEIGHT_EXP_MAX_PARENT_WEIGHT = 3;
    public final static int HIERWEIGHT_NO_WEIGHT = 4;

    public final static String[] HIERDIST = { "WeightedEuclidean", "Jaccard", "NoDistance" };

    public final static int HIERDIST_WEIGHTED_EUCLIDEAN = 0;
    public final static int HIERDIST_JACCARD = 1;
    public final static int HIERDIST_NO_DIST = 2; // for poolAUPRC case

    public final static String[] HIERMEASURES = { "AverageAUROC", "AverageAUPRC", "WeightedAverageAUPRC", "PooledAUPRC" };

    public final static int HIERMEASURE_AUROC = 0;
    public final static int HIERMEASURE_AUPRC = 1;
    public final static int HIERMEASURE_WEIGHTED_AUPRC = 2;
    public final static int HIERMEASURE_POOLED_AUPRC = 3;

    INIFileSection m_SectionHierarchical;
    protected INIFileNominal m_HierType;
    protected INIFileNominal m_HierWType;
    protected INIFileNominal m_HierDistance;
    protected INIFileDouble m_HierWParam;
    protected INIFileString m_HierSep;
    protected INIFileString m_HierEmptySetIndicator;
    protected INIFileNominal m_HierOptimizeErrorMeasure;
    protected INIFileString m_DefinitionFile;
    protected INIFileBool m_HierNoRootPreds;
    protected INIFileBool m_HierSingleLabel;
    protected INIFileBool m_CalErr;
    protected INIFileDouble m_HierPruneInSig;
    protected INIFileBool m_HierUseBonferroni;
    protected INIFileNominalOrDoubleOrVector m_HierClassThreshold;
    protected INIFileNominalOrDoubleOrVector m_RecallValues;
    protected INIFileString m_HierEvalClasses;
    protected static INIFileBool m_HierUseMEstimate;

    /** Denotes if Clustering attributes contain Hierarchy as well as numeric or nominal attributes */
    protected boolean isHierAndClassAndReg = false;


    public void setIsHierAndClassAndReg(boolean value) {
        isHierAndClassAndReg = value;
    }


    /**
     * Do Clustering attributes contain Hierarchy as well as numeric or nominal attributes?
     * 
     * @return
     */
    public boolean isHierAndClassAndReg() {
        return isHierAndClassAndReg;
    }


    public void setSectionHierarchicalEnabled(boolean enable) {
        m_SectionHierarchical.setEnabled(enable);
    }


    public boolean isSectionHierarchicalEnabled() {
        return m_SectionHierarchical.isEnabled();
    }


    public boolean getHierSingleLabel() {
        return m_HierSingleLabel.getValue();
    }


    public int getHierType() {
        return m_HierType.getValue();
    }


    public int getHierDistance() {
        return m_HierDistance.getValue();
    }


    public int getHierWType() {
        return m_HierWType.getValue();
    }


    public double getHierWParam() {
        return m_HierWParam.getValue();
    }


    public boolean isCalError() {
        return m_CalErr.getValue();
    }


    public INIFileNominalOrDoubleOrVector getClassificationThresholds() {
        return m_HierClassThreshold;
    }


    public INIFileNominalOrDoubleOrVector getRecallValues() {
        return m_RecallValues;
    }


    public boolean isHierNoRootPreds() {
        return m_HierNoRootPreds.getValue();
    }


    public boolean isUseBonferroni() {
        return m_HierUseBonferroni.getValue();
    }


    public double getHierPruneInSig() {
        return m_HierPruneInSig.getValue();
    }


    public boolean hasHierEvalClasses() {
        return !StringUtils.unCaseCompare(m_HierEvalClasses.getValue(), NONE);
    }


    public String getHierEvalClasses() {
        return m_HierEvalClasses.getValue();
    }


    public int getHierOptimizeErrorMeasure() {
        return m_HierOptimizeErrorMeasure.getValue();
    }


    public boolean hasDefinitionFile() {
        return !StringUtils.unCaseCompare(m_DefinitionFile.getValue(), NONE);
    }


    public String getDefinitionFile() {
        return m_DefinitionFile.getValue();
    }


    public void initHierarchical() {
        ClassesValue.setHSeparator(m_HierSep.getValue());
        ClassesValue.setEmptySetIndicator(m_HierEmptySetIndicator.getValue());
    }


    public boolean useMEstimate() {
        return m_HierUseMEstimate.getValue();
    }


    @Override
    public INIFileSection create() {

        m_SectionHierarchical = new INIFileSection("Hierarchical");
        m_SectionHierarchical.addNode(m_HierType = new INIFileNominal("Type", HIERTYPES, 0));
        m_SectionHierarchical.addNode(m_HierDistance = new INIFileNominal("Distance", HIERDIST, 0));
        m_SectionHierarchical.addNode(m_HierWType = new INIFileNominal("WType", HIERWEIGHT, 0));
        m_SectionHierarchical.addNode(m_HierWParam = new INIFileDouble("WParam", 0.75));
        m_SectionHierarchical.addNode(m_HierSep = new INIFileString("HSeparator", "."));
        m_SectionHierarchical.addNode(m_HierEmptySetIndicator = new INIFileString("EmptySetIndicator", "n"));
        m_SectionHierarchical.addNode(m_HierOptimizeErrorMeasure = new INIFileNominal("OptimizeErrorMeasure", HIERMEASURES, HIERMEASURE_POOLED_AUPRC));
        m_SectionHierarchical.addNode(m_DefinitionFile = new INIFileString("DefinitionFile", NONE));
        m_SectionHierarchical.addNode(m_HierNoRootPreds = new INIFileBool("NoRootPredictions", false));
        m_SectionHierarchical.addNode(m_HierPruneInSig = new INIFileDouble("PruneInSig", 0.0));
        m_SectionHierarchical.addNode(m_HierUseBonferroni = new INIFileBool("Bonferroni", false));
        m_SectionHierarchical.addNode(m_HierSingleLabel = new INIFileBool("SingleLabel", false));
        m_SectionHierarchical.addNode(m_CalErr = new INIFileBool("CalculateErrors", true));
        m_SectionHierarchical.addNode(m_HierClassThreshold = new INIFileNominalOrDoubleOrVector("ClassificationThreshold", NONELIST));
        m_HierClassThreshold.setNominal(0);
        m_SectionHierarchical.addNode(m_RecallValues = new INIFileNominalOrDoubleOrVector("RecallValues", NONELIST));
        m_RecallValues.setNominal(0);
        m_SectionHierarchical.addNode(m_HierEvalClasses = new INIFileString("EvalClasses", NONE));
        m_SectionHierarchical.addNode(m_HierUseMEstimate = new INIFileBool("MEstimate", false));
        m_SectionHierarchical.setEnabled(false);

        return m_SectionHierarchical;
    }


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub

    }

}
