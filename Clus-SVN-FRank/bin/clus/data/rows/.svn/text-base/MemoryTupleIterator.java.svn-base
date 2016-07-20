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

import java.io.IOException;

import clus.data.*;
import clus.data.type.*;
import clus.util.ClusException;

public class MemoryTupleIterator extends TupleIterator {

	protected RowData m_Data;
	protected int m_Index;

	public MemoryTupleIterator(RowData data) {
		m_Data = data;
	}

	public MemoryTupleIterator(RowData data, DataPreprocs procs) {
		super(procs);
		m_Data = data;
	}

	public void init() throws IOException, ClusException {
		m_Index = 0;
	}

	public int getNbExamples() {
		return m_Data.getNbRows();
	}

	public final ClusSchema getSchema() {
		return m_Data.getSchema();
	}

  public final ClusData getData() {
    return m_Data;
  }

	public final DataTuple readTuple() throws ClusException {
		if (m_Index >= m_Data.getNbRows()) return null;
		DataTuple tuple = m_Data.getTuple(m_Index++);
		preprocTuple(tuple);
		return tuple;
	}
}
