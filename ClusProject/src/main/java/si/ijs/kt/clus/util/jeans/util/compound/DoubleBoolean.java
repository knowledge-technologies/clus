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

package si.ijs.kt.clus.util.jeans.util.compound;

import java.io.Serializable;

import si.ijs.kt.clus.main.settings.Settings;


/**
 * 
 * Parent class of the {@ code DoubleBooleanCount} that stores statistics that are used when building ROC- and
 * PR-curves.
 *
 */
public class DoubleBoolean implements Comparable, Serializable {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected double m_Double;
    protected boolean m_Boolean;


    /**
     * See {@code DoubleBooleanCount.DoubleBooleanCount(double val, boolean bol)}.
     * 
     * @param val
     * @param bol
     */
    public DoubleBoolean(double val, boolean bol) {
        m_Double = val;
        m_Boolean = bol;
    }


    public double getDouble() {
        return m_Double;
    }


    public Boolean getBoolean() {
        return m_Boolean;
    }


    public boolean equals(Object o) {
        DoubleBoolean ot = (DoubleBoolean) o;
        return ot.m_Boolean == m_Boolean && ot.m_Double == m_Double;
    }


    /**
     * Hash function that is used for adding objects of the type {@code DoubleBooleanCount} to the
     * {@code clus.error.BinaryPredictionList.m_ValueSet}.
     */
    public int hashCode() {
        long v = Double.doubleToLongBits(m_Double);
        return (int) (v ^ (v >>> 32)) ^ (m_Boolean ? 1 : 0);
    }


    /**
     * Comparing function that is used for sorting the objects of the type {@code DoubleBooleanCount}. The objects of
     * the given list are sorted decreasingly by the field {@code m_Double},
     * which comes in handy when constructing as many points for ROC- and PR-curves as possible.
     *
     */
    public int compareTo(Object o) {
        DoubleBoolean ot = (DoubleBoolean) o;
        if (m_Double == ot.m_Double)
            return 0;
        if (m_Double < ot.m_Double)
            return 1;
        return -1;
    }
}
