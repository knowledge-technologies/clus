/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package clus.error.multiscore;

import clus.error.*;
import clus.main.*;
import clus.statistic.ClusStatistic;
import clus.data.rows.DataTuple;
import clus.data.type.*;

import java.io.*;

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

	public boolean shouldBeLow() {
		return m_Child.shouldBeLow();
	}

	public void reset() {
		m_Child.reset();
	}

	public double getModelError() {
		return m_Child.getModelError();
	}

	public void addExample(double[] real, double[] predicted) {
		for (int i = 0; i < m_Real.length; i++) {
			m_Real[i] = (byte)(real[i] > 0.5 ? 0 : 1);
			m_Pred[i] = predicted[i] > 0.5 ? 0 : 1;
		}
		// m_Child.addExample(m_Real, m_Pred);
	}

	public void addInvalid(DataTuple tuple) {
	}

	public void addExample(DataTuple tuple, ClusStatistic pred) {
		// double[] predicted = pred.getNumericPred();
		for (int i = 0; i < m_Dim; i++) {
			// double err = m_Attrs[i].getNumeric(tuple) - predicted[i];
			// m_AbsError[i] += Math.abs(err);
		}
	}

	public void add(ClusError other) {
		MultiScoreWrapper oe = (MultiScoreWrapper)other;
		m_Child.add(oe.m_Child);
	}

	public void showModelError(PrintWriter out, int detail) {
		m_Child.showModelError(out, detail);
	}

//	public boolean hasSummary() {
//		m_Child.hasSummary();
//	}

	public String getName() {
		return m_Child.getName();
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new MultiScoreWrapper((ClusNominalError)m_Child.getErrorClone(par), m_Attrs);
	}
}
