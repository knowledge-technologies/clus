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

public class DrawableImage extends Drawable {

	Image image;

	public DrawableImage(Image image) {
		super();
		this.image = image;
	}

	public Rectangle getBoundRect(DrawableProvider prov) {
		if (wd == -1 || hi == -1) {
			wd = image.getWidth(prov.getDCanvas());
			hi = image.getWidth(prov.getDCanvas());
		}
		return new Rectangle(xp, yp, wd, hi);
	}

	public void draw(DrawableProvider prov, int xofs, int yofs) {
		Graphics g = prov.getDGraphics();
		int xpos = xp-xofs;
		int ypos = yp-yofs;
		g.drawImage(image, xpos, ypos, prov.getDCanvas());
	}
}
