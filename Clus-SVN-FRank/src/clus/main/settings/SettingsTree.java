
package clus.main.settings;

import clus.heuristic.FTest;
import clus.util.jeans.io.ini.INIFileBool;
import clus.util.jeans.io.ini.INIFileDouble;
import clus.util.jeans.io.ini.INIFileInt;
import clus.util.jeans.io.ini.INIFileNominal;
import clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import clus.util.jeans.io.ini.INIFileSection;
import clus.util.jeans.io.range.IntRangeCheck;


public class SettingsTree implements ISettings {

    /***********************************************************************
     * Section: Tree *
     ***********************************************************************/

    private final String[] TREE_OPTIMIZE_VALUES = { "NoClusteringStats", "NoInodeStats" };
    public final static int[] TREE_OPTIMIZE_NONE = {};
    public final static int TREE_OPTIMIZE_NO_CLUSTERING_STATS = 0;
    public final static int TREE_OPTIMIZE_NO_INODE_STATS = 1;

    //@!TODO
    /* consider creating new section Structured Data for heuristic and structured data distance measures */
    // TODO: good suggestion because this is a mess; martinb
    private final String[] HEURISTIC_COMPLEXITY = { "N2", "LOG", "LINEAR", "NPAIRS", "TEST" };

    // Added by Eduardo Costa - 06/06/2011

    private final String[] INDUCTION_ORDER = { "DepthFirst", "BestFirst" };
    public final static int DEPTH_FIRST = 0;
    public final static int BEST_FIRST = 1;

    private INIFileNominal m_InductionOrder;

    // end block added by Eduardo

    // Added by Eduardo Costa - 27/09/2011

    private final String[] ENTROPY_TYPE = { "StandardEntropy", "ModifiedEntropy" };
    public final static int STANDARD_ENTROPY = 0;
    public final static int MODIFIED_ENTROPY = 1;

    private INIFileNominal m_EntropyType;

    private INIFileBool m_ConsiderUnlableInstancesInIGCalc;

    // end block added by Eduardo

    private INIFileSection m_SectionTree;
    private INIFileNominal m_Heuristic;
    private INIFileNominal m_SetDistance;
    private INIFileNominal m_TupleDistance;
    private INIFileNominal m_TSDistance;
    private INIFileNominal m_HeuristicComplexity;

    private INIFileBool m_BinarySplit;
    private INIFileBool m_AlternativeSplits;
    private INIFileNominalOrDoubleOrVector m_FTest;
    private INIFileNominal m_PruningMethod;
    private INIFileBool m_1SERule;
    private INIFileBool m_MSENominal;
    private INIFileDouble m_M5PruningMult;
    /** Do we transform leaves or all nodes of tree to rules */
    private INIFileNominal m_RulesFromTree;
    private INIFileNominal m_TreeOptimize;

    private INIFileNominal m_SpatialMatrix;
    private INIFileNominal m_SpatialMeasure;
    private INIFileDouble m_Bandwidth;
    private INIFileBool m_Longlat;
    private INIFileDouble m_NeighCount;
    private INIFileDouble m_SpatialAlpha;
    private INIFileInt m_TreeSplitSampling; // Amount of datapoints to include for calculating split heuristic.  Datapoints will be selected randomly.

    public static double ALPHA; // danielae

    //added by Jurica Levatic, JSI
    private INIFileNominal m_MissingClusteringAttrHandling; //determines how we handle the case where when searching evaluating candidate split all examples have only missing values for a clustering attriute, in one of the branches 
    private final String[] MISSING_CLUSTERING_ATTR_HANDLING_TYPE = { "Ignore", "EstimateFromTrainingSet", "EstimateFromParentNode" };
    private INIFileNominal m_MissingTargetAttrHandling; //determines how we calculate prototype (i.e., prediction) if all the tuple in leaf node have only missing values for target attriute 

    private final String[] MISSING_TARGET_ATTR_HANDLING_TYPE = { "Zero", "DefaultModel", "ParentNode" };
    public static final int MISSING_ATTRIBUTE_HANDLING_PARENT = 2; //variance and mean (i.e., prediction) will be estimated on the basis of the node's parent
    public static final int MISSING_ATTRIBUTE_HANDLING_TRAINING = 1; //variance and mean (i.e., prediction) will be estimated on the basis of the root node (i.e., default model)
    public static final int MISSING_ATTRIBUTE_HANDLING_NONE = 0; //variance will not be estimated, while mean (i.e., prediction) will be equal to zero


    public int getMissingClusteringAttrHandling() {
        return m_MissingClusteringAttrHandling.getValue();
    }


    public int getMissingTargetAttrHandling() {
        return m_MissingTargetAttrHandling.getValue();
    }


    public int getSpatialMatrix() {
        return m_SpatialMatrix.getValue();
    }


    public int getSpatialMeasure() {
        return m_SpatialMeasure.getValue();
    }


    public double getBandwidth() {
        return m_Bandwidth.getValue();
    }


    public boolean isLonglat() {
        return m_Longlat.getValue();
    }


    public double getNumNeightbours() {
        return m_NeighCount.getValue();
    }


    public double getSpatialAlpha() {
        return m_SpatialAlpha.getValue();
    }


    public void setSpatialMeasure(int method) {
        m_SpatialMeasure.setSingleValue(method);
    }


    public void setSpatialMatrix(int method) {
        m_SpatialMatrix.setSingleValue(method);
    }


    public int getHeuristicComplexity() {
        return m_HeuristicComplexity.getValue();
    }


    public void setSectionTreeEnabled(boolean enable) {
        m_SectionTree.setEnabled(enable);
    }


    public int getHeuristic() {
        return m_Heuristic.getValue();
    }


    public void setHeuristic(int value) {
        m_Heuristic.setSingleValue(value);
    }


    public boolean checkHeuristic(String value) {
        return m_Heuristic.getStringSingle().equals(value);
    }


    public void setTSDistance(int value) {
        m_TSDistance.setSingleValue(value);
    }


    public int getTSDistance() {
        return m_TSDistance.getValue();
    }


    public int getSetDistance() {
        return m_SetDistance.getValue();
    }


    public void setSetDistance(int value) {
        m_SetDistance.setSingleValue(value);
    }


    public boolean checkSetDistance(String value) {
        return m_SetDistance.getStringSingle().equals(value);
    }


    public int getTupleDistance() {
        return m_TupleDistance.getValue();
    }


    public void setTupleDistance(int value) {
        m_TupleDistance.setSingleValue(value);
    }


    public boolean checkTupleDistance(String value) {
        return m_TupleDistance.getStringSingle().equals(value);
    }


    // added by Eduardo
    public int getInductionOrder() {
        return m_InductionOrder.getValue();
    }


    public void setInductionOrder(int value) {
        m_InductionOrder.setSingleValue(value);
    }


    public boolean checkInductionOrder(String value) {
        return m_InductionOrder.getStringSingle().equals(value);
    }
    // end block added by Eduardo


    // added by Eduardo - 27/09/2011
    public int getEntropyType() {
        return m_EntropyType.getValue();
    }


    public void setEntropyType(int value) {
        m_EntropyType.setSingleValue(value);
    }


    public boolean checkEntropyType(String value) {
        return m_EntropyType.getStringSingle().equals(value);
    }


    public boolean considerUnlableInstancesInIGCalc() {
        return m_ConsiderUnlableInstancesInIGCalc.getValue();
    }

    // end block added by Eduardo


    /**
     * To find the best split, heuristic can be calculated on a
     * random sample of the training set to conserve time
     * 
     * @return the size the random sample should be
     */
    public int getTreeSplitSampling() {
        return m_TreeSplitSampling.getValue();
    }


    /**
     * To find the best split, heuristic can be calculated on a
     * random sample of the training set to conserve time
     * 
     * @param value
     *        the size the random sample should be
     */
    public void setTreeSplitSampling(int value) {
        m_TreeSplitSampling.setValue(value);
    }


    public boolean isBinarySplit() {
        return m_BinarySplit.getValue();
    }


    public boolean showAlternativeSplits() {
        return m_AlternativeSplits.getValue();
    }


    public INIFileNominalOrDoubleOrVector getFTestArray() {
        return m_FTest;
    }


    public double getFTest() {
        return m_FTest.getDouble();
    }


    public void setFTest(double ftest, int verbosityLevel) {
        FTEST_VALUE = ftest;
        FTEST_LEVEL = FTest.getLevelAndComputeArray(ftest, verbosityLevel);
        m_FTest.setDouble(ftest);
    }


    public int getPruningMethod() {
        return m_PruningMethod.getValue();
    }


    public void setPruningMethod(int method) {
        m_PruningMethod.setSingleValue(method);
    }


    public String getPruningMethodName() {
        return m_PruningMethod.getStringValue();
    }


    public boolean get1SERule() {
        return m_1SERule.getValue();
    }


    public boolean isMSENominal() {
        return m_MSENominal.getValue();
    }


    public double getM5PruningMult() {
        return m_M5PruningMult.getValue();
    }


    public void setTreeSplitSamplingNamedValue(int value, String name) {
        m_TreeSplitSampling.setNamedValue(value, name);
    }


    /**
     * If we transform the induced trees to rules.
     */
    public int rulesFromTree() {
        return m_RulesFromTree.getValue();
    }


    public boolean hasTreeOptimize(int value) {
        return m_TreeOptimize.contains(value);
    }

    /***********************************************************************
     * Section: Tree - Heuristic *
     ***********************************************************************/

    public final static String[] HEURISTICS = { "Default", "ReducedError", "Gain", "GainRatio", "SSPD", "VarianceReduction", "MEstimate", "Morishita", "DispersionAdt", "DispersionMlt", "RDispersionAdt", "RDispersionMlt", "GeneticDistance", "SemiSupervised", "VarianceReductionMissing", "StructuredData", "VarianceReductionGIS" };

    public final static int HEURISTIC_DEFAULT = 0;
    public final static int HEURISTIC_REDUCED_ERROR = 1;
    public final static int HEURISTIC_GAIN = 2;
    public final static int HEURISTIC_GAIN_RATIO = 3;

    public final static int HEURISTIC_SSPD = 4;
    /** Sum of Squared Distances, the default for ensemble tree regression learning */
    public final static int HEURISTIC_VARIANCE_REDUCTION = 5;
    public final static int HEURISTIC_MESTIMATE = 6;
    public final static int HEURISTIC_MORISHITA = 7;
    public final static int HEURISTIC_DISPERSION_ADT = 8;
    public final static int HEURISTIC_DISPERSION_MLT = 9;
    public final static int HEURISTIC_R_DISPERSION_ADT = 10;
    public final static int HEURISTIC_R_DISPERSION_MLT = 11;
    public final static int HEURISTIC_GENETIC_DISTANCE = 12;
    public final static int HEURISTIC_SEMI_SUPERVISED = 13;
    public final static int HEURISTIC_SS_REDUCTION_MISSING = 14;
    public final static int HEURISTIC_VARIANCE_REDUCTION_GIS = 15;

    // SPATIAL
    private final String[] SPATIAL_MATRIX_TYPE = { "Binary", "Euclidean", "Modified", "Gaussian" };
    public final static int SPATIAL_MATRIX_BINARY = 0;
    public final static int SPATIAL_MATRIX_EUCLIDEAN = 1;
    public final static int SPATIAL_MATRIX_MODIFIED = 2;
    public final static int SPATIAL_MATRIX_GAUSSIAN = 3;

    private final String[] SPATIAL_MEASURE_TYPE = { "GlobalMoran", "GlobalGeary", "GlobalGetis", "LocalMoran", "LocalGeary", "LocalGetis", "StandardizedGetis", "EquvalentI", "IwithNeighbours", "EquvalentIwithNeighbours", "GlobalMoranDistance", "GlobalGearyDistance", "CI", "MultiVariateMoranI", "CwithNeighbours", "Lee", "MultiIwithNeighbours", "CIwithNeighbours", "LeewithNeighbours", "Pearson", "CIDistance", "DH", "EquvalentIDistance", "PearsonDistance", "EquvalentG", "EquvalentGDistance", "EquvalentPDistance" };

    public final static int SPATIAL_MEASURE_GLOBAL_MORAN = 0;
    public final static int SPATIAL_MEASURE_GLOBAL_GEARY = 1;
    public final static int SPATIAL_MEASURE_GLOBAL_GETIS = 2;
    public final static int SPATIAL_MEASURE_LOCAL_MORAN = 3;
    public final static int SPATIAL_MEASURE_LOCAL_GEARY = 4;
    public final static int SPATIAL_MEASURE_LOCAL_GETIS = 5;
    public final static int SPATIAL_MEASURE_LOCAL_GETIS_STANDARDIZED = 6;
    public final static int SPATIAL_MEASURE_EQUVALENT_I = 7;
    public final static int SPATIAL_MEASURE_I_WITH_NEGHBOURS = 8;
    public final static int SPATIAL_MEASURE_EQUVALENT_I_WITH_NEIGHBOURS = 9;
    public final static int SPATIAL_MEASURE_GLOBAL_MORAN_DISTANCE = 10;
    public final static int SPATIAL_MEASURE_GLOBAL_GEARY_DISTANCE = 11;
    public final static int SPATIAL_MEASURE_CI = 12;
    public final static int SPATIAL_MEASURE_MULTIVARIATE_MORAN_I = 13;
    public final static int SPATIAL_MEASURE_C_WITH_NEIGHBOURS = 14;
    public final static int SPATIAL_MEASURE_LEE = 15;
    public final static int SPATIAL_MEASURE_MULTI_I_WITH_NEIGHBOURS = 16;
    public final static int SPATIAL_MEASURE_CI_WITH_NEIGHBOURS = 17;
    public final static int SPATIAL_MEASURE_LEE_WITH_NEIGHBOURS = 18;
    public final static int SPATIAL_MEASURE_PEARSON = 19;
    public final static int SPATIAL_MEASURE_CI_DISTANCE = 20;
    public final static int SPATIAL_MEASURE_DH = 21;
    public final static int SPATIAL_MEASURE_EQUVALENT_I_DISTANCE = 22;
    public final static int SPATIAL_MEASURE_PEARSON_DISTANCE = 23;
    public final static int SPATIAL_MEASURE_EQUVALENT_G = 24;
    public final static int SPATIAL_MEASURE_EQUVALENT_G_DISTANCE = 25;
    public final static int SPATIAL_MEASURE_EQUVALENT_P_DISTANCE = 26;

    public static int FTEST_LEVEL;
    public static double FTEST_VALUE;
    public static double MINIMAL_WEIGHT;
    public static boolean ONE_NOMINAL = true;
    public final static double BANDWIDTH = 0.01;
    public final static double NumNeightbours = 0.0;
    public static boolean LONGLAT = false;

    /***********************************************************************
     * Section: Tree - SetDistance *
     ***********************************************************************/

    private final String[] SETDISTANCES = { "GSMDistance", "HammingLoss", "Jaccard", "Matching", "Euclidean" };

    public final static int SETDISTANCES_GSM = 0;
    public final static int SETDISTANCES_HAMMING = 1;
    public final static int SETDISTANCES_JACCARD = 2;
    public final static int SETDISTANCES_MATCHING = 3;
    public final static int SETDISTANCES_EUCLIDEAN = 4;

    /***********************************************************************
     * Section: Tree - TupleDistance *
     ***********************************************************************/

    private final String[] TUPLEDISTANCES = { "Euclidean", "Minkowski" };
    public final static int TUPLEDISTANCES_EUCLIDEAN = 0;
    public final static int TUPLEDISTANCES_MINKOWSKI = 1;

    /***********************************************************************
     * Section: Tree - TimeSeriesDistance *
     ***********************************************************************/

    private final String[] TIME_SERIES_DISTANCE_MEASURE = { "DTW", "QDM", "TSC" };

    public final static int TIME_SERIES_DISTANCE_MEASURE_DTW = 0;
    public final static int TIME_SERIES_DISTANCE_MEASURE_QDM = 1;
    public final static int TIME_SERIES_DISTANCE_MEASURE_TSC = 2;

    /***********************************************************************
     * Section: Tree - Pruning method *
     ***********************************************************************/

    private final String[] PRUNING_METHODS = { "Default", "None", "C4.5", "M5", "M5Multi", "ReducedErrorVSB", "Garofalakis", "GarofalakisVSB", "CartVSB", "CartMaxSize", "EncodingCost", "CategoryUtility" };

    public final static int PRUNING_METHOD_DEFAULT = 0;
    public final static int PRUNING_METHOD_NONE = 1;
    public final static int PRUNING_METHOD_C45 = 2;
    public final static int PRUNING_METHOD_M5 = 3;
    public final static int PRUNING_METHOD_M5_MULTI = 4;
    public final static int PRUNING_METHOD_REDERR_VSB = 5;
    public final static int PRUNING_METHOD_GAROFALAKIS = 6;
    public final static int PRUNING_METHOD_GAROFALAKIS_VSB = 7;
    public final static int PRUNING_METHOD_CART_VSB = 8;
    public final static int PRUNING_METHOD_CART_MAXSIZE = 9;
    public final static int PRUNING_METHOD_ENCODING_COST = 10;
    public final static int PRUNING_METHOD_CATEGORY_UTILITY = 11;


    public void setM5PruningMultEnabled(boolean value) {
        m_M5PruningMult.setEnabled(value);
    }


    public void set1SERuleEnabled(boolean value) {
        m_1SERule.setEnabled(value);
    }


    public void setFTestEnabled(boolean value) {
        m_FTest.setEnabled(value);
    }


    @Override
    public INIFileSection create() {
        m_SectionTree = new INIFileSection("Tree");
        m_SectionTree.addNode(m_Heuristic = new INIFileNominal("Heuristic", HEURISTICS, HEURISTIC_DEFAULT));

        m_SectionTree.addNode(m_HeuristicComplexity = new INIFileNominal("HeuristicComlexity", HEURISTIC_COMPLEXITY, 0));
        m_SectionTree.addNode(m_SetDistance = new INIFileNominal("SetDistance", SETDISTANCES, SETDISTANCES_GSM));
        m_SectionTree.addNode(m_TupleDistance = new INIFileNominal("TupleDistance", TUPLEDISTANCES, TUPLEDISTANCES_EUCLIDEAN));
        m_SectionTree.addNode(m_TSDistance = new INIFileNominal("TSDistance", TIME_SERIES_DISTANCE_MEASURE, TIME_SERIES_DISTANCE_MEASURE_DTW));

        m_SectionTree.addNode(m_PruningMethod = new INIFileNominal("PruningMethod", PRUNING_METHODS, PRUNING_METHOD_DEFAULT));
        m_SectionTree.addNode(m_M5PruningMult = new INIFileDouble("M5PruningMult", 2.0));
        m_SectionTree.addNode(m_1SERule = new INIFileBool("1-SE-Rule", false));
        m_SectionTree.addNode(m_FTest = new INIFileNominalOrDoubleOrVector("FTest", NONELIST));
        m_FTest.setDouble(1.0);
        m_SectionTree.addNode(m_BinarySplit = new INIFileBool("BinarySplit", true));
        m_SectionTree.addNode(m_RulesFromTree = new INIFileNominal("ConvertToRules", SettingsOutput.CONVERT_RULES, SettingsOutput.CONVERT_RULES_NONE));
        m_SectionTree.addNode(m_AlternativeSplits = new INIFileBool("AlternativeSplits", false));
        m_SectionTree.addNode(m_TreeOptimize = new INIFileNominal("Optimize", TREE_OPTIMIZE_VALUES, TREE_OPTIMIZE_NONE));
        m_SectionTree.addNode(m_MSENominal = new INIFileBool("MSENominal", false));
        m_SectionTree.addNode(m_TreeSplitSampling = new INIFileInt("SplitSampling", 0));
        m_TreeSplitSampling.setValueCheck(new IntRangeCheck(0, Integer.MAX_VALUE));

        //added by Jurica Levatic, JSI
        m_SectionTree.addNode(m_MissingClusteringAttrHandling = new INIFileNominal("MissingClusteringAttrHandling", MISSING_CLUSTERING_ATTR_HANDLING_TYPE, MISSING_ATTRIBUTE_HANDLING_PARENT));
        m_SectionTree.addNode(m_MissingTargetAttrHandling = new INIFileNominal("MissingTargetAttrHandling", MISSING_TARGET_ATTR_HANDLING_TYPE, MISSING_ATTRIBUTE_HANDLING_PARENT));
        
        // added by Eduardo Costa 06/06/2011
        m_SectionTree.addNode(m_InductionOrder = new INIFileNominal("InductionOrder", INDUCTION_ORDER, DEPTH_FIRST));

        // added by Eduardo Costa 29/09/2011
        m_SectionTree.addNode(m_EntropyType = new INIFileNominal("EntropyType", ENTROPY_TYPE, STANDARD_ENTROPY));
        m_SectionTree.addNode(m_ConsiderUnlableInstancesInIGCalc = new INIFileBool("ConsiderUnlableInstancesInIGCalc", false));

        m_SectionTree.addNode(m_SpatialMatrix = new INIFileNominal("SpatialMatrix", SPATIAL_MATRIX_TYPE, SPATIAL_MATRIX_BINARY));
        m_SectionTree.addNode(m_SpatialMeasure = new INIFileNominal("SpatialMeasure", SPATIAL_MEASURE_TYPE, SPATIAL_MEASURE_GLOBAL_MORAN));
        m_SectionTree.addNode(m_Bandwidth = new INIFileDouble("Bandwidth", 0.001));
        m_SectionTree.addNode(m_Longlat = new INIFileBool("Longlat", false));
        m_SectionTree.addNode(m_NeighCount = new INIFileDouble("NumNeightbours", 0.0));
        m_SectionTree.addNode(m_SpatialAlpha = new INIFileDouble("Alpha", 1.0));

        return m_SectionTree;
    }

}
