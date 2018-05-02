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

package si.ijs.kt.clus.algo.split;

import java.util.Random;

import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.heuristic.rules.ClusRuleHeuristicDispersion;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.section.SettingsPhylogeny.PhylogenySequence;
import si.ijs.kt.clus.model.test.SubsetTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.CombStat;
import si.ijs.kt.clus.util.ClusException;


public class SubsetSplit extends NominalSplit {

    ClusStatistic m_PStat, m_CStat, m_MStat;
    ClusStatManager m_StatManager;


    @Override
    public void initialize(ClusStatManager manager) {
        m_PStat = manager.createClusteringStat();
        m_CStat = m_PStat.cloneStat();
        m_MStat = m_PStat.cloneStat();
        m_StatManager = manager;
    }


    @Override
    public void setSDataSize(int size) {
        m_PStat.setSDataSize(size);
        m_CStat.setSDataSize(size);
        m_MStat.setSDataSize(size);
    }


    public ClusStatManager getStatManager() {
        return m_StatManager;
    }


    public void showTest(NominalAttrType type, boolean[] isin, int add, double mheur, ClusStatistic tot, ClusStatistic pos) throws ClusException {
        int count = 0;
        // System.out.print(type.getName()+ " in {");
        for (int i = 0; i < type.getNbValues(); i++) {
            if (isin[i] || i == add) {
                // if (count != 0) System.out.print(",");
                // System.out.print(type.getValue(i));
                count++;
            }
        }
        tot.calcMean();
        pos.calcMean();
        // System.out.println("}: "+mheur+" "+tot+" "+pos);
        // System.out.println("}: "+mheur);
    }


    @Override
    public void findSplit(CurrentBestTestAndHeuristic node, NominalAttrType type) throws ClusException {
        // System.out.println("find split for attr: " + type);
        double unk_freq = 0.0;
        int nbvalues = type.getNbValues();
        boolean isin[] = new boolean[nbvalues];
        boolean acceptable = true; // can only be changed for phylogenetic trees
        // If has missing values?
        if (type.hasMissing()) {
            ClusStatistic unknown = node.m_TestStat[nbvalues];
            m_MStat.copy(node.m_TotStat);
            m_MStat.subtractFromThis(unknown);
            unk_freq = unknown.getTotalWeight() / node.m_TotStat.getTotalWeight();
        }
        else {
            m_MStat.copy(node.m_TotStat);
        }
        int card = 0;
        double pos_freq = 0.0;
        double bheur = Double.NEGATIVE_INFINITY;
        // Not working for rules except if constraint of tests to '1' is desired!
        if (nbvalues == 2 && (!getStatManager().isRuleInduceOnly() || getStatManager().getSettings().getRules().isConstrainedToFirstAttVal())) {
            // Handle binary splits efficiently
            card = 1;
            isin[0] = true;
            ClusStatistic CStat = node.m_TestStat[0];
            bheur = node.calcHeuristic(m_MStat, CStat);
            // showTest(type, isin, -1, bheur, m_MStat, m_CStat);
            pos_freq = CStat.getTotalWeight() / m_MStat.getTotalWeight();
        }
        else if ((getStatManager().getMode() == ClusStatManager.MODE_PHYLO) && (getStatManager().getSettings().getPhylogeny().getPhylogenySequence().equals(PhylogenySequence.DNA))) {
            // for phylogenetic trees with DNA sequences, we use an optimization method: tests like pos10={A,t} are
            // based on the results for tests pos10=A and pos10=T
            // we do not do this for protein sequences, since there the alphabet is much larger, which would complicate
            // things
            // else if (false) {
            boolean[] valid = new boolean[nbvalues];
            // we try all subsets of size 1 (e.g. pos10=A) and 2 (e.g. pos10={AT}) (nbvalues = 5)
            // first we try all subsets of size 1
            for (int j = 0; j < nbvalues; j++) {
                m_PStat.reset();
                m_PStat.add(node.m_TestStat[j]);
                double mheur = node.calcHeuristic(m_MStat, m_PStat);
                if (mheur > bheur) {
                    bheur = mheur;
                    isin = new boolean[nbvalues];
                    isin[j] = true;
                    card = 1;
                    // Calculate pos freq (of current best one)
                    pos_freq = m_PStat.getTotalWeight() / m_MStat.getTotalWeight();
                }
                if (mheur > Double.NEGATIVE_INFINITY) {
                    valid[j] = true;
                }
            }
            // then we try all subsets of size 2 (if both of the singleton subsets returned a valid heuristic value)
            for (int j = 0; j < nbvalues; j++) {
                if (valid[j])
                    for (int k = j + 1; k < nbvalues; k++) {
                        if (valid[k]) {
                            m_PStat.reset();
                            m_CStat.copy(m_PStat);
                            m_CStat.add(node.m_TestStat[j]);
                            m_PStat.add(node.m_TestStat[j]);
                            m_PStat.add(node.m_TestStat[k]);
                            // double mheur = node.calcHeuristic(m_MStat, m_PStat); // use this if you want to compute
                            // the heuristic only in the case both singletons were valid
                            ClusStatistic[] csarray = new ClusStatistic[2];
                            csarray[0] = m_PStat;
                            csarray[1] = m_CStat;
                            double mheur = node.calcHeuristic(m_MStat, csarray, 2); // use this if you want to compute
                                                                                    // the heuristic only in the case
                                                                                    // both singletons were valid AND
                                                                                    // you want to reuse their
                                                                                    // computations
                                                                                    // System.out.println("mheur: " +
                                                                                    // mheur);
                            if (mheur > bheur) {
                                bheur = mheur;
                                isin = new boolean[nbvalues];
                                isin[j] = true;
                                isin[k] = true;
                                card = 2;
                                // Calculate pos freq (of current best one)
                                pos_freq = m_PStat.getTotalWeight() / m_MStat.getTotalWeight();
                            }
                        }
                    }
            }
        }
        else {
            // Try to add values to subsets
            // Each iteration the cardinality increases by at most one
            m_PStat.reset();
            int bvalue = 0;
            if ((m_PStat instanceof CombStat) && ((CombStat) m_PStat).getSettings().getRules().isHeurRuleDist()) {
                ((ClusRuleHeuristicDispersion) node.m_Heuristic).setDataIndexes(new int[0]);
            }
            boolean allowSubsetSplits = getStatManager().getSettings().getModel().isNominalSubsetTests();
            while ((bvalue != -1) && ((card + 1) < nbvalues)) {
                bvalue = -1;
                for (int j = 0; j < nbvalues; j++) {
                    if (!isin[j]) {
                        // Try to add this one to the positive stat
                        m_CStat.copy(m_PStat);
                        m_CStat.add(node.m_TestStat[j]);
                        if ((m_PStat instanceof CombStat) && ((CombStat) m_PStat).getSettings().getRules().isHeurRuleDist()) {
                            boolean isin_current[] = new boolean[nbvalues];
                            for (int k = 0; k < nbvalues; k++) {
                                isin_current[k] = isin[k];
                            }
                            isin_current[j] = true;
                            ((ClusRuleHeuristicDispersion) node.m_Heuristic).setDataIndexes(isin_current);
                        }
                        // Calc heuristic

                        boolean isin_current[] = new boolean[nbvalues];
                        for (int k = 0; k < nbvalues; k++) {
                            isin_current[k] = isin[k];
                        }
                        isin_current[j] = true;
                        double mheur = node.calcHeuristic(m_MStat, m_CStat);
                        // showTest(type, isin_current, -1, mheur, m_MStat, m_CStat);
                        // System.out.println("mheur: " + mheur);
                        if (mheur > bheur) {
                            bheur = mheur;
                            bvalue = j;
                            // Calculate pos freq (of current best one)
                            pos_freq = m_CStat.getTotalWeight() / m_MStat.getTotalWeight();
                            node.checkAcceptable(m_MStat, m_CStat);
                        }
                    }
                }
                if (bvalue != -1) {
                    card++;
                    isin[bvalue] = true;
                    m_PStat.add(node.m_TestStat[bvalue]);
                }
                if (!allowSubsetSplits) {
                    // just generate equality tests for nominal attributes
                    break;
                }
            }
        }
        // System.out.println("pos_freq_orig = " + pos_freq);
        // System.out.println("attr: " + type + " best heur: " + bheur);
        if (bheur > node.m_BestHeur + ClusHeuristic.DELTA) {
            node.m_UnknownFreq = unk_freq;
            node.m_BestHeur = bheur;
            node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
            node.m_BestTest = new SubsetTest(type, card, isin, pos_freq);
            node.resetAlternativeBest();
            // System.out.println("attr: " + type + " best test: " + node.m_BestTest.getString());
        }
        else if (getStatManager().getSettings().getTree().showAlternativeSplits() && (((bheur > node.m_BestHeur - ClusHeuristic.DELTA) && (bheur < node.m_BestHeur + ClusHeuristic.DELTA)) || (bheur == Double.POSITIVE_INFINITY))) {
            // if same heuristic: add to alternatives (list will later be pruned to remove those tests that do
            // not yield exactly the same subsets)
            node.addAlternativeBest(new SubsetTest(type, card, isin, pos_freq));
        }
    }


    @Override
    public void findRandomSplit(CurrentBestTestAndHeuristic node, NominalAttrType type, Random rn) throws ClusException {
        double unk_freq = 0.0;
        int nbvalues = type.getNbValues();
        boolean isin[] = new boolean[nbvalues];
        // If has missing values?
        if (type.hasMissing()) {
            ClusStatistic unknown = node.m_TestStat[nbvalues];
            m_MStat.copy(node.m_TotStat);
            m_MStat.subtractFromThis(unknown);
            unk_freq = unknown.getTotalWeight() / node.m_TotStat.getTotalWeight();
        }
        else {
            m_MStat.copy(node.m_TotStat);
        }
        int card = 0;
        double pos_freq = 0.0;
        // Generate non-empty and non-full subset
        while (true) {
            for (int i = 0; i < isin.length; i++) {
                isin[i] = rn.nextBoolean();
            }
            int sum = 0;
            for (int i = 0; i < isin.length; i++) {
                if (isin[i]) {
                    sum++;
                }
            }
            if (!((sum == 0) || (sum == nbvalues))) {
                card = sum;
                break;
            }
        }
        // Calculate statistics ...
        m_PStat.reset();
        for (int j = 0; j < nbvalues; j++) {
            if (isin[j]) {
                m_PStat.add(node.m_TestStat[j]);
            }
        }
        pos_freq = m_PStat.getTotalWeight() / m_MStat.getTotalWeight();
        node.m_UnknownFreq = unk_freq;
        node.m_BestHeur = node.calcHeuristic(m_MStat, m_PStat);
        node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
        node.m_BestTest = new SubsetTest(type, card, isin, pos_freq);
    }


    @Override
    public void findExtraTreeSplit(CurrentBestTestAndHeuristic node, NominalAttrType type, Random rnd) throws ClusException {
        // System.out.println("find split for attr: " + type);
        double minAllowedFrequency = 0.01;
        double unk_freq = 0.0;
        int nbvalues = type.getNbValues();
        boolean isin[] = new boolean[nbvalues];
        // If has missing values?
        if (type.hasMissing()) {
            ClusStatistic unknown = node.m_TestStat[nbvalues];
            m_MStat.copy(node.m_TotStat);
            m_MStat.subtractFromThis(unknown);
            unk_freq = unknown.getTotalWeight() / node.m_TotStat.getTotalWeight();
        }
        else {
            m_MStat.copy(node.m_TotStat);
        }
        int card = 0;
        double pos_freq = 0.0;
        double bheur = Double.NEGATIVE_INFINITY;
        boolean found_test = false;

        if (nbvalues == 2 && (!getStatManager().isRuleInduceOnly() || getStatManager().getSettings().getRules().isConstrainedToFirstAttVal())) {
            // Handle binary splits efficiently
            card = 1;
            isin[0] = true;
            m_PStat.reset();
            for (int j = 0; j < nbvalues; j++) { // for loop in not really necessary, we simply follow the style from
                                                 // the else case
                if (isin[j]) {// if selected
                    m_PStat.add(node.m_TestStat[j]);
                }
            }
            // <--- ClusStatistic CStat = node.m_TestStat[0];
            bheur = node.calcHeuristic(m_MStat, m_PStat); // <--- bheur = node.calcHeuristic(m_MStat, CStat);
            showTest(type, isin, -1, bheur, m_MStat, m_PStat); // <--- showTest(type, isin, -1, bheur, m_MStat, CStat);
            pos_freq = m_PStat.getTotalWeight() / m_MStat.getTotalWeight(); // <--- CStat.m_SumWeight /
                                                                            // m_MStat.m_SumWeight;

            boolean acc_test = node.m_IsAcceptable && (m_MStat.getTotalWeight() >= 4.0) && (m_PStat.getTotalWeight() >= 2.0) && ((m_MStat.getTotalWeight() - m_PStat.getTotalWeight()) >= 2.0) && (pos_freq > minAllowedFrequency) && (pos_freq < 1.0 - minAllowedFrequency);
            // <---
            // boolean acc_test = node.m_IsAcceptable && (m_MStat.m_SumWeight >= 4.0) && (CStat.m_SumWeight >= 2.0) &&
            // ((m_MStat.m_SumWeight - CStat.m_SumWeight) >= 2.0) && (pos_freq > minAllowedFrequency) && (pos_freq < 1.0
            // - minAllowedFrequency);

            if (acc_test) {// select only meaningful splits
                found_test = true;
            }

        }
        else if ((getStatManager().getMode() == ClusStatManager.MODE_PHYLO) && (getStatManager().getSettings().getPhylogeny().getPhylogenySequence().equals(PhylogenySequence.DNA))) {
            System.err.println("Extra-Tree split selection not implemented for Phylogentic trees.");
            throw new ClusException("Error while searching for a random split in: " + getClass().getName());
        }
        else {
            int count = 0; // allow for nbTries opportunities to select a valid random test... otherwise assign worst
                           // split score
            int nbTries = 10;
            // boolean select = true;
            while (!found_test && count < nbTries) { // && select) {
                count++;
                // random selection of a subset of classes
                int sum = 0;
                for (int i = 0; i < isin.length; i++) {
                    isin[i] = rnd.nextBoolean();
                    if (isin[i])
                        sum++;
                }

                card = sum;

                // if (!((sum == 0) || (sum == nbvalues))) {
                if (sum == 0 || sum == nbvalues) {
                    // card = sum;
                    // select = false;
                }
                else {
                    m_PStat.reset();
                    for (int j = 0; j < nbvalues; j++) {
                        if (isin[j]) {// if selected
                            m_PStat.add(node.m_TestStat[j]);
                        }
                    }
                    pos_freq = m_PStat.getTotalWeight() / m_MStat.getTotalWeight();

                    node.checkAcceptable(m_MStat, m_PStat);
                    boolean acc_test = node.m_IsAcceptable && (m_MStat.getTotalWeight() >= 4.0) && (m_PStat.getTotalWeight() >= 2.0) && ((m_MStat.getTotalWeight() - m_PStat.getTotalWeight()) >= 2.0) && (pos_freq > minAllowedFrequency) && (pos_freq < 1.0 - minAllowedFrequency);
                    // node.getStat(i);

                    // acc_test = acc_test && node.getHeuristic().stopCriterion(node.getTotStat(), m_PStat, m_MStat);

                    // if (((node.m_TotStat.m_NbExamples - m_PStat.m_SumWeight) > 0) && (m_PStat.m_SumWeight >
                    // 0)){//select
                    // only meaningful splits
                    // found_test = true;
                    // }
                    if (acc_test) {// select only meaningful splits
                        found_test = true;
                    }
                }
            }

            if (count == nbTries || card == nbvalues || card == 0) {
                System.out.println("Due to the randomness in split search, a usefull split was not found in " + count + " tries:");
                System.out.println("   Cardinality = " + card);
                System.out.println("   nb values = " + nbvalues);
            }
            if (found_test) {
                bheur = node.calcHeuristic(m_MStat, node.m_TestStat[0]);
            }
        }

        boolean valid_test = node.m_IsAcceptable && (m_MStat.getTotalWeight() >= 4.0) && (m_PStat.getTotalWeight() >= 2.0) && ((m_MStat.getTotalWeight() - m_PStat.getTotalWeight()) >= 2.0) && (pos_freq > minAllowedFrequency) && (pos_freq < 1.0 - minAllowedFrequency);

        showTest(type, isin, -1, bheur, m_MStat, m_CStat);
        if (found_test && valid_test) {
            node.m_UnknownFreq = unk_freq;
            node.m_BestHeur = bheur;
            node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
            node.m_BestTest = new SubsetTest(type, card, isin, pos_freq);
            node.resetAlternativeBest();
        }
        else {
            node.m_BestHeur = Double.NEGATIVE_INFINITY;// this should ensure that this test is not considered at all
        }
        node.checkAcceptable(m_MStat, m_PStat);

    }


    // makes a random subsetsplit for a given attribute, where the subset includes one value and excludes another value
    // used in PERT trees
    public void findRandomPertSplit(CurrentBestTestAndHeuristic node, NominalAttrType type, Random rn, int valueincl, int valueexcl) {
        int nbvalues = type.getNbValues();
        boolean isin[] = new boolean[nbvalues];

        // Generate non-empty and non-full subset
        int card = 0;
        double pos_freq = 0.0;
        while (true) {
            for (int i = 0; i < isin.length; i++) {
                isin[i] = rn.nextBoolean();
            }
            isin[valueincl] = true;
            isin[valueexcl] = false;
            int sum = 0;
            for (int i = 0; i < isin.length; i++) {
                if (isin[i]) {
                    sum++;
                }
            }
            if (!((sum == 0) || (sum == nbvalues))) {
                card = sum;
                break;
            }
        }
        // Calculate statistics ...
        m_PStat.reset();
        for (int j = 0; j < nbvalues; j++) {
            if (isin[j]) {
                m_PStat.add(node.m_TestStat[j]);
            }
        }
        node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
        node.m_BestTest = new SubsetTest(type, card, isin, pos_freq);
    }

}
