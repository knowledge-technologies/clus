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

public class ImageUtil {

	public static void draw3DRect(Graphics g, int x, int y, int width, int height, int shadow, boolean raised) {
		Color c = g.getColor();
		Color brighter = c.brighter();
		Color darker = c.darker();

		// upper left corner
		g.setColor(raised ? brighter : darker);
		for (int i=0; i<shadow; i++) {
			g.drawLine(x+i, y+i, x+width-1-i, y+i);
			g.drawLine(x+i, y+i, x+i, y+height-1-i);
		}
		// lower right corner
		g.setColor(raised ? darker : brighter);
		for (int i=0; i<shadow; i++) {
			g.drawLine(x+i, y+height-1-i, x+width-1-i, y+height-1-i);
			g.drawLine(x+width-1-i, y+height-1-i, x+width-1-i, y+i);
		}
		g.setColor(c);
	}

	public static void drawThickRect(Graphics g, int x, int y, int w, int h, int d) {
		g.fillRect(x,y,w,d);
		g.fillRect(x,y+d,d,h-2*d);
		g.fillRect(x,y+h-d,w,d);
		g.fillRect(x+w-d,y+d,d,h-2*d);
	}

	public static Polygon makeParalRect(int x1,int y1,int x2,int y2,int x3,int y3) {
		int x4, y4;
		Polygon p = new Polygon();
		p.addPoint(x1,y1);
		p.addPoint(x2,y2);
		x4 = x2+x3-x1;
		y4 = y2+y3-y1;
		p.addPoint(x4,y4);
		p.addPoint(x3,y3);
		return p;
	}

	public static Polygon makeHexagon(int x, int y, int wd) {
		int side = wd*55/100; //(sqrt(7)-1)/3
		int delta = (wd-side)/2;
		Polygon p = new Polygon();
		p.addPoint(x,y);
		p.addPoint(x+delta,y-wd/2);
		p.addPoint(x+wd-delta,y-wd/2);
		p.addPoint(x+wd,y);
		p.addPoint(x+wd-delta,y+wd/2);
		p.addPoint(x+delta,y+wd/2);
		return p;
	}

	public static void drawCube(Graphics g, int x, int y, int w, int h, int d, float fac) {
		int dx = (int)((float)d*fac);
		int dy = (int)((float)d*(1-fac));
		Color color = g.getColor();
		g.fillRect(x,y,w+1,h+1);
		g.setColor(color.darker());
		Polygon p1 = makeParalRect(x+w,y+h,x+w,y,x+w+dx,y+h-dy);
		Polygon p2 = makeParalRect(x,y,x+dx,y-dy,x+w,y);
		g.fillPolygon(p1);
		g.fillPolygon(p2);
		g.setColor(color);
		g.drawPolygon(p1);
		g.drawPolygon(p2);
	}

}

