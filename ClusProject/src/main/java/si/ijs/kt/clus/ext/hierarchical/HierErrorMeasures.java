
package si.ijs.kt.clus.ext.hierarchical;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.error.BinaryPredictionList;
import si.ijs.kt.clus.error.ROCAndPRCurve;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsGeneral;
import si.ijs.kt.clus.main.settings.section.SettingsHMLC;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;


public class HierErrorMeasures extends ClusError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClassHierarchy m_Hier;
    protected boolean[] m_EvalClass;
    protected BinaryPredictionList[] m_ClassWisePredictions;
    protected ROCAndPRCurve[] m_ROCAndPRCurves;
    protected int m_Compatibility;
    protected int m_OptimizeMeasure;
    protected boolean m_WriteCurves;
    protected double[] m_RecallValues;
    protected double[] m_AvgPrecisionAtRecall;

    protected double m_AverageAUROC;
    protected double m_AverageAUPRC;
    protected double m_WAvgAUPRC;
    protected double m_PooledAUPRC;

    protected transient PrintWriter m_PRCurves;
    protected transient PrintWriter m_ROCCurves;

    private boolean m_IsGzipOutput;


    public HierErrorMeasures(ClusErrorList par, ClassHierarchy hier, double[] recalls, int compat, int optimize, boolean wrCurves, boolean isGzipOutput) {
        super(par, hier.getTotal());
        m_Hier = hier;
        m_Compatibility = compat;
        m_OptimizeMeasure = optimize;
        m_WriteCurves = wrCurves;
        m_RecallValues = recalls;
        m_EvalClass = hier.getEvalClassesVector();
        // m_EvalClass = new boolean[hier.getTotal()];
        // m_EvalClass[19] = true;
        m_ClassWisePredictions = new BinaryPredictionList[hier.getTotal()];
        m_ROCAndPRCurves = new ROCAndPRCurve[hier.getTotal()];
        for (int i = 0; i < hier.getTotal(); i++) {
            BinaryPredictionList predlist = new BinaryPredictionList();
            m_ClassWisePredictions[i] = predlist;
            m_ROCAndPRCurves[i] = new ROCAndPRCurve(predlist);
        }

        m_IsGzipOutput = isGzipOutput;
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
        double[] predarr = ((WHTDStatistic) pred).getNumericPred();
        boolean[] actual = tp.getVectorBooleanNodeAndAncestors(m_Hier);
        for (int i = 0; i < m_Dim; i++) {
            m_ClassWisePredictions[i].addExample(actual[i], predarr[i]);
        }
    }


    @Override
    public void addInvalid(DataTuple tuple) {
        ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
        boolean[] actual = tp.getVectorBooleanNodeAndAncestors(m_Hier);
        for (int i = 0; i < m_Dim; i++) {
            m_ClassWisePredictions[i].addInvalid(actual[i]);
        }
    }


    @Override
    public boolean isComputeForModel(String name) {
        if (name.equals("Original") || name.equals("Pruned") || name.equals("Default")) {
            return true;
        }
        else if (name.startsWith("Original ") && name.contains("-nn model with ") || name.equals("Default 1-nn model with no weighting")) {
            // this is kNN hackish solution
            return true;
        }
        else if (name.startsWith("Forest with ") && !name.contains("T = ")) { // added because of the option Iterations
                                                                              // = [10, 20, 30, ...]
            return true;
        }
        return false;
    }


    @Override
    public boolean shouldBeLow() {
        return false;
    }


    @Override
    public double getModelError() {
        computeAll();
        switch (m_OptimizeMeasure) {
            case SettingsHMLC.HIERMEASURE_AUROC:
                return m_AverageAUROC;
            case SettingsHMLC.HIERMEASURE_AUPRC:
                return m_AverageAUPRC;
            case SettingsHMLC.HIERMEASURE_WEIGHTED_AUPRC:
                return m_WAvgAUPRC;
            case SettingsHMLC.HIERMEASURE_POOLED_AUPRC:
                return m_PooledAUPRC;
        }
        return 0.0;
    }


    public boolean isEvalClass(int idx) {
        // Don't include trivial classes (with only pos or only neg examples)
        return m_EvalClass[idx] && includeZeroFreqClasses(idx); // && m_ClassWisePredictions[idx].hasBothPosAndNegEx();
    }


    @Override
    public void reset() {
        for (int i = 0; i < m_Dim; i++) {
            m_ClassWisePredictions[i].clear();
        }
    }


    @Override
    public void add(ClusError other) {
        BinaryPredictionList[] olist = ((HierErrorMeasures) other).m_ClassWisePredictions;
        for (int i = 0; i < m_Dim; i++) {
            m_ClassWisePredictions[i].add(olist[i]);
        }
    }


    // For errors computed on a subset of the examples, it is sometimes useful
    // to also have information about all the examples, this information is
    // passed via this method in the global error measure "global"
    @Override
    public void updateFromGlobalMeasure(ClusError global) {
        BinaryPredictionList[] olist = ((HierErrorMeasures) global).m_ClassWisePredictions;
        for (int i = 0; i < m_Dim; i++) {
            m_ClassWisePredictions[i].copyActual(olist[i]);
        }
    }


    // prints the evaluation results for each single predicted class
    public void printResultsRec(ClusNumberFormat fr, PrintWriter out, ClassTerm node, boolean[] printed) {
        int idx = node.getIndex();
        // avoid printing a given node several times
        if (printed[idx])
            return;
        printed[idx] = true;
        if (isEvalClass(idx)) {
            ClassesValue val = new ClassesValue(node);
            out.print("      " + idx + ": " + val.toStringWithDepths(m_Hier));
            out.print(", AUROC: " + fr.format(m_ROCAndPRCurves[idx].getAreaROC()));
            out.print(", AUPRC: " + fr.format(m_ROCAndPRCurves[idx].getAreaPR()));
            out.print(", Freq: " + fr.format(m_ClassWisePredictions[idx].getFrequency()));
            if (m_RecallValues != null) {
                int nbRecalls = m_RecallValues.length;
                for (int i = 0; i < nbRecalls; i++) {
                    int rec = (int) Math.floor(100.0 * m_RecallValues[i] + 0.5);
                    out.print(", P" + rec + "R: " + fr.format(100.0 * m_ROCAndPRCurves[idx].getPrecisionAtRecall(i)));
                }
            }
            out.println();
        }
        for (int i = 0; i < node.getNbChildren(); i++) {
            printResultsRec(fr, out, (ClassTerm) node.getChild(i), printed);
        }
    }


    public void printResults(ClusNumberFormat fr, PrintWriter out, ClassHierarchy hier) {
        ClassTerm node = hier.getRoot();
        boolean[] printed = new boolean[hier.getTotal()];
        for (int i = 0; i < node.getNbChildren(); i++) {
            printResultsRec(fr, out, (ClassTerm) node.getChild(i), printed);
        }
    }


    @Override
    public boolean isMultiLine() {
        return true;
    }


    public void compatibility(ROCAndPRCurve[] curves, ROCAndPRCurve pooled) {
        double[] thr = null;
        if (m_Compatibility <= SettingsGeneral.COMPATIBILITY_MLJ08) {
            thr = new double[51];
            for (int i = 0; i <= 50; i++) {
                thr[i] = (double) 2 * i / 100.0;
            }
        }
        for (int i = 0; i < curves.length; i++) {
            curves[i].setThresholds(thr);
        }
        pooled.setThresholds(thr);
    }


    public boolean includeZeroFreqClasses(int idx) {
        // Averages never include classes with zero frequency in test set
        return m_ClassWisePredictions[idx].getNbPos() > 0;
    }


    public void computeAll() {
        BinaryPredictionList pooled = new BinaryPredictionList();
        ROCAndPRCurve pooledCurve = new ROCAndPRCurve(pooled);
        compatibility(m_ROCAndPRCurves, pooledCurve);
        for (int i = 0; i < m_Dim; i++) {
            if (isEvalClass(i)) {
                m_ClassWisePredictions[i].sort();
                m_ROCAndPRCurves[i].computeCurves();
                m_ROCAndPRCurves[i].computePrecisions(m_RecallValues);
                outputPRCurve(i, m_ROCAndPRCurves[i]);
                outputROCCurve(i, m_ROCAndPRCurves[i]);
                m_ROCAndPRCurves[i].clear();
                pooled.add(m_ClassWisePredictions[i]);
                m_ClassWisePredictions[i].clearData();
            }
        }
        pooled.sort();
        pooledCurve.computeCurves();
        outputPRCurve(-1, pooledCurve);
        outputROCCurve(-1, pooledCurve);
        pooledCurve.clear();
        // Compute averages
        int cnt = 0;
        double sumAUROC = 0.0;
        double sumAUPRC = 0.0;
        double sumAUPRCw = 0.0;
        double sumFrequency = 0.0;
        for (int i = 0; i < m_Dim; i++) {
            // In compatibility mode, averages never include classes with zero frequency in test set
            if (isEvalClass(i)) {
                double freq = m_ClassWisePredictions[i].getFrequency();
                sumAUROC += m_ROCAndPRCurves[i].getAreaROC();
                sumAUPRC += m_ROCAndPRCurves[i].getAreaPR();
                sumAUPRCw += freq * m_ROCAndPRCurves[i].getAreaPR();
                sumFrequency += freq;
                cnt++;
            }
        }
        m_AverageAUROC = sumAUROC / cnt;
        m_AverageAUPRC = sumAUPRC / cnt;
        m_WAvgAUPRC = sumAUPRCw / sumFrequency;
        m_PooledAUPRC = pooledCurve.getAreaPR();
        // Compute average precisions at recall values
        if (m_RecallValues != null) {
            int nbRecalls = m_RecallValues.length;
            m_AvgPrecisionAtRecall = new double[nbRecalls];
            for (int j = 0; j < nbRecalls; j++) {
                int nbClass = 0;
                for (int i = 0; i < m_Dim; i++) {
                    if (isEvalClass(i)) {
                        double prec = m_ROCAndPRCurves[i].getPrecisionAtRecall(j);
                        m_AvgPrecisionAtRecall[j] += prec;
                        nbClass++;
                    }
                }
                m_AvgPrecisionAtRecall[j] /= nbClass;
            }
        }
    }


    public void ouputCurve(int ci, ArrayList points, PrintWriter curves) {
        String clName = "ALL";
        if (ci != -1) {
            ClassTerm cl = m_Hier.getTermAt(ci);
            clName = "\"" + cl.toStringHuman(m_Hier) + "\"";
        }
        for (int i = 0; i < points.size(); i++) {
            double[] pt = (double[]) points.get(i);
            curves.println(clName + "," + pt[0] + "," + pt[1]);
        }
    }


    public void outputPRCurve(int i, ROCAndPRCurve curve) {
        if (m_PRCurves != null) {
            ArrayList points = curve.getPRCurve();
            ouputCurve(i, points, m_PRCurves);
        }
    }


    public void outputROCCurve(int i, ROCAndPRCurve curve) {
        if (m_ROCCurves != null) {
            ArrayList points = curve.getROCCurve();
            ouputCurve(i, points, m_ROCCurves);
        }
    }


    public void writeCSVFilesPR(String fname) throws IOException {
        if (m_IsGzipOutput) {
            fname += ".gz";
            m_PRCurves = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fname))));
        }
        else {
            m_PRCurves = new PrintWriter(fname);
        }
        m_PRCurves.println("Class,Recall,Precision");
    }


    public void writeCSVFilesROC(String fname) throws IOException {
        if (m_IsGzipOutput) {
            fname += ".gz";
            m_ROCCurves = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fname))));
        }
        else {
            m_ROCCurves = new PrintWriter(fname);
        }

        m_ROCCurves.println("Class,FP,TP");
    }


    @Override
    public void showModelError(PrintWriter out, String bName, int detail) throws IOException {
        if (m_WriteCurves && bName != null) {
            writeCSVFilesPR(bName + ".pr.csv");
            writeCSVFilesROC(bName + ".roc.csv");
        }
        ClusNumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
        computeAll();
        out.println();
        out.println("      Average AUROC:            " + m_AverageAUROC);
        out.println("      Average AUPRC:            " + m_AverageAUPRC);
        out.println("      Average AUPRC (weighted): " + m_WAvgAUPRC);
        out.println("      Pooled AUPRC:             " + m_PooledAUPRC);
        if (m_RecallValues != null) {
            int nbRecalls = m_RecallValues.length;
            for (int i = 0; i < nbRecalls; i++) {
                int rec = (int) Math.floor(100.0 * m_RecallValues[i] + 0.5);
                out.println("      P" + rec + "R: " + (100.0 * m_AvgPrecisionAtRecall[i]));
            }
        }
        if (detail != ClusError.DETAIL_VERY_SMALL) {
            printResults(fr1, out, m_Hier);
        }
        if (m_PRCurves != null) {
            m_PRCurves.close();
            m_PRCurves = null;
        }
        if (m_ROCCurves != null) {
            m_ROCCurves.close();
            m_ROCCurves = null;
        }
    }


    @Override
    public String getName() {
        return "Hierarchical error measures";
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new HierErrorMeasures(par, m_Hier, m_RecallValues, m_Compatibility, m_OptimizeMeasure, m_WriteCurves, m_IsGzipOutput);
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        try {
            showModelError(out, null, detail);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
