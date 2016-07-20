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

package jeans.io.ini;

import java.io.*;
import java.util.*;

import jeans.util.MStreamTokenizer;

public class INIFile extends INIFileSection {

	public final static long serialVersionUID = 1;

	public final static int TYPE_DOUBLE_ARRAY = 0;
	public final static int TYPE_STRING_ARRAY = 1;
	public final static int TYPE_INT_ARRAY = 2;

	public INIFile() {
		super("");
	}

	public void addNode(INIFileNode entry) {
		m_hEntries.put(entry.getName(), entry);
		m_hEntryList.addElement(entry);
	}

	public void load(String fname) throws FileNotFoundException, IOException {
		load(fname, '#');
	}

	public void load(String fname, char comment) throws FileNotFoundException, IOException {
		MStreamTokenizer tokens = new MStreamTokenizer(fname);
		load(tokens, comment);
		tokens.close();
	}

	public void load(Reader reader, char comment) throws IOException {
		MStreamTokenizer tokens = new MStreamTokenizer(reader);
		load(tokens, comment);
		tokens.close();
	}

	public void save(String fname) throws IOException {
		PrintWriter writer = new PrintWriter(new FileOutputStream(fname));
		save(writer);
		writer.close();
	}

	public void load(MStreamTokenizer tokens, char comment) throws IOException {
		tokens.setCharTokens("[]=<>,{}");
		tokens.setCommentChar(comment);
		while (true) {
			String token = "";
			while (token != null && !token.equals("[")) token = tokens.getToken();
			if (token == null) break;
			int saveline = tokens.getLine();
			String name = tokens.readTillEol();
			// Kill trailing ']'
			int idx1 = name.indexOf(']');
			if (idx1 == -1) throw new IOException("Error in the settings file. Character ']' expected at line: "+saveline);
			// Check for sectiongroup ','
			int idx2 = name.indexOf(',');
			if (idx2 != -1) {
				String groupName = name.substring(0, idx2).trim();
				String sectionName = name.substring(idx2+1, idx1).trim();
				doLoad(sectionName, groupName, tokens);
			} else {
				doLoad(name.substring(0, idx1).trim(), tokens);
			}
		}
	}

	public void save(PrintWriter writer) throws IOException {
		for (Enumeration e = getNodes(); e.hasMoreElements(); ) {
			INIFileNode section = (INIFileNode)e.nextElement();
			if (section.isEnabled()) {
				section.save(writer);
				writer.println();
			}
		}
	}
}
