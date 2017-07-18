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

package si.ijs.kt.clus.error.mlc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNominalError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusFormat;


/**
 * @author matejp
 *
 *         Ranking loss is used in multi-label classification scenario.
 */
public class RankingLoss extends ClusNominalError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double m_NonnormalisedLoss;// sum over samples sample_i of the terms t_i = |D_i| / (|Y_i| |L \ Y_i|),
                                         // where L is the set of labels, Y_i is the predicted set of labels,
                                         // and D_i is the set of pairs (l1, l2), such that l1 is falsely positive and
                                         // l2 is falsely negative.
                                         // If Y_i is either empty set or it equals L, then t_i = 0
    protected int m_NbKnown;// number of the examples seen


    public RankingLoss(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_NonnormalisedLoss = 0.0;
        m_NbKnown = 0;
    }


    public boolean shouldBeLow() {
        return true;
    }


    public void reset() {
        m_NonnormalisedLoss = 0.0;
        m_NbKnown = 0;
    }


    public void add(ClusError other) {
        RankingLoss rl = (RankingLoss) other;
        m_NonnormalisedLoss += rl.m_NonnormalisedLoss;
        m_NbKnown += rl.m_NbKnown;
    }


    // NEDOTAKNJENO
    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }
    // // A MA TO SPLOH SMISU?
    // public double getRankingLoss(int i) {
    // return getModelErrorComponent(i);
    // }


    public double getModelError() {
        return m_NonnormalisedLoss / m_NbKnown;
    }


    public void showModelError(PrintWriter out, int detail) {
        out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
    }


    public String getName() {
        return "RankingLoss";
    }


    public ClusError getErrorClone(ClusErrorList par) {
        return new RankingLoss(par, m_Attrs);
    }


    public void addExample(DataTuple tuple, ClusStatistic pred) {
        final double[] scores = ((ClassificationStat) pred).calcScores();
        int wrongPairs = 0;
        int nbIrrelevant = 0, nbRelevant = 0;
        ArrayList<Integer> indicesOfKnownValues = new ArrayList<Integer>();
        NominalAttrType attr;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                indicesOfKnownValues.add(i);
            }
        }

        Collections.sort(indicesOfKnownValues, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return -Double.compare(scores[o1], scores[o2]);
            }
        });

        for (int i = 0; i < indicesOfKnownValues.size(); i++) { // possible improvement: break, when you reach the
                                                                // relevant label with the lowest score
            attr = getAttr(indicesOfKnownValues.get(i));
            if (attr.getNominal(tuple) == 0) {
                wrongPairs += nbIrrelevant;
                nbRelevant++;
            }
            else {
                nbIrrelevant++;
            }
        }
        if (nbRelevant > 0 && nbIrrelevant > 0) {
            m_NonnormalisedLoss += ((double) wrongPairs) / (nbRelevant * nbIrrelevant);
        }
        m_NbKnown++;
    }


    public void addExample(DataTuple tuple, DataTuple pred) {
        try {
            throw new Exception("RankingLoss.addExample(DataTuple tuple, DataTuple pred) cannot be implemented.");
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // NEDOTAKNJENO
    public void addInvalid(DataTuple tuple) {
    }

}
