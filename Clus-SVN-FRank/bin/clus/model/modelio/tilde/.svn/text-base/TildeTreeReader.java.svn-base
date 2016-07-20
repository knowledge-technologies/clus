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
import jeans.util.*;

import clus.algo.tdidt.ClusNode;
import clus.main.*;
import clus.model.test.*;
import clus.statistic.*;

public class TildeTreeReader {

	protected TildeOutReader m_Parent;

	public TildeTreeReader(TildeOutReader parent) {
		m_Parent = parent;
	}

	public ClusNode readTree() throws IOException {
		String head = getTillEOL();
		if (m_Parent.getDebug()) System.out.println("Head: "+head);
		ClusNode node = doReadTree(head);
		return node;
	}

	public ClusNode doReadTree(String name) throws IOException {
		ClusNode tree = new ClusNode();
		boolean isleaf = cleanString(getTillEOL(), tree);
		if (!isleaf) {
			skipEntry("yes");
			ClusNode left = doReadTree("yes");
			tree.addChild(left);
			skipEntry("no");
			ClusNode right = doReadTree("no");
			tree.addChild(right);
		}
		return tree;
	}

	public void readLeaf(MStreamTokenizer tokens, ClusNode node) throws IOException {
		int mode = m_Parent.getMode();
		if (mode == TildeOutReader.MULTI_CLASSIFY) {
			tokens.readToken();
			tokens.readChar(']');
			tokens.readToken();
			tokens.readChar('[');
			tokens.readChar('[');
		}
		ClusStatManager mgr = m_Parent.getStatMgr();
		ClassificationStat stat = (ClassificationStat)mgr.createClusteringStat();
		if (mode == TildeOutReader.REGRESSION) {
			int nbdim = m_Parent.getDim();
			double[] propvec = new double[nbdim];
			for (int i = 0; i < nbdim; i++) {
				if (i != 0) tokens.readChar(',');
				propvec[i] = (double)tokens.readFloat();
				System.out.println("Found = "+propvec[i]);
			}
			tokens.readChar(']');
			double count = (double)tokens.readFloat();
			for (int i = 0; i < nbdim; i++) {
				stat.setCount(i, 0, count*propvec[i]);
				stat.setCount(i, 1, count*(1.0-propvec[i]));
			}
			stat.m_SumWeight = count;
		} else {
			int nb = 1; /*mgr.getTargetSchema().getNbNomValues(0);*/
			double weight = 0.0;
			for (int i = 0; i < nb; i++) {
				if (i > 0) tokens.readChar(',');
				tokens.readToken();
				tokens.readChar(':');
				double nbclass = (double)tokens.readFloat();
				stat.setCount(0, i, nbclass);
				weight += nbclass;
			}
			stat.m_SumWeight = weight;
		}
		stat.calcMean();
		node.setClusteringStat(stat);
	}

	public boolean cleanString(String str, ClusNode node) throws IOException {
		String res = str;
		int qps = res.indexOf('?');
		boolean isleaf = true;
		if (qps != -1) {
			isleaf = false;
			res = res.substring(0,qps) + "?";
		}
		int count = 0;
		int nbpar = 0;
		String token = "";
		boolean ena = true;
		boolean crena = true;
		FakeTest test = new FakeTest();
		test.setLine(res);
		StringBuffer result = new StringBuffer();
		MStreamTokenizer tokens = MStreamTokenizer.createStringParser(res);
		tokens.setCharTokens(",[]():");
		while (token != null) {
			token = tokens.getToken();
			if (token != null) {
				if (!isleaf) {
					if (token.equals("(")) nbpar++;
					if (token.equals(")")) nbpar--;
					if (nbpar == 0 && token.equals(",")) {
						crena = false;
						if (ena) {
							result.append(',');
							test.addLine(result.toString());
							result.setLength(0);
						} else {
							ena = true;
						}
					}
				} else if (token.equals("[")) {
					readLeaf(tokens, node);
					break;
				}
				if (ena && crena) result.append(token);
				crena = true;
				count++;
			}
		}
		if (!isleaf) {
			if (m_Parent.getDebug()) System.out.println("Test: "+res);
			if (result.length() > 0) test.addLine(result.toString());
			node.setTest(test);
		} else {
			if (m_Parent.getDebug()) System.out.println("Leaf: "+node.getClusteringStat());

		}
		return isleaf;
	}

	public void skipEntry(String which) throws IOException {
		MStreamTokenizer tokens = m_Parent.getStream();
		while (tokens.isNextTokenIn("|+-") != null);
		String token = tokens.readToken();
		if (!token.equals(which)) {
			System.out.println("Error token: "+token+" must be "+which);
		}
		tokens.readChar(':');
	}

	public boolean isLeaf(String node) {
		return node.indexOf('?') == -1;
	}

	public String getTillEOL() throws IOException {
		MStreamTokenizer tokens = m_Parent.getStream();
		String str = null;
		while (str == null) {
			str = tokens.readTillEol().trim();
			if (str.equals("")) str = null;
		}
		return str;
	}
}
