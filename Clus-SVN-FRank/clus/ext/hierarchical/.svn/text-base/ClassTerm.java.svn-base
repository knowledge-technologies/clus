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

import java.io.*;
import java.util.*;

import jeans.math.*;
import jeans.util.*;
import jeans.util.compound.*;

import jeans.tree.*;
import clus.util.*;
import clus.main.*;

public class ClassTerm extends IndexedItem implements Node, Comparable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected String m_ID;
	protected HashMap m_Hash = new HashMap();
	protected ArrayList m_SubTerms = new ArrayList();
	protected ArrayList m_Parents = new ArrayList();
	protected int m_MinDepth = Integer.MAX_VALUE;
	protected int m_MaxDepth = 0;

	public ClassTerm() {
		m_ID = "root";
	}

	public ClassTerm(String id) {
		m_ID = id;
	}

	public void addParent(ClassTerm term) {
		m_Parents.add(term);
	}

	public int getNbParents() {
		return m_Parents.size();
	}

	public ClassTerm getParent(int i) {
		return (ClassTerm)m_Parents.get(i);
	}

	public boolean isNumeric() {
		for (int i = 0; i < m_ID.length(); i++) {
			if (m_ID.charAt(i) < '0' || m_ID.charAt(i) > '9') return false;
		}
		return true;
	}

	public int compareTo(Object o) {
		ClassTerm other = (ClassTerm)o;
		String s1 = getID();
		String s2 = other.getID();
		if (s1.equals(s2)) {
			return 0;
		} else {
			if (isNumeric() && other.isNumeric()) {
				int i1 = Integer.parseInt(s1);
				int i2 = Integer.parseInt(s2);
				return i1 > i2 ? 1 : -1;
			} else {
				return s1.compareTo(s2);
			}
		}
	}

	public void addClass(ClassesValue val, int level, ClassHierarchy hier) {
		String cl_idx = val.getClassID(level);
		if (!cl_idx.equals("0")) {
			ClassTerm found = (ClassTerm)m_Hash.get(cl_idx);
			if (found == null) {
				boolean is_dag = hier.isDAG();
				if (is_dag) {
					// class may already occur in a different place in the hierarchy
					found = hier.getClassTermByNameAddIfNotIn(cl_idx);
				} else {
					found = new ClassTerm(cl_idx);
				}
				// addParent() can be called at most once, because after that
				// found will will be a child of this class
				found.addParent(this);
				m_Hash.put(cl_idx, found);
				m_SubTerms.add(found);
			}
			level++;
			if (level < val.getNbLevels()) found.addClass(val, level, hier);
		}
	}

	public void sortChildrenByID() {
		Collections.sort(m_SubTerms);
	}

	public void getMeanBranch(boolean[] enabled, SingleStat stat) {
		int nb_branch = 0;
		for (int i = 0; i < getNbChildren(); i++) {
			ClassTerm child = (ClassTerm)getChild(i);
			if (enabled == null || enabled[child.getIndex()]) {
				nb_branch += 1;
				child.getMeanBranch(enabled, stat);
			}
		}
		if (nb_branch != 0) {
			stat.addFloat(nb_branch);
		}
	}

	public boolean hasChildrenIn(boolean[] enable) {
		for (int i = 0; i < getNbChildren(); i++) {
			ClassTerm child = (ClassTerm)getChild(i);
			if (enable[child.getIndex()]) return true;
		}
		return false;
	}

	public Node getParent() {
		return null;
	}

	public Node getChild(int idx) {
		return (Node)m_SubTerms.get(idx);
	}

	public int getNbChildren() {
		return m_SubTerms.size();
	}

	public boolean atTopLevel() {
		return m_Parents.size() == 0;
	}

	public boolean atBottomLevel() {
		return m_SubTerms.size() == 0;
	}

	public void setParent(Node parent) {
	}

	public final String getID() {
		return m_ID;
	}

	public final void setID(String id) {
		m_ID = id;
	}

	public String getKeysVector() {
		StringBuffer buf = new StringBuffer();
		ArrayList keys = new ArrayList(m_Hash.keySet());
		for (int i = 0; i < keys.size(); i++) {
			if (i != 0) buf.append(", ");
			buf.append(keys.get(i));
		}
		return buf.toString();
	}

	public final ClassTerm getByName(String name) {
		return (ClassTerm)m_Hash.get(name);
	}

	public final ClassTerm getCTParent() {
		return null;
	}

	public final void print(int tabs, PrintWriter wrt, double[] counts, double[] weights) {
		for (int i = 0; i < m_SubTerms.size(); i++) {
			ClassTerm subterm = (ClassTerm)m_SubTerms.get(i);
			wrt.print(StringUtils.makeString(' ', tabs)+subterm.getID());
			int nb_par = subterm.getNbParents();
			if (nb_par > 1) {
				// DAG mode: node has more than one parent
				int p_idx = 0;
				StringBuffer buf = new StringBuffer();
				buf.append("(p: ");
				for (int j = 0; j < nb_par; j++) {
					ClassTerm cr_par = subterm.getParent(j);
					if (cr_par != this) {
						if (p_idx != 0) buf.append(",");
						buf.append(cr_par.getID());
						p_idx ++;
					}
				}
				buf.append(")");
				wrt.print(" "+buf.toString());
			}
			int no = subterm.getIndex();
			if (no == -1) {
				wrt.print(": [error index -1]");
			} else {
				if (counts != null) {
					double count = counts[no];
					wrt.print(": "+ClusFormat.FOUR_AFTER_DOT.format(count));
				}
				if (weights != null) {
					double weight = weights[no];
					wrt.print(": "+ClusFormat.THREE_AFTER_DOT.format(weight));
				}
			}
			wrt.println();
			subterm.print(tabs+6, wrt, counts, weights);
		}
	}

	public void fillVectorNodeAndAncestors(double[] array) {
		int idx = getIndex();
		if (idx != -1 && array[idx] == 0.0) {
			array[idx] = 1.0;
			for (int i = 0; i < getNbParents(); i++) {
				getParent(i).fillVectorNodeAndAncestors(array);
			}
		}
	}

	public void fillBoolArrayNodeAndAncestors(boolean[] array) {
		int idx = getIndex();
		if (idx != -1 && !array[idx]) {
			array[idx] = true;
			for (int i = 0; i < getNbParents(); i++) {
				getParent(i).fillBoolArrayNodeAndAncestors(array);
			}
		}
	}

	public int getNbLeaves() {
		int nbc = m_SubTerms.size();
		if (nbc == 0) {
			return 1;
		} else {
			int total = 0;
			for (int i = 0; i < m_SubTerms.size(); i++) {
				ClassTerm subterm = (ClassTerm)m_SubTerms.get(i);
				total += subterm.getNbLeaves();
			}
			return total;
		}
	}

	public void addChild(Node node) {
		String id = ((ClassTerm)node).getID();
		m_Hash.put(id, node);
		m_SubTerms.add(node);
	}

	public void addChildCheckAndParent(ClassTerm node) {
		String id = node.getID();
		if (!m_Hash.containsKey(id)) {
			m_Hash.put(id, node);
			m_SubTerms.add(node);
			node.addParent(this);
		}
	}

	public void removeChild(int idx) {
		// used by artificial data generator only
		ClassTerm child = (ClassTerm)getChild(idx);
		m_Hash.remove(child.getID());
		m_SubTerms.remove(idx);
	}

	public void numberChildren() {
		m_Hash.clear();
		for (int i = 0; i < m_SubTerms.size(); i++) {
			ClassTerm subterm = (ClassTerm)m_SubTerms.get(i);
			String key = String.valueOf(i+1);
			subterm.setID(key);
			m_Hash.put(key, subterm);
		}
	}

	public void removeChild(Node node) {
			ClassTerm child = (ClassTerm)node;
			m_Hash.remove(child.getID());
			m_SubTerms.remove(child);
	}

	public int getLevel() {
		int depth = 0;
		ClassTerm parent = getCTParent();
		while (parent != null) {
			parent = parent.getCTParent();
			depth++;
		}
		return depth;
	}

	public int getMaxDepth() {
		return m_MaxDepth;
	}

	public int getMinDepth() {
		return m_MinDepth;
	}

	public void setMinDepth(int depth) {
		m_MinDepth = depth;
	}

	public void setMaxDepth(int depth) {
		m_MaxDepth = depth;
	}

	public String toPathString() {
		return toPathString("/");
	}

	public String toPathString(String sep) {
		if (getIndex() == -1) {
			return "R";
		} else {
			ClassTerm term = this;
			String path = term.getID();
			while (true) {
				int nb_par = term.getNbParents();
				if (nb_par != 1) {
					return "P" + sep + path;
				}
				term = term.getParent(0);
				if (term.getIndex() == -1) return path;
				path = term.getID() + sep + path;
			}
		}
	}

	public String toString() {
		return toStringHuman(null);
	}

	public String toStringHuman(ClassHierarchy hier) {
		if (hier != null && hier.isDAG()) {
			return getID();
		} else {
			return toPathString();
		}
	}
}
