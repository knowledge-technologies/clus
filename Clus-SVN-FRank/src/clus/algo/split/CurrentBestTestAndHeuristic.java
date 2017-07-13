/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package clus.algo.split;

import java.util.ArrayList;
import java.util.HashMap;

import clus.algo.tdidt.ClusDecisionTree;
import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.heuristic.ClusHeuristic;
import clus.heuristic.GISHeuristic;
import clus.heuristic.VarianceReductionHeuristicCompatibility;
import clus.main.ClusStatManager;
import clus.main.settings.SettingsTree;
import clus.model.test.InverseNumericTest;
import clus.model.test.NodeTest;
import clus.model.test.NumericTest;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;


public class CurrentBestTestAndHeuristic {

    private int m_Verbose;

    public final static int TYPE_NONE = -1;
    public final static int TYPE_NUMERIC = 0;
    public final static int TYPE_TEST = 1;
    public final static int TYPE_INVERSE_NUMERIC = 2;

    // Statistics
    public ClusStatistic m_TotStat; // Points to total statistic of node
    public ClusStatistic m_TotCorrStat; // Corrected total statistic
    public ClusStatistic m_MissingStat; // Points to last test statistic (see reset())
    public ClusStatistic m_PosStat; // Points to m_TestStat[0] (see create())
    public ClusStatistic[] m_TestStat;

    // Heuristic
    public ClusHeuristic m_Heuristic;
    public ClusAttributeWeights m_ClusteringWeights;

    // Best test information
    public NodeTest m_BestTest;
    public int m_TestType;
    public double m_BestHeur;
    public double m_UnknownFreq;
    public ClusAttrType m_SplitAttr;
    public ArrayList m_AlternativeBest = new ArrayList();
    public boolean m_IsAcceptable = true;

    // Cache for numeric attributes
    public double m_BestSplit;
    public double m_PosFreq;

    // Data set
    private RowData m_Subset;
    // daniela
    public double m_BestI;
    public double m_BestC;

    public static HashMap<String, Double> mins = new HashMap<String, Double>();
    public static HashMap<String, Double> maxes = new HashMap<String, Double>();

    public double hMinB = Double.POSITIVE_INFINITY;
    public double hMaxB = Double.NEGATIVE_INFINITY;
    public double hMin = Double.POSITIVE_INFINITY;
    public double hMax = Double.NEGATIVE_INFINITY;

    private static final int GIS_NOT_CHECKED = -1;
    private static final int GIS_NO = 0;
    private static final int GIS_YES = 1;
    private int isGIS = GIS_NOT_CHECKED;
    // daniela end


    public CurrentBestTestAndHeuristic(int verbosityLevel) {
        m_Verbose = verbosityLevel;
    }


    /***************************************************************************
     * Reset
     ***************************************************************************/

    public String toString() {
        return m_PosStat.getString2();
    }


    public final boolean hasBestTest() {
        return (m_IsAcceptable == true) && (m_TestType != TYPE_NONE);
    }


    public final String getTestString() {
        return m_BestTest.getString();
    }


    public final NodeTest updateTest() {
        if (m_TestType == TYPE_NUMERIC) {
            m_TestType = TYPE_TEST;
            m_BestTest = new NumericTest(m_SplitAttr.getType(), m_BestSplit, m_PosFreq);
        }
        else if (m_TestType == TYPE_INVERSE_NUMERIC) {
            m_TestType = TYPE_TEST;
            m_BestTest = new InverseNumericTest(m_SplitAttr.getType(), m_BestSplit, m_PosFreq);
        }
        if (m_BestTest == null) {
            System.out.println("Best test is null");
        }
        m_BestTest.preprocess(ClusDecisionTree.DEPTH_FIRST);
        m_BestTest.setUnknownFreq(m_UnknownFreq);
        m_BestTest.setHeuristicValue(m_BestHeur);
        // daniela
        m_BestTest.setIValue(m_BestI);
        m_BestTest.setIValue(m_BestC);
        // daniela end
        return m_BestTest;
    }


    public void setInitialData(ClusStatistic totstat, RowData data) throws ClusException {
        m_Heuristic.setInitialData(totstat, data);
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
        m_BestTest = null;
        m_TestType = TYPE_NONE;
        m_BestHeur = Double.NEGATIVE_INFINITY;
        m_UnknownFreq = 0.0;
        m_SplitAttr = null;
        // daniela
        m_BestI = Double.NEGATIVE_INFINITY;
        m_BestC = Double.POSITIVE_INFINITY;
        // daniela end
        resetAlternativeBest();
    }


    public final void resetAlternativeBest() {
        m_AlternativeBest.clear();
    }


    public final void addAlternativeBest(NodeTest nt) {
        m_AlternativeBest.add(nt);
    }


    public final void setBestHeur(double value) {
        m_BestHeur = value;
    }


    public final void reset(int nb) {
        // daniela
        hMax = Double.NEGATIVE_INFINITY;
        hMin = Double.POSITIVE_INFINITY;
        // daniela end
        for (int i = 0; i < nb; i++) {
            m_TestStat[i].reset();
        }
        m_MissingStat = m_TestStat[nb - 1];
    }


    public final void reset() {
        m_PosStat.reset();
    }


    /***************************************************************************
     * Create statistics
     ***************************************************************************/

    public final void create(ClusStatManager smanager, int nbstat) throws ClusException {
        m_TotStat = null;
        m_Heuristic = smanager.getHeuristic();
        m_TestStat = new ClusStatistic[nbstat];
        for (int i = 0; i < nbstat; i++) {
            m_TestStat[i] = smanager.createClusteringStat();
        }
        m_ClusteringWeights = smanager.getClusteringWeights();
        m_TotCorrStat = smanager.createClusteringStat();
        m_PosStat = m_TestStat[0];
    }


    public final void setHeuristic(ClusHeuristic heur) {
        m_Heuristic = heur;
    }


    /***************************************************************************
     * Inspectors
     ***************************************************************************/

    public final double getPosWeight() {
        return m_PosStat.m_SumWeight;
    }


    public final double getTotWeight() {
        return m_TotStat.m_SumWeight;
    }


    public final double getTotNoUnkW() {
        return m_TotCorrStat.m_SumWeight;
    }


    public final void subtractMissing() {
        m_TotCorrStat.subtractFromThis(m_MissingStat);
    }


    public final void copyTotal() {
        m_TotCorrStat.copy(m_TotStat);
    }


    public final void calcPosFreq() {
        m_PosFreq = m_PosStat.m_SumWeight / m_TotStat.m_SumWeight;
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


    public final ArrayList getAlternativeBest() {
        return m_AlternativeBest;
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
     ***************************************************************************/

    // Where is this used?
    public final void updateNumeric(double val, ClusStatistic pos, ClusAttrType at) {
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, pos, m_MissingStat);
        if (heur > m_BestHeur + ClusHeuristic.DELTA) {
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_NUMERIC;
            m_PosFreq = pos.m_SumWeight / tot_no_unk;
            m_BestSplit = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
        }
    }


    // daniela
    public final void calcMinMax(ClusAttrType at, double value) {
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);
        if (m_Verbose >= 2)
            System.err.println("Heur: " + heur + " nb: " + m_PosStat.m_SumWeight);
        if (heur != Double.NEGATIVE_INFINITY) {
            //System.out.println(at.getName()+" > "+value + "--> heur=" + heur);
            Double currentMin = mins.get(at.getName());
            if (currentMin == null) {
                currentMin = Double.POSITIVE_INFINITY;
            }
            if (heur < currentMin)
                mins.put(at.getName(), heur);
            Double currentMax = maxes.get(at.getName());
            if (currentMax == null) {
                currentMax = Double.NEGATIVE_INFINITY;
            }
            if (heur > currentMax)
                maxes.put(at.getName(), heur);
            //System.out.println("at/h/min/max1: "+at+" "+heur+" "+mins.get(at.getName())+" "+maxes.get(at.getName()));
        }
    }


    // min max Heur, scaling of H
    public final void calculateHMinMax(double val, ClusAttrType at) {
        double heur;
        heur = m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);
        if (heur == Double.POSITIVE_INFINITY || heur == Double.NEGATIVE_INFINITY)
            return;
        if (heur > hMax) {
            hMax = heur;
        }
        if (heur < hMin) {
            hMin = heur;
        }
    }
    // daniela end


    /**
     * Version of updateNumeric for if there are any GIS attributes.
     * 
     * @param val
     * @param at
     * @param permutation
     */
    public final void updateNumericGIS(double val, ClusAttrType at, Integer[] permutation) {
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);
        if (m_Verbose >= 2)
            System.err.println("Heur: " + heur + " nb: " + m_PosStat.m_SumWeight);

        double I = 0;
        double alpha = SettingsTree.ALPHA; // daniela
        if (!at.getSettings().getAttribute().isNullGIS()) { // handle Daniela case ...
            // daniela
            if (m_Heuristic instanceof VarianceReductionHeuristicCompatibility && (SettingsTree.ALPHA != 1.0)) {
                VarianceReductionHeuristicCompatibility gisHeuristic = (VarianceReductionHeuristicCompatibility) m_Heuristic;
                I = gisHeuristic.calcI(m_TotCorrStat, m_PosStat, m_MissingStat, permutation);
                if (alpha != 0.0) {
                    heur = 2 * (heur - hMin) / (hMax - hMin);
                    heur = alpha * heur + (1 - alpha) * I;
                }
                else {
                    heur = I;
                }
            }
            else if (m_Heuristic instanceof GISHeuristic && (SettingsTree.ALPHA != 1.0)) { //GIS heuristics (scaled heur, spatial heur)
                //boolean conditionI=true;
                //conditionI = (I > m_BestI) && (I > 0);                    //1.option max I, max variance reduction    
                //heur = heur*I;                                            //2.option multiplication of both
                //System.out.println("old Var: "+heur+" "+hMin+"-->"+hMax+" old I: "+I); //3.option linear combination of both  
                GISHeuristic gisHeuristic = (GISHeuristic) m_Heuristic;
                I = gisHeuristic.calcI(m_TotCorrStat, m_PosStat, m_MissingStat, permutation);
                if (alpha != 0.0) {
                    heur = 2 * (heur - hMin) / (hMax - hMin);
                    heur = alpha * heur + (1 - alpha) * I;
                }
                else {
                    heur = I;
                }
            }
            // daniela end
        }
        else {
            throw new RuntimeException("Use the standard updateNumeric!");
        }

        if (heur > m_BestHeur + ClusHeuristic.DELTA) {
            if (m_Verbose >= 2)
                System.err.println("Better.");
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            if (m_Verbose >= 2) {
                System.err.println(" tot_w: " + tot_w + " tot_no_unk: " + tot_no_unk);
            }
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_NUMERIC;
            m_PosFreq = getPosWeight() / tot_no_unk;
            m_BestSplit = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
            // daniela
            m_BestI = I;
            //m_BestC = I;
            // daniela end
        }
    }


    public final void updateNumeric(double val, ClusAttrType at, double ss_tot, boolean isEfficient) {
        double heur = isEfficient ? m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat, ss_tot) : m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);
        //      System.out.println(t1 - t0);
        if (m_Verbose >= 2)
            System.err.println("Heur: " + heur + " nb: " + m_PosStat.m_SumWeight);

        if (heur > m_BestHeur + ClusHeuristic.DELTA) {
            if (m_Verbose >= 2)
                System.err.println("Better.");
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            if (m_Verbose >= 2) {
                System.err.println(" tot_w: " + tot_w + " tot_no_unk: " + tot_no_unk);
            }
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_NUMERIC;
            m_PosFreq = getPosWeight() / tot_no_unk;
            m_BestSplit = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
        }
        // System.out.println("Try: "+at+">"+ClusFormat.TWO_AFTER_DOT.format(val)+" -> "+heur);
        // DebugFile.log(""+at.getType().getName()+">"+ClusFormat.TWO_AFTER_DOT.format(val)+","+heur);
    }


    public final void updateNumeric(double val, ClusAttrType at) {
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);

        if (m_Verbose >= 2)
            System.err.println("Heur: " + heur + " nb: " + m_PosStat.m_SumWeight);

        if (heur > m_BestHeur + ClusHeuristic.DELTA) {
            if (m_Verbose >= 2)
                System.err.println("Better.");
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            if (m_Verbose >= 2) {
                System.err.println(" tot_w: " + tot_w + " tot_no_unk: " + tot_no_unk);
            }
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_NUMERIC;
            m_PosFreq = getPosWeight() / tot_no_unk;
            m_BestSplit = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
        }
        // System.out.println("Try: "+at+">"+ClusFormat.TWO_AFTER_DOT.format(val)+" -> "+heur);
        // DebugFile.log(""+at.getType().getName()+">"+ClusFormat.TWO_AFTER_DOT.format(val)+","+heur);
    }


    /**
     * Take the inverse test '<=' instead of the default '>'.
     * 
     * @param val
     *        Split value.
     * @param at
     *        Attribute
     */
    public final void updateInverseNumeric(double val, ClusAttrType at) {
        double heur = m_Heuristic.calcHeuristic(m_TotCorrStat, m_PosStat, m_MissingStat);
        if (m_Verbose >= 2)
            System.err.println("Heur: " + heur + " nb: " + m_PosStat.m_SumWeight);
        if (heur > m_BestHeur + ClusHeuristic.DELTA) {
            if (m_Verbose >= 2)
                System.err.println("Better.");
            double tot_w = getTotWeight();
            double tot_no_unk = getTotNoUnkW();
            m_UnknownFreq = (tot_w - tot_no_unk) / tot_w;
            m_TestType = TYPE_INVERSE_NUMERIC;
            m_PosFreq = getPosWeight() / tot_no_unk;
            m_BestSplit = val;
            m_BestHeur = heur;
            m_SplitAttr = at;
        }
    }


    /***************************************************************************
     * Heuristics
     ***************************************************************************/

    public final double calcHeuristic(ClusStatistic stat) {
        return m_Heuristic.calcHeuristic(m_TotStat, stat, m_MissingStat);
    }


    public final double calcHeuristic(ClusStatistic tot, ClusStatistic pos) {
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
}
