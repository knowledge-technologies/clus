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

package clus.ext.tuples;

import java.io.Serializable;
import java.util.StringTokenizer;

import clus.main.settings.Settings;


// TODO: extends single class, ClusType...
public class Tuple implements Serializable {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private Object[] m_Values;
    private double m_TSWeight;


    public Tuple(String values) {
        values = values.trim();
        values = values.replace("[", "");
        values = values.replace("]", "");
        //values = values.replaceAll("\\[", "");
        //values = values.replaceAll("\\]", "");
        StringTokenizer st = new StringTokenizer(values, ",");
        m_Values = new Object[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            m_Values[i++] = Double.parseDouble(st.nextToken());
        }
    }


    public Tuple(Object[] values) {
        m_Values = new Object[values.length];
        System.arraycopy(values, 0, m_Values, 0, values.length);
    }


    public Tuple(int size) {
        m_Values = new Object[size];
        for (int i = 0; i < size; i++) {
            m_Values[i] = 0.0;
        }
    }


    public Tuple(Tuple series) {
        this(series.getValues());
    }


    public int length() {
        if (m_Values == null)
            return 0;
        return m_Values.length;
    }


    public Object[] getValues() {
        Object[] result = new Object[m_Values.length];
        System.arraycopy(m_Values, 0, result, 0, m_Values.length);
        return result;
    }


    public Object[] getValuesNoCopy() {
        return m_Values;
    }


    public Object getValue(int index) {
        return m_Values[index];
    }


    public void setValues(Object[] values) {
        System.arraycopy(values, 0, this.m_Values, 0, values.length);
    }


    /*
     * [Aco]
     * Seting a single value
     */
    public void setValue(int index, Object value) {
        m_Values[index] = value;
    }


    /*
     * [Aco]
     * For easy printing of the series
     * @see java.lang.Object#toString()
     */
    public String toString() {
        //NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        StringBuffer a = new StringBuffer("[");
        for (int i = 0; i < length() - 1; i++) {
            if (m_Values[i] != null)
                a.append(m_Values[i]);
            else
                a.append("?");
            a.append(',');
        }
        if (length() > 0)
            a.append(m_Values[length() - 1]);
        a.append(']');
        return a.toString();
    }


    public double geTSWeight() {
        return m_TSWeight;
    }


    public void setTSWeight(double weight) {
        m_TSWeight = weight;
    }

}
