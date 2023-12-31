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

import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.io.ClusSerializable;


public class IndexAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


    protected int m_CrValue;

    protected int[] m_Index;
    protected int m_Max = Integer.MIN_VALUE;
    protected int m_Min = Integer.MAX_VALUE;


    public IndexAttrType(String name) {
        super(name);
    }


    public IndexAttrType(String name, int min, int max) {
        super(name);
        m_Max = max;
        m_Min = min;
    }


    @Override
    public ClusAttrType cloneType() {
        IndexAttrType at = new IndexAttrType(m_Name, m_Min, m_Max);
        cloneType(at);
        return at;
    }


    public int getCrValue() {
        return m_CrValue;
    }


    @Override
    public AttributeType getAttributeType() {
        return AttributeType.Index;
    }


    @Override
    public String getTypeName() {
        return getAttributeType().getName();
    }


    @Override
    public ValueType getValueType() {
        return ValueType.Double;
    }


    @Override
    public void setNbRows(int nb) {
        m_Index = new int[nb];
    }


    public void setValue(int row, int value) {
        m_Index[row] = value;
        if (value > m_Max)
            m_Max = value;
        if (value < m_Min)
            m_Min = value;
    }


    public int getMaxValue() {
        return m_Max;
    }


    public int getValue(int row) {
        return m_Index[row];
    }


    public int getNbRows() {
        return m_Index.length;
    }


    /*
     * public boolean addToData(ColData data) {
     * return true;
     * }
     */
    public ClusSerializable createRowSerializable(RowData data, boolean istarget) throws ClusException {
        return new MyReader();
    }

    public class MyReader extends ClusSerializable {

        public int getValue(ClusReader data, String value) throws IOException {
            try {
                int ival = Integer.parseInt(value);
                if (ival > m_Max)
                    m_Max = ival;
                if (ival < m_Min)
                    m_Min = ival;
                return ival;
            }
            catch (NumberFormatException e) {
                throw new IOException("Illegal value '" + value + "' for attribute " + getName() + " at row " + (data.getRow() + 1));
            }
        }


        public boolean read(ClusReader data, int row) throws IOException {
            String value = data.readString();
            if (value == null)
                return false;
            m_Index[row] = getValue(data, value);
            return true;
        }


        @Override
        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            String value = data.readString();
            if (value == null)
                return false;
            m_CrValue = getValue(data, value);
            return true;
        }
    }
}
