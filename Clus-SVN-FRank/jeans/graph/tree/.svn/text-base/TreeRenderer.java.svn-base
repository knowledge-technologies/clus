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
import jeans.tree.*;
import jeans.util.*;

import java.util.*;
import java.awt.*;

public class TreeRenderer implements DrawableRenderer {

    	protected MyDrawableNode m_Node;
    	protected Dimension m_Size;
	protected boolean m_Vert;
	protected ArrayList m_Debug = new ArrayList();
	protected int m_Levels;

    	public TreeRenderer(MyDrawableNode node) {
		m_Node = node;
		m_Vert = true;
    	}

	public void setZoomLevels(int levels) {
		m_Levels = levels;
	}

	public void setHorzVert(boolean horz) {
		m_Vert = !horz;
	}

	public void render(Graphics2D g, FontMetrics fm, DrawableCanvas canvas) {
		calcSizes(m_Node, g, fm, canvas);
		int depth = m_Node.getMaxLeafDepth();
		MyNodePainter paint = m_Node.getPainter();
		paint.setXY(0,0);
		Renderer renderer = new Renderer(depth, true, m_Levels);
		renderer.calcHi(m_Node, 0);
		NodePainterSettings sett = m_Node.getPaintSettings();
		renderer.positionY(m_Node, 0, sett.YTOP);
		renderer.render(m_Node);
		renderer.moveTop(m_Node, sett.XLEFT);
		m_Size = canvas.transformDimension(renderer.getSize(m_Node));
	}

	public class Renderer {

		int m_MaxHi;
		int[][] m_Hi, m_LHi;
		IntervalTreeRB m_ITree;

		public Renderer(int depth, boolean calcHi, int levels) {
			if (calcHi) {
				m_Hi  = new int[depth+1][levels];
				m_LHi = new int[depth+1][levels];
			}
			m_ITree = new IntervalTreeRB();
			m_MaxHi = Integer.MIN_VALUE;
			m_Debug.clear();
		}

		public Dimension getSize(MyDrawableNode node) {
			NodePainterSettings sett = m_Node.getPaintSettings();
			int wd = m_ITree.findMax(sett.XLEFT);
			if (m_Vert) {
				return new Dimension(wd, m_MaxHi);
			} else {
				return new Dimension(m_MaxHi, wd);
			}
		}

		public void calcHi(MyDrawableNode node, int depth) {
			MyNodePainter pdr = node.getPainter();
			Drawable label = pdr.getLabel();
			int level = pdr.getZoom();
			if (label != null)
				m_LHi[depth][level] = Math.max(m_LHi[depth][level], getHeight(label));
			m_Hi[depth][level] = Math.max(m_Hi[depth][level], getHeight(pdr));
			for (int i = 0; i < node.getNbFakeChildren(); i++) {
				MyDrawableNode child = (MyDrawableNode)node.getChild(i);
				calcHi(child, depth + 1);
			}
		}

		public void positionY(MyDrawableNode node, int depth, int ypos) {
			MyNodePainter pdr = node.getPainter();
			setY(pdr, ypos);
			NodePainterSettings sett = node.getPaintSettings();
			// For label max over children
			int labelhi = 0;
			for (int i = 0; i < node.getNbFakeChildren(); i++) {
				MyDrawableNode child = (MyDrawableNode)node.getChild(i);
				labelhi = Math.max(labelhi, m_LHi[depth+1][child.getZoom()]);
			}
			// For height, max over siblings
			int maxhi = 0;
			MyDrawableNode parent = (MyDrawableNode)node.getParent();
			if (parent == null) {
				maxhi = m_Hi[depth][node.getZoom()];
			} else {
				for (int i = 0; i < parent.getNbFakeChildren(); i++) {
					MyDrawableNode sibling = (MyDrawableNode)parent.getChild(i);
					maxhi = Math.max(maxhi, m_Hi[depth][sibling.getZoom()]);
				}
			}
			// Update height
			ypos += sett.YGAP + maxhi + labelhi;
			if (ypos > m_MaxHi) {
				m_MaxHi = ypos;
			}
			for (int i = 0; i < node.getNbFakeChildren(); i++) {
				MyDrawableNode child = (MyDrawableNode)node.getChild(i);
				positionY(child, depth + 1, ypos);
			}
		}

		public void render(MyDrawableNode node) {
			if (!node.atFakeBottomLevel()) {
				positionNode(node);
				// addDebugLines();
			}
			for (int i = 0; i < node.getNbFakeChildren(); i++) {
				MyDrawableNode child = (MyDrawableNode)node.getChild(i);
				render(child);
			}
		}

		private void moveTop(MyDrawableNode node, int gap) {
			MyNodePainter pdr = node.getPainter();
			int xpos = getX(pdr);
			if (xpos < gap) moveNodeRecursive(node, gap-xpos);
		}

//              +--------+
//              | parent |
//              +--------+
//
// +----+    - calculate width of all children + gaps
// |    |    - two cases: odd and even nb children
// +----+

		private void positionNode(MyDrawableNode node) {
			MyNodePainter pdr = node.getPainter();
			/* Check how much space child nodes need to the left */
			int toLeft = 0;
			int nbChildren = node.getNbFakeChildren();
			NodePainterSettings sett = node.getPaintSettings();
			for (int i = 0; i < nbChildren/2; i++) {
				MyNodePainter cdr = node.getChildPainter(i);
				toLeft += getWidthP(cdr);
				if (i < nbChildren-1) {
					MyNodePainter ch2dr = node.getChildPainter(i+1);
					int xgap = Math.max(sett.XGAP[cdr.getZoom()], sett.XGAP[ch2dr.getZoom()]);
					if (i == nbChildren/2-1 && nbChildren % 2 == 0) {
						toLeft += xgap/2;
					} else {
						toLeft += xgap;
					}
				}
			}
			if (nbChildren % 2 != 0) {
				MyNodePainter cdr = node.getChildPainter(nbChildren/2);
				toLeft += getWidthP(cdr)/2;
			}
			int movepar = 0;
			int xps = getXMid(pdr)-toLeft;
			/* Compute y-interval of child nodes */
			int ymin = Integer.MAX_VALUE;
			int ymax = Integer.MIN_VALUE;
			for (int i = 0; i < nbChildren; i++) {
				MyDrawableNode chi = (MyDrawableNode)node.getChild(i);
				MyNodePainter cdr = chi.getPainter();
				int ch_y0 = getY(cdr)-sett.YGAP+1;
				int ch_y1 = chi.atFakeBottomLevel() ? getYBottom(cdr) : getY(chi.getChildPainter(0))-sett.YGAP;
				if (ch_y0 < ymin) { ymin = ch_y0; }
				if (ch_y1 > ymax) { ymax = ch_y1; }
			}
			/* Check if parent node should be moved or not */
			int xmax = m_ITree.findOverlappingIntervalsMax(ymin, ymax, sett.XLEFT);
			if (xps < xmax) {
				movepar = xmax-xps;
				xps = xmax;
			}
			for (int i = 0; i < nbChildren; i++) {
				MyNodePainter cdr = node.getChildPainter(i);
				setXP(cdr, xps);
				xps = getRightP(cdr);
				if (i < nbChildren-1) {
					MyNodePainter ch2dr = node.getChildPainter(i+1);
					xps += Math.max(sett.XGAP[cdr.getZoom()], sett.XGAP[ch2dr.getZoom()]);
				} else {
					xps += sett.XGAP[0];
				}
			}
			/* Update max xpos */
			m_ITree.addInterval(ymin, ymax, xps);
			if (movepar > 0) repositionParent(node, movepar);
		}

		private void moveNode(MyDrawableNode node, int delta) {
			MyNodePainter paint = node.getPainter();
			if (m_Vert) {
				paint.translate(delta, 0);
			} else {
				paint.translate(0, delta);
			}
			NodePainterSettings sett = node.getPaintSettings();
			int ymin = getY(paint)-sett.YGAP+1;
			int ymax = node.atFakeBottomLevel() ? getYBottom(paint) : getY(node.getChildPainter(0))-sett.YGAP;
			int xmax = m_ITree.findOverlappingIntervalsMax(ymin, ymax, sett.XLEFT);
			m_ITree.addInterval(ymin, ymax, Math.max(xmax, getRightP(paint) + sett.XGAP[0]));
		}

		private void moveNodeRecursive(MyDrawableNode node, int delta) {
			moveNode(node, delta);
			for (int i = 0; i < node.getNbFakeChildren(); i++) {
				MyDrawableNode child = (MyDrawableNode)node.getChild(i);
				moveNodeRecursive(child, delta);
			}
		}

		private void repositionParent(MyDrawableNode node, int delta) {
			moveNode(node, delta);
			int idx = node.getIndex();
			node = (MyDrawableNode)node.getParent();
			while (node != null) {
				int nbCh = node.getNbFakeChildren();
				for (int i = idx+1; i < nbCh; i++) {
					MyDrawableNode child = (MyDrawableNode)node.getChild(i);
					moveNode(child, delta);
				}
/*
				if (idx != 0) {
					MyNodePainter paint = node.getPainter();
					int parmid  = getMidParX(node, nbCh);
					int prevpar = getXMid(paint);
					int bestpos  = -1;
					int bestdist = Integer.MAX_VALUE;
					MyNodePainter prdr = node.getChildPainter(0);
					for (int i = 1; i < nbCh; i++) {
						MyNodePainter cdr = node.getChildPainter(i);
						int mypos = getXMid(cdr);
						int mydist = Math.abs(mypos - parmid);
						if (mydist < bestdist && mypos > prevpar) {
							bestpos  = mypos;
							bestdist = mydist;
						}
						mypos = (getRightP(prdr) + getX(cdr))/2;
						mydist = Math.abs(mypos - parmid);
						if (mydist < bestdist && mypos > prevpar) {
							bestpos  = mypos;
							bestdist = mydist;
						}
						prdr = cdr;
					}
					if (bestpos <= prevpar) break;
					delta = bestpos - prevpar;
				}
*/
				MyNodePainter paint = node.getPainter();
				if (node.getNbChildren() % 2 == 0) {
					MyNodePainter ch1 = node.getChildPainter(node.getNbChildren()/2-1);
					MyNodePainter ch2 = node.getChildPainter(node.getNbChildren()/2);
					delta = (getXMid(ch1)+getXMid(ch2))/2 - getXMid(paint);
					if (delta > 0) {
						moveNode(node, delta);
						idx = node.getIndex();
						node = (MyDrawableNode)node.getParent();
					} else {
						node = null;
					}
				} else {
					MyNodePainter ch1 = node.getChildPainter(node.getNbChildren()/2);
					delta = getXMid(ch1) - getXMid(paint);
					if (delta > 0) {
						moveNode(node, delta);
						idx = node.getIndex();
						node = (MyDrawableNode)node.getParent();
					} else {
						node = null;
					}
				}
			}
		}

		private int getWidthP(MyNodePainter pdr) {
			int wd = getWidth(pdr);
			Drawable label = pdr.getLabel();
			if (label != null && getWidth(label) > wd) {
				int delta = (getWidth(label)-wd)/2;
				wd += 2*delta;
			}
			return wd;
		}

		private int getRightP(MyNodePainter pdr) {
			int wd = getWidth(pdr);
			Drawable label = pdr.getLabel();
			if (label != null && getWidth(label) > wd) {
				int delta = (getWidth(label)-wd)/2;
				return getX(pdr) + wd + delta;
			} else {
				return getX(pdr) + wd;
			}
		}

		private void setXP(MyNodePainter pdr, int x) {
			int wd = getWidth(pdr);
			Drawable label = pdr.getLabel();
			if (label != null && getWidth(label) > wd) {
				int delta = (getWidth(label)-wd)/2;
				setX(pdr, x+delta);
			} else {
				setX(pdr, x);
			}
		}

		public void addDebugLines() {
			DebugLineAdder adder = new DebugLineAdder();
			m_ITree.execute(adder);
		}
	}

	protected class DebugLineAdder implements Executer {

		public void execute(Object param) {
			IntervalTreeNodeRB node = (IntervalTreeNodeRB)param;
			if (m_Vert) {
				m_Debug.add(new DrawableRectangle(node.value, node.key, 1, node.high-node.key, Color.red));
			} else {
				m_Debug.add(new DrawableRectangle(node.key, node.value, node.high-node.key, 1, Color.red));
			}
		}
	}

	private int getX(Drawable dr) {
		return m_Vert ? dr.getX() : dr.getY();
	}

	private int getY(Drawable dr) {
		return m_Vert ? dr.getY() : dr.getX();
	}

	private int getWidth(Drawable dr) {
		return m_Vert ? dr.getWidth() : dr.getHeight();
	}

	private int getHeight(Drawable dr) {
		return m_Vert ? dr.getHeight() : dr.getWidth();
	}

	public int getRight(Drawable dr) {
		return m_Vert ? dr.getRight() : dr.getYBottom();
	}

	private int getXMid(Drawable dr) {
		return m_Vert ? dr.getXMid() : dr.getYMid();
	}

	private int getYBottom(Drawable dr) {
		return m_Vert ? dr.getYBottom() : dr.getRight();
	}

	private void setX(Drawable dr, int pos) {
		if (m_Vert) dr.setX(pos);
		else dr.setY(pos);
	}

	private void setY(Drawable dr, int pos) {
		if (m_Vert) dr.setY(pos);
		else dr.setX(pos);
	}

	public Dimension getSize() {
		return m_Size;
	}

	public void removeAll(DrawableCanvas canvas) {
	}

	public void addAll(DrawableCanvas canvas) {
	    	addLines(m_Node, canvas);
		addNodes(m_Node, canvas);
		// addDebug(canvas);
	}

	public void addDebug(DrawableCanvas canvas) {
		for (int i = 0; i < m_Debug.size(); i++) {
			canvas.addDrawable((Drawable)m_Debug.get(i));
		}
	}

	public void addLines(MyDrawableNode node, DrawableCanvas canvas) {
		NodePainterSettings sett = node.getPaintSettings();
		MyNodePainter pdr = node.getPainter();
		Drawable label = pdr.getLabel();
		if (label != null) {
			label.setY(pdr.getY() - sett.YGAP/2 - label.getHeight() - 4);
			label.setX(pdr.getXMid() - label.getWidth()/2);
			canvas.addDrawable(label);
		}
		int nbChildren = node.getNbFakeChildren();
		if (nbChildren == 0) return;
		MyDrawableNode ch1 = (MyDrawableNode)node.getChild(0);
		MyNodePainter  p1  = ch1.getPainter();
		MyDrawableNode chN = (MyDrawableNode)node.getChild(nbChildren-1);
		MyNodePainter  pN  = chN.getPainter();
		int lwd = 2;
		if (m_Vert) {
			int ypos = p1.getY() - sett.YGAP/2;
			canvas.addDrawable(new DrawableRectangle(pdr.getXMid()-1, pdr.getYBottom()+1, lwd, ypos-pdr.getYBottom(), Color.black));
			canvas.addDrawable(new DrawableRectangle(p1.getXMid()-1, ypos-1, pN.getXMid()-p1.getXMid()+2, lwd, Color.black));
			for (int i = 0; i < nbChildren; i++) {
				MyDrawableNode child = (MyDrawableNode)node.getChild(i);
				MyNodePainter cdr = child.getPainter();
				int xchild = cdr.getXMid();
				canvas.addDrawable(new DrawableRectangle(xchild-1, ypos, lwd, cdr.getY()-ypos+2, Color.black));
				DrawableExpandButton bt = cdr.getExpandButton();
				if (bt != null) {
					bt.setXY(xchild-4, ypos-3);
					canvas.addDrawable(bt);
				}
				addLines(child, canvas);
			}
		} else {
			int xpos = p1.getX() - sett.YGAP/2;
			int xpb  = pdr.getRight();
			canvas.addDrawable(new DrawableRectangle(xpb+1, pdr.getYMid()-1, xpos-xpb, lwd, Color.black));
			canvas.addDrawable(new DrawableRectangle(xpos, p1.getYMid()-1, lwd, pN.getYMid()-p1.getYMid()+2, Color.black));
			for (int i = 0; i < nbChildren; i++) {
				MyDrawableNode child = (MyDrawableNode)node.getChild(i);
				MyNodePainter cdr = child.getPainter();
				int ychild = cdr.getYMid();
				canvas.addDrawable(new DrawableRectangle(xpos, ychild-1, cdr.getX()-xpos+2, lwd, Color.black));
				DrawableExpandButton bt = cdr.getExpandButton();
				if (bt != null) {
					bt.setXY(xpos-4, ychild-4);
					canvas.addDrawable(bt);
				}
				addLines(child, canvas);
			}
		}
	}

	public static void addNodes(MyDrawableNode node, DrawableCanvas canvas) {
		MyNodePainter pdr = node.getPainter();
		canvas.addDrawable(pdr);
		int nbChildren = node.getNbFakeChildren();
		if (nbChildren == 0) return;
		for (int i = 0; i < nbChildren; i++) {
			MyDrawableNode child = (MyDrawableNode)node.getChild(i);
			addNodes(child, canvas);
		}
	}

	public static void calcSizes(MyDrawableNode node, Graphics2D g, FontMetrics fm, DrawableCanvas canvas) {
		MyNodePainter paint = node.getPainter();
		paint.calcSize(g, fm, canvas);
		Drawable label = paint.getLabel();
		if (label != null) label.calcSize(g, fm, canvas);
		for (int i = 0; i < node.getNbFakeChildren(); i++) {
			MyDrawableNode child = (MyDrawableNode)node.getChild(i);
			calcSizes(child, g, fm, canvas);
		}
	}
}
