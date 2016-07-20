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

package jeans.io.range;

public class DoubleRangeCheck implements ValueCheck {

	protected double m_MinDouble, m_MaxDouble;

	public DoubleRangeCheck(double min, double max) {
		m_MinDouble = min;
		m_MaxDouble = max;
	}

	public boolean checkValue(Object value) {
		double number = ((Double)value).doubleValue();
		return number >= m_MinDouble && number <= m_MaxDouble;
	}

	public String getString(String name, Object value) {
		return name + " = " + value + " out of range ["+m_MinDouble+", "+m_MaxDouble+"]";
	}
}
