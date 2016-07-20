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
import java.util.*;

public class PercTableLayout implements LayoutManager {

    private int b_size, m_cols;

    private int[] sizes;
    private int[] modes;
    private int[] heights;

    private int num, nbDummies;

    private int preferredWidth, preferredHeight;
    private boolean sizeUnknown = true;

    public final static int PERCENT   = 1;
    public final static int ABSOLUTE  = 2;
    public final static int PREF      = 4;
    public final static int DUMMY     = 8;

    public PercTableLayout(String descr, int gap) {
	int pos;
	b_size = gap;
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
		pos = token.indexOf('p');
		if (pos != -1) {
			modes[ctr] |= PREF;
		} else {
			try {
				sizes[ctr] = Integer.parseInt(token);
			} catch (NumberFormatException e) {}
		}
		if ((modes[ctr] & (PERCENT | PREF)) == 0) modes[ctr] |= ABSOLUTE;
	}
	m_cols = num - nbDummies;
    }

    public void addLayoutComponent(String name, Component comp) { }

    public void removeLayoutComponent(Component comp) {}

    public int getComponentCount(Container parent) {
	return parent.getComponentCount() + nbDummies;
    }

    private void calcSizes(Container parent) {
    	int nComps = getComponentCount(parent);
    	int nRows = nComps / m_cols;
    	heights = new int[nRows];
    	for (int i = 0; i < num; i++) {
    		if ((modes[i] & PREF) != 0) {
    			sizes[i] = 0;
    		}
    	}
    	for (int row = 0; row < nRows; row++) {
    		heights[row] = 0;
    		int col = 0;
  		for (int i = 0; i < num; i++) {
			if ((modes[i] & DUMMY) == 0) {
				Component c = parent.getComponent(col + row*m_cols);
				if (c.isVisible()) {
					Dimension d = c.getPreferredSize();
					heights[row] = Math.max(heights[row], d.height);
					if ((modes[i] & PREF) != 0) {
						sizes[i] = Math.max(sizes[i], d.width);
					}
				}
				col++;
	     		}
        	}
	}
    }

    private int getColumnWidth(Container parent, int i, int col) {
    	int wd = 0;
	if ((modes[i] & ABSOLUTE) != 0) return sizes[i];
	if ((modes[i] & PREF) != 0) return sizes[i];
    	int nComps = getComponentCount(parent);
    	int nRows = nComps / m_cols;
    	for (int row = 0; row < nRows; row++) {
    		Component c = parent.getComponent(col + row*m_cols);
		if (c.isVisible()) {
			Dimension d = c.getPreferredSize();
			wd = Math.max(wd, d.width);
		}
	}
	return wd;
    }

    private void setSizes(Container parent) {
    	calcSizes(parent);
	int dim_vert = 0;
	int dim_horz = 0;
	int cr_col = 0;
	int nComps = getComponentCount(parent);
	int nRows = nComps / m_cols;
    	for (int row = 0; row < nRows; row++) {
    		dim_vert += heights[row];
    	}
    	dim_vert += (nRows-1)*b_size;
	for (int i = 0; i < num; i++) {
		if ((modes[i] & DUMMY) != 0) {
			if ((modes[i] & ABSOLUTE) != 0)
				dim_horz += sizes[i];
		} else {
			if ((modes[i] & ABSOLUTE) != 0)
				dim_horz += sizes[i];
			else
				dim_horz += getColumnWidth(parent, i, cr_col);
			cr_col++;
	     }
        }
	dim_horz += (num - 1)*b_size;
       	preferredWidth = dim_horz;
       	preferredHeight = dim_vert;
        sizeUnknown = false;
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        setSizes(parent);
        Insets insets = parent.getInsets();
        dim.width = preferredWidth + insets.left + insets.right;
        dim.height = preferredHeight + insets.top + insets.bottom;
        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
	return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getSize().width - (insets.left + insets.right);
	int nComps = getComponentCount(parent);
	int nRows = nComps / m_cols;
        if (sizeUnknown) setSizes(parent);
	int i, locksize = 0, cr_col = 0;
        for (i = 0 ; i < nComps && i < num; i++) {
		if ((modes[i] & ABSOLUTE) != 0) locksize += sizes[i];
		if ((modes[i] & PREF) != 0) {
			locksize += sizes[i];
		}
		if ((modes[i] & DUMMY) == 0) cr_col++;
	}
	int rem_size, horz_ofs = 0;
	rem_size = maxWidth - locksize - (num - 1)*b_size;
	cr_col = 0;
        for (i = 0; i < num; i++) {
        	int vert_ofs = 0;
		int mysize = 0;
		if ((modes[i] & ABSOLUTE) != 0) mysize = sizes[i]*100;
		if ((modes[i] & PERCENT) != 0) mysize = rem_size*sizes[i];
		if ((modes[i] & DUMMY) == 0) {
			if ((modes[i] & PREF) != 0) {
				mysize = sizes[i]*100;
			}
			int xp = (horz_ofs+50)/100;
    			int wd = (mysize+50)/100;
			for (int row = 0; row < nRows; row++) {
    				Component c = parent.getComponent(cr_col + row*m_cols);
				c.setBounds(xp,vert_ofs,wd,heights[row]);
				vert_ofs += heights[row] + b_size;
			}
			cr_col++;
		}
		horz_ofs += mysize + b_size*100;
        }
    }

    public String toString() {
        return getClass().getName() + " [Gap: " + b_size + "]";
    }
}
