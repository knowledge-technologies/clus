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

package jeans.graph;

import java.awt.*;

public abstract class SyntaxHighlighter {

	public abstract Color getColor(String token);

	public abstract void parseString(String strg);

	public abstract String getColorToken();

	public abstract Color getColor();

	public void drawLighted(String strg, Graphics g, int xpos, int ypos) {
		boolean done = false;
		parseString(strg);
		FontMetrics fm = g.getFontMetrics();
		while (!done) {
			String token = getColorToken();
			if (token == null) {
				done = true;
			} else {
				g.setColor(getColor());
				g.drawString(token, xpos, ypos);
				xpos += fm.stringWidth(token);
			}
		}
	}

}
