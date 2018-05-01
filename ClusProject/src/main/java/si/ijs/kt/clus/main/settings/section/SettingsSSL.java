
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileStringOrDouble;


public class SettingsSSL extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsSSL(int position) {
        super(position);
    }

    private INIFileSection m_SectionSSL;
    private boolean m_SemiSupervisedMode = false;

    private INIFileEnum<SSLMethod> m_SSL_SemiSupervisedMethod;

    public enum SSLMethod {
        SelfTraining, SelfTrainingFTF, PCT
    };

    /**
     * unlabeled criteria is the criteria by which the unlabeled data will be added to the training set (used by the
     * Self Training algorithm)
     */
    public enum SSLUnlabeledCriteria {
        /**
         * Threshold - all of the unlabeled instances with confidence of prediction greater than Threshold will be added
         * to the training set
         */
        Threshold,

        /**
         * KMostConfident - K unlabeled instances with the most confident predictions will be added to the training set,
         * K is a parameter
         */
        KMostConfident,

        /**
         * KPercentageMostConfident - K percentage of unlabeled instances with the most confident predictions will be
         * added to the training set
         */
        KPercentageMostConfident,

        /** AutomaticOOB - the optimal threshold will be selected on the basis of OOB error of the labeled examples */
        AutomaticOOB,

        /** ExhaustiveSearch - the optimal threshold will be selected from the specified list at each iteration */
        ExhaustiveSearch,

        /**
         * KPercentageMostAverage - the threshold is set to average confidence score of the K percentage most confident
         * examples (set initially, and then this threshold is used throughout iterations)
         */
        KPercentageMostAverage,

        /**
         * AutomaticOOBInitial - the threshold will be selected according to AutomaticOOB at initial iteration, this
         * threshold is in next iterations
         */
        AutomaticOOBInitial
    };

    private INIFileEnum<SSLUnlabeledCriteria> m_SSL_UnlabeledCriteria;

    /** Stopping criteria for self training */
    public enum SSLStoppingCriteria {
        /** If no instance met the UnlabeledCriteria, i.e., no instance was added to the training set */
        NoneAdded,

        /** Iterations - stop after specified number of iterations */
        Iterations,

        /**
         * (Leistner et al., 2009), based on Out of Bag Error estimate of Random Forest (applicable only if the base
         * method for the Self Training is Random Forest)
         */
        Airbag
    };

    private INIFileEnum<SSLStoppingCriteria> m_SSL_StoppingCriteria;

    /** Confidence (i.e., reliability) score for Self-Training */
    public enum SSLConfidenceMeasure {
        /** Variance - standard deviation of votes of ensemble */
        Variance,

        /** RandomUniform - uniformly distribuited random scores */
        RandomUniform,

        /** RandomGaussian - normally distribuited random scores */
        RandomGaussian,

        /**
         * Oracle - real score, just for testing purposes, actual error on unlabeled examples is calculated to establish
         * reliability scores
         */
        Oracle,

        /**
         * RForestProximities - extrapolation of error of unlabeled examples via OOB error of labeled examples in their
         * Random Forest proximity
         */
        RForestProximities,

        /** RForestProximitiesV2 - ?? */
        RForestProximitiesV2,

        /**
         * "Precision" - used for HMC, on the basis of OOB examples, classification threshold is found such that
         * precision
         * of precision is larger that the specified threshold
         */
        Precision,

        /** Reliability score for MLC and HMLC, based on empirical probabilities of classes */
        ClassesProbabilities
    };

    private INIFileEnum<SSLConfidenceMeasure> m_SSL_ConfidenceMeasure;

    private INIFileInt m_SSL_Iterations;
    /** number of iterations for Self Training method, applies if StoppingCriteria = Iterations */
    private INIFileDouble m_SSL_ConfidenceThreshold;
    /** Threshold for the confidence of predictions of unlabeled instances to be added to the training set */
    private INIFileInt m_SSL_K;
    /** K parameter for "KMostConfident" unlabeled criteria */
    private INIFileStringOrDouble m_SSL_UnlabeledData;
    /**
     * (Optional) File containing unlabeled data. If unlabeled data is not provided, and SSL is initialized, unlabeled
     * examples will be randomly selected from the training set according to m_SSL_PercentageLabeled setting
     */
    private INIFileInt m_SSL_PercentageLabeled;
    /**
     * From the training data, specified percentage will be used as labeled data, other will be used as unlabeled. This
     * is used when UnlabeledData parameter is not set.
     */
    private INIFileBool m_SSL_useWeights;
    /**
     * whether to add weights to unlabeled examples when adding them to the labeled set. Weights correspond to the
     * confidence score
     */
    private INIFileInt m_SSL_airbagTrials;
    /**
     * number of times it is allowed for the trained model's OOBError to be worse than of the model in previous
     * iteration.
     */
    private INIFileNominalOrDoubleOrVector m_SSL_ExhaustiveSearchThresholds;
    /**
     * a set of thresholds which will be tested when searching for the optimal threshold, if UnlabeledCriteria =
     * ExhaustiveSearch
     */
    private INIFileEnum<SSLOOBErrorCalculation> m_SSL_OOBErrorCalculation;

    /**
     * Specifies which data will be used for calculation of OOB error, only originally labeled data or all examples
     * (including the ones with predicted labeles with Self-training
     */
    public enum SSLOOBErrorCalculation {
        /** calculate OOB error only on original labeled part */
        LabeledOnly,

        /**
         * calculate OOB error on all examples in the training set, i.e., unlabeled examples added to the training set
         * are also considered
         */
        AllData
    };

    private INIFileEnum<SSLNormalization> m_SSL_normalization;

    /** Normalization of per target reliability scores */
    public enum SSLNormalization {
        /** normalization to [0,1] interval */
        MinMaxNormalization,

        /** ranks per target scores according to their reliability */
        Ranking,

        /**
         * standardization to 0.5 mean and 0.125 standard deviation (ensures that 99.98% of the scores are in [0,1]
         * interval)
         */
        Standardization,

        /** does nothing, leaves per-target scores as they are */
        NoNormalization
    };

    private INIFileEnum<SSLAggregation> m_SSL_aggregation;

    /** Aggregation of per target reliability scores */
    public enum SSLAggregation {
        Average, Minimum, Maximum, AverageIgnoreZeros
    };

    private INIFileBool m_CalibrateHmcThreshold;
    /**
     * if set to yes, HMC threshold is calibrated such that the difference between label cardinality of labeled examples
     * and predicted unlabeled examples is minimal
     */

    private INIFileNominalOrDoubleOrVector m_SSL_PossibleWeights;
    /** Candidate weights for automatic w optimization */
    private INIFileBool m_SSL_PruningWhenTuning;
    /** Should the trees be pruned when optimizing w parameter */
    private INIFileInt m_SSL_InternalFolds;
    /** How many folds for internal cross validation for optimizing w */
    private INIFileString m_SSL_WeightScoresFile;


    /** File where results for each candidate w will be written during optimization */

    /**
     * Should calibrate HMC threshold so that the difference between label cardinality of labeled examples and predicted
     * unlabeled examples is minimal
     * 
     * @return
     */
    public boolean shouldCalibrateHmcThreshold() {
        return m_CalibrateHmcThreshold.getValue();
    }


    public boolean isSemiSupervisedMode() {
        return m_SemiSupervisedMode;
    }


    public void setSemiSupervisedMode(boolean value) {
        m_SemiSupervisedMode = value;
    }


    public SSLMethod getSemiSupervisedMethod() {
        return m_SSL_SemiSupervisedMethod.getValue();
    }


    public String getSSLWeightScoresFile() {
        return m_SSL_WeightScoresFile.getValue();
    }


    public boolean getSSLPruningWhenTuning() {
        return m_SSL_PruningWhenTuning.getValue();
    }


    public int getSSLInternalFolds() {
        return m_SSL_InternalFolds.getValue();
    }


    public double[] getSSLPossibleWeights() {
        if (m_SSL_PossibleWeights.isVector())
            return m_SSL_PossibleWeights.getDoubleVector();

        return new double[] { m_SSL_PossibleWeights.getDouble() };
    }


    public void setSemiSupervisedMethod(SSLMethod value) {
        m_SSL_SemiSupervisedMethod.setValue(value);
    }


    public void setSSLPruningWhenTuning(String value) {
        m_SSL_PruningWhenTuning.setValue(value);
    }


    public void setSSLInternalFolds(String value) {
        m_SSL_InternalFolds.setValue(value);
    }


    public void setStoppingCriteria(SSLStoppingCriteria value) {
        m_SSL_StoppingCriteria.setValue(value);
    }


    public SSLStoppingCriteria getStoppingCriteria() {
        return m_SSL_StoppingCriteria.getValue();
    }


    public void setUnlabeledCriteria(SSLUnlabeledCriteria value) {
        m_SSL_UnlabeledCriteria.setValue(value);
    }


    public SSLUnlabeledCriteria getUnlabeledCriteria() {
        return m_SSL_UnlabeledCriteria.getValue();
    }


    public void setIterations(int it) {
        m_SSL_Iterations.setValue(it);
    }


    public int getIterations() {
        return m_SSL_Iterations.getValue();
    }


    public void setConfidenceThreshold(double d) {
        m_SSL_ConfidenceThreshold.setValue(d);
    }


    public double getConfidenceThreshold() {
        return m_SSL_ConfidenceThreshold.getValue();
    }


    public void setK(int K) {
        m_SSL_K.setValue(K);
    }


    public int getK() {
        return m_SSL_K.getValue();
    }


    public String getUnlabeledFile() {
        return m_SSL_UnlabeledData.getValue();
    }


    public boolean isNullUnlabeledFile() {
        return m_SSL_UnlabeledData.getValue() == null || m_SSL_UnlabeledData.getValue() == "";
    }


    public int getPercentageLabeled() {
        return m_SSL_PercentageLabeled.getValue();
    }


    public SSLConfidenceMeasure getConfidenceMeasure() {
        return m_SSL_ConfidenceMeasure.getValue();
    }


    public boolean getUseWeights() {
        return m_SSL_useWeights.getValue();
    }


    public int getAirbagTrails() {
        return m_SSL_airbagTrials.getValue();
    }


    public double[] getExhaustiveSearchThresholds() {
        return m_SSL_ExhaustiveSearchThresholds.getDoubleVector();
    }


    public SSLOOBErrorCalculation getOOBErrorCalculation() {
        return m_SSL_OOBErrorCalculation.getValue();
    }


    public SSLNormalization getSSLNormalization() {
        return m_SSL_normalization.getValue();
    }


    public SSLAggregation getSSLAggregation() {
        return m_SSL_aggregation.getValue();
    }


    @Override
    public INIFileSection create() {

        m_SectionSSL = new INIFileSection("SemiSupervised");
        m_SectionSSL.addNode(m_SSL_SemiSupervisedMethod = new INIFileEnum<>("SemiSupervisedMethod", SSLMethod.PCT));
        m_SectionSSL.addNode(m_SSL_StoppingCriteria = new INIFileEnum<>("StoppingCriteria", SSLStoppingCriteria.NoneAdded));
        m_SectionSSL.addNode(m_SSL_UnlabeledCriteria = new INIFileEnum<>("UnlabeledCriteria", SSLUnlabeledCriteria.Threshold));
        m_SectionSSL.addNode(m_SSL_ConfidenceThreshold = new INIFileDouble("ConfidenceThreshold", 0.8));
        m_SectionSSL.addNode(m_SSL_ConfidenceMeasure = new INIFileEnum<SSLConfidenceMeasure>("ConfidenceMeasure", SSLConfidenceMeasure.Variance));
        m_SectionSSL.addNode(m_SSL_Iterations = new INIFileInt("Iterations", 10));
        m_SectionSSL.addNode(m_SSL_K = new INIFileInt("K", 5));
        m_SectionSSL.addNode(m_SSL_UnlabeledData = new INIFileStringOrDouble("UnlabeledData"));
        m_SectionSSL.addNode(m_SSL_PercentageLabeled = new INIFileInt("PercentageLabeled", 5));
        m_SectionSSL.addNode(m_SSL_useWeights = new INIFileBool("UseWeights", false));
        m_SectionSSL.addNode(m_SSL_airbagTrials = new INIFileInt("AirbagTrials", 0));
        m_SectionSSL.addNode(m_SSL_ExhaustiveSearchThresholds = new INIFileNominalOrDoubleOrVector("ExhaustiveSearchThresholds", NONELIST));
        m_SSL_ExhaustiveSearchThresholds.setDoubleArray(new double[] { 0.5, 0.6, 0.7, 0.8, 0.9, 0.99 });
        m_SectionSSL.addNode(m_SSL_OOBErrorCalculation = new INIFileEnum<>("OOBErrorCalculation", SSLOOBErrorCalculation.LabeledOnly));
        m_SectionSSL.addNode(m_SSL_normalization = new INIFileEnum<>("Normalization", SSLNormalization.MinMaxNormalization));
        m_SectionSSL.addNode(m_SSL_aggregation = new INIFileEnum<>("Aggregation", SSLAggregation.Average));
        m_SectionSSL.addNode(m_CalibrateHmcThreshold = new INIFileBool("CalibrateHmcThreshold", false));
        // end added section by Jurica
        // added 25/1/2017, Tomaz Stepisnik Perdih (JSI)
        m_SectionSSL.addNode(m_SSL_PruningWhenTuning = new INIFileBool("PruningWhenTuning", false));
        m_SectionSSL.addNode(m_SSL_InternalFolds = new INIFileInt("InternalFolds", 5));
        m_SectionSSL.addNode(m_SSL_WeightScoresFile = new INIFileString("WeightScoresFile", "NO"));
        INIFileNominalOrDoubleOrVector temp = new INIFileNominalOrDoubleOrVector("PossibleWeights", new String[] {});
        temp.setDoubleArray(new double[] { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 });
        m_SectionSSL.addNode(m_SSL_PossibleWeights = temp);
        // end added by Tomaz

        return m_SectionSSL;
    }
}
