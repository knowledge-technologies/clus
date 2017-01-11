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

public class QDMTimeSeriesDist extends TimeSeriesDist {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public QDMTimeSeriesDist(TimeSeriesAttrType attr) {
		super(attr);
	}

	public double calcDistance(TimeSeries t1, TimeSeries t2) {
		// Ljupco's measure if the time series are the same length
		// my proposal if they are not is cyclic, to be defined with Ljupco
		double[] vt1 = t1.getValuesNoCopy();
		double[] vt2 = t2.getValuesNoCopy();
		int m = Math.max(vt1.length, vt2.length);
		int n = Math.min(vt1.length, vt2.length);
		double distance = 0;
		for (int i = 0; i < m; i++) {
			for (int j = i + 1; j < m; j++) {
				//x=y means x=y
				//distance += Math.abs(Math.signum(vt1[j] - vt1[i]) - Math.signum(vt2[j % n] - vt2[i % n]));
				//x=y means ABS((x/y)-1)<0.02
				distance += Math.abs(diff(vt1[j],vt1[i]) - diff(vt2[j % n],vt2[i % n]));
			}
		}
		distance = distance / (m * (m-1));
		return distance;
	}

	public static int diff(double a, double b ){
		if (a==0 && b==0) return 0;
		if (b!=0 && Math.abs((a/b)-1)<0.02)
			return 0;
		else if (a<b)
			return -1;
		else
			return 1;
	}

	public String getDistanceName() {
		return "QDMTimeSeriesDist";
	}
}
