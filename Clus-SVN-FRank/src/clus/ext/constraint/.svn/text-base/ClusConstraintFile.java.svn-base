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

/*
 * Created on Apr 26, 2005
 */
package clus.ext.constraint;

import java.io.*;
import java.util.*;

import clus.algo.tdidt.*;
import clus.data.type.*;
import clus.model.modelio.*;

public class ClusConstraintFile {

	public static ClusConstraintFile m_Instance;
	HashMap m_Constraints = new HashMap();

	public static ClusConstraintFile getInstance() {
		if (m_Instance == null) m_Instance = new ClusConstraintFile();
		return m_Instance;
	}

	public ClusNode get(String fname) {
		return (ClusNode)m_Constraints.get(fname);
	}

	public ClusNode getClone(String fname) {
		return (ClusNode)get(fname).cloneTree();
	}

	public void load(String fname, ClusSchema schema) throws IOException {
		ClusTreeReader rdr = new ClusTreeReader();
		ClusNode root = rdr.loadTree(fname, schema);
		System.out.println("Constraint: ");
		root.printTree();
		m_Constraints.put(fname, root);
	}
}
