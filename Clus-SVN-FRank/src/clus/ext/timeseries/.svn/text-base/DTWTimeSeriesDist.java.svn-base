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

public class DTWTimeSeriesDist extends TimeSeriesDist {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public DTWTimeSeriesDist(TimeSeriesAttrType attr) {
		super(attr);
	}

	/*
	 * For implementation details please see
	 * Yuu Yamada et.al.
	 * Decision-tree Induction from Time-series Data Based on a Standard-example Split Test
	 * http://www.hpl.hp.com/conferences/icml2003/papers/145.pdf
	 *
	 */
	public double calcDistance(TimeSeries t1, TimeSeries t2, int adjustmentWindow) {

		int m = t1.length();
		int n = t2.length();
		double[][] wrappingPathMatrix = new double[m][n];
		double[] vt1 = t1.getValues();
		double[] vt2 = t2.getValues();
		wrappingPathMatrix[0][0]=Math.abs((vt1[0]-vt2[0]))*2;
		int aw = Math.min(m,adjustmentWindow);

		for (int i=1;i<aw;i++){
			wrappingPathMatrix[i][0]=wrappingPathMatrix[i-1][0]+Math.abs((vt1[i]-vt2[0]));
		}

		for (int i=aw;i<m;i++){
			wrappingPathMatrix[i][0]=Double.POSITIVE_INFINITY;
		}

		aw = Math.min(n,adjustmentWindow);
		for (int i=1;i<aw;i++){
			wrappingPathMatrix[0][i]=wrappingPathMatrix[0][i-1]+Math.abs((vt1[0]-vt2[i]));
		}

		for (int i=aw;i<n;i++){
			wrappingPathMatrix[0][i]=Double.POSITIVE_INFINITY;
		}

		for (int k=2;k<m+n-1;k++){
			for (int i=Math.max(k-n+1,1);i<Math.min(k, m);i++){
				if (Math.abs(2*i-k)<=adjustmentWindow){
					double dfk = Math.abs(vt1[i]-vt2[k-i]);
					wrappingPathMatrix[i][k-i]=Math.min(wrappingPathMatrix[i][k-i-1]+dfk, Math.min(wrappingPathMatrix[i-1][k-i]+dfk, wrappingPathMatrix[i-1][k-i-1]+dfk*2));
				}else{
					wrappingPathMatrix[i][k-i]=Double.POSITIVE_INFINITY;
				}
			}
		}
//		if ( wrappingPathMatrix[m-1][n-1] == Double.POSITIVE_INFINITY){
//			System.out.println("SD");
//		}
		return wrappingPathMatrix[m-1][n-1]/(m+n);
	}

	public double calcDistance(TimeSeries t1, TimeSeries t2) {
		return calcDistance(t1,t2,Math.max(Math.max(Math.abs(t1.length()-t2.length())+1,t1.length()/2),t2.length()/2));
	}

	public String getDistanceName() {
		return "DTWTimeSeriesDist";
	}
}
