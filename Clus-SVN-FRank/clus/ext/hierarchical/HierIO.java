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

import jeans.util.*;
import jeans.util.array.*;

import clus.util.*;

public class HierIO {

	public final static int TAB_SIZE = 8;

	protected StringTable m_Stab = new StringTable();

	public HierIO() {
	}

	public ClassHierarchy readHierarchy(String fname) throws IOException, ClusException {
		HierIOReader rdr = new HierIOReader(fname);
		ClassHierarchy hier = new ClassHierarchy();
		rdr.readHierarchy(hier.getRoot(), 0);
		return hier;
	}

	public StringTable getTable() {
		return m_Stab;
	}

	public static int getDepth(String line) {
		int idx = 0, spc = 0, tab = 0;
		int len = line.length();
		while (idx < len && (line.charAt(idx) == ' ' || line.charAt(idx) == '\t')) {
			if (line.charAt(idx) == ' ') spc++;
			if (line.charAt(idx) == '\t') tab++;
			idx++;
		}
		return spc/TAB_SIZE + tab;
	}

	public String getID(String line) {
		String id = StringUtils.trimSpacesAndTabs(line);
		return m_Stab.get(id);
	}

	public static void writePrologTerm(ClassTerm term, PrintWriter writer) {
		String prologID = StringUtils.removeChar(term.getID(), '\'');
		writer.print("node('"+prologID+"',[");
		for (int i = 0; i < term.getNbChildren(); i++) {
			if (i != 0) writer.print(",");
			ClassTerm subterm = (ClassTerm)term.getChild(i);
			writePrologTerm(subterm, writer);
		}
		writer.print("])");
	}

	public static boolean hasActiveChild(ClassTerm term, boolean[] bits) {
		for (int i = 0; i < term.getNbChildren(); i++) {
			ClassTerm child = (ClassTerm)term.getChild(i);
			if (bits[child.getIndex()]) return true;
		}
		return false;
	}

	public static void writePrologTerm(ClassTerm term, boolean[] bits, PrintWriter writer) {
		// String prologID = StringUtils.removeChar(term.getID(), '\'');
		writer.print("node([");
		for (int i = 0; i < term.getNbChildren(); i++) {
			if (i != 0) writer.print(",");
			ClassTerm subterm = (ClassTerm)term.getChild(i);
			if (bits[subterm.getIndex()]) {
				if (hasActiveChild(subterm, bits)) writePrologTerm(subterm, bits, writer);
				else writer.print("1");
			} else {
				writer.print("0");
			}
		}
		writer.print("])");
	}

	public static void writePrologGraph(String name, ClassTerm term, PrintWriter writer) {
		String prologID = StringUtils.removeChar(term.getID(), '\'');
		writer.println(name+"_name("+term.getIndex()+",'"+prologID+"').");
		writer.print(name+"_children("+term.getIndex()+",[");
		for (int i = 0; i < term.getNbChildren(); i++) {
			if (i != 0) writer.print(",");
			ClassTerm subterm = (ClassTerm)term.getChild(i);
			writer.print(subterm.getIndex());
		}
		writer.println("]).");
		for (int i = 0; i < term.getNbChildren(); i++) {
			ClassTerm subterm = (ClassTerm)term.getChild(i);
			writer.println(name+"_child("+term.getIndex()+","+subterm.getIndex()+").");
		}
		writer.println();
		for (int i = 0; i < term.getNbChildren(); i++) {
			ClassTerm subterm = (ClassTerm)term.getChild(i);
			writePrologGraph(name, subterm, writer);
		}
	}

	public void writeHierarchy(ClassTerm term) {
		writeHierarchy(term, ClusFormat.OUT_WRITER);
		ClusFormat.OUT_WRITER.flush();
	}

	public void writeHierarchy(ClassTerm term, PrintWriter writer) {
		writeHierarchy(0, term, writer);
		writer.flush();
	}

	public void writeHierarchy(int tabs, ClassTerm term, PrintWriter writer) {
		for (int i = 0; i < term.getNbChildren(); i++) {
			ClassTerm subterm = (ClassTerm)term.getChild(i);
			writer.print(StringUtils.makeString('\t', tabs)+subterm.getID());

//			int no = subterm.getIndex();
//			if (counts != null) {
//				double count = counts[no];
//				wrt.print(": "+ClusFormat.FOUR_AFTER_DOT.format(count));
//			}

			writer.println();
			writeHierarchy(tabs+1, subterm, writer);
		}
	}

	private class HierIOReader {

		protected LineNumberReader m_Reader;
		protected String m_LastLine;

		public HierIOReader(String fname) throws IOException {
			m_Reader = new LineNumberReader(new InputStreamReader(new FileInputStream(fname)));
			m_LastLine = null;
		}

		public void readHierarchy(ClassTerm parent, int depth) throws IOException, ClusException {
			ClassTerm child = null;
			String line = getLine();
			while (line != null) {
				int mydepth = getDepth(line);
				if (mydepth == depth) {
					String id = getID(line);
					child = new ClassTerm(id);
					child.addParent(parent);
					parent.addChild(child);
				} else {
					setLastLine(line);
					if (mydepth < depth) {
						return;
					} else {
						if (mydepth > depth+1) {
							throw new ClusException("Jump to big in hierarchy");
						}
						if (child == null) {
							throw new ClusException("Term has no parent");
						}
						readHierarchy(child, depth+1);
					}
				}
				line = getLine();
			}
		}

		public String getLine() throws IOException {
			if (m_LastLine != null) {
				String result = m_LastLine;
				clearLastLine();
				return result;
			}
			return m_Reader.readLine();
		}

		public void setLastLine(String line) {
			m_LastLine = line;
		}

		public void clearLastLine() {
			m_LastLine = null;
		}


	}
}
