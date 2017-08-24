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

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNominalError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusFormat;


/**
 * @author matejp
 * 
 *         MLPrecision is used in multi-label classification scenario.
 */
public class MLPrecision extends ClusNominalError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double m_PrecisionSum; // sum of |prediction(sample_i) INTERSECTION target(sample_i)| /
                                     // |prediction(sample_i)|,
                                     // where prediction (target) of a sample_i is the predicted (true) label set.

    protected int m_NbKnown; // number of the examples seen with at least one known target value


    public MLPrecision(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_PrecisionSum = 0.0;
        m_NbKnown = 0;
    }


    @Override
    public boolean shouldBeLow() {
        return false;
    }


    @Override
    public void reset() {
        m_PrecisionSum = 0.0;
        m_NbKnown = 0;
    }


    @Override
    public void add(ClusError other) {
        MLPrecision mlcr = (MLPrecision) other;
        m_PrecisionSum += mlcr.m_PrecisionSum;
        m_NbKnown += mlcr.m_NbKnown;
    }


    // NEDOTAKNJENO
    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }
    // // A MA TO SPLOH SMISU?
    // public double getMLPrecision(int i) {
    // return getModelErrorComponent(i);
    // }


    @Override
    public double getModelError() {
        return m_PrecisionSum / m_NbKnown;
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
    }


    @Override
    public String getName() {
        return "MLPrecision";
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new MLPrecision(par, m_Attrs);
    }


    @Override
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
                if (predicted[i] == 0) {
                    nbRelevantPredicted++;
                    if (attr.getNominal(tuple) == 0) {
                        intersection++;
                    }
                }
                if (attr.getNominal(tuple) == 0) {
                    nbRelevant++;
                }
            }
        }
        if (atLeastOneKnown) {
            m_PrecisionSum += nbRelevantPredicted != 0 ? ((double) intersection) / nbRelevantPredicted : (nbRelevant == 0 ? 1.0 : 0.0); // take
                                                                                                                                        // care
                                                                                                                                        // of
                                                                                                                                        // degenerated
                                                                                                                                        // cases
            m_NbKnown++;
        }
    }


    @Override
    public void addExample(DataTuple tuple, DataTuple pred) {
        NominalAttrType attr;
        int intersection = 0, nbRelevant = 0, nbRelevantPredicted = 0;
        boolean atLeastOneKnown = false;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                atLeastOneKnown = true;
                if (attr.getNominal(pred) == 0) {
                    nbRelevantPredicted++;
                    if (attr.getNominal(tuple) == 0) {
                        intersection++;
                    }
                }
                if (attr.getNominal(tuple) == 0) {
                    nbRelevant++;
                }
            }
        }
        if (atLeastOneKnown) {
            m_PrecisionSum += nbRelevantPredicted != 0 ? ((double) intersection) / nbRelevantPredicted : (nbRelevant == 0 ? 1.0 : 0.0); // take
                                                                                                                                        // care
                                                                                                                                        // of
                                                                                                                                        // degenerated
                                                                                                                                        // cases
            m_NbKnown++;
        }
    }


    // NEDOTAKNJENO
    @Override
    public void addInvalid(DataTuple tuple) {
    }

}
