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

public class MStreamTokenizer {

	protected Reader reader;
	protected int lineno;
	protected String pushbacktoken;
	protected int pushbackchar;
	protected String chartokens;
	protected int commentchar;
	protected boolean septype;
	protected long position;
	protected String filename;

	public MStreamTokenizer() {
	}

	public MStreamTokenizer(String fname) throws FileNotFoundException {
		this(new FileInputStream(fname));
		filename = fname;
	}

	public MStreamTokenizer(InputStream stream) {
		this(new InputStreamReader(stream));
	}

	public MStreamTokenizer(Reader myreader) {
		reader = new BufferedReader(myreader);
		init();
	}

	public String getFileName() {
		return filename;
	}

	public String getFileNameForErrorMsg() {
		if (filename == null) return "";
		else return " while reading '"+filename+"'";
	}

	public static MStreamTokenizer createStringParser(String strg) {
		MStreamTokenizer tokens = new MStreamTokenizer();
		tokens.reader = new StringReader(strg);
		tokens.init();
		return tokens;
	}

	private final void init() {
		lineno = 1;
		pushbacktoken = null;
		septype = true;
		pushbackchar = -1;
		chartokens = ":;,[]{}";
		commentchar = '#';
	}

	public final void setSeparatorType(boolean dontUseSpace) {
		septype = dontUseSpace;
	}

	public final boolean isSeparator(int ch) {
		if (septype) return ch == ' ' || ch == 10 || ch == -1 || ch == '\t';
		else return ch == 10 || ch == -1;
	}

	public final boolean isRealSeparator(int ch) {
		if (septype) return ch == ' ' || ch == 10 || ch == '\t';
		else return ch == 10;
	}

	public final String getToken() throws IOException {
		if (pushbacktoken == null) {
			int ch = readSignificantChar();
			if (ch != -1) {
				StringBuffer strg = new StringBuffer();
				boolean done = false;
				do {
					strg.append((char)ch);
					if (chartokens.indexOf((char)ch) == -1) {
						ch = readChar();
						if (isSeparator(ch)) {
							pushBackChar(ch);
							done = true;
						}
						if (chartokens.indexOf((char)ch) != -1) {
							pushBackChar(ch);
							done = true;
						}
					} else {
						done = true;
					}
				} while (!done);
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

	public final String getDelimToken(char opendelim, char closedelim) throws IOException {
		int ch = readSignificantChar();
		if (ch == opendelim) {
			int depth = 0;
			StringBuffer strg = new StringBuffer();
			ch = readCharNoPushback();
			while (ch != closedelim || depth > 0) {
				if (ch == opendelim) depth++;
				if (ch != closedelim || depth > 0) strg.append((char)ch);
				if (ch == closedelim) depth--;
				ch = readCharNoPushback();
			}
			return strg.toString();
		} else {
			int ch2 = readChar();
			if (isRealSeparator(ch2)) return String.valueOf((char)ch);
			else {
				pushBackChar(ch2);
				return String.valueOf((char)ch) + getToken();
			}
		}
	}

	public final int getCharToken() throws IOException {
		if (pushbacktoken == null) {
			return readSignificantChar();
		} else {
			String betw = pushbacktoken;
			int len = betw.length();
			if (len >= 2) {
				pushbacktoken = betw.substring(1);
				return betw.charAt(0);
			} else {
				pushbacktoken = null;
				return betw.charAt(0);
			}
		}
	}

	public final long getPosition() {
		return position;
	}

	public final void gotoPosition(long pos) throws IOException {
		reader.skip(pos - position);
	}

	public final String readToken() throws IOException {
		int saveline = getLine();
		String token = getToken();
		if (token == null)
			throw new IOException("Unexpected end of file at line: "+saveline);
		return token;
	}

	public final boolean hasMoreTokens() throws IOException {
		String token = getToken();
		if (token == null) {
			return false;
		} else {
			pushBackToken(token);
			return true;
		}

	}

	public final int readSignificantChar() throws IOException {
		int ch;
		do {
			ch = readChar();
			if (ch == commentchar) do {
				ch = readCharNoPushback();
			} while (ch != -1 && ch != 10);
		} while (isRealSeparator(ch));
		return ch;
	}

	public final int readChar() throws IOException {
		if (pushbackchar == -1) {
			int ch = reader.read();
			position++;
			if (ch == 13) {
				ch = reader.read();
				position++;
			}
			if (ch == 10) lineno++;
			return ch;
		} else {
			int betw = pushbackchar;
			pushbackchar = -1;
			return betw;
		}
	}

	public final int readCharNoPushback() throws IOException {
		int ch = reader.read();
		position++;
		if (ch == 13) {
			ch = reader.read();
			position++;
		}
		if (ch == 10) lineno++;
		return ch;
	}

	public final void skipTillLine(int line) throws IOException {
		while (lineno < line) {
			readChar();
		}
	}

	public final boolean skipTillLine(String line) throws IOException {
		int len = line.length();
		int ch = readChar();
		int first = line.charAt(0);
		while (true) {
			// try to read whole line
			if (ch == first && len >= 2) {
				int idx = 1;
				ch = readCharNoPushback();
				while (idx < len-1 && ch == line.charAt(idx)) {
					ch = readCharNoPushback();
					idx++;
				}
				if (idx == len-1) return true;
			}
			if (ch == -1) return false;
			// skip till cr
			while (ch != 10 && ch != -1)
				ch = readCharNoPushback();
			if (ch == -1) return false;
			ch = readCharNoPushback();
		}
	}

	public final String readTillEol() throws IOException {
		int ch;
		StringBuffer strg = new StringBuffer();
		if (pushbacktoken == null) {
			ch = readSignificantChar();
			if (ch == -1) return null;
		} else {
			strg.append(pushbacktoken);
			pushbacktoken = null;
			ch = readChar();
		}
		while (ch != 10 && ch != -1) {
			strg.append((char)ch);
			ch = readCharNoPushback();
		}
		pushBackChar(ch);
		return strg.toString();
	}

	public final int readInteger() throws IOException {
		int saveline = getLine();
		String token = readToken();
		try {
			return Integer.parseInt(token);
		} catch (NumberFormatException e) {
			throw new IOException("Integer value expected at line: "+saveline);
		}
	}

	public final float readFloat() throws IOException {
		int saveline = getLine();
		String token = readToken();
		try {
			Float fl = new Float(token);
			return fl.floatValue();
		} catch (NumberFormatException e) {
			throw new IOException("Float value expected at line: "+saveline);
		}
	}

	public final String isNextTokenIn(String strg) throws IOException {
		String token = getToken();
		if (token == null || strg.indexOf(token) != -1) {
			return token;
		} else {
			pushBackToken(token);
			return null;
		}
	}

	public final boolean isNextToken(String strg) throws IOException {
		boolean yes = false;
		String token = getToken();
		if (token == null) return false;
		if (token.equals(strg)) yes = true;
		else pushBackToken(token);
		return yes;
	}

	public final boolean isNextToken(char ch) throws IOException {
		return isNextToken(String.valueOf(ch));
	}

	public final int readChar(String which) throws IOException {
		int character = getCharToken();
		if (character == -1)
			throw new IOException("Unexpected end of file reading character at line: "+getLine());
		int idx = which.indexOf(character);
		if (idx < 0)
			throw new IOException("Character '"+which+"' expected at line: "+getLine()+" (found '"+(char)character+"')");
		return idx;
	}

	public final void readChar(char ch) throws IOException {
		int character = getCharToken();
		if (character == -1)
			throw new IOException("Unexpected end of file reading character at line: "+getLine());
		if (character != ch)
			throw new IOException("Character '"+ch+"' expected at line: "+getLine()+" (found '"+(char)character+"')");
	}

	public final void pushBackChar(int ch) {
		pushbackchar = ch;
	}

	public final void pushBackToken(String token) {
		pushbacktoken = token;
	}

	public final String setCharTokens(String chartokens) {
		String result = this.chartokens;
		this.chartokens = chartokens;
		return result;
	}

	public final int setCommentChar(int commentchar) {
		int result = this.commentchar;
		this.commentchar = commentchar;
		return result;
	}

	public final String getCharTokens() {
		return chartokens;
	}

	public final int setCommentChar() {
		return commentchar;
	}

	public final int getLine() {
		return lineno;
	}

	public final Reader getReader() {
		return reader;
	}

	public final void close() throws IOException {
		reader.close();
	}

	public String toString() {
		return "MStreamTokenizer ["+commentchar+"] ["+chartokens+"]";
	}

}
