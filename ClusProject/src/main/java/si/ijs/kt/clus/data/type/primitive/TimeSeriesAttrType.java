/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.data.type.primitive;

import java.io.IOException;
import java.io.PrintWriter;

import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.timeseries.TimeSeries;
import si.ijs.kt.clus.io.ClusSerializable;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;


public class TimeSeriesAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public static boolean m_isEqualLength = true;
    int m_Length = -1;


    public TimeSeriesAttrType(String name) {
        super(name, null);
    }

	public TimeSeriesAttrType(String name, String typeDefinition) {
		super(name, typeDefinition);		
	}
    @Override
    public ClusAttrType cloneType() {
		TimeSeriesAttrType tsat = new TimeSeriesAttrType(m_Name,this.getTypeDefinition());
		return tsat;
	}


    @Override
    public AttributeType getAttributeType() {
        return AttributeType.TimeSeries;
    }


    @Override
    public ValueType getValueType() {
        return ValueType.Object;
    }


    @Override
    public String getTypeName() {
        return getAttributeType().getName();
    }


    public TimeSeries getTimeSeries(DataTuple tuple) {
        return (TimeSeries) tuple.getObjVal(m_ArrayIndex);
    }


    public void setTimeSeries(DataTuple tuple, TimeSeries value) {
        tuple.setObjectVal(value, m_ArrayIndex);
    }


    @Override
    public String getString(DataTuple tuple) {
        TimeSeries ts_data = (TimeSeries) tuple.getObjVal(0);
        return ts_data.toString();
    }


    @Override
    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }


    public boolean isEqualLength() {
        return m_isEqualLength;
    }

    public class MySerializable extends ClusSerializable {

        public String getString(DataTuple tuple) {
            TimeSeries ts_data = (TimeSeries) tuple.getObjVal(0);
            double[] data = ts_data.getValues();
            String str = "[";
            for (int k = 0; k < data.length; k++) {
                str.concat(String.valueOf(data[k]));
                if (k < (data.length - 1))
                    str.concat(", ");
            }
            str.concat("]");
            return str;
        }


        @Override
        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            String str = data.readTimeSeries();
            if (str == null)
                return false;
            TimeSeries value = new TimeSeries(str);
            tuple.setObjectVal(value, getArrayIndex());
            if (m_Length != -1) {
                if (m_Length != value.length())
                    m_isEqualLength = false;
            }
            else {
                m_Length = value.length();
            }
            return true;
        }
    }


    @Override
    public void writeARFFType(PrintWriter wrt) throws ClusException {
        wrt.print("TimeSeries");
    }
    
    @Override
    public boolean isTimeSeries(){
    	return true;
    }
}
