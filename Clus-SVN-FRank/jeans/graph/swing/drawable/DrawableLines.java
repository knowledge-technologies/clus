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

public class DrawableLines extends Drawable {

	protected String[] m_hLines;
	protected Color m_Background;
	protected Color m_Border;
	protected Color m_Text = Color.black;
	protected Font m_Font;

	public DrawableLines(String[] lines) {
		m_hLines = lines;
	}

	public DrawableLines(String line) {
		m_hLines = new String[1];
		m_hLines[0] = line;
	}

	public void setFont(Font font) {
		m_Font = font;
	}

	public void setBackground(Color color) {
		m_Background = color;
	}

	public void setBorder(Color color) {
		m_Border = color;
	}

	public void calcSize(Graphics2D g, FontMetrics fm, DrawableCanvas canvas) {
		wd = 0;
		FontMetrics fm2 = fm;
		if (m_Font != null) {
			g.setFont(m_Font);
			fm2 = g.getFontMetrics();
		}
		hi = m_hLines.length*fm2.getHeight() + 6;
		for (int i = 0; i < m_hLines.length; i++)
			wd = Math.max(wd, fm2.stringWidth(m_hLines[i])+6);
	}

	public void draw(Graphics2D g, DrawableCanvas canvas, int xofs, int yofs) {
		if (m_Font != null) g.setFont(m_Font);
		FontMetrics fm = g.getFontMetrics();
		int ypos = yp-yofs-1;
		int xpos = xp+3-xofs;
		if (m_Background != null) {
			g.setColor(m_Background);
			g.fillRect(xpos, ypos, wd, hi);
		}
		if (m_Border != null) {
			g.setColor(m_Border);
			g.drawRect(xpos, ypos, wd, hi);
		}
		g.setColor(m_Text);
		ypos += fm.getAscent() + 3;
		for (int i = 0; i < m_hLines.length; i++) {
			g.drawString(m_hLines[i], xpos, ypos);
			ypos += fm.getHeight();
		}
	}
}
