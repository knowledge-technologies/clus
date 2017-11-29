/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.ensemble.ClusOOBErrorEstimate;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.ClusFormat;
import si.ijs.kt.clus.util.jeans.resource.ResourceInfo;
import si.ijs.kt.clus.util.jeans.util.FileUtil;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


/**
 * Class for outputting the training and testing results to .out file.
 * All the information during the process is gathered here.
 *
 */
public class ClusOutput {

    protected ClusSchema m_Schema;
    protected Settings m_Sett;
    protected PrintWriter m_Writer;
    protected String m_Fname;
    protected Settings m_Sett2;
    protected StringWriter m_StrWrt;


    public ClusOutput(String fname, ClusSchema schema, Settings sett) throws IOException {
        m_Schema = schema;
        m_Sett = sett;
        m_Sett2 = sett;
        m_Fname = fname;
        m_Writer = sett.getGeneric().getFileAbsoluteWriter(fname);
    }


    public ClusOutput(ClusSchema schema, Settings sett) throws IOException {
        m_Schema = schema;
        m_Sett = sett;
        m_Sett2 = sett;
        m_StrWrt = new StringWriter();
        m_Writer = new PrintWriter(m_StrWrt);
    }


    public void print(String str) {
        m_Writer.print(str);
    }


    public String getString() {
        return m_StrWrt.toString();
    }


    public Settings getSettings() {
        return m_Sett;
    }


    public void writeHeader() throws IOException {
        String relname = m_Schema.getRelationName();
        m_Writer.println("Clus run " + relname);
        m_Writer.println(StringUtils.makeString('*', 9 + relname.length()));
        m_Writer.println();
        Date date = m_Schema.getSettings().getGeneric().getDate();
        m_Writer.println("Date: " + DateFormat.getInstance().format(date));
        m_Writer.println("File: " + m_Fname);
        int a_tot = m_Schema.getNbAttributes();
        int a_in = m_Schema.getNbDescriptiveAttributes();
        int a_out = m_Schema.getNbTargetAttributes();
        m_Writer.println("Attributes: " + a_tot + " (input: " + a_in + ", output: " + a_out + ")");
        m_Writer.println("Missing values: " + (m_Schema.hasMissing() ? "Yes" : "No"));
        if (ResourceInfo.isLibLoaded()) {
            m_Writer.println("Memory usage: " + ClusStat.m_InitialMemory + " kB (initial), " + ClusStat.m_LoadedMemory + " kB (data loaded)");
        }
        m_Writer.println();
        m_Sett.show(m_Writer);
        m_Writer.flush();
    }


    public void writeBrief(ClusRun cr) throws IOException {
        String ridx = cr.getIndexString();
        m_Writer.println("Run: " + ridx);
        ClusErrorList te_err = cr.getTestError();
        if (te_err != null) {
            te_err.showErrorBrief(cr, ClusModelInfo.TEST_ERR, m_Writer);
        }
        ClusErrorList tr_err = cr.getTrainError();
        if (m_Sett.getOutput().isOutTrainError() && tr_err != null) {
            tr_err.showErrorBrief(cr, ClusModelInfo.TRAIN_ERR, m_Writer);
        }
        m_Writer.println();
    }


    public void writeOutput(ClusRun cr, boolean detail) throws IOException, ClusException, InterruptedException {
        writeOutput(cr, detail, false);
    }


    public void writeOutput(ClusRun cr, boolean detail, boolean outputtrain) throws IOException, ClusException, InterruptedException {
        ArrayList<ClusModel> models = new ArrayList<ClusModel>();
        String ridx = cr.getIndexString();

        if (getSettings().getHMTR().isSectionHMTREnabled()) {
            m_Writer.println("HMTR Tree");
            m_Writer.println("---------");
            m_Writer.println();
            m_Writer.println(m_Schema.getHMTRHierarchy().printHierarchyTree());
        }

        m_Writer.println("Run: " + ridx);
        m_Writer.println(StringUtils.makeString('*', 5 + ridx.length()));
        m_Writer.println();
        m_Writer.println("Statistics");
        m_Writer.println("----------");
        m_Writer.println();
        m_Writer.println("FTValue (FTest): " + m_Sett.getTree().getFTest());
        double tsec = cr.getInductionTime() / 1000.0;
        double tpru = cr.getPruneTime() / 1000.0;
        // Prepare models for printing if required
        for (int i = 0; i < cr.getNbModels(); i++) {
            ClusModelInfo mi = cr.getModelInfo(i);
            if (mi != null) {
                ClusModel root = mi.getModel();
                if (mi.shouldPruneInvalid()) {
                    root = root.prune(ClusModel.PRUNE_INVALID);
                }
                models.add(root);
            }
            else {
                models.add(null);
            }
        }

        String parallelTime = "";
        if (getSettings().getEnsemble().isEnsembleWithParallelExecution()) {
            parallelTime = " (sequential " + ClusFormat.FOUR_AFTER_DOT.format((cr.getInductionTimeSequential() / 1000.0)) + " sec)";
        }

        // Compute statistics
        String cpu = ResourceInfo.isLibLoaded() ? " (CPU)" : "";
        m_Writer.println("Induction Time: " + ClusFormat.FOUR_AFTER_DOT.format(tsec) + " sec" + parallelTime + cpu);
        m_Writer.println("Pruning Time: " + ClusFormat.FOUR_AFTER_DOT.format(tpru) + " sec" + cpu);
        m_Writer.println("Model information:");
        for (int i = 0; i < cr.getNbModels(); i++) {
            ClusModelInfo mi = cr.getModelInfo(i);
            if (mi != null) {
                ClusModel model = models.get(i);
                // A model info without an actual model is possible
                // E.g., to report error measures in HMCAverageSingleClass
                if (model != null) {
                    m_Writer.print("     " + mi.getName() + ": ");
                    String info_str = model.getModelInfo();
                    String[] info = info_str.split("\\s*\\,\\s*");
                    for (int j = 0; j < info.length; j++) {
                        if (j > 0)
                            m_Writer.print(StringUtils.makeString(' ', mi.getName().length() + 7));
                        m_Writer.println(info[j]);
                    }
                }
            }
        }

        // print multi-label thresholds
        if (cr.getStatManager().getSettings().getMLC().shouldShowThresholds()) {
            String mlThresholdsTitle = "MultiLabelThresholds:";
            m_Writer.println(System.lineSeparator() + mlThresholdsTitle);
            m_Writer.println(StringUtils.makeString('-', mlThresholdsTitle.length()));
            for (int i = 0; i < cr.getNbModels(); i++) {
                ClusModel root = models.get(i);
                String modelName = cr.getModelInfo(i).getName();
                if (cr.getStatManager().getSettings().getMLC().shouldShowThresholds(modelName)) {
                    if (root instanceof ClusForest) {
                        m_Writer.println(modelName);
                        ClusForest forest = (ClusForest) root;
                        int forestSize = forest.getModelSize();
                        for (int tree = 0; tree < forestSize; tree++) {
                            ((ClusNode) forest.getModel(tree)).printMultiLabelThresholds(m_Writer, tree);
                        }
                        m_Writer.println();
                    }
                    else if (root instanceof ClusNode) {
                        m_Writer.println(modelName);
                        ((ClusNode) root).printMultiLabelThresholds(m_Writer, -1);
                        m_Writer.println();
                    }
                }
            }
        }

        // Compute basename - not needed
        String bName = FileUtil.getName(m_Fname);
        m_Writer.println();
        ClusErrorList te_err = cr.getTestError();
        if (m_Sett.getOutput().isOutFoldError() || detail) {
            if (outputtrain) {
                ClusErrorList tr_err = cr.getTrainError();
                if (tr_err != null) {
                    if (ClusOOBErrorEstimate.isOOBCalculation())
                        m_Writer.println("Out-Of-Bag Estimate of Error");
                    else
                        m_Writer.println("Training error");
                    m_Writer.println("--------------");
                    m_Writer.println();
                    tr_err.showError(cr, ClusModelInfo.TRAIN_ERR, bName + ".train", m_Writer, m_Sett);
                    // tr_err.showError(cr, ClusModelInfo.TRAIN_ERR, m_Writer);
                    m_Writer.println();
                }
                ClusErrorList.printExtraError(cr, ClusModelInfo.TRAIN_ERR, m_Writer);
            }
            ClusErrorList va_err = cr.getValidationError();
            if (va_err != null && m_Sett.getOutput().isOutValidError()) {
                m_Writer.println("Validation error");
                m_Writer.println("----------------");
                m_Writer.println();
                va_err.showError(cr, ClusModelInfo.VALID_ERR, bName + ".valid", m_Writer, m_Sett);
                // va_err.showError(cr, ClusModelInfo.VALID_ERR, m_Writer);
                m_Writer.println();
            }
            if (te_err != null && m_Sett.getOutput().isOutTestError()) {
                m_Writer.println("Testing error");
                m_Writer.println("-------------");
                m_Writer.println();
                te_err.showError(cr, ClusModelInfo.TEST_ERR, bName + ".test", m_Writer, m_Sett);
                // te_err.showError(cr, ClusModelInfo.TEST_ERR, m_Writer);
                m_Writer.println();
            }
        }
        StatisticPrintInfo info = m_Sett.getOutput().getStatisticPrintInfo();
        for (int i = 0; i < cr.getNbModels(); i++) {
            if (cr.getModelInfo(i) != null && models.get(i) != null && m_Sett.getOutput().shouldShowModel(i)) {
                ClusModelInfo mi = cr.getModelInfo(i);
                ClusModel root = models.get(i);
                String modelname = mi.getName() + " Model";
                m_Writer.println(modelname);
                m_Writer.println(StringUtils.makeString('*', modelname.length()));
                m_Writer.println();
                if (m_Sett.getOutput().isPrintModelAndExamples()) {
                    RowData pex = (RowData) cr.getTrainingSet();
                    // System.out.println(te_err);
                    if (te_err != null)
                        pex = cr.getTestSet();
                    root.printModelAndExamples(m_Writer, info, pex);
                }
                else {
                    root.printModel(m_Writer, info);
                }
                m_Writer.println();
                if (getSettings().getOutput().isOutputPythonModel()) {
                    if (getSettings().getEnsemble().isEnsembleMode() && (i == ClusModel.ORIGINAL)) {
                        root.printModelToPythonScript(m_Writer);// root is a forest
                    }
                    else if (i == ClusModel.DEFAULT) {
                        // prints the python code for the default model
                        m_Writer.print("def clus_default(xs):");
                        root.printModelToPythonScript(m_Writer);// root is a forest
                        
                        
                        
                        /*
                        try {
                            File pyscript = new File(m_AppName + "_default.py");
                            PrintWriter wrtr = new PrintWriter(new FileOutputStream(pyscript));
                            wrtr.println("# Python code of the trees in the ensemble");
                            wrtr.println();
                            
                            for (int i = 0; i < m_Forest.size(); i++) {
                                ClusModel model = m_Forest.get(i);
                                wrtr.println("#Model " + (i + 1));
                                wrtr.println("def clus_tree_" + (i + 1) + "(xs):"); // m_AttributeList
                                ((ClusNode) model).printModelToPythonScript(wrtr, m_DescriptiveIndex);
                                wrtr.println();
                            }
                            
                            wrtr.flush();
                            wrtr.close();
                            System.out.println("Python code for Forest model written to: " + pyscript.getName());
                        }
                        catch (IOException e) {
                            System.err.println(this.getClass().getName() + ".printForestToPython(): Error while writing models to python script");
                            e.printStackTrace();
                        }                        
                        
                        */
                        
                        
                        
                        
                        
                        

                    }
                }
            }
        }
        if (getSettings().getOutput().isOutputDatabaseQueries()) {
            int starttree = getSettings().getExhaustiveSearch().getStartTreeCpt();
            int startitem = getSettings().getExhaustiveSearch().getStartItemCpt();
            ClusModel root = models.get(cr.getNbModels() - 1);
            // use the following lines for creating a SQL file that will put the tree into a database
            String out_database_name = m_Sett2.getGeneric().getAppName() + ".txt";
            PrintWriter database_writer = m_Sett2.getGeneric().getFileAbsoluteWriter(out_database_name);
            root.printModelToQuery(database_writer, cr, starttree, startitem, getSettings().getExhaustiveSearch().isExhaustiveSearch());
            database_writer.close();
            System.out.println("The queries are in " + out_database_name);
        }

        if (getSettings().getOutput().isOutputClowdFlowsJSON()) {
            JsonObject output = new JsonObject();
            StringWriter settingsStringWriter = new StringWriter();
            PrintWriter settingsWriter = new PrintWriter(settingsStringWriter);
            m_Sett.show(settingsWriter);
            output.addProperty("settings", settingsStringWriter.toString());
            JsonArray outputModels = new JsonArray();
            output.add("models", outputModels);
            for (int i = 0; i < cr.getNbModels(); i++) {
                if (cr.getModelInfo(i) != null && models.get(i) != null && m_Sett.getOutput().shouldShowModel(i)) {
                    ClusModelInfo mi = cr.getModelInfo(i);
                    ClusModel root = models.get(i);
                    String modelname = mi.getName();
                    JsonObject currentModel = new JsonObject();
                    currentModel.addProperty("name", modelname);
                    RowData pex = (RowData) cr.getTrainingSet();
                    // System.out.println(te_err);
                    if (te_err != null)
                        pex = cr.getTestSet();
                    currentModel.add("representation", root.getModelJSON(info, pex));
                    outputModels.add(currentModel);
                }
            }

            String jsonFileName = m_Sett2.getGeneric().getAppName() + ".json";
            PrintWriter jsonWriter = m_Sett2.getGeneric().getFileAbsoluteWriter(jsonFileName);
            jsonWriter.write(output.toString());
            //System.out.print(output.toString());
            jsonWriter.close();
        }
        m_Writer.flush();
    }


    public String getQuotient(int a, int b) {
        double val = b == 0 ? 0.0 : (double) a / b;
        return ClusFormat.ONE_AFTER_DOT.format(val);
    }


    public void writeSummary(ClusSummary summary) throws IOException {
        m_Writer.println("Summary");
        m_Writer.println("*******");
        m_Writer.println();
        int runs = summary.getNbRuns();
        m_Writer.println("Runs: " + runs);
        double tsec = summary.getInductionTime() / 1000.0;
        m_Writer.println("Induction time: " + ClusFormat.FOUR_AFTER_DOT.format(tsec) + " sec");
        double psec = summary.getPrepareTime() / 1000.0;
        m_Writer.println("Preprocessing time: " + ClusFormat.ONE_AFTER_DOT.format(psec) + " sec");
        m_Writer.println("Mean number of tests");
        for (int i = ClusModel.ORIGINAL; i <= ClusModel.PRUNED; i++) {
            ClusModelInfo mi = summary.getModelInfo(i);
            if (mi != null)
                m_Writer.println("     " + mi.getName() + ": " + getQuotient(mi.getModelSize(), runs));
        }
        m_Writer.println();
        String bName = FileUtil.getName(m_Fname);
        ClusErrorList tr_err = summary.getTrainError();
        if (m_Sett.getOutput().isOutTrainError() && tr_err != null) {
            m_Writer.println("Training error");
            m_Writer.println("--------------");
            m_Writer.println();
            tr_err.showError(summary, ClusModelInfo.TRAIN_ERR, bName + ".train", m_Writer, m_Sett);
            // tr_err.showError(summary, ClusModelInfo.TRAIN_ERR, m_Writer);
            m_Writer.println();
        }
        ClusErrorList va_err = summary.getValidationError();
        if (va_err != null) {
            m_Writer.println("Validation error");
            m_Writer.println("----------------");
            m_Writer.println();
            va_err.showError(summary, ClusModelInfo.VALID_ERR, bName + ".valid", m_Writer, m_Sett);
            // va_err.showError(summary, ClusModelInfo.VALID_ERR, m_Writer);
            m_Writer.println();
        }
        ClusErrorList te_err = summary.getTestError();
        if (te_err != null) {
            m_Writer.println("Testing error");
            m_Writer.println("-------------");
            m_Writer.println();
            te_err.showError(summary, ClusModelInfo.TEST_ERR, bName + ".test", m_Writer, m_Sett);
            // te_err.showError(summary, ClusModelInfo.TEST_ERR, m_Writer);
        }
        m_Writer.println();
        m_Writer.flush();
    }


    public PrintWriter getWriter() {
        return m_Writer;
    }


    public void close() {
        if (m_Fname != null)
            System.out.println("Output written to: " + m_Fname);
        m_Writer.close();
    }


    public static void printHeader() {
        System.out.println("Clus v" + Clus.VERSION + " - Software for Predictive Clustering");
        System.out.println();
        System.out.println("Copyright (C) 2007, 2008, 2009, 2010");
        System.out.println("   Katholieke Universiteit Leuven, Leuven, Belgium");
        System.out.println("   Jozef Stefan Institute, Ljubljana, Slovenia");
        System.out.println();
        System.out.println("This program is free software and comes with ABSOLUTELY NO");
        System.out.println("WARRANTY. You are welcome to redistribute it under certain");
        System.out.println("conditions. Type 'clus -copying' for distribution details.");
        System.out.println();
    }


    public static void showHelp() {
        System.out.println("Usage: clus appname");
        System.out.println("Database: appname.arff");
        System.out.println("Settings: appname.s");
        System.out.println("Output:   appname.out");
        System.out.println();
        System.out.println("More information on:");
        System.out.println("http://www.cs.kuleuven.be/~dtai/clus");
    }


    public static void printGPL() {
        System.out.println("The GNU GENERAL PUBLIC LICENSE is written in the file 'LICENSE.txt'.");
    }
}
