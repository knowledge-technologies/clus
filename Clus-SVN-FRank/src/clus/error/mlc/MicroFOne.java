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
import java.util.Arrays;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.settings.Settings;
import clus.error.common.ClusError;
import clus.error.common.ClusErrorList;
import clus.error.common.ClusNominalError;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;


/**
 * @author matejp
 * 
 */
public class MicroFOne extends ClusNominalError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected int[] m_NbTruePositives, m_NbFalsePositives, m_NbFalseNegatives;


    public MicroFOne(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_NbTruePositives = new int[m_Dim];
        m_NbFalsePositives = new int[m_Dim];
        m_NbFalseNegatives = new int[m_Dim];
    }


    public boolean shouldBeLow() {
        return false;
    }


    public void reset() {
        Arrays.fill(m_NbTruePositives, 0);
        Arrays.fill(m_NbFalsePositives, 0);
        Arrays.fill(m_NbFalseNegatives, 0);
    }


    public void add(ClusError other) {
        MicroFOne mF1 = (MicroFOne) other;
        for (int i = 0; i < m_Dim; i++) {
            m_NbTruePositives[i] += mF1.m_NbTruePositives[i];
            m_NbFalsePositives[i] += mF1.m_NbFalsePositives[i];
            m_NbFalseNegatives[i] += mF1.m_NbFalseNegatives[i];
        }
    }


    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }


    public double getMicroFOne(int i) {
        throw new RuntimeException("Not implemented.");
        // return getModelErrorComponent(i);
    }

    public double getModelError() {
        int truePositives = 0, falsePositives = 0, falseNegatives = 0;
        for (int i = 0; i < m_Dim; i++) {
            truePositives += m_NbTruePositives[i];
            falsePositives += m_NbFalsePositives[i];
            falseNegatives += m_NbFalseNegatives[i];
        }
        double precision = ((double) truePositives) / (truePositives + falsePositives);
        double recall = ((double) truePositives) / (truePositives + falseNegatives);
        return 2.0 * precision * recall / (precision + recall);
    }


    public void showModelError(PrintWriter out, int detail) {
        out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
    }


    public String getName() {
        return "MicroFOne";
    }


    public ClusError getErrorClone(ClusErrorList par) {
        return new MicroFOne(par, m_Attrs);
    }


    public void addExample(DataTuple tuple, ClusStatistic pred) {
        int[] predicted = pred.getNominalPred();
        NominalAttrType attr;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                if (attr.getNominal(tuple) == 0) { // label relevant
                    if (predicted[i] == 0) {
                        m_NbTruePositives[i]++;
                    }
                    else {
                        m_NbFalseNegatives[i]++;
                    }
                }
                else if (predicted[i] == 0) {
                    m_NbFalsePositives[i]++;
                }
            }
        }
    }


    public void addExample(DataTuple tuple, DataTuple pred) {
        NominalAttrType attr;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                if (attr.getNominal(tuple) == 0) { // label relevant
                    if (attr.getNominal(pred) == 0) {
                        m_NbTruePositives[i]++;
                    }
                    else {
                        m_NbFalseNegatives[i]++;
                    }
                }
                else if (attr.getNominal(pred) == 0) {
                    m_NbFalsePositives[i]++;
                }
            }
        }
    }


    public void addInvalid(DataTuple tuple) {
    }
}
