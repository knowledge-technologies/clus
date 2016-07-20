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

package clus.ext.optxval;

import java.io.*;
import java.util.*;

import jeans.tree.*;
import jeans.util.array.*;

import clus.algo.tdidt.ClusNode;
import clus.main.*;
import clus.util.*;

public class OptXValNode extends MyNode {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int [] m_Folds;
	protected ClusNode[] m_Nodes;

	public long m_Time;

	public void init(int[] folds) {
		int mnb = folds.length;
		m_Nodes = new ClusNode[mnb];
		m_Folds = new int[mnb];
		System.arraycopy(folds, 0, m_Folds, 0, mnb);
	}

	public void oneFold(int fold, ClusNode onode) {
	}

	public ClusNode[] getNodes() {
		return m_Nodes;
	}

	public final void printTree(PrintWriter writer, String prefix) {
		int lvc = 0;
		for (int i = 0; i < m_Folds.length; i++) {
			ClusNode node = m_Nodes[i];
			if (!node.hasBestTest()) {
				if (lvc != 0) writer.print(", ");
				writer.print(m_Folds[i]+": ");
				writer.print(ClusFormat.ONE_AFTER_DOT.format(node.getTotWeight()));
				lvc++;
			}
		}
		if (lvc > 0) {
			writer.print(" ");
			showPath(getPath(), writer);
		}
		int nb = getNbChildren();
		if (nb > 0) {
			if (lvc > 0) {
				writer.println();
				writer.print(prefix);
			}
		} else {
			writer.println();
		}
		for (int i = 0; i < nb; i++) {
			OptXValSplit split = (OptXValSplit)getChild(i);
			if (i != 0) {
				writer.println(prefix+"|  ");
				writer.print(prefix);
			}
			writer.print("G"+i+" ");
			writer.print(MyIntArray.print(split.getFolds()));
			writer.print(" - ");
			writer.print(split.getTest().getString());
			writer.println();
			int mb = split.getNbChildren();
			String gfix = (i != nb-1) ? "|  " : "   ";
			for (int j = 0; j < mb; j++) {
				OptXValNode node = (OptXValNode)split.getChild(j);
				String suffix = (j != mb-1) ? "|      " : "       ";
				if (j == 0) writer.print(prefix+gfix+"+-yes: ");
				else {
					writer.println(prefix+gfix+"|");
					writer.print(prefix+gfix+"+-no:  ");
				}
				node.printTree(writer, prefix+gfix+suffix);
			}
		}
		writer.flush();
	}

	public final ClusNode getTree(int fold) {
		int idx = Arrays.binarySearch(m_Folds, fold);
		ClusNode node = m_Nodes[idx];
		if (node.hasBestTest() && node.atBottomLevel()) {
			OptXValSplit split = null;
			int nb = getNbChildren();
			for (int i = 0; i < nb; i++) {
				OptXValSplit msplit = (OptXValSplit)getChild(i);
				if (msplit.contains(fold)) {
					split = msplit;
					break;
				}
			}
			int arity = node.updateArity();
			for (int i = 0; i < arity; i++) {
				OptXValNode subnode = (OptXValNode)split.getChild(i);
				node.setChild(subnode.getTree(fold), i);
			}
		}
		return node;
	}

	public final void setNodeIndex(int idx, ClusNode node) {
		m_Nodes[idx] = node;
	}

	public final void setNode(int fold, ClusNode node) {
		int idx = Arrays.binarySearch(m_Folds, fold);
		m_Nodes[idx] = node;
	}
}
