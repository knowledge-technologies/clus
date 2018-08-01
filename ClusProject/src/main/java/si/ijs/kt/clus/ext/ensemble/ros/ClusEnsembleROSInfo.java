
package si.ijs.kt.clus.ext.ensemble.ros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSAlgorithmType;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSVotingType;
import si.ijs.kt.clus.util.ClusUtil;


/**
 * @author martinb
 *
 */
public class ClusEnsembleROSInfo implements Serializable {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private ArrayList<int[]> m_AllSubspaces;
    private double[] m_Coverage; // how many times a target was used (per-target)
    private int[] m_CoverageOpt; // used when running ROS ensembles are running in optimized mode (this is a per-target
                                 // counter)
    private double m_AverageTargetsUsed; // average number of targets used in the ensemble
    private ClusSchema m_Schema;

    // new
    private EnsembleROSAlgorithmType m_ROSAlgorithmType = null;
    private EnsembleROSVotingType m_ROSVotingType = null;
    private Integer m_ROSSubspaceSize = null;


    public ClusEnsembleROSInfo(ClusSchema schema, ArrayList<int[]> info, EnsembleROSAlgorithmType algorithmType, EnsembleROSVotingType votingType, int subspaceSize) {
        m_Schema = schema;
        m_ROSAlgorithmType = algorithmType;
        m_ROSVotingType = votingType;
        m_ROSSubspaceSize = subspaceSize;

        m_AllSubspaces = info;

        calculateCoverage();

        m_CoverageOpt = new int[m_Schema.getNbTargetAttributes()];
        for (int i = 0; i < m_CoverageOpt.length; i++) {
            m_CoverageOpt[i] = 0;
        }
    }


    public int[] getModelSubspace(int i) {
        return m_AllSubspaces.get(i);
    }


    public boolean[] getModelSubspaceBoolean(int subspaceIdx) {
        int[] e = getModelSubspace(subspaceIdx);
        boolean[] b = new boolean[e.length];
        for (int j = 0; j < e.length; j++)
            b[j] = e[j] != 0;

        return b;
    }


    public int[] getOnlyTargets(int[] enabled) {
        ClusAttrType[] targets = m_Schema.getTargetAttributes();
        int[] result = new int[targets.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = enabled[targets[i].getIndex()];
        }

        return result;
    }


    private void calculateCoverage() {
        m_Coverage = new double[m_Schema.getNbTargetAttributes()];
        Arrays.fill(m_Coverage, 0.0);

        for (int i = 0; i < m_AllSubspaces.size(); i++) {
            int[] vals = getOnlyTargets(getModelSubspace(i));

            for (int t = 0; t < vals.length; t++) {
                if (vals[t] == 1) {
                    m_Coverage[t]++;
                }
            }
        }

        m_AverageTargetsUsed = 0.0;
        for (int i = 0; i < m_Coverage.length; i++) {
            m_AverageTargetsUsed += m_Coverage[i];
        }

        m_AverageTargetsUsed = ClusUtil.roundDouble(m_AverageTargetsUsed / m_AllSubspaces.size(), 3);
    }


    public double[] getCoverage() {
        return m_Coverage;
    }


    public double[] getCoverageNormalized() {
        double[] d = new double[m_Coverage.length];

        for (int t = 0; t < m_Coverage.length; t++)
            d[t] = ClusUtil.roundDouble(m_Coverage[t] / m_AllSubspaces.size(), 3);

        return d;
    }


    public double getAverageNumberOfTargetsUsed() {
        return m_AverageTargetsUsed;
    }


    public String getInfo(int i) {

        int[] vals = getOnlyTargets(getModelSubspace(i));
        String sb = "";
        if (vals.length > 15) {
            sb = ClusEnsembleROSInfo.getEnabledCount(vals) + " of " + vals.length;
        }
        else {
            sb = "";
            ClusAttrType[] targets = m_Schema.getTargetAttributes();

            for (int t = 0; t < vals.length; t++) {
                if (vals[t] == 1) {
                    // sb += (m_AllTargetIDs[t]+1) + "\t";
                    sb += (targets[t].getIndex() + 1) + "\t";
                }
                else {
                    sb += "-\t";
                }
            }
        }
        return sb;
    }


    public String getCoverageNormalizedInfo() {
        return String.format("Target coverage normalized: %s", Arrays.toString(getCoverageNormalized()).replace(",", " "));
    }


    public String getCoverageInfo() {
        return String.format("Target coverage: %s", Arrays.toString(m_Coverage).replace(",", " "));
    }


    public String getAverageNumberOfTargetsUsedInfo() {
        return String.format("Average number of targets used: %s | Average percentage of targets used: %s", String.valueOf(m_AverageTargetsUsed), String.valueOf(ClusUtil.roundDouble(m_AverageTargetsUsed / m_Schema.getNbTargetAttributes() * 100, 3) + "%"));
    }


    public static int getEnabledCount(int[] enabled) {
        int cnt = 0;
        for (int i = 0; i < enabled.length; i++) {
            if (enabled[i] == 1) {
                cnt++;
            }
        }
        return cnt;
    }


    /** Used when ensembles run in optimized mode */
    public void incrementCoverageOpt(int[] enabled) {
        for (int b = 0; b < enabled.length; b++) {
            if (enabled[b] == 1)
                m_CoverageOpt[b]++;
        }
    }


    /** Used when ensembles run in optimized mode */
    public int getCoverageOpt(int target) {
        return m_CoverageOpt[target];
    }


    /** Returns a trimmed variant of current object, containing only {@code numberOfSubspaces} ROS subspaces */
    public ClusEnsembleROSInfo getTrimmedInfo(int numberOfSubspaces) {

        if (numberOfSubspaces > m_AllSubspaces.size()) {
            throw new RuntimeException("number of subspaces > m_AllSubspaces.size()");
        }
        else if (numberOfSubspaces == m_AllSubspaces.size()) { return this; }

        ArrayList<int[]> ary = new ArrayList<int[]>();
        for (int i = 0; i < numberOfSubspaces; i++) {
            ary.add(m_AllSubspaces.get(i));
        }

        return new ClusEnsembleROSInfo(this.m_Schema, ary, m_ROSAlgorithmType, m_ROSVotingType, m_ROSSubspaceSize);
    }
}
