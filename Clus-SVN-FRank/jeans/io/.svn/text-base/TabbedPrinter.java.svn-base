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

package jeans.io;

import java.io.*;

import jeans.util.StringUtils;
import jeans.util.IntegerStack;

public class TabbedPrinter {

	protected PrintWriter m_writer;
	protected IntegerStack m_tabs = new IntegerStack();
	protected int m_linePos, m_lineSize;

	protected TabbedPrinter(PrintWriter writer) {
		m_writer = writer;
		m_lineSize = 100;
		m_linePos = 0;
	}

	public static TabbedPrinter getSystemOutPrinter() {
		return new TabbedPrinter(new PrintWriter(System.out));
	}

	public void clearTabs() {
		m_tabs.clear();
	}

	public void addTab(int pos) {
		m_tabs.push(pos);
	}

	public void addTabs(int[] poss) {
		for (int i = 0; i < poss.length; i++)
			addTab(poss[i]);
	}

	public void setLineSize(int lineSize) {
		m_lineSize = lineSize;
	}

	public int getNextTab(int pos) {
		int idx = 0;
		while (idx < m_tabs.getSize()) {
			int tabpos = m_tabs.getElementAt(idx++);
			if (pos <= tabpos) return tabpos;
		}
		return -1;
	}

	public void print(String strg) {
		while (strg != null)
			strg = printSub(strg);
	}

	public void println(String strg) {
		print(strg+'\n');
	}

	public void newLine() {
		m_writer.println();
		m_writer.flush();
		m_linePos = 0;
	}

	public void forceNewLine() {
		if (m_linePos != 0) newLine();
	}

	public void printTab(String strg) {
		int tab = getNextTab(m_linePos+1);
		int len = strg.length();
		int max = tab != -1 ? Math.max(tab-m_linePos-1, 0) :
		                      Math.max(m_lineSize-m_linePos-1, 0);
		if (max < len) print(strg.substring(0,max)+"\t");
		else print(strg+"\t");
	}

	private String printSub(String strg) {
		int idx = strg.indexOf('\t');
		int len = strg.length();
		if (idx != -1) {
			m_writer.print(strg.substring(0,idx));
			m_linePos += idx;
			int nxtSpc = getNextTab(m_linePos);
			if (nxtSpc != -1) {
				int nbSpc = nxtSpc-m_linePos;
				m_linePos = nxtSpc;
				m_writer.print(StringUtils.makeString(' ', nbSpc));
			} else {
				newLine();
			}
			if (idx+1 >= len) return null;
			else return strg.substring(idx+1);
		} else {
			idx = strg.indexOf('\n');
			if (idx != -1) {
				m_writer.println(strg.substring(0,idx));
				m_writer.flush();
				m_linePos = 0;
				if (idx+1 >= len) return null;
				else return strg.substring(idx+1);
			} else {
				m_writer.print(strg);
				m_linePos += len;
				return null;
			}
		}
	}

	public static void main(String[] args) {
		TabbedPrinter result = TabbedPrinter.getSystemOutPrinter();
		int[] tabs = {10,20,30};
		result.addTabs(tabs);
		result.println("1\t2\t3\t4");
		result.printTab("jakke");
		result.printTab("marsje");
		result.printTab("willy");
		result.printTab("bakker");
		result.printTab("kwak");
		result.printTab("dropje");
		result.printTab("snope");
		result.printTab("qdf");
		result.newLine();
	}

}
