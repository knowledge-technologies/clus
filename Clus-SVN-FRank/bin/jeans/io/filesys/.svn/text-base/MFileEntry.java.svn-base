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

package jeans.io.filesys;

import java.text.*;
import java.util.*;

public class MFileEntry {

	public final static int T_ICON = 0;
	public final static int T_NAME = 1;
	public final static int T_DATE = 2;
	public final static int T_TIME = 3;
	public final static int T_SIZE = 4;

	public final static String m_hSizeString[] = {"kB", "MB", "GB"};

	protected String m_sName;
	protected long m_iLength;
	protected Date m_hDate;

	public MFileEntry(String name, long len, long modified) {
		m_sName = name;
		m_iLength = len;
		m_hDate = new Date(modified);
	}

	public String getName() {
		return m_sName;
	}

	public long getLength() {
		return m_iLength;
	}

	public Date getDate() {
		return m_hDate;
	}

	public String getExtension() {
		int idx = m_sName.lastIndexOf('.');
		if (idx == -1 || idx >= m_sName.length()-1) return "";
		else return m_sName.substring(idx+1);
	}

	public String getTimeString() {
		return DateFormat.getTimeInstance(DateFormat.SHORT).format(m_hDate);
	}

	public String getDateString() {
		return DateFormat.getDateInstance(DateFormat.SHORT).format(m_hDate);
	}

	public String getLengthString() {
		int idx = 0;
		long len = m_iLength;
		while (idx < 3 && len >= 1024) {
			idx++;
			len /= 1024;
		}
		if (idx == 0) return String.valueOf(len);
		else return String.valueOf(len)+" "+m_hSizeString[idx-1];
	}

	protected int compareString(String a, String b) {
		return a.compareTo(b);
	}

	protected int compareLong(long a, long b) {
		if (a > b) return -1;
		if (a == b) return 0;
		return 1;
	}

	protected int compareDate(Date a, Date b) {
		if (a.equals(b)) return 0;
		if (a.after(b)) return -1;
		return 1;
	}

	protected int compareExtention(MFileEntry a, MFileEntry b) {
		int res = compareString(a.getExtension(), b.getExtension());
		if (res == 0) res = compareString(a.getName(), b.getName());
		return res;
	}

	public int compareTo(MFileEntry ent, int type) {
		switch (type) {
			case T_ICON:
				return compareExtention(this, ent);
			case T_NAME:
				return compareString(m_sName, ent.getName());
			case T_TIME:
			case T_DATE:
				return compareDate(m_hDate, ent.getDate());
			case T_SIZE:
				return compareLong(m_iLength, ent.getLength());
		}
		return 0;
	}
}
