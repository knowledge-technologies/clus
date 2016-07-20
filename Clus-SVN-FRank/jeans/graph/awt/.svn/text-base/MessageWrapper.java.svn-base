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

package jeans.graph.awt;

import java.awt.*;

public class MessageWrapper {

	protected final static int GROW = 2;

	protected String m_msg;
	protected int m_hi, m_wd;
	protected int[] m_wrap;
	protected int m_nb;

	public MessageWrapper(String msg) {
		m_msg = msg;
		m_wrap = new int[GROW];
	}

	public void doWrap(FontMetrics fm, Graphics g, int wmax) {
		int size = m_msg.length();
		boolean done = false;
		int pos = 0;
		int argpos = 0;
		m_hi = 0;
		m_wd = wmax;
		removeAllElements();
		addInteger(0);
		while (!done) {
			int prevpos = pos;
			while (pos < size && m_msg.charAt(pos) != ' ') pos++;
			if (pos >= size) done = true;
			String strg = m_msg.substring(argpos, pos);
			if (fm.stringWidth(strg) > wmax) {
				if (argpos == prevpos) {
					addInteger(pos);
					while (pos < size && m_msg.charAt(pos) == ' ') pos++;
					addInteger(pos);
					argpos = pos;
				} else {
					addInteger(prevpos);
					while (prevpos < size && m_msg.charAt(prevpos) == ' ') prevpos++;
					addInteger(prevpos);
					pos = argpos = prevpos;
				}
				m_hi += fm.getHeight()+1;
			} else {
				pos++;
			}
		}
		if (argpos < size) {
			addInteger(size+1);
			m_hi += fm.getHeight()+1;
		}
		shrink();
	}

	public int getLoBoundary(int ctr) {
		return m_wrap[ctr*2];
	}

	public int getHiBoundary(int ctr) {
		return m_wrap[ctr*2+1]-1;
	}

	public int getNbLines() {
		return m_nb/2;
	}

	public String getString() {
		return m_msg;
	}

	public String getLine(int ctr) {
		return m_msg.substring(getLoBoundary(ctr), getHiBoundary(ctr));
	}

	public void mayBeWrap(FontMetrics fm, Graphics g, int wmax) {
		if (m_wd != wmax) doWrap(fm, g, wmax);
	}

	public void drawWrapped(Graphics g, int xpos, int ypos) {
		int nb = getNbLines();
		if (nb != 0) {
			int txtHi = getHeight()/nb;
			for (int ctr = 0; ctr < nb; ctr++) {
				String strg = getLine(ctr);
				g.drawString(strg, xpos, ypos);
				ypos += txtHi;
			}
		}
	}

	public int getHeight() {
		return m_hi;
	}

	protected void grow() {
		int size = m_wrap.length;
		if (m_nb+1 > size) {
			int[] newWrap = new int[size+GROW];
			for (int ctr = 0; ctr < size; ctr++)
			System.arraycopy(m_wrap, 0, newWrap, 0, size);
			m_wrap = newWrap;
		}
	}

	protected void shrink() {
		if (m_nb < m_wrap.length) {
			int[] newWrap = new int[m_nb];
			System.arraycopy(m_wrap, 0, newWrap, 0, m_nb);
			m_wrap = newWrap;
		}
	}

	protected void addInteger(int value) {
		grow();
		m_wrap[m_nb++] = value;
	}

	protected void removeAllElements() {
		m_nb = 0;
	}
}
