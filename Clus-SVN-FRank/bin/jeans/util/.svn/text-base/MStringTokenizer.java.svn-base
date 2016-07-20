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

package jeans.util;

import java.io.*;

public class MStringTokenizer {

	protected String pushbacktoken;
	protected int pushbackchar;
	protected String chartokens, edblock, stblock, spacechars;
	protected String strg;
	protected int pos;

	public MStringTokenizer() {
		this.chartokens = ":;,[]{} \t";
	}

	public void setString(String strg) {
		this.strg = strg;
		this.pos = 0;
		this.pushbacktoken = null;
		this.pushbackchar = -1;
	}

	public void setBlockChars(String stBlock, String edBlock) {
		this.stblock = stBlock;
		this.edblock = edBlock;
	}

	public void setSpaceChars(String space) {
		this.spacechars = space;
	}

	public String getToken() {
		if (pushbacktoken == null) {
			int ch = readSignificantChar();
			if (ch != -1) {
				StringBuffer strg = new StringBuffer();
				boolean done = false;
				if (stblock.indexOf((char)ch) != -1) {
					//we don't need stblock and edblock characters
					//strg.append((char)ch);
					while (!done) {
						ch = readChar();
						if (ch == -1) {
							done = true;
						} else {
							if (edblock.indexOf((char)ch) != -1){
								done = true;
							}else{
								strg.append((char)ch);
							}
						}
					}
				}
				while (!done) {
					strg.append((char)ch);
					if (chartokens.indexOf((char)ch) == -1) {
						ch = readChar();
						if (ch == -1 || spacechars.indexOf((char)ch) != -1) {
							done = true;
						} else if (chartokens.indexOf((char)ch) != -1) {
							pushBackChar(ch);
							done = true;
						}
					} else {
						done = true;
					}
				}
				return strg.toString();
			} else {
				return null;
			}
		} else {
			String betw = pushbacktoken;
			pushbacktoken = null;
			return betw;
		}
	}

	public String readToken() throws IOException {
		String token = getToken();
		if (token == null)
			throw new IOException("Unexpected end of file");
		return token;
	}

	public boolean hasMoreTokens() throws IOException {
		String token = getToken();
		if (token == null) {
			return false;
		} else {
			pushBackToken(token);
			return true;
		}

	}

	public int readSignificantChar() {
		int ch = readChar();
		while (spacechars.indexOf((char)ch) != -1) {
			ch = readChar();
		}
		return ch;
	}

	public int read() {
		if (pos == strg.length()) return -1;
		return strg.charAt(pos++);
	}

	public int readChar() {
		if (pushbackchar == -1) {
			int ch = read();
			return ch;
		} else {
			int betw = pushbackchar;
			pushbackchar = -1;
			return betw;
		}
	}

	public int readInteger() throws IOException {
		String token = readToken();
		try {
			return Integer.parseInt(token);
		} catch (NumberFormatException e) {
			throw new IOException("Integer value expected");
		}
	}

	public float readFloat() throws IOException {
		String token = readToken();
		try {
			Float fl = new Float(token);
			return fl.floatValue();
		} catch (NumberFormatException e) {
			throw new IOException("Float value expected");
		}
	}

	public String isNextTokenIn(String strg) throws IOException {
		String token = getToken();
		if (token == null || strg.indexOf(token) != -1) {
			return token;
		} else {
			pushBackToken(token);
			return null;
		}
	}

	public boolean isNextToken(String strg) throws IOException {
		boolean yes = false;
		String token = getToken();
		if (token == null) return false;
		if (token.equals(strg)) yes = true;
		else pushBackToken(token);
		return yes;
	}

	public int readChar(String which) throws IOException {
		String character = getToken();
		if (character == null)
			throw new IOException("Unexpected end of file reading character");
		int idx = which.indexOf(character.charAt(0));
		if (idx < 0 || character.length() != 1)
			throw new IOException("Character '"+which+"' expected");
		return idx;
	}

	public void readChar(char ch) throws IOException {
		String character = getToken();
		if (character == null)
			throw new IOException("Unexpected end of file reading character");
		if (!character.equals(String.valueOf(ch)))
			throw new IOException("Character '"+ch+"' expected at line");
	}

	public void pushBackChar(int ch) {
		pushbackchar = ch;
	}

	public void pushBackToken(String token) {
		pushbacktoken = token;
	}

	public String setCharTokens(String chartokens) {
		String result = this.chartokens;
		this.chartokens = chartokens;
		return result;
	}

	public String getCharTokens() {
		return chartokens;
	}
}
