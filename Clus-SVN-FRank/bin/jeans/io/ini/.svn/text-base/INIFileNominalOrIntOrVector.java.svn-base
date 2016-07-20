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

public class INIFileNominalOrIntOrVector extends INIFileEntry {

	public final static long serialVersionUID = 1;

	protected NominalType m_Type;
	protected int m_SingleNominal;
	protected int m_SingleInt;
	protected int[] m_ArrayNominal;
	protected int[] m_ArrayInt;

	public INIFileNominalOrIntOrVector(String name, String[] values) {
		this(name, new NominalType(values));
	}

	public INIFileNominalOrIntOrVector(String name, NominalType values) {
		super(name);
		m_Type = values;
		m_SingleNominal = -1;
	}

	public NominalType getType() {
		return m_Type;
	}

	public INIFileNode cloneNode() {
		return new INIFileNominalOrIntOrVector(getName(), getType());
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

	public int getInt(int idx) {
		if (isVector()) {
			return m_ArrayInt[idx];
		} else {
			return getInt();
		}
	}

	public int getNominal() {
		return m_SingleNominal;
	}

	public int getInt() {
		return m_SingleInt;
	}

	public void setInt(int value) {
		m_SingleInt = value;
		m_ArrayNominal = null;
		m_ArrayInt = null;
	}

	public void setNominal(int value) {
		m_SingleNominal = value;
		m_ArrayNominal = null;
		m_ArrayInt = null;
	}

	public void setInt(int idx, int value) {
		m_ArrayInt[idx] = value;
		m_ArrayNominal[idx] = -1;
	}

	public void setNominal(int idx, int value) {
		m_ArrayNominal[idx] = value;
	}

	public void setVector(int len) {
		m_ArrayNominal = new int[len];
		m_ArrayInt = new int[len];
	}

	public int getVectorLength() {
		return m_ArrayNominal == null ? 1 : m_ArrayNominal.length;
	}

	public int[] getIntVector() {
		if (isVector()) {
			int[] res = new int[m_ArrayInt.length];
			System.arraycopy(m_ArrayInt, 0, res, 0, m_ArrayInt.length);
			return res;
		} else {
			int[] res = new int[1];
			res[0] = m_SingleInt;
			return res;
		}
	}

	public int[] getIntVectorSorted(){
		int[] result = getIntVector();
		Arrays.sort(result);
		return result;
	}

	public void build(MStreamTokenizer tokens) throws IOException {
		m_Type.setReader(true);
		setNominal(-1); // Do not forget to reset.
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
						setInt(i, Integer.parseInt(value));
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
			} else {
				try {
					// System.out.println("Setting int value: "+token+" for "+getName());
					setInt(Integer.parseInt(token));
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
		if (isVector()) {
			StringBuffer buf = new StringBuffer();
			buf.append("[");
			for (int i = 0; i < getVectorLength(); i++) {
				if (i != 0) buf.append(",");
				if (isNominal(i)) buf.append(getNominalString(i));
				else buf.append(String.valueOf(getInt(i)));
			}
			buf.append("]");
			return buf.toString();
		} else {
			if (isNominal()) return m_Type.getName(getNominal());
			else return String.valueOf(getInt());
		}
	}
}
