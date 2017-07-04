
package clus.ext.ensembles;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import clus.Clus;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusSchema;
import clus.error.ClusErrorList;
import clus.ext.hierarchical.WHTDStatistic;
import clus.main.ClusOutput;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.settings.Settings;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.model.processor.ModelProcessorCollection;
import clus.selection.OOBSelection;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStat;
import clus.util.ClusException;


public class ClusOOBErrorEstimate {

    static HashMap m_OOBPredictions;
    static HashMap<Integer, Integer> m_OOBUsage;
    static boolean m_OOBCalculation;
    int m_Mode;
    Settings m_Settings;

    static ClusReadWriteLock m_LockPredictions = new ClusReadWriteLock();
    static ClusReadWriteLock m_LockUsage = new ClusReadWriteLock();
    static ClusReadWriteLock m_LockCalculation = new ClusReadWriteLock();


    public ClusOOBErrorEstimate(int mode, Settings sett) {
        m_OOBPredictions = new HashMap();
        m_OOBUsage = new HashMap<Integer, Integer>();
        m_OOBCalculation = false;
        m_Mode = mode;
        m_Settings = sett;
    }


    private Settings getSettings() {
        return m_Settings;
    }


    public static boolean containsPredictionForTuple(DataTuple tuple) {
        m_LockPredictions.readingLock();
        boolean contains = m_OOBPredictions.containsKey(tuple.hashCode());
        m_LockPredictions.readingUnlock();
        return contains;
    }


    public static double[] getPredictionForRegressionHMCTuple(DataTuple tuple) {
        m_LockPredictions.readingLock();
        double[] pred = (double[]) m_OOBPredictions.get(tuple.hashCode());
        double[] predictions = Arrays.copyOf(pred, pred.length);
        m_LockPredictions.readingUnlock();
        return predictions;
    }


    public static double[][] getPredictionForClassificationTuple(DataTuple tuple) {
        m_LockPredictions.readingLock();
        double[][] pred = (double[][]) m_OOBPredictions.get(tuple.hashCode());
        double[][] predictions = new double[pred.length][];
        for (int i = 0; i < pred.length; i++) {
            predictions[i] = Arrays.copyOf(pred[i], pred[i].length);
        }
        m_LockPredictions.readingUnlock();
        return predictions;
    }


    public synchronized void postProcessForestForOOBEstimate(ClusRun cr, OOBSelection oob_total, RowData all_data, Clus cl, String addname) throws ClusException, IOException {
        Settings sett = cr.getStatManager().getSettings();
        ClusSchema schema = all_data.getSchema();
        ClusOutput output = new ClusOutput(sett.getGeneric().getAppName() + addname + ".oob", schema, sett);
        setOOBCalculation(true);

        // this is the part for writing the predictions from the OOB estimate
        // should new option in .s file be introduced???
        // ClusStatistic target = getStatManager().createStatistic(ClusAttrType.ATTR_USE_TARGET);
        // PredictionWriter wrt = new PredictionWriter(sett.getAppName() + addname + ".oob.pred", sett, target);
        // wrt.globalInitialize(schema);
        // ClusModelInfo allmi = cr.getAllModelsMI();
        // allmi.addModelProcessor(ClusModelInfo.TRAIN_ERR, wrt);
        // cr.copyAllModelsMIs();
        // wrt.initializeAll(schema);

        calcOOBError(oob_total, all_data, ClusModelInfo.TRAIN_ERR, cr);
        cl.calcExtraTrainingSetErrors(cr);
        output.writeHeader();
        output.writeOutput(cr, true, cl.getSettings().getOutput().isOutTrainError());
        output.close();
        // wrt.close();
        setOOBCalculation(false);
        // m_OOBCalculation = false;
    }


    public synchronized void updateOOBTuples(OOBSelection oob_sel, RowData train_data, ClusModel model) throws IOException, ClusException {
        for (int i = 0; i < train_data.getNbRows(); i++) {
            if (oob_sel.isSelected(i)) {
                DataTuple tuple = train_data.getTuple(i);
                if (existsOOBtuple(tuple))
                    updateOOBTuple(tuple, model);
                else
                    addOOBTuple(tuple, model);
            }
        }
    }


    public boolean existsOOBtuple(DataTuple tuple) {
        boolean exists = false;
        boolean existsInUsage = existsInOOBUsage(tuple); // m_OOBUsage.containsKey(tuple.hashCode())
        boolean existsInPred = existsInOOBPredictions(tuple); // m_OOBPredictions.containsKey(tuple.hashCode())
        if (existsInUsage && existsInPred)
            exists = true;
        if (!existsInUsage && existsInPred)
            System.err.println(this.getClass().getName() + ":existsOOBtuple(DataTuple) OOB tuples mismatch-> Usage = False, Predictions = True");
        if (existsInUsage && !existsInPred)
            System.err.println(this.getClass().getName() + ":existsOOBtuple(DataTuple) OOB tuples mismatch-> Usage = True, Predictions = False");
        return exists;
    }


    public void addOOBTuple(DataTuple tuple, ClusModel model) {
        putToOOBUsage(tuple, 1); // m_OOBUsage.put(tuple.hashCode(), 1);

        if (m_Mode == ClusStatManager.MODE_HIERARCHICAL) {
            // for HMC we store the averages
            WHTDStatistic stat = (WHTDStatistic) model.predictWeighted(tuple);
            put1DArrayToOOBPredictions(tuple, stat.getNumericPred());// m_OOBPredictions.put(tuple.hashCode(),stat.getNumericPred());
        }

        if (m_Mode == ClusStatManager.MODE_REGRESSION) {
            // for Regression we store the averages
            RegressionStat stat = (RegressionStat) model.predictWeighted(tuple);
            put1DArrayToOOBPredictions(tuple, stat.getNumericPred());// m_OOBPredictions.put(tuple.hashCode(),
                                                                     // stat.getNumericPred());
        }

        if (m_Mode == ClusStatManager.MODE_CLASSIFY) {
            // this should have a [][].for each attribute we store: Majority: the winning class, for Probability
            // distribution, the class distribution
            ClassificationStat stat = (ClassificationStat) model.predictWeighted(tuple);
            switch (getSettings().getEnsemble().getClassificationVoteType()) {// default is Majority Vote
                case 0:
                    // m_OOBPredictions.put(tuple.hashCode(),
                    // ClusEnsembleInduceOptimization.transformToMajority(stat.m_ClassCounts));
                    put2DArrayToOOBPredictions(tuple, ClusEnsembleInduceOptimization.transformToMajority(stat.m_ClassCounts));//
                    break;
                case 1:
                    // m_OOBPredictions.put(tuple.hashCode(),
                    // ClusEnsembleInduceOptimization.transformToProbabilityDistribution(stat.m_ClassCounts));
                    put2DArrayToOOBPredictions(tuple, ClusEnsembleInduceOptimization.transformToProbabilityDistribution(stat.m_ClassCounts));
                    break;
                default:
                    // m_OOBPredictions.put(tuple.hashCode(),
                    // ClusEnsembleInduceOptimization.transformToMajority(stat.m_ClassCounts));
                    put2DArrayToOOBPredictions(tuple, ClusEnsembleInduceOptimization.transformToMajority(stat.m_ClassCounts));
                    break;
            }
        }
    }


    public void updateOOBTuple(DataTuple tuple, ClusModel model) {
        Integer used = getFromOOBUsage(tuple); // m_OOBUsage.get(tuple.hashCode());
        used = used.intValue() + 1;
        putToOOBUsage(tuple, used); // m_OOBUsage.put(tuple.hashCode(), used);

        if (m_Mode == ClusStatManager.MODE_HIERARCHICAL) {
            // the HMC and Regression have the same voting scheme: average
            WHTDStatistic stat = (WHTDStatistic) model.predictWeighted(tuple);
            double[] predictions = stat.getNumericPred();
            double[] avg_predictions = get1DArrayFromOOBPredictions(tuple); // (double[])m_OOBPredictions.get(tuple.hashCode());
            avg_predictions = ClusEnsembleInduceOptimization.incrementPredictions(avg_predictions, predictions, used.doubleValue());
            put1DArrayToOOBPredictions(tuple, avg_predictions); // m_OOBPredictions.put(tuple.hashCode(),
                                                                // avg_predictions);
        }

        if (m_Mode == ClusStatManager.MODE_REGRESSION) {
            // the HMC and Regression have the same voting scheme: average
            RegressionStat stat = (RegressionStat) model.predictWeighted(tuple);
            double[] predictions = stat.getNumericPred();
            double[] avg_predictions = get1DArrayFromOOBPredictions(tuple); // (double[])m_OOBPredictions.get(tuple.hashCode());
            avg_predictions = ClusEnsembleInduceOptimization.incrementPredictions(avg_predictions, predictions, used.doubleValue());
            put1DArrayToOOBPredictions(tuple, avg_predictions); // m_OOBPredictions.put(tuple.hashCode(),
                                                                // avg_predictions);
        }

        if (m_Mode == ClusStatManager.MODE_CLASSIFY) {
            // implement just addition!!!! and then
            ClassificationStat stat = (ClassificationStat) model.predictWeighted(tuple);
            double[][] predictions = stat.m_ClassCounts.clone();
            switch (getSettings().getEnsemble().getClassificationVoteType()) {// default is Majority Vote
                case 0:
                    predictions = ClusEnsembleInduceOptimization.transformToMajority(predictions);
                    break;
                case 1:
                    predictions = ClusEnsembleInduceOptimization.transformToProbabilityDistribution(predictions);
                    break;
                default:
                    predictions = ClusEnsembleInduceOptimization.transformToMajority(predictions);
                    break;
            }
            double[][] sum_predictions = get2DArrayFromOOBPredictions(tuple); // (double[][])m_OOBPredictions.get(tuple.hashCode());
            sum_predictions = ClusEnsembleInduceOptimization.incrementPredictions(sum_predictions, predictions);
            put2DArrayToOOBPredictions(tuple, sum_predictions);// m_OOBPredictions.put(tuple.hashCode(),
                                                               // sum_predictions);
        }
    }


    public final void calcOOBError(OOBSelection oob_tot, RowData all_data, int type, ClusRun cr) throws IOException, ClusException {
        ClusSchema mschema = all_data.getSchema();
        // if (iter.shouldAttach()) attachModels(mschema, cr);
        cr.initModelProcessors(type, mschema);
        ModelProcessorCollection allcoll = cr.getAllModelsMI().getAddModelProcessors(type);
        DataTuple tuple;// = iter.readTuple();

        for (int t = 0; t < all_data.getNbRows(); t++) {
            if (oob_tot.isSelected(t)) {
                tuple = all_data.getTuple(t);
                allcoll.exampleUpdate(tuple);
                for (int i = 0; i < cr.getNbModels(); i++) {
                    ClusModelInfo mi = cr.getModelInfo(i);
                    ClusModel model = mi.getModel();
                    if (model != null) {
                        ClusStatistic pred = model.predictWeighted(tuple);
                        ClusErrorList err = mi.getError(type);
                        if (err != null)
                            err.addExample(tuple, pred);
                        ModelProcessorCollection coll = mi.getModelProcessors(type);
                        if (coll != null) {
                            if (coll.needsModelUpdate()) {
                                model.applyModelProcessors(tuple, coll);
                                coll.modelDone();
                            }
                            coll.exampleUpdate(tuple, pred);
                        }
                    }
                }
                allcoll.exampleDone();
            }
        }
        cr.termModelProcessors(type);
    }

    // NONSTATIC GETTERS, SETTERS and 'CHECKERS' for


    // OOBCalculation

    public static boolean isOOBCalculation() {
        m_LockCalculation.readingLock();
        boolean isCalc = m_OOBCalculation;
        m_LockCalculation.readingUnlock();
        return isCalc;
    }


    public void setOOBCalculation(boolean value) {
        m_LockCalculation.writingLock();
        m_OOBCalculation = value;
        m_LockCalculation.writingUnlock();
    }


    // OOBPredictions

    private boolean existsInOOBPredictions(DataTuple tuple) {
        m_LockPredictions.readingLock();
        boolean exists = m_OOBPredictions.containsKey(tuple.hashCode());
        m_LockPredictions.readingUnlock();
        return exists;
    }


    public void put1DArrayToOOBPredictions(DataTuple tuple, double[] value) {
        m_LockPredictions.writingLock();
        m_OOBPredictions.put(tuple.hashCode(), value);
        m_LockPredictions.writingUnlock();
    }


    public void put2DArrayToOOBPredictions(DataTuple tuple, double[][] value) {
        m_LockPredictions.writingLock();
        m_OOBPredictions.put(tuple.hashCode(), value);
        m_LockPredictions.writingUnlock();
    }


    private double[] get1DArrayFromOOBPredictions(DataTuple tuple) {
        m_LockPredictions.readingLock();
        double[] pred = (double[]) m_OOBPredictions.get(tuple.hashCode());
        double[] predictions = Arrays.copyOf(pred, pred.length);
        m_LockPredictions.readingUnlock();
        return predictions;
    }


    private double[][] get2DArrayFromOOBPredictions(DataTuple tuple) {
        m_LockPredictions.readingLock();
        double[][] pred = (double[][]) m_OOBPredictions.get(tuple.hashCode());
        double[][] predictions = new double[pred.length][];
        for (int i = 0; i < pred.length; i++) {
            predictions[i] = Arrays.copyOf(pred[i], pred[i].length);
        }
        m_LockPredictions.readingUnlock();
        return predictions;
    }


    // OOBUsage

    private boolean existsInOOBUsage(DataTuple tuple) {
        m_LockUsage.readingLock();
        boolean exists = m_OOBUsage.containsKey(tuple.hashCode());
        m_LockUsage.readingUnlock();
        return exists;
    }


    private void putToOOBUsage(DataTuple tuple, int i) {
        m_LockUsage.writingLock();
        m_OOBUsage.put(tuple.hashCode(), i);
        m_LockUsage.writingUnlock();
    }


    public Integer getFromOOBUsage(DataTuple tuple) {
        m_LockUsage.readingLock();
        Integer i = m_OOBUsage.get(tuple.hashCode());
        m_LockUsage.readingUnlock();
        return i;
    }

}
