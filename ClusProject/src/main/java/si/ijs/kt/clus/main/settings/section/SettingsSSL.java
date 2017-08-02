
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileStringOrDouble;


public class SettingsSSL extends SettingsBase {

    public SettingsSSL(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }


    private INIFileSection m_SectionSSL;
    private boolean m_SemiSupervisedMode = false;

    private INIFileNominal m_SSL_SemiSupervisedMethod;
    private final String[] SSL_METHOD = { "SelfTraining", "SelfTrainingFTF", "PCT" };
    public final static int SSL_METHOD_SELF_TRAINING = 0;
    public final static int SSL_METHOD_SELF_TRAINING_FTF = 1;
    public final static int SSL_METHOD_PCT = 2;

    /**
     * unlabeled criteria is the criteria by which the unlabeled data will be added to the training set (used by the
     * Self Training algorithm)
     */
    private final String[] SSL_UNLABELED_CRITERIA = { "Threshold", "KMostConfident", "KPercentageMostConfident", "AutomaticOOB", "ExhaustiveSearch", "KPercentageMostAverage", "AutomaticOOBInitial" };
    private INIFileNominal m_SSL_UnlabeledCriteria;
    public final static int SSL_UNLABELED_CRITERIA_THRESHOLD = 0; // Threshold - all of the unlabeled instances with confidence of prediction greater than Threshold will be added to the training set
    public final static int SSL_UNLABELED_CRITERIA_KMOSTCONFIDENT = 1; // KMostConfident - K unlabeled instances with the most confident predictions will be added to the training set, K is a parameter
    public final static int SSL_UNLABELED_CRITERIA_KPERCENTAGEMOSTCONFIDENT = 2; // KPercentageMostConfident - K percentage of unlabeled instances with the most confident predictions will be added to the training set
    public final static int SSL_UNLABELED_CRITERIA_AUTOMATICOOB = 3; // AutomaticOOB - the optimal threshold will be selected on the basis of OOB error of the labeled examples
    public final static int SSL_UNLABELED_CRITERIA_EXHAUSTIVESEARCH = 4; // ExhaustiveSearch - the optimal threshold will be selected from the specified list at each iteration
    public final static int SSL_UNLABELED_CRITERIA_KPERCENTAGEMOSTAVERAGE = 5; // KPercentageMostAverage - the threshold is set to average confidence score of the K percentage most confident examples (set initially, and then this threshold is used throughout iterations)
    public final static int SSL_UNLABELED_CRITERIA_AUTOMATICOOBINITIAL = 6; // AutomaticOOBInitial - the threshold will be selected according to AutomaticOOB at initial iteration, this threshold is in next iterations 

    /** Stopping criteria for self training */
    private final String[] SSL_STOPPING_CRITERIA = { "NoneAdded", "Iterations", "Airbag" };
    private INIFileNominal m_SSL_StoppingCriteria;
    public final static int SSL_STOPPING_CRITERIA_NONEADDED = 0;
    /** If no instance met the UnlabeledCriteria, i.e., no instance was added to the training set */
    public final static int SSL_STOPPING_CRITERIA_ITERATIONS = 1;
    /** Iterations - stop after specified number of iterations */
    public final static int SSL_STOPPING_CRITERIA_AIRBAG = 2;
    /**
     * (Leistner et al., 2009), based on Out of Bag Error estimate of Random Forest (applicable only if the base method
     * for the Self Training is Random Forest)
     */

    /** Confidence (i.e., reliability) score for Self-Training */
    private final String[] SSL_CONFIDENCE_MEASURE = { "Variance", "RandomUniform", "RandomGaussian", "Oracle", "RForestProximities", "RForestProximitiesV2", "Precision", "ClassesProbabilities" };
    private INIFileNominal m_SSL_ConfidenceMeasure;
    public final static int SSL_CONFIDENCE_MEASURE_VARIANCE = 0;
    /** Variance - standard deviation of votes of ensemble */
    public final static int SSL_CONFIDENCE_MEASURE_RANDOMUNIFORM = 1;
    /** RandomUniform - uniformly distribuited random scores */
    public final static int SSL_CONFIDENCE_MEASURE_RANDOMGAUSSIAN = 2;
    /** RandomGaussian - normally distribuited random scores */
    public final static int SSL_CONFIDENCE_MEASURE_ORACLE = 3;
    /**
     * Oracle - real score, just for testing purposes, actual error on unlabeled examples is calculated to establish
     * reliability scores
     */
    public final static int SSL_CONFIDENCE_MEASURE_RFPROXIMITIES = 4;
    /**
     * RForestProximities - extrapolation of error of unlabeled examples via OOB error of labeled examples in their
     * Random Forest proximity
     */
    public final static int SSL_CONFIDENCE_MEASURE_RFPROXIMITIES_V2 = 5;
    /** RForestProximitiesV2 - ?? */
    public final static int SSL_CONFIDENCE_MEASURE_PRECISION = 6;
    /**
     * "Precision" - used for HMC, on the basis of OOB examples, classification threshold is found such that precision
     * of precision is larger that the specified threshold
     */
    public final static int SSL_CONFIDENCE_MEASURE_CLASSESPROBABILITIES = 7;
    /** Reliability score for MLC and HMLC, based on empirical probabilities of classes */

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
    private INIFileNominal m_SSL_OOBErrorCalculation;
    /**
     * Specifies which data will be used for calculation of OOB error, only originally labeled data or all examples
     * (including the ones with predicted labeles with Self-training
     */
    private final String[] SSL_OOB_ERROR_CALCULATION = { "LabeledOnly", "AllData" };
    public final static int SSL_OOB_ERROR_CALCULATION_LABELED_ONLY = 0;
    public final static int SSL_OOB_ERROR_CALCULATION_ALL_DATA = 1;

    private INIFileNominal m_SSL_normalization;
    /** Normalization of per target reliability scores */
    private final String[] SSL_NORMALIZATION = { "MinMaxNormalization", "Ranking", "Standardization", "NoNormalization" };
    public final static int SSL_NORMALIZATION_MINMAXNORM = 0; //normalization to [0,1] interval
    public final static int SSL_NORMALIZATION_RANKING = 1; //ranks per target scores according to their reliability
    public final static int SSL_NORMALIZATION_STANDARDIZATION = 2; //standardization to 0.5 mean and 0.125 standard deviation (ensures that 99.98% of the scores are in [0,1] interval)
    public final static int SSL_NORMALIZATION_NONORMALIZATION = 3; //does nothing, leaves per-target scores as they are

    private INIFileNominal m_SSL_aggregation;
    /** Aggregation of per target reliability scores */
    private final String[] SSL_AGGREGATION = { "Average", "Minimum", "Maximum" };
    public final static int SSL_AGGREGATION_AVERAGE = 0;
    public final static int SSL_AGGREGATION_MINIMUM = 1;
    public final static int SSL_AGGREGATION_MAXIMUM = 2;
    public final static int SSL_AGGREGATION_AVERAGEIGNOREZEROS = 3; //can be used for HMC, averages all values but zeros 

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

    public String getSemiSupervisedMethodName(int method){
        return SSL_METHOD[method];
    }

    public boolean isSemiSupervisedMode() {
        return m_SemiSupervisedMode;
    }


    public void setSemiSupervisedMode(boolean value) {
        m_SemiSupervisedMode = value;
    }


    public int getSemiSupervisedMethod() {
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


    public void setSemiSupervisedMethod(String value) {
        m_SSL_SemiSupervisedMethod.setValue(value);
    }


    public void setSSLPruningWhenTuning(String value) {
        m_SSL_PruningWhenTuning.setValue(value);
    }


    public void setSSLInternalFolds(String value) {
        m_SSL_InternalFolds.setValue(value);
    }


    public void setStoppingCriteria(String value) {
        m_SSL_StoppingCriteria.setValue(value);
    }


    public int getStoppingCriteria() {
        return m_SSL_StoppingCriteria.getValue();
    }


    public void setUnlabeledCriteria(String value) {
        m_SSL_UnlabeledCriteria.setValue(value);
    }


    public int getUnlabeledCriteria() {
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


    public int getConfidenceMeasure() {
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


    public int getOOBErrorCalculation() {
        return m_SSL_OOBErrorCalculation.getValue();
    }


    public int getSSLNormalization() {
        return m_SSL_normalization.getValue();
    }


    public int getSSLAggregation() {
        return m_SSL_aggregation.getValue();
    }


    @Override
    public INIFileSection create() {

        m_SectionSSL = new INIFileSection("SemiSupervised");
        m_SectionSSL.addNode(m_SSL_SemiSupervisedMethod = new INIFileNominal("SemiSupervisedMethod", SSL_METHOD, SSL_METHOD_PCT));
        m_SectionSSL.addNode(m_SSL_StoppingCriteria = new INIFileNominal("StoppingCriteria", SSL_STOPPING_CRITERIA, SSL_STOPPING_CRITERIA_NONEADDED));
        m_SectionSSL.addNode(m_SSL_UnlabeledCriteria = new INIFileNominal("UnlabeledCriteria", SSL_UNLABELED_CRITERIA, SSL_UNLABELED_CRITERIA_THRESHOLD));
        m_SectionSSL.addNode(m_SSL_ConfidenceThreshold = new INIFileDouble("ConfidenceThreshold", 0.8));
        m_SectionSSL.addNode(m_SSL_ConfidenceMeasure = new INIFileNominal("ConfidenceMeasure", SSL_CONFIDENCE_MEASURE, SSL_CONFIDENCE_MEASURE_VARIANCE));
        m_SectionSSL.addNode(m_SSL_Iterations = new INIFileInt("Iterations", 10));
        m_SectionSSL.addNode(m_SSL_K = new INIFileInt("K", 5));
        m_SectionSSL.addNode(m_SSL_UnlabeledData = new INIFileStringOrDouble("UnlabeledData"));
        m_SectionSSL.addNode(m_SSL_PercentageLabeled = new INIFileInt("PercentageLabeled", 5));
        m_SectionSSL.addNode(m_SSL_useWeights = new INIFileBool("UseWeights", false));
        m_SectionSSL.addNode(m_SSL_airbagTrials = new INIFileInt("AirbagTrials", 0));
        m_SectionSSL.addNode(m_SSL_ExhaustiveSearchThresholds = new INIFileNominalOrDoubleOrVector("ExhaustiveSearchThresholds", NONELIST));
        m_SSL_ExhaustiveSearchThresholds.setDoubleArray(new double[] { 0.5, 0.6, 0.7, 0.8, 0.9, 0.99 });
        m_SectionSSL.addNode(m_SSL_OOBErrorCalculation = new INIFileNominal("OOBErrorCalculation", SSL_OOB_ERROR_CALCULATION, SSL_OOB_ERROR_CALCULATION_LABELED_ONLY));
        m_SectionSSL.addNode(m_SSL_normalization = new INIFileNominal("Normalization", SSL_NORMALIZATION, SSL_NORMALIZATION_MINMAXNORM));
        m_SectionSSL.addNode(m_SSL_aggregation = new INIFileNominal("Aggregation", SSL_AGGREGATION, SSL_AGGREGATION_AVERAGE));
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

    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub
        
    }

}
