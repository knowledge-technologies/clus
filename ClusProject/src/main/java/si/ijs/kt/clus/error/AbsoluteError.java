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

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNumericError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.format.ClusNumberFormat;


public class AbsoluteError extends ClusNumericError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double[] m_AbsError;
    protected double[] m_AbsDefError;


    public AbsoluteError(ClusErrorList par, NumericAttrType[] num) {
        this(par, num, "");
    }


    public AbsoluteError(ClusErrorList par, NumericAttrType[] num, String info) {
        super(par, num);
        m_AbsError = new double[m_Dim];
        m_AbsDefError = new double[m_Dim];

        setAdditionalInfo(info);
    }


    @Override
    public double getModelError() {
        double avg_abs = 0.0;
        for (int i = 0; i < m_Dim; i++)
            avg_abs += m_AbsError[i];
        return avg_abs / m_Dim;
    }


    @Override
    public void addExample(double[] real, double[] predicted) {
        for (int i = 0; i < m_Dim; i++) {
            double err = real[i] - predicted[i];
            m_AbsError[i] += Math.abs(err);
        }
    }


    @Override
    public void addInvalid(DataTuple tuple) {
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        double[] predicted = pred.getNumericPred();
        for (int i = 0; i < m_Dim; i++) {
            double err = m_Attrs[i].getNumeric(tuple) - predicted[i];
            m_AbsError[i] += Math.abs(err);
        }
    }


    @Override
    public void add(ClusError other) {
        AbsoluteError oe = (AbsoluteError) other;
        for (int i = 0; i < m_Dim; i++) {
            m_AbsError[i] += oe.m_AbsError[i];
            m_AbsDefError[i] += oe.m_AbsDefError[i];
        }
    }


    public void showDefaultError(PrintWriter out, boolean detail) {
        out.println(getPrefix() + DEFAULT_ERROR + DEFAULT_POSTFIX + showDoubleArray(m_AbsDefError));
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
    	ClusNumberFormat fr = getFormat();
        StringBuffer buf = new StringBuffer();
        buf.append(showDoubleArray(m_AbsError, getNbExamples()));
        if (m_Dim > 1) {
            buf.append(": " + fr.format(getModelError() / getNbExamples()));
        }
        out.println(buf.toString());
    }


    public void showRelativeError(PrintWriter out, boolean detail) {
        out.println(getPrefix() + RELATIVE_ERROR + RELATIVE_POSTFIX + showDoubleArray(m_AbsError, m_AbsDefError));
    }


    public void showSummaryError(PrintWriter out, boolean detail) {
    	ClusNumberFormat fr = getFormat();
        double ss_def = 0.0;
        double ss_tree = 0.0;
        for (int i = 0; i < m_Dim; i++) {
            ss_tree += m_AbsError[i];
            ss_def += m_AbsDefError[i];
        }
        double re = ss_def != 0.0 ? ss_tree / ss_def : 0.0;
        out.println(getPrefix() + "Sum over components RE: " + fr.format(re) + " = " + fr.format(ss_tree) + " / " + fr.format(ss_def));
    }


    public double getSummaryError() {
        double ss_tree = 0.0;
        for (int i = 0; i < m_Dim; i++) {
            ss_tree += m_AbsError[i];
        }
        return ss_tree;
    }


    @Override
    public String getName() {
        return "Mean absolute error (MAE)" + getAdditionalInfoFormatted();
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new AbsoluteError(par, m_Attrs, getAdditionalInfo());
    }


    @Override
    public boolean shouldBeLow() {
        return true;
    }
}
