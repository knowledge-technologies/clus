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

import java.io.*;
import java.util.*;

import jeans.util.MStreamTokenizer;
import jeans.io.range.NominalType;

/**
 * Corresponds to a nominal settings file field.
 */
public class INIFileNominal extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected NominalType m_Type;
	protected int[] m_Values;
	protected boolean m_IsSet;

	public INIFileNominal(String name, String[] values) {
		super(name);
		m_Type = new NominalType(values);
	}

	/**
	 * Constructor for INIFileNominal
	 * @param name Parameter name
	 * @param values Possible values in strings
	 * @param def The default value number
	 */
	public INIFileNominal(String name, String[] values, int def) {
		super(name);
		m_Type = new NominalType(values);
		m_Values = new int[1];
		m_Values[0] = def;
	}

	public INIFileNominal(String name, String[] type, int[] values) {
		super(name);
		m_Type = new NominalType(type);
		m_Values = values;
		m_IsSet = true;
	}

	public INIFileNominal(String name, NominalType type) {
		super(name);
		m_Type = type;
	}

	public INIFileNominal(String name, NominalType type, int[] values) {
		super(name);
		m_Type = type;
		m_Values = values;
		m_IsSet = true;
	}

	public INIFileNominal(String name, NominalType type, int value) {
		super(name);
		setSingleValue(value);
		m_Type = type;
		m_IsSet = false;
	}

	public void setSingleValue(int idx) {
		m_Values = new int[1];
		m_Values[0] = idx;
	}

	public int getSize() {
		return m_Values.length;
	}

	public NominalType getType() {
		return m_Type;
	}

	public boolean contains(int idx) {
		return Arrays.binarySearch(m_Values, idx) >= 0;
	}

	public int getIntAt(int idx) {
		return m_Values[idx];
	}

	public String getStringSingle() {
		return m_Type.getName(m_Values[0]);
	}

	public int getValue() {
		return m_Values[0];
	}

	public int getSingle() {
		return m_Values[0];
	}

	public String getStringAt(int idx) {
		return m_Type.getName(m_Values[idx]);
	}

	public void setValues(int[] vals) {
		m_Values = vals;
	}

	public INIFileNode cloneNode() {
		return new INIFileNominal(getName(), getType());
	}

	public void build(MStreamTokenizer tokens) throws IOException {
		m_Type.setReader(true);
		if (tokens.isNextToken('{')) {
			if (m_IsSet) {
				Vector vals = new Vector();
				String token = tokens.getToken();
				while (token != null && !token.equals("}")) {
					int idx = m_Type.getValue(token);
					if (idx == -1) throw new IOException(m_Type.getError(getName(), token));
					vals.addElement(new Integer(idx));
					token = readNextEntry(tokens);
				}
				m_Values = new int[vals.size()];
				for (int i = 0; i < vals.size(); i++)
					m_Values[i] = ((Integer)vals.elementAt(i)).intValue();
				Arrays.sort(m_Values);
			} else {
				throw new IOException("'"+getName()+"' is not a set");
			}
		} else {
			String token = tokens.getToken();
			int idx = m_Type.getValue(token);
			if (idx == -1) throw new IOException(m_Type.getError(getName(), token));
			setSingleValue(idx);
		}
	}

	public void setValue(String value) {
	}

	public String readNextEntry(MStreamTokenizer tokens) throws IOException {
		String token = tokens.getToken();
		if (token != null && token.equals(",")) token = tokens.getToken();
		return token;
	}

	public String getStringValue() {
		if (m_IsSet) {
			StringBuffer buffer = new StringBuffer("{");
			for (int idx = 0; idx < m_Values.length; idx++) {
				if (idx != 0) buffer.append(", ");
				buffer.append(m_Type.getName(m_Values[idx]));
			}
			buffer.append("}");
			return buffer.toString();
		} else {
			return getStringSingle();
		}
	}
}
