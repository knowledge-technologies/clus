package clus.error.mlcForHmlc;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import clus.data.rows.DataTuple;
import clus.error.common.ClusError;
import clus.error.common.ClusErrorList;
import clus.error.common.ComponentError;
import clus.ext.hierarchical.ClassHierarchy;
import clus.ext.hierarchical.ClassesTuple;
import clus.ext.hierarchical.WHTDStatistic;
import clus.main.settings.Settings;
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
            m_SubErrors.add(new AveragePrecision());
            m_SubErrors.add(new Coverage());
            m_SubErrors.add(new MacroFOne(m_DimEval));
            m_SubErrors.add(new MacroPrecision(m_DimEval));
            m_SubErrors.add(new MacroRecall(m_DimEval));
            m_SubErrors.add(new MicroFOne(m_DimEval));
            m_SubErrors.add(new MicroPrecision(m_DimEval));
            m_SubErrors.add(new MicroRecall(m_DimEval));
            m_SubErrors.add(new MLAccuracy());
            m_SubErrors.add(new MLFOneMeasure());
            m_SubErrors.add(new MLPrecision());
            m_SubErrors.add(new MLRecall());
            m_SubErrors.add(new OneError());
            m_SubErrors.add(new RankingLoss());
            m_SubErrors.add(new SubsetAccuracy());
            m_SubErrors.add(new MLaverageAUPRC(m_DimEval));
            m_SubErrors.add(new MLaverageAUROC(m_DimEval));
            m_SubErrors.add(new MLpooledAUPRC(m_DimEval));
            m_SubErrors.add(new MLweightedAUPRC(m_DimEval));

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
                suberror.addExample(actual, predicted, predictedThresholded);
            }
        }


        public void addInvalid(DataTuple tuple) {
            ClassesTuple tp = (ClassesTuple) tuple.getObjVal(m_Hier.getType().getArrayIndex());
           // boolean[] actual = tp.getVectorBooleanNodeAndAncestors(m_Hier);
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
            throw new RuntimeException("This call has no sense!");
        }


        public boolean isEvalClass(int idx) {
            return m_EvalClass[idx];
        }


        public void reset() {

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
            NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
            int maxLength = 0;
            for(MlcHmlcSubError suberror : m_SubErrors){
                maxLength = Math.max(maxLength, suberror.getName().length());
            }
            String formater = String.format("      %%-%ds %%s%%s", 9 + maxLength);
            computeAll();
            
            out.println();
            for(int i = 0; i < m_ComputedErrors.length; i++){
                String components = "";
                if(m_SubErrors.get(i) instanceof ComponentError){
                    String[] componentErrors = new String[m_DimEval];
                    for (int comp = 0; comp < m_DimEval; comp++) {
                        componentErrors[comp] = fr.format(((ComponentError) m_SubErrors.get(i)).getModelErrorComponent(comp));
                    }
                    components = String.format("%s: ", Arrays.toString(componentErrors));
                }
                out.println(String.format(formater, m_SubErrors.get(i).getName() + ":", components, fr.format(m_ComputedErrors[i])));
            }

        }

        public String getName() {
            return "Multilabel error measures";
        }

        public ClusError getErrorClone(ClusErrorList par) {
            return new MlcMeasuresForHmlc(par, m_Hier);
        }

}
