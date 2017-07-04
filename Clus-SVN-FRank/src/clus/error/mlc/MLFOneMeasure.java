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
import clus.error.ClusError;
import clus.error.ClusErrorList;
import clus.error.ClusNominalError;
import clus.main.settings.Settings;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;


/**
 * @author matejp
 * 
 *         MLFOneMeasure is used in multi-label classification scenario.
 */
public class MLFOneMeasure extends ClusNominalError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double m_F1Sum; // sum of 2|prediction(sample_i) INTERSECTION target(sample_i)| / (|target(sample_i)| +
                              // |target(sample_i)|),
                              // where prediction (target) of a sample_i is the predicted (true) label set.

    protected int m_NbKnown; // number of the examples seen with at least one known target value


    public MLFOneMeasure(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_F1Sum = 0.0;
        m_NbKnown = 0;
    }


    public boolean shouldBeLow() {
        return false;
    }


    public void reset() {
        m_F1Sum = 0.0;
        m_NbKnown = 0;
    }


    public void add(ClusError other) {
        MLFOneMeasure mlcF1 = (MLFOneMeasure) other;
        m_F1Sum += mlcF1.m_F1Sum;
        m_NbKnown += mlcF1.m_NbKnown;
    }


    // NEDOTAKNJENO
    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }
    // // A MA TO SPLOH SMISU?
    // public double getMLFOneMeasure(int i) {
    // return getModelErrorComponent(i);
    // }


    public double getModelError() {
        return m_F1Sum / m_NbKnown;
    }


    public void showModelError(PrintWriter out, int detail) {
        out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
    }


    public String getName() {
        return "MLFOneMeasure";
    }


    public ClusError getErrorClone(ClusErrorList par) {
        return new MLFOneMeasure(par, m_Attrs);
    }


    public void addExample(DataTuple tuple, ClusStatistic pred) {
        int[] predicted = pred.getNominalPred(); // predicted[i] == 0 IFF label_i is predicted to be relevant for the
                                                 // example
        NominalAttrType attr;
        int intersection = 0, nbRelevant = 0, nbRelevantPredicted = 0;
        boolean atLeastOneKnown = false;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                atLeastOneKnown = true;
                if (attr.getNominal(tuple) == 0) {
                    nbRelevant++;
                    if (predicted[i] == 0) {
                        intersection++;
                    }
                }
                if (predicted[i] == 0) {
                    nbRelevantPredicted++;
                }
            }
        }
        if (atLeastOneKnown) {
            m_F1Sum += nbRelevant + nbRelevantPredicted > 0 ? 2.0 * intersection / (nbRelevant + nbRelevantPredicted) : 1.0; // take
                                                                                                                             // care
                                                                                                                             // of
                                                                                                                             // the
                                                                                                                             // degenerate
                                                                                                                             // case
            m_NbKnown++;
        }
    }


    public void addExample(DataTuple tuple, DataTuple pred) {
        NominalAttrType attr;
        int intersection = 0, nbRelevant = 0, nbRelevantPredicted = 0;
        boolean atLeastOneKnown = false;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                atLeastOneKnown = true;
                if (attr.getNominal(tuple) == 0) {
                    nbRelevant++;
                    if (attr.getNominal(pred) == 0) {
                        intersection++;
                    }
                }
                if (attr.getNominal(pred) == 0) {
                    nbRelevantPredicted++;
                }
            }
        }
        if (atLeastOneKnown) {
            m_F1Sum += nbRelevant + nbRelevantPredicted > 0 ? 2 * intersection / (nbRelevant + nbRelevantPredicted) : 1.0; // take
                                                                                                                           // care
                                                                                                                           // of
                                                                                                                           // the
                                                                                                                           // degenerate
                                                                                                                           // case
            m_NbKnown++;
        }
    }


    // NEDOTAKNJENO
    public void addInvalid(DataTuple tuple) {
    }

}
