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

import clus.main.*;
import clus.data.io.*;
import clus.data.type.*;
import clus.util.*;

public class DiskTupleIterator extends FileTupleIterator {

	protected String m_File;
	protected ClusSchemaInitializer m_Init;
	protected Settings m_Sett;

	public DiskTupleIterator(String file, ClusSchemaInitializer init, Settings sett) {
		this(file, init, null, sett);
	}

	public DiskTupleIterator(String file, ClusSchemaInitializer init, DataPreprocs procs, Settings sett) {
		super(procs);
		m_File = file;
		m_Init = init;
		m_Sett = sett;
	}

	public void init() throws IOException, ClusException {
		System.out.println("Loading '"+m_File+"'");
		m_Reader = new ClusReader(m_File, m_Sett);
		ARFFFile arff = new ARFFFile(m_Reader);
		ClusSchema schema = arff.read(m_Sett);
		if (m_Init != null) m_Init.initSchema(schema);
		schema.addIndices(ClusSchema.ROWS);
		m_Data = new RowData(schema);
		super.init();
	}
}
