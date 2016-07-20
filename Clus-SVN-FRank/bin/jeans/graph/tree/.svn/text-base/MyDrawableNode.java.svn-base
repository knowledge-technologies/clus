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

import jeans.tree.*;
import jeans.util.*;

public class MyDrawableNode extends MyVisitableNode {

	public final static long serialVersionUID = 1;

	protected boolean m_bFakeLeaf;
	protected MyNodePainter m_Painter;
	protected int m_Index;

	public MyDrawableNode(MyVisitorParent vpar) {
		super(vpar);
	}

	public static MyDrawableNode createTree(Node node, MyNodePainter paint) {
		NodePainterSettings par = new NodePainterSettings();
		return createTree(node, par, paint);
	}

	public static MyDrawableNode createTree(Node node, NodePainterSettings par, MyNodePainter paint) {
		MyDrawableNode root = new MyDrawableNode(par);
		par.addVisitor();
		root.recursiveCreateTree(node, paint);
		return root;
	}

	public void deleteChild(int idx) {
		removeChild(idx);
		for (int i = 0; i < getNbChildren(); i++) {
			MyDrawableNode node = (MyDrawableNode)getChild(i);
			node.setIndex(idx);
		}
	}

	public void swapChildren(int ch1, int ch2) {
		MyDrawableNode child1 = (MyDrawableNode)getChild(ch1);
		MyDrawableNode child2 = (MyDrawableNode)getChild(ch2);
		setChild(child2, ch1);
		setChild(child1, ch2);
		child2.setIndex(ch1);
		child1.setIndex(ch2);
	}

	public int getIndex() {
		return m_Index;
	}

	public void setIndex(int index) {
		m_Index = index;
	}

	public NodePainterSettings getPaintSettings() {
		return (NodePainterSettings)m_VisParent;
	}

	public MyNodePainter getPainter() {
		return m_Painter;
	}

	public MyNodePainter getChildPainter(int i) {
		return ((MyDrawableNode)getChild(i)).getPainter();
	}

	public void setPainter(MyNodePainter paint) {
		m_Painter = paint;
	}

	public void setFakeLeaf(boolean fake) {
		m_bFakeLeaf = fake;
		m_Painter.onFakeLeaf(fake);
	}

	public boolean isFakeLeaf() {
		return m_bFakeLeaf;
	}

	public void recursiveFakeLeaf(boolean fake) {
		setFakeLeaf(fake);
		for (int i = 0; i < getNbChildren(); i++) {
			MyDrawableNode child = (MyDrawableNode)getChild(i);
			child.recursiveFakeLeaf(fake);
		}
	}

	public void recursiveZoom(int zoom) {
		setZoom(zoom);
		for (int i = 0; i < getNbChildren(); i++) {
			MyDrawableNode child = (MyDrawableNode)getChild(i);
			child.recursiveZoom(zoom);
		}
	}

	public void recursiveZoomSubtree(int zoom) {
		for (int i = 0; i < getNbChildren(); i++) {
			MyDrawableNode child = (MyDrawableNode)getChild(i);
			child.recursiveZoom(zoom);
		}
	}

	public void setZoom(int zoom) {
		if (m_Painter != null) m_Painter.setZoom(zoom);
	}

	public int getZoom() {
		return m_Painter != null ? m_Painter.getZoom() : 0;
	}

	public boolean atFakeBottomLevel() {
		if (isFakeLeaf()) return true;
		else return atBottomLevel();
	}

	public int getNbFakeChildren() {
		if (isFakeLeaf()) return 0;
		else return getNbChildren();
	}

	public int getFakeDepth() {
		int nb = getNbChildren();
		if (nb == 0 || isFakeLeaf()) {
			return 1;
		} else {
			int max = 0;
			for (int i = 0; i < nb; i++) {
				MyDrawableNode node = (MyDrawableNode)getChild(i);
				max = Math.max(max, node.getFakeDepth());
			}
			return max + 1;
		}
	}

	public String toString() {
		return getVisitor(0).toString();
	}

	private void recursiveCreateTree(Node node, MyNodePainter paint) {
		setVisitor(node, 0);
		setPainter(paint.createPainter(this));
		int arity = node.getNbChildren();
		setNbChildren(arity);
		for (int i = 0; i < arity; i++) {
			MyDrawableNode child = new MyDrawableNode(getVisParent());
			setChild(child, i);
			child.setIndex(i);
			child.recursiveCreateTree(node.getChild(i), paint);
		}
	}
}
