
package si.ijs.kt.clus.ext.semisupervised;

import si.ijs.kt.clus.heuristic.ClusHeuristicImpl;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTree;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.jeans.math.MathUtil;

//TODO: Jurica: This was implemented in Clus by somebody else, I don't know where it is used, or what it does  
public class ModifiedGainHeuristic extends ClusHeuristicImpl {

    public ModifiedGainHeuristic(ClusStatistic stat, Settings sett) {
        super(stat, sett);
    }


    public double calcHeuristic(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic c_nstat, ClusStatistic missing) {
        ClassificationStat tstat = (ClassificationStat) c_tstat;
        ClassificationStat pstat = (ClassificationStat) c_pstat;
        ClassificationStat nstat = (ClassificationStat) c_nstat;
        // Equal for all target attributes
        int nb = tstat.m_NbTarget;
        double n_tot = tstat.m_SumWeight;
        double n_pos = pstat.m_SumWeight;
        double n_neg = nstat.m_SumWeight;
        // Acceptable?
        if (n_pos < SettingsTree.MINIMAL_WEIGHT || n_neg < SettingsTree.MINIMAL_WEIGHT) { return Double.NEGATIVE_INFINITY; }
        // Initialize entropy's
        double pos_ent = 0.0;
        double neg_ent = 0.0;
        double tot_ent = 0.0;
        // Entropy?
        for (int i = 0; i < nb; i++) {
            pos_ent += pstat.entropy(i);
            neg_ent += nstat.entropy(i);
            tot_ent += tstat.entropy(i);
        }
        // Gain?
        double gain = tot_ent - (n_pos * pos_ent + n_neg * neg_ent) / n_tot;
        if (gain < MathUtil.C1E_6)
            return Double.NEGATIVE_INFINITY;
        return gain;
    }


    public String getName() {
        return "Gain modified for semi-supervised learning";
    }
}
