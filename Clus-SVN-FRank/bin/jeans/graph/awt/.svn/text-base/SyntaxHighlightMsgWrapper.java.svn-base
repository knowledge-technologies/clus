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

import jeans.graph.SyntaxHighlighter;

public class SyntaxHighlightMsgWrapper extends MessageWrapper {

	protected SyntaxHighlighter m_lighter;

	public SyntaxHighlightMsgWrapper(String msg, SyntaxHighlighter lighter) {
		super(msg);
		m_lighter = lighter;
	}

	public void drawWrapped(Graphics g, int xpos, int ypos) {
		int nb = getNbLines();
		if (nb != 0) {
			boolean done = false;
			int txtHi = getHeight()/nb;
			int xp = xpos;
			int crline = 0;
			int crpos = 0;
			m_lighter.parseString(m_msg);
			FontMetrics fm = g.getFontMetrics();
			while (!done) {
				String token = m_lighter.getColorToken();
				if (token == null) {
					done = true;
				} else {
					g.setColor(m_lighter.getColor());
					int len = token.length();
					while (len > 0 && crline < nb) {
						if (crpos < getLoBoundary(crline)) {
							int delta = getLoBoundary(crline) - crpos;
							token = token.substring(delta);
							len -= delta;
							crpos += delta;
						}
						if (crpos + len > getHiBoundary(crline)) {
							int delta = getHiBoundary(crline) - crpos;
							g.drawString(token.substring(0,delta), xp, ypos);
							token = token.substring(delta);
							ypos += txtHi;
							xp = xpos;
							crline++;
							len -= delta;
							crpos += delta;
						} else {
							g.drawString(token, xp, ypos);
							xp += fm.stringWidth(token);
							crpos += len;
							len = 0;
						}
					}
				}
			}
		}
	}

}
