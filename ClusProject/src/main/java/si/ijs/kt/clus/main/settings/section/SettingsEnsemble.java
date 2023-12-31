
package si.ijs.kt.clus.main.settings.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.jeans.io.ini.*;
import si.ijs.kt.clus.util.jeans.io.range.IntRangeCheck;


public class SettingsEnsemble extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsEnsemble(int position) {
        super(position, "Ensemble");
    }

    /***********************************************************************
     * Section: Ensemble methods *
     ***********************************************************************/

    public enum EnsembleBootstrapping {
        Yes, No, Undefined;
    };

    public enum EnsembleMethod {
        Bagging(EnsembleBootstrapping.Yes),
        /** */
        RForest(EnsembleBootstrapping.Yes),
        /** */
        RSubspaces(EnsembleBootstrapping.No),
        /** */
        BagSubspaces(EnsembleBootstrapping.Yes),
        /** */
        Boosting(EnsembleBootstrapping.No),
        /** */
        RFeatSelection(EnsembleBootstrapping.No),
        /** */
        Pert(EnsembleBootstrapping.Yes),
        /** */
        ExtraTrees(EnsembleBootstrapping.No);

        EnsembleBootstrapping ShouldBootstrapData = null;


        EnsembleMethod(EnsembleBootstrapping bt) {
            ShouldBootstrapData = bt;
        }
    };

    public enum EnsembleVotingType {
        Majority, ProbabilityDistribution, OOBModelWeighted, OOBTargetWeighted
    };

    public enum EnsembleRanking {
        RForest, Genie3, Symbolic
    };

    public enum EnsembleROSAlgorithmType {

        /** ROS is disabled */
        Disabled,

        /** Algorithm generates fixed subspaces before learning and uses them. */
        FixedSubspaces,

        /**
         * Similar to Random forest algorithm, this option selects a different subset of
         * targets on every split.
         */
        DynamicSubspaces
    };

    /** How ROS ensemble make predictions */
    public enum EnsembleROSVotingType {
        /** Use all targets for voting. */
        TotalAveraging,

        /**
         * Vote only with those targets, that were used for learning, i.e., votes of
         * individual ensemble members contribute to the overall prediction only with
         * those target predictions, that have been used for learning the individual
         * ensemble members.
         * 
         * When used with {@code EnsembleROSAlgorithmType.FixedSubspaces}, When used
         * with {@code EnsembleROSAlgorithmType.DynamicSubspaces},
         */
        SubspaceAveraging
    };

    private INIFileNominalOrIntOrVector m_NbBags;
    // Number of seconds for learning trees
    private INIFileInt m_TimeBudget;
    /** Ensemble bootstrapping: yes or no */
    private INIFileEnum<EnsembleBootstrapping> m_EnsembleBootstrapping;
    /** Used ensemble method */
    private INIFileEnum<EnsembleMethod> m_EnsembleMethod;
    /**
     * Voting type, for regression mean is always used, the options are for
     * classification
     */
    private INIFileEnum<EnsembleVotingType> m_EnsembleVotingType;
    /**
     * Size of the feature set used during tree induction. Used for random forests,
     * random subspaces and bagging of subspaces. If left to default 0, floor(log_2
     * #DescAttr) + 1 is used.
     */
    private INIFileString m_RandomAttrSelected;

    /** Size of the target feature space (similar to m_RandomAttrSelected) */
    private INIFileString m_RandomTargetAttrSelected;

    /** Used for ROS (Random Output Selections) */
    private INIFileEnum<EnsembleROSAlgorithmType> m_EnsembleROSAlgorithmType;
    private INIFileEnum<EnsembleROSVotingType> m_EnsembleROSVotingType;

    private int m_SubsetSize;
    private INIFileBool m_PrintAllModels;
    private INIFileBool m_PrintAllModelFiles;
    private INIFileBool m_PrintAllModelInfo;
    private INIFileBool m_PrintPaths;
    private boolean m_EnsembleMode = false;
    /** Time and memory optimization */
    private INIFileBool m_EnsembleShouldOpt;
    /** Estimate error with time and memory optimization */
    private INIFileBool m_EnsembleOOBestimate;
    // protected INIFileBool m_FeatureRanking;
    // private INIFileNominal m_FeatureRanking;
    private INIFileEnumList<EnsembleRanking> m_FeatureRanking;
    private INIFileString m_SymbolicWeight;
    public static final String DYNAMIC_WEIGHT = "Dynamic";
    private INIFileBool m_SortFeaturesByRelevance;
    private INIFileBool m_WriteEnsemblePredictions;
    private INIFileNominalOrIntOrVector m_BagSelection;
    /** Number of threads that are used when growing the trees. */
    private INIFileInt m_NumberOfThreads;

    /**
     * Do we want to use different random depth for different iterations of
     * ensemble. Used in tree to rules optimization method. The MaxDepth of tree is
     * used as average.
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
        return m_Section.isEnabled();
    }


    /** Do we print ensemble settings to output files */
    public void setSectionEnsembleEnabled(boolean value) {
        m_Section.setEnabled(value);
    }


    public EnsembleBootstrapping getEnsembleBootstrapping() {
        return m_EnsembleBootstrapping.getValue();
    }


    public EnsembleMethod getEnsembleMethod() {
        return m_EnsembleMethod.getValue();
    }


    public void setEnsembleMethod(String method) throws IOException {
        m_EnsembleMethod.setValue(method);
    }


    public EnsembleROSAlgorithmType getEnsembleROSAlgorithmType() {
        return m_EnsembleROSAlgorithmType.getValue();
    }


    public EnsembleROSVotingType getEnsembleROSVotingType() {
        return m_EnsembleROSVotingType.getValue();
    }

    public void setEnsembleROSVotingType(EnsembleROSVotingType value) {
    	m_EnsembleROSVotingType.setValue(value);
    }

    public boolean isEnsembleROSEnabled() {
        return !getEnsembleROSAlgorithmType().equals(EnsembleROSAlgorithmType.Disabled) && isEnsembleMode();
    }


    public List<EnsembleRanking> getRankingMethods() {
        return m_FeatureRanking.getValue();
    }


    public String getRankingMethodName() {
        return m_FeatureRanking.getValue().toString();
    }


    public boolean shouldPerformRanking() {
        return m_FeatureRanking.getVectorSize() > 0;
    }


    public void setFeatureRankingMethod(String value) throws IOException {
        m_FeatureRanking.setValue(value);
    }


    public INIFileNominalOrIntOrVector getNbBaggingSets() {
        if (!m_NbBags.isVector() && (m_NbBags.getInt() == 0))
            m_NbBags.setInt(100);
        return m_NbBags;
    }

    public void setNbBags(int value) {
        m_NbBags.setInt(value);
    }

    public INIFileInt getTimeBudget() { return m_TimeBudget;}

    public void setTimeBudget(int value) {m_TimeBudget.setValue(value);}


    public int getNbRandomAttrSelected() {
        return m_SubsetSize;
    }


    public String getNbRandomAttrString() {
        return m_RandomAttrSelected.getValue();
    }


    public String getNbRandomTargetAttrString() {
        return m_RandomTargetAttrSelected.getValue();
    }

    public void setNbRandomTargetAttrString(String nbTargets) {
        m_RandomTargetAttrSelected.setValue(nbTargets);
    }


    public INIFileNominalOrIntOrVector getBagSelection() {
        return m_BagSelection;
    }

    public enum RandomAttributeTypeSelection {
        Descriptive, Clustering
    };
    
    
    public String[] getSymbolicWeights() {
    	String s = m_SymbolicWeight.getValue();
    	String[] values;
    	if (s.startsWith("[") && s.endsWith("]")) {
    		values = s.substring(1, s.length() - 1).split(",");
    	} else {
    		values = new String[] {s};
    	}
    	for (int i = 0; i < values.length; i++) {
    		values[i] = values[i].trim();
    	}
    	return values;    	
    }


    public int calculateNbRandomAttrSelected(ClusSchema schema, RandomAttributeTypeSelection type) {
        String value, s;
        int fsize = -1;
        int ubound;

        if (type.equals(RandomAttributeTypeSelection.Descriptive)) { // for descriptive subspacing
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
            ClusLogger.severe("The number of subspaces can't be negative.");
            ClusLogger.severe("\t Setting this value to default (log2).");
            value = "0";
        }

        if (value.equalsIgnoreCase("SQRT") || value.equalsIgnoreCase("0")) {
            fsize = (int) Math.ceil(Math.sqrt(ubound));// upper bound of sqrt
        }
        else if (value.equalsIgnoreCase("LOG") ) {
            fsize = (int) Math.ceil(Math.log(ubound) / Math.log(2));// upper bound of log2
        }
        else if (value.equalsIgnoreCase("RANDOM")) { // sample independently at random
            fsize = -1; // dummy value
        }
        else if (value.equalsIgnoreCase("RANDOMPERTREE")) {// sample independently at random but fix the number of
                                                           // randomly selected attributes for each tree
            fsize = -2; // dummy value
        }
        else {
            try {
                int val = Integer.parseInt(value);
                if (val > ubound) {
                    ClusLogger.severe("The size of the subset can't be larger than the number of " + s + " attributes.");
                    ClusLogger.severe("\t Setting this value to: the number of all attributes");
                    val = ubound;
                }
                fsize = val;
            }
            catch (NumberFormatException e) {// not an integer
                try {
                    double val = Double.parseDouble(value);
                    if (val > 1.0) {
                        ClusLogger.severe("The fraction of the features used can't be larger than 1.");
                        ClusLogger.severe("\t Setting this value to 1, i.e., to bagging.");
                        val = 1.0;
                    }
                    fsize = (int) Math.ceil(val * ubound); // upper bound on the fraction number;
                }
                catch (Exception e2) {
                    ClusLogger.severe("Error while setting the feature subset size!");
                    ClusLogger.severe("The set of possible values include:");
                    ClusLogger.severe("\t 0 or LOG for taking the log2,");
                    ClusLogger.severe("\t SQRT for taking the sqrt,");
                    ClusLogger.severe("\t RANDOM for taking a random subset of targets (works only in ensemble mode for target subspacing).");
                    ClusLogger.severe("\t integer for taking the absolute number of attributes,");
                    ClusLogger.severe("\t double (0,1) for taking the fraction values.");
                    System.exit(-1);
                }
            }
        }

        if (fsize < 0)
            return fsize; // for random/randompertree

        return Math.max(1, fsize);
    }


    public void updateNbRandomAttrSelected(ClusSchema schema) {

        int fsize = calculateNbRandomAttrSelected(schema, RandomAttributeTypeSelection.Descriptive);

        setNbRandomAttrSelected(fsize);
    }


    public void setNbRandomAttrSelected(int value) {
        m_SubsetSize = value;
        m_RandomAttrSelected.setValue(Integer.toString(value));
    }


    public void setBagSelection(int value) {
        m_BagSelection.setInt(value);
    }


    public boolean isVotingOOBWeighted() {
        return Arrays.asList(EnsembleVotingType.OOBModelWeighted, /** */
                EnsembleVotingType.OOBTargetWeighted) /** */
                .contains(getEnsembleVotingType());
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
     * Do we want to use different random depth for different iterations of
     * ensemble. Used in tree to rules optimization method. The MaxDepth of tree is
     * used as average.
     */
    public boolean isEnsembleRandomDepth() {
        return m_EnsembleRandomDepth.getValue();
    }


    /**
     * Gets the size of bags for ensembles
     * 
     * @return the bag size
     */
    public int getEnsembleBagSize() {
        return m_EnsembleBagSize.getValue();
    }


    /**
     * Sets the size of bags for ensembles
     * 
     * @param value
     *        the size of the training set for individual bags
     */
    public void setEnsembleBagSize(int value) {
        m_EnsembleBagSize.setValue(value);
    }


    public EnsembleVotingType getEnsembleVotingType() {
        return m_EnsembleVotingType.getValue();
    }


    public int getNumberOfThreads() {
        return m_NumberOfThreads.getValue();
    }


    public boolean isEnsembleWithParallelExecution() {
        return isEnsembleMode() && (m_NumberOfThreads.getValue() > 1);
    }


    public String getEnsembleMethodName() {
        return getEnsembleMethod().toString();
    }


    @Override
    public void create() {

        m_Section.addNode(m_NbBags = new INIFileNominalOrIntOrVector("Iterations", NONELIST));
        m_Section.addNode(m_TimeBudget = new INIFileInt("TimeBudget", 0));
        m_Section.addNode(m_EnsembleMethod = new INIFileEnum<>("EnsembleMethod", EnsembleMethod.Bagging));
        m_Section.addNode(m_EnsembleBootstrapping = new INIFileEnum<>("EnsembleBootstrapping", EnsembleBootstrapping.Undefined));
        m_Section.addNode(m_EnsembleVotingType = new INIFileEnum<>("VotingType", EnsembleVotingType.ProbabilityDistribution));
        m_Section.addNode(m_RandomAttrSelected = new INIFileString("SelectRandomSubspaces", "0"));
        m_Section.addNode(m_RandomTargetAttrSelected = new INIFileString("ROSTargetSubspaceSize", "SQRT"));
        m_Section.addNode(m_EnsembleROSAlgorithmType = new INIFileEnum<>("ROSAlgorithmType", EnsembleROSAlgorithmType.Disabled));
        m_Section.addNode(m_EnsembleROSVotingType = new INIFileEnum<>("ROSVotingType", EnsembleROSVotingType.SubspaceAveraging));
        m_Section.addNode(m_PrintAllModels = new INIFileBool("PrintAllModels", false));
        m_Section.addNode(m_PrintAllModelFiles = new INIFileBool("PrintAllModelFiles", false));
        m_Section.addNode(m_PrintAllModelInfo = new INIFileBool("PrintAllModelInfo", false));
        m_Section.addNode(m_PrintPaths = new INIFileBool("PrintPaths", false));
        m_Section.addNode(m_EnsembleShouldOpt = new INIFileBool("Optimize", false));
        m_Section.addNode(m_EnsembleOOBestimate = new INIFileBool("OOBestimate", false));
        m_Section.addNode(m_FeatureRanking = new INIFileEnumList<>("FeatureRanking", Arrays.asList(), EnsembleRanking.class));
        m_Section.addNode(m_FeatureRankingPerTarget = new INIFileBool("FeatureRankingPerTarget", false));
        m_Section.addNode(m_SymbolicWeight = new INIFileString("SymbolicWeight", "0.5"));

        m_Section.addNode(m_SortFeaturesByRelevance = new INIFileBool("SortRankingByRelevance", true));
        m_Section.addNode(m_WriteEnsemblePredictions = new INIFileBool("WriteEnsemblePredictions", false));
        m_Section.addNode(m_EnsembleRandomDepth = new INIFileBool("EnsembleRandomDepth", false));

        m_Section.addNode(m_BagSelection = new INIFileNominalOrIntOrVector("BagSelection", NONELIST));
        m_BagSelection.setInt(-1);

        m_Section.addNode(m_EnsembleBagSize = new INIFileInt("BagSize", 0));
        m_EnsembleBagSize.setValueCheck(new IntRangeCheck(0, Integer.MAX_VALUE));

        m_Section.addNode(m_NumberOfThreads = new INIFileInt("NumberOfThreads", 1));
        m_NumberOfThreads.setValueCheck(new IntRangeCheck(0, 200));// Runtime.getRuntime().availableProcessors()));

        m_Section.setEnabled(false);
    }


    @Override
    public List<String> validateSettingsInternal() {
        ArrayList<String> incompatible = new ArrayList<String>();

        /* ROS */
        HashMap<EnsembleROSAlgorithmType, List<EnsembleROSVotingType>> validROSCombinations = new HashMap<>();
        validROSCombinations.put(EnsembleROSAlgorithmType.FixedSubspaces, Arrays.asList(EnsembleROSVotingType.TotalAveraging, EnsembleROSVotingType.SubspaceAveraging));
        validROSCombinations.put(EnsembleROSAlgorithmType.DynamicSubspaces, Arrays.asList(EnsembleROSVotingType.TotalAveraging, EnsembleROSVotingType.SubspaceAveraging));

        if (
        /* is ROS is not disabled */ !getEnsembleROSAlgorithmType().equals(EnsembleROSAlgorithmType.Disabled) &&
        /* combination of ROS algorithm and prediction algorithm is not valid */
                (!validROSCombinations.get(getEnsembleROSAlgorithmType()).contains(getEnsembleROSVotingType()))) {

            incompatible.add(formatInvalid(m_EnsembleROSAlgorithmType, getEnsembleROSAlgorithmType(), String.format("Cannot be used with %s = %s", m_EnsembleROSVotingType.getName(), getEnsembleROSVotingType())));
        }

        /* RANKING */
        if (isEnsembleMode()) {
            EnsembleMethod m = getEnsembleMethod();
            //EnsembleRanking r = getRankingMethod();
            List<EnsembleMethod> allowed = Arrays.asList(EnsembleMethod.Bagging, EnsembleMethod.RForest, EnsembleMethod.ExtraTrees);
            if (shouldPerformRanking() && !allowed.contains(m)){
                incompatible.add(String.format("Feature Ranking not implemented for %s", m.name()));
            }
        }

        return incompatible;
    }


    /**
     * If bootstrapping is not defined, choose the appropriate default value for
     * the selected ensemble method.
     */
    public void determineBoostrapping() {
        EnsembleMethod ens = getEnsembleMethod();
        EnsembleBootstrapping chosen = getEnsembleBootstrapping();
        EnsembleBootstrapping def = ens.ShouldBootstrapData;
        if (chosen.equals(EnsembleBootstrapping.Undefined)) {
            m_EnsembleBootstrapping.setValue(def);
        }
        else if (!ens.equals(EnsembleMethod.ExtraTrees)) { // We only allow bootstrapping changes for ExtraTrees
            if (!chosen.equals(def)) {
                ClusLogger.severe("Bootstrapping value changed to default: " + def);
                m_EnsembleBootstrapping.setValue(def);
            }
        }
    }
}
