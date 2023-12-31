
package si.ijs.kt.clus.ext.semisupervised.confidence;

import java.util.HashMap;
import java.util.Map;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.semisupervised.confidence.aggregation.Aggregation;
import si.ijs.kt.clus.ext.semisupervised.confidence.aggregation.Average;
import si.ijs.kt.clus.ext.semisupervised.confidence.aggregation.AverageHMC;
import si.ijs.kt.clus.ext.semisupervised.confidence.aggregation.Maximum;
import si.ijs.kt.clus.ext.semisupervised.confidence.aggregation.Minimum;
import si.ijs.kt.clus.ext.semisupervised.confidence.normalization.MinMaxNormalization;
import si.ijs.kt.clus.ext.semisupervised.confidence.normalization.NoNormalization;
import si.ijs.kt.clus.ext.semisupervised.confidence.normalization.Normalization;
import si.ijs.kt.clus.ext.semisupervised.confidence.normalization.Ranking;
import si.ijs.kt.clus.ext.semisupervised.confidence.normalization.Standardization;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLAggregation;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLConfidenceMeasure;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLNormalization;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;


public abstract class PredictionConfidence {

    protected double[] m_ConfidenceScores;
    protected Map<Integer, double[]> m_perTargetScores;
    // protected ClusStatistic[] m_Predictions;
    protected int m_counter, m_NbTargets; // m_counter is counter for examples with weight larger than 0. Examples with
                                          // weight=0 are not considered for calculation of confidence score
    protected Normalization m_Normalization;
    protected Aggregation m_Aggregation;
    protected ClusStatManager m_StatManager;


    public PredictionConfidence(ClusStatManager statManager, SSLNormalization normalizationType, SSLAggregation aggregationType) {
        m_StatManager = statManager;

        switch (m_StatManager.getTargetMode()) {
            case REGRESSION:
                m_NbTargets = statManager.getSchema().getNbTargetAttributes();
                break;
            case CLASSIFY:
                m_NbTargets = statManager.getSchema().getNbTargetAttributes();
                break;
            case HIERARCHICAL:
                m_NbTargets = statManager.getHier().getTotal();
                break;
        }

        switch (normalizationType) {
            case MinMaxNormalization:
                m_Normalization = new MinMaxNormalization(m_NbTargets);
                break;
            case Ranking:
                m_Normalization = new Ranking(m_NbTargets);
                break;
            case Standardization:
                m_Normalization = new Standardization(m_NbTargets);
                break;
            case NoNormalization:
                m_Normalization = new NoNormalization();
                break;
            default:
                m_Normalization = new MinMaxNormalization(m_NbTargets);
        }

        switch (aggregationType) {
            case Average:
                m_Aggregation = new Average();
                break;
            case Minimum:
                m_Aggregation = new Minimum();
                break;
            case Maximum:
                m_Aggregation = new Maximum();
                break;
            case AverageIgnoreZeros:
                m_Aggregation = new AverageHMC();
            default:
                m_Aggregation = new Average();
        }

        if (m_StatManager.getTargetMode() == ClusStatManager.Mode.CLASSIFY && m_StatManager.getSettings().getSSL().getConfidenceMeasure().equals(SSLConfidenceMeasure.Variance)) {
            m_Normalization.setIsLessBetter(false);
        }
    }


    public void calculateConfidenceScores(ClusModel model, RowData data, boolean OOB) throws ClusException, InterruptedException {
        int nb_unlabeled = data.getNbRows();
        m_ConfidenceScores = new double[nb_unlabeled];
        m_counter = 0;

        m_perTargetScores = new HashMap<Integer, double[]>();

        for (int i = 0; i < nb_unlabeled; i++) {
            DataTuple tuple = data.getTuple(i);

            double[] instancePerTargetScores = null;

            // we will not consider instances with weight = 0
            if (tuple != null && tuple.getWeight() > 0) {
                if (OOB) {
                    if (((ClusForest) model).containsOOBForTuple(tuple)) {

                        // calculate OOB per-target scores
                        instancePerTargetScores = calculatePerTargetOOBScores((ClusForest) model, tuple);
                        m_perTargetScores.put(i, instancePerTargetScores);

                        m_counter++;
                    }
                    else {
                        m_ConfidenceScores[i] = Double.NaN;
                    }
                }
                else {
                    instancePerTargetScores = calculatePerTargetScores(model, tuple);
                    m_perTargetScores.put(i, instancePerTargetScores);
                    m_counter++;
                }
            }
            else {
                m_ConfidenceScores[i] = Double.NaN;
            }
        }

        // normalize per-target scores
        m_Normalization.normalize(m_perTargetScores);

        // aggregate per target scores into single score
        for (Integer key : m_perTargetScores.keySet()) {
            m_ConfidenceScores[key] = m_Aggregation.aggregate(m_perTargetScores.get(key));
        }

        ClusLogger.info();

    }


    public void calculateConfidenceScores(ClusModel model, RowData unlabeledData) throws ClusException, InterruptedException {
        calculateConfidenceScores(model, unlabeledData, false);
    }


    public void calculateOOBConfidenceScores(ClusForest model, RowData data) throws ClusException, InterruptedException {
        calculateConfidenceScores(model, data, true);
    }


    /**
     * Per-target reliability scores for a given tuple. Each score should be in
     * [0,1] interval, larger values correspond to greater confidence
     *
     * @param model
     * @param tuple

     * @throws ClusException
     * @throws InterruptedException
     */
    public abstract double[] calculatePerTargetScores(ClusModel model, DataTuple tuple) throws ClusException, InterruptedException;


    /**
     * Per-target reliability scores for out-of-bag (labeled) examples.
     * Applicable only for ClusForest.
     *
     * @param model
     * @param tuple

     * @throws ClusException
     * @throws InterruptedException
     */
    public abstract double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple) throws ClusException, InterruptedException;


    public double getConfidence(int i) {
        return m_ConfidenceScores[i];
    }


    public double[] getConfidenceScores() {
        return m_ConfidenceScores;
    }


    public double[] getPerTargetConfidenceScores(int i) {
        return m_perTargetScores.get(i);
    }


    /**
     * Return the number of instances for which the confidence score is !=
     * Double.NaN
     *

     */
    public int getCounter() {
        return m_counter;
    }


    public int getNbTargetAttributes() {
        return m_NbTargets;
    }


    public Normalization getNormalization() {
        return m_Normalization;
    }
}