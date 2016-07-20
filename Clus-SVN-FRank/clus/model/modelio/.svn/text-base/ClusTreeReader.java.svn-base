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
 * Created on Apr 28, 2005
 */
package clus.model.modelio;

import java.io.*;
import java.util.*;

import jeans.util.*;

import clus.algo.tdidt.*;
import clus.data.type.*;
import clus.model.test.*;

public class ClusTreeReader {

	public final static String[] YESNO = {"yes", "no"};

	String m_FName;
	ClusSchema m_Schema;
	LineNumberReader m_Reader;
	MStringTokenizer m_Tokens = createTokenizer();
	int m_StartLine, m_CrLine, m_PushBack;
	boolean m_IsReading, m_NoPartialTree;
	ArrayList m_Lines, m_StartPos;
	String m_LineAfterTree;

	public ClusNode loadTree(String fname, ClusSchema schema) throws IOException {
		m_FName = fname;
		m_Schema = schema;
		System.out.println("Loading constraint file: "+fname);
		LineNumberReader rdr = new LineNumberReader(new InputStreamReader(new FileInputStream(fname)));
		String line = rdr.readLine();
		/* Skip empty lines */
		while (line != null && isSkipLine(line)) {
			line = rdr.readLine();
		}
		/* Read tree */
		ClusNode root = null;
		if (line != null) {
			root = readTree(line, rdr);
		}
		line = getFirstNonEmptyLine(rdr);
		if (line != null) {
			createError("Extra data after tree '"+line+"'");
		}
		rdr.close();
		return root;
	}

	public ClusNode loadOutTree(String fname, ClusSchema schema, String find) throws IOException {
		m_FName = fname;
		m_Schema = schema;
		m_NoPartialTree = true;
		System.out.println("Loading .out file: "+fname);
		LineNumberReader rdr = new LineNumberReader(new InputStreamReader(new FileInputStream(fname)));
		String line = rdr.readLine();
		while (line != null && !line.trim().equals(find)) {
			line = rdr.readLine();
		}
		/* Skip line with stars */
		line = rdr.readLine();
		line = rdr.readLine();
		/* Skip empty lines */
		while (line != null && isSkipLine(line)) {
			line = rdr.readLine();
		}
		/* Read tree */
		ClusNode root = null;
		if (line != null) {

			root = readTree(line, rdr);
		}
		line = getFirstNonEmptyLine(rdr);
		m_LineAfterTree = line;
		rdr.close();
		return root;
	}

	public ClusNode loadTreeTree(String fname, ClusSchema schema) throws IOException {
		m_FName = fname;
		m_Schema = schema;
		m_NoPartialTree = true;
		System.out.println("Loading .tree file: "+fname);
		LineNumberReader rdr = new LineNumberReader(new InputStreamReader(new FileInputStream(fname)));
		String line = rdr.readLine();

		/* Read tree */
		ClusNode root = null;
		root = readTree(line, rdr);

		rdr.close();
		return root;
	}

	public ClusNode readTree(String line, LineNumberReader rdr) throws IOException {
		m_Reader = rdr;
		m_IsReading = true;
		m_PushBack = -1;
		return readTree(line);
	}

	public ClusNode readTree(String line) throws IOException {
		String trim = line.trim();
		System.out.println("Reading: '"+line+"'");
		ClusNode result = new ClusNode();
		if (!trim.equals("?")) {
			int arity = 2;
			String[] branchnames = YESNO;
			MStringTokenizer tokens = getTokens();
			tokens.setString(trim);
			if (tokens.hasMoreTokens()) 			{
				String attrname = tokens.getToken();
				ClusAttrType attr = findAttrType(attrname, allowPartialTree());
				if (attr != null) {
					readTest(attr, result, tokens);
					if (result.getTest() instanceof NominalTest) {
						NominalAttrType ntype = (NominalAttrType)attr;
						arity = ntype.getNbValues();
						branchnames = ntype.getValues();
					}
				} else {
					return result;
				}
			} else {
				createError("No attribute name found");
			}
			result.setNbChildren(arity);
			boolean[] has_ch = new boolean[arity];
			boolean is_wildcard = false;
			for (int i = 0; i < arity && !is_wildcard; i++) {
				is_wildcard = readNextChild(result, branchnames, has_ch);
				if (is_wildcard) {
					readWildCardChild(result, has_ch);
					startPlayBack();
					for (int j = i+1; j < arity; j++) {
						reset();
						readWildCardChild(result, has_ch);
					}
					stopPlayBack();
				}
			}
		} else {
			checkPartialTreeAllowed("while reading child node");
		}
		return result;
	}

	public String getLineAfterTree() {
		return m_LineAfterTree;
	}

	public boolean allowPartialTree() {
		return !m_NoPartialTree;
	}

	public void checkPartialTreeAllowed(String str) throws IOException {
		if (!allowPartialTree()) {
			createError("No question marks allowed in tree ("+str+")");
		}
	}

	public static boolean isSkipLine(String line) {
		String trim = line.trim();
		if (trim.equals("")) return true;
		if (trim.length() > 1 && trim.charAt(0) == '%') return true;
		return false;
	}

	public ClusAttrType findAttrType(String name, boolean shouldfind) throws IOException {
		ClusAttrType type = m_Schema.getAttrType(name);
		if (type == null && shouldfind) {
			createError("Unknown attribute name: '"+name+"'");
		}
		return type;
	}

	public void readCharTokens(String chars, MStringTokenizer tokens) throws IOException {
		String token = tokens.getToken();
		for (int pos = 0; pos < chars.length(); pos++) {
			if (!chars.substring(pos,pos+1).equals(token)) {
				createError("Expected '"+chars.substring(pos,pos+1)+"', not '"+token+"'");
			}
			if (pos < chars.length()-1) {
				if (tokens.hasMoreTokens()) {
					token = tokens.getToken();
				} else {
					createError("Expected '"+chars.substring(pos+1,pos+2)+"' at end of line");
				}
			}
		}
	}

	public void readNumericTest(ClusAttrType attr, ClusNode node, MStringTokenizer tokens) throws IOException {
		NumericTest test = new NumericTest(attr);
		if (tokens.hasMoreTokens()) {
			readCharTokens(">", tokens);
			if (tokens.hasMoreTokens()) {
				String value = tokens.getToken();
				if (value.equals("?")) {
					checkPartialTreeAllowed("in numeric test");
				} else {
					try {
						double bound = Double.parseDouble(value);
						test.setBound(bound);
					} catch (NumberFormatException e) {
						createError("Expected numeric value for '"+attr.getName()+"', not '"+value+"'");
					}
				}
			} else {
				createError("Expected numeric value for test on '"+attr.getName()+"'");
			}
		}
		node.setTest(test);
	}

	public int readSingleValue(boolean[] isin, NominalAttrType attr, MStringTokenizer tokens) throws IOException {
		if (tokens.hasMoreTokens()) {
			String token = tokens.getToken();
			if (token.equals("?")) {
				checkPartialTreeAllowed("in subset test");
			} else {
				Integer res = attr.getValueIndex(token);
				if (res == null) {
					createError("Value '"+token+"=' not in domain of '"+attr.getName()+"'");
				}
				isin[res.intValue()] = true;
				return 1;
			}
		} else {
			createError("Expected value after '=' while reading test on '"+attr.getName()+"'");
		}
		return 0;
	}

	public int readMultiValue(boolean[] isin, NominalAttrType attr, MStringTokenizer tokens) throws IOException {
		int nb = 0;
		if (tokens.hasMoreTokens()) {
			String token = tokens.getToken();
			if (token.equals("?")) {
				checkPartialTreeAllowed("in subset test");
			} else {
				if (!token.equals("{")) {
					createError("Expected set after 'in' while reading test on '"+attr.getName()+"'");
				}
				while (tokens.hasMoreTokens()) {
					String name = tokens.getToken();
					Integer res = attr.getValueIndex(name);
					if (res == null) {
						createError("Value '"+name+"=' not in domain of '"+attr.getName()+"'");
					}
					isin[res.intValue()] = true; nb++;
					if (tokens.hasMoreTokens()) {
						String sep = tokens.getToken();
						if (sep.equals("}")) break;
						if (!sep.equals(",")) {
							createError("Expected '}' or ',' while reading test on '"+attr.getName()+"'");
						}
					} else {
						createError("End of set expected while reading test on '"+attr.getName()+"'");
					}
				}
			}
		} else {
			createError("Expected value after 'in' while reading test on '"+attr.getName()+"'");
		}
		return nb;
	}

	public void readNominalTest(ClusAttrType attr, ClusNode node, MStringTokenizer tokens) throws IOException {
		NominalAttrType ntype = (NominalAttrType)attr;
		if (tokens.hasMoreTokens()) {
			int nb = 0;
			boolean[] isin = new boolean[ntype.getNbValues()];
			String token = tokens.getToken();
			if (token.equals("=")) {
				nb = readSingleValue(isin, ntype, tokens);
			} else if (token.equals("in")) {
				nb = readMultiValue(isin, ntype, tokens);
			} else {
				createError("Expected '=' or 'in' while reading test on '"+attr.getName()+"'");
			}
			SubsetTest test = new SubsetTest(ntype, nb, isin, Double.NEGATIVE_INFINITY);
			node.setTest(test);
		} else {
			double[] freqs = new double[ntype.getNbValues()];
			Arrays.fill(freqs, Double.NEGATIVE_INFINITY);
			NominalTest test = new NominalTest(ntype, freqs);
			node.setTest(test);
		}
	}

	public void readTest(ClusAttrType attr, ClusNode node, MStringTokenizer tokens) throws IOException {
		switch (attr.getTypeIndex()) {
			case NumericAttrType.THIS_TYPE:
				readNumericTest(attr, node, tokens);
				break;
			case NominalAttrType.THIS_TYPE:
				readNominalTest(attr, node, tokens);
				break;
			default:
				createError("Unsupported attribute type '"+attr.getTypeName()+"'");
		}
	}

	public boolean readNextChild(ClusNode node, String[] bnames, boolean[] has_ch) throws IOException {
		String str = readLine();
		/* Count number of branches that have already been read */
		int cnt = 1;
		for (int i = 0; i < has_ch.length; i++) {
			if (has_ch[i]) cnt++;
		}
		/* End of file encountered? */
		if (str == null) {
			createError("End of file encountered while reading branch "+cnt+" for "+node.getTest());
		}
		/* Find branch name marker */
		int idx = str.indexOf(':');
		if (idx == -1) {
			createError("Expected ':' in start of branch "+cnt+" for "+node.getTest()+", not '"+str+"'");
		}
		/* Extract branch name */
		String name = str.substring(0, idx);
		int idxmm = name.lastIndexOf("--");
		if (idxmm != -1) {
			name = name.substring(idxmm+2).trim();
		}
		/* Is this a wildcard branch? */
		if (name.equals("?")) {
			checkPartialTreeAllowed("as child node '"+str+"'");
			startRecording(str);
			return true;
		}
		/* At what index should we put this? */
		int pos = -1;
		for (int i = 0; i < bnames.length; i++) {
			if (name.equals(bnames[i])) {
				pos = i;
				break;
			}
		}
		/* Valid branch name? */
		if (pos == -1) {
			StringBuffer names = new StringBuffer("{");
			for (int i = 0; i < bnames.length; i++) {
				if (i != 0) names.append(",");
				names.append(bnames[i]);
			}
			names.append("}");
			createError("Branch start '"+name+"' does not match "+names+" for "+node.getTest());
		}
		/* Did we already see this one? */
		if (has_ch[pos]) {
			createError("Branch '"+bnames[pos]+"' occurs twice for "+node.getTest());
		}
		has_ch[pos] = true;
		/* Is our string long enough? */
		if (str.length() <= idx+1) {
			createError("Expected start of branch after '"+name+"' for "+node.getTest());
		}
		/* OK, I guess we have a valid branch here :-) */
		String rest = str.substring(idx+1).trim();
		ClusNode child = readTree(rest);
		node.setChild(child, pos);
		return false;
	}

	public void readWildCardChild(ClusNode node, boolean[] has_ch) throws IOException {
		String str = readLine();
		/* Count number of branches that have already been read */
		int cnt = 1;
		int first_free = -1;
		for (int i = 0; i < has_ch.length; i++) {
			if (has_ch[i]) cnt++;
			else if (first_free == -1) first_free = i;
		}
		/* Full? */
		if (first_free == -1) {
			createError("Internal error (all branches already read) while reading wildcard branch for "+node.getTest());
		}
		/* End of file encountered? */
		if (str == null) {
			createError("End of file encountered while reading wildcard branch "+cnt+" for "+node.getTest());
		}
		/* Find branch name marker */
		int idx = str.indexOf(':');
		if (idx == -1) {
			createError("Expected ':' in start of wildcard branch "+cnt+" for "+node.getTest()+", not '"+str+"'");
		}
		/* Is our string long enough? */
		if (str.length() <= idx+1) {
			createError("Expected start of branch after wildcard '?:' for "+node.getTest());
		}
		/* OK, I guess we have a valid branch here :-) */
		String rest = str.substring(idx+1).trim();
		ClusNode child = readTree(rest);
		node.setChild(child, first_free);
		has_ch[first_free] = true;
	}

	public void createError(String err) throws IOException {
		throw new IOException(err+" ("+m_FName+": "+getLineNumber()+")");
	}

	public String readLine() throws IOException {
		if (m_PushBack != -1) {
			String result = (String)m_Lines.get(m_PushBack);
			m_PushBack = -1;
			return result;
		}
		if (m_IsReading) {
			String line = m_Reader.readLine();
			if (isRecording() && line != null) {
				m_Lines.add(line);
				m_CrLine++;
			}
			return line;
		} else {
			return (String)m_Lines.get(m_CrLine++);
		}
	}

	public void markStartRecording() {
		m_PushBack = m_CrLine-1;
		m_StartPos.add(new Integer(m_CrLine-1));
	}

	public void startRecording(String firstline) {
		if (m_Lines != null) {
			markStartRecording();
		} else {
			m_CrLine = 1;
			m_StartLine = m_Reader.getLineNumber() - 1;
			m_Lines = new ArrayList();
			m_Lines.add(firstline);
			m_StartPos = new ArrayList();
			markStartRecording();
		}
	}

	public void startPlayBack() {
		m_IsReading = false;
		reset();
	}

	public void stopPlayBack() {
		m_StartPos.remove(m_StartPos.size()-1);
		if (m_StartPos.size() == 0) {
			m_IsReading = true;
			m_Lines = null;
			m_StartPos = null;
		}
	}

	public void reset() {
		Integer pos = (Integer)m_StartPos.get(m_StartPos.size()-1);
		m_CrLine = pos.intValue();
	}

	public boolean isRecording() {
		return m_Lines != null;
	}

	public int getLineNumber() {
		if (m_IsReading) return m_Reader.getLineNumber();
		else return m_StartLine + m_CrLine;
	}

	public MStringTokenizer getTokens() {
		return m_Tokens;
	}

	public static boolean checkAtEnd(LineNumberReader rdr) throws IOException {
		String line = rdr.readLine();
		/* Skip empty lines */
		while (line != null && isSkipLine(line)) {
			line = rdr.readLine();
		}
		return line == null;
	}

	public static String getFirstNonEmptyLine(LineNumberReader rdr) throws IOException {
		String line = rdr.readLine();
		/* Skip empty lines */
		while (line != null && isSkipLine(line)) {
			line = rdr.readLine();
		}
		return line;
	}

	public static MStringTokenizer createTokenizer() {
		MStringTokenizer tokens = new MStringTokenizer();
		tokens.setCharTokens("=<>{},");
		//to use spaces in attributes when reading tree(eg. specify constraints)
		tokens.setBlockChars("\"", "\"");
		tokens.setSpaceChars(" \t");
		return tokens;
	}
}
