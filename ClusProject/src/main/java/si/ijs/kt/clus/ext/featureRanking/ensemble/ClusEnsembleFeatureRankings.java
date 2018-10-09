package si.ijs.kt.clus.ext.featureRanking.ensemble;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleRanking;

public class ClusEnsembleFeatureRankings {

    private HashMap<EnsembleRanking, ClusEnsembleFeatureRanking> m_Rankings;
    private Settings m_Settings;
    
    public ClusEnsembleFeatureRankings(Settings s) {
    	m_Rankings = new HashMap<>();
        List<EnsembleRanking> rankingTypes = s.getEnsemble().getRankingMethods();
        for (EnsembleRanking rankingType : rankingTypes){
            m_Rankings.put(rankingType, new ClusEnsembleFeatureRanking(s, rankingType, this));
        }
    }
    
    public Settings getSettings(){
        return m_Settings; 
    }
    
    public HashMap<EnsembleRanking, ClusEnsembleFeatureRanking> getRankings(){
        return m_Rankings;
    }
    
    public void initializeAttributes(ClusAttrType[] descriptive, HashMap<EnsembleRanking, Integer> nbRankings){
        for (EnsembleRanking r : m_Rankings.keySet()){
            m_Rankings.get(r).initializeAttributes(descriptive, nbRankings.get(r).intValue());
        }
    }
    
    public void setEnsembleRankigDescription(int realTrees){
        for (EnsembleRanking r : m_Rankings.keySet()){
            m_Rankings.get(r).setEnsembleRankigDescription(realTrees);
        }
    }
    
    public void createFimp(ClusRun cr, String appendixToFimpName, int expectedNumberTrees, int realNumberOfTrees) throws IOException{
        for (EnsembleRanking r : m_Rankings.keySet()){
            m_Rankings.get(r).createFimp(cr, appendixToFimpName + r.toString(), expectedNumberTrees, realNumberOfTrees);
        }
    }
    
    public void putAttributesInfos(HashMap<EnsembleRanking, HashMap<String, double[][]>> importances) throws InterruptedException{
        for (EnsembleRanking r : m_Rankings.keySet()){
            m_Rankings.get(r).putAttributesInfos(importances.get(r));
        }
    }

}
