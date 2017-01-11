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
import java.util.*;

import clus.util.*;
import clus.data.*;
import clus.data.type.*;

public abstract class TupleIterator {

	protected DataPreprocs m_Procs;
	protected boolean m_ShouldAttach;

	public TupleIterator() {
		m_Procs = new DataPreprocs();
	}

	public TupleIterator(DataPreprocs procs) {
		m_Procs = procs != null ? procs : new DataPreprocs();
	}

	public abstract DataTuple readTuple() throws IOException, ClusException;

	public abstract ClusSchema getSchema();

	public void init() throws IOException, ClusException {
	}

	public void close() throws IOException {
	}

	public final void preprocTuple(DataTuple tuple) throws ClusException {
		if (tuple != null) m_Procs.preprocSingle(tuple);
	}

	public final void setPreprocs(DataPreprocs procs) {
		m_Procs = procs;
	}

	public final boolean shouldAttach() {
		return m_ShouldAttach;
	}

	public final void setShouldAttach(boolean attach) {
		m_ShouldAttach = attach;
	}

	public ClusData getData() {
		return null;
	}

	public ClusData createInMemoryData() throws IOException, ClusException {
		init();
		ArrayList list = new ArrayList();
		DataTuple tuple = readTuple();
		while (tuple != null) {
			list.add(tuple);
			tuple = readTuple();
		}
		return new RowData(list, getSchema());
	}
}
