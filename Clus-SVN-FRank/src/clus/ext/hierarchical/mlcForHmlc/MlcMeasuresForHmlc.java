package clus.ext.hierarchical.mlcForHmlc;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import clus.data.rows.DataTuple;
import clus.error.ClusError;
import clus.error.ClusErrorList;
import clus.ext.hierarchical.ClassHierarchy;
import clus.ext.hierarchical.ClassTerm;
import clus.ext.hierarchical.ClassesTuple;
import clus.ext.hierarchical.ClassesValue;
import clus.ext.hierarchical.WHTDStatistic;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;

/**
 * Use if you want to compute MLC-measures in HMLC case.
 * @author matejp
 *
 */
public class MlcMeasuresForHmlc extends ClusError {

        public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;
        private static final double MAJORITY_PROPORTION = 0.5;

        protected ClassHierarchy m_Hier;
        protected boolean[] m_EvalClass;
        private int m_DimEval;
        
        private ArrayList<MlcHmlcSubError> m_SubErrors; 
        
        private double[] m_ComputedErrors;

        
        


        public MlcMeasuresForHmlc(ClusErrorList par, ClassHierarchy hier) {
            super(par, hier.getTotal());
            m_Hier = hier;
            m_EvalClass = hier.getEvalClassesVector();
            m_DimEval = 0;
            for(boolean shouldEval : m_EvalClass){
                m_DimEval += shouldEval ? 1 : 0;
            }
            
            m_SubErrors = new ArrayList<MlcHmlcSubError>();
            m_SubErrors.add(new HammingLoss());
            m_SubErrors.add(new HammingLoss());


        }


        public void addExample(DataTuple tuple, ClusStatistic pred) {
            ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
            boolean[] actual = tp.getVectorBooleanNodeAndAncestors(m_Hier);
            double[] predicted = ((WHTDStatistic) pred).getNumericPred(); // percentages
            
            boolean[] actualFiltered = new boolean[m_DimEval];
            double[] predictedFiltered = new double[m_DimEval]; 
            boolean[] predictedThresholdedFiltered = new boolean[m_DimEval];  // true predictions
            int index = 0;            
            for(int i = 0; i < predicted.length; i++){
                if(m_EvalClass[i]){
                    actualFiltered[index] = actual[i];
                    predictedFiltered[index] = predicted[i];
                    predictedThresholdedFiltered[index] = predicted[i] >= MAJORITY_PROPORTION;
                    index++;
                }
            }           
            updateAll(actualFiltered, predictedFiltered, predictedThresholdedFiltered);
        }
        
        public void updateAll(boolean[] actual, double[] predicted, boolean[] predictedThresholded){
            for(MlcHmlcSubError suberror : m_SubErrors){
                suberror.addExample(actual, predictedThresholded);
            }
        }


        public void addInvalid(DataTuple tuple) {
            ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
            boolean[] actual = tp.getVectorBooleanNodeAndAncestors(m_Hier);
            for (int i = 0; i < m_Dim; i++) {
//                m_ClassWisePredictions[i].addInvalid(actual[i]);
            }
        }


        public boolean isComputeForModel(String name) {
            if (name.equals("Original") || name.equals("Pruned") || name.equals("Default")) {
                return true;
            }
            else if (name.startsWith("Original ") && name.contains("-nn model with ") || name.equals("Default 1-nn model with no weighting")) {
                // this is kNN hackish solution
                return true;
            } else if(name.startsWith("Forest with ") && !name.contains("T = ")){  // added because of the option Iterations  = [10, 20, 30, ...]
                return true;
            }
            return false;
        }


        public boolean shouldBeLow() {
            System.err.println("MlcMeasuresForHmlc are sometimes desired to be low and sometimes not. Will return false");
            return false;
        }


        public double getModelError() {
//            computeAll();
//            switch (m_OptimizeMeasure) {
//                case Settings.HIERMEASURE_AUROC:
//                    return m_AverageAUROC;
//                case Settings.HIERMEASURE_AUPRC:
//                    return m_AverageAUPRC;
//                case Settings.HIERMEASURE_WEIGHTED_AUPRC:
//                    return m_WAvgAUPRC;
//                case Settings.HIERMEASURE_POOLED_AUPRC:
//                    return m_PooledAUPRC;
//            }
            return 0.0;
        }


        public boolean isEvalClass(int idx) {
            // Don't include trivial classes (with only pos or only neg examples)
            return m_EvalClass[idx]; // && includeZeroFreqClasses(idx); // && m_ClassWisePredictions[idx].hasBothPosAndNegEx();
        }


        public void reset() {
//            for (int i = 0; i < m_Dim; i++) {
//                m_ClassWisePredictions[i].clear();
//            }
        }


//        public void add(ClusError other) {
//            BinaryPredictionList[] olist = ((HierErrorMeasures) other).m_ClassWisePredictions;
//            for (int i = 0; i < m_Dim; i++) {
//                m_ClassWisePredictions[i].add(olist[i]);
//            }
//        }
//
//
//        // For errors computed on a subset of the examples, it is sometimes useful
//        // to also have information about all the examples, this information is
//        // passed via this method in the global error measure "global"
//        public void updateFromGlobalMeasure(ClusError global) {
//            BinaryPredictionList[] olist = ((HierErrorMeasures) global).m_ClassWisePredictions;
//            for (int i = 0; i < m_Dim; i++) {
//                m_ClassWisePredictions[i].copyActual(olist[i]);
//            }
//        }


        // prints the evaluation results for each single predicted class
        public void printResultsRec(NumberFormat fr, PrintWriter out, ClassTerm node, boolean[] printed) {
            int idx = node.getIndex();
            // avoid printing a given node several times
            if (printed[idx])
                return;
            printed[idx] = true;
            if (isEvalClass(idx)) {
                ClassesValue val = new ClassesValue(node);
//                out.print("      " + idx + ": " + val.toStringWithDepths(m_Hier));
//                out.print(", AUROC: " + fr.format(m_ROCAndPRCurves[idx].getAreaROC()));
//                out.print(", AUPRC: " + fr.format(m_ROCAndPRCurves[idx].getAreaPR()));
//                out.print(", Freq: " + fr.format(m_ClassWisePredictions[idx].getFrequency()));
//                if (m_RecallValues != null) {
//                    int nbRecalls = m_RecallValues.length;
//                    for (int i = 0; i < nbRecalls; i++) {
//                        int rec = (int) Math.floor(100.0 * m_RecallValues[i] + 0.5);
//                        out.print(", P" + rec + "R: " + fr.format(100.0 * m_ROCAndPRCurves[idx].getPrecisionAtRecall(i)));
//                    }
//                }
//                out.println();
            }
            for (int i = 0; i < node.getNbChildren(); i++) {
                printResultsRec(fr, out, (ClassTerm) node.getChild(i), printed);
            }
        }


        public void printResults(NumberFormat fr, PrintWriter out, ClassHierarchy hier) {
            ClassTerm node = hier.getRoot();
            boolean[] printed = new boolean[hier.getTotal()];
            for (int i = 0; i < node.getNbChildren(); i++) {
                printResultsRec(fr, out, (ClassTerm) node.getChild(i), printed);
            }
        }


        public boolean isMultiLine() {
            return true;
        }


        public void computeAll() {
            int n = m_SubErrors.size();
            m_ComputedErrors = new double[n];
            for(int i = 0; i < n; i++){
                m_ComputedErrors[i] = m_SubErrors.get(i).compute(m_DimEval);
            }

        }




        public void showModelError(PrintWriter out, String bName, int detail) throws IOException {

            NumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
            computeAll();
            
            out.println();
            for(int i = 0; i < m_ComputedErrors.length; i++){
                out.println("      " + m_SubErrors.get(i).getName() + ":            " + m_ComputedErrors[i]);
            }

        }


        public String getName() {
            return "Multilabel error measures";
        }


        public ClusError getErrorClone(ClusErrorList par) {
            return new MlcMeasuresForHmlc(par, m_Hier);
//            return new HierErrorMeasures(par, m_Hier, m_RecallValues, m_Compatibility, m_OptimizeMeasure, m_WriteCurves);
        }

}
