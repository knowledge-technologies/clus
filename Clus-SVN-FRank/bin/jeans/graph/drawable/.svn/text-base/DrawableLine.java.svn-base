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

package jeans.graph.drawable;

import java.awt.*;

public class DrawableLine extends Drawable {

	public final static int LOWER = 0;
	public final static int CENTER = 1;
	public final static int UPPER = 2;

	String line;
	Font font;
	boolean spacing;
	Color color = Color.black;
	int xorient, yorient;

	public DrawableLine(String line, Font font, boolean spacing) {
		super();
		this.line = line;
		this.font = font;
		this.spacing = spacing;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setOrientation(int xo, int yo) {
		xorient = xo;
		yorient = yo;
	}

	public Rectangle getBoundRect(DrawableProvider prov) {
		if (wd == -1 || hi == -1) {
			FontMetrics fm = prov.getDMetrics(font);
			wd = fm.stringWidth(line);
			hi = getHeight(fm);
		}
		return new Rectangle(xp, yp, wd, hi);
	}

	public int getHeight(FontMetrics fm) {
		if (spacing) return fm.getHeight();
		return fm.getAscent();
	}

	public void draw(DrawableProvider prov, int xofs, int yofs) {
		Graphics g = prov.getDGraphics();
		g.setColor(color);
		g.setFont(font);
		FontMetrics fm = prov.getDMetrics(font);
		int xpos = xp-xofs;
		int ypos = yp+fm.getAscent()-yofs;
		switch (xorient) {
			case CENTER: xpos = xpos + wd/2 - fm.stringWidth(line)/2; break;
			case UPPER: xpos = xpos + wd - fm.stringWidth(line); break;
		}
		switch (yorient) {
			case CENTER: ypos = ypos + hi/2 - getHeight(fm)/2; break;
			case UPPER: ypos = ypos + hi - getHeight(fm); break;
		}
		g.drawString(line, xpos, ypos);
	}
}
