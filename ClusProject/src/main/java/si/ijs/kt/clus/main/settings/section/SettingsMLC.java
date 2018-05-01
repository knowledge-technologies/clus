
package si.ijs.kt.clus.main.settings.section;

import java.util.Arrays;
import java.util.List;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnumList;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsMLC extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    SettingsHMLC m_SettHMLC;
    SettingsRelief m_SettRelief;


    public SettingsMLC(int position, SettingsHMLC settHMLC, SettingsRelief settRelief) {
        super(position);

        m_SettHMLC = settHMLC;
        m_SettRelief = settRelief;
    }

    /***********************************************************************
     * Section: Multi-label classification *
     ***********************************************************************/

    INIFileSection m_SectionMultiLabel;
    protected INIFileNominalOrDoubleOrVector m_MultiLabelThreshold;
    protected INIFileEnum<MultiLabelThresholdOptimization> m_MultiLabelOptimizeThreshold;
    protected INIFileEnumList<MultiLabelMeasures> m_MultiLabelRankingMeasure;

    public enum MultiLabelThresholdOptimization {
        Yes, No
    };

    public enum MultiLabelMeasures {
        /* Example-based measures */
        HammingLoss, MLAccuracy, MLPrecision, MLRecall, MLFOne, SubsetAccuracy,

        /* Label-based measures */
        MacroPrecision, MacroRecall, MacroFOne, MicroPrecision, MicroRecall, MicroFOne,

        /* Ranking based measures */
        OneError, Coverage, RankingLoss, AveragePrecision,

        /* ROC- and PR-curves based measures */
        AverageAUROC, AverageAUPRC, WeightedAverageAUPRC, PooledAUPRC,

        /* All errors */
        All
    };


    public void setSectionMultiLabelEnabled(boolean enable) {
        m_SectionMultiLabel.setEnabled(enable);
    }


    public INIFileSection getSectionMultiLabel() {
        return m_SectionMultiLabel;
    }


    public INIFileNominalOrDoubleOrVector getMultiLabelThreshold() {
        return m_MultiLabelThreshold;
    }


    public List<MultiLabelMeasures> getMultiLabelRankingMeasures() {
        return m_MultiLabelRankingMeasure.getValue();
    }


    public void setToAllMultiLabelRankingMeasures() {
        // consider all measures
        List<MultiLabelMeasures> vals = Arrays.asList(MultiLabelMeasures.values());
        m_MultiLabelRankingMeasure.setValue(vals);
    }


    public MultiLabelThresholdOptimization getMultiLabelThresholdOptimization() {
        return m_MultiLabelOptimizeThreshold.getValue();
    }


    public boolean shouldRunThresholdOptimization() {
        // settings = mgr.getSettings
        if (getSectionMultiLabel().isEnabled() || m_SettHMLC.isSectionHierarchicalEnabled()) { // MLC or HMLC
            return getMultiLabelThresholdOptimization().equals(MultiLabelThresholdOptimization.Yes);
        }
        else {
            return false;
        }
    }


    public boolean shouldShowThresholds() {
        if (m_SettRelief.isRelief()) {
            return false;
        }
        else if (shouldRunThresholdOptimization()) {
            return true;
        }
        else if (m_SectionMultiLabel.isEnabled()) {
            return true;
        }
        else {
            return false;
        }
    }


    public boolean shouldShowThresholds(String modelName) {
        if (modelName.equals("Default") || modelName.equals("Original") || modelName.equals("Pruned")) {
            return true;
        }
        else if (modelName.startsWith("Forest with ") && !modelName.contains("trees(T = ")) {
            return true;
        }
        else {
            return false;
        }
    }


    @Override
    public INIFileSection create() {

        m_SectionMultiLabel = new INIFileSection("MultiLabel");
        m_SectionMultiLabel.addNode(m_MultiLabelThreshold = new INIFileNominalOrDoubleOrVector("MLCThreshold", NONELIST));
        m_MultiLabelThreshold.setDouble(0.5);
        m_SectionMultiLabel.addNode(m_MultiLabelOptimizeThreshold = new INIFileEnum<>("OptimizeThresholds", MultiLabelThresholdOptimization.No));
        m_SectionMultiLabel.addNode(m_MultiLabelRankingMeasure = new INIFileEnumList<>("MultiLabelRankingMeasure", MultiLabelMeasures.HammingLoss));

        return m_SectionMultiLabel;
    }
}
