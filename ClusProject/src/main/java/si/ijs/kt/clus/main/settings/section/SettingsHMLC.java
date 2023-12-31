
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.ext.hierarchical.ClassesValue;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsHMLC extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsHMLC(int position) {
        super(position, "Hierarchical");
    }

    /***********************************************************************
     * Section: Hierarchical multi-label classification *
     ***********************************************************************/

    // Hierarchical multi-classification now supports both trees and DAGS
    // This was required because Gene Ontology terms are organized in a partial order
    public enum HierarchyType {
        Tree, DAG, Unknown
    };

    public enum HierarchyWeight {
        NoWeight, ExpSumParentWeight, ExpAvgParentWeight, ExpMinParentWeight, ExpMaxParentWeight
    };

    public enum HierarchyDistance {
        WeightedEuclidean, Jaccard, PooledAUPRC
    };

    public enum HierarchyMeasures {
        AverageAUROC, AverageAUPRC, WeightedAverageAUPRC, PooledAUPRC, Undefined
    };

    protected INIFileEnum<HierarchyType> m_HierType;
    protected INIFileEnum<HierarchyWeight> m_HierWType;
    protected INIFileEnum<HierarchyDistance> m_HierDistance;
    protected INIFileDouble m_HierWParam;
    protected INIFileString m_HierSep;
    protected INIFileString m_HierEmptySetIndicator;
    protected INIFileEnum<HierarchyMeasures> m_HierOptimizeErrorMeasure;
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



    public void setSectionHierarchicalEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    public boolean isSectionHierarchicalEnabled() {
        return m_Section.isEnabled();
    }


    public boolean getHierSingleLabel() {
        return m_HierSingleLabel.getValue();
    }


    public HierarchyType getHierType() {
        return m_HierType.getValue();
    }


    public HierarchyDistance getHierDistance() {
        return m_HierDistance.getValue();
    }


    public HierarchyWeight getHierWType() {
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


    public HierarchyMeasures getHierOptimizeErrorMeasure() {
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
    public void create() {
        m_Section.addNode(m_HierType = new INIFileEnum<>("Type", HierarchyType.Unknown));
        m_Section.addNode(m_HierDistance = new INIFileEnum<HierarchyDistance>("Distance", HierarchyDistance.WeightedEuclidean));
        m_Section.addNode(m_HierWType = new INIFileEnum<>("WType", HierarchyWeight.ExpSumParentWeight));
        m_Section.addNode(m_HierWParam = new INIFileDouble("WParam", 0.75));
        m_Section.addNode(m_HierSep = new INIFileString("HSeparator", "."));
        m_Section.addNode(m_HierEmptySetIndicator = new INIFileString("EmptySetIndicator", "n"));
        m_Section.addNode(m_HierOptimizeErrorMeasure = new INIFileEnum<>("OptimizeErrorMeasure", HierarchyMeasures.PooledAUPRC));
        m_Section.addNode(m_DefinitionFile = new INIFileString("DefinitionFile", NONE));
        m_Section.addNode(m_HierNoRootPreds = new INIFileBool("NoRootPredictions", false));
        m_Section.addNode(m_HierPruneInSig = new INIFileDouble("PruneInSig", 0.0));
        m_Section.addNode(m_HierUseBonferroni = new INIFileBool("Bonferroni", false));
        m_Section.addNode(m_HierSingleLabel = new INIFileBool("SingleLabel", false));
        m_Section.addNode(m_CalErr = new INIFileBool("CalculateErrors", true));
        
        m_Section.addNode(m_HierClassThreshold = new INIFileNominalOrDoubleOrVector("ClassificationThreshold", NONELIST));
        m_HierClassThreshold.setNominal(0);
        
        m_Section.addNode(m_RecallValues = new INIFileNominalOrDoubleOrVector("RecallValues", NONELIST));
        m_RecallValues.setNominal(0);
        
        m_Section.addNode(m_HierEvalClasses = new INIFileString("EvalClasses", NONE));
        m_Section.addNode(m_HierUseMEstimate = new INIFileBool("MEstimate", false));
        m_Section.setEnabled(false);
    }
}
