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

package clus.data.io;

import java.io.*;
import java.util.*;

import clus.io.*;
import clus.util.ClusException;
import clus.data.rows.*;
import clus.data.type.*;

public class ClusView {

	protected ArrayList m_Attr = new ArrayList();

	public int getNbAttributes() {
		return m_Attr.size();
	}

	public ClusSerializable getAttribute(int idx) {
		return (ClusSerializable)m_Attr.get(idx);
	}

	public void addAttribute(ClusSerializable attr) {
		m_Attr.add(attr);
	}

	public RowData readData(ClusReader reader, ClusSchema schema) throws IOException, ClusException {
		schema.setReader(true);
		ArrayList items = new ArrayList();
		DataTuple tuple = readDataTupleFirst(reader, schema);
		while (tuple != null) {
			items.add(tuple);
			tuple = readDataTupleNext(reader, schema);
		}
		for (int j = 0; j < m_Attr.size(); j++) {
			ClusSerializable attr = (ClusSerializable)m_Attr.get(j);
			attr.term(schema);
		}
		schema.setReader(false);
		return new RowData(items, schema);
	}

	public DataTuple readDataTupleFirst(ClusReader reader, ClusSchema schema) throws IOException, ClusException {
		if (!reader.hasMoreTokens()) return null;
		boolean sparse = reader.isNextChar('{');
		if (sparse) {
			m_Attr.clear();
			schema.ensureSparse();
			schema.createNormalView(this);
		}
		return readDataTuple(reader, schema, sparse);
	}

	public DataTuple readDataTupleNext(ClusReader reader, ClusSchema schema) throws IOException {
		if (!reader.hasMoreTokens()) return null;
		boolean sparse = reader.isNextChar('{');
		if (sparse && !schema.isSparse()) {
			throw new IOException("Sparse tuple found in a non-sparse data set (at row "+(reader.getRow()+1)+")");
		}
		return readDataTuple(reader, schema, sparse);
	}

	public DataTuple readDataTuple(ClusReader reader, ClusSchema schema) throws IOException {
		if (!reader.hasMoreTokens()) return null;
		boolean sparse = reader.isNextChar('{');
		return readDataTuple(reader, schema, sparse);
	}

	public DataTuple readDataTuple(ClusReader reader, ClusSchema schema, boolean sparse) throws IOException {
		DataTuple tuple = schema.createTuple();
		if (sparse) {
			while (!reader.isNextChar('}')) {
				int idx = reader.readIntIndex();
				if (idx < 1 || idx > m_Attr.size()) {
					throw new IOException("Error attribute index '"+idx+"' out of range [1,"+m_Attr.size()+"] at row "+(reader.getRow()+1));
				}
				ClusSerializable attr = (ClusSerializable)m_Attr.get(idx-1);
				if (!attr.read(reader, tuple)) {
					throw new IOException("Error reading attirbute "+m_Attr+" at row "+(reader.getRow()+1));
				}
			}
		} else {
			if (m_Attr.size() > 0) {
				ClusSerializable attr_0 = (ClusSerializable)m_Attr.get(0);
				if (!attr_0.read(reader, tuple)) return null;
				for (int j = 1; j < m_Attr.size(); j++) {
					ClusSerializable attr = (ClusSerializable)m_Attr.get(j);
					if (!attr.read(reader, tuple)) {
						throw new IOException("Error reading attirbute "+m_Attr+" at row "+(reader.getRow()+1));
					}
				}
			}
		}
		// Attribute read operations eat ',' after attribute field
		if (reader.isNextCharNoSpace('{')) {
			if (!reader.readNoSpace()) {
				throw new IOException("Error reading tuple weight at row "+(reader.getRow()+1));
			}
			tuple.setWeight(reader.getFloat());
			if (!reader.isNextChar('}')) {
				throw new IOException("Expected closing '}' after tuple weight at row "+(reader.getRow()+1));
			}
		}
		reader.readEol();
		return tuple;
	}
}
