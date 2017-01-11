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

package clus.ext.exhaustivesearch;

import java.util.*;
import java.io.*;
import clus.algo.tdidt.*;
import clus.ext.beamsearch.*;

public class ClusExhaustive {

	TreeMap m_Tree;
	Collection m_Values;
	int m_MaxWidth;
	int m_CrWidth;
	boolean m_RemoveEqualHeur;


	public ClusExhaustive(int width) {
		m_Tree = new TreeMap(); //trees in the beam
		m_Values = m_Tree.values();
		m_MaxWidth = width;
	}

	//add a tree to the beam if it not already there
	public int addIfNotIn(ClusBeamModel model) {
		Double key = new Double(model.getValue());
		ClusBeamTreeElem found = (ClusBeamTreeElem)m_Tree.get(key);
		if (found == null) {
			m_Tree.put(key, new ClusBeamTreeElem(model));
			return 1;
		} else return 0;
	}

	public void print(PrintWriter wrt, int best_n) {
		ArrayList lst = toArray();
		for (int i = 0; i < Math.min(best_n, lst.size()); i++) {
			if (i != 0) wrt.println();
			ClusBeamModel mdl = (ClusBeamModel)lst.get(lst.size()-i-1);
			ClusNode tree = (ClusNode)mdl.getModel();
			double error = Double.NaN; // tree.estimateError();
			wrt.println("Model: "+i+" value: "+mdl.getValue()+" error: "+error+" parent: "+mdl.getParentModelIndex());
			tree.printModel(wrt);
		}
	}

	public Iterator getIterator() {
		return m_Values.iterator();
	}

	public ArrayList toArray() {
		ArrayList lst = new ArrayList();
		Iterator iter = m_Values.iterator();
		while (iter.hasNext()) {
			ClusBeamTreeElem elem = (ClusBeamTreeElem)iter.next();
			elem.addAll(lst);
		}
		return lst;
	}

	public int getMaxWidth() {
		return m_MaxWidth;
	}

	public int getCrWidth() {
		return m_CrWidth;
	}


	public void print() {
/*		System.out.println("Beam:");
		m_Tree.printStructure();
		System.out.println("All:");
		m_Tree.printTree();
		System.out.println("Done"); */
	}
}
