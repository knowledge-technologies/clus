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

public abstract class Drawable {

	protected int xp, yp;
	protected int wd, hi;

	public Drawable() {
		this.xp = 0;
		this.yp = 0;
		this.wd = -1;
		this.hi = -1;
	}

	public void setPosition(int xp, int yp) {
		this.xp = xp;
		this.yp = yp;
	}

	public int isInY(int ypos) {
		if (ypos < yp) return -1;
		if (ypos > yp+hi) return +1;
		return 0;
	}

	public abstract Rectangle getBoundRect(DrawableProvider prov);

	public abstract void draw(DrawableProvider prov, int xofs, int yofs);

}
