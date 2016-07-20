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
import java.util.*;

public class PercentLayout implements LayoutManager {

    private int b_size, m_sides;
    private boolean b_vert;

    private int[] sizes;
    private int[] modes;

    private int num, nbDummies;

    private int preferredWidth, preferredHeight;
    private boolean sizeUnknown = true;

    public final static int PERCENT   = 1;
    public final static int ABSOLUTE  = 2;
    public final static int PREF      = 4;
    public final static int MIN       = 8;
    public final static int DUMMY     = 16;

    public final static int NORTH = 1;
    public final static int EAST  = 2;
    public final static int SOUTH = 4;
    public final static int WEST  = 8;
    public final static int ALL   = NORTH | SOUTH | WEST | EAST;

    public PercentLayout(String descr, int gap, int sides, boolean vert) {
	int pos;
	b_size = gap;
	m_sides = sides;
	b_vert = vert;
	StringTokenizer tokens = new StringTokenizer(descr);
	num = tokens.countTokens();
	modes = new int[num];
	sizes = new int[num];
	for (int ctr = 0; ctr < num; ctr++) {
		modes[ctr] = 0;
		sizes[ctr] = 0;
		String token = tokens.nextToken();
		pos = token.indexOf('d');
		if (pos != -1) {
			nbDummies++;
			modes[ctr] |= DUMMY;
			token = token.substring(0,pos);
		}
		pos = token.indexOf('%');
		if (pos != -1) {
			modes[ctr] |= PERCENT;
			token = token.substring(0,pos);
		}
		if (token.indexOf('p') != -1) {
			modes[ctr] |= PREF;
		} if (token.indexOf('m') != -1) {
			modes[ctr] |= MIN;
		} else {
			try {
				sizes[ctr] = Integer.parseInt(token);
			} catch (NumberFormatException e) {}
		}
		if ((modes[ctr] & (PERCENT | PREF)) == 0) modes[ctr] |= ABSOLUTE;
	}
    }

    /* Required by LayoutManager. */
    public void addLayoutComponent(String name, Component comp) {
    }

    /* Required by LayoutManager. */
    public void removeLayoutComponent(Component comp) {
    }

    public int getComponentCount(Container parent) {
	return parent.getComponentCount() + nbDummies;
    }

    private void setSizes(Container parent) {
        int nComps = getComponentCount(parent);
        Dimension d = null;

        //Reset preferred/minimum width and height.

	int dim_one = 0, my_one;
	int dim_mult = 0, my_mult;
	int idx = 0;

	for (int i = 0; i < nComps && i < num; i++) {
		if ((modes[i] & DUMMY) != 0) {
			if ((modes[i] & ABSOLUTE) != 0)
				dim_mult += sizes[i];
		} else {
			Component c = parent.getComponent(idx++);
			if (c.isVisible()) {
				d = (modes[i] & PREF) != 0 ? c.getPreferredSize() : c.getMinimumSize();
				if (b_vert) {
					my_one = d.width;
					my_mult = d.height;
				} else {
        				my_one = d.height;
        				my_mult = d.width;
				}
				if ((modes[i] & ABSOLUTE) != 0)
					dim_mult += sizes[i];
				else
					dim_mult += my_mult;
				dim_one = Math.max(my_one,dim_one);
			}
	     }
        }

	dim_mult += (num - 1)*b_size;

	if (b_vert) {
        	preferredWidth = dim_one;
        	preferredHeight = dim_mult;
	} else {
        	preferredWidth = dim_mult;
        	preferredHeight = dim_one;
	}


	if ((m_sides & WEST) != 0) preferredWidth += b_size;

	if ((m_sides & EAST) != 0) preferredWidth += b_size;

	if ((m_sides & NORTH) != 0) preferredHeight += b_size;

	if ((m_sides & SOUTH) != 0) preferredHeight += b_size;


        sizeUnknown = false;
    }


    /* Required by LayoutManager. */
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        setSizes(parent);

        //Always add the container's getInsets!
        Insets insets = parent.getInsets();
        dim.width = preferredWidth + insets.left + insets.right;
        dim.height = preferredHeight + insets.top + insets.bottom;

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
        int maxWidth = parent.getSize().width
                       - (insets.left + insets.right);
        int maxHeight = parent.getSize().height
                        - (insets.top + insets.bottom);

	if ((m_sides & WEST) != 0) maxWidth -= b_size;

	if ((m_sides & EAST) != 0) maxWidth -= b_size;

	if ((m_sides & NORTH) != 0) maxHeight -= b_size;

	if ((m_sides & SOUTH) != 0) maxHeight -= b_size;

	int nComps = getComponentCount(parent);

        // Go through the components' sizes, if neither preferredLayoutSize()
        // nor minimumLayoutSize() has been called.
        if (sizeUnknown) {
            setSizes(parent);
        }

	int i, locksize = 0, idx = 0;
	Dimension d;

        for (i = 0 ; i < nComps && i < num; i++) {
		if ((modes[i] & ABSOLUTE) != 0) locksize += sizes[i];
		if ((modes[i] & PREF) != 0 || (modes[i] & MIN) != 0) {
			Component c = parent.getComponent(idx);
			if (c.isVisible()) {
				d = (modes[i] & PREF) != 0 ? c.getPreferredSize() : c.getMinimumSize();
				if (b_vert)
					locksize += d.height;
				else
					locksize += d.width;
			}
		}
		if ((modes[i] & DUMMY) == 0) idx++;
	}


	int rem_size, mult_ofs = 0, one_ofs = 0;
	if (b_vert) {
		rem_size = maxHeight - locksize - (num - 1)*b_size;
		if ((m_sides & NORTH) != 0) mult_ofs = b_size*100;
		if ((m_sides & WEST) != 0) one_ofs = b_size;
	} else {
		rem_size = maxWidth - locksize - (num - 1)*b_size;
		if ((m_sides & WEST) != 0) mult_ofs = b_size*100;
		if ((m_sides & NORTH) != 0) one_ofs = b_size;
	}

	idx = 0;
        for (i = 0 ; i < nComps && i < num; i++) {
		int mysize = 0;
		if ((modes[i] & ABSOLUTE) != 0) mysize = sizes[i]*100;
		if ((modes[i] & PERCENT) != 0) mysize = rem_size*sizes[i];
		if ((modes[i] & DUMMY) == 0) {
			Component c = parent.getComponent(idx++);
			if (c.isVisible()) {
				// Set the component's size and position.
				if ((modes[i] & PREF) != 0 || (modes[i] & MIN) != 0) {
					d = (modes[i] & PREF) != 0 ? c.getPreferredSize() : c.getMinimumSize();
					if (b_vert)
						mysize = d.height*100;
					else
						mysize = d.width*100;
				}
				if (b_vert) {
					c.setBounds(one_ofs,(mult_ofs+50)/100,maxWidth,(mysize+50)/100);
				} else {
					c.setBounds((mult_ofs+50)/100,one_ofs,(mysize+50)/100,maxHeight);
				}
			}
		}
		mult_ofs += mysize + b_size*100;
        }
    }

    public String toString() {
        return getClass().getName() + " [Gap: " + b_size + "]";
    }
}
