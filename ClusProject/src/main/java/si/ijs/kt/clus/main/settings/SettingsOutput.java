
package si.ijs.kt.clus.main.settings;

import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsOutput implements ISettings {

    /***********************************************************************
     * Section: Output *
     ***********************************************************************/

    protected INIFileBool m_OutFoldErr;
    /** Print out data to .arff files for each fold, m_WritePredictions has to be given value to this to work */
    protected INIFileBool m_OutFoldData;
    protected INIFileBool m_OutFoldModels;
    protected INIFileBool m_OutTrainErr;
    protected INIFileBool m_OutValidErr;
    protected INIFileBool m_OutTestErr;

    protected INIFileBool m_ShowBrFreq;
    protected INIFileBool m_ShowUnknown;
    protected INIFileNominal m_ShowInfo;
    protected INIFileNominal m_ShowModels;
    protected INIFileBool m_PrintModelAndExamples;
    /** Write test/train predictions to files */
    protected INIFileNominal m_WritePredictions;
    protected INIFileBool m_GzipOutput; // added by Jurica Levatic JSI, July 2014
    protected INIFileBool m_WriteErrorFile;
    protected INIFileBool m_WriteModelFile;
    protected INIFileBool m_ModelIDFiles;
    protected INIFileBool m_OutputPythonModel;
    protected INIFileBool m_OutputJSONModel;
    protected INIFileBool m_OutputDatabaseQueries;
    protected INIFileBool m_WriteCurves;
    protected INIFileBool m_OutputClowdFlowsJSON;


    public boolean isOutTrainError() {
        return m_OutTrainErr.getValue();
    }


    public void setOutTrainError(boolean value) {
        m_OutTrainErr.setValue(value);
    }


    public boolean isOutValidError() {
        return m_OutValidErr.getValue();
    }


    public void setOutValidError(boolean value) {
        m_OutValidErr.setValue(value);
    }


    public boolean isOutTestError() {
        return m_OutTestErr.getValue();
    }


    public void setOutTestError(boolean value) {
        m_OutTestErr.setValue(value);
    }


    public boolean isShowBranchFreq() {
        return m_ShowBrFreq.getValue();
    }


    public boolean isShowUnknown() {
        return m_ShowUnknown.getValue();
    }


    public boolean isPrintModelAndExamples() {
        return m_PrintModelAndExamples.getValue();
    }


    public boolean isOutFoldError() {
        return m_OutFoldErr.getValue();
    }


    public boolean isOutFoldData() {
        return m_OutFoldData.getValue();
    }


    public boolean isOutputFoldModels() {
        return m_OutFoldModels.getValue();
    }


    public boolean isWriteTestSetPredictions() {
        return m_WritePredictions.contains(WRITE_PRED_TEST);
    }


    public boolean isWriteTrainSetPredictions() {
        return m_WritePredictions.contains(WRITE_PRED_TRAIN);
    }


    public boolean isWriteErrorFile() {
        return m_WriteErrorFile.getValue();
    }


    public boolean isWriteModelIDPredictions() {
        return m_ModelIDFiles.getValue();
    }


    public boolean isOutputPythonModel() {
        return m_OutputPythonModel.getValue();
    }


    public boolean isOutputJSONModel() {
        return m_OutputJSONModel.getValue();
    }


    public boolean isOutputClowdFlowsJSON() {
        return m_OutputClowdFlowsJSON.getValue();
    }


    public boolean isOutputDatabaseQueries() {
        return m_OutputDatabaseQueries.getValue();
    }


    public boolean isWriteCurves() {
        return m_WriteCurves.getValue();
    }


    public boolean isWriteModelFile() {
        return m_WriteModelFile.getValue();
    }


    public boolean getShowModel(int i) {
        return m_ShowModels.contains(i);
    }


    public boolean shouldShowModel(int model) {
        boolean others = getShowModel(SHOW_MODELS_OTHERS);
        if (model == ClusModel.DEFAULT && getShowModel(SHOW_MODELS_DEFAULT))
            return true;
        else if (model == ClusModel.ORIGINAL && getShowModel(SHOW_MODELS_ORIGINAL))
            return true;
        else if (model == ClusModel.PRUNED && (getShowModel(SHOW_MODELS_PRUNED) || others))
            return true;
        else if (others)
            return true;
        return false;
    }


    public StatisticPrintInfo getStatisticPrintInfo() {
        StatisticPrintInfo info = new StatisticPrintInfo();
        info.SHOW_EXAMPLE_COUNT = m_ShowInfo.contains(0);
        info.SHOW_EXAMPLE_COUNT_BYTARGET = m_ShowInfo.contains(1);
        info.SHOW_DISTRIBUTION = m_ShowInfo.contains(2);
        info.SHOW_INDEX = m_ShowInfo.contains(3);
        info.INTERNAL_DISTR = m_ShowInfo.contains(4);
        info.SHOW_KEY = m_ShowInfo.contains(5);
        return info;
    }

    /***********************************************************************
     * Section: Output - Show info in .out file *
     ***********************************************************************/

    public final static String[] SHOW_MODELS = { "Default", "Original", "Pruned", "Others" };

    public final static int[] SHOW_MODELS_VALUES = { 0, 2, 3 };
    public final static int SHOW_MODELS_DEFAULT = 0;
    public final static int SHOW_MODELS_ORIGINAL = 1;
    public final static int SHOW_MODELS_PRUNED = 2;
    public final static int SHOW_MODELS_OTHERS = 3;

    public final static String[] SHOW_INFO = { "Count", "CountByTarget", "Distribution", "Index", "NodePrototypes", "Key" };

    public final static int[] SHOW_INFO_VALUES = { 0 };

    public final static String[] CONVERT_RULES = { "No", "Leaves", "AllNodes" };

    public final static int CONVERT_RULES_NONE = 0;
    public final static int CONVERT_RULES_LEAVES = 1;
    public final static int CONVERT_RULES_ALLNODES = 2;

    public static boolean SHOW_UNKNOWN_FREQ;
    public static boolean SHOW_BRANCH_FREQ;

    /***********************************************************************
     * Section: Output - Write predictions to file *
     ***********************************************************************/

    public final static String[] WRITE_PRED = { "None", "Test", "Train" };

    public final static int[] WRITE_PRED_VALUES = { 0 };
    public final static int WRITE_PRED_NONE = 0;
    public final static int WRITE_PRED_TEST = 1;
    public final static int WRITE_PRED_TRAIN = 2;


    @Override
    public INIFileSection create() {

        INIFileSection output = new INIFileSection("Output");
        output.addNode(m_ShowModels = new INIFileNominal("ShowModels", SHOW_MODELS, SHOW_MODELS_VALUES));
        output.addNode(m_OutTrainErr = new INIFileBool("TrainErrors", true));
        output.addNode(m_OutValidErr = new INIFileBool("ValidErrors", true));
        output.addNode(m_OutTestErr = new INIFileBool("TestErrors", true));
        output.addNode(m_OutFoldModels = new INIFileBool("AllFoldModels", true));
        output.addNode(m_OutFoldErr = new INIFileBool("AllFoldErrors", false));
        output.addNode(m_OutFoldData = new INIFileBool("AllFoldDatasets", false));
        output.addNode(m_ShowUnknown = new INIFileBool("UnknownFrequency", false));
        output.addNode(m_ShowBrFreq = new INIFileBool("BranchFrequency", false));
        output.addNode(m_ShowInfo = new INIFileNominal("ShowInfo", SHOW_INFO, SHOW_INFO_VALUES));
        output.addNode(m_PrintModelAndExamples = new INIFileBool("PrintModelAndExamples", false));
        output.addNode(m_WriteErrorFile = new INIFileBool("WriteErrorFile", false));
        output.addNode(m_WriteModelFile = new INIFileBool("WriteModelFile", false));
        output.addNode(m_WritePredictions = new INIFileNominal("WritePredictions", WRITE_PRED, WRITE_PRED_VALUES));
        output.addNode(m_GzipOutput = new INIFileBool("GzipOutput", false));
        // If this option name is to be changed, it must also be changed in testsets/iris-classify.s
        // output.addNode(m_ModelIDFiles = new INIFileBool("WriteModelIDFiles", false));
        output.addNode(m_ModelIDFiles = new INIFileBool("ModelIDFiles", false));
        output.addNode(m_WriteCurves = new INIFileBool("WriteCurves", false));
        output.addNode(m_OutputPythonModel = new INIFileBool("OutputPythonModel", false));
        output.addNode(m_OutputJSONModel = new INIFileBool("OutputJSONModel", false));
        output.addNode(m_OutputDatabaseQueries = new INIFileBool("OutputDatabaseQueries", false));
        output.addNode(m_OutputClowdFlowsJSON = new INIFileBool("OutputClowdFlowsJSON", false));

        return output;

    }


    public void setOutputClowdFlows(boolean value) {
        m_OutputClowdFlowsJSON.setValue(value);
    }


    /**
     * If set to Yes, the output files will be GZiped (including .out and .pred.arff files)
     * 
     * @return
     */
    public boolean isGzipOutput() {
        return m_GzipOutput.getValue();
    }
}
