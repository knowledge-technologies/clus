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

package clus.data.type;

import java.io.IOException;
import java.io.PrintWriter;

import clus.data.io.ClusReader;
import clus.data.rows.DataTuple;
import clus.io.ClusSerializable;
import clus.main.Settings;
import clus.util.ClusException;
import clus.ext.timeseries.*;

public class TimeSeriesAttrType extends ClusAttrType {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static String THIS_TYPE_NAME = "TimeSeries";
	public final static int THIS_TYPE = 5;

	public static boolean m_isEqualLength = true;
	int m_Length = -1;

	public TimeSeriesAttrType(String name) {
		super(name);
	}

	public ClusAttrType cloneType() {
		TimeSeriesAttrType tsat = new TimeSeriesAttrType(m_Name);
		return tsat;
	}

	public int getTypeIndex() {
		return THIS_TYPE;
	}

	public int getValueType() {
		return VALUE_TYPE_OBJECT;
	}

	public String getTypeName() {
		return THIS_TYPE_NAME;
	}

	public TimeSeries getTimeSeries(DataTuple tuple) {
		return (TimeSeries)tuple.getObjVal(m_ArrayIndex);
	}

	public void setTimeSeries(DataTuple tuple, TimeSeries value) {
		tuple.setObjectVal(value, m_ArrayIndex);
	}

	public String getString(DataTuple tuple) {
		TimeSeries ts_data = (TimeSeries)tuple.getObjVal(0);
		return ts_data.toString();
	}

	public ClusSerializable createRowSerializable() throws ClusException {
		return new MySerializable();
	}

	public boolean isEqualLength(){
		return m_isEqualLength;
	}

	public class MySerializable extends ClusSerializable {

		public String getString(DataTuple tuple){
			TimeSeries ts_data=(TimeSeries)tuple.getObjVal(0);
			double[] data=ts_data.getValues();
			String str="[";
			for (int k=0;k<data.length;k++){
				str.concat(String.valueOf(data[k]));
				if (k < (data.length-1))str.concat(", ");
			}
			str.concat("]");
			return str;
		}

		public boolean read(ClusReader data, DataTuple tuple) throws IOException {
			String str = data.readTimeSeries();
			if (str == null) return false;
			TimeSeries value = new TimeSeries(str);
			tuple.setObjectVal(value, 0);
			if (m_Length != -1) {
				if (m_Length != value.length()) m_isEqualLength = false;
			} else {
				m_Length = value.length();
			}
			return true;
		}
	}

	public void writeARFFType(PrintWriter wrt) throws ClusException {
		wrt.print("TimeSeries");
	}
}
