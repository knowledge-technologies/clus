package si.ijs.kt.clus.ext.featureRanking;

import java.util.HashMap;
import java.util.List;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleRanking;

public class ClusEnsembleFeatureRankings {

    private HashMap<EnsembleRanking, ClusEnsembleFeatureRanking> m_Rankings;
    private Settings m_Settings;
    
    public ClusEnsembleFeatureRankings(Settings s) {
        List<EnsembleRanking> rankingTypes = s.getEnsemble().getRankingMethods();
        for (EnsembleRanking rankingType : rankingTypes){
            m_Rankings.put(rankingType, new ClusEnsembleFeatureRanking(s, rankingType, this));
        }
    }
    
    public Settings getSettings(){
        return m_Settings; 
    }

}
