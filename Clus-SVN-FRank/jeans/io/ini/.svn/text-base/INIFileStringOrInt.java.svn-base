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

import jeans.util.StringUtils;

public class INIFileStringOrInt extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected String m_Value;
	protected int m_Int;
	protected boolean m_IsInt;

	public INIFileStringOrInt(String name) {
		this(name, "");
	}

	public INIFileStringOrInt(String name, String value) {
		super(name);
		setValue(value);
	}

	public INIFileNode cloneNode() {
		return new INIFileStringOrInt(getName(), getValue());
	}

	public String getValue() {
		return m_Value;
	}

	public int getIntValue() {
		return m_Int;
	}

	public void setIntValue(int val) {
		m_IsInt = true;
		m_Int = val;
		m_Value = "";
	}

	public boolean isIntOrNull(String nullstr) {
		if (isInt()) return true;
		if (StringUtils.unCaseCompare(getStringValue(), nullstr)) return true;
		return false;
	}

	public boolean isString(String str) {
		if (isInt()) return false;
		if (StringUtils.unCaseCompare(getStringValue(), str)) return true;
		return false;
	}

	public boolean isInt() {
		return m_IsInt;
	}

	public void setValue(String value) {
		if (isInt(value)) {
			m_Int = Integer.parseInt(value);
			m_IsInt = true;
		} else {
			m_IsInt = false;
		}
		m_Value = value;
	}

	public String getStringValue() {
		if (m_IsInt) {
			return String.valueOf(m_Int);
		} else {
			return getValue();
		}
	}

	public static boolean isInt(String value) {
		int len = value.length();
		if (len == 0) return false;
		for (int i = 0; i < len; i++) {
			int ch = value.charAt(i);
			if (!(ch >= '0' && ch <= '9')) return false;
		}
		return true;
	}
}
