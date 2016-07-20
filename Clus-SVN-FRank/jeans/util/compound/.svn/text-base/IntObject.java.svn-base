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

public class IntObject implements Comparable {

	protected int m_Int;
	protected Object m_Object;

	public IntObject(int val, Object obj) {
		m_Int = val;
		m_Object = obj;
	}

	public int getValue() {
		return m_Int;
	}

	public void setValue(int value) {
		m_Int = value;
	}

	public void incValue() {
		m_Int++;
	}

	public Object getObject() {
		return m_Object;
	}

	public int compareTo(Object o) {
		IntObject ot = (IntObject)o;
		if (m_Int == ot.m_Int) return 0;
		if (m_Int < ot.m_Int) return 1;
		return -1;
	}
}
