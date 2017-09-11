
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.io.range.IntRangeCheck;


public class SettingsEnsemble extends SettingsBase {

    public SettingsEnsemble(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }



    /***********************************************************************
     * Section: Ensemble methods *
     ***********************************************************************/

    private final String[] ENSEMBLE_METHOD = { "Bagging", "RForest", "RSubspaces", "BagSubspaces", "Boosting", "RFeatSelection", "Pert", "ExtraTrees" };
    public final static int ENSEMBLE_METHOD_BAGGING = 0;
    public final static int ENSEMBLE_METHOD_RFOREST = 1;
    public final static int ENSEMBLE_METHOD_RSUBSPACES = 2;
    /** Random subspaces */
    public final static int ENSEMBLE_METHOD_BAGSUBSPACES = 3;
    /** Bagging of subspaces */
    public final static int ENSEMBLE_METHOD_BOOSTING = 4;
    public final static int ENSEMBLE_METHOD_RFOREST_NO_BOOTSTRAP = 5;
    public final static int ENSEMBLE_METHOD_PERT = 6;
    public final static int ENSEMBLE_METHOD_EXTRA_TREES = 7;

    private final String[] VOTING_TYPE = { "Majority", "ProbabilityDistribution" };
    public final static int VOTING_TYPE_MAJORITY = 0;
    public final static int VOTING_TYPE_PROBAB_DISTR = 1;

    private final String[] RANKING_TYPE = { "None", "RForest", "GENIE3", "SYMBOLIC" };
    public final static int RANKING_TYPE_NONE = 0;
    public final static int RANKING_TYPE_RFOREST = 1;
    public final static int RANKING_TYPE_GENIE3 = 2;
    public final static int RANKING_TYPE_SYMBOLIC = 3;

    private final String[] ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE = { "None", "TotalAveraging", "SubspaceAveraging", "SMARTERWAY" };
    public final static int ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_NONE = 0; /* IF THIS IS SELECTED, ROS IS NOT ACTIVATED */
    public final static int ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_TOTAL_AVERAGING = 1;
    public final static int ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_SUBSET_AVERAGING = 2;
    public final static int ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_SMARTERWAY = 3; /* TBD */

    private INIFileSection m_SectionEnsembles;
    private INIFileNominalOrIntOrVector m_NbBags;
    /** Used ensemble method */
    private INIFileNominal m_EnsembleMethod;
    /** Voting type, for regression mean is always used, the options are for classification */
    private INIFileNominal m_ClassificationVoteType;
    /**
     * Size of the feature set used during tree induction. Used for random forests, random
     * subspaces and bagging of subspaces. If left to default 0, floor(log_2 #DescAttr) + 1 is used.
     */
    private INIFileString m_RandomAttrSelected;

    /** Size of the target feature space (similar to m_RandomAttrSelected) */
    private INIFileString m_RandomTargetAttrSelected;

    /** Used for ROS (Random Output Selections) */
    private INIFileNominal m_EnsembleROSScope;

    private int m_SubsetSize;
    private INIFileBool m_PrintAllModels;
    private INIFileBool m_PrintAllModelFiles;
    private INIFileBool m_PrintAllModelInfo;
    private INIFileBool m_PrintPaths;
    private boolean m_EnsembleMode = false;
    /** Time & memory optimization */
    private INIFileBool m_EnsembleShouldOpt;
    /** Estimate error with time & memory optimization */
    private INIFileBool m_EnsembleOOBestimate;
    // protected INIFileBool m_FeatureRanking;
    private INIFileNominal m_FeatureRanking;
    private INIFileNominalOrDoubleOrVector m_SymbolicWeight;
    private INIFileBool m_SortFeaturesByRelevance;
    private INIFileBool m_WriteEnsemblePredictions;
    private INIFileNominalOrIntOrVector m_BagSelection;
    /** Number of threads that are used when growing the trees. */
    private INIFileInt m_NumberOfThreads;

    /**
     * Do we want to use different random depth for different iterations of ensemble.
     * Used in tree to rules optimization method. The MaxDepth of tree is used as average.
     */
    private INIFileBool m_EnsembleRandomDepth;
    private INIFileInt m_EnsembleBagSize;
    private INIFileBool m_FeatureRankingPerTarget;


    public boolean shouldPerformRankingPerTarget() {
        return m_FeatureRankingPerTarget.getValue();
    }


    public void setPerformRankingPerTarget(boolean b) {
        m_FeatureRankingPerTarget.setValue(b);
    }


    public boolean isEnsembleMode() {
        return m_EnsembleMode;
    }


    public void setEnsembleMode(boolean value) {
        m_EnsembleMode = value;
        setSectionEnsembleEnabled(value);
    }


    /** Do we print ensemble settings to output files */
    public boolean isSectionEnsembleEnabled() {
        return m_SectionEnsembles.isEnabled();
    }


    /** Do we print ensemble settings to output files */
    public void setSectionEnsembleEnabled(boolean value) {
        m_SectionEnsembles.setEnabled(value);
    }


    public int getEnsembleMethod() {
        return m_EnsembleMethod.getValue();
    }


    public void setEnsembleMethod(String value) {
        m_EnsembleMethod.setValue(value);
    }


    public void setEnsembleMethod(int value) {
        m_EnsembleMethod.setSingleValue(value);
    }


    public int getEnsembleROSScope() {
        return m_EnsembleROSScope.getValue();
    }


    public void setEnsembleROSScope(int value) {
        m_EnsembleROSScope.setSingleValue(value);
    }


    public boolean isEnsembleROSEnabled() {
        return (getEnsembleROSScope() != ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_NONE) 
                && isEnsembleMode();
    }


    public int getRankingMethod() {
        return m_FeatureRanking.getValue();
    }


    public String getRankingMethodName() {
        return m_FeatureRanking.getStringSingle();
    }


    public double[] getSymbolicWeights() {
        return m_SymbolicWeight.getDoubleVector();
    }


    public double getSymbolicWeight() {
        return m_SymbolicWeight.getDouble();
    }


    public boolean shouldPerformRanking() {
        return m_FeatureRanking.getValue() != 0;
    }


    public void setFeatureRankingMethod(String value) {
        m_FeatureRanking.setValue(value);
    }


    public void setFeatureRankingMethod(int value) {
        m_FeatureRanking.setSingleValue(value);
    }


    public INIFileNominalOrIntOrVector getNbBaggingSets() {
        if (!m_NbBags.isVector() && (m_NbBags.getInt() == 0))
            m_NbBags.setInt(10);
        return m_NbBags;
    }


    public void setNbBags(int value) {
        m_NbBags.setInt(value);
    }


    public int getNbRandomAttrSelected() {
        return m_SubsetSize;
    }


    public String getNbRandomAttrString() {
        return m_RandomAttrSelected.getValue();
    }


    public String getNbRandomTargetAttrString() {
        return m_RandomTargetAttrSelected.getValue();
    }


    public INIFileNominalOrIntOrVector getBagSelection() {
        return m_BagSelection;
    }


    public int calculateNbRandomAttrSelected(ClusSchema schema, int type) {
        String value, s;
        int fsize = -1;
        int ubound;

        if (type == 1) { // for descriptive subspacing
            value = getNbRandomAttrString();
            ubound = schema.getNbDescriptiveAttributes();
            s = "descriptive";
        }
        else { // for ROS
            value = getNbRandomTargetAttrString();
            ubound = schema.getNbTargetAttributes();
            s = "target";
        }

        if (value.contains("-")) {
            System.err.println("The number of subspaces can't be negative.");
            System.err.println("\t Setting this value to default (log2).");
            value = "0";
        }

        if (value.equalsIgnoreCase("SQRT")) {
            fsize = (int) Math.ceil(Math.sqrt(ubound));// upper bound of sqrt
        }
        else if (value.equalsIgnoreCase("LOG") || value.equalsIgnoreCase("0")) {// the 0 is to keep the previous setting
            fsize = (int) Math.ceil(Math.log(ubound) / Math.log(2));// upper bound of log2
        }
        else if (value.equalsIgnoreCase("RANDOM")) { // sample independently at random
            fsize = -1; // dummy value
        }
        else {
            try {
                int val = Integer.parseInt(value);
                if (val > ubound) {
                    System.err.println("The size of the subset can't be larger than the number of " + s + " attributes.");
                    System.err.println("\t Setting this value to the number of attributes, i.e., to bagging.");
                    val = ubound;
                }
                fsize = val;
            }
            catch (NumberFormatException e) {// not an integer
                try {
                    double val = Double.parseDouble(value);
                    if (val > 1.0) {
                        System.err.println("The fraction of the features used can't be larger than 1.");
                        System.err.println("\t Setting this value to 1, i.e., to bagging.");
                        val = 1.0;
                    }
                    fsize = (int) Math.ceil(val * ubound); // upper bound on the fraction number;
                }
                catch (Exception e2) {
                    System.err.println("Error while setting the feature subset size!");
                    System.err.println("The set of possible values include:");
                    System.err.println("\t 0 or LOG for taking the log2,");
                    System.err.println("\t SQRT for taking the sqrt,");
                    System.err.println("\t RANDOM for taking a random subset of targets (works only in ensemble mode for target subspacing).");
                    System.err.println("\t integer for taking the absolute number of attributes,");
                    System.err.println("\t double (0,1) for taking the fraction values.");
                    System.exit(-1);
                }
            }
        }

        return fsize;
    }


    public void updateNbRandomAttrSelected(ClusSchema schema) {

        int fsize = calculateNbRandomAttrSelected(schema, 1);

        // if (getNbRandomAttrSelected() == 0)
        // fsize = (int) (Math.log(schema.getNbDescriptiveAttributes())/Math.log(2) + 1);
        // else fsize = getNbRandomAttrSelected();

        setNbRandomAttrSelected(fsize);
    }


    public void setNbRandomAttrSelected(int value) {
        m_SubsetSize = value;
        m_RandomAttrSelected.setValue(value + "");
    }


    public void setBagSelection(int value) {
        m_BagSelection.setInt(value);
    }


    public boolean isPrintEnsembleModels() {
        return m_PrintAllModels.getValue();
    }


    public boolean isPrintEnsembleModelFiles() {
        return m_PrintAllModelFiles.getValue();
    }


    public boolean isPrintEnsemblePaths() {
        return m_PrintPaths.getValue();
    }


    public boolean isPrintEnsembleModelInfo() {
        return m_PrintAllModelInfo.getValue();
    }


    public boolean shouldOptimizeEnsemble() {
        return m_EnsembleShouldOpt.getValue();
    }


    public boolean shouldSortRankingByRelevance() {
        return m_SortFeaturesByRelevance.getValue();
    }


    public boolean shouldWritePredictionsFromEnsemble() {
        return m_WriteEnsemblePredictions.getValue();
    }


    public boolean shouldEstimateOOB() {
        return m_EnsembleOOBestimate.getValue();
    }


    public void setOOBestimate(boolean value) {
        m_EnsembleOOBestimate.setValue(value);
    }


    /**
     * Do we want to use different random depth for different iterations of ensemble.
     * Used in tree to rules optimization method. The MaxDepth of tree is used as average.
     */
    public boolean isEnsembleRandomDepth() {
        return m_EnsembleRandomDepth.getValue();
    }


    /**
     * Gets the size of bags for ensembles in a bagging scheme
     * 
     * @return the bag size
     */
    public int getEnsembleBagSize() {
        return m_EnsembleBagSize.getValue();
    }


    /**
     * Sets the size of bags for ensembles in a bagging scheme
     * 
     * @param value
     *        the size of the training set for individual bags
     */
    public void setEnsembleBagSize(int value) {
        m_EnsembleBagSize.setValue(value);
    }


    public int getNumberOfThreads() {
        return m_NumberOfThreads.getValue();
    }


    public boolean isEnsembleWithParallelExecution() {
        return isEnsembleMode() && (m_NumberOfThreads.getValue() > 1);
    }


    public String getRankingTypeName(int type) {
        return RANKING_TYPE[type];
    }


    public int getClassificationVoteType() {
        return m_ClassificationVoteType.getValue();
    }

    public String getEnsembleMethodName(int type) {
        return ENSEMBLE_METHOD[type];
    }
    
   

    @Override
    public INIFileSection create() {

        m_SectionEnsembles = new INIFileSection("Ensemble");
        m_SectionEnsembles.addNode(m_NbBags = new INIFileNominalOrIntOrVector("Iterations", NONELIST));
        m_SectionEnsembles.addNode(m_EnsembleMethod = new INIFileNominal("EnsembleMethod", ENSEMBLE_METHOD, ENSEMBLE_METHOD_BAGGING));
        m_SectionEnsembles.addNode(m_ClassificationVoteType = new INIFileNominal("VotingType", VOTING_TYPE, VOTING_TYPE_PROBAB_DISTR));
        m_SectionEnsembles.addNode(m_RandomAttrSelected = new INIFileString("SelectRandomSubspaces", "0"));
        m_SectionEnsembles.addNode(m_RandomTargetAttrSelected = new INIFileString("SelectRandomTargetSubspaces", "SQRT"));
        m_SectionEnsembles.addNode(m_EnsembleROSScope = new INIFileNominal("RandomOutputSelection", ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE, ENSEMBLE_ROS_VOTING_FUNCTION_SCOPE_NONE));
        m_SectionEnsembles.addNode(m_PrintAllModels = new INIFileBool("PrintAllModels", false));
        m_SectionEnsembles.addNode(m_PrintAllModelFiles = new INIFileBool("PrintAllModelFiles", false));
        m_SectionEnsembles.addNode(m_PrintAllModelInfo = new INIFileBool("PrintAllModelInfo", false));
        m_SectionEnsembles.addNode(m_PrintPaths = new INIFileBool("PrintPaths", false));
        m_SectionEnsembles.addNode(m_EnsembleShouldOpt = new INIFileBool("Optimize", false));
        m_SectionEnsembles.addNode(m_EnsembleOOBestimate = new INIFileBool("OOBestimate", false));
        m_SectionEnsembles.addNode(m_FeatureRanking = new INIFileNominal("FeatureRanking", RANKING_TYPE, RANKING_TYPE_NONE));
        m_SectionEnsembles.addNode(m_FeatureRankingPerTarget = new INIFileBool("FeatureRankingPerTarget", false));
        m_SectionEnsembles.addNode(m_SymbolicWeight = new INIFileNominalOrDoubleOrVector("SymbolicWeight", NONELIST));
        m_SymbolicWeight.setDouble(1.0);
        m_SectionEnsembles.addNode(m_SortFeaturesByRelevance = new INIFileBool("SortRankingByRelevance", true));
        m_SectionEnsembles.addNode(m_WriteEnsemblePredictions = new INIFileBool("WriteEnsemblePredictions", false));
        m_SectionEnsembles.addNode(m_EnsembleRandomDepth = new INIFileBool("EnsembleRandomDepth", false));
        m_SectionEnsembles.addNode(m_BagSelection = new INIFileNominalOrIntOrVector("BagSelection", NONELIST));
        m_BagSelection.setInt(-1);
        m_SectionEnsembles.addNode(m_EnsembleBagSize = new INIFileInt("BagSize", 0));
        m_EnsembleBagSize.setValueCheck(new IntRangeCheck(0, Integer.MAX_VALUE));
        m_SectionEnsembles.addNode(m_NumberOfThreads = new INIFileInt("NumberOfThreads", 1));
        m_NumberOfThreads.setValueCheck(new IntRangeCheck(0, 200));//Runtime.getRuntime().availableProcessors()));

        m_SectionEnsembles.setEnabled(false);

        return m_SectionEnsembles;
    }


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub
        
    }

}
