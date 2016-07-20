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

public class INIFileBool extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected boolean m_Value;

	public INIFileBool(String name) {
		this(name, false);
	}

	public INIFileBool(String name, String value) {
		super(name);
		setValue(value);
	}

	public INIFileBool(String name, boolean value) {
		super(name);
		setValue(value);
	}

	public INIFileNode cloneNode() {
		return new INIFileBool(getName(), getValue());
	}

	public boolean getValue() {
		return m_Value;
	}

	public void setValue(String value) {
	    setValue(StringUtils.getBoolean(value) == 1);
	}

	public void setValue(boolean value) {
		m_Value = value;
	}

	public String getStringValue() {
		if (m_Value) return "Yes";
		else return "No";
	}
}
