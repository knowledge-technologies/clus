
package si.ijs.kt.clus.main.settings;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsMLC implements SettingsBase {

    SettingsHMLC m_SettHMLC;
    SettingsRelief m_SettRelief;

    public SettingsMLC(SettingsHMLC settHMLC, SettingsRelief settRelief) {
        m_SettHMLC = settHMLC;
        m_SettRelief = settRelief;
    }

    /***********************************************************************
     * Section: Multi-label classification *
     ***********************************************************************/

    INIFileSection m_SectionMultiLabel;
    protected INIFileNominalOrDoubleOrVector m_MultiLabelThreshold;
    protected INIFileNominal m_MultiLabelOptimizeThreshold;
    protected INIFileNominalOrIntOrVector m_MultiLabelRankingMeasure;

    public final static String[] MULTILABEL_THRESHOLD_OPTIMIZATION = { "Yes", "No" };
    public final static int MULTILABEL_THRESHOLD_OPTIMIZATION_YES = 0;
    public final static int MULTILABEL_THRESHOLD_OPTIMIZATION_NO = 1;

    public final static String[] MULTILABEL_MEASURES = { "HammingLoss", "MLAccuracy", "MLPrecision", "MLRecall", "MLFOne", "SubsetAccuracy", // Example
            // based
            // measures
            "MacroPrecision", "MacroRecall", "MacroFOne", "MicroPrecision", "MicroRecall", "MicroFOne", // Label based
            // measures
            "OneError", "Coverage", "RankingLoss", "AveragePrecision", // Ranking based measures
            "AverageAUROC", "AverageAUPRC", "WeightedAverageAUPRC", "PooledAUPRC", // ROC- and PR-curves based measures
            "all" }; // all previous errors: must be the last!
    public final static int MULTILABEL_MEASURES_HAMMINGLOSS = 0;
    public final static int MULTILABEL_MEASURES_MLACCURACY = 1;
    public final static int MULTILABEL_MEASURES_MLPRECISION = 2;
    public final static int MULTILABEL_MEASURES_MLRECALL = 3;
    public final static int MULTILABEL_MEASURES_MLFONE = 4;
    public final static int MULTILABEL_MEASURES_SUBSETACCURACY = 5;

    public final static int MULTILABEL_MEASURES_MACROPRECISION = 6;
    public final static int MULTILABEL_MEASURES_MACRORECALL = 7;
    public final static int MULTILABEL_MEASURES_MACROFONE = 8;
    public final static int MULTILABEL_MEASURES_MICROPRECISION = 9;
    public final static int MULTILABEL_MEASURES_MICRORECALL = 10;
    public final static int MULTILABEL_MEASURES_MICROFONE = 11;

    public final static int MULTILABEL_MEASURES_ONEERROR = 12;
    public final static int MULTILABEL_MEASURES_COVERAGE = 13;
    public final static int MULTILABEL_MEASURES_RANKINGLOSS = 14;
    public final static int MULTILABEL_MEASURES_AVERAGEPRECISION = 15;

    public final static int MULTILABEL_MEASURES_AUROC = 16;
    public final static int MULTILABEL_MEASURES_AUPRC = 17;
    public final static int MULTILABEL_MEASURES_WEIGHTED_AUPRC = 18;
    public final static int MULTILABEL_MEASURES_POOLED_AUPRC = 19;

    public final static int MULTILABEL_MEASURES_ALL = MULTILABEL_MEASURES.length - 1; // must be the last in
                                                                                      // MULTILABEL_MEASURES


    public void setSectionMultiLabelEnabled(boolean enable) {
        m_SectionMultiLabel.setEnabled(enable);
    }


    public INIFileSection getSectionMultiLabel() {
        return m_SectionMultiLabel;
    }


    public INIFileNominalOrDoubleOrVector getMultiLabelThreshold() {
        return m_MultiLabelThreshold;
    }


    public int[] getMultiLabelRankingMeasures() {
        return m_MultiLabelRankingMeasure.getNominalVector();
    }


    public void setToAllMultiLabelRankingMeasures() {
        m_MultiLabelRankingMeasure.setVector(MULTILABEL_MEASURES_ALL);
        for (int measure = 0; measure < MULTILABEL_MEASURES_ALL; measure++) {
            m_MultiLabelRankingMeasure.setNominal(measure, measure);
        }
    }


    public int getMultiLabelThresholdOptimization() {
        return m_MultiLabelOptimizeThreshold.getValue();
    }


    public boolean shouldRunThresholdOptimization() {
        // settings = mgr.getSettings
        if (getSectionMultiLabel().isEnabled() || m_SettHMLC.isSectionHierarchicalEnabled()) { // MLC or HMLC
            return getMultiLabelThresholdOptimization() == MULTILABEL_THRESHOLD_OPTIMIZATION_YES;
        }
        else {
            return false;
        }
    }

    public boolean shouldShowThresholds(){
        if(m_SettRelief.isRelief()){
            return false;
        } else if (shouldRunThresholdOptimization()){
            return true;
        } else if (m_SectionMultiLabel.isEnabled()){
            return true;
        } else {
            return false;
        }
    }
    
    public boolean shouldShowThresholds(String modelName){
        if (modelName.equals("Default") || modelName.equals("Original") || modelName.equals("Pruned")){
            return true;
        } else if (modelName.startsWith("Forest with ") && !modelName.contains("trees(T = ")){
            return true;
        } else{
            return false;
        }
    }

    @Override
    public INIFileSection create() {

        m_SectionMultiLabel = new INIFileSection("MultiLabel");
        m_SectionMultiLabel.addNode(m_MultiLabelThreshold = new INIFileNominalOrDoubleOrVector("MLCThreshold", NONELIST));
        m_MultiLabelThreshold.setDouble(0.5);
        m_SectionMultiLabel.addNode(m_MultiLabelOptimizeThreshold = new INIFileNominal("OptimizeThresholds", MULTILABEL_THRESHOLD_OPTIMIZATION, MULTILABEL_THRESHOLD_OPTIMIZATION_YES));
        m_SectionMultiLabel.addNode(m_MultiLabelRankingMeasure = new INIFileNominalOrIntOrVector("MultiLabelRankingMeasure", MULTILABEL_MEASURES));
        m_MultiLabelRankingMeasure.setNominal(MULTILABEL_MEASURES_HAMMINGLOSS);

        return m_SectionMultiLabel;
    }

}
