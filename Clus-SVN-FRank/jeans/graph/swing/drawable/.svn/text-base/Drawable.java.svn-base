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

public abstract class Drawable {

	protected int xp, yp;
	protected int wd = -1;
	protected int hi = -1;

	public Drawable() {
		xp = 0; yp = 0;
	}

	public void calcSize(Graphics2D g, FontMetrics fm, DrawableCanvas canvas) {
	}

	public void undoTransform() {
	}

	public abstract void draw(Graphics2D g, DrawableCanvas canvas, int xofs, int yofs);

	public boolean mousePressed(DrawableCanvas canvas, int x, int y, MouseEvent evt) {
		return false;
	}

	public boolean mouseSensitive() {
		return false;
	}

	public int getX() {
		return xp;
	}

	public int getY() {
		return yp;
	}

	public int getWidth() {
		return wd;
	}

	public int getHeight() {
		return hi;
	}

	public int getRight() {
		return xp+wd;
	}

	public int getXMid() {
		return xp+wd/2;
	}

	public int getYMid() {
		return yp+hi/2;
	}

	public int getYBottom() {
		return yp+hi;
	}

	public Rectangle getBoundRect() {
		return new Rectangle(xp, yp, wd, hi);
	}

	public Dimension getSize() {
		return new Dimension(wd, hi);
	}

	public void translate(int dx, int dy) {
		xp += dx;
		yp += dy;
	}

	public void setXY(int xpos, int ypos) {
		xp = xpos; yp = ypos;
	}

	public void setX(int xpos) {
		xp = xpos;
	}

	public void setY(int ypos) {
		yp = ypos;
	}

	public void setWidth(int wd) {
		this.wd = wd;
	}

	public void setHeight(int hi) {
		this.hi = hi;
	}

	public void setSize(int wd, int hi) {
		this.wd = wd;
		this.hi = hi;
	}
}
