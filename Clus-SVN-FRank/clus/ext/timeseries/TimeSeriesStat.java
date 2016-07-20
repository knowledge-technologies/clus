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

import java.text.NumberFormat;
import java.util.*;

import clus.data.attweights.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.ext.sspd.*;
import clus.main.*;
import clus.statistic.*;
import clus.util.*;

public class TimeSeriesStat extends SumPairwiseDistancesStat {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	// m_RepresentativeMean is the time series representing the cluster

	// TODO: Investigate the usage of Medoid vs. mean?

	protected TimeSeriesAttrType m_Attr;
	private ArrayList m_TimeSeriesStack = new ArrayList();
	public TimeSeries m_RepresentativeMean = new TimeSeries("[]");
	public TimeSeries m_RepresentativeMedoid = new TimeSeries("[]");

	// public TimeSeries m_RepresentativeQuantitve=new TimeSeries("[]");

	protected double m_AvgDistances;

	public TimeSeriesStat(TimeSeriesAttrType attr, ClusDistance dist, int efflvl) {
		super(dist, efflvl);
		m_Attr = attr;
	}

	public ClusStatistic cloneStat() {
		TimeSeriesStat stat = new TimeSeriesStat(m_Attr, m_Distance, m_Efficiency);
		stat.cloneFrom(this);
		return stat;
	}

	public ClusStatistic cloneSimple() {
		TimeSeriesStat stat = new TimeSeriesStat(m_Attr, m_Distance, m_Efficiency);
		stat.m_RepresentativeMean = new TimeSeries(m_RepresentativeMean.length());
		stat.m_RepresentativeMedoid = new TimeSeries(m_RepresentativeMedoid.length());
		return stat;
	}

	public void copy(ClusStatistic other) {
		TimeSeriesStat or = (TimeSeriesStat)other;
		super.copy(or);
		//m_Value = or.m_Value;
		//m_AvgDistances = or.m_AvgDistances;
		//m_AvgSqDistances = or.m_AvgSqDistances;
		m_TimeSeriesStack.clear();
		m_TimeSeriesStack.addAll(or.m_TimeSeriesStack);
		// m_RepresentativeMean = or.m_RepresentativeMean;
		// m_RepresentativeMedoid = or.m_RepresentativeMedoid;
	}

	/**
	 * Used for combining weighted predictions.
	 */
	public TimeSeriesStat normalizedCopy() {
		TimeSeriesStat copy = (TimeSeriesStat)cloneSimple();
		copy.m_NbExamples = 0;
		copy.m_SumWeight = 1;
		copy.m_TimeSeriesStack.add(getTimeSeriesPred());
		copy.m_RepresentativeMean.setValues(m_RepresentativeMean.getValues());
		copy.m_RepresentativeMedoid.setValues(m_RepresentativeMedoid.getValues());
		return copy;
	}

	public void addPrediction(ClusStatistic other, double weight) {
		TimeSeriesStat or = (TimeSeriesStat)other;
		m_SumWeight += weight*or.m_SumWeight;
		TimeSeries pred = new TimeSeries(or.getTimeSeriesPred());
		pred.setTSWeight(weight);
		m_TimeSeriesStack.add(pred);
	}

	/*
	 * Add a weighted time series to the statistic.
	 */
	public void updateWeighted(DataTuple tuple, int idx){
		super.updateWeighted(tuple,idx);
	    TimeSeries newTimeSeries = new TimeSeries((TimeSeries)tuple.m_Objects[0]);
	    newTimeSeries.setTSWeight(tuple.getWeight());
	    m_TimeSeriesStack.add(newTimeSeries);
	}

	public double calcDistance(TimeSeries ts1, TimeSeries ts2) {
		TimeSeriesDist dist = (TimeSeriesDist)getDistance();
		return dist.calcDistance(ts1, ts2);
	}

	/**
	 * Currently only used to compute the default dispersion within rule heuristics.
	 */
	public double getDispersion(ClusAttributeWeights scale, RowData data) {
		return getSVarS(scale, data);
	}

	public double getAbsoluteDistance(DataTuple tuple, ClusAttributeWeights weights) {
		int idx = m_Attr.getIndex();
		TimeSeries actual = (TimeSeries)tuple.getObjVal(0);
		return calcDistance(m_RepresentativeMean, actual) * weights.getWeight(idx);
	}

	public void initNormalizationWeights(ClusAttributeWeights weights, boolean[] shouldNormalize) {
		int idx = m_Attr.getIndex();
		if (shouldNormalize[idx]) {
			double var = m_SVarS / getTotalWeight();
			double norm = var > 0 ? 1/var : 1; // No normalization if variance = 0;
			weights.setWeight(m_Attr, norm);
		}
	}

	public void calcSumAndSumSqDistances(TimeSeries prototype) {
		m_AvgDistances = 0.0;
		int count = m_TimeSeriesStack.size();
		for (int i = 0; i < count; i++){
			double dist = calcDistance(prototype,(TimeSeries)m_TimeSeriesStack.get(i));
			m_AvgDistances += dist;
		}
		m_AvgDistances /= count;
	}

	/*
	 * [Aco]
	 * this is executed in the end
	 * @see clus.statistic.ClusStatistic#calcMean()
	 */
	public void calcMean() {
		// Medoid
		m_RepresentativeMedoid = null;
		double minDistance = Double.POSITIVE_INFINITY;
		for(int i=0; i<m_TimeSeriesStack.size(); i++){
			double crDistance = 0.0;
			TimeSeries t1 = (TimeSeries)m_TimeSeriesStack.get(i);
			for (int j=0; j<m_TimeSeriesStack.size(); j++){
				TimeSeries t2 = (TimeSeries)m_TimeSeriesStack.get(j);
				double dist = calcDistance(t1, t2);
				crDistance += dist * t2.geTSWeight();
			}
			if (crDistance<minDistance) {
				m_RepresentativeMedoid = (TimeSeries)m_TimeSeriesStack.get(i);
				minDistance = crDistance;
			}
		}
		calcSumAndSumSqDistances(m_RepresentativeMedoid);
		// Mean
		if (m_Attr.isEqualLength()){
			m_RepresentativeMean.setSize(m_RepresentativeMedoid.length());
			for (int i=0; i< m_RepresentativeMean.length(); i++){
				double sum = 0.0;
				for(int j=0; j<m_TimeSeriesStack.size(); j++){
					TimeSeries t1 = (TimeSeries)m_TimeSeriesStack.get(j);
					sum += t1.getValue(i) * t1.geTSWeight();
				}
		    	m_RepresentativeMean.setValue(i, sum/m_SumWeight);
		    }
		}
		double sumwi = 0.0;
		for(int j=0; j<m_TimeSeriesStack.size(); j++){
			TimeSeries t1 = (TimeSeries)m_TimeSeriesStack.get(j);
			sumwi += t1.geTSWeight();
		}
		double diff = Math.abs(m_SumWeight-sumwi);
		if (diff > 1e-6) {
			System.err.println("Error: Sanity check failed! - "+diff);
		}

		// Qualitative distance
/*
		double[][] m_RepresentativeQualitativeMatrix = new double[m_RepresentativeMean.length()][m_RepresentativeMean.length()];
		for(int i=0;i<m_RepresentativeMean.length();i++){
			for(int j=0;j<m_RepresentativeMean.length();j++){
				m_RepresentativeQualitativeMatrix[i][j]=0;
			}
		}
		for(int i=0; i<TimeSeriesStack.size();i++){
			TimeSeries newTemeSeries = (TimeSeries)TimeSeriesStack.get(i);
			for (int j = 0; j < newTemeSeries.length(); j++) {
				for (int k = 0; k < newTemeSeries.length(); k++) {
					m_RepresentativeQualitativeMatrix[j][k]+=Math.signum(newTemeSeries.getValue(k) - newTemeSeries.getValue(j));
				}
			}
		}
		double tmpMaxValue=(double)(m_RepresentativeQualitativeMatrix.length - 1);
		m_RepresentativeQuantitve=new TimeSeries(m_RepresentativeQualitativeMatrix.length);
		for (int i=0;i<m_RepresentativeQualitativeMatrix.length;i++){
			int numSmaller=0;
			int numBigger=0;
			for (int j=0; j<m_RepresentativeQualitativeMatrix.length;j++){
				if (m_RepresentativeQualitativeMatrix[i][j]>0){numBigger++;}
				if (m_RepresentativeQualitativeMatrix[i][j]<0){numSmaller++;}
			}
			m_RepresentativeQuantitve.setValue(i,((double)(numSmaller+tmpMaxValue-numBigger))/2);
		}
		m_RepresentativeQuantitve.rescale(m_RepresentativeMedoid.min(),m_RepresentativeMedoid.max());
*/
	}

	public void reset() {
		super.reset();
		m_TimeSeriesStack.clear();
	}

	/*
	 * [Aco]
	 * for printing in the nodes
	 * @see clus.statistic.ClusStatistic#getString(clus.statistic.StatisticPrintInfo)
	 */
	public String getString(StatisticPrintInfo info){
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		StringBuffer buf = new StringBuffer();
		buf.append("Mean: ");
		buf.append(m_RepresentativeMean.toString());
		if (info.SHOW_EXAMPLE_COUNT) {
			buf.append(": ");
			buf.append(fr.format(m_SumWeight));
		}
		buf.append("; ");

		buf.append("Medoid: ");
		buf.append(m_RepresentativeMedoid.toString());
		if (info.SHOW_EXAMPLE_COUNT) {
			buf.append(": ");
			buf.append(fr.format(m_SumWeight));
			buf.append(", ");
			buf.append(fr.format(m_AvgDistances));
		}
		buf.append("; ");
/*
		buf.append("Quantitive: ");
		buf.append(m_RepresentativeQuantitve.toString());
		if (info.SHOW_EXAMPLE_COUNT) {
			buf.append(": ");
			buf.append(fr.format(m_SumWeight));
		}
		buf.append("; ");
*/
		return buf.toString();
	}

	public void addPredictWriterSchema(String prefix, ClusSchema schema) {
		schema.addAttrType(new TimeSeriesAttrType(prefix+"-p-TimeSeries"));
		schema.addAttrType(new NumericAttrType(prefix+"-p-Distance"));
		schema.addAttrType(new NumericAttrType(prefix+"-p-Size"));
		schema.addAttrType(new NumericAttrType(prefix+"-p-AvgDist"));
	}

	public String getPredictWriterString(DataTuple tuple) {
		StringBuffer buf = new StringBuffer();
		buf.append(m_RepresentativeMedoid.toString());
		double dist = calcDistanceToCentroid(tuple);
		buf.append(",");
		buf.append(dist);
		buf.append(",");
		buf.append(getTotalWeight());
		buf.append(",");
		buf.append(m_AvgDistances);
		return buf.toString();
	}

	public TimeSeries getRepresentativeMean() {
		return m_RepresentativeMean;
	}

	public TimeSeries getRepresentativeMedoid() {
		return m_RepresentativeMedoid;
	}

	public TimeSeries getTimeSeriesPred() {
		return m_RepresentativeMedoid;
	}

	public TimeSeriesAttrType getAttribute() {
		return m_Attr;
	}
}
