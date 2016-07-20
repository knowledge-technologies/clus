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

package jeans.util.compound;

import java.io.Serializable;

import clus.main.Settings;

public class DoubleBoolean implements Comparable, Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_Double;
	protected boolean m_Boolean;

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
		DoubleBoolean ot = (DoubleBoolean)o;
		return ot.m_Boolean == m_Boolean && ot.m_Double == m_Double;
	}

	public int hashCode() {
		long v = Double.doubleToLongBits(m_Double);
		return (int)(v^(v>>>32)) ^ (m_Boolean ? 1 : 0);
	}

	public int compareTo(Object o) {
		DoubleBoolean ot = (DoubleBoolean)o;
		if (m_Double == ot.m_Double) return 0;
		if (m_Double < ot.m_Double) return 1;
		return -1;
	}
}
