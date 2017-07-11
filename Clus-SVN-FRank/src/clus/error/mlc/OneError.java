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

package clus.error.mlc;

import java.io.PrintWriter;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.settings.Settings;
import clus.error.common.ClusError;
import clus.error.common.ClusErrorList;
import clus.error.common.ClusNominalError;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;


/**
 * @author matejp
 *
 *         One_error is used in multi-label classification scenario.
 */
public class OneError extends ClusNominalError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected int m_NbWrong; // sum over samples sample_i of the terms INDICATOR[(arg max_j proportion of samples with
                             // label_j in the leaf which sample_i belongs to) is not an element of Y_i],
                             // where Y_i is the true set of relevant labels for sample_i,
                             // i.e., the proportion of misclassified top label.

    protected int m_NbKnown; // number of the examples seen


    public OneError(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_NbWrong = 0;
        m_NbKnown = 0;
    }


    public boolean shouldBeLow() {
        return true;
    }


    public void reset() {
        m_NbWrong = 0;
        m_NbKnown = 0;
    }


    public void add(ClusError other) {
        OneError oe = (OneError) other;
        m_NbWrong += oe.m_NbWrong;
        m_NbKnown += oe.m_NbKnown;
    }


    // NEDOTAKNJENO
    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }
    // // A MA TO SPLOH SMISU?
    // public double getOneError(int i) {
    // return getModelErrorComponent(i);
    // }


    public double getModelError() {
        return ((double) m_NbWrong) / m_NbKnown;
    }


    public void showModelError(PrintWriter out, int detail) {
        out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
    }


    public String getName() {
        return "OneError";
    }


    public ClusError getErrorClone(ClusErrorList par) {
        return new OneError(par, m_Attrs);
    }


    public void addExample(DataTuple tuple, ClusStatistic pred) {
        int[] predicted = pred.getNominalPred(); // Codomain is {"1", "0"} - see clus.data.type.NominalAtterType
                                                 // constructor
        double[] scores = ((ClassificationStat) pred).calcScores();
        int maxScoreLabel = -1;
        double maxScore = -1.0; // something < 0
        NominalAttrType attr;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                if (scores[i] > maxScore) {
                    maxScoreLabel = i;
                    maxScore = scores[i];
                }
            }
        }
        if (maxScoreLabel >= 0) { // at least one label value is non-missing
            attr = getAttr(maxScoreLabel);
            if (attr.getNominal(tuple) != predicted[maxScoreLabel]) {
                m_NbWrong++;
            }
            m_NbKnown++;
        }
    }


    public void addExample(DataTuple tuple, DataTuple pred) {
        try {
            throw new Exception("OneError.addExample(DataTuple tuple, DataTuple pred) cannot be implemented.");
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
