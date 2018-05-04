
package si.ijs.kt.clus.main.settings.section;

import java.util.ArrayList;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.main.settings.section.SettingsOutput.ConvertRules;
import si.ijs.kt.clus.main.settings.section.SettingsTimeSeries.TimeSeriesPrototypeComplexity;
import si.ijs.kt.clus.util.FTest;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnumList;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.range.IntRangeCheck;


public class SettingsTree extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsTree(int position) {
        super(position, "Tree");
    }

    /***********************************************************************
     * Section: Tree *
     ***********************************************************************/
    public enum TreeOptimizeValues {
        NoClusteringStats, NoInodeStats
    };

    public enum InductionOrder {
        DepthFirst, BestFirst
    };

    private INIFileEnum<InductionOrder> m_InductionOrder;

    public enum EntropyType {
        StandardEntropy, ModifiedEntropy
    };

    private INIFileEnum<EntropyType> m_EntropyType;

    private INIFileBool m_ConsiderUnlableInstancesInIGCalc;

    // end block added by Eduardo

    private INIFileEnum<Heuristic> m_Heuristic;
    private INIFileEnum<SetDistance> m_SetDistance;
    private INIFileEnum<TupleDistance> m_TupleDistance;
    private INIFileEnum<TimeSeriesDistanceMeasure> m_TSDistance;
    private INIFileEnum<TimeSeriesPrototypeComplexity> m_HeuristicComplexity;

    private INIFileBool m_BinarySplit;
    private INIFileBool m_AlternativeSplits;
    private INIFileNominalOrDoubleOrVector m_FTest;
    private INIFileEnum<PruningMethod> m_PruningMethod;
    private INIFileBool m_1SERule;
    private INIFileBool m_MSENominal;
    private INIFileDouble m_M5PruningMult;
    /** Do we transform leaves or all nodes of tree to rules */
    private INIFileEnum<ConvertRules> m_RulesFromTree;
    private INIFileEnumList<TreeOptimizeValues> m_TreeOptimize;
    // daniela
    private INIFileEnum<SpatialMatrixType> m_SpatialMatrix;
    private INIFileEnum<SpatialMeasure> m_SpatialMeasure;
    private INIFileDouble m_Bandwidth;
    private INIFileBool m_Longlat;
    private INIFileDouble m_NeighCount;
    private INIFileDouble m_SpatialAlpha;
    // end daniela
    private INIFileInt m_TreeSplitSampling; // Amount of datapoints to include for calculating split heuristic.
                                            // Datapoints will be selected randomly.

    private INIFileEnum<SplitPositions> m_SplitPosition;

    public enum SplitPositions {
        Exact, Middle
    };

    public static double ALPHA; // daniela

    // added by Jurica Levatic, JSI
    private INIFileEnum<MissingClusteringAttributeHandlingType> m_MissingClusteringAttrHandling;

    /**
     * Determines how we handle the case where when searching evaluating candidate
     * split all examples have only missing values for a clustering attriute, in one
     * of the branches.
     */
    public enum MissingClusteringAttributeHandlingType {
        Ignore, EstimateFromTrainingSet, EstimateFromParentNode
    };

    /**
     * Determines how we calculate prototype (i.e., prediction) if all the tuple in
     * leaf node have only missing values for target attribute.
     */
    private INIFileEnum<MissingTargetAttributeHandlingType> m_MissingTargetAttrHandling;

    public enum MissingTargetAttributeHandlingType {

        /**
         * variance will not be estimated, while mean (i.e., prediction) will be equal
         * to zero
         */
        Zero,

        /**
         * variance and mean (i.e., prediction) will be estimated on the basis of the
         * root node (i.e., default model)
         */
        DefaultModel,

        /**
         * variance and mean (i.e., prediction) will be estimated on the basis of the
         * node's parent
         */
        ParentNode
    };


    public SplitPositions getSplitPosition() {
        return m_SplitPosition.getValue();
    }


    public MissingClusteringAttributeHandlingType getMissingClusteringAttrHandling() {
        return m_MissingClusteringAttrHandling.getValue();
    }


    public MissingTargetAttributeHandlingType getMissingTargetAttrHandling() {
        return m_MissingTargetAttrHandling.getValue();
    }


    public SpatialMatrixType getSpatialMatrix() {
        return m_SpatialMatrix.getValue();
    }


    public SpatialMeasure getSpatialMeasure() {
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


    public void setSpatialMeasure(SpatialMeasure method) {
        m_SpatialMeasure.setValue(method);
    }


    public void setSpatialMatrix(SpatialMatrixType method) {
        m_SpatialMatrix.setValue(method);
    }


    public TimeSeriesPrototypeComplexity getHeuristicComplexity() {
        return m_HeuristicComplexity.getValue();
    }


    public void setSectionTreeEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    public Heuristic getHeuristic() {
        return m_Heuristic.getValue();
    }


    public void setHeuristic(Heuristic value) {
        m_Heuristic.setValue(value);
    }


    public void setTSDistance(TimeSeriesDistanceMeasure value) {
        m_TSDistance.setValue(value);
    }


    public TimeSeriesDistanceMeasure getTSDistance() {
        return m_TSDistance.getValue();
    }


    public SetDistance getSetDistance() {
        return m_SetDistance.getValue();
    }


    public void setSetDistance(SetDistance value) {
        m_SetDistance.setValue(value);
    }


    public boolean checkSetDistance(SetDistance value) {
        return m_SetDistance.getValue().equals(value);
    }


    public TupleDistance getTupleDistance() {
        return m_TupleDistance.getValue();
    }


    public void setTupleDistance(TupleDistance value) {
        m_TupleDistance.setValue(value);
    }


    public boolean checkTupleDistance(TupleDistance value) {
        return m_TupleDistance.getValue().equals(value);
    }


    public InductionOrder getInductionOrder() {
        return m_InductionOrder.getValue();
    }


    public void setInductionOrder(InductionOrder value) {
        m_InductionOrder.setValue(value);
    }


    public EntropyType getEntropyType() {
        return m_EntropyType.getValue();
    }


    public void setEntropyType(EntropyType value) {
        m_EntropyType.setValue(value);
    }


    public boolean considerUnlableInstancesInIGCalc() {
        return m_ConsiderUnlableInstancesInIGCalc.getValue();
    }


    // end block added by Eduardo

    /**
     * To find the best split, heuristic can be calculated on a random sample of the
     * training set to conserve time
     * 
     * @return the size the random sample should be
     */
    public int getTreeSplitSampling() {
        return m_TreeSplitSampling.getValue();
    }


    /**
     * To find the best split, heuristic can be calculated on a random sample of the
     * training set to conserve time
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


    public PruningMethod getPruningMethod() {
        return m_PruningMethod.getValue();
    }


    public void setPruningMethod(PruningMethod method) {
        m_PruningMethod.setValue(method);
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
    public ConvertRules rulesFromTree() {
        return m_RulesFromTree.getValue();
    }


    public boolean hasTreeOptimize(TreeOptimizeValues value) {
        return m_TreeOptimize.getValue().contains(value);
    }

    /***********************************************************************
     * Section: Tree - Heuristic *
     ***********************************************************************/

    public enum Heuristic {
        Default, ReducedError, Gain, GainRatio, SSPD, VarianceReduction, MEstimate, Morishita, DispersionAdt, DispersionMlt, RDispersionAdt, RDispersionMlt, GeneticDistance, SemiSupervised, VarianceReductionMissing, StructuredData, VarianceReductionGIS
    }

    // SPATIAL
    public enum SpatialMatrixType {
        Binary, Euclidean, Modified, Gaussian
    };

    public enum SpatialMeasure {
        GlobalMoran, GlobalGeary, GlobalGetis, LocalMoran, LocalGeary, LocalGetis, StandardizedGetis, EquvalentI, IwithNeighbours, EquvalentIwithNeighbours, GlobalMoranDistance, GlobalGearyDistance, CI, MultiVariateMoranI, CwithNeighbours, Lee, MultiIwithNeighbours, CIwithNeighbours, LeewithNeighbours, Pearson, CIDistance, DH, EquvalentIDistance, PearsonDistance, EquvalentG, EquvalentGDistance, EquvalentPDistance
    };

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

    public enum SetDistance {
        GSMDistance, HammingLoss, Jaccard, Matching, Euclidean
    };

    /***********************************************************************
     * Section: Tree - TupleDistance *
     ***********************************************************************/

    public enum TupleDistance {
        Euclidean, Minkowski
    }

    /***********************************************************************
     * Section: Tree - TimeSeriesDistance *
     ***********************************************************************/

    public enum TimeSeriesDistanceMeasure {
        DTW, QDM, TSC
    };

    /***********************************************************************
     * Section: Tree - Pruning method *
     ***********************************************************************/

    public enum PruningMethod {
        Default, None, C45, M5, M5Multi, ReducedErrorVSB, Garofalakis, GarofalakisVSB, CartVSB, CartMaxSize, EncodingCost, CategoryUtility
    };


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
    public void create() {
        m_Section.addNode(m_Heuristic = new INIFileEnum<>("Heuristic", Heuristic.Default));

        m_Section.addNode(m_HeuristicComplexity = new INIFileEnum<>("HeuristicComplexity", TimeSeriesPrototypeComplexity.N2));
        m_Section.addNode(m_SetDistance = new INIFileEnum<>("SetDistance", SetDistance.GSMDistance));
        m_Section.addNode(m_TupleDistance = new INIFileEnum<>("TupleDistance", TupleDistance.Euclidean));
        m_Section.addNode(m_TSDistance = new INIFileEnum<>("TSDistance", TimeSeriesDistanceMeasure.DTW));

        m_Section.addNode(m_PruningMethod = new INIFileEnum<>("PruningMethod", PruningMethod.Default));
        m_Section.addNode(m_M5PruningMult = new INIFileDouble("M5PruningMult", 2.0));
        m_Section.addNode(m_1SERule = new INIFileBool("1-SE-Rule", false));
        m_Section.addNode(m_FTest = new INIFileNominalOrDoubleOrVector("FTest", NONELIST));
        m_FTest.setDouble(1.0);
        m_Section.addNode(m_BinarySplit = new INIFileBool("BinarySplit", true));
        m_Section.addNode(m_RulesFromTree = new INIFileEnum<>("ConvertToRules", ConvertRules.No));
        m_Section.addNode(m_AlternativeSplits = new INIFileBool("AlternativeSplits", false));
        m_Section.addNode(m_TreeOptimize = new INIFileEnumList<>("Optimize", new ArrayList<TreeOptimizeValues>() {}));
        m_Section.addNode(m_MSENominal = new INIFileBool("MSENominal", false));
        m_Section.addNode(m_TreeSplitSampling = new INIFileInt("SplitSampling", 0));
        m_TreeSplitSampling.setValueCheck(new IntRangeCheck(0, Integer.MAX_VALUE));

        m_Section.addNode(m_MissingClusteringAttrHandling = new INIFileEnum<>("MissingClusteringAttrHandling", MissingClusteringAttributeHandlingType.EstimateFromParentNode));
        m_Section.addNode(m_MissingTargetAttrHandling = new INIFileEnum<>("MissingTargetAttrHandling", MissingTargetAttributeHandlingType.ParentNode));

        m_Section.addNode(m_InductionOrder = new INIFileEnum<>("InductionOrder", InductionOrder.DepthFirst));
        m_Section.addNode(m_EntropyType = new INIFileEnum<>("EntropyType", EntropyType.StandardEntropy));
        m_Section.addNode(m_ConsiderUnlableInstancesInIGCalc = new INIFileBool("ConsiderUnlableInstancesInIGCalc", false));

        m_Section.addNode(m_SpatialMatrix = new INIFileEnum<>("SpatialMatrix", SpatialMatrixType.Binary));
        m_Section.addNode(m_SpatialMeasure = new INIFileEnum<>("SpatialMeasure", SpatialMeasure.GlobalMoran));
        m_Section.addNode(m_Bandwidth = new INIFileDouble("Bandwidth", 0.001));
        m_Section.addNode(m_Longlat = new INIFileBool("Longlat", false));
        m_Section.addNode(m_NeighCount = new INIFileDouble("NumNeightbours", 0.0));
        m_Section.addNode(m_SpatialAlpha = new INIFileDouble("Alpha", 1.0));
        m_Section.addNode(m_SplitPosition = new INIFileEnum<>("SplitPosition", SplitPositions.Exact));
    }


    @Override
    public void initNamedValues() {
        m_TreeSplitSampling.setNamedValue(0, "None");
    }
}
