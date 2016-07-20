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

package clus.gui;

import jeans.graph.swing.drawable.*;
import jeans.graph.tree.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import clus.util.*;
import clus.model.test.*;
import clus.statistic.*;
import clus.algo.tdidt.*;
import clus.gui.statvis.*;

public class ClusNodePainter extends MyNodePainter implements ActionListener {

	protected DrawableExpandButton m_Button;
	protected DrawableLines m_Lines,  m_Label;
	protected Drawable m_StatVis;

	protected final static Font font1 = new Font("Times", Font.PLAIN, 12);
	protected final static Font font2 = new Font("Times", Font.PLAIN, 8);
	protected final static Font font3 = new Font("Times", Font.PLAIN, 10);

	public ClusNodePainter() {
		super(null);
	}

	public ClusNodePainter(MyDrawableNode node) {
		super(node);
		ClusNode cnode = (ClusNode)node.getVisitor(0);
		String[] lines;
		if (cnode.hasBestTest()) {
			NodeTest test = cnode.getTest();
			int nb = test.getNbLines();
//			if (test.getHeuristicValue() == 0.0){
				lines = new String[nb];
				for (int i = 0; i < nb; i++){
					lines[i] = test.getLine(i);
				}
/*			} else {
				lines = new String[nb+1];
				for (int i = 0; i < nb; i++){
					lines[i] = test.getLine(i);
				}
				lines[nb] = "H = "+ClusFormat.FOUR_AFTER_DOT.format(test.getHeuristicValue());
			}*/
			TreePanel panel = (TreePanel)m_Node.getPaintSettings().getDocument();
			ClusStatistic stat = cnode.getClusteringStat();
			m_StatVis = panel.createStatVisualiser(stat);

			m_Button = new DrawableExpandButton(8, 8, m_Node.isFakeLeaf());
			m_Button.setActionListener(this);
		} else {
			ClusStatistic stat = cnode.getClusteringStat();
			lines = new String[1];

			lines[0] = stat.getSimpleString();
			TreePanel panel = (TreePanel)m_Node.getPaintSettings().getDocument();
			m_StatVis = panel.createStatVisualiser(stat);
		}
		m_Label = createLabel(cnode, node);
		m_Lines = new DrawableLines(lines);
	}

	public MyNodePainter createPainter(MyDrawableNode node) {
		return new ClusNodePainter(node);
	}

	public DrawableExpandButton getExpandButton() {
		return m_Button;
	}

	public Drawable getLabel() {
		if (getZoom() <= 1) {
//			return m_Label;
			return null;
		} else {
			return null;
		}
	}

	public void onFakeLeaf(boolean fake) {
		if (m_Button != null) m_Button.setState(fake);
	}

	public boolean mousePressed(DrawableCanvas cnv, int x, int y, MouseEvent evt) {
		try {
			if (evt.isPopupTrigger() || evt.getButton() == MouseEvent.BUTTON2 || evt.getButton() == MouseEvent.BUTTON3) {
				JPopupMenu pop = new JPopupMenu();
				pop.add(makeZoomMenu("Zoom node", false));
				if (!m_Node.atBottomLevel()) {
					pop.add(makeZoomMenu("Zoom subtree", true));
					JMenuItem item = new JMenuItem("Expand subtree");
					item.addActionListener(new MyExpandListener());
					pop.add(item);
					item = new JMenuItem("Prune subtree");
					item.addActionListener(new MyPruneListener());
					pop.add(item);
				}
				JMenuItem item = new JMenuItem("Properties");
				pop.add(item);
				pop.show(cnv, evt.getX(), evt.getY());
			} else {
				TreePanel panel = (TreePanel)m_Node.getPaintSettings().getDocument();
				ClusNode cnode = (ClusNode)m_Node.getVisitor(0);
				panel.showInfo(cnode);
			}
		} catch (ClusException ex) {
			System.err.println("Clus error: "+ex.getMessage());
		}
		return true;
	}

	public JMenu makeZoomMenu(String title, boolean isSubTree) {
		JMenu menu = new JMenu(title);
		JMenuItem item = new JMenuItem("Zoom 0");
		item.addActionListener(new MyZoomListener(0, isSubTree));
		menu.add(item);
		item = new JMenuItem("Zoom 1");
		item.addActionListener(new MyZoomListener(1, isSubTree));
		menu.add(item);
		item = new JMenuItem("Zoom 2");
		item.addActionListener(new MyZoomListener(2, isSubTree));
		menu.add(item);
		item = new JMenuItem("Zoom 3");
		item.addActionListener(new MyZoomListener(3, isSubTree));
		menu.add(item);
		item = new JMenuItem("Zoom 4");
		item.addActionListener(new MyZoomListener(4, isSubTree));
		menu.add(item);
		return menu;
	}

	public boolean mouseSensitive() {
		return true;
	}

	public void calcSize(Graphics2D g, FontMetrics fm, DrawableCanvas cnv) {
		if (getZoom() <= 1) {
			if (getZoom() == 0) {
				/* Large fonts */
				m_Lines.setFont(font1);
				if (m_Label != null) m_Label.setFont(font1);
			} else {
				/* Smaller fonts */
				m_Lines.setFont(font2);
				if (m_Label != null) m_Label.setFont(font2);
			}
			m_Lines.calcSize(g, fm, cnv);
			if (m_Label != null) m_Label.calcSize(g, fm, cnv);
			wd = m_Lines.getWidth();
			hi = m_Lines.getHeight();
			if (m_StatVis != null) {
				if (getZoom() == 0) {
					hi += 12;
				} else {
					hi += 5;
				}
			}
		} else {
			switch (getZoom()) {
				case 2: /* Only number of examples and distribution */
					wd = 50;
					hi = 8;
					ClusNode cnode = (ClusNode)m_Node.getVisitor(0);
					ClusStatistic stat = cnode.getClusteringStat();
					if (stat != null) {
						String totstr = ""+((int)stat.getTotalWeight());
						g.setFont(font3);
						FontMetrics fm2 = g.getFontMetrics();
						wd += fm2.stringWidth(totstr)+6;
						hi = Math.max(hi, fm2.getHeight()+2);
					}
					break;
				case 3: /* Only distribution */
					wd = 50;
					hi = 8;
					break;
				case 4: /* Only color of majority class */
					wd = 10;
					hi = 6;
					break;
			}
		}
	}

	public void draw(Graphics2D g, DrawableCanvas cnv, int xofs, int yofs) {
		super.draw(g, cnv, xofs, yofs);
		if (getZoom() <= 1) {
			m_Lines.setXY(xp, yp);
			m_Lines.draw(g, cnv, xofs, yofs);
			if (m_StatVis != null) {
				int ypos = yp-yofs+m_Lines.getHeight();
				int xpos = xp-xofs;
				if (getZoom() == 0) {
					m_StatVis.setSize(wd-6, 8);
					m_StatVis.setXY(xpos+3, ypos);
				} else {
					m_StatVis.setSize(wd-6, 4);
					m_StatVis.setXY(xpos+3, ypos-2);
				}
				m_StatVis.draw(g, cnv, 0, 0);
			}
		} else {
			int ypos = yp-yofs;
			int xpos = xp-xofs;
			ClusNode cnode = (ClusNode)m_Node.getVisitor(0);
			ClusStatistic stat = cnode.getClusteringStat();
			Color node_color = m_Node.atBottomLevel() ? m_Node.getPaintSettings().LEAF_COLOR : m_Node.getPaintSettings().NODE_COLOR;
			if (getZoom() == 4 || getZoom() == 5) {
				try {
					ClassificationStat cstat = (ClassificationStat)stat;
					node_color = ClassStatVis.getBinColorStatic(cstat.getMajorityClass(0));
				} catch (Exception e) {}
			}
			switch (getZoom()) {
				case 2: /* Only number of examples and distribution */
					if (stat != null && m_StatVis != null) {
						String totstr = ""+((int)stat.getTotalWeight());
						g.setFont(font3);
						g.setColor(Color.black);
						FontMetrics fm2 = g.getFontMetrics();
						int txtwd = fm2.stringWidth(totstr);
						g.drawString(totstr, xpos+3, ypos+fm2.getAscent()+2);
						m_StatVis.setSize(wd-6-txtwd-2, hi-4);
						m_StatVis.setXY(xpos+txtwd+6, ypos+2);
						m_StatVis.draw(g, cnv, 0, 0);
					}
					break;
				case 3: /* Only distribution */
					if (m_StatVis != null) {
						m_StatVis.setSize(wd-4, hi-4);
						m_StatVis.setXY(xpos+2, ypos+2);
						m_StatVis.draw(g, cnv, 0, 0);
					}
					break;
				case 4: /* Only color of majority class */
					g.setColor(node_color);
					g.fillRect(xpos+1, ypos+1, wd-1, hi-1);
					break;
			}
		}
	}

	public void renderTree() {
		TreePanel panel = (TreePanel)m_Node.getPaintSettings().getDocument();
		panel.doRender();
	}

	public void actionPerformed(ActionEvent evt) {
		boolean state = m_Button.getState();
		if (state) {
			m_Node.recursiveFakeLeaf(true);
		} else {
			m_Node.setFakeLeaf(false);
		}
		renderTree();
	}

	private DrawableLines createLabel(ClusNode cnode, MyDrawableNode node) {
		ClusNode parent = (ClusNode)cnode.getParent();
		if (parent != null) {
			NodeTest test = parent.getTest();
			if (test.hasBranchLabels()) {
				String label = test.getBranchLabel(node.getIndex());
				DrawableLines res = new DrawableLines(label);
				res.setBackground(new Color(204,204,204));
				return res;
			}
		}
		return null;
	}

	private class MyPruneListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			TreePanel panel = (TreePanel)m_Node.getPaintSettings().getDocument();
			String str = JOptionPane.showInputDialog(panel, "Enter subtree size");
			try {
				int nodes = Integer.parseInt(str);
				ClusNode node = (ClusNode)m_Node.getVisitor(0);
				panel.pruneTree(node, nodes);
			} catch (NumberFormatException e) {
				System.err.println("Number expected, got: "+str);
			} catch (ClusException e) {
				System.err.println("Clus error: "+e.getMessage());
			}
		}
	}

	private class MyExpandListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			m_Node.recursiveFakeLeaf(false);
			renderTree();
		}
	}

	private class MyZoomListener implements ActionListener {

		protected int m_Zoom;
		protected boolean m_IsSubtree;

		public MyZoomListener(int zoom, boolean subtree) {
			m_Zoom = zoom;
			m_IsSubtree = subtree;
		}

		public void actionPerformed(ActionEvent evt) {
			if (m_IsSubtree) {
				m_Node.recursiveZoom(m_Zoom);
			} else {
				m_Node.setZoom(m_Zoom);
			}
			renderTree();
		}
	}
}
