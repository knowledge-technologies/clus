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

import jeans.util.array.*;

import clus.util.*;
import clus.main.*;

public class ClassesValue implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public static String HIERARCY_SEPARATOR = "/";
	public static String ABUNDANCE_SEPARATOR = ":";
	public static String EMPTY_SET_INDICATOR = "?";

	public final static int NO_ABUNDANCE = 0;
	public final static int PATH_ABUNDANCE = 1;
	public final static int NODE_ABUNDANCE = 2;

	protected String[] m_Path;
	protected boolean m_Intermediate;
	protected double m_Abundance = 1.0;
	protected ClassTerm m_ClassTerm;

	public ClassesValue(String constr, StringTable table) throws ClusException {
		StringTokenizer tokens = new StringTokenizer(constr, HIERARCY_SEPARATOR + ABUNDANCE_SEPARATOR);
		int plen = tokens.countTokens();
		m_Path = new String[plen];
		if (plen == 0)
			throw new ClusException("Path length should be >= 1");
		int idx = 0;
		while (tokens.hasMoreTokens()) {
			String st = table.get(tokens.nextToken());
			if (st.equals("0")) {
				String[] old_path = m_Path;
				m_Path = new String[idx];
				System.arraycopy(old_path, 0, m_Path, 0, m_Path.length);
				while (tokens.hasMoreTokens()) {
					st = table.get(tokens.nextToken());
					if (!st.equals("0")) throw new ClusException("Hierarchical class must not contain internal zeros");
				}
				return;
			} else {
				m_Path[idx] = st;
			}
			idx++;
		}
	}

	public ClassesValue(ClassTerm term) {
		m_ClassTerm = term;
	}

	public ClassesValue(ClassTerm term, double abundance) {
		m_Abundance = abundance;
		m_ClassTerm = term;
	}

	public ClassesValue(int len) {
		m_Path = new String[len];
	}

	public boolean isRoot() {
		return getTerm().getIndex() == -1;
	}

	public ClassTerm getTerm() {
		return m_ClassTerm;
	}

	public void setClassTerm(ClassTerm term) {
		m_ClassTerm = term;
	}

	public final int getIndex() {
		return m_ClassTerm.getIndex();
	}

	public ClassesValue toFlat(StringTable table) {
		ClassesValue val = new ClassesValue(1);
		val.setPath(table.get(toPathString()), 0);
		return val;
	}

	public double getAbundance() {
		return m_Abundance;
	}

	public void setAbundance(double abundance) {
		m_Abundance = abundance;
	}

	public void setIntermediate(boolean inter) {
		m_Intermediate = inter;
	}

	public boolean isIntermediate() {
		return m_Intermediate;
	}

	public static void setHSeparator(String hsep) {
		HIERARCY_SEPARATOR = hsep;
	}

	public static void setEmptySetIndicator(String empty) {
		EMPTY_SET_INDICATOR = empty;
	}

	public boolean equalsValue(ClassesValue other) {
		if (m_Path.length != other.m_Path.length) return false;
		for (int i = 0; i < m_Path.length; i++) {
			if (!m_Path[i].equals(other.m_Path[i])) return false;
		}
		return true;
	}

	public String getMostSpecificClass() {
		return m_Path[m_Path.length-1];
	}

	public String getClassID(int level) {
		return m_Path[level];
	}

	public int getNbLevels() {
		return m_Path.length;
	}

	public void setPath(String strg, int i) {
		m_Path[i] = strg;
	}

	public final void addHierarchyIndices(ClassHierarchy hier) throws ClusException {
		ClassTerm term = hier.getClassTerm(this);
		setClassTerm(term);
    }

	public String toPathString() {
		ClassTerm term = getTerm();
		if (term != null) {
			return term.toPathString();
		} else {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < getNbLevels(); i++) {
				if (i != 0) buf.append('/');
				buf.append(getClassID(i));
			}
			return buf.toString();
		}
	}

	public String toStringData(ClassHierarchy hier) {
		if (hier != null && hier.isDAG()) {
			return getTerm().getID();
		} else {
			return toPathString();
		}
	}

	public String toStringWithDepths(ClassHierarchy hier) {
		if (hier != null && hier.isDAG()) {
			ClassTerm term = getTerm();
			if (term.getMinDepth() == term.getMaxDepth()) {
				return getTerm().getID()+"["+term.getMinDepth()+"]";
			} else {
				return getTerm().getID()+"["+term.getMinDepth()+";"+term.getMaxDepth()+"]";
			}
		} else {
			return toPathString();
		}
	}

	public String toString() {
		return toPathString();
	}
}
