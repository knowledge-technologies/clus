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

import java.util.*;

public class INIFileInt extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected int m_Value;
	protected ValueCheck m_Check;
	protected transient HashMap m_NameToValue, m_ValueToName;

	public INIFileInt(String name, String value) throws NumberFormatException {
		super(name);
		setValue(value);
	}

	public INIFileInt(String name, int value) {
		super(name);
		setValue(value);
	}

	public INIFileInt(String name) {
		this(name, 0);
	}

	public void setValueCheck(ValueCheck check) {
		m_Check = check;
	}

	public INIFileNode cloneNode() {
		return new INIFileInt(getName(), getValue());
	}

	public void setNamedValue(int value, String name) {
		if (m_NameToValue == null) m_NameToValue = new HashMap();
		if (m_ValueToName == null) m_ValueToName = new HashMap();
		Integer value_int = new Integer(value);
		m_NameToValue.put(name, value_int);
		m_ValueToName.put(value_int, name);
	}

	public int getValue() {
		return m_Value;
	}

	public void setValue(int value) {
		if (m_Check != null) {
			Integer dval = new Integer(value);
			if (!m_Check.checkValue(dval))
				throw new NumberFormatException(m_Check.getString(getName(), dval));
		}
		m_Value = value;
	}

	public void setValue(String value) throws NumberFormatException {
		if (m_NameToValue != null) {
			Integer int_value = (Integer)m_NameToValue.get(value);
			if (int_value != null) {
				setValue(int_value.intValue());
			} else {
				setValue(Integer.parseInt(value));
			}
		} else {
			setValue(Integer.parseInt(value));
		}
	}

	public String getStringValue() {
		if (m_ValueToName != null) {
			String name = (String)m_ValueToName.get(new Integer(getValue()));
			if (name != null) return name;
		}
		return String.valueOf(getValue());
	}
}
