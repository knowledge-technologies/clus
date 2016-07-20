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

package clus.ext.hierarchical;

import jeans.util.array.*;
import jeans.util.*;
import jeans.tree.*;

import java.io.*;

import clus.util.*;

public class ClassHierarchyIO {

	protected StringTable m_Table = new StringTable();

	public ClassHierarchy loadHierarchy(String fname) throws ClusException, IOException {
		ClassHierarchy hier = new ClassHierarchy((ClassTerm)null);
		loadHierarchy(fname, hier);
		return hier;
	}

	public ClassHierarchy loadHierarchy(String fname, ClassesAttrType type) throws ClusException, IOException {
		ClassHierarchy hier = new ClassHierarchy(type);
		loadHierarchy(fname, hier);
		return hier;
	}

	public void loadHierarchy(String fname, ClassHierarchy hier) throws ClusException, IOException {
		MStreamTokenizer tokens = new MStreamTokenizer(fname);
		String token = tokens.getToken();
		while (token != null) {
			ClassesTuple tuple = new ClassesTuple(token, m_Table);
			tuple.addToHierarchy(hier);
			token = tokens.getToken();

		    }
		tokens.close();
	}

	public void saveHierarchy(String fname, ClassHierarchy hier) throws IOException {
		PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		CompleteTreeIterator iter = hier.getNoRootIter();
		while (iter.hasMoreNodes()) {
			ClassTerm node = (ClassTerm)iter.getNextNode();
			wrt.println(node.toString());
		}
		wrt.close();
	}

	public StringTable getStringTable() {
		return m_Table;
	}


}
