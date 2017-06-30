package clus.ext.semisupervised.confidence;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.ext.ensembles.ClusForest;
import clus.ext.semisupervised.confidence.aggregation.Aggregation;
import clus.ext.semisupervised.confidence.aggregation.Average;
import clus.ext.semisupervised.confidence.aggregation.AverageHMC;
import clus.ext.semisupervised.confidence.aggregation.Maximum;
import clus.ext.semisupervised.confidence.aggregation.Minimum;
import clus.ext.semisupervised.confidence.normalization.MinMaxNormalization;
import clus.ext.semisupervised.confidence.normalization.NoNormalization;
import clus.ext.semisupervised.confidence.normalization.Normalization;
import clus.ext.semisupervised.confidence.normalization.Ranking;
import clus.ext.semisupervised.confidence.normalization.Standardization;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.util.ClusException;
import java.util.HashMap;
import java.util.Map;

public abstract class PredictionConfidence {

    protected double[] m_ConfidenceScores;
    protected Map<Integer, double[]> m_perTargetScores;
//    protected ClusStatistic[] m_Predictions;
    protected int m_counter, m_NbTargets; // m_counter is counter for examples with weight larger than 0. Examples with weight=0 are not considered for calculation of confidence score
    protected Normalization m_Normalization;
    protected Aggregation m_Aggregation;
    protected ClusStatManager m_StatManager;

    public PredictionConfidence(ClusStatManager statManager, int normalizationType, int aggregationType) {
        m_StatManager = statManager;

        switch (m_StatManager.getMode()) {
            case ClusStatManager.MODE_REGRESSION:
                m_NbTargets = statManager.getSchema().getNbTargetAttributes();
                break;
            case ClusStatManager.MODE_CLASSIFY:
                m_NbTargets = statManager.getSchema().getNbTargetAttributes();
                break;
            case ClusStatManager.MODE_HIERARCHICAL:
                m_NbTargets = statManager.getHier().getTotal();
                break;
        }

        switch (normalizationType) {
            case Settings.SSL_NORMALIZATION_MINMAXNORM:
                m_Normalization = new MinMaxNormalization(m_NbTargets);
                break;
            case Settings.SSL_NORMALIZATION_RANKING:
                m_Normalization = new Ranking(m_NbTargets);
                break;
            case Settings.SSL_NORMALIZATION_STANDARDIZATION:
                m_Normalization = new Standardization(m_NbTargets);
                break;
            case Settings.SSL_NORMALIZATION_NONORMALIZATION:
                m_Normalization = new NoNormalization();
                break;
            default:
                m_Normalization = new MinMaxNormalization(m_NbTargets);
        }

        switch (aggregationType) {
            case Settings.SSL_AGGREGATION_AVERAGE:
                m_Aggregation = new Average();
                break;
            case Settings.SSL_AGGREGATION_MINIMUM:
                m_Aggregation = new Minimum();
                break;
            case Settings.SSL_AGGREGATION_MAXIMUM:
                m_Aggregation = new Maximum();
                break;
            case Settings.SSL_AGGREGATION_AVERAGEIGNOREZEROS:
                m_Aggregation = new AverageHMC();
            default:
                m_Aggregation = new Average();
        }
        
        if (m_StatManager.getMode() == ClusStatManager.MODE_CLASSIFY && m_StatManager.getSettings().getConfidenceMeasure() == Settings.SSL_CONFIDENCE_MEASURE_VARIANCE) {
            m_Normalization.setIsLessBetter(false);
        }
    }

    public void calculateConfidenceScores(ClusModel model, RowData data, boolean OOB) {
        int nb_unlabeled = data.getNbRows();
        m_ConfidenceScores = new double[nb_unlabeled];
        m_counter = 0;

        m_perTargetScores = new HashMap<Integer, double[]>();

        for (int i = 0; i < nb_unlabeled; i++) {
            DataTuple tuple = data.getTuple(i);

            double[] instancePerTargetScores = null;
            
            //we will not consider instances with weight = 0
            if (tuple != null && tuple.getWeight() > 0) {
                if (OOB) {
                    if (((ClusForest) model).containsOOBForTuple(tuple)) {

                        //calculate OOB per-target scores
                        instancePerTargetScores = calculatePerTargetOOBScores((ClusForest) model, tuple);
                        m_perTargetScores.put(i, instancePerTargetScores);
                        
                        m_counter++;
                    } else {
                        m_ConfidenceScores[i] = Double.NaN;
                    }
                } else {
                    instancePerTargetScores = calculatePerTargetScores(model, tuple);
                    m_perTargetScores.put(i, instancePerTargetScores);
                    m_counter++;
                }
            } else {
                m_ConfidenceScores[i] = Double.NaN;
            }          
        }

        
        
        //normalize per-target scores
        m_Normalization.normalize(m_perTargetScores);

        //aggregate per target scores into single score
        for (Integer key : m_perTargetScores.keySet()) {
            m_ConfidenceScores[key] = m_Aggregation.aggregate(m_perTargetScores.get(key));
        }
        
        System.out.println();
        
    }

    public void calculateConfidenceScores(ClusModel model, RowData unlabeledData) throws ClusException {
        calculateConfidenceScores(model, unlabeledData, false);
    }

    public void calculateOOBConfidenceScores(ClusForest model, RowData data) throws ClusException {
        calculateConfidenceScores(model, data, true);
    }

    /**
     * Per-target reliability scores for a given tuple. Each score should be in
     * [0,1] interval, larger values correspond to greater confidence
     *
     * @param model
     * @param tuple
     * @return
     */
    public abstract double[] calculatePerTargetScores(ClusModel model, DataTuple tuple);

    /**
     * Per-target reliability scores for out-of-bag (labeled) examples.
     * Applicable only for ClusForest.
     *
     * @param model
     * @param tuple
     * @return
     */
    public abstract double[] calculatePerTargetOOBScores(ClusForest model, DataTuple tuple);

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
     * @return
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