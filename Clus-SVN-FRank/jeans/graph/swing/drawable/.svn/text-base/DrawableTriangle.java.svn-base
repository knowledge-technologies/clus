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

public class DrawableTriangle extends Drawable {

	private Color color;
	private Stroke stroke;

	public DrawableTriangle(int xp, int yp, int wd, int hi, Color color) {
		this.xp = xp;
		this.yp = yp;
		this.wd = wd;
		this.hi = hi;
		this.color = color;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public void draw(Graphics2D g, DrawableCanvas canvas, int xofs, int yofs) {
		drawTriangle(g, xp-xofs, yp-yofs, wd, hi);
	}

	public void drawTriangle(Graphics2D g, int x, int y, int w, int h) {
		Polygon poly = new Polygon();
		poly.addPoint(x, y+h);
		poly.addPoint(x+w/2, y);
		poly.addPoint(x+w, y+h);
		g.setColor(color);
		g.setStroke(stroke);
		g.draw(poly);
	}
}
