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

import java.util.*;
import java.io.*;

import jeans.util.array.*;

import clus.main.Settings;
import clus.util.*;

public class ClassesTuple implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected ClassesValue[] m_Tuple;
	protected int m_Count;

	public ClassesTuple() {
	}

	public ClassesTuple(String constr, StringTable table) throws ClusException {
		if (constr.equals(ClassesValue.EMPTY_SET_INDICATOR)) {
			m_Tuple = new ClassesValue[0];
		} else {
			int idx = 0;
			StringTokenizer tokens = new StringTokenizer(constr, "@");
			int tlen = tokens.countTokens();
			m_Tuple = new ClassesValue[tlen];
			while (tokens.hasMoreTokens()) {
				ClassesValue val = new ClassesValue(tokens.nextToken(), table);
				m_Tuple[idx++] = val;
			}
			if (tlen == 0) new ClusException("Number of classes should be >= 1");
		}
	}

	public ClassesTuple(int size) {
		m_Tuple = new ClassesValue[size];
	}

	public boolean isRoot() {
		if (m_Tuple.length == 0) return true;
		if (m_Tuple.length == 1) {
			return ((ClassesValue)m_Tuple[0]).isRoot();
		}
		return false;
	}

	public boolean hasClass(int index) {
		for (int i = 0; i < m_Tuple.length; i++) {
			ClassesValue val = getClass(i);
			if (index == val.getIndex()) return true;
		}
		return false;
	}

	public ClassesTuple toFlat(StringTable table) {
		ClassesTuple tuple = new ClassesTuple(m_Tuple.length);
		for (int i = 0; i < m_Tuple.length; i++) {
			tuple.setItemAt(getClass(i).toFlat(table), i);
		}
		return tuple;
	}

	public void setLength(int size) {
		ClassesValue[] old = m_Tuple;
		m_Tuple = new ClassesValue[size];
		System.arraycopy(old, 0, m_Tuple, 0, size);
	}

	public boolean equalsTuple(ClassesTuple other) {
		if (m_Tuple.length != other.m_Tuple.length) return false;
		for (int i = 0; i < m_Tuple.length; i++) {
			if (!m_Tuple[i].equalsValue(other.m_Tuple[i])) return false;
		}
		return true;
	}

	public final void setSize(int size) {
		m_Tuple = new ClassesValue[size];
	}

	public final void setItemAt(ClassesValue item, int pos) {
		m_Tuple[pos] = item;
	}

	public final void addItem(ClassesValue item) {
		m_Tuple[m_Count++] = item;
	}

	public int getPosition(int idx) {
		return m_Tuple[idx].getIndex();
	}

	public final int getNbClasses() {
		return m_Tuple.length;
	}

	public final ClassesValue getClass(int idx) {
		return m_Tuple[idx];
	}

	public void updateDistribution(double[] distr, double weight) {
		for (int i = 0; i < getNbClasses(); i++) {
			distr[getClass(i).getIndex()] += weight;
		}
	}

	public final boolean[] getVectorBoolean(ClassHierarchy hier) {
		boolean[] vec = new boolean[hier.getTotal()];
		for (int i = 0; i < getNbClasses(); i++) {
			vec[getClass(i).getIndex()] = true;
		}
		return vec;
	}

	public final double[] getVector(ClassHierarchy hier) {
		double[] vec = new double[hier.getTotal()];
		for (int i = 0; i < getNbClasses(); i++) {
			vec[getClass(i).getIndex()] = 1.0;
		}
		return vec;
	}

	public final boolean[] getVectorBooleanNodeAndAncestors(ClassHierarchy hier) {
		boolean[] vec = new boolean[hier.getTotal()];
		fillBoolArrayNodeAndAncestors(vec);
		return vec;
	}

	public final double[] getVectorNodeAndAncestors(ClassHierarchy hier) {
		double[] vec = new double[hier.getTotal()];
		for (int i = 0; i < getNbClasses(); i++) {
			ClassesValue val = getClass(i);
			val.getTerm().fillVectorNodeAndAncestors(vec);
		}
		return vec;
	}

	public final void fillBoolArrayNodeAndAncestors(boolean[] interms) {
		for (int i = 0; i < getNbClasses(); i++) {
			ClassesValue val = getClass(i);
			val.getTerm().fillBoolArrayNodeAndAncestors(interms);
		}
	}

	public final void addIntermediateElems(ClassHierarchy hier, boolean[] alllabels, ArrayList added) {
		fillBoolArrayNodeAndAncestors(alllabels);
		for (int i = 0; i < hier.getTotal(); i++) {
			if (alllabels[i]) {
				ClassTerm term = hier.getTermAt(i);
				ClassesValue val = new ClassesValue(term);
				val.setIntermediate(term.hasChildrenIn(alllabels));
				added.add(val);
			}
		}
		m_Tuple = new ClassesValue[added.size()];
		System.arraycopy(added.toArray(), 0, m_Tuple, 0, added.size());
	}

	public final void cloneFrom(ClassesTuple tuple) {
		int size = tuple.m_Tuple.length;
		m_Tuple = new ClassesValue[size];
		System.arraycopy(tuple.m_Tuple, 0, m_Tuple, 0, size);
	}

	public String toString() {
		return toString(null, '@', false);
	}

	// Represent the set of classes to a human
	// Difference with toStringData() is that ',' is used as separator
	public String toStringHuman(ClassHierarchy hier) {
		return toString(hier, ',', false);
	}

	// For writing data, e.g., to a .arff file
	// Used by ClassesAttrType.getString(DataTuple tuple)
	public String toStringData(ClassHierarchy hier) {
		return toString(hier, '@', false);
	}

	public String toString(ClassHierarchy hier, char separator, boolean allowinterm) {
		if (getNbClasses() > 0) {
			int idx = 0;
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < getNbClasses(); i++) {
				ClassesValue val = getClass(i);
				if (allowinterm || !val.isIntermediate()) {
					if (idx != 0) buf.append(separator);
					buf.append(val.toStringData(hier));
					idx++;
				}
			}
			return buf.toString();
		} else {
			return "none";
		}
	}

	public void setAllIntermediate(boolean inter) {
		for (int i = 0; i < m_Tuple.length; i++) {
			((ClassesValue)m_Tuple[i]).setIntermediate(inter);
		}
	}

	public final void addToHierarchy(ClassHierarchy hier) {
		for (int i = 0; i < getNbClasses(); i++) {
			hier.addClass(getClass(i));
		}
	}

	public final void addHierarchyIndices(ClassHierarchy hier) throws ClusException {
		for (int i = 0; i < getNbClasses(); i++) {
			ClassesValue val = getClass(i);
			ClassTerm term = hier.getClassTerm(val);
			val.setClassTerm(term);
		}
	}

	public void removeLabels(boolean[] removed) {
		ArrayList left = new ArrayList();
		for (int i = 0; i < getNbClasses(); i++) {
			ClassesValue val = getClass(i);
			if (!removed[val.getIndex()]) left.add(val);
		}
		m_Tuple = new ClassesValue[left.size()];
		for (int i = 0; i < left.size(); i++) {
			m_Tuple[i] = (ClassesValue)left.get(i);
		}
	}

	public static ClassesTuple readFromFile(String fname, ClassHierarchy hier) throws ClusException, IOException {
		int idx = 0;
		BufferedReader input = new BufferedReader(new FileReader(fname));
		StringBuffer classes = new StringBuffer();
		String line = input.readLine();
		while (line != null) {
			line = line.trim();
			if (!line.equals("")) {
				if (idx != 0) classes.append("@");
				classes.append(line);
			}
			line = input.readLine();
			idx++;
		}
		input.close();
		ClassesTuple tuple = new ClassesTuple(classes.toString(), hier.getType().getTable());
		tuple.addHierarchyIndices(hier);
		return tuple;
	}

}
