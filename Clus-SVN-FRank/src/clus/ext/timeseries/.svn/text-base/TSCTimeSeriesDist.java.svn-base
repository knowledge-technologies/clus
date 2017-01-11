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

import clus.data.type.TimeSeriesAttrType;
import clus.main.Settings;

public class TSCTimeSeriesDist extends TimeSeriesDist {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public TSCTimeSeriesDist(TimeSeriesAttrType attr) {
		super(attr);
	}

	public double calcDistance(TimeSeries t1, TimeSeries t2) {
		//this calculates the Correlation coefficient of two TimeSeries
		//the two TimeSeries have same length
		double[] ts1 = t1.getValues();
		double[] ts2 = t2.getValues();
		double mean_ts1 = calculateMean(ts1);
		double mean_ts2 = calculateMean(ts2);
		double cc = 0;
		double sum_ts1_sqr = 0;
		double sum_ts2_sqr = 0;
		double sum_ts1_ts2 = 0;
		if (ts1.length != ts2.length){
			System.err.println("TimeSeriesCorrelation applies only to Time Series with equal length");
	//		throw new ArrayIndexOutOfBoundsException("TimeSeriesCorrelation applies only to Time Series with equal length");
			System.exit(1);
		}
		for (int k = 0; k < ts1.length; k++){
			sum_ts1_ts2 += (ts1[k] - mean_ts1) * (ts2[k] - mean_ts2);
			sum_ts1_sqr += Math.pow((ts1[k] - mean_ts1),2);
			sum_ts2_sqr += Math.pow((ts2[k] - mean_ts2),2);
		}
		cc = 1 - Math.abs(sum_ts1_ts2/Math.sqrt(sum_ts1_sqr*sum_ts2_sqr));
		return cc;
	}

	/*
	public double calcDistance(TimeSeries t1, TimeSeries t2) {
		//this calculates the Correlation coefficient of two TimeSeries
		//the two TimeSeries may have different lengths - Cycle Arrays

		double[] ts1=t1.getValues();
		double[] ts2=t2.getValues();
		double mean_ts1=calculateMean(ts1);
		double mean_ts2=calculateMean(ts2);
		double cc=0;
		double sum_ts1_sqr=0;
		double sum_ts2_sqr=0;
		double sum_ts1_ts2=0;
		boolean run=true;
		int k=0;
		while (run){
			sum_ts1_ts2+=(ts1[k%ts1.length]-mean_ts1)*(ts2[k%ts2.length]-mean_ts2);
			sum_ts1_sqr+=Math.pow((ts1[k%ts1.length]-mean_ts1),2);
			sum_ts2_sqr+=Math.pow((ts2[k%ts2.length]-mean_ts2),2);
			k++;
			if ((k%ts1.length==0)&&(k%ts2.length==0))run=false;
		}
		cc=1-Math.abs(sum_ts1_ts2/Math.sqrt(sum_ts1_sqr*sum_ts2_sqr));
		return cc;
	}*/

	public double calculateMean(double[] ts){
		double sum = 0;
		for (int k = 0; k < ts.length; k++)sum += ts[k];
		sum = sum / ts.length;
		return sum;
	}

	public String getDistanceName() {
		return "TSCTimeSeriesDist";
	}
}
