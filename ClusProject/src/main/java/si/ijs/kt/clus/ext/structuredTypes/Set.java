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

package si.ijs.kt.clus.ext.structuredTypes;

import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;

import si.ijs.kt.clus.ext.timeseries.TimeSeries;
import si.ijs.kt.clus.main.settings.Settings;


public class Set implements Serializable {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private Object[] m_Values;
    private double m_SetWeight;


    public Set(String values) throws IOException {
        this(values, "");
    }


    public Set(String values, String typeDefinition) throws IOException {
        values = values.trim();

        if (values.charAt(0) != '{' || values.charAt(values.length() - 1) != '}')
            throw new IOException("This is not a well defined set! Please use {,} brackets to define a set!");
        values = values.substring(1);
        values = values.substring(0, values.length() - 1);

        String subType = typeDefinition.trim().substring(4);//take of SET{		
        subType = subType.substring(0, subType.length() - 1);

        if (subType.equalsIgnoreCase("numeric")) {
            StringTokenizer st = new StringTokenizer(values, ",");
            m_Values = new Object[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                m_Values[i++] = Double.parseDouble(st.nextToken());
            }
        }
        else if (subType.equalsIgnoreCase("timeseries")) {
            StringTokenizer st = new StringTokenizer(values, "]");
            m_Values = new Object[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                String ts = st.nextToken();
                ts = ts.replace(",[", "");
                ts = ts.replace("[", "");
                m_Values[i++] = new TimeSeries(ts);
            }
        }
    }


    public Set(Object[] values) {
        m_Values = new Object[values.length];
        System.arraycopy(values, 0, m_Values, 0, values.length);
    }


    public Set(int size) {
        m_Values = new Object[size];
        for (int i = 0; i < size; i++) {
            m_Values[i] = 0.0;
        }
    }


    public Set(Set set) {
        this(set.getValues());
    }


    public int size() {
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


    public void setValues(Object[] values) {
        System.arraycopy(values, 0, this.m_Values, 0, values.length);
    }


    @Override
    public String toString() {
        StringBuffer a = new StringBuffer("{");
        for (int i = 0; i < size() - 1; i++) {
            a.append(String.valueOf(m_Values[i]));
            a.append(',');
        }
        if (size() > 0)
            a.append(String.valueOf(m_Values[size() - 1]));
        a.append('}');
        return a.toString();
    }


    public double getSetWeight() {
        return m_SetWeight;
    }


    public void setSetWeight(double weight) {
        m_SetWeight = weight;
    }

}
