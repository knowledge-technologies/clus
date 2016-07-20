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

public class DoubleObject implements Comparable {

	protected double m_Double;
	protected Object m_Object;

	public DoubleObject() {
	}

	public DoubleObject(double val, Object obj) {
		m_Double = val;
		m_Object = obj;
	}

	public double getValue() {
		return m_Double;
	}

	public Object getObject() {
		return m_Object;
	}

	public void set(double val, Object obj) {
		m_Double = val;
		m_Object = obj;
	}

	public int compareTo(Object o) {
		DoubleObject ot = (DoubleObject)o;
		if (m_Double == ot.m_Double) return 0;
		if (m_Double < ot.m_Double) return 1;
		return -1;
	}
}
