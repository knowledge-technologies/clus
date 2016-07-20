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

/*
 * Copyright (c) 1995, 1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package jeans.graph;

import java.awt.*;

public class MyColumnLayout implements LayoutManager {

    private int gap, cols, rows;
    private int[] wds, his;
    private boolean border;
    private boolean sizeKnown;

    public MyColumnLayout(int cols, int rows, int gap, boolean border) {
	this.gap = gap;
	this.border = border;
	this.cols = cols;
	this.rows = rows;
	this.wds = new int[cols];
	this.his = new int[rows];
    }

    /* Required by LayoutManager. */
    public void addLayoutComponent(String name, Component comp) {
    }

    /* Required by LayoutManager. */
    public void removeLayoutComponent(Component comp) {
    }

    public int getComponentCount(Container parent) {
	return parent.getComponentCount();
    }

    public void calcSizes(Container parent, int nComps) {

	for (int i = 0; i < cols; i++) wds[i] = 0;
	for (int i = 0; i < rows; i++) his[i] = 0;

	int idx = 0;
	for (int row = 0; row < rows && idx < nComps; row++) {
		for (int col = 0; col < cols && idx < nComps; col++) {
			Component c = parent.getComponent(idx++);
			if (c.isVisible()) {
				Dimension d = c.getPreferredSize();
				wds[col] = Math.max(wds[col], d.width);
				his[row] = Math.max(his[row], d.height);
			}
		}
        }

	sizeKnown = true;
    }

    /* Required by LayoutManager. */
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        //Always add the container's getInsets!
        Insets insets = parent.getInsets();

	int nComps = getComponentCount(parent);
	calcSizes(parent, nComps);

	for (int i = 0; i < cols; i++) dim.width += wds[i];
	for (int i = 0; i < rows; i++) dim.height += his[i];

        dim.width += insets.left + insets.right + (cols-1)*gap;
        dim.height += insets.top + insets.bottom + (rows-1)*gap;

        if (border) {
	        dim.width += 2*gap;
        	dim.height += 2*gap;
        }

        return dim;
    }

    /* Required by LayoutManager. */
    public Dimension minimumLayoutSize(Container parent) {
	return preferredLayoutSize(parent);
    }

    /* Required by LayoutManager. */
    /* This is called when the panel is first displayed,
     * and every time its size changes.
     * Note: You CAN'T assume preferredLayoutSize() or minimumLayoutSize()
     * will be called -- in the case of applets, at least, they probably
     * won't be. */
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getSize().width - (insets.left + insets.right);

 	if (border) maxWidth -= 2*gap;

 	int ypos = insets.top;
 	int xpos = insets.left;
	if (border) {
		xpos += gap;
		ypos += gap;
	}

	int nComps = getComponentCount(parent);
	if (!sizeKnown) calcSizes(parent, nComps);

	int idx = 0;
	for (int row = 0; row < rows && idx < nComps; row++) {
		for (int col = 0; col < cols && idx < nComps; col++) {
			Component c = parent.getComponent(idx++);
			if (c.isVisible()) {
				Dimension d = c.getPreferredSize();
				c.setBounds(xpos, ypos, d.width, d.height);
				xpos += wds[col]+gap;
			}
		}
		ypos += his[row]+gap;
		xpos = insets.left;
		if (border) xpos += gap;
        }
    }

}
