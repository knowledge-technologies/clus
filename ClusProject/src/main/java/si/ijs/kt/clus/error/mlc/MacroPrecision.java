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
import java.util.Arrays;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNominalError;
import si.ijs.kt.clus.error.common.ComponentError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.format.ClusFormat;


/**
 * @author matejp
 * 
 */
public class MacroPrecision extends ClusNominalError implements ComponentError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected int[] m_NbTruePositives, m_NbFalsePositives;


    public MacroPrecision(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_NbTruePositives = new int[m_Dim];
        m_NbFalsePositives = new int[m_Dim];
    }


    @Override
    public boolean shouldBeLow() {
        return false;
    }


    @Override
    public void reset() {
        Arrays.fill(m_NbTruePositives, 0);
        Arrays.fill(m_NbFalsePositives, 0);
    }


    @Override
    public void add(ClusError other) {
        MacroPrecision mp = (MacroPrecision) other;
        for (int i = 0; i < m_Dim; i++) {
            m_NbTruePositives[i] += mp.m_NbTruePositives[i];
            m_NbFalsePositives[i] += mp.m_NbFalsePositives[i];
        }
    }


    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }


    public double getMacroPrecision(int i) {
        return getModelErrorComponent(i);
    }


    @Override
    public double getModelErrorComponent(int i) {
        return ((double) m_NbTruePositives[i]) / (m_NbTruePositives[i] + m_NbFalsePositives[i]);
    }


    @Override
    public double getModelError() {
        double avg = 0.0;
        for (int i = 0; i < m_Dim; i++) {
            avg += getModelErrorComponent(i);
        }
        return avg / m_Dim;
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        String[] componentErrors = new String[m_Dim];
        for (int i = 0; i < m_Dim; i++) {
            componentErrors[i] = ClusFormat.FOUR_AFTER_DOT.format(getModelErrorComponent(i));
        }
        out.println(String.format("%s: %s", Arrays.toString(componentErrors), ClusFormat.FOUR_AFTER_DOT.format(getModelError())));
    }


    @Override
    public String getName() {
        return "MacroPrecision";
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new MacroPrecision(par, m_Attrs);
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        int[] predicted = pred.getNominalPred();
        NominalAttrType attr;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                if (predicted[i] == 0) { // predicted positive
                    if (attr.getNominal(tuple) == 0) {
                        m_NbTruePositives[i]++;
                    }
                    else {
                        m_NbFalsePositives[i]++;
                    }
                }
            }
        }
    }


    @Override
    public void addExample(DataTuple tuple, DataTuple pred) {
        NominalAttrType attr;
        for (int i = 0; i < m_Dim; i++) {
            attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                if (attr.getNominal(pred) == 0) { // predicted positive
                    if (attr.getNominal(tuple) == 0) {
                        m_NbTruePositives[i]++;
                    }
                    else {
                        m_NbFalsePositives[i]++;
                    }
                }
            }
        }
    }


    @Override
    public void addInvalid(DataTuple tuple) {
    }
}
