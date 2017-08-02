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

package si.ijs.kt.clus.error;

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


public class Accuracy extends ClusNominalError implements ComponentError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected int[] m_NbCorrect;
    protected int[] m_NbKnown;


    public Accuracy(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
        m_NbCorrect = new int[m_Dim];
        m_NbKnown = new int[m_Dim];
    }


    @Override
    public boolean shouldBeLow() {
        return false;
    }


    @Override
    public void reset() {
        Arrays.fill(m_NbCorrect, 0);
        Arrays.fill(m_NbKnown, 0);
    }


    @Override
    public void add(ClusError other) {
        Accuracy acc = (Accuracy) other;
        for (int i = 0; i < m_Dim; i++) {
            m_NbCorrect[i] += acc.m_NbCorrect[i];
            m_NbKnown[i] += acc.m_NbKnown[i];
        }
    }


    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }


    public double getAccuracy(int i) {
        return getModelErrorComponent(i);
    }


    @Override
    public double getModelErrorComponent(int i) {
        // System.out.println("Correct: "+m_NbCorrect[i]+" known: "+m_NbKnown[i]+" nbex: "+getNbExamples());
        return ((double) m_NbCorrect[i]) / m_NbKnown[i];
    }


    @Override
    public double getModelError() {
        double avg = 0.0;
        for (int i = 0; i < m_Dim; i++) {
            avg += getModelErrorComponent(i);
        }
        // System.out.println("in ACCURACY class, error = "+(avg / m_Dim));
        return avg / m_Dim;
    }


    @Override
    public String getName() {
        return "Accuracy";
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new Accuracy(par, m_Attrs);
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        int[] predicted = pred.getNominalPred();
        for (int i = 0; i < m_Dim; i++) {
            NominalAttrType attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                if (attr.getNominal(tuple) == predicted[i])
                    m_NbCorrect[i]++;
                m_NbKnown[i]++;
            }
        }
    }


    @Override
    public void addExample(DataTuple tuple, DataTuple pred) {
        for (int i = 0; i < m_Dim; i++) {
            NominalAttrType attr = getAttr(i);
            if (!attr.isMissing(tuple)) {
                if (attr.getNominal(tuple) == attr.getNominal(pred))
                    m_NbCorrect[i]++;
                m_NbKnown[i]++;
            }
        }
    }


    @Override
    public void addInvalid(DataTuple tuple) {
    }
}
