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

package jeans.io.ini;

import jeans.io.range.ValueCheck;

public class INIFileDouble extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected double m_Value;
	protected ValueCheck m_Check;

	public INIFileDouble(String name, String value) {
		super(name);
		setValue(value);
	}

	public INIFileDouble(String name, double value) {
		super(name);
		setValue(value);
	}

	public INIFileDouble(String name) {
		this(name, 0);
	}

	public void setValueCheck(ValueCheck check) {
		m_Check = check;
	}

	public INIFileNode cloneNode() {
		return new INIFileDouble(getName(), getValue());
	}

	public double getValue() {
		return m_Value;
	}

	public void setValue(double value) {
		if (m_Check != null) {
			Double dval = new Double(value);
			if (!m_Check.checkValue(dval))
				throw new NumberFormatException(m_Check.getString(getName(), dval));
		}
		m_Value = value;
	}

	public void setValue(String value) {
		setValue(Double.parseDouble(value));
	}

	public String getStringValue() {
		return String.valueOf(getValue());
	}
}
