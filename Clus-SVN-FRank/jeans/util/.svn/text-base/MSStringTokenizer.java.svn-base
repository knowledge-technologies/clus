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

import java.util.Enumeration;


public class MSStringTokenizer implements Enumeration {

	public final static int NUMBER   = 0;
	public final static int ALPHA    = 1;
	public final static int SPECIAL  = 2;
	public final static int SPACE    = 3;
	public final static int CONTROL  = 4;
	public final static int FLOAT    = 5;
	public final static int GROUP    = 6;

	private String strg;
	private int pos, prevpos, len, typegroup;

	public MSStringTokenizer(String strg) {
		this.strg = strg;
		this.len = strg.length();
		skipControlsAndSpaces();
	}

	public int getGroup(char ch) {
		if (Character.isDigit(ch)) return NUMBER;
		if (Character.isLetter(ch)) return ALPHA;
		if (ch == '_') return ALPHA;
		if (ch == '(' || ch == ')') return GROUP;
		if (Character.isSpaceChar(ch)) return SPACE;
		if (Character.isISOControl(ch)) return CONTROL;
		return SPECIAL;
	}

	public String nextToken() {
		StringBuffer buffer = new StringBuffer();
		prevpos = pos;
		char ch = strg.charAt(pos);
		int group = getGroup(ch);
		int prevgroup = typegroup = group;
		while (group == prevgroup && pos < len) {
			buffer.append(ch); pos++;
			if (pos < len) {
				ch = strg.charAt(pos);
				prevgroup = group;
				group = getGroup(ch);
			}
		}
		skipControlsAndSpaces();
		return buffer.toString();
	}

	public String nextGroupToken() throws GeneralException {
		StringBuffer buffer = new StringBuffer();
		prevpos = pos;
		char ch = strg.charAt(pos);
		int group = getGroup(ch);
		int level = 0;
		int prevgroup = typegroup = group;
		while ((level > 0 || group == prevgroup) && pos < len) {
			if (ch == ')') level--;
			if (level > 0 || group != GROUP) buffer.append(ch);
			if (ch == '(') level++;
			if ((++pos) < len) {
				ch = strg.charAt(pos);
				prevgroup = group;
				group = getGroup(ch);
			}
		}
		if (level > 0)
			throw new GeneralException("'(,)' don't match.");
		skipControlsAndSpaces();
		return buffer.toString();
	}

	public String nextNumberToken() {
		StringBuffer buffer = new StringBuffer();
		boolean done = false;
		char prevChar = 0;
		prevpos = pos;
		typegroup = NUMBER;
		do {
			char ch = Character.toUpperCase(strg.charAt(pos));
			done = true;
			if (Character.isDigit(ch)) done = false;
			else if (ch == 'E' || ch == '.') {
				done = false;
				typegroup = FLOAT;
			} else if ((ch == '+' || ch == '-') && (prevChar == 0 || prevChar == 'E')) done = false;
			if (!done) {
				buffer.append(ch);
				pos++;
				if (pos >= len) done = true;
				if (!done) {
					prevChar = ch;
					ch = Character.toUpperCase(strg.charAt(pos));
				}
			}
		} while (!done);
		skipControlsAndSpaces();
		return buffer.toString();
	}

	public int getPosition() {
		return prevpos;
	}

	public boolean hasMoreTokens() {
		return hasMoreElements();
	}

	public int getGroup() {
		return typegroup;
	}

	public void pushBack() {
		pos = prevpos;
	}

	public boolean hasMoreElements() {
		return pos < len;
	}

	public Object nextElement() {
		return nextToken();
	}

	private void skipControlsAndSpaces() {
		if (pos < len) {
			char ch = strg.charAt(pos);
			int group = getGroup(ch);
			while ((group == SPACE || group == CONTROL) && pos < len) {
				pos++;
				ch = strg.charAt(pos);
				group = getGroup(ch);
			}
		}
	}

}
