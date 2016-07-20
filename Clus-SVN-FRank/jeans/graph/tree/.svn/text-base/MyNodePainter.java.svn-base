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

import jeans.graph.swing.drawable.*;

import java.awt.*;

public abstract class MyNodePainter extends Drawable {

	protected MyDrawableNode m_Node;
	protected int m_Zoom;

	public MyNodePainter(MyDrawableNode node) {
		m_Node = node;
	}

	public MyDrawableNode getNode() {
		return m_Node;
	}

	public Color getNodeColor() {
		if (m_Node.atBottomLevel()) return m_Node.getPaintSettings().LEAF_COLOR;
		else return m_Node.getPaintSettings().NODE_COLOR;
	}

	public Color getBorderColor() {
		if (m_Node.atBottomLevel()) return m_Node.getPaintSettings().LEAF_BORDER_COLOR;
		else return m_Node.getPaintSettings().NODE_BORDER_COLOR;
	}

	public abstract MyNodePainter createPainter(MyDrawableNode node);

	public DrawableExpandButton getExpandButton() {
		return null;
	}

	public Drawable getLabel() {
		return null;
	}

	public int getZoom() {
		return m_Zoom;
	}

	public void setZoom(int zoom) {
		m_Zoom = zoom;
	}

	public void onFakeLeaf(boolean fake) {
	}

	public void draw(Graphics2D g, DrawableCanvas canvas, int xofs, int yofs) {
		g.setColor(getNodeColor());
		g.fillRect(xp-xofs, yp-yofs, wd, hi);
		g.setColor(getBorderColor());
		g.drawRect(xp-xofs, yp-yofs, wd, hi);
	}
}
