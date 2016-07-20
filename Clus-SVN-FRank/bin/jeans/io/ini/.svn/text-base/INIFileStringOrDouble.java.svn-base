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

import jeans.math.*;
import jeans.util.StringUtils;

public class INIFileStringOrDouble extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected String m_Value;
	protected double m_Double;
	protected boolean m_IsDouble;

	public INIFileStringOrDouble(String name) {
		this(name, "");
	}

	public INIFileStringOrDouble(String name, String value) {
		super(name);
		setValue(value);
	}

	public INIFileNode cloneNode() {
		return new INIFileStringOrDouble(getName(), getValue());
	}

	public String getValue() {
		return m_Value;
	}

	public double getDoubleValue() {
		return m_Double;
	}

	public void setDoubleValue(double val) {
		m_IsDouble = true;
		m_Double = val;
		m_Value = "";
	}

	public boolean isDoubleOrNull(String nullstr) {
		if (isDouble()) return true;
		if (StringUtils.unCaseCompare(getStringValue(), nullstr)) return true;
		return false;
	}

	public boolean isString(String str) {
		if (isDouble()) return false;
		if (StringUtils.unCaseCompare(getStringValue(), str)) return true;
		return false;
	}

	public boolean isDouble() {
		return m_IsDouble;
	}

	public void setValue(String value) {
		if (MDouble.isDouble(value)) {
			m_Double = Double.parseDouble(value);
			m_IsDouble = true;
		}
		m_Value = value;
	}

	public String getStringValue() {
		if (m_IsDouble) {
			return String.valueOf(m_Double);
		} else {
			return getValue();
		}
	}
}
