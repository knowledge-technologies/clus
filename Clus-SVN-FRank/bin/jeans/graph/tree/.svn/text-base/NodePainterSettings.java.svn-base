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

package jeans.graph.tree;

import jeans.util.*;

import java.awt.*;

public class NodePainterSettings extends MyVisitorParent {

	public Color NODE_COLOR = Color.white;
	public Color LEAF_COLOR = Color.white;
	public Color NODE_BORDER_COLOR = Color.black;
	public Color LEAF_BORDER_COLOR = Color.black;

	public int YTOP  = 10;
	public int XLEFT = 10;

	/* One value for each zoom level */
	public int[] XGAP;
	public int YGAP = 20;

	protected Object m_Document;

	public void setDocument(Object doc) {
		m_Document = doc;
	}

	public Object getDocument() {
		return m_Document;
	}

	public void print() {
		System.out.println("YTOP  = "+YTOP);
		System.out.println("XLEFT = "+XLEFT);
		System.out.println("XGAP  = "+XGAP);
		System.out.println("YGAP  = "+YGAP);
	}

}
