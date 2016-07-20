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

package jeans.graph.swing;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import jeans.graph.awt.MessageWrapper;

public class MessageComponent extends JComponent {

	public final static long serialVersionUID = 1;

	public final static int ALIGN_TOP = 0;
	public final static int ALIGN_BOTTOM = 1;

	protected Vector messages = new Vector();
	protected int m_offs;
	protected int m_limit = 50;
	protected int m_wd, m_hi;
	protected int m_align = ALIGN_TOP;

	public MessageComponent(int wd, int hi) {
		m_wd = wd;
		m_hi = hi;
	}

	public void setAlign(int align) {
		m_align = align;
	}

	public Dimension getPreferredSize() {
		FontMetrics fm = getGraphics().getFontMetrics();
		Insets insets = getInsets();
		int hi = insets.top + insets.bottom;
		int wd = insets.left + insets.right;
		return new Dimension(m_wd*fm.getMaxAdvance()+wd, m_hi*fm.getHeight()+hi);
	}

	public void addMessage(MessageWrapper wrapper) {
		messages.insertElementAt(wrapper, 0);
		int size = messages.size();
		for (int ctr = size-1; ctr >= m_limit; ctr--)
			messages.removeElementAt(ctr);
		repaint();
	}

	public void removeLast() {
		int size = messages.size();
		if (size > 0) messages.removeElementAt(0);
	}

	public void removeAll() {
		messages.removeAllElements();
		m_offs = 0;
	}

	public void setLimit(int limit) {
		m_limit = limit;
	}

	public int getNbMessages() {
		return messages.size();
	}

	public void setOffs(int offs) {
		m_offs = offs;
	}

	public MessageWrapper getMessage(int which) {
		return (MessageWrapper)messages.elementAt(which);
	}

	public int paintMessageBottom(MessageWrapper msg, Graphics g, FontMetrics fm, int xp, int yp, int wd) {
		int mwd = wd-xp-5;
		msg.mayBeWrap(fm, g, mwd);
		int hi = msg.getHeight();
                g.setColor(Color.black);
		msg.drawWrapped(g, xp+3, yp-hi);
		return hi;
	}

	public int paintMessageTop(MessageWrapper msg, Graphics g, FontMetrics fm, int xp, int yp, int wd) {
		int mwd = wd-xp-5;
		msg.mayBeWrap(fm, g, mwd);
		int hi = msg.getHeight();
                g.setColor(Color.black);
		msg.drawWrapped(g, xp+3, yp);
		return hi;
	}

	 public void paintComponent(Graphics g) {
                super.paintComponent(g);
		Insets insets = getInsets();
                int wd = getWidth() - insets.left - insets.right;
                int hi = getHeight() - insets.top - insets.bottom;
		int idx = m_offs;
		int nb = messages.size();
		FontMetrics fm = g.getFontMetrics();
		if (m_align == ALIGN_BOTTOM) {
			int yp = hi + fm.getMaxAscent() - 3 + insets.top;
			while (yp > 0 && idx < nb) {
				MessageWrapper msg = (MessageWrapper)messages.elementAt(idx++);
				yp -= paintMessageBottom(msg, g, fm, 3+insets.left, yp, wd);
			}
		} else {
			int yp = fm.getMaxAscent() + 3 + insets.top;
			while (yp > 0 && idx >= 0 && idx < nb) {
				MessageWrapper msg = (MessageWrapper)messages.elementAt(idx--);
				yp += paintMessageTop(msg, g, fm, 3+insets.left, yp, wd);
			}
		}
	}

}
