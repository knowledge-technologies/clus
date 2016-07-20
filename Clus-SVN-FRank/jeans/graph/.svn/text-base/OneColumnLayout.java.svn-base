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

public class OneColumnLayout implements LayoutManager {

    private int gap;
    private boolean border;

    public OneColumnLayout(int gap, boolean border) {
	this.gap = gap;
	this.border = border;
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

    /* Required by LayoutManager. */
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        //Always add the container's getInsets!
        Insets insets = parent.getInsets();

	int nComps = getComponentCount(parent);
	for (int i = 0; i < nComps; i++) {
		Component c = parent.getComponent(i);
		if (c.isVisible()) {
			Dimension d = c.getPreferredSize();
			dim.width = Math.max(dim.width, d.width);
			dim.height += d.height;
		}
        }

        dim.width += insets.left + insets.right + (nComps-1)*gap;
        dim.height += insets.top + insets.bottom + (nComps-1)*gap;

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
	int nComps = getComponentCount(parent);
        for (int i = 0 ; i < nComps; i++) {
		Component c = parent.getComponent(i);
		if (c.isVisible()) {
			// Set the component's size and position.
			Dimension d = c.getPreferredSize();
			c.setBounds(xpos, ypos, maxWidth, d.height);
			ypos += d.height + gap;
		}
        }
    }

}
