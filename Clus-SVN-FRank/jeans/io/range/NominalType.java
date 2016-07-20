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

import java.util.*;
import java.io.*;

public class NominalType implements Serializable {

	public final static long serialVersionUID = 1;

	protected String[] m_Names;
	protected Hashtable m_Lookup;

	public NominalType(String[] names) {
		m_Names = names;
	}

	public void setReader(boolean reader) {
		if (reader) {
			m_Lookup = new Hashtable();
			for (int i = 0; i < m_Names.length; i++)
				m_Lookup.put(m_Names[i].toUpperCase(), new Integer(i));
		} else {
			m_Lookup = null;
		}
	}

	public String getName(int i) {
		return m_Names[i];
	}

	public int getValue(String name) {
		Integer val = (Integer)m_Lookup.get(name.toUpperCase());
		if (val == null) return -1;
		return val.intValue();
	}

	public String getAllValues() {
		StringBuffer allowed = new StringBuffer();
		allowed.append("{");
		for (int i = 0; i < m_Names.length; i++) {
			if (i != 0) allowed.append(", ");
			allowed.append(m_Names[i]);
		}
		allowed.append("}");
		return allowed.toString();
	}

	public String getError(String name, String token) {
		return "'"+token+"' is illegal value for "+name+" allowed values are: "+getAllValues();
	}
}
