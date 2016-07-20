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
import java.util.*;

import jeans.graph.BufferCanvas;

public class MessageCanvas extends BufferCanvas {

	public final static long serialVersionUID = 1;

	protected Vector messages = new Vector();
	protected int m_offs;
	protected int m_limit = 50;

	public MessageCanvas(int wd, int hi) {
		super(wd, hi);
	}

	public void addMessage(MessageWrapper wrapper) {
		messages.insertElementAt(wrapper, 0);
		int size = messages.size();
		for (int ctr = size-1; ctr >= m_limit; ctr--)
			messages.removeElementAt(ctr);
		redraw();
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

	public int paintMessage(MessageWrapper msg, Graphics g, FontMetrics fm, int yp, int wd) {
		int xp = 3;
		int mwd = wd-xp-5;
		msg.mayBeWrap(fm, g, mwd);
		int hi = msg.getHeight();
                g.setColor(Color.black);
		msg.drawWrapped(g, xp+3, yp-hi);
		return hi;
	}

	public void paintIt(Graphics g, Dimension d) {
		int wd = d.width;
		int idx = m_offs;
		g.setColor(SystemColor.control);
		g.fillRect(0,0,d.width,d.height);
		int nb = messages.size();
		FontMetrics fm = g.getFontMetrics();
		int yp = d.height + fm.getMaxAscent() - 3;
		while (yp > 0 && idx < nb) {
			MessageWrapper msg = (MessageWrapper)messages.elementAt(idx++);
			yp -= paintMessage(msg, g, fm, yp, wd);
		}
	}

}
