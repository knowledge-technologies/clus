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

package jeans.graph.swing.drawable;

import java.awt.*;
import java.awt.event.*;

public class DrawableExpandButton extends Drawable {

	protected boolean m_bState = true;
	protected ActionListener m_hListener;
	protected Color m_Background = Color.white;

	public DrawableExpandButton(int wd, int hi, boolean state) {
		this.wd = wd;
		this.hi = hi;
		m_bState = state;
	}

	public void setBackground(Color color) {
		m_Background = color;
	}

	public void draw(Graphics2D g, DrawableCanvas canvas, int xofs, int yofs) {
		g.setStroke(DrawableCanvas.SINGLE_STROKE);
		g.setColor(m_Background);
		g.fillRect(xp-xofs,yp-yofs,wd,hi);
		g.setColor(Color.black);
		g.drawRect(xp-xofs,yp-yofs,wd,hi);
		int ymid = yp-yofs+hi/2;
		g.drawLine(xp-xofs+2, ymid, xp-xofs+wd-2, ymid);
		if (m_bState) {
			int xmid = xp-xofs+wd/2;
			g.drawLine(xmid, yp-yofs+2, xmid, yp-yofs+hi-2);
		}
	}

	public boolean getState() {
		return m_bState;
	}

	public void setState(boolean state) {
		m_bState = state;
	}

	public void setActionListener(ActionListener listener) {
		m_hListener = listener;
	}

	public boolean mousePressed(DrawableCanvas canvas, int x, int y, MouseEvent evt) {
		m_bState = !m_bState;
		if (m_hListener != null) m_hListener.actionPerformed(new ActionEvent(this,0,""));
		return true;
	}

	public boolean mouseSensitive() {
		return true;
	}
}
