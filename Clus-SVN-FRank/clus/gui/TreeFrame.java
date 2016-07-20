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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import clus.ext.hierarchical.*;

import java.io.*;
import jeans.util.*;
import jeans.graph.*;

import clus.main.*;
import clus.util.*;
import clus.model.ClusModelInfo;
import clus.model.modelio.*;
import clus.algo.ClusInductionAlgorithmType;
import clus.algo.tdidt.ClusNode;
import clus.data.attweights.*;
import clus.data.type.*;

public class TreeFrame extends JFrame {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int INITIAL_WD = 800;
	public final static int INITIAL_HI = 600;

	public final static int[] m_Permute = {1,2,0};

	double m_VPerc = 0.75;
	double m_HPerc = 0.8;

	JSplitPane m_HSplit, m_VSplit, m_HRSplit;
	ShowHierarchy m_ShowHier;
	TreePanel m_TreePanel;

	JTextField m_Stat;
	JTextArea m_Info;
	JSlider m_Fac, m_Sig;
	JCheckBoxMenuItem m_Rel, m_Horz;
	JTree m_Tree;
	DefaultTreeModel m_TreeModel;
	DefaultMutableTreeNode m_Root;
	JList m_DSList;
	DefaultListModel m_DSListModel;
	JTextArea m_TextArea = new JTextArea();
	JMenuItem m_Find, m_Open, m_ShowSett;
	JFileChooser m_FileChoose = new JFileChooser();
	TreeMap m_Files = new TreeMap();

	public TreeFrame(String title, TreePanel tpanel, ShowHierarchy sh) {
		super(title);
		m_ShowHier = sh;
		m_TreePanel = tpanel;
		JPanel panel = new JPanel();
		//		m_HSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_TreePanel, makeBottomPanel());
		//		m_VSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_HSplit, makeRightPanel());
		//		m_VSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new MySplitListener(1));
		panel.setLayout(new BorderLayout(3,3));
		m_TextArea.setEditable(false);
		m_HSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_TreePanel, m_TextArea);
		m_VSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_HSplit, makeRightPanel());
		panel.add(m_VSplit, BorderLayout.CENTER);
		//		panel.add(m_TreePanel, BorderLayout.CENTER);
		setContentPane(panel);
		setJMenuBar(createMenu());
		addComponentListener(new MyResizeListener());
		m_FileChoose.setCurrentDirectory(FileUtil.getCurrentDir());
	}

	public JMenuBar createMenu() {
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		//file.add(m_Open = new JMenuItem("Open Data Set"));
		//m_Open.addActionListener(new MyOpenListener());
		file.add(m_Find = new JMenuItem("Find Data Sets"));
		m_Find.addActionListener(new MyFindListener());
		JMenu sett = new JMenu("Settings");
		sett.add(m_Horz = new JCheckBoxMenuItem("Horizontal"));
		m_Horz.addActionListener(new MyHorzListener());
		m_Horz.setSelected(true);
		sett.add(m_ShowSett = new JMenuItem("Show settings"));
		m_ShowSett.addActionListener(new MyShowSettingsListener());
		menu.add(file);
		menu.add(sett);
		return menu;
	}

	public void init() {
		pack();
		setSize(INITIAL_WD, INITIAL_HI);
	}

	public void setDividers(int wd, int hi) {
		int vval = (int)(m_VPerc*wd);
		int hval = (int)(m_HPerc*hi);
		m_VSplit.setDividerLocation(vval);
		m_HSplit.setDividerLocation(hval);
		m_HRSplit.setDividerLocation((int)(0.25*hi));
	}

	public JPanel makeRightPanel() {
		JPanel panel = new JPanel();
		if (m_ShowHier != null) {
			panel.setLayout(new PercentLayout("100% p p p p", 3, PercentLayout.ALL, true));
			panel.add(m_ShowHier);
			//			m_Rel = new JCheckBox("Relative");
			//			panel.add(m_Rel);
			panel.add(new JLabel("Scale factor"));
			panel.add(m_Fac = new JSlider(JSlider.HORIZONTAL, 0, 100, 10));
			m_Fac.addChangeListener(new MyScaleListener());
			panel.add(new JLabel("Significance level"));
			panel.add(m_Sig = new JSlider(JSlider.HORIZONTAL, 0, 100, 50));
			m_Sig.addChangeListener(new MySignificanceListener());
		} else {
			m_Root = new DefaultMutableTreeNode("Root");
			m_TreeModel = new DefaultTreeModel(m_Root);
			m_Tree = new JTree(m_TreeModel);
			// m_Tree.setShowsRootHandles(false);
			// m_Tree.setRootVisible(false);
			m_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			m_Tree.addTreeSelectionListener(new MyFileTreeListener());
			JScrollPane treepane = new JScrollPane(m_Tree);
			m_DSListModel = new DefaultListModel();
			m_DSList = new JList(m_DSListModel);
			m_DSList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			m_DSList.addListSelectionListener(new MyListListener());
			JScrollPane listpane = new JScrollPane(m_DSList);
			m_HRSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listpane, treepane);
			panel.setLayout(new PercentLayout("100%", 3, PercentLayout.ALL, true));
			panel.add(m_HRSplit);
		}
		return panel;
	}

	public JPanel makeBottomPanel() {
		JPanel panel = new JPanel();

		JPanel spanel = new JPanel();
		spanel.setLayout(new PercentLayout("p 100%", 3, PercentLayout.ALL, false));
		spanel.add(new JLabel("Statistic: "));
		spanel.add(m_Stat = new JTextField());
		m_Stat.setEditable(false);

		JPanel ipanel = new JPanel();
		ipanel.setLayout(new PercentLayout("p 100%", 3, PercentLayout.ALL, false));
		JPanel sub = new JPanel();
		sub.setLayout(new PercentLayout("p 100%d", 3, 0, true));
		sub.add(new JLabel("Info: "));
		ipanel.add(sub);
		ipanel.add(m_Info = new JTextArea());
		m_Info.setEditable(false);
		m_Info.setFont(new Font("MonoSpaced", Font.PLAIN, 12));

		panel.setLayout(new BorderLayout(3,3));
		panel.add(spanel, BorderLayout.NORTH);
		panel.add(ipanel, BorderLayout.CENTER);

		return panel;
	}

	public DefaultMutableTreeNode getParentFNode(String[] path) {
		int pos = 0;
		DefaultMutableTreeNode curr = m_Root;
		while (pos < path.length-1) {
			String pstr = path[pos];
			boolean found = false;
			for (int i = 0; i < curr.getChildCount(); i++) {
				DefaultMutableTreeNode ch = (DefaultMutableTreeNode)curr.getChildAt(i);
				if (pstr.equals(ch.getUserObject())) {
					found = true;
					curr = ch;
					break;
				}
			}
			if (!found) {
				int ipos = 0;
				while (ipos < curr.getChildCount() &&
						cmpName(curr.getChildAt(ipos).toString(), pstr)) {
					ipos++;
				}
				DefaultMutableTreeNode ch = new DefaultMutableTreeNode(pstr);
				m_TreeModel.insertNodeInto(ch, curr, ipos);
				curr = ch;
			}
			pos++;
		}
		return curr;
	}

	public static boolean isNumber(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i))) return false;
		}
		return true;
	}

	public boolean cmpName(String s1, String s2) {
		if (isNumber(s2) && isNumber(s1)) {
			try {
				return Integer.parseInt(s1) <= Integer.parseInt(s2);
			} catch (NumberFormatException e) {
				return false;
			}
		} else {
			return s1.compareTo(s2) <= 0;
		}
	}

	public void openDataSet(String file) throws IOException {
		String path = (String)m_Files.get(file);
		if (path != null) {
			openDir(path, file);
		}
	}

	public String[] doPermute(String[] input) {
		String[] res = new String[input.length];
		for (int i = 0; i < input.length; i++) {
			res[i] = input[i];
		}
		int max = 0;
		for (int i = 0; i < m_Permute.length; i++) {
			max = Math.max(max, m_Permute[i]);
		}
		if (max < input.length && m_Permute.length <= input.length) {
			for (int i = 0; i < m_Permute.length; i++) {
				res[i] = input[m_Permute[i]];
			}
		}
		return res;
	}

	public String correctUnderscores(String fn, String ds) {
		if (ds != null) {
			int pos = fn.indexOf(ds);
			if (pos != -1) {
				StringBuffer res = new StringBuffer(fn);
				for (int j = 0; j < ds.length(); j++) {
					if (res.charAt(pos+j) == '-') {
						res.setCharAt(pos+j, '_');
					}
				}
				return res.toString();
			}
		}
		return fn;
	}

	public DefaultMutableTreeNode addFile(String full, String ds) {
		String fn = FileUtil.removePath(full);
		fn = correctUnderscores(fn, ds);
		String[] name = FileUtil.getName(fn).split("\\-");
		String chname = name[name.length-1];
		DefaultMutableTreeNode parent = getParentFNode(name);
		ClusFileTreeElem elem = new ClusFileTreeElem(chname, full);
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(elem);
		int ipos = 0;
		while (ipos < parent.getChildCount() &&
			cmpName(parent.getChildAt(ipos).toString(), chname)) {
			ipos++;
		}
		m_TreeModel.insertNodeInto(child, parent, ipos);
		return child;
	}

	public void openDir(String dir, String ds) throws IOException {
		for (int i = m_Root.getChildCount()-1; i >= 0; i--) {
			DefaultMutableTreeNode ch = (DefaultMutableTreeNode)m_Root.getChildAt(i);
			m_TreeModel.removeNodeFromParent(ch);
		}
		File dir_file = new File(dir);
		ArrayList files = new ArrayList();
		System.out.println("Searching for models in: "+dir);
		FileUtil.recursiveFindAll(dir_file, ".model", files);
		FileUtil.recursiveFindAll(dir_file, ".tree", files);
		for (int i = 0; i < files.size(); i++) {
			String full = (String)files.get(i);
			addFile(full, ds);
		}
		for (int i = 0; i < m_Root.getChildCount(); i++) {
			DefaultMutableTreeNode ch = (DefaultMutableTreeNode)m_Root.getChildAt(i);
			m_Tree.expandPath(new TreePath(ch.getPath()));
			System.out.println("Expanding: "+ch);
		}
	}

	public void setTree(ClusNode root, ClusStatManager mgr) throws ClusException {
		m_TreePanel.setTree(root, true);
		if (mgr != null) m_TreePanel.setStatManager(mgr);
		showInfo(root);
	}

	public void showInfo(ClusNode root) throws ClusException {
		StringBuffer buf = new StringBuffer();
		buf.append("Size: "+root.getModelSize()+" (Leaves: "+root.getNbLeaves()+")\n");
		ClusAttributeWeights scale = m_TreePanel.createClusAttributeWeights();
		String relerr = ClusFormat.SIX_AFTER_DOT.format(root.estimateError(scale));
		String abserr = ""+root.estimateErrorAbsolute(scale);
		buf.append("Examples: "+root.getClusteringStat().m_SumWeight+"\n");
		buf.append("Error: "+relerr+" ("+abserr+") ss = "+root.estimateClusteringSS(scale)+" ("+scale.getName()+")\n");
		buf.append("Statistic: "+root.getClusteringStat());
		m_TextArea.setText(buf.toString());
	}

	public void showSettings() {
		try {
			ClusStatManager mgr = m_TreePanel.getStatManager();
			Settings sett = mgr.getSettings();
			PrintWriter wrt = new PrintWriter(new OutputStreamWriter(System.out));
			sett.show(wrt);
			wrt.flush();
		} catch (IOException ex) {
			System.err.println("IOError: "+ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void addModelList(ClusModelCollectionIO io, ClusFileTreeElem elem, DefaultMutableTreeNode node) {
		int pos = 0;
		elem.setObject1(io);
		for (int i = 0; i < io.getNbModels(); i++) {
			ClusModelInfo m = (ClusModelInfo)io.getModelInfo(i);
			ClusFileTreeElem celem = new ClusFileTreeElem(m.getName(), "");
			celem.setObject1(m);
			celem.setType(ClusInductionAlgorithmType.REGULAR_TREE);
			DefaultMutableTreeNode ch = new DefaultMutableTreeNode(celem);
			m_TreeModel.insertNodeInto(ch, node, pos++);
		}
	}

	public void loadModelType(ClusFileTreeElem elem, DefaultMutableTreeNode node) throws ClusException {
		ClusModelInfo m = (ClusModelInfo)elem.getObject1();
		if (m.getModel() instanceof ClusNode) {
			ClusNode root = (ClusNode)m.getModel();
			root.updateTree();
			setTree(root, m.getStatManager());
		}
	}

	public void loadModelType2(ClusFileTreeElem elem) throws ClusException {
		ClusModelCollectionIO io = (ClusModelCollectionIO)elem.getObject1();
		if (io.getNbModels() > 0) {
			ClusModelInfo m = (ClusModelInfo)io.getModelInfo(0);
			if (m.getModel() instanceof ClusNode) {
				ClusNode root = (ClusNode)m.getModel();
				root.updateTree();
				setTree(root, m.getStatManager());
			}
		}
	}

	public void loadModel(DefaultMutableTreeNode node) throws ClusException {
		ClusFileTreeElem elem = (ClusFileTreeElem)node.getUserObject();
		try {
			if (elem.getType() != -1) {
				loadModelType(elem, node);
				return;
			}
			System.out.println("Name: "+elem.getFullName());
			ClusModelCollectionIO io = ClusModelCollectionIO.load(elem.getFullName());
			addModelList(io, elem, node);
			loadModelType2(elem);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Can't open model: "+elem.getFullName());
			System.err.println("IOError: "+e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Error "+e.getMessage());
			e.printStackTrace();
		}
	}

	private class MyScaleListener implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider)e.getSource();
			if (!source.getValueIsAdjusting()) {
				int val = (int)source.getValue();
				m_ShowHier.setFac((double)val/10.0);
				m_ShowHier.updateScreen();
			}
		}
	}

	private class MySignificanceListener implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider)e.getSource();
			if (!source.getValueIsAdjusting()) {
				int val = (int)source.getValue();
				m_ShowHier.setSig((double)val/100.0);
				m_ShowHier.renewTree();
			}
		}
	}

	private class MyHorzListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JCheckBoxMenuItem source = (JCheckBoxMenuItem)e.getSource();
			m_TreePanel.setHorzVert(source.isSelected());
		}
	}

	private class MyFileTreeListener implements TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent e) {
			try {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)m_Tree.getLastSelectedPathComponent();
				if (node == null) return;
				if (node.isLeaf()) {
					loadModel(node);
				} else if (node.getUserObject() instanceof ClusFileTreeElem) {
					ClusFileTreeElem elem = (ClusFileTreeElem)node.getUserObject();
					loadModelType2(elem);
				}
			} catch (ClusException ex) {
				System.err.println("Clus error: "+ex.getMessage());
			}
		}
	}

	/*
	 private class MySplitListener implements PropertyChangeListener {

	 protected MySplitListener(int which) {
	 }

	 public void  propertyChange(PropertyChangeEvent evt)  {
	 System.out.println(evt.getNewValue());
	 }
	 }
	 */
	private class MyResizeListener implements ComponentListener {

		public void componentHidden(ComponentEvent e) {}

		public void componentMoved(ComponentEvent e) {}

		public void componentResized(ComponentEvent e) {
			Dimension d = ((JFrame)e.getSource()).getSize();
			setDividers(d.width, d.height);
		}

		public void componentShown(ComponentEvent e) {}
	}

	private class MyShowSettingsListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			showSettings();
		}
	}

	private class MyListListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				String file = (String)m_DSList.getSelectedValue();
				if (file != null) {
					try {
						openDataSet(file);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(TreeFrame.this, "Error: "+ex.getMessage());
					}
				}
			}
		}
	}

	private class MyFindListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			m_FileChoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int res = m_FileChoose.showOpenDialog(TreeFrame.this);
			if (res == JFileChooser.APPROVE_OPTION) {
				try {
					File file = m_FileChoose.getSelectedFile();
					System.out.println("Opening: " + file.getName());
					ArrayList list = FileUtil.recursiveFind(file, ".s");
					for (int i = 0; i < list.size(); i++) {
						String full = (String)list.get(i);
						String dsname = FileUtil.getName(FileUtil.removePath(full));
						String dspath = FileUtil.getPath(full);
						m_Files.put(dsname, dspath);
						System.out.println("Name = "+dsname);
					}
					m_DSListModel.clear();
					Iterator iter = m_Files.keySet().iterator();
					while (iter.hasNext()) {
						String name = (String)iter.next();
						m_DSListModel.addElement(name);
					}
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(TreeFrame.this, "Error: "+ex.getMessage());
				}
			}
		}
	}

	public static TreeFrame createFrame(ClusStatManager manager, ClusNode root, ClassHierarchy hier) {
		TreeFrame frame;
		String[] lines = new String[0];
		TreePanel tpanel = new TreePanel(root, lines);
		tpanel.setStatManager(manager);
		if (manager == null) {
			frame = new TreeFrame("Clus", tpanel, null);
		} else {
			ClusSchema schema = manager.getSchema();
			if (hier != null) {
				ShowHierarchy sh = new ShowHierarchy(root, hier);
				frame = new TreeFrame(schema.getRelationName(), tpanel, sh);
			} else {
				frame = new TreeFrame(schema.getRelationName(), tpanel, null);
			}
		}
		tpanel.setFrame(frame);
		frame.init();
		frame.setDividers(INITIAL_WD, INITIAL_HI);
		return frame;
	}


	public static TreeFrame showTree(String fname) throws ClusException, IOException, ClassNotFoundException {
		TreeFrame frame = createFrame(null, null, null);
		frame.addWindowListener(new WindowClosingListener(frame, WindowClosingListener.TYPE_EXIT));
		DefaultMutableTreeNode node = frame.addFile(fname, null);
		frame.loadModel(node);
		frame.setVisible(true);
		return frame;
	}


	public static TreeFrame start(ClusStatManager manager, String opendir) throws IOException {
		TreeFrame frame = createFrame(manager, null, null);
		frame.addWindowListener(new WindowClosingListener(frame, WindowClosingListener.TYPE_EXIT));
		frame.openDir(opendir, null);
		frame.setVisible(true);
		return frame;
	}
}
