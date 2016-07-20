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

package clus.model.modelio.tilde;

import java.io.*;
import java.util.*;
import jeans.util.*;

import clus.main.*;
import clus.algo.tdidt.ClusNode;
import clus.data.type.*;
import clus.util.*;

public class TildeOutReader {

	public final static int SINGLE_CLASSIFY = 0;
	public final static int MULTI_CLASSIFY = 1;
	public final static int REGRESSION = 2;

	protected final static boolean m_Debug = true;

	protected int m_Dim = 1;
	protected int m_Mode = MULTI_CLASSIFY;
	protected ClusNode m_Root;
	protected MStreamTokenizer m_Tokens;
	protected MyArray m_Info = new MyArray();
	protected ClusSchema m_Schema = new ClusSchema("TildeTree");
	protected NominalAttrType m_Target;
	protected ClusStatManager m_StatMgr;

	public TildeOutReader(InputStream strm) {
		m_Tokens = new MStreamTokenizer(strm);
		m_Tokens.setCharTokens(":,+-()|[]");
	}

	public void doParse() throws IOException {
		toSettings();
		readSettings();
		skipHeader();
		try {
			m_Schema.addIndices(ClusSchema.ROWS);
			m_StatMgr = new ClusStatManager(m_Schema, new Settings());
			m_StatMgr.initStatisticAndStatManager();
			TildeTreeReader treer = new TildeTreeReader(this);
			m_Root = treer.readTree();
			m_Root.addChildStats();
			m_Schema.showDebug();
			System.out.println("Total Root Node: "+m_Root.getClusteringStat());
		} catch (ClusException e) {
			throw new IOException(e.getMessage());
		}
	}

	public void setDim(int dim) {
		m_Dim = dim;
	}

	public int getDim() {
		return m_Dim;
	}

	public boolean getDebug() {
		return m_Debug;
	}

	public ClusStatManager getStatMgr() {
		return m_StatMgr;
	}

	public ClusNode getTree() {
		return m_Root;
	}

	public int getMode() {
		return m_Mode;
	}

	public void readTargetSchema(String fname) throws IOException {
		MStreamTokenizer tokens = new MStreamTokenizer(fname);
		int nb = Math.min(1/*target.getNbNom()*/, getDim());
		for (int i = 0; i < nb; i++) {
			NominalAttrType nom = null; /*target.getNomType(i);*/
			nom.setName(tokens.readToken());
			tokens.readChar('{');
			for (int j = 0; j < nom.getNbValues(); j++) {
				if (j != 0) tokens.readChar(',');
				nom.setValue(j, tokens.readToken());
			}
			tokens.readChar('}');
		}
	}

	public void skip(String skip) throws IOException {
		boolean done = false;
		while (!done) {
			String line = m_Tokens.readTillEol();
			if (line.indexOf(skip) != -1) done = true;
		}
	}

	public void skipHeader() throws IOException {
		skip("notation of pruned tree");
	}

	public void toSettings() throws IOException {
		skip("ettings");

	}

	public void readInfo() throws IOException {
		skip("--------------------------");
		boolean done = false;
		while (!done) {
			String line = m_Tokens.readTillEol();
			if (line.indexOf("Compact notation") != -1) done = true;
			else m_Info.addElement(line);
		}
		skip("Compact notation of pruned tree");
    	}

	public void readSetting(String setting) throws IOException {
		if (m_Debug) System.out.println("Setting: "+setting);
		if (setting.equals("classes")) {
			System.out.println("Do classes ***********");
			int count = 0;
			Vector values = new Vector();
			m_Tokens.readChar('[');
			while (!m_Tokens.isNextToken(']')) {
				if (count > 0) m_Tokens.readChar(',');
				values.addElement(m_Tokens.readToken());
				count++;
			}
			m_Target = new NominalAttrType("Target", values.size());
			m_Target.setStatus(ClusAttrType.STATUS_TARGET);
			for (int i = 0; i < values.size(); i++) {
				String value = (String)values.elementAt(i);
				m_Target.setValue(i, value);
			}
			m_Tokens.readTillEol();
		} else if (setting.equals("tilde_mode")) {
			String mode = m_Tokens.readTillEol();
			if (mode.indexOf("classify") != -1) m_Mode = MULTI_CLASSIFY;
			else m_Mode = REGRESSION;
			System.out.println("Tilde mode: "+mode);
		} else {
			m_Tokens.readTillEol();
		}
	}

	public void readSettings() throws IOException {
		boolean done = false;
		while (!done) {
			String token = m_Tokens.readToken();
			if (token.equals("*")) {
				String setting = m_Tokens.readToken();
				m_Tokens.readChar(':');
				readSetting(setting);
			} else if (token.equals("**")) {
				m_Tokens.readTillEol();
			} else {
		    		System.out.println("Error: "+token);
				done = true;
		    	}
		}
		if (m_Mode == REGRESSION) {
			System.out.println("Dimension = "+getDim());
			for (int i = 0; i < getDim(); i++) {
				NominalAttrType target = new NominalAttrType("Target", 2);
				target.setStatus(ClusAttrType.STATUS_TARGET);
				target.setValue(0, "pos");
				target.setValue(1, "neg");
				m_Schema.addAttrType(target);
			}
		} else {
			m_Schema.addAttrType(m_Target);
		}
	}

	public MStreamTokenizer getStream() {
		return m_Tokens;
	}

	public void close() throws IOException {
		m_Tokens.close();
	}
}
