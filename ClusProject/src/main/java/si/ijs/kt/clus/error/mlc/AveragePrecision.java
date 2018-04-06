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
import si.ijs.kt.clus.util.format.ClusFormat;


/**
 * @author matejp
 *
 *         Average precision is used in multi-label classification scenario.
 */
public class AveragePrecision extends ClusNominalError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double m_NonnormalisedPrec; // sum over samples sample_i of the terms t_i = 1 / |Y_i| * sum_(lambda_j in
                                          // Y_i) u_ij, where u_ij = |L_ij| / rank(lambda_j),
                                          // where Y_i is the true set of labels that are relevant for sample_i and L_ij
                                          // is the set of labels that have lower or equal
                                          // rank than lambda_ij.

    protected int m_NbKnown; // number of the examples seen with at least one known target value


    public AveragePrecision(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_NonnormalisedPrec = 0.0;
        m_NbKnown = 0;
    }


    @Override
    public boolean shouldBeLow() {
        return false;
    }


    @Override
    public void reset() {
        m_NonnormalisedPrec = 0.0;
        m_NbKnown = 0;
    }


    @Override
    public void add(ClusError other) {
        AveragePrecision ap = (AveragePrecision) other;
        m_NonnormalisedPrec += ap.m_NonnormalisedPrec;
        m_NbKnown += ap.m_NbKnown;
    }


    // NEDOTAKNJENO
    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }
    // // A MA TO SPLOH SMISU?
    // public double getAveragePrecision(int i) {
    // return getModelErrorComponent(i);
    // }


    @Override
    public double getModelError() {
        return m_NonnormalisedPrec / m_NbKnown;
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
    }


    @Override
    public String getName() {
        return "AveragePrecision";
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new AveragePrecision(par, m_Attrs);
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        final double[] scores = ((ClassificationStat) pred).calcScores();
        ArrayList<Integer> indicesOfKnownValues = new ArrayList<Integer>();
        boolean[] isRelevant = new boolean[m_Dim];
        NominalAttrType attr;
        // find (indices of) known values and relevant labels
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                indicesOfKnownValues.add(i);
                if (attr.getNominal(tuple) == 0) {
                    isRelevant[i] = true;
                }
            }
        }
        // sort with respect to the scores
        Collections.sort(indicesOfKnownValues, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return -Double.compare(scores[o1], scores[o2]);
            }
        });
        int nbOfRelevant = 0;
        double u = 0.0;
        if (indicesOfKnownValues.size() > 0) {
            for (int i = 0; i < indicesOfKnownValues.size(); i++) {
                if (isRelevant[indicesOfKnownValues.get(i)]) {
                    nbOfRelevant++;
                    u += ((double) nbOfRelevant) / (i + 1);
                }
            }
            m_NonnormalisedPrec += u / nbOfRelevant;
            m_NbKnown++;
        }

    }


    @Override
    public void addExample(DataTuple tuple, DataTuple pred) {
        try {
            throw new Exception("AveragePrecision.addExample(DataTuple tuple, DataTuple pred) cannot be implemented.");
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // NEDOTAKNJENO
    @Override
    public void addInvalid(DataTuple tuple) {
    }

}
