package si.ijs.kt.clus.main.settings;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileStringOrInt;


public class SettingsRules implements SettingsBase {

    /***********************************************************************
     * Section: Rules *
     ***********************************************************************/

    private INIFileSection m_SectionRules;
    private INIFileBool m_PrintAllRules;


    public void setSectionRulesEnabled(boolean enable) {
        m_SectionRules.setEnabled(enable);
    }


    public boolean isPrintAllRules() {
        return m_PrintAllRules.getValue();
    }

    public void setPrintAllRules(boolean value) {
        m_PrintAllRules.setValue(value);
    }
    
    private final String[] COVERING_METHODS = { "Standard", "WeightedMultiplicative", "WeightedAdditive", "WeightedError", "Union", "BeamRuleDefSet", "RandomRuleSet", "StandardBootstrap", "HeurOnly", "RulesFromTree", "SampledRuleSet" };

    // Standard covering: ordered rules (decision list)
    public final static int COVERING_METHOD_STANDARD = 0;

    // 'Weighted' coverings: unordered rules
    public final static int COVERING_METHOD_WEIGHTED_MULTIPLICATIVE = 1;
    public final static int COVERING_METHOD_WEIGHTED_ADDITIVE = 2;
    public final static int COVERING_METHOD_WEIGHTED_ERROR = 3;

    public final static int COVERING_METHOD_UNION = 4; // In multi-label classification: predicted set of classes is union of predictions of individual rules

    // Evaluates rules in the context of complete rule set: unordered rules
    // public final static int COVERING_METHOD_RULE_SET = 5;

    // Evaluates rules in the context of complete rule set, checks all rules
    // in the beam: unordered rules
    // public final static int COVERING_METHOD_BEAM_RULE_SET = 6;

    /**
     * Evaluates rules in the context of complete rule set, builds default
     * rule first, checks all rules in the beam: unordered rules
     * FIXME Obsolete - should be deleted!
     */
    public final static int COVERING_METHOD_BEAM_RULE_DEF_SET = 5;

    /**
     * Evaluates rules in the context of complete rule set, separate rules
     * are constructed randomly: unordered rules.
     * This is set only if the amount of RandomRules is greater than 0.
     */
    public final static int COVERING_METHOD_RANDOM_RULE_SET = 6;

    /**
     * Repeated standard covering on bootstraped data
     */
    public final static int COVERING_METHOD_STANDARD_BOOTSTRAP = 7;

    /**
     * No covering, only heuristic
     */
    public final static int COVERING_METHOD_HEURISTIC_ONLY = 8;

    /**
     * No covering, rules transcribed from tree
     */
    public final static int COVERING_METHOD_RULES_FROM_TREE = 9;

    /**
     * No covering, rules created with PCT enemble, converted to rules and then sampled
     */
    public final static int COVERING_METHOD_SAMPLED_RULE_SET = 10;

    private final String[] RULE_PREDICTION_METHODS = { "DecisionList", "TotCoverageWeighted", "CoverageWeighted", "AccuracyWeighted", "AccCovWeighted", "EquallyWeighted", "Optimized", "Union", "GDOptimized", "GDOptimizedBinary" };

    public final static int RULE_PREDICTION_METHOD_DECISION_LIST = 0;

    /**
     * Each rule's prediction has a weight proportional to its coverage on the total learning set
     */
    public final static int RULE_PREDICTION_METHOD_TOT_COVERAGE_WEIGHTED = 1;

    /**
     * Each rule's prediction has a weight proportional to its coverage on the current learning set
     * i.e., learning set on which the rule was learned
     */
    public final static int RULE_PREDICTION_METHOD_COVERAGE_WEIGHTED = 2;

    /**
     * Each rule's prediction has a weight proportional to its accuracy on the total learning set
     * TODO Not yet implemented.
     */
    public final static int RULE_PREDICTION_METHOD_ACCURACY_WEIGHTED = 3;

    /**
     * Each rule's prediction has a weight proportional a product of to its accuracy on
     * the total learning set and its coverage
     */
    public final static int RULE_PREDICTION_METHOD_ACC_COV_WEIGHTED = 4;
    // TODO Not yet implemented.
    public final static int RULE_PREDICTION_METHOD_EQUALLY_WEIGHTED = 5;
    /** Differential evolution optimization of rule weights */
    public final static int RULE_PREDICTION_METHOD_OPTIMIZED = 6;
    public final static int RULE_PREDICTION_METHOD_UNION = 7;
    /** Gradient descent optimization of rule weights */
    public final static int RULE_PREDICTION_METHOD_GD_OPTIMIZED = 8;
    /** Use external binary file for gradient descent optimization of rule weights */
    public final static int RULE_PREDICTION_METHOD_GD_OPTIMIZED_BINARY = 9;

    private final String[] RULE_ADDING_METHODS = { "Always", "IfBetter", "IfBetterBeam" };
    // Always adds a rule to the rule set
    public final static int RULE_ADDING_METHOD_ALWAYS = 0;
    // Only adds a rule to the rule set if it improves the rule set performance
    public final static int RULE_ADDING_METHOD_IF_BETTER = 1;
    // Only adds a rule to the rule set if it improves the rule set performance.
    // If not, it checks other rules in the beam
    public final static int RULE_ADDING_METHOD_IF_BETTER_BEAM = 2;

    private boolean IS_RULE_SIG_TESTING = false;

    /**
     * How the initial rules are generated when using SampledRuleSet covering method
     */
    public final static String[] INITIAL_RULE_GENERATING_METHODS = { "RandomForest", "OptionTree" };
    public final static int INITIAL_RULE_GENERATING_METHOD_RANDOM_FOREST = 0;
    public final static int INITIAL_RULE_GENERATING_METHOD_OPTION_TREE = 1;

    // ***************** WEIGHT OPTIMIZATION

    // Differential evolution algorithm
    /** Possible loss functions for evolutionary algorithm optimization */
    private final String[] OPT_LOSS_FUNCTIONS = { "Squared", "01Error", "RRMSE", "Huber" };

    /** Optimization Loss function type. Default for regression: Square of differences. */
    public final static int OPT_LOSS_FUNCTIONS_SQUARED = 0;
    /** Optimization Loss function type. 0/1 error for classification. Zenko 2007, p. 26 */
    public final static int OPT_LOSS_FUNCTIONS_01ERROR = 1;
    /** Optimization Loss function type. Relative root mean squared error */
    public final static int OPT_LOSS_FUNCTIONS_RRMSE = 2;
    /**
     * Optimization Loss function type. Huber 1962 error. Like squared but robust for outliers. Friedman&Popescu 2005,
     * p. 7
     */
    public final static int OPT_LOSS_FUNCTIONS_HUBER = 3;

    /** GD optimization. Possible values for combining gradient targets to single gradient value. */
    private final String[] OPT_GD_MT_COMBINE_GRADIENTS = { "Avg", "Max", "MaxLoss", "MaxLossFast" };
    /** GD optimization, combining of targets - combine by taking average. */
    public final static int OPT_GD_MT_GRADIENT_AVG = 0;
    /** GD optimization, combining of targets - combine by taking max gradient. */
    public final static int OPT_GD_MT_GRADIENT_MAX_GRADIENT = 1;
    /** GD optimization, combining of targets - combine by taking the gradient of target with maximal loss. */
    public final static int OPT_GD_MT_GRADIENT_MAX_LOSS_VALUE = 2;
    /**
     * GD optimization, combining of targets - combine by taking the gradient of target with maximal LINEAR loss.
     * I.e. if the real loss is something else, we still use linear loss. NOT IMPLEMENTED!
     * In fact was not any faster AND max loss is worse than avg.
     */
    public final static int OPT_GD_MT_GRADIENT_MAX_LOSS_VALUE_FAST = 3;

    /** For external GD binary, do we use GD or brute force method */
    private final String[] GD_EXTERNAL_METHOD_VALUES = { "update", "brute" };
    public final static int GD_EXTERNAL_METHOD_GD = 0;
    public final static int GD_EXTERNAL_METHOD_BRUTE = 1;

    private final String[] OPT_LINEAR_TERM_NORM_VALUES = { "No", "Yes", "YesAndConvert" };
    /** Do not normalize linear terms at all */
    public final static int OPT_LIN_TERM_NORM_NO = 0;
    /**
     * Normalize linear terms for optimization and leave the
     * normalization to the resulting rule ensemble. DEFAULT.
     */
    public final static int OPT_LIN_TERM_NORM_YES = 1;
    /**
     * Normalize linear terms for optimization, but after optimization
     * convert the linear terms to plain terms without normalization without changing the resulting
     * prediction.
     */
    public final static int OPT_LIN_TERM_NORM_CONVERT = 2;

    private final String[] OPT_GD_ADD_LINEAR_TERMS = { "No", "Yes", "YesSaveMemory" };
    /** Do not add linear terms. DEFAULT */
    public final static int OPT_GD_ADD_LIN_NO = 0;
    /** Add linear terms explicitly. May cause huge memory usage if lots of targets and descriptive attrs. */
    public final static int OPT_GD_ADD_LIN_YES = 1;
    /** Use linear terms in optimization, but add them explicitly after optimization (if weight is zero). */
    public final static int OPT_GD_ADD_LIN_YES_SAVE_MEMORY = 2;

    public final static String[] OPT_NORMALIZATION = { "No", "Yes", "OnlyScaling", "YesVariance" };
    /** Do not normalize */
    public final static int OPT_NORMALIZATION_NO = 0;
    /** Normalize during optimization with 2*std dev and shifting with average. Default. */
    public final static int OPT_NORMALIZATION_YES = 1;
    /** Normalize during optimization with 2*std dev. Default. */
    public final static int OPT_NORMALIZATION_ONLY_SCALING = 2;
    /** Normalize during optimization with variance. */
    public final static int OPT_NORMALIZATION_YES_VARIANCE = 3;

    // Settings in the settings file.
    private INIFileNominal m_CoveringMethod;
    private INIFileNominal m_PredictionMethod;
    private INIFileNominal m_RuleAddingMethod;
    private INIFileNominal m_InitialRuleGeneratingMethod;
    private INIFileDouble m_CoveringWeight;
    private INIFileDouble m_InstCoveringWeightThreshold;
    private INIFileInt m_MaxRulesNb;
    private INIFileDouble m_HeurDispOffset;
    private INIFileDouble m_HeurCoveragePar;
    private INIFileDouble m_HeurRuleDistPar;
    private INIFileDouble m_HeurPrototypeDistPar;
    private INIFileDouble m_RuleSignificanceLevel;
    private INIFileInt m_RuleNbSigAtts;
    private INIFileBool m_ComputeDispersion;
    private INIFileDouble m_VarBasedDispNormWeight;
    private INIFileNominalOrDoubleOrVector m_DispersionWeights;
    /** How many random rules are wanted. If > 0 only random rules are generated */
    private INIFileInt m_RandomRules;
    private INIFileBool m_RuleWiseErrors;

    // Rule tests are constrained to the first possible attribute value
    private INIFileBool m_constrainedToFirstAttVal;

    // Differential evolution optimization
    /** DE Number of individuals (population) during every iteration */
    private INIFileInt m_OptDEPopSize;
    /**
     * Differential evolution, number of individual evaluations to be done. Divide this with m_OptDEPopSize
     * to get the number of 'iterations'
     */
    private INIFileInt m_OptDENumEval;
    /** DE Crossover probability */
    private INIFileDouble m_OptDECrossProb;
    private INIFileDouble m_OptDEWeight;
    private INIFileInt m_OptDESeed;
    /** DE The power of regularization function. The default is 1, i.e. l1 norm. */
    private INIFileDouble m_OptDERegulPower;
    /** DE A probability to mutate certain value to zero. Useful if zero weights are wanted */
    private INIFileDouble m_OptDEProbMutationZero;
    /**
     * DE A reverse for the zeroing. A probability to mutate certain value to nonzero random value.
     * Could be used if zeroing is used.
     */
    private INIFileDouble m_OptDEProbMutationNonZero;

    // For all the optimization
    /** Optimization regularization parameter */
    private INIFileDouble m_OptRegPar;
    /** Optimization regularization parameter - number of zeroes. Especially useful for DE optimization. */
    private INIFileDouble m_OptNbZeroesPar;
    /** The treshold for rule weights. If weight < this, rule is removed. */
    private INIFileDouble m_OptRuleWeightThreshold;
    /** If this flag is set, weights higher than m_OptRuleWeightThreshold will get value 1, others will be set to 0 */
    protected INIFileBool m_OptRuleWeightBinarization;
    /** DE The loss function. The default is squared loss. */
    private INIFileNominal m_OptLossFunction;
    /** Optimization For Huber 1962 loss function an alpha value for outliers has to be given. */
    private INIFileDouble m_OptHuberAlpha;
    /**
     * Shift RULE predictions according to the default prediction. Should increase the accuracy. Default true.
     * The default prediction is based on statistical factors of TARGET ATTRIBUTES.
     * Linear terms are not touched (similar is done for them with m_OptNormalizeLinearTerms).
     */
    private INIFileBool m_OptDefaultShiftPred;
    /** Do we add the descriptive attributes as linear terms to rule set. Default No. */
    private INIFileNominal m_OptAddLinearTerms;
    /**
     * If linear terms are added, are they scaled so that each variable has similar effect.
     * The normalization is done via statistical factors of DESCRIPTIVE ATTRIBUTES.
     * You get similar effect by normalizing the data. Default Yes.
     */
    private INIFileNominal m_OptNormalizeLinearTerms;
    /**
     * If linear terms are added, are truncated so that they do not predict values greater or lower
     * than found in the training set. Default Yes.
     */
    private INIFileBool m_OptLinearTermsTruncate;
    /**
     * Do we omit the rule predictions such that the (single target) predictions are changed to 1. This does
     * not do anything to the linear terms. Default Yes.
     */
    private INIFileBool m_OptOmitRulePredictions;
    /**
     * Do we scale the predictions for optimization based on the coverage
     * This should put more weight to general rules. Default No.
     */
    private INIFileBool m_OptWeightGenerality;
    /**
     * Do we normalize the targets of predictions and true values internally for optimization.
     * The normalization is done with statistical factors of TARGET ATTRIBUTES.
     * This normalization is inverted after the optimization. On default YES, because
     * it should make at least GD optimization work better (covariance computing may not work otherwise).
     * This makes the error function very similar to RRMSE.
     * Alternatively YesVariance normalizes with variance.
     */
    // protected INIFileBool m_OptNormalization;
    private INIFileNominal m_OptNormalization;

    // Gradient descent optimization
    /** GD Maximum amount of iterations */
    private INIFileInt m_OptGDMaxIter;
    /**
     * GD Treshold [0,1] for changing the gradient. This portion of maximum gradients are affecting.
     * A value between [0,1].If 1 (default) this is simliar to L1 regularization (Lasso) and 0 similar to L2.
     */
    private INIFileDouble m_OptGDGradTreshold;
    /** GD Initial step size ]0,1] for each iteration. If m_OptGDIsDynStepsize is true, this is not used. */
    private INIFileDouble m_OptGDStepSize;
    /** GD Compute lower limit of step size based on the predictions. Default Yes. */
    private INIFileBool m_OptGDIsDynStepsize;
    /**
     * GD Maximum number of nonzero weights. If the number reached, only old ones are altered.
     * If = 0, no limit for nonzero weights.
     */
    private INIFileInt m_OptGDMaxNbWeights;
    /** GD User early stopping criteria for this amount of data. If 0, no early stopping used. */
    private INIFileDouble m_OptGDEarlyStopAmount;
    /**
     * GD Early stopping criteria treshold. Value should be greater than 1. Used at least
     * for stopping GD optimization for single T value. However, if OptGDEarlyTTryStop true, also
     * used for stopping to try new T values.
     */
    private INIFileDouble m_OptGDEarlyStopTreshold;
    /**
     * GD When early stopping is found, how many times we try to reduce the step size and try again
     * Default is Infinity. In this case we use all the iterations by reducing step size.
     */
    private INIFileStringOrInt m_OptGDNbOfStepSizeReduce;
    /** GD External binary, do we use GD or brute force method */
    private INIFileNominal m_OptGDExternalMethod;
    /** GD How to combine multiple targets to single gradient value for step taking */
    private INIFileNominal m_OptGDMTGradientCombine;
    /** GD How many different parameter combinations we try for T. Values between [m_OptGDGradTreshold,1] */
    private INIFileInt m_OptGDNbOfTParameterTry;
    /**
     * GD When running from T=1 down, do we stop if the error starts to increase. Should make optimization
     * a lot faster, but may decrease the accuracy. Default Yes.
     */
    private INIFileBool m_OptGDEarlyTTryStop;

    // Probabilistic Rule sampling
    /** Expected cardinality (excluding default rule) */
    private INIFileInt m_MaxRuleCardinality;
    /** Number of iterations in org.apache.commons.math4.distribution.PoissonDistribution */
    private INIFileInt m_MaxPoissonIterations;
    /** Number of generated rule sets */
    private INIFileInt m_NumberOfSampledRuleSets;
    /** Percentage of training data to use as validation set when selecting the best sampled rule set */
    private INIFileDouble m_ValidationSetPercentage;


    public INIFileNominalOrDoubleOrVector getDispersionWeights() {
        return m_DispersionWeights;
    }


    /**
     * Returns if random rules are wanted. That is RandomRules in settings file is above 0.
     */
    public boolean isRandomRules() {
        return (m_RandomRules.getValue() > 0);
    }


    /**
     * How many random rules are wanted by RandomRules in the settings file.
     */
    public int nbRandomRules() {
        return m_RandomRules.getValue();
    }


    public boolean isRuleWiseErrors() {
        return m_RuleWiseErrors.getValue();
    }


    public int getCoveringMethod() {
        return m_CoveringMethod.getValue();
    }


    public void setCoveringMethod(int method) {
        m_CoveringMethod.setSingleValue(method);
    }


    public int getRulePredictionMethod() {
        return m_PredictionMethod.getValue();
    }


    public boolean isRulePredictionOptimized() {
        return (getRulePredictionMethod() == RULE_PREDICTION_METHOD_OPTIMIZED
                || getRulePredictionMethod() == RULE_PREDICTION_METHOD_GD_OPTIMIZED
                || getRulePredictionMethod() == RULE_PREDICTION_METHOD_GD_OPTIMIZED_BINARY);
    }


    public void setRulePredictionMethod(int method) {
        m_PredictionMethod.setSingleValue(method);
    }


    public int getRuleAddingMethod() {
        return m_RuleAddingMethod.getValue();
    }


    public void setRuleAddingMethod(int method) {
        m_RuleAddingMethod.setSingleValue(method);
    }


    public double getCoveringWeight() {
        return m_CoveringWeight.getValue();
    }


    public void setCoveringWeight(double weight) {
        m_CoveringWeight.setValue(weight);
    }


    public double getInstCoveringWeightThreshold() {
        return m_InstCoveringWeightThreshold.getValue();
    }


    public void setInstCoveringWeightThreshold(double thresh) {
        m_InstCoveringWeightThreshold.setValue(thresh);
    }


    public int getInitialRuleGeneratingMethod() {
        return m_InitialRuleGeneratingMethod.getValue();
    }


    public void setInitialRuleGeneratingMethod(int method) {
        m_InitialRuleGeneratingMethod.setSingleValue(method);
    }


    public int getMaxRulesNb() {
        return m_MaxRulesNb.getValue();
    }


    public void setMaxRulesNb(int nb) {
        m_MaxRulesNb.setValue(nb);
    }


    public double getRuleSignificanceLevel() {
        return m_RuleSignificanceLevel.getValue();
    }


    public int getRuleNbSigAtt() {
        return m_RuleNbSigAtts.getValue();
    }


    public boolean isRuleSignificanceTesting() {
        return m_RuleNbSigAtts.getValue() != 0;
    }


    public double getHeurDispOffset() {
        return m_HeurDispOffset.getValue();
    }


    public double getHeurCoveragePar() {
        return m_HeurCoveragePar.getValue();
    }


    public double getHeurRuleDistPar() {
        return m_HeurRuleDistPar.getValue();
    }


    public void setHeurRuleDistPar(double value) {
        m_HeurRuleDistPar.setValue(value);
    }


    public boolean isHeurRuleDist() {
        return m_HeurRuleDistPar.getValue() > 0;
    }


    public boolean isWeightedCovering() {
        if (m_CoveringMethod.getValue() == COVERING_METHOD_WEIGHTED_ADDITIVE
                || m_CoveringMethod.getValue() == COVERING_METHOD_WEIGHTED_MULTIPLICATIVE
                || m_CoveringMethod.getValue() == COVERING_METHOD_WEIGHTED_ERROR) {
            return true;
        }
        else {
            return false;
        }
    }


    public double getHeurPrototypeDistPar() {
        return m_HeurPrototypeDistPar.getValue();
    }


    public void setHeurPrototypeDistPar(double value) {
        m_HeurPrototypeDistPar.setValue(value);
    }


    public boolean isHeurPrototypeDistPar() {
        return m_HeurPrototypeDistPar.getValue() > 0;
    }

    private boolean m_ruleInduceParamsDisabled = false;
    private double m_origHeurRuleDistPar = 0;
    private int m_origRulePredictionMethod = 0;
    private int m_origCoveringMethod = 0;


    public boolean getRuleInduceParamsDisabled() {
        return m_ruleInduceParamsDisabled;
    }


    /**
     * For forest induction, disable rule parameters that interfere. If you need to return the originals
     * use returnRuleInduceParams
     */
    public void disableRuleInduceParams() {
        if (!m_ruleInduceParamsDisabled) { // Make sure the original values are not lost.
            m_origHeurRuleDistPar = getHeurRuleDistPar();
            m_origRulePredictionMethod = getRulePredictionMethod();
            m_origCoveringMethod = getCoveringMethod();

            setHeurRuleDistPar(0.0);
            setRulePredictionMethod(RULE_PREDICTION_METHOD_DECISION_LIST);
            setCoveringMethod(COVERING_METHOD_RULES_FROM_TREE);
            m_ruleInduceParamsDisabled = true; // Mark that these are disabled
        }
    }


    /** For TreesToRules induction return the original parameters after forest induced */
    public void returnRuleInduceParams() {
        if (m_ruleInduceParamsDisabled) { // Only if they have been disabled previously
            setHeurRuleDistPar(m_origHeurRuleDistPar);
            setRulePredictionMethod(m_origRulePredictionMethod);
            setCoveringMethod(m_origCoveringMethod);
            m_ruleInduceParamsDisabled = false; // Mark that not anymore disabled
        }
    }


    public boolean computeDispersion() {
        return m_ComputeDispersion.getValue();
    }


    public double getVarBasedDispNormWeight() {
        return m_VarBasedDispNormWeight.getValue();
    }


    public boolean isConstrainedToFirstAttVal() {
        return m_constrainedToFirstAttVal.getValue();
    }


    public double getOptDECrossProb() {
        return m_OptDECrossProb.getValue();
    }


    public int getOptDENumEval() {
        return m_OptDENumEval.getValue();
    }


    public int getOptDEPopSize() {
        return m_OptDEPopSize.getValue();
    }


    public int getOptDESeed() {
        return m_OptDESeed.getValue();
    }


    public double getOptDEWeight() {
        return m_OptDEWeight.getValue();
    }


    /** Optimization regularization parameter */
    public double getOptRegPar() {
        return m_OptRegPar.getValue();
    }


    /** Optimization regularization parameter */
    public void setOptRegPar(double newValue) {
        m_OptRegPar.setValue(newValue);
    }


    /** Optimization regularization parameter - number of zeroes */
    public double getOptNbZeroesPar() {
        return m_OptNbZeroesPar.getValue();
    }


    /** Optimization regularization parameter - number of zeroes */
    public void setOptNbZeroesPar(double newValue) {
        m_OptNbZeroesPar.setValue(newValue);
    }


    public double getOptRuleWeightThreshold() {
        return m_OptRuleWeightThreshold.getValue();
    }


    /** If this flag is set, weights higher than m_OptRuleWeightThreshold will get value 1, others will be set to 0 */
    public boolean getOptRuleWeightBinarization() {
        return m_OptRuleWeightBinarization.getValue();
    }


    /** Shift predictions according to the default prediction. Should increase the accuracy */
    public boolean isOptDefaultShiftPred() {
        return m_OptDefaultShiftPred.getValue();
    }


    /** Do we add linear terms to rule set */
    public boolean isOptAddLinearTerms() {
        return (m_OptAddLinearTerms.getValue() != OPT_GD_ADD_LIN_NO);
    }


    /** How we add linear terms to rule set. Use memory saving? */
    public int getOptAddLinearTerms() {
        return m_OptAddLinearTerms.getValue();
    }


    /** Do we scale linear terms so that the attributes have similar effect */
    public boolean isOptNormalizeLinearTerms() {
        return (m_OptNormalizeLinearTerms.getValue() != OPT_LIN_TERM_NORM_NO);
    }


    /** What kind of normalization are we using */
    public int getOptNormalizeLinearTerms() {
        return m_OptNormalizeLinearTerms.getValue();
    }


    /**
     * Are linear terms truncated so that they do not predict values greater or lower
     * than found in the training set.
     */
    public boolean isOptLinearTermsTruncate() {
        return m_OptLinearTermsTruncate.getValue();
    }


    /** Do we omit rule predictions */
    public boolean isOptOmitRulePredictions() {
        return m_OptOmitRulePredictions.getValue();
    }


    /**
     * Do we scale the predictions of the rules with the generality. This puts more weight to general rules
     */
    public boolean isOptWeightGenerality() {
        return m_OptWeightGenerality.getValue();
    }


    /** Do we normalize the predictions and true values internally for optimization. */
    public boolean isOptNormalization() {
        return m_OptNormalization.getValue() != OPT_NORMALIZATION_NO;
    }


    /** How we normalize the predictions and true values internally for optimization. */
    public int getOptNormalization() {
        return m_OptNormalization.getValue();
    }


    /** Type of Loss function for DE optimization */
    public int getOptDELossFunction() {
        return m_OptLossFunction.getValue();
    }


    /** Power for regularization parameter */
    public double getOptDERegulPower() {
        return m_OptDERegulPower.getValue();
    }


    /** DE A probability to mutate certain value to zero. Useful if zero weights are wanted */
    public double getOptDEProbMutationZero() {
        return m_OptDEProbMutationZero.getValue();
    }


    /**
     * DE A reverse for the zeroing. A probability to mutate certain value to nonzero random value.
     * Could be used if zeroing is used.
     */
    public double getOptDEProbMutationNonZero() {
        return m_OptDEProbMutationNonZero.getValue();
    }


    /** For Huber 1962 loss function an alpha value for outliers has to be given. */
    public double getOptHuberAlpha() {
        return m_OptHuberAlpha.getValue();
    }


    /** GD Maximum amount of iterations */
    public int getOptGDMaxIter() {
        return m_OptGDMaxIter.getValue();
    }


    /** GD The used loss function */
    public int getOptGDLossFunction() {
        return m_OptLossFunction.getValue();
    }


    /**
     * GD Treshold [0,1] for changing the gradient. This portion of maximum gradients are affecting.
     * This can be considered as the regularization parameter, if this is 1 it is similar to L1 (Lasso) penalty.
     */
    public double getOptGDGradTreshold() {
        return m_OptGDGradTreshold.getValue();
    }


    /**
     * GD Treshold [0,1] for changing the gradient. This portion of maximum gradients are affecting.
     * This can be considered as the regularization parameter, if this is 1 it is similar to L1 (Lasso) penalty.
     */
    public void setOptGDGradTreshold(double newVal) {
        m_OptGDGradTreshold.setValue(newVal);
    }


    /** GD Step size ]0,1] for each iteration. */
    public double getOptGDStepSize() {
        return m_OptGDStepSize.getValue();
    }


    /** GD Step size ]0,1] for each iteration. */
    public boolean isOptGDIsDynStepsize() {
        return m_OptGDIsDynStepsize.getValue();
    }


    /** Amount of data used for early stopping check. If zero, not used. */
    public double getOptGDEarlyStopAmount() {
        return m_OptGDEarlyStopAmount.getValue();
    }


    /** Early stopping criterion treshold */
    public double getOptGDEarlyStopTreshold() {
        return m_OptGDEarlyStopTreshold.getValue();
    }


    /**
     * GD Maximum number of nonzero weights. If the number reached, only old ones are altered.
     * If = 0, no limit for nonzero weights.
     */
    public int getOptGDMaxNbWeights() {
        return m_OptGDMaxNbWeights.getValue();
    }


    /**
     * GD Maximum number of nonzero weights. If the number reached, only old ones are altered.
     * If = 0, no limit for nonzero weights.
     */
    public void setOptGDMaxNbWeights(int nbWeights) {
        m_OptGDMaxNbWeights.setValue(nbWeights);
    }


    /**
     * GD When early stopping is found, how many times we try to reduce the step size and try again
     * Default is 0, but can be Infinity. In this case we use all the iterations by reducing step size.
     */
    public int getOptGDNbOfStepSizeReduce() {
        if (m_OptGDNbOfStepSizeReduce.isString(INFINITY_STRING))
            return Integer.MAX_VALUE;
        else
            return m_OptGDNbOfStepSizeReduce.getIntValue();
    }


    /** What method we use for external GD optimization algorithm */
    public int getOptGDExternalMethod() {
        return m_OptGDExternalMethod.getValue();
    }


    /** GD How to combine multiple targets to single gradient value for step taking */
    public int getOptGDMTGradientCombine() {
        return m_OptGDMTGradientCombine.getValue();
    }


    /** GD How many different parameter combinations we try for T. Values between [T,1] */
    public int getOptGDNbOfTParameterTry() {
        return m_OptGDNbOfTParameterTry.getValue();
    }


    /**
     * GD When running from T=1 down, do we stop if the error starts to increase. Should make optimization
     * a lot faster, but may decrease the accuracy.
     */
    public boolean getOptGDEarlyTTryStop() {
        return m_OptGDEarlyTTryStop.getValue();
    }


    /** Expected cardinality (excluding default rule) */
    public int getMaxRuleCardinality() {
        return m_MaxRuleCardinality.getValue();
    }


    /** Number of iterations in org.apache.commons.math4.distribution.PoissonDistribution */
    public int getMaxPoissonIterations() {
        return m_MaxPoissonIterations.getValue();
    }


    /** Number of generated rule sets */
    public int getNumberOfSampledRuleSets() {
        return m_NumberOfSampledRuleSets.getValue();
    }


    /** Percentage of training data to use as validation set when selecting the best sampled rule set */
    public Double getValidationSetPercentage() {
        return m_ValidationSetPercentage.getValue();
    }


    public void setIS_RULE_SIG_TESTING(boolean value) {
        IS_RULE_SIG_TESTING = value;
    }


    public boolean getIS_RULE_SIG_TESTING() {
        return IS_RULE_SIG_TESTING;
    }


    @Override
    public INIFileSection create() {
        m_SectionRules = new INIFileSection("Rules");
        m_SectionRules.addNode(m_CoveringMethod = new INIFileNominal("CoveringMethod", COVERING_METHODS, COVERING_METHOD_STANDARD));
        m_SectionRules.addNode(m_PredictionMethod = new INIFileNominal("PredictionMethod", RULE_PREDICTION_METHODS, RULE_PREDICTION_METHOD_DECISION_LIST));
        m_SectionRules.addNode(m_RuleAddingMethod = new INIFileNominal("RuleAddingMethod", RULE_ADDING_METHODS, RULE_ADDING_METHOD_ALWAYS));
        m_SectionRules.addNode(m_CoveringWeight = new INIFileDouble("CoveringWeight", 0.1));
        m_SectionRules.addNode(m_InstCoveringWeightThreshold = new INIFileDouble("InstCoveringWeightThreshold", 0.1));
        m_SectionRules.addNode(m_MaxRulesNb = new INIFileInt("MaxRulesNb", 1000));
        m_SectionRules.addNode(m_HeurDispOffset = new INIFileDouble("HeurDispOffset", 0.0));
        m_SectionRules.addNode(m_HeurCoveragePar = new INIFileDouble("HeurCoveragePar", 1.0));
        m_SectionRules.addNode(m_HeurRuleDistPar = new INIFileDouble("HeurRuleDistPar", 0.0));
        m_SectionRules.addNode(m_InitialRuleGeneratingMethod = new INIFileNominal("InitialRuleGeneratingMethod", INITIAL_RULE_GENERATING_METHODS, INITIAL_RULE_GENERATING_METHOD_RANDOM_FOREST));
        m_SectionRules.addNode(m_HeurPrototypeDistPar = new INIFileDouble("HeurPrototypeDistPar", 0.0));
        m_SectionRules.addNode(m_RuleSignificanceLevel = new INIFileDouble("RuleSignificanceLevel", 0.05));
        m_SectionRules.addNode(m_RuleNbSigAtts = new INIFileInt("RuleNbSigAtts", 0));
        m_SectionRules.addNode(m_ComputeDispersion = new INIFileBool("ComputeDispersion", false));
        m_SectionRules.addNode(m_VarBasedDispNormWeight = new INIFileDouble("VarBasedDispNormWeight", 4.0));
        m_SectionRules.addNode(m_DispersionWeights = new INIFileNominalOrDoubleOrVector("DispersionWeights", EMPTY));
        m_DispersionWeights.setArrayIndexNames(SettingsAttribute.NUM_NOM_TAR_NTAR_WEIGHTS);
        m_DispersionWeights.setDoubleArray(FOUR_ONES);
        m_DispersionWeights.setArrayIndexNames(true);
        m_SectionRules.addNode(m_RandomRules = new INIFileInt("RandomRules", 0));
        m_SectionRules.addNode(m_RuleWiseErrors = new INIFileBool("PrintRuleWiseErrors", false));
        m_SectionRules.addNode(m_PrintAllRules = new INIFileBool("PrintAllRules", true));
        m_SectionRules.addNode(m_constrainedToFirstAttVal = new INIFileBool("ConstrainedToFirstAttVal", false));
        m_SectionRules.addNode(m_OptDEPopSize = new INIFileInt("OptDEPopSize", 500));
        m_SectionRules.addNode(m_OptDENumEval = new INIFileInt("OptDENumEval", 10000));
        m_SectionRules.addNode(m_OptDECrossProb = new INIFileDouble("OptDECrossProb", 0.3));
        m_SectionRules.addNode(m_OptDEWeight = new INIFileDouble("OptDEWeight", 0.5));
        m_SectionRules.addNode(m_OptDESeed = new INIFileInt("OptDESeed", 0));
        m_SectionRules.addNode(m_OptDERegulPower = new INIFileDouble("OptDERegulPower", 1.0));
        m_SectionRules.addNode(m_OptDEProbMutationZero = new INIFileDouble("OptDEProbMutationZero", 0.0));
        m_SectionRules.addNode(m_OptDEProbMutationNonZero = new INIFileDouble("OptDEProbMutationNonZero", 0.0));
        m_SectionRules.addNode(m_OptRegPar = new INIFileDouble("OptRegPar", 0.0));
        m_SectionRules.addNode(m_OptNbZeroesPar = new INIFileDouble("OptNbZeroesPar", 0.0));
        m_SectionRules.addNode(m_OptRuleWeightThreshold = new INIFileDouble("OptRuleWeightThreshold", 0.1));
        m_SectionRules.addNode(m_OptRuleWeightBinarization = new INIFileBool("OptRuleWeightBinarization", false));
        m_SectionRules.addNode(m_OptLossFunction = new INIFileNominal("OptDELossFunction", OPT_LOSS_FUNCTIONS, OPT_LOSS_FUNCTIONS_SQUARED));
        m_SectionRules.addNode(m_OptDefaultShiftPred = new INIFileBool("OptDefaultShiftPred", true));
        m_SectionRules.addNode(m_OptAddLinearTerms = new INIFileNominal("OptAddLinearTerms", OPT_GD_ADD_LINEAR_TERMS, OPT_GD_ADD_LIN_NO));
        m_SectionRules.addNode(m_OptNormalizeLinearTerms = new INIFileNominal("OptNormalizeLinearTerms", OPT_LINEAR_TERM_NORM_VALUES, OPT_LIN_TERM_NORM_YES));
        m_SectionRules.addNode(m_OptLinearTermsTruncate = new INIFileBool("OptLinearTermsTruncate", true));
        m_SectionRules.addNode(m_OptOmitRulePredictions = new INIFileBool("OptOmitRulePredictions", true));
        m_SectionRules.addNode(m_OptWeightGenerality = new INIFileBool("OptWeightGenerality", false));
        // m_SectionRules.addNode(m_OptNormalization = new INIFileBool("OptNormalization", true));
        m_SectionRules.addNode(m_OptNormalization = new INIFileNominal("OptNormalization", OPT_NORMALIZATION, OPT_NORMALIZATION_YES));
        m_SectionRules.addNode(m_OptHuberAlpha = new INIFileDouble("OptHuberAlpha", 0.9));
        m_SectionRules.addNode(m_OptGDMaxIter = new INIFileInt("OptGDMaxIter", 1000));
        // m_SectionRules.addNode(m_OptGDLossFunction = new INIFileNominal("OptGDLossFunction", GD_LOSS_FUNCTIONS, 0));
        m_SectionRules.addNode(m_OptGDGradTreshold = new INIFileDouble("OptGDGradTreshold", 1));
        m_SectionRules.addNode(m_OptGDStepSize = new INIFileDouble("OptGDStepSize", 0.1));
        m_SectionRules.addNode(m_OptGDIsDynStepsize = new INIFileBool("OptGDIsDynStepsize", true));
        m_SectionRules.addNode(m_OptGDMaxNbWeights = new INIFileInt("OptGDMaxNbWeights", 0));
        m_SectionRules.addNode(m_OptGDEarlyStopAmount = new INIFileDouble("OptGDEarlyStopAmount", 0.0));
        m_SectionRules.addNode(m_OptGDEarlyStopTreshold = new INIFileDouble("OptGDEarlyStopTreshold", 1.1));
        m_SectionRules.addNode(m_OptGDNbOfStepSizeReduce = new INIFileStringOrInt("OptGDNbOfStepSizeReduce", INFINITY_STRING));
        m_SectionRules.addNode(m_OptGDExternalMethod = new INIFileNominal("OptGDExternalMethod", GD_EXTERNAL_METHOD_VALUES, GD_EXTERNAL_METHOD_GD));
        m_SectionRules.addNode(m_OptGDMTGradientCombine = new INIFileNominal("OptGDMTGradientCombine", OPT_GD_MT_COMBINE_GRADIENTS, OPT_GD_MT_GRADIENT_AVG));
        m_SectionRules.addNode(m_OptGDNbOfTParameterTry = new INIFileInt("OptGDNbOfTParameterTry", 1));
        m_SectionRules.addNode(m_OptGDEarlyTTryStop = new INIFileBool("OptGDEarlyTTryStop", true));

        m_SectionRules.addNode(m_MaxRuleCardinality = new INIFileInt("MaxRuleCardinality", 30));
        m_SectionRules.addNode(m_MaxPoissonIterations = new INIFileInt("MaxPoissonIterations", 1000));
        m_SectionRules.addNode(m_NumberOfSampledRuleSets = new INIFileInt("NumberOfSampledRuleSets", 100));
        m_SectionRules.addNode(m_ValidationSetPercentage = new INIFileDouble("ValidationSetPercentage", 0.33));

        m_SectionRules.setEnabled(false);

        return m_SectionRules;
    }
}
