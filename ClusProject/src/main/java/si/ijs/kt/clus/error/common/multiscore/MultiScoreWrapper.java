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

package si.ijs.kt.clus.error.common.multiscore;

import java.io.PrintWriter;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNominalError;
import si.ijs.kt.clus.error.common.ClusNumericError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.exception.ClusException;


public class MultiScoreWrapper extends ClusNumericError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClusNominalError m_Child;
    protected byte[] m_Real;
    protected int[] m_Pred;


    public MultiScoreWrapper(ClusNominalError child, NumericAttrType[] num) {
        super(child.getParent(), num);
        int dim = getDimension();
        m_Real = new byte[dim];
        m_Pred = new int[dim];
        m_Child = child;
    }


    @Override
    public boolean shouldBeLow() {
        return m_Child.shouldBeLow();
    }


    @Override
    public void reset() {
        m_Child.reset();
    }


    @Override
    public double getModelError() {
        return m_Child.getModelError();
    }


    @Override
    public void addExample(double[] real, double[] predicted) {
        for (int i = 0; i < m_Real.length; i++) {
            m_Real[i] = (byte) (real[i] > 0.5 ? 0 : 1);
            m_Pred[i] = predicted[i] > 0.5 ? 0 : 1;
        }
        // m_Child.addExample(m_Real, m_Pred);
    }


    @Override
    public void addInvalid(DataTuple tuple) {
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        // double[] predicted = pred.getNumericPred();
        for (int i = 0; i < m_Dim; i++) {
            // double err = m_Attrs[i].getNumeric(tuple) - predicted[i];
            // m_AbsError[i] += Math.abs(err);
        }
    }


    @Override
    public void add(ClusError other) {
        MultiScoreWrapper oe = (MultiScoreWrapper) other;
        m_Child.add(oe.m_Child);
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        m_Child.showModelError(out, detail);
    }


    // public boolean hasSummary() {
    // m_Child.hasSummary();
    // }

    @Override
    public String getName() {
        return m_Child.getName();
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) throws ClusException {
        return new MultiScoreWrapper((ClusNominalError) m_Child.getErrorClone(par), m_Attrs);
    }
}
