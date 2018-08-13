/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package si.ijs.kt.clus.ext.optiontree;

import si.ijs.kt.clus.algo.split.CurrentBestTestAndHeuristic;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.test.InverseNumericTest;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.model.test.NumericTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;

public class TestAndHeuristic {

    public final static int TYPE_NONE = -1;
    public final static int TYPE_NUMERIC = 0;
    public final static int TYPE_TEST = 1;
    public final static int TYPE_INVERSE_NUMERIC = 2;

    // Statistics
    public ClusStatistic m_TotStat;     // Points to total statistic of node
    public ClusStatistic m_TotCorrStat; // Corrected total statistic
    public ClusStatistic m_MissingStat; // Points to last test statistic (see reset())
    public ClusStatistic m_PosStat;     // Points to m_TestStat[0] (see create())
    public ClusStatistic[] m_TestStat;

    // Heuristic
    public ClusHeuristic m_Heuristic;
    public ClusAttributeWeights m_ClusteringWeights;

    // Best test information
    public NodeTest m_Test;
    public int m_TestType;
    public double m_BestHeur;
    public double m_UnknownFreq;
    public ClusAttrType m_SplitAttr;
    // public ArrayList m_AlternativeBest = new ArrayList();
    public boolean m_IsAcceptable = true;

    // Cache for numeric attributes
    public double m_Split;
    public double m_PosFreq;

    // Data set
    public RowData m_Subset;
    
    private Settings m_Sett;
    
    public TestAndHeuristic(Settings sett) {
        m_Sett = sett;
    }

/***************************************************************************
 * Reset
 ***************************************************************************/

    @Override
    public String toString() {
        return m_PosStat.getString2();
    }

    public final boolean hasBestTest() { // TODO rename
        return (m_IsAcceptable == true) && (m_TestType != TYPE_NONE);
    }

    public final String getTestString() {
        return m_Test.getString();
    }

    public final NodeTest updateTest() {
        if (m_TestType == TYPE_NUMERIC) {
            m_TestType = TYPE_TEST;
            //ClusLogger.info(m_SplitAttr);
            m_Test = new NumericTest(m_SplitAttr.getType(), m_Split, m_PosFreq);
        } else if (m_TestType == TYPE_INVERSE_NUMERIC) {
            m_TestType = TYPE_TEST;
            m_Test = new InverseNumericTest(m_SplitAttr.getType(), m_Split, m_PosFreq);
        }
        if (m_Test == null) {
            ClusLogger.info("Best test is null");
        }
        m_Test.preprocess(ClusDecisionTree.DEPTH_FIRST);
        m_Test.setUnknownFreq(m_UnknownFreq);
        m_Test.setHeuristicValue(m_BestHeur);
        return m_Test;
    }

    public void setInitialData(ClusStatistic totstat, RowData data) throws ClusException {
        m_Heuristic.setInitialData(totstat,data);
    }

    public final void initTestSelector(ClusStatistic totstat, RowData subset) {
        initTestSelector(totstat);
        // Attach data set to heuristics and statistics
        for (int i = 0; i < m_TestStat.length; i++) {
            m_TestStat[i].setSDataSize(subset.getNbRows());
        }
        m_Heuristic.setData(subset);
        m_Subset = subset;
    }

    // Method for systems that do not support stats on data (like eff. xval.)
    public final void initTestSelector(ClusStatistic totstat) {
        m_TotStat = totstat;
        resetBestTest();
    }

    public final void resetBestTest() {
        m_Test = null;
        m_TestType = TYPE_NONE;
        m_BestHeur = Double.NEGATIVE_INFINITY;
        m_UnknownFreq = 0.0;
        //ClusLogger.info("Reset test");
        // resetAlternativeBest();
    }

//    public final void resetAlternativeBest() {
//        m_AlternativeBest.clear();
//    }

//    public final void addAlternativeBest(NodeTest nt) {
//        m_AlternativeBest.add(nt);
//    }

    public final void setBestHeur(double value) {
        m_BestHeur = value;
    }

    public final void reset(int nb) {
        for (int i = 0; i < nb; i++) {
            m_TestStat[i].reset();
        }
        m_MissingStat = m_TestStat[nb-1];
    }

    public final void reset() {
        m_PosStat.reset();
    }

    public CurrentBestTestAndHeuristic makeCurrentBesTestAndHeuristic() {
    	CurrentBestTestAndHeuristic node = new CurrentBestTestAndHeuristic(m_Sett);
    	
    	node.m_TotStat = this.m_TotStat;		// Points to total statistic of node
    	node.m_TotCorrStat = this.m_TotCorrStat;	// Corrected total statistic
    	node.m_MissingStat = this.m_MissingStat;	// Points to last test statistic (see reset())
    	node.m_PosStat = this.m_PosStat;		// Points to m_TestStat[0] (see create())
    	node.m_TestStat = this.m_TestStat;

    	// Heuristic
    	node.m_Heuristic = this.m_Heuristic;
    	node.m_ClusteringWeights = this.m_ClusteringWeights;

    	// Best test information
    	node.m_BestTest = this.m_Test;
    	node.m_TestType = this.m_TestType;
    	node.m_BestHeur = this.m_BestHeur;
    	node.m_UnknownFreq = this.m_UnknownFreq;
    	node.m_SplitAttr = this.m_SplitAttr;
    	node.m_IsAcceptable = this.m_IsAcceptable;

    	// Cache for numeric attributes
    	node.m_BestSplit = this.m_Split;
    	node.m_PosFreq = this.m_PosFreq;
    	
    	return node;
    }
    
    public void loadCurrentBesTestAndHeuristic(CurrentBestTestAndHeuristic node) {
    	
    	this.m_TotStat = node.m_TotStat;		// Points to total statistic of this
    	this.m_TotCorrStat = node.m_TotCorrStat;	// Corrected total statistic
    	this.m_MissingStat = node.m_MissingStat;	// Points to last test statistic (see reset())
    	this.m_PosStat = node.m_PosStat;		// Points to m_TestStat[0] (see create())
    	this.m_TestStat = node.m_TestStat;

    	// Heuristic
    	this.m_Heuristic = node.m_Heuristic;
    	this.m_ClusteringWeights = node.m_ClusteringWeights;

    	// Best test information
    	this.m_Test = node.m_BestTest;
    	this.m_TestType = node.m_TestType;
    	this.m_BestHeur = node.m_BestHeur;
    	this.m_UnknownFreq = node.m_UnknownFreq;
    	this.m_SplitAttr = node.m_SplitAttr;
    	this.m_IsAcceptable = node.m_IsAcceptable;

    	// Cache for numeric attributes
    	this.m_Split = node.m_BestSplit;
    	this.m_PosFreq = node.m_PosFreq;
    	
    }
        
/***************************************************************************
 * Create statistics
 ***************************************************************************/

    public final void create(ClusStatManager smanager, int nbstat) throws ClusException {
        //ClusLogger.info("Create called");
        m_TotStat = null;
        m_Heuristic = smanager.getHeuristic();
        //ClusLogger.info(m_Heuristic == null);
        m_TestStat = new ClusStatistic[nbstat];
        for (int i = 0; i < nbstat; i++) {
            m_TestStat[i] = smanager.createClusteringStat();
        }
        m_ClusteringWeights = smanager.getClusteringWeights();
        m_TotCorrStat = smanager.createClusteringStat();
        //m_TotCorrStat.copy(m_TotStat);
        m_PosStat = m_TestStat[0];
    }

    public final void setHeuristic(ClusHeuristic heur) {
        m_Heuristic = heur;
    }

/***************************************************************************
 * Inspectors
 ***************************************************************************/

    public final double getPosWeight() {
        return m_PosStat.getTotalWeight();
    }

    public final double getTotWeight() {
        return m_TotStat.getTotalWeight();
    }

    public final double getTotNoUnkW() {
        return m_TotCorrStat.getTotalWeight();
    }

    public final void subtractMissing() {
        m_TotCorrStat.subtractFromThis(m_MissingStat);
    }

    public final void copyTotal() {
        m_TotCorrStat.copy(m_TotStat);
    }

    public final void calcPosFreq() {
        m_PosFreq = m_PosStat.getTotalWeight() / m_TotStat.getTotalWeight();
    }

    public final ClusStatistic getStat(int i) {
        return m_TestStat[i];
    }

    public final ClusStatistic getPosStat() {
        return m_PosStat;
    }

    public final ClusStatistic getMissStat() {
        return m_MissingStat;
    }

    public final ClusStatistic getTotStat() {
        return m_TotStat;
    }

//    public final ArrayList getAlternativeBest() {
//        return m_AlternativeBest;
//    }
    
    public double getSplitValue() {
        return m_Split;
    }
    
    
/***************************************************************************
 * Stopping criterion
 ***************************************************************************/

    public final boolean stopCrit() {
        // It is normal that the stopping criterion is completely handled by the heuristics
        // I had a look in the cvs history and this appears to have been always the case
        // Note: this is also how Clus is presented in most papers, so it might be ok
        return false;
    }

/***************************************************************************
 * Numeric splits
 * @throws ClusException 
 ***************************************************************************/

    // Where is this used?
    public final void updateNumeric(double val, ClusStatistic pos, ClusAttrType at) throws ClusException {
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, pos, m_MissingStat);
        //ClusLogger.info(heur);
        //ClusLogger.info(m_BestHeur);
        if (heur > m_BestHeur + ClusHeuristic.DELTA) {
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_NUMERIC;
            m_PosFreq = pos.getTotalWeight() / tot_no_unk;
            m_Split = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
            //ClusLogger.info("Test set");
        }
    }

    public final void updateNumeric(double val, ClusAttrType at) throws ClusException {
        //ClusLogger.info(m_Heuristic == null);
        // ClusLogger.info(val);
        
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);
        if (m_Sett.getGeneral().getVerbose() >= 2) System.err.println("Heur: " + heur + " nb: " + m_PosStat.getTotalWeight());
        //ClusLogger.info(m_BestHeur);
        //ClusLogger.info(heur);
        if (heur - ClusHeuristic.DELTA > m_BestHeur ) {
            if (m_Sett.getGeneral().getVerbose() >= 2) System.err.println("Better.");
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            if (m_Sett.getGeneral().getVerbose() >= 2) {
                System.err.println(" tot_w: " + tot_w + " tot_no_unk: " + tot_no_unk);
            }
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_NUMERIC;
            m_PosFreq = getPosWeight() / tot_no_unk;
            m_Split = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
        }
//      ClusLogger.info("Try: "+at+">"+ClusFormat.TWO_AFTER_DOT.format(val)+" -> "+heur);
//      DebugFile.log(""+at.getType().getName()+">"+ClusFormat.TWO_AFTER_DOT.format(val)+","+heur);
    }


    /**
     * Take the inverse test '<=' instead of the default '>'.
     * @param val Split value.
     * @param at Attribute
     * @throws ClusException 
     */
    public final void updateInverseNumeric(double val, ClusAttrType at) throws ClusException {
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);
        if (m_Sett.getGeneral().getVerbose() >= 2) System.err.println("Heur: " + heur + " nb: " + m_PosStat.getTotalWeight());
        if (heur > m_BestHeur + ClusHeuristic.DELTA) {
            if (m_Sett.getGeneral().getVerbose() >= 2) System.err.println("Better.");
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_INVERSE_NUMERIC;
            m_PosFreq = getPosWeight() / tot_no_unk;
            m_Split = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
            
            
        }
    }

/***************************************************************************
 * Heuristics
 * @throws ClusException 
 ***************************************************************************/

    public final double calcHeuristic(ClusStatistic stat) throws ClusException {
        return m_Heuristic.calcHeuristic(m_TotStat, stat, m_MissingStat);
    }

    public final double calcHeuristic(ClusStatistic tot, ClusStatistic pos) throws ClusException {
        return m_Heuristic.calcHeuristic(tot, pos, m_MissingStat);
    }

    public final double calcHeuristic(ClusStatistic tot, ClusStatistic[] set, int arity) {
        return m_Heuristic.calcHeuristic(tot, set, arity);
    }

    public final ClusHeuristic getHeuristic() {
        return m_Heuristic;
    }

    public final double getHeuristicValue() {
        return m_BestHeur;
    }
    
    public void checkAcceptable(ClusStatistic tot, ClusStatistic pos) {
        m_IsAcceptable = m_Heuristic.isAcceptable(tot, pos, m_MissingStat);
    }


/***************************************************************************
 * Statistics on data
 ***************************************************************************/

    public final void setRootStatistic(ClusStatistic stat) {
        m_Heuristic.setRootStatistic(stat);
        for (int i = 0; i < m_TestStat.length; i++) {
            m_TestStat[i].setTrainingStat(stat);
        }
        m_TotCorrStat.setTrainingStat(stat);
    }

    public final void statOnData(RowData data) {
        setSDataSize(data.getNbRows());
        m_Heuristic.setData(data);
    }

    private final void setSDataSize(int nbex) {
        m_TotStat.setSDataSize(nbex);
        int nbstat = m_TestStat.length;
        for (int i = 0; i < nbstat; i++)
            m_TestStat[i].setSDataSize(nbex);
    }

//    public void setNumericSplit(double value) {
//        m_Split = value;
//        m_IsAcceptable = true;
//        m_TestType = TYPE_NUMERIC;
//    }
    
    public void setAttribtue(ClusAttrType at) {
        m_SplitAttr = at;
    }
}
