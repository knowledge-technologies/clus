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

public class INIFileNominalOrDoubleOrVector extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected NominalType m_Type;
	protected int m_SingleNominal;
	protected double m_SingleDouble;
	protected int[] m_ArrayNominal;
	protected double[] m_ArrayDouble;
	private boolean m_HasArrayIndexNames;
	private String[] m_Names;

	public INIFileNominalOrDoubleOrVector(String name, String[] values) {
		this(name, new NominalType(values));
	}

	public INIFileNominalOrDoubleOrVector(String name, NominalType values) {
		super(name);
		m_Type = values;
		m_SingleNominal = -1;
		m_Names = new String[0];
	}

	public NominalType getType() {
		return m_Type;
	}

	public INIFileNode cloneNode() {
		return new INIFileNominalOrDoubleOrVector(getName(), getType());
	}

	public boolean isVector() {
		return m_ArrayNominal != null;
	}

	public boolean isNominal() {
		return m_SingleNominal != -1;
	}

	public boolean isNominal(int idx) {
		return getNominal(idx) != -1;
	}

	public int getNominal(int idx) {
		if (isVector()) {
			return m_ArrayNominal[idx];
		} else {
			return getNominal();
		}
	}

	public String getNominalString(int idx) {
		return m_Type.getName(getNominal(idx));
	}

	public double getDouble(int idx) {
		if (isVector()) {
			return m_ArrayDouble[idx];
		} else {
			return getDouble();
		}
	}

	public int getNominal() {
		return m_SingleNominal;
	}

	public double getDouble() {
		return m_SingleDouble;
	}

	public void setDouble(double value) {
		m_SingleDouble = value;
		m_ArrayNominal = null;
		m_ArrayDouble = null;
	}

	public void setNominal(int value) {
		m_SingleNominal = value;
		m_ArrayNominal = null;
		m_ArrayDouble = null;
	}

	public void setDouble(int idx, double value) {
		m_ArrayDouble[idx] = value;
		m_ArrayNominal[idx] = -1;
	}

	public void setNominal(int idx, int value) {
		m_ArrayNominal[idx] = value;
	}

	public void setDoubleArray(double[] values) {
		setVector(values.length);
		for (int i = 0; i < values.length; i++) {
			setDouble(i, values[i]);
		}
	}

	public void setVector(int len) {
		m_ArrayNominal = new int[len];
		m_ArrayDouble = new double[len];
	}

	public int getVectorLength() {
		return m_ArrayNominal == null ? 1 : m_ArrayNominal.length;
	}

	public boolean hasVector() {
		return m_ArrayNominal != null;
	}

	public double[] getDoubleVector() {
		return m_ArrayDouble;
	}

	public void build(MStreamTokenizer tokens) throws IOException {
		m_Type.setReader(true);
		setNominal(-1); // Do not forget to reset.
		setArrayIndexNames(false);
		if (tokens.isNextToken('[')) {
			ArrayList values = new ArrayList();
			String token = tokens.getToken();
			while (token != null && !token.equals("]")) {
				values.add(token);
				token = readNextEntry(tokens);
			}
			setVector(values.size());
			for (int i = 0; i < values.size(); i++) {
				String value = (String)values.get(i);
				int idx = m_Type.getValue(value);
				if (idx != -1) {
					setNominal(i, idx);
				} else {
					try {
						setDouble(i, Double.parseDouble(value));
					} catch (NumberFormatException e) {
						throw new IOException("Illegal value '"+value+"' in vector at pos "+i+" for setting '"+getName()+"'");
					}
				}
			}
		} else {
			String token = tokens.getToken();
			int idx = m_Type.getValue(token);
			if (idx != -1) {
				setNominal(idx);
			} else if (isArrayIndexName(0, token)) {
				setVector(m_Names.length);
				setArrayIndexNames(true);
				for (int i = 0; i < m_Names.length; i++) {
					if (!isArrayIndexName(i, token)) {
						throw new IOException("Expected one of ["+getArrayIndexNamesString()+"] and not '"+token+"' for setting '"+getName()+"'");
					}
					String value = tokens.getToken();
					if (!value.equals("=")) {
						throw new IOException("Expected '=' after '"+m_Names[i]+"' for setting '"+getName()+"'");
					}
					value = tokens.getToken();
					int idx2 = m_Type.getValue(value);
					if (idx2 != -1) {
						setNominal(i, idx2);
					} else {
						try {
							setDouble(i, Double.parseDouble(value));
						} catch (NumberFormatException e) {
							throw new IOException("Illegal value '"+value+"' for '"+m_Names[i]+"' for setting '"+getName()+"'");
						}
					}
					if (i != m_Names.length-1) token = tokens.getToken();
				}
			} else {
				try {
					// System.out.println("Setting double value: "+token+" for "+getName());
					setDouble(Double.parseDouble(token));
				} catch (NumberFormatException e) {
					throw new IOException("Illegal value '"+token+"' for setting '"+getName()+"'");
				}
			}
		}
	}

	public void setValue(String value) throws IOException {
		build(new MStreamTokenizer(new StringReader(value)));
	}

	public String readNextEntry(MStreamTokenizer tokens) throws IOException {
		String token = tokens.getToken();
		if (token != null && token.equals(",")) token = tokens.getToken();
		return token;
	}

	public String getStringValue() {
		if (hasArrayIndexNames()) {
			StringBuffer buf = new StringBuffer();
			buf.append("\n");
			for (int i = 0; i < m_Names.length; i++) {
				buf.append("  "+m_Names[i]+" = ");
				if (isNominal(i)) buf.append(getNominalString(i));
				else buf.append(String.valueOf(getDouble(i)));
				if (i != m_Names.length-1) buf.append("\n");
			}
			return buf.toString();
		} else if (isVector()) {
			StringBuffer buf = new StringBuffer();
			buf.append("[");
			for (int i = 0; i < getVectorLength(); i++) {
				if (i != 0) buf.append(",");
				if (isNominal(i)) buf.append(getNominalString(i));
				else buf.append(String.valueOf(getDouble(i)));
			}
			buf.append("]");
			return buf.toString();
		} else {
			if (isNominal()) return m_Type.getName(getNominal());
			else return String.valueOf(getDouble());
		}
	}

	public String getArrayIndexNamesString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_Names.length; i++) {
			if (i != 0) buf.append(",");
			buf.append(m_Names[i]);
		}
		return buf.toString();
	}

	public boolean isArrayIndexName(int i, String name) {
		if (i >= m_Names.length) return false;
		if (m_Names[i].equals(name)) return true;
		return false;
	}

	public boolean isArrayIndexName(String name) {
		for (int i = 0; i < m_Names.length; i++) {
			if (m_Names[i].equals(name)) return true;
		}
		return false;
	}

	public void setArrayIndexNames(String[] names) {
		m_Names = names;
	}

	public void setArrayIndexNames(boolean enable) {
		m_HasArrayIndexNames = enable;
	}

	public boolean hasArrayIndexNames() {
		return m_HasArrayIndexNames;
	}
}
