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

import java.text.*;
import java.util.ArrayList;

import clus.main.Settings;
import clus.statistic.*;
import clus.util.*;
import clus.data.cols.*;
import clus.data.rows.*;

public class MultiScoreStat extends ClusStatistic {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int m_NbTarget;
	protected int[] m_Score;
	protected double[] m_MeanValues;

	public MultiScoreStat(ClusStatistic stat, MultiScore score) {
		m_MeanValues = stat.getNumericPred();
		m_NbTarget = m_MeanValues.length;
		m_Score = score.multiScore(m_MeanValues);
	}


	public String getArrayOfStatistic() {
		return null;
	}


	public String getString(StatisticPrintInfo info) {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < m_NbTarget; i++) {
			if (i != 0) buf.append(",");
			buf.append(1-m_Score[i]);
		}
		buf.append("] : [");
		for (int i = 0; i < m_NbTarget; i++) {
			if (i != 0) buf.append(",");
//			buf.append(fr.format(m_Target.transform(m_MeanValues[i], i)));
			buf.append(fr.format(m_MeanValues[i]));
		}
		buf.append("]");
		return buf.toString();

	}

	public String getPredictedClassName(int idx) {
		return "";
	}

	public double[] getNumericPred() {
		return m_MeanValues;
	}

	public int[] getNominalPred() {
		return m_Score;
	}

	public boolean samePrediction(ClusStatistic other) {
		MultiScoreStat or = (MultiScoreStat)other;
		for (int i = 0; i < m_NbTarget; i++)
			if (m_Score[i] != or.m_Score[i]) return false;
		return true;
	}

	public ClusStatistic cloneStat() {
		return null;
	}

	public void update(ColTarget target, int idx) {
	}

	public void updateWeighted(DataTuple tuple, int idx) {
	}

	public void calcMean() {
	}

	public void reset() {
	}

	public void copy(ClusStatistic other) {
	}

	public void addPrediction(ClusStatistic other, double weight) {
	}

	public void add(ClusStatistic other) {
	}

	public void addScaled(double scale, ClusStatistic other) {
	}

	public void subtractFromThis(ClusStatistic other) {
	}

	public void subtractFromOther(ClusStatistic other) {
	}

	public void vote(ArrayList votes) {
		System.err.println(getClass().getName() + "vote (): Not implemented");
	}

	public void updateWeighted(SparseDataTuple tuple, int idx) {
	}
}
