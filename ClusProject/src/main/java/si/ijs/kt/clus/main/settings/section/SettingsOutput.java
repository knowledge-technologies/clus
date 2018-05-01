
package si.ijs.kt.clus.main.settings.section;

import java.util.Arrays;
import java.util.List;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnumList;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsOutput extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

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
    protected INIFileEnumList<ShowInfo> m_ShowInfo;
    protected INIFileEnumList<ShowModels> m_ShowModels;
    protected INIFileBool m_PrintModelAndExamples;
    /** Write test/train predictions to files */
    protected INIFileEnumList<WritePredictions> m_WritePredictions;
    protected INIFileBool m_GzipOutput; // added by Jurica Levatic JSI, July 2014
    protected INIFileBool m_WriteErrorFile;
    protected INIFileBool m_WriteModelFile;
    protected INIFileBool m_WritePerBagModelFiles;
    protected INIFileBool m_WriteOOBFile;
    protected INIFileBool m_ModelIDFiles;

    protected INIFileBool m_OutputPythonModel;

    public enum PythonModelType {
        Function, Object
    };

    private INIFileEnum<PythonModelType> m_PythonModelType;

    protected INIFileBool m_OutputJSONModel;
    protected INIFileBool m_OutputDatabaseQueries;
    protected INIFileBool m_WriteCurves;
    protected INIFileBool m_OutputClowdFlowsJSON;
    protected INIFileBool m_OutputROSSubspaces;


    public SettingsOutput(int position) {
        super(position);
    }


    public boolean shouldWritePerBagModelFiles() {
        return m_WritePerBagModelFiles.getValue();
    }


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


    public boolean isOutputROSSubspaces() {
        return m_OutputROSSubspaces.getValue();
    }


    public boolean isWriteTestSetPredictions() {
        return m_WritePredictions.getValue().contains(WritePredictions.Test);
    }


    public boolean isWriteTrainSetPredictions() {
        return m_WritePredictions.getValue().contains(WritePredictions.Train);
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


    public PythonModelType getPythonModelType() {
        return m_PythonModelType.getValue();
    }


    public boolean isWriteOOBFile() {
        return m_WriteOOBFile.getValue();
    }


    public void setWriteOOBFile(boolean value) {
        m_WriteOOBFile.setValue(value);
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


    public boolean getShowModel(ShowModels model) {
        return m_ShowModels.getValue().contains(model);
    }


    public boolean shouldShowModel(int model) {
        boolean others = getShowModel(ShowModels.Others);
        if (model == ClusModel.DEFAULT && getShowModel(ShowModels.Default))
            return true;
        else if (model == ClusModel.ORIGINAL && getShowModel(ShowModels.Original))
            return true;
        else if (model == ClusModel.PRUNED && (getShowModel(ShowModels.Pruned) || others))
            return true;
        else if (others)
            return true;
        return false;
    }


    public StatisticPrintInfo getStatisticPrintInfo() {
        StatisticPrintInfo info = new StatisticPrintInfo();
        List<ShowInfo> vals = m_ShowInfo.getValue();

        info.SHOW_EXAMPLE_COUNT = vals.contains(ShowInfo.Count);
        info.SHOW_EXAMPLE_COUNT_BYTARGET = vals.contains(ShowInfo.CountByTarget);
        info.SHOW_DISTRIBUTION = vals.contains(ShowInfo.Distribution);
        info.SHOW_INDEX = vals.contains(ShowInfo.Index);
        info.INTERNAL_DISTR = vals.contains(ShowInfo.NodePrototypes);
        info.SHOW_KEY = vals.contains(ShowInfo.Key);

        return info;
    }

    /***********************************************************************
     * Section: Output - Show info in .out file *
     ***********************************************************************/

    public enum ShowModels {
        Default, Original, Pruned, Others
    };

    public enum ShowInfo {
        Count, CountByTarget, Distribution, Index, NodePrototypes, Key
    };

    public enum ConvertRules {
        No, Leaves, AllNodes
    };

    public static boolean SHOW_UNKNOWN_FREQ;
    public static boolean SHOW_BRANCH_FREQ;

    /***********************************************************************
     * Section: Output - Write predictions to file *
     ***********************************************************************/

    public enum WritePredictions {
        None, Test, Train
    };


    @Override
    public INIFileSection create() {

        INIFileSection output = new INIFileSection("Output");
        output.addNode(m_ShowModels = new INIFileEnumList<>("ShowModels", Arrays.asList(ShowModels.Default, ShowModels.Pruned, ShowModels.Others)));
        output.addNode(m_OutTrainErr = new INIFileBool("TrainErrors", true));
        output.addNode(m_OutValidErr = new INIFileBool("ValidErrors", true));
        output.addNode(m_OutTestErr = new INIFileBool("TestErrors", true));
        output.addNode(m_OutFoldModels = new INIFileBool("AllFoldModels", true));
        output.addNode(m_OutFoldErr = new INIFileBool("AllFoldErrors", false));
        output.addNode(m_OutFoldData = new INIFileBool("AllFoldDatasets", false));
        output.addNode(m_ShowUnknown = new INIFileBool("UnknownFrequency", false));
        output.addNode(m_ShowBrFreq = new INIFileBool("BranchFrequency", false));
        output.addNode(m_ShowInfo = new INIFileEnumList<>("ShowInfo", Arrays.asList(ShowInfo.Count)));
        output.addNode(m_PrintModelAndExamples = new INIFileBool("PrintModelAndExamples", false));
        output.addNode(m_WriteErrorFile = new INIFileBool("WriteErrorFile", false)); // TODO: bug: see issue #51 in the
                                                                                     // repository
        output.addNode(m_WriteModelFile = new INIFileBool("WriteModelFile", false));
        output.addNode(m_WritePerBagModelFiles = new INIFileBool("WritePerBagModelFile", true));
        output.addNode(m_WriteOOBFile = new INIFileBool("WriteOOBFile", false));
        output.addNode(m_WritePredictions = new INIFileEnumList<>("WritePredictions", Arrays.asList(WritePredictions.None)));
        output.addNode(m_GzipOutput = new INIFileBool("GzipOutput", false));

        // If this option name is to be changed, it must also be changed in testsets/iris-classify.s
        // output.addNode(m_ModelIDFiles = new INIFileBool("WriteModelIDFiles", false));
        output.addNode(m_ModelIDFiles = new INIFileBool("ModelIDFiles", false));
        output.addNode(m_WriteCurves = new INIFileBool("WriteCurves", false));

        output.addNode(m_OutputPythonModel = new INIFileBool("OutputPythonModel", false));
        output.addNode(m_PythonModelType = new INIFileEnum<>("PythonModelType", PythonModelType.Object));

        output.addNode(m_OutputROSSubspaces = new INIFileBool("OutputROSSubspaces", false));
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
