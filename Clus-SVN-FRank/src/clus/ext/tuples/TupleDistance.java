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

package clus.ext.tuples;

import clus.data.rows.DataTuple;
import clus.data.type.TupleAttrType;
import clus.ext.timeseries.TimeSeries;
import clus.main.Settings;
import clus.statistic.ClusDistance;
import clus.statistic.ClusStatistic;
import clus.statistic.ClusStructuredDistance;

public abstract class TupleDistance extends ClusStructuredDistance {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected TupleAttrType m_Attr;

	public TupleDistance(TupleAttrType attr) {
		m_Attr = attr;
	}

	public abstract double calcDistance(Tuple t1, Tuple t2);

	public double calcDistance(Object t1, Object t2){
		Tuple s1 = (Tuple)t1;
		Tuple s2 = (Tuple)t2;
		return calcDistance(s1, s2);
	}

	
	public double calcDistance(DataTuple t1, DataTuple t2) {
		Tuple s1 = m_Attr.getTuple(t1);
		Tuple s2 = m_Attr.getTuple(t2);
		return calcDistance(s1, s2);
	}

	public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
		Tuple tu1 = m_Attr.getTuple(t1);
		TupleStatistic stat = (TupleStatistic)s2;
		return calcDistance(tu1, stat.getRepresentativeMedoid());
	}
}
