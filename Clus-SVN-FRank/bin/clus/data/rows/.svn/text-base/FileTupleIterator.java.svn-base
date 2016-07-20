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

package clus.data.rows;

import java.io.*;

import clus.util.*;
import clus.data.io.*;
import clus.data.type.*;

public class FileTupleIterator extends TupleIterator {

	protected ClusReader m_Reader;
	protected RowData m_Data;
	protected ClusView m_View;

	public FileTupleIterator(DataPreprocs preproc) {
		super(preproc);
	}

	public FileTupleIterator(ClusSchema schema, ClusReader reader) throws ClusException {
		m_Data = new RowData(schema);
		m_Reader = reader;
	}

	public FileTupleIterator(ClusSchema schema, ClusReader reader, DataPreprocs procs) throws ClusException {
		super(procs);
		m_Data = new RowData(schema);
		m_Reader = reader;
	}

	public final ClusSchema getSchema() {
		return m_Data.getSchema();
	}

	public void init() throws IOException, ClusException {
		ClusSchema schema = getSchema();
		m_View = schema.createNormalView();
		schema.setReader(true);
	}

	public final DataTuple readTuple() throws IOException, ClusException {
		DataTuple tuple = m_View.readDataTuple(m_Reader, m_Data.getSchema());
		preprocTuple(tuple);
		return tuple;
	}

	public final void close() throws IOException {
		ClusSchema schema = m_Data.getSchema();
		schema.setReader(false);
		m_Reader.close();
	}
}
