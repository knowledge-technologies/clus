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

public class INIFileArray extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected Vector m_hValue = new Vector();
	protected int m_iType;

	public INIFileArray(String name, int type) {
		super(name);
		m_iType = type;
	}

	public INIFileArray(String name, int[] init) {
		super(name);
		fromArray(init);
	}

	public INIFileArray(String name, String[] init) {
		super(name);
		fromArray(init);
	}

	public INIFileArray(String name, double[] init) {
		super(name);
		fromArray(init);
	}

	public int getSize() {
		return m_hValue.size();
	}

	public int getType() {
		return m_iType;
	}

	public void removeAllElements() {
		m_hValue.removeAllElements();
	}

	public Vector getVector() {
		return m_hValue;
	}

	public void fromArray(int[] init) {
		removeAllElements();
		m_iType = INIFile.TYPE_INT_ARRAY;
		for (int i = 0; i < init.length; i++) addValue(init[i]);
 	}

	public void fromArray(String[] init) {
		removeAllElements();
		m_iType = INIFile.TYPE_STRING_ARRAY;
		for (int i = 0; i < init.length; i++) addValue(init[i]);
 	}

	public void fromArray(double[] init) {
		removeAllElements();
		m_iType = INIFile.TYPE_DOUBLE_ARRAY;
		for (int i = 0; i < init.length; i++) addValue(init[i]);
 	}

	public void addValue(int value) {
		if (m_iType == INIFile.TYPE_INT_ARRAY)
			m_hValue.addElement(new Integer(value));
	}

	public void addValue(String value) {
		if (m_iType == INIFile.TYPE_STRING_ARRAY)
			m_hValue.addElement(value);
	}

	public void addValue(double value) {
		if (m_iType == INIFile.TYPE_DOUBLE_ARRAY)
			m_hValue.addElement(new Double(value));
	}

	public int getIntAt(int idx) {
		if (m_iType == INIFile.TYPE_INT_ARRAY) {
			Integer value = (Integer)m_hValue.elementAt(idx);
			return value.intValue();
		} else {
			return 0;
		}
	}

	public String getStringAt(int idx) {
		if (m_iType == INIFile.TYPE_STRING_ARRAY) {
			return (String)m_hValue.elementAt(idx);
		} else {
			return "";
		}
	}

	public double getDoubleAt(int idx) {
		if (m_iType == INIFile.TYPE_DOUBLE_ARRAY) {
			Double value = (Double)m_hValue.elementAt(idx);
			return value.doubleValue();
		} else {
			return 0.0;
		}
	}

	public INIFileNode cloneNode() {
		return new INIFileArray(getName(), getType());
	}

	public void build(MStreamTokenizer tokens) throws IOException {
		tokens.readChar('{');
		removeAllElements();
		String token = tokens.getToken();
		while (token != null && !token.equals("}")) {
			addToken(token, tokens);
			token = readNextEntry(tokens);
		}
	}

	public void addToken(String token, MStreamTokenizer tokens) throws IOException {
		switch (m_iType) {
			case INIFile.TYPE_DOUBLE_ARRAY:
				try {
					addValue(Double.parseDouble(token.trim()));
				} catch (NumberFormatException e) {
					throw new IOException("Double expected at line "+tokens.getLine());
				}
				break;
			case INIFile.TYPE_INT_ARRAY:
				try {
					addValue(Integer.parseInt(token.trim()));
				} catch (NumberFormatException e) {
					throw new IOException("Integer expected at line "+tokens.getLine());
				}
				break;
			case INIFile.TYPE_STRING_ARRAY:
				m_hValue.addElement(token.trim());
				break;
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
		StringBuffer buffer = new StringBuffer("{");
		for (int idx = 0; idx < m_hValue.size(); idx++) {
			Object value = m_hValue.elementAt(idx);
			if (idx != 0) buffer.append(", ");
			buffer.append(value.toString());
		}
		buffer.append("}");
		return buffer.toString();
	}
}
