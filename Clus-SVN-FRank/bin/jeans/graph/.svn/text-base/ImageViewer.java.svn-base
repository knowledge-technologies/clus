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

public class ImageViewer extends Canvas {

	public final static long serialVersionUID = 1;

	public final static int NOBORDER    = 0;
	public final static int LINEBORDER  = 1;
	public final static int SUNKEN3D = 1;
	public final static int RAISED3D = 2;

	private Image image;
	private Dimension size;
	private Color fillColor;
	private int fillWidth;
	private int borderWidth;
	private int borderType;

	public ImageViewer(Image image, int type) {
		this.image = image;
		this.fillColor = Color.black;
		this.fillWidth = 0;
		this.borderWidth = getDefaultSize(type);
		this.borderType = type;
		this.size = calcSize(image.getWidth(this), image.getHeight(this));
	}

	public ImageViewer(Image image, int wd, int hi, int bType) {
		this(image, new Dimension(wd, hi), Color.black, 0, getDefaultSize(bType), bType);
	}

	public ImageViewer(Image image, int wd, int hi, int bWidth, int bType) {
		this(image, new Dimension(wd, hi), Color.black, 0, bWidth, bType);
	}

	public ImageViewer(Image image, Dimension size, Color fColor, int fWidth, int bWidth, int bType) {
		this.image = image;
		this.fillColor = fColor;
		this.fillWidth = fWidth;
		this.borderWidth = bWidth;
		this.borderType = bType;
		this.size = size;
	}

	public Dimension getPreferredSize() {
		return size;
	}

	public void paint(Graphics g) {
		update(g);
	}

	public void update(Graphics g) {
		Dimension d = getSize();
		int width = image.getWidth(this);
		int height = image.getHeight(this);
		if (fillWidth != 0) {
			g.setColor(fillColor);
			int ofs = 3*borderWidth;
			g.fillRect(ofs, ofs, d.width-2*ofs, d.height-2*ofs);
		}
		g.drawImage(image,d.width/2-width/2,d.height/2-height/2,this);
		if (borderType == LINEBORDER) {
			g.setColor(getBackground().darker());
			g.drawRect(0, 0, d.width-1, d.height-1);
		} else if (borderType == RAISED3D) {
			g.setColor(getBackground());
			ImageUtil.draw3DRect(g, 0, 0, d.width, d.height, borderWidth, true);
		} else if (borderType == SUNKEN3D) {
			g.setColor(getBackground());
			ImageUtil.draw3DRect(g, 0, 0, d.width, d.height, borderWidth, false);
		}
	}

	private Dimension calcSize(int wd, int hi) {
		int cte = borderWidth*6 + fillWidth*2;
		return new Dimension(wd+cte,hi+cte);
	}

	private static int getDefaultSize(int type) {
		if (type == LINEBORDER) return 1;
		if (type != NOBORDER) return 2;
		return 0;
	}

}
