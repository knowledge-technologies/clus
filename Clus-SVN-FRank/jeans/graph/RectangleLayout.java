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

public class RectangleLayout implements LayoutManager {

    protected int m_PrefWidth = 800;
    protected int m_PrefHeight = 600;
    protected Dimension m_MinSize = new Dimension(30, 30);

    public RectangleLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public int getComponentCount(Container parent) {
	return parent.getComponentCount();
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        //Always add the container's getInsets!
        Insets insets = parent.getInsets();
        dim.width = m_PrefWidth + insets.left + insets.right;
        dim.height = m_PrefHeight + insets.top + insets.bottom;
        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
	return m_MinSize;
    }

    public void layoutContainer(Container parent) {
        Insets ins = parent.getInsets();
        int maxWidth = parent.getSize().width - (ins.left + ins.right);
        int maxHeight = parent.getSize().height - (ins.top + ins.bottom);
	int nComps = getComponentCount(parent);
	if (nComps == 2) {
		Component c1 = parent.getComponent(0);
		if (c1.isVisible()) {
			c1.setBounds(ins.left, ins.top, maxHeight, maxHeight);
		}
		Component c2 = parent.getComponent(1);
		if (c2.isVisible()) {
			c2.setBounds(ins.left+maxHeight, ins.top, maxWidth-maxHeight, maxHeight);
		}
	}
    }

    public String toString() {
        return "RectangleLayout";
    }
}
