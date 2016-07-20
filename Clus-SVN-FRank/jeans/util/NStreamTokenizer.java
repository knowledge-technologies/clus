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

public class NStreamTokenizer {

	public final static int BUFFER_SINGLE = 512;
	public final static int BUFFER_SIZE = BUFFER_SINGLE*2;

	public final static char CHAR_LF = 10;
	public final static char CHAR_CR = 13;
	public final static char CHAR_EOF = 0;

	public final static int TYPE_DELIM = 0;
	public final static int TYPE_SKIP = 1;
	public final static int TYPE_TOKEN = 2;

	protected Reader m_reader;
	protected int m_lineNo, m_prevLineNo;
	protected int m_crPos, m_prevCrPos;
	protected char[] m_doubleBuffer;

	protected int m_bufOfs, m_bufStart;
	protected int m_posModulo, m_markPos;
	protected CallBackFunction m_posCallback;

	public NStreamTokenizer(Reader reader) {
		m_reader = reader;
		m_lineNo = m_prevLineNo = 1;
		m_crPos = m_prevCrPos = 0;
		m_bufOfs = -BUFFER_SIZE;
		m_posCallback = null;
		m_bufStart = -1;
		m_doubleBuffer = new char[BUFFER_SIZE];
	}

	public String getToken() throws IOException {
		return null;
	}

	public String readToken() throws IOException {
		String token = getToken();
		if (token == null)
			throw new IOException("Unexpected end of file at line: "+getPrevLine());
		return token;
	}

	public boolean hasMoreTokens() throws IOException {
		String token = getToken();
		if (token == null) {
			return false;
		} else {
			pushBackToken();
			return true;
		}

	}

	public void doRead(int start, int len) throws IOException {
		int nbRead = m_reader.read(m_doubleBuffer, start, len);
		if (nbRead == -1) nbRead = 0;
		int till = start+len;
		for (int ctr = start+nbRead; ctr < till; ctr++)	m_doubleBuffer[ctr] = CHAR_EOF;
	}

	public char readChar() throws IOException {
		int bufPos = m_crPos-m_bufOfs;
		if (bufPos >= BUFFER_SIZE) {
			if (m_bufStart != -1) {
				if (m_bufStart == 0) {
					doRead(0, BUFFER_SINGLE);
					m_bufStart = BUFFER_SINGLE;
					m_bufOfs += BUFFER_SINGLE;
				} else {
					doRead(BUFFER_SINGLE, BUFFER_SINGLE);
					m_bufStart = 0;
					m_bufOfs += BUFFER_SINGLE;
				}
			} else {
				doRead(0, BUFFER_SIZE);
				m_bufStart = 0;
				m_bufOfs = 0;
			}
			bufPos = m_crPos-m_bufOfs;
		}
		bufPos = (bufPos + m_bufStart) % BUFFER_SIZE;
		char ch = m_doubleBuffer[bufPos];
		if (ch == CHAR_LF) m_lineNo++;
		m_crPos++;
		if (m_posCallback != null && m_crPos % m_posModulo == 0)
			m_posCallback.callBackFunction(this);
		return ch;
	}

	public String readTillEol() throws IOException {
		markPosition();
		char ch = readChar();
		while (ch != CHAR_LF && ch != CHAR_CR && ch != CHAR_EOF)
			ch = readChar();
		if (ch == CHAR_EOF) return null;
		pushBackChar();
		String res = makeString();
		if (readChar() == CHAR_CR) {
			if (readChar() != CHAR_LF) pushBackChar();
		}
		return res;
	}

	public void markPosition() {
		if (m_bufStart != -1)
			m_markPos = (m_crPos - m_bufOfs + m_bufStart) % BUFFER_SIZE;
		else
			m_markPos = 0;
	}

	public void pushBackChar() {
		m_crPos--;
		int bufPos = (m_crPos - m_bufOfs + m_bufStart) % BUFFER_SIZE;
		if (m_doubleBuffer[bufPos] == CHAR_LF) m_lineNo--;
	}

	public String makeString() {
		int bufPos = (m_crPos - m_bufOfs + m_bufStart) % BUFFER_SIZE;
		if (bufPos >= m_markPos) {
			return new String(m_doubleBuffer, m_markPos, bufPos-m_markPos);
		} else {
			return (new String(m_doubleBuffer, bufPos, BUFFER_SINGLE-bufPos-1)) +
			       (new String(m_doubleBuffer, 0, m_markPos));
		}
	}

	public String readTillDelim(char delim) throws IOException {
		markPosition();
		char ch = readChar();
		while (ch != delim && ch != CHAR_EOF)
			ch = readChar();
		if (ch == CHAR_EOF) return null;
		pushBackChar();
		String res = makeString();
		readChar();
		return res;
	}

	public int readInteger() throws IOException {
		try {
			return Integer.parseInt(readToken());
		} catch (NumberFormatException e) {
			throw new IOException("Integer value expected at line: "+getPrevLine());
		}
	}

	public float readFloat() throws IOException {
		try {
			Float fl = new Float(readToken());
			return fl.floatValue();
		} catch (NumberFormatException e) {
			throw new IOException("Float value expected at line: "+getPrevLine());
		}
	}

	public String isNextTokenIn(String strg) throws IOException {
		String token = getToken();
		if (token == null || strg.indexOf(token) != -1) {
			return token;
		} else {
			pushBackToken();
			return null;
		}
	}

	public boolean isNextToken(String strg) throws IOException {
		String token = getToken();
		if (token == null) return false;
		if (token.equals(strg)) {
			return true;
		} else {
			pushBackToken();
			return false;
		}
	}

	public boolean isNextChar(char ch) throws IOException {
		if (readChar() == ch) {
			return true;
		} else {
			pushBackChar();
			return false;
		}
	}

	public void pushBackToken() {
	}

	public int getLine() {
		return m_lineNo;
	}

	public int getPrevLine() {
		return m_prevLineNo;
	}

	public void close() throws IOException {
		m_reader.close();
	}

}
