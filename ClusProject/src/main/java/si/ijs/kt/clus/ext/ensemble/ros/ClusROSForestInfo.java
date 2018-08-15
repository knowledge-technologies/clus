
package si.ijs.kt.clus.ext.ensemble.ros;

import java.util.ArrayList;
import java.util.Arrays;

import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSAlgorithmType;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSVotingType;
import si.ijs.kt.clus.util.ClusUtil;


public class ClusROSForestInfo {

    private final ArrayList<ClusROSModelInfo> m_Trees;

    private double[] m_Coverage; // how many times a certain target was used
    private int[] m_CoverageOpt; // used when running in optimization mode
    private double[] m_CoverageNormalized; // coverage of each target, normalized with number of trees

    private double m_AverageTargetsUsed; // average number of targets used
    private double m_AverageTargetsUsedPercentage; // average percentage of targets used

    private final EnsembleROSAlgorithmType m_ROSAlgorithmType;
    private final EnsembleROSVotingType m_ROSVotingType;
    private final int m_NbTargetAttributes;

    private String m_StringValue;


    public ClusROSForestInfo(EnsembleROSAlgorithmType rosAlgorithmType, EnsembleROSVotingType rosVotingType, int nbTargetAttributes) {
        m_Trees = new ArrayList<>();

        m_ROSAlgorithmType = rosAlgorithmType;
        m_ROSVotingType = rosVotingType;
        m_NbTargetAttributes = nbTargetAttributes;

        m_StringValue = null;
    }


    public EnsembleROSAlgorithmType getROSAlgorithmType() {
        return m_ROSAlgorithmType;
    }


    public EnsembleROSVotingType getROSVotingType() {
        return m_ROSVotingType;
    }


    public void addROSModelInfo(ClusROSModelInfo info) {
        m_Trees.add(info);
    }


    public ClusROSModelInfo getROSModelInfo(int treeNumber) {
        return m_Trees.stream().filter(p -> p.getTreeNumber() == treeNumber).findFirst().get();
    }


    /**
     * Calculates how much individual targets are used in the learning process.
     */
    private void calculateCoverage() {
        m_Coverage = new double[m_NbTargetAttributes];
        m_CoverageNormalized = new double[m_Coverage.length];
        m_AverageTargetsUsed = 0d;
        m_AverageTargetsUsedPercentage = 0d;

        Arrays.fill(m_Coverage, 0d);

        // for each ensemble model
        for (ClusROSModelInfo mi : m_Trees) {

            switch (m_ROSAlgorithmType) {
                case FixedSubspaces:
                    /**
                     * Count the number of trees that use a target.
                     * This is the same as calculating number of nodes in the tree that use that target and then
                     * dividing that count with all possible nodes.
                     * 
                     */

                    for (Integer enabled : mi.getTargets()) {
                        m_Coverage[enabled]++;
                    }

                    break;

                case DynamicSubspaces:
                   
                    /** Do the same as with Fixed subspaces - can this be smarter? 
                     * 
                     * The current implementation only considers the subspace of root node...
                     * */
                    for (Integer enabled : mi.getTargets()) {
                        m_Coverage[enabled]++;
                    }
                    
                   
                    ///**
                    // * Count number of nodes that use a certain target and then normalize with number of all
                    // * nodes in the tree.
                    // */
                    /*
                     * int allPossibleNodes = countNbNodesWithoutLeaf(mi);
                     * double[] counts = countCoverageWithoutLeaf(mi);
                     * for (int i = 0; i < counts.length; i++) {
                     * m_Coverage[i] += (counts[i] / allPossibleNodes);
                     * }
                     */

                    break;
                default:
                    throw new RuntimeException("ROS: unable to calculate coverage.");
            }
        }

        // calculate normalized coverage and average targets used
        for (int i = 0; i < m_CoverageNormalized.length; i++) {
            m_CoverageNormalized[i] = ClusUtil.roundDouble(m_Coverage[i] / m_Trees.size(), 3);

            m_AverageTargetsUsed += m_Coverage[i];
        }

        m_AverageTargetsUsedPercentage = ClusUtil.roundDouble(m_AverageTargetsUsed / m_Trees.size() / m_NbTargetAttributes * 100, 3);
        m_AverageTargetsUsed = ClusUtil.roundDouble(m_AverageTargetsUsed / m_Trees.size(), 3);
    }


    
    
    
    
    
    private int[] getCounts(ClusROSModelInfo mi) {

        int[] counts = new int[m_NbTargetAttributes];
        Arrays.fill(counts, 0);

        for (ClusROSModelInfo info : mi.getChildren()) {
            int[] ret = getCounts(info);

            for (int i = 0; i < ret.length; i++) {

                counts[i] += ret[i];
            }
        }

        for (Integer enabled : mi.getTargets()) {
            counts[enabled]++;
        }

        return counts;
    }


    /**
     * Counts all nodes except leaf nodes in a tree
     */
    private int countNbNodesWithoutLeaf(ClusROSModelInfo info) {
        if (info.getNbChildren() == 0) { return 0; }

        int count = 0;
        for (ClusROSModelInfo child : info.getChildren()) {
            count += countNbNodesWithoutLeaf(child);
        }
        return count + 1;
    }


    /**
     * Calculates coverage when using {@code EnsembleROSAlgorithmType.DynamicSubspaces}.
     */
    private double[] countCoverageWithoutLeaf(ClusROSModelInfo info) {
        if (info.getNbChildren() == 0) {
            double[] x = new double[m_NbTargetAttributes];
            Arrays.fill(x, 0d);
            return x;
        }

        double[] x = new double[m_NbTargetAttributes];
        Arrays.fill(x, 0d);

        for (ClusROSModelInfo child : info.getChildren()) {
            double[] y = countCoverageWithoutLeaf(child);

            for (int i = 0; i < x.length; i++) {
                x[i] += y[i];
            }
        }

        // actual coverage
        for (Integer i : info.getTargets()) {
            x[i]++;
        }

        return x;
    }


    public String getCoverageInfo() {
        return String.format("Target coverage: %s", Arrays.toString(m_Coverage).replace(",", " "));
    }


    public String getCoverageNormalizedInfo() {
        return String.format("Target coverage normalized: %s", Arrays.toString(m_CoverageNormalized).replace(",", " "));
    }


    public String getAverageNumberOfTargetsUsedInfo() {
        return String.format("Average number of targets used per tree: %s | Average percentage of targets used per tree: %s", String.valueOf(m_AverageTargetsUsed), String.valueOf(m_AverageTargetsUsedPercentage) + "%");
    }


    public double[] getCoverage() {
        if (m_Coverage == null) {
            calculateCoverage();
        }

        return m_Coverage;
    }


    public ClusROSForestInfo getNew(int numberOfModels) {
        if (numberOfModels == m_Trees.size()) {
            if (m_Coverage == null) {
                calculateCoverage();
            }
            return this;
        }

        ClusROSForestInfo info = new ClusROSForestInfo(m_ROSAlgorithmType, m_ROSVotingType, m_NbTargetAttributes);

        for (int i = 0; i < numberOfModels; i++) {
            info.addROSModelInfo(m_Trees.get(i));
        }

        info.calculateCoverage();

        return info;
    }


    @Override
    public String toString() {
        if (m_StringValue != null) { return m_StringValue; }

        ArrayList<String> all = new ArrayList<>();
        ArrayList<String> lst = new ArrayList<>();

        for (int i = 0; i < m_Trees.size(); i++) {
            ClusROSModelInfo info = m_Trees.get(i);
            lst.clear();

            for (int m = 0; m < m_NbTargetAttributes; m++) {
                if (info.isTargetEnabled(m)) {
                    lst.add(Integer.toString(m));
                }
                else {
                    lst.add("-");
                }
            }

            all.add(String.join(",", lst) + System.lineSeparator());
        }

        m_StringValue = String.join(System.lineSeparator(), all);

        return m_StringValue;
    }
}
