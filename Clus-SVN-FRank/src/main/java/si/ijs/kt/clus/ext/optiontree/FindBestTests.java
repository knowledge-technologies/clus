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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import si.ijs.kt.clus.algo.split.CurrentBestTestAndHeuristic;
import si.ijs.kt.clus.algo.split.NominalSplit;
import si.ijs.kt.clus.algo.split.SubsetSplit;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.RowDataSortHelper;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.ClusRandomNonstatic;

public class FindBestTests {

    public ArrayList<TestAndHeuristic> m_Tests;
    
    protected RowDataSortHelper m_SortHelper = new RowDataSortHelper();

    protected ClusStatManager m_StatManager;
    protected int m_MaxStats;
    
    protected int m_MaxSplits;

    public FindBestTests(ClusStatManager mgr) {
        m_StatManager = mgr;
        m_MaxStats = getSchema().getMaxNbStats();
        m_Tests = new ArrayList<TestAndHeuristic>();
    }

    public FindBestTests(ClusStatManager mgr, NominalSplit split) {
        m_StatManager = mgr;
        m_MaxStats = getSchema().getMaxNbStats();
    }

    public ClusSchema getSchema() {
        return getStatManager().getSchema();
    }

    public ClusStatManager getStatManager() {
        return m_StatManager;
    }

    public RowDataSortHelper getSortHelper() {
        return m_SortHelper;
    }

    public Settings getSettings() {
        return getStatManager().getSettings();
    }

    public void addBestNominalTest(NominalAttrType at, RowData data, ClusStatistic totstat) throws ClusException {
        // Reset positive statistic
        RowData sample = createSample(data);
        int nbvalues = at.getNbValues();
        
        //System.out.println("Adding nom test");
        
        SubsetSplit split = new SubsetSplit();
        split.initialize(m_StatManager);
        TestAndHeuristic tnh = new TestAndHeuristic(getSettings());
        tnh.create(m_StatManager, m_MaxStats);
        tnh.initTestSelector(totstat, data);
        tnh.setInitialData(totstat, data);
        tnh.setAttribtue(at);
        tnh.resetBestTest();
        int nb_rows = sample.getNbRows();
        if (nbvalues == 2 && !at.hasMissing()) {
            // Only count ones for binary attributes (optimization)
            for (int j = 0; j < nb_rows; j++) {
                DataTuple tuple = sample.getTuple(j);
                int value = at.getNominal(tuple);
                // The value "1" has index 0 in the list of attribute values
                if (value == 0) {
                    tnh.m_TestStat[0].updateWeighted(tuple, j);
                }
            }
            // Also compute the statistic for the zeros
            tnh.m_TestStat[1].copy(tnh.m_TotStat);
            tnh.m_TestStat[1].subtractFromThis(tnh.m_TestStat[0]);
        } else {
            // Regular code for non-binary attributes
            for (int j = 0; j < nb_rows; j++) {
                DataTuple tuple = sample.getTuple(j);
                int value = at.getNominal(tuple);
                tnh.m_TestStat[value].updateWeighted(tuple, j);
            }
        }
        CurrentBestTestAndHeuristic curr = tnh.makeCurrentBesTestAndHeuristic();
        split.findSplit(curr, at);
        tnh.loadCurrentBesTestAndHeuristic(curr);
        // Find best split
        m_Tests.add(tnh);
    }

    public void addBestNumericTest(ClusAttrType at, RowData data, ClusStatistic totstat) throws ClusException{
        RowData sample = createSample(data);
        DataTuple tuple;

        //System.out.println("Adding num test");
        
        TestAndHeuristic tnh = new TestAndHeuristic(getSettings());
        tnh.create(m_StatManager, m_MaxStats);
        tnh.initTestSelector(totstat, data);
        tnh.setInitialData(totstat, data);
        tnh.setAttribtue(at);
        tnh.resetBestTest();
        if (at.isSparse()) {
            sample.sortSparse((NumericAttrType) at, m_SortHelper);
        } else {
            sample.sort((NumericAttrType) at);
        }
        tnh.reset(2);
        // Missing values
        int first = 0;
        int nb_rows = sample.getNbRows();
        // Copy total statistic into corrected total
        tnh.copyTotal();
        if (at.hasMissing()) {
            // Because of sorting, all missing values are in the front :-)
            while (first < nb_rows && at.isMissing(tuple = sample.getTuple(first))) {
                tnh.m_MissingStat.updateWeighted(tuple, first);
                first++;
            }
            tnh.subtractMissing();
        }
        
        //System.out.println(nb_rows);
        
        double prev = Double.NaN;
        for (int j = first; j < nb_rows; j++) {
            tuple = sample.getTuple(j);
            double value = at.getNumeric(tuple);
            if (value != prev) {
                if (value != Double.NaN) {
                    // System.err.println("Value (>): " + value);
                    tnh.updateNumeric(value, at);
                }
                prev = value;
            }
            tnh.m_PosStat.updateWeighted(tuple, j);
        }
        m_Tests.add(tnh);
    }

    public void initSelectorAndSplit(ClusStatistic totstat) throws ClusException {
        for (int i = 0; i < m_Tests.size(); i++) {
            m_Tests.get(i).create(m_StatManager, m_MaxStats);
            m_Tests.get(i).setRootStatistic(totstat);
        }
        
        
        // if (getSettings().isBinarySplit()) m_Split = new SubsetSplit();
        // else m_Split = new NArySplit();
        // m_Split.initialize(m_StatManager);
    }

    public boolean initSelectorAndStopCrit(ClusStatistic total, RowData data) {
        for (int i = 0; i < m_Tests.size(); i++) {
            m_Tests.get(i).initTestSelector(total, data);
        }
        // This is adapted from the original CurrentBestTestAndHeuristic implementation where it is hardcoded to false
        return false;
    }

    public void setInitialData(ClusStatistic total, RowData data) throws ClusException {
        for (int i = 0; i < m_Tests.size(); i++) {
            m_Tests.get(i).setInitialData(total, data);
        }
    }
    
    private RowData createSample(RowData original) {
        return original.sample(getSettings().getTree().getTreeSplitSampling(), new ClusRandomNonstatic(getSettings().getGeneral().getRandomSeed()));
    }

    public void sort() {
        Collections.sort(m_Tests, new HeuristicComparator());
    }
    
    public class HeuristicComparator implements Comparator<TestAndHeuristic> {
        @Override
        public int compare(TestAndHeuristic o1, TestAndHeuristic o2) {
            if (o1.getHeuristicValue() == o2.getHeuristicValue()) return 0;
            if (o1.getHeuristicValue() < o2.getHeuristicValue()) return 1;
            else return -1; 
        }
    }

    public ArrayList<TestAndHeuristic> getBestTests(int n) {
        return new ArrayList<TestAndHeuristic>(m_Tests.subList(0, Math.min(n, m_Tests.size())));
    }

    public TestAndHeuristic getBestTest() {
    	if (m_Tests.isEmpty()) {
    		return null;
    	}
        TestAndHeuristic cur = m_Tests.get(0);
        double m = cur.m_BestHeur;
        
        for (int i = 1; i < m_Tests.size(); i++) {
            if (m_Tests.get(i).m_BestHeur > m) {
                cur = m_Tests.get(i);
                m = cur.m_BestHeur;
            }
        }
        return cur;
    }

    public int getNbCandidates() {
        double bestValue = m_Tests.get(0).getHeuristicValue();
        if (bestValue == Double.NEGATIVE_INFINITY) return 0;
        int n = 1;
        for (int i = 1; i < m_Tests.size(); i++) {
            if (m_Tests.get(i).getHeuristicValue() / bestValue > 1 - getSettings().getOptionTree().getOptionEpsilon()) {
                n++;
            }
        }
        return n;
    }
    
    public boolean selectMultiple() {
        return getNbCandidates() > 1;
    }

    public void printCandidates() {
        for (TestAndHeuristic tnh : m_Tests) {
            System.out.print("Attribute: ");
            System.out.print(tnh.m_SplitAttr);
            System.out.print(" heuristic: ");
            System.out.print(tnh.m_BestHeur);
            System.out.print(" split: ");
            System.out.println(tnh.m_Split);
        }
    }
    
    public void reset() {
        // Shouldn't be called, the class get instantiated for each new node
    }

}
