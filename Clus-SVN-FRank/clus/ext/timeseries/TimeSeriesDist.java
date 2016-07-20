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

import clus.data.rows.DataTuple;
import clus.data.type.TimeSeriesAttrType;
import clus.main.Settings;
import clus.statistic.ClusDistance;
import clus.statistic.ClusStatistic;

public abstract class TimeSeriesDist extends ClusDistance {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected TimeSeriesAttrType m_Attr;

	public TimeSeriesDist(TimeSeriesAttrType attr) {
		m_Attr = attr;
	}

	public abstract double calcDistance(TimeSeries t1, TimeSeries t2);

	public double calcDistance(DataTuple t1, DataTuple t2) {
		TimeSeries ts1 = m_Attr.getTimeSeries(t1);
		TimeSeries ts2 = m_Attr.getTimeSeries(t2);
		return calcDistance(ts1, ts2);
	}

	public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
		TimeSeries ts1 = m_Attr.getTimeSeries(t1);
		TimeSeriesStat stat = (TimeSeriesStat)s2;
		return calcDistance(ts1, stat.getRepresentativeMedoid());
	}
}
