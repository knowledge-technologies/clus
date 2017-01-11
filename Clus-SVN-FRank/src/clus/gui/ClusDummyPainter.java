/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package clus.gui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.SystemColor;

import clus.algo.tdidt.ClusNode;
import clus.jeans.graph.swing.drawable.Drawable;
import clus.jeans.graph.swing.drawable.DrawableCanvas;
import clus.jeans.graph.swing.drawable.DrawableLines;
import clus.jeans.graph.tree.MyDrawableNode;
import clus.jeans.graph.tree.MyNodePainter;
import clus.model.test.NodeTest;


public class ClusDummyPainter extends MyNodePainter {

    protected Drawable m_Label;


    public ClusDummyPainter(ClusNode node, int idx) {
        super(null);
        m_Label = createLabel(node, idx);
    }


    public MyNodePainter createPainter(MyDrawableNode node) {
        return null;
    }


    public Drawable getLabel() {
        return m_Label;
    }


    public void calcSize(Graphics2D g, FontMetrics fm, DrawableCanvas cnv) {
        if (m_Label != null)
            m_Label.calcSize(g, fm, cnv);
        wd = 20;
        hi = 0;
    }


    public void draw(Graphics2D g, DrawableCanvas cnv, int xofs, int yofs) {
    }


    private Drawable createLabel(ClusNode parent, int idx) {
        NodeTest test = parent.getTest();
        if (test.hasBranchLabels()) {
            String label = test.getBranchLabel(idx);
            DrawableLines res = new DrawableLines(label);
            res.setBackground(SystemColor.control);
            return res;
        }
        else {
            return null;
        }
    }
}
