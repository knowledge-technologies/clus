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

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import clus.algo.tdidt.ClusNode;
import clus.ext.hierarchical.*;

import java.util.*;

import clus.main.*;
import clus.statistic.*;

public class ShowHierarchy extends JPanel {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClusNode m_RootNode;
    protected ClassHierarchy m_Hier;
    protected JTHierTreeNode m_JTNode;
    protected Hashtable m_ToolTips;
    protected JTree m_JTree;
    protected boolean[] m_Expanded;

    double[] m_RCnts;
    double m_RWeight;
    double[] m_RVars;
    double[] m_NCnts;
    double m_NWeight;
    double m_Fac = 1.0, m_Sig = 0.5;

    public ShowHierarchy(ClusNode rootClass, ClassHierarchy hier) {
	m_Hier = hier;
	m_RootNode = rootClass;
	// m_ToolTips = loadToolTips("funcat.txt");
	setRootCounts(rootClass);
	m_Expanded = new boolean[hier.getTotal()];
	m_JTNode = createJTree(hier.getRoot());
	m_JTree = new JTree(m_JTNode);
	ToolTipManager.sharedInstance().registerComponent(m_JTree);
	m_JTree.setCellRenderer(new MyRenderer());
	m_JTree.addTreeExpansionListener(new MyExpansionListener());
	JScrollPane scroll = new JScrollPane(m_JTree);
	setLayout(new BorderLayout());
	add(scroll, BorderLayout.CENTER);
    }

    public void setFac(double fac) {
	m_Fac = fac;
    }

    public void setSig(double sig) {
    	m_Sig = sig;
    }

    public void renewTree() {
	m_JTNode.removeAllChildren();
	createSignificantJTree(m_Hier.getRoot(), m_JTNode);
	m_JTree.setModel(new DefaultTreeModel(m_JTNode));
	doExpansion(m_JTNode);
    }

    // Only update probability bars
    public void updateScreen() {
	updateScreen(m_JTNode);
    }

    public void updateScreen(JTHierTreeNode node) {
	m_JTree.getModel().valueForPathChanged(new TreePath(node.getPath()), node.getUserObject());
	for (Enumeration e = node.children(); e.hasMoreElements(); ) {
	    JTHierTreeNode subnode = (JTHierTreeNode)e.nextElement();
	    updateScreen(subnode);
	}
    }

    private void doExpansion(JTHierTreeNode node) {
    	int nb = node.getChildCount();
	for (int i = 0; i < nb; i++) {
		JTHierTreeNode child = (JTHierTreeNode)node.getChildAt(i);
		ClassTerm term = (ClassTerm)child.getUserObject();
		if (m_Expanded[term.getIndex()]) {
			m_JTree.expandPath(new TreePath(child.getPath()));
		}
		doExpansion(child);
	}
    }

    private JTHierTreeNode createSignificantJTree(ClassTerm term, JTHierTreeNode node) {
	node.setUserObject(term);
	int nb = term.getNbChildren();
	for (int i = 0; i < nb; i++) {
	    ClassTerm child = (ClassTerm)term.getChild(i);
	    // needs a more efficient implementation (new attribute of ClassTerm ??)
	    if (significant(child, m_Sig))
	    	node.add(createSignificantJTree(child, new JTHierTreeNode()));
	}
	return node;
    }

    private boolean significant(ClassTerm term, double zValue) {
	if (zValue(term)>zValue) return true;
	else {
	    boolean sign = false;
	    int nb = term.getNbChildren();
	    for (int i = 0; i < nb; i++) {
		if (significant((ClassTerm)term.getChild(i),zValue)) {
		    sign = true;
		    break;
		}
	    }
	return sign;
	}
    }


    private JTHierTreeNode createJTree(ClassTerm term) {
	JTHierTreeNode node = new JTHierTreeNode(term);
	int nb = term.getNbChildren();
	for (int i = 0; i < nb; i++) {
	    ClassTerm child = (ClassTerm)term.getChild(i);
	    node.add(createJTree(child));
	}
	return node;
    }

    public ClassHierarchy getHier() {
	return m_Hier;
    }

    public ClusNode getRootNode(){
	return m_RootNode;
    }

    public void setRootCounts(ClusNode root) {
	m_RCnts = getCounts(root);
	m_RWeight = getWeight(root);
	m_RVars = getVariances(root);
    }

    public void setNodeCounts(ClusNode node) {
	m_NCnts = getCounts(node);
	m_NWeight = getWeight(node);
    }

    /*
    private Hashtable loadToolTips(String fname) {
	Hashtable tooltips = new Hashtable();
	try {
	    MStreamTokenizer tokens = new MStreamTokenizer(fname);
	    String token = tokens.getToken();
	    while (token != null) {
		int cnt = 0;
		StringBuffer mbuf = new StringBuffer();
		StringTokenizer stok = new StringTokenizer(token, "/");
		while (stok.hasMoreTokens() && cnt != -1) {
		    String nxt = stok.nextToken();
		    if (nxt.equals("0")) {
			cnt = -1;
		    } else {
			if (cnt != 0) mbuf.append("/");
			mbuf.append(nxt);
			cnt++;
		    }
		}
		String val = tokens.readTillEol();
		tooltips.put(mbuf.toString(), val);
		token = tokens.getToken();
	    }
	} catch (IOException e) {
	    System.out.println("IO error: "+e.getMessage());
	}
	return tooltips;
    }
    */

    /*	public void toScreen() {
	pack();
	setSize(500,500);
	setVisible(true);
	}*/


    //public static double[] getCounts(ClusNode node, ClassHierarchy hier) {
    public static double[] getCounts(ClusNode node) {
	//HierStatistic stat = (HierStatistic)node.getClusteringStat();
	//double[] accu = hier.calcAccumulated(stat.getCounts(), stat.m_SumWeight);
	//		hier.makeRelative(accu);
	return null;
    }

    public static double getWeight(ClusNode node) {
	ClusStatistic stat = (ClusStatistic)node.getClusteringStat();
	return stat.m_SumWeight;
    }

    public static double[] getVariances(ClusNode node) {
	/*HierStatistic stat = (HierStatistic)node.getClusteringStat();
	double[] arr = stat.getVariances();
	if (arr == null) return null;
	else return MDoubleArray.clone(stat.getVariances());*/
    	return null;
    }


    private double zValue(ClassTerm child) {
	int index = child.getIndex();
	//System.out.println("------------------");
	//System.out.println(child.toString());
	double zValue = 0.0;
	if (m_RVars != null) {
	        zValue = Math.abs(((m_RCnts[index]/m_RWeight)-(m_NCnts[index]/m_NWeight))/Math.sqrt(m_RVars[index]));
	} else {
		zValue = Double.MAX_VALUE;
	}
	//System.out.println(zValue);
        return zValue;
    }


    protected Color[] COLOR_BOUNDS =
    {Color.black, Color.blue, Color.green, Color.red};

    public Color createColor(double val) {
	int idx = 0;
	double fac = (double)1.0/(COLOR_BOUNDS.length-1);
	double lb = 0;
	double ub = fac;
	while (val > ub) {
	    idx++;
	    lb = ub;
	    ub = (idx+1)*fac;
	}
	if (idx >= COLOR_BOUNDS.length-1) return Color.red;
	double perc = (val - lb)/fac;
	double red = (1-perc)*COLOR_BOUNDS[idx].getRed()+perc*COLOR_BOUNDS[idx+1].getRed();
	double green = (1-perc)*COLOR_BOUNDS[idx].getGreen()+perc*COLOR_BOUNDS[idx+1].getGreen();
	double blue = (1-perc)*COLOR_BOUNDS[idx].getBlue()+perc*COLOR_BOUNDS[idx+1].getBlue();
	return new Color((int)red, (int)green, (int)blue);
    }

    private class MyExpansionListener implements TreeExpansionListener {

    	public void treeCollapsed(TreeExpansionEvent event) {
		JTHierTreeNode node = (JTHierTreeNode)event.getPath().getLastPathComponent();
		m_Expanded[((ClassTerm)node.getUserObject()).getIndex()] = false;
	}

	public void treeExpanded(TreeExpansionEvent event) {
		JTHierTreeNode node = (JTHierTreeNode)event.getPath().getLastPathComponent();
		m_Expanded[((ClassTerm)node.getUserObject()).getIndex()] = true;
	}
    }

    private class MyRenderer extends DefaultTreeCellRenderer {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected final static int DX = 24;
	protected String m_String;
	protected ClassTerm m_Term;

	public Dimension getPreferredSize() {
	    Dimension d = super.getPreferredSize();
	    //			return new Dimension(d.width+5+12*2, d.height);
	    int half = (d.height+1)/2;
	    return new Dimension(d.width+6+80, 2*half);
	}

	public Color getColor(int which) {
	    if (which == 0) {
		return createColor(m_RCnts[m_Term.getIndex()] * m_Fac);
	    } else {
		if (m_NCnts == null) return Color.black;
		else return createColor(m_NCnts[m_Term.getIndex()] * m_Fac);
	    }
	}

	public int getWidth(double val) {
	    return (int)Math.min((double)val*m_Fac*80.0, 80.0);
	}

	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Dimension d = super.getPreferredSize();
	    g.setColor(Color.blue);
    	    int half = (d.height+1)/2;
	    int hi = half-4;
	    int idx  = m_Term.getIndex();
	    double rRelCnt = m_RCnts[idx]/m_RWeight;
	    int wd1 = getWidth(rRelCnt);
	    g.fillRect(d.width+3,4,wd1,hi);
	    if (m_NCnts != null) {
		double nRelCnt = m_NCnts[idx]/m_NWeight;
		if (m_RVars != null) {
			double zValue = Math.abs(rRelCnt - nRelCnt)/Math.sqrt(m_RVars[idx]);
			if (zValue > m_Sig)
			    g.setColor(Color.red);
			else
			    g.setColor(Color.gray);
		} else {
			g.setColor(Color.red);
		}
		int wd2 = getWidth(nRelCnt);
		g.fillRect(d.width+3,5+hi,wd2,hi);
	    }
	    g.setColor(Color.black);
	    g.drawRect(d.width+1, 2, 80+4, 2*half-4);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	    JTHierTreeNode node = (JTHierTreeNode)value;
	    m_Term = (ClassTerm)node.getUserObject();
	    m_String = m_Term.toString();
//	    String tip = (String)m_ToolTips.get(m_String);
//	    setToolTipText(tip);
	    return this;
	}
    }
}
