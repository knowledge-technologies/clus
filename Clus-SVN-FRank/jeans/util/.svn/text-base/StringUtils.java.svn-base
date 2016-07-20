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

public class StringUtils {

	public final static String DOUBLE_CHARS = "+-eE.";

	public static String printInt(int nb, int tabs) {
		String out = String.valueOf(nb);
		int len = out.length();
		return out+makeString(' ', tabs-len);
	}

	public static String trimSpacesAndTabs(String line) {
		int start = 0, end = line.length()-1;
		while (start <= end && (line.charAt(start) == ' ' || line.charAt(start) == '\t')) {
			start++;
		}
		while (end >= start && (line.charAt(end) == ' ' || line.charAt(end) == '\t')) {
			end--;
		}
		return line.substring(start, end+1);
	}

	public static String printStrMax(String out, int tabs) {
		int len = out.length();
		if (len > tabs) {
			return out.substring(0, tabs);
		} else {
			return out+makeString(' ', tabs-len);
		}
	}

	public static String printStr(String out, int tabs) {
		int len = out.length();
		return out+makeString(' ', tabs-len);
	}

	public static void printTabs(PrintWriter out, int tabs) {
		for (int i = 0; i < tabs; i++) {
			out.print("\t");
		}
	}

	public static String replaceChars(String strg, char ch1, char ch2) {
		StringBuffer res = new StringBuffer();
		for (int ctr = 0; ctr < strg.length(); ctr++) {
			char ch = strg.charAt(ctr);
			if (ch == ch1) ch = ch2;
			res.append(ch);
		}
		return res.toString();
	}

	public static String removeChar(String strg, char ch1) {
		StringBuffer res = new StringBuffer();
		for (int ctr = 0; ctr < strg.length(); ctr++) {
			char ch = strg.charAt(ctr);
			if (ch != ch1) res.append(ch);
		}
		return res.toString();
	}

	public static String makeString(char ch, int cnt) {
		StringBuffer res = new StringBuffer();
		for (int ctr = 0; ctr < cnt; ctr++) res.append(ch);
		return res.toString();
	}

	public static boolean isInteger(String str) {
		for (int ctr = 0; ctr < str.length(); ctr++) {
			char ch = str.charAt(ctr);
			if ((ch < '0' || ch > '9') && ch != '+' && ch != '-') return false;
		}
		return true;
	}

	public static boolean isDouble(String str) {
		for (int ctr = 0; ctr < str.length(); ctr++) {
			char ch = str.charAt(ctr);
			if ((ch < '0' || ch > '9') && DOUBLE_CHARS.indexOf(ch) == -1) return false;
		}
		return true;
	}

	public static boolean unCaseCompare(String a, String b) {
		int l1 = a.length();
		int l2 = b.length();
		if (l1 != l2) return false;
		for (int i = 0; i < l1; i++)
			if (Character.toUpperCase(a.charAt(i)) != Character.toUpperCase(b.charAt(i)))
				 return false;
		return true;
	}

	public static int getBoolean(String str) {
		String upper = str.toUpperCase();
		if (upper.equals("TRUE")) return 1;
		if (upper.equals("FALSE")) return 0;
		if (upper.equals("ON")) return 1;
		if (upper.equals("OFF")) return 0;
		if (upper.equals("YES")) return 1;
		if (upper.equals("NO")) return 0;
		return -1;
	}

	public static String roundDouble(double d, int place) {
		if (place <= 0) return String.valueOf((int)(d+((d > 0)? 0.5 : -0.5)));
		String s = "";
		if (d < 0) {
			s += "-";
			d = -d;
		}
		d += 0.5*Math.pow(10,-place);
		if (d > 1) {
			int i = (int)d;
			s += i;
			d -= i;
		} else {
			s += "0";
		}
		if (d > 0) {
			d += 1.0;
			String f = ""+(int)(d*Math.pow(10,place));
			s += "."+f.substring(1);
		}
		return s;
	}

	public static int occurs(char ch, String str) {
		int nb = 0;
		int len = str.length();
		for (int i = 0; i < len; i++)
			if (str.charAt(i) == ch) nb++;
		return nb;
	}

	public static String removeSuffix(String str, String suffix) {
		if (str.endsWith(suffix)) {
			return str.substring(0, str.length()-suffix.length());
		} else {
			return str;
		}
	}

	public static String removeSingleQuote(String str) {
		int len = str.length();
		if (len >= 2 && str.charAt(0) == '\'' && str.charAt(len-1) == '\'') {
			return str.substring(1, len-1);
		} else {
			return str;
		}
	}
}
