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

package jeans.graph;

import java.awt.*;
import java.util.*;

import jeans.util.MStringTokenizer;

public class SimpleHighlighter extends SyntaxHighlighter {

	protected String m_ops, m_token;
	protected MStringTokenizer m_tokens;
	protected Vector m_blockColors = new Vector();
	protected Vector m_funcColors = new Vector();
	protected Color m_color, m_newColor;

	protected Color m_ops_color = Color.blue;
	protected Color m_default_color = Color.black;

	public void setOperators(String ops, Color opcolor) {
		m_ops = ops;
		m_ops_color = opcolor;
	}

	public void addFunctions(String[] fcts, Color color) {
		FuncColor fc = new FuncColor(fcts, color);
		m_funcColors.addElement(fc);
	}

	public void initialize() {
		m_tokens = new MStringTokenizer();
		m_tokens.setCharTokens(m_ops+" \t");
		String stBlock = "";
		String edBlock = "";
		for (int ctr = 0; ctr < m_blockColors.size(); ctr++) {
			BlockColor bc = (BlockColor)m_blockColors.elementAt(ctr);
			stBlock += String.valueOf((char)bc.ch1);
			edBlock += String.valueOf((char)bc.ch2);
		}
		m_tokens.setBlockChars(stBlock, edBlock);
	}

	public void addBlockColor(int ch1, int ch2, Color color) {
		BlockColor bc = new BlockColor(ch1, ch2, color);
		m_blockColors.addElement(bc);
	}

	public Color getColor(String token) {
		if (token.length() > 0) {
			int ch = token.charAt(0);
			if (m_ops.indexOf(ch) != -1) return m_ops_color;
			for (int ctr = 0; ctr < m_blockColors.size(); ctr++) {
				BlockColor bc = (BlockColor)m_blockColors.elementAt(ctr);
				if (ch == bc.ch1) return bc.color;
			}
			for (int ctr = 0; ctr < m_funcColors.size(); ctr++) {
				FuncColor fc = (FuncColor)m_funcColors.elementAt(ctr);
				for (int n = 0; n < fc.func.length; n++) {
					if (fc.func[n].equals(token))
						return fc.color;
				}
			}
		}
		return m_default_color;
	}

	public Color getColor() {
		return m_color;
	}

	public void parseString(String strg) {
		m_tokens.setString(strg);
		m_newColor = m_default_color;
		m_token = "";
	}

	public String getColorToken() {
		m_color = m_newColor;
		while (true) {
			String token = m_tokens.getToken();
			if (token == null) {
				if (m_token.length() > 0) {
					String output = m_token;
					m_token = "";
					return output;
				} else {
					return null;
				}
			}
			if (token.equals(" ") || token.equals("\t")) {
				m_token += token;
			} else {
				Color newColor = getColor(token);
				if (m_color.equals(newColor)) {
					m_token += token;
				} else {
					m_newColor = newColor;
					if (m_token.length() > 0) {
						String output = m_token;
						m_token = token;
						return output;
					} else {
						m_token = token;
						m_color = newColor;
					}
				}
			}
		}
	}
}


class BlockColor {

	public int ch1, ch2;
	public Color color;

	public BlockColor(int ch1, int ch2, Color color) {
		this.ch1 = ch1;
		this.ch2 = ch2;
		this.color = color;
	}
}

class FuncColor {

	public String[] func;
	public Color color;

	public FuncColor(String[] func, Color color) {
		this.func = func;
		this.color = color;
	}
}
