
package clus.main.settings;

import clus.heuristic.FTest;
import clus.jeans.io.ini.INIFileBool;
import clus.jeans.io.ini.INIFileDouble;
import clus.jeans.io.ini.INIFileInt;
import clus.jeans.io.ini.INIFileNominal;
import clus.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import clus.jeans.io.ini.INIFileSection;
import clus.jeans.io.range.IntRangeCheck;


public class SettingsTree implements ISettings {

    /***********************************************************************
     * Section: Tree *
     ***********************************************************************/

    public final static String[] TREE_OPTIMIZE_VALUES = { "NoClusteringStats", "NoInodeStats" };
    public final static int[] TREE_OPTIMIZE_NONE = {};
    public final static int TREE_OPTIMIZE_NO_CLUSTERING_STATS = 0;
    public final static int TREE_OPTIMIZE_NO_INODE_STATS = 1;

    // Added by Eduardo Costa - 06/06/2011

    public final static String[] INDUCTION_ORDER = { "DepthFirst", "BestFirst" };
    public final static int DEPTH_FIRST = 0;
    public final static int BEST_FIRST = 1;

    protected INIFileNominal m_InductionOrder;

    // end block added by Eduardo

    // Added by Eduardo Costa - 27/09/2011

    public final static String[] ENTROPY_TYPE = { "StandardEntropy", "ModifiedEntropy" };
    public final static int STANDARD_ENTROPY = 0;
    public final static int MODIFIED_ENTROPY = 1;

    protected INIFileNominal m_EntropyType;

    protected INIFileBool m_ConsiderUnlableInstancesInIGCalc;

    // end block added by Eduardo

    protected INIFileSection m_SectionTree;
    protected INIFileNominal m_Heuristic;
    protected INIFileInt m_TreeMaxDepth;
    protected INIFileBool m_BinarySplit;
    protected INIFileBool m_AlternativeSplits;
    protected INIFileNominalOrDoubleOrVector m_FTest;
    protected INIFileNominal m_PruningMethod;
    protected INIFileBool m_1SERule;
    protected INIFileBool m_MSENominal;
    protected INIFileDouble m_M5PruningMult;
    /** Do we transform leaves or all nodes of tree to rules */
    protected INIFileNominal m_RulesFromTree;
    protected INIFileNominal m_TreeOptimize;
    /**
     * Amount of datapoints to include for calculating split heuristic
     * Datapoints will be selected randomly
     **/
    protected INIFileInt m_TreeSplitSampling;


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


    public int getTreeMaxDepth() {
        return m_TreeMaxDepth.getValue();
    }


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


    /**
     * For tree to rules procedure, we want to induce a tree without maximum
     * depth
     */
    public void setTreeMaxDepth(int value) {
        m_TreeMaxDepth.setValue(value);
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


    public void setFTest(double ftest) {
        FTEST_VALUE = ftest;
        FTEST_LEVEL = FTest.getLevelAndComputeArray(ftest);
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

    public final static String[] HEURISTICS = { "Default", "ReducedError", "Gain", "GainRatio", "SSPD", "VarianceReduction", "MEstimate", "Morishita", "DispersionAdt", "DispersionMlt", "RDispersionAdt", "RDispersionMlt", "GeneticDistance", "SemiSupervised", "VarianceReductionMissing" };

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

    public static int FTEST_LEVEL;
    public static double FTEST_VALUE;
    public static double MINIMAL_WEIGHT;
    public static boolean ONE_NOMINAL = true;

    /***********************************************************************
     * Section: Tree - Pruning method *
     ***********************************************************************/

    public final static String[] PRUNING_METHODS = { "Default", "None", "C4.5", "M5", "M5Multi", "ReducedErrorVSB", "Garofalakis", "GarofalakisVSB", "CartVSB", "CartMaxSize", "EncodingCost", "CategoryUtility" };

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


    @Override
    public INIFileSection create() {
        m_SectionTree = new INIFileSection("Tree");
        m_SectionTree.addNode(m_Heuristic = new INIFileNominal("Heuristic", HEURISTICS, HEURISTIC_DEFAULT));
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

        // added by Eduardo Costa 06/06/2011
        m_SectionTree.addNode(m_InductionOrder = new INIFileNominal("InductionOrder", INDUCTION_ORDER, DEPTH_FIRST));

        // added by Eduardo Costa 29/09/2011
        m_SectionTree.addNode(m_EntropyType = new INIFileNominal("EntropyType", ENTROPY_TYPE, 0));
        m_SectionTree.addNode(m_ConsiderUnlableInstancesInIGCalc = new INIFileBool("ConsiderUnlableInstancesInIGCalc", false));

        return m_SectionTree;
    }

}
