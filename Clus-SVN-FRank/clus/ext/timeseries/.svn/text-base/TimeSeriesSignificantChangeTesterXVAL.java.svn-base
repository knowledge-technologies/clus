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

package clus.ext.timeseries;

import java.io.*;
import java.util.*;

import clus.error.*;
import clus.algo.rules.ClusRule;
import clus.data.rows.*;
import clus.data.type.ClusSchema;
import clus.data.type.TimeSeriesAttrType;
import clus.main.*;
import clus.model.ClusModel;
import clus.selection.XValMainSelection;
import clus.selection.XValRandomSelection;
import clus.selection.XValSelection;
import clus.util.ClusException;

public class TimeSeriesSignificantChangeTesterXVAL extends ClusError {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected TimeSeriesStat m_Stat;
	protected ArrayList m_FoldsMean, m_FoldsMedoid;

	public TimeSeriesSignificantChangeTesterXVAL(ClusErrorList par, TimeSeriesStat stat) {
		super(par);
		m_Stat = stat;
	}

	public boolean isComputeForModel(String name) {
		return false;
	}

	public double computeMeanValue(TimeSeriesAttrType attr, RowData data) {
		int nb = 0;
		double sum = 0;
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			double[] series = attr.getTimeSeries(tuple).getValues();
			for (int j = 0; j < series.length; j++) {
				sum += series[j];
				nb++;
			}
		}
		return sum / nb;
	}

	public int getTimeSeriesLength(TimeSeriesAttrType attr, RowData data) {
		int len = 0;
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			double[] series = attr.getTimeSeries(tuple).getValues();
			len = Math.max(len, series.length);
		}
		return len;
	}

	public double computeError(RowData data, TimeSeries ts) {
		double sum = 0;
		TimeSeriesDist dist = (TimeSeriesDist)m_Stat.getDistance();
		TimeSeriesAttrType attr = m_Stat.getAttribute();
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			TimeSeries series = attr.getTimeSeries(tuple);
			sum += dist.calcDistance(series, ts);
		}
		return sum / data.getNbRows();
	}

	public void doOneFold(RowData train, RowData test) {
		/* create mean training set time series */
		TimeSeriesAttrType attr = m_Stat.getAttribute();
		double mean = computeMeanValue(attr, train);
		double[] mean_series = new double[getTimeSeriesLength(attr, train)];
		Arrays.fill(mean_series, mean);
		TimeSeries mean_ts = new TimeSeries(mean_series);
		/* create medoid training set time series */
		TimeSeriesStat stat = (TimeSeriesStat)m_Stat.cloneStat();
		train.calcTotalStatBitVector(stat);
		stat.calcMean();
		TimeSeries medoid_ts = stat.getRepresentativeMedoid();
		/* compute errors */
		double mean_err = computeError(test, mean_ts);
		double medoid_err = computeError(test, medoid_ts);
		m_FoldsMean.add(new Double(mean_err));
		m_FoldsMedoid.add(new Double(medoid_err));
	}


	public void computeSignificantChangePValueXVAL(RowData data) throws ClusException {
		m_FoldsMean = new ArrayList();
		m_FoldsMedoid = new ArrayList();
		Random random = new Random(0);
		int nbfolds = 10;
		XValMainSelection sel = new XValRandomSelection(data.getNbRows(), nbfolds, random);
		for (int i = 0; i < nbfolds; i++) {
			XValSelection msel = new XValSelection(sel, i);
			RowData train = (RowData)data.cloneData();
			RowData test = (RowData)train.select(msel);
			doOneFold(train, test);
		}
	}

	public void computeForRule(ClusRule rule, ClusSchema schema) throws ClusException {
		RowData covered = new RowData(rule.getData(), schema);
		computeSignificantChangePValueXVAL(covered);
	}

	public void compute(RowData data, ClusModel model) throws ClusException {
		if (model instanceof ClusRule) {
			computeForRule((ClusRule)model, data.getSchema());
		}
	}

	public void printArray(ArrayList arr, StringBuffer res) {
		res.append("[");
		double sum = 0;
		for (int i = 0; i < arr.size(); i++) {
			double v = ((Double)arr.get(i)).doubleValue();
			if (i != 0) res.append(",");
			res.append(String.valueOf(v));
			sum += v;
		}
		res.append("]");
	}

	public void showModelError(PrintWriter wrt, int detail) {
		StringBuffer res = new StringBuffer();
		res.append("\n");
		res.append("   Medoid: ");
		printArray(m_FoldsMedoid, res);
		res.append("\n");
		res.append("   Mean: ");
		printArray(m_FoldsMean, res);
		wrt.println(res.toString());
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new TimeSeriesSignificantChangeTesterXVAL(getParent(), m_Stat);
	}

	public String getName() {
		return "Significant Time Change XVAL";
	}
}
