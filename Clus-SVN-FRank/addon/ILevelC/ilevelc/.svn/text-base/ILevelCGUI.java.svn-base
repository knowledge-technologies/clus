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

package addon.ILevelC.ilevelc;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import clus.main.Settings;
import clus.util.ClusException;
import clus.data.rows.*;
import clus.data.type.*;
import clus.data.io.*;
import clus.ext.ilevelc.*;

import jeans.util.*;

public class ILevelCGUI extends JFrame {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;
	public final static double LOGSIZE = 500;

	protected JComboBox m_Combo, m_Class;
	protected ILevelCComponent m_Canvas;
	protected ClusSchema m_Schema;
	protected JButton m_Load, m_Save, m_Closure;
	protected JFileChooser m_Chooser;
	protected NominalAttrType m_ClassAttr;

	public ILevelCGUI() throws ClusException, IOException {
		super("ILevelCGUI");
		m_Chooser = new JFileChooser();
		m_Chooser.setCurrentDirectory(new File("."));
		m_Schema = new ClusSchema("2DData");
		m_Schema.addAttrType(new NumericAttrType("X"));
		m_Schema.addAttrType(new NumericAttrType("Y"));
		m_Schema.addAttrType(m_ClassAttr = new NominalAttrType("CLASS",2));
		m_ClassAttr.setValue(0, "pos");
		m_ClassAttr.setValue(1, "neg");
		m_Schema.initialize();
		JPanel panel = new JPanel();
		JPanel top = new JPanel();
		JPanel bot = new JPanel();
		top.setLayout(new BorderLayout(3,3));
		bot.setLayout(new BorderLayout(3,3));
		top.add(m_Combo = new JComboBox(), BorderLayout.CENTER);
		top.add(m_Load = new JButton("Load"), BorderLayout.WEST);
		top.add(m_Save = new JButton("Save"), BorderLayout.EAST);
		bot.add(m_Class = new JComboBox(), BorderLayout.WEST);
		bot.add(m_Closure = new JButton("Compute Closure"), BorderLayout.EAST);
		m_Save.addActionListener(new SaveListener());
		m_Load.addActionListener(new LoadListener());
		m_Closure.addActionListener(new ClosureListener());
		panel.setLayout(new BorderLayout());
		m_Combo.addItem("Add object");
		m_Combo.addItem("Delete object");
		m_Combo.addItem("Add must link");
		m_Combo.addItem("Add cannot link");
		m_Combo.setSelectedIndex(0);
		m_Class.addItem("pos");
		m_Class.addItem("neg");
		m_Class.setSelectedIndex(0);
		panel.add(top, BorderLayout.NORTH);
		panel.add(m_Canvas = new ILevelCComponent(), BorderLayout.CENTER);
		panel.add(bot, BorderLayout.SOUTH);
		m_Canvas.addMouseListener(new ILevelMouseListener());
		setContentPane(panel);
	}

	public int getClassValue() {
		return m_Class.getSelectedIndex();
	}

	public JFileChooser getFileOpen() {
		return m_Chooser;
	}

	public ClusSchema getSchema() {
		return m_Schema;
	}

	public class ILevelCComponent extends JComponent {

		public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

		public int m_FirstIndex = -1;
		public ArrayList m_Points = new ArrayList();
		public ArrayList m_Constraints = new ArrayList();

		public void computeClosure() {
			DerivedConstraintsComputer comp = new DerivedConstraintsComputer(m_Points, m_Constraints);
			comp.compute();
			repaint();
		}

		public void indexPoints() {
			for (int i = 0; i < m_Points.size(); i++) {
				DataTuple tuple = (DataTuple)m_Points.get(i);
				tuple.setIndex(i);
			}
		}

		public void load() {
			int returnVal = getFileOpen().showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				String fname = getFileOpen().getSelectedFile().getAbsolutePath();
				try {
					RowData data = ARFFFile.readArff(fname);
					m_Points = data.toArrayList();
					indexPoints();
					m_Constraints.clear();
					String mname = FileUtil.getName(fname)+".ilevelc";
					ILevelConstraint.loadConstraints(mname, m_Constraints, m_Points);
					repaint();
				} catch (Exception e) {
					System.out.println("Error saving: "+fname);
					System.out.println("Exception: "+e);
					e.printStackTrace();
				}
		    }
		}

		public void save() {
			int returnVal = getFileOpen().showSaveDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				String fname = getFileOpen().getSelectedFile().getAbsolutePath();
				try {
					RowData data = new RowData(m_Points, getSchema());
					ARFFFile.writeArff(fname, data);
					String mname = FileUtil.getName(fname);
					PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mname+".ilevelc")));
					indexPoints();
					wrt.println("TYPE,T1,T2");
					for (int i = 0; i < m_Constraints.size(); i++) {
						ILevelConstraint ic = (ILevelConstraint)m_Constraints.get(i);
						int type = ic.getType();
						DataTuple t1 = ic.getT1();
						DataTuple t2 = ic.getT2();
						wrt.println(String.valueOf(type)+","+t1.getIndex()+","+t2.getIndex());
					}
					wrt.close();
				} catch (Exception e) {
					System.out.println("Error saving: "+fname);
					System.out.println("Exception: "+e);
					e.printStackTrace();
				}
		    }
		}

		public int getClosestPoint(int x, int y) {
			int select = -1;
			double mindist = Double.POSITIVE_INFINITY;
			for (int i = 0; i < m_Points.size(); i++) {
				DataTuple tuple = (DataTuple)m_Points.get(i);
				double ptx = tuple.getDoubleVal(0);
				double pty = tuple.getDoubleVal(1);
				double dist = Math.sqrt((ptx-x)*(ptx-x)+(pty-y)*(pty-y));
				if (dist < mindist) {
					mindist = dist;
					select = i;
				}
			}
			return select;
		}

		public boolean isIn(int x, int y) {
			for (int i = 0; i < m_Points.size(); i++) {
				DataTuple tuple = (DataTuple)m_Points.get(i);
				double ptx = tuple.getDoubleVal(0);
				double pty = tuple.getDoubleVal(1);
				if (ptx == x && pty == y) return true;
			}
			return false;
		}

		public void paintComponent(Graphics g) {
			Dimension dim = getSize();
			for (int i = 0; i < m_Points.size(); i++) {
				DataTuple tuple = (DataTuple)m_Points.get(i);
				int cls = tuple.getIntVal(0);
				if (cls == 0) g.setColor(Color.blue);
				else g.setColor(Color.black);
				g.fillOval((int)(tuple.getDoubleVal(0)*dim.getWidth()/LOGSIZE)-3, (int)(tuple.getDoubleVal(1)*dim.getHeight()/LOGSIZE)-3, 6, 6);
			}
			for (int i = 0; i < m_Constraints.size(); i++) {
				ILevelConstraint ic = (ILevelConstraint)m_Constraints.get(i);
				int type = ic.getType();
				DataTuple t1 = ic.getT1();
				DataTuple t2 = ic.getT2();
				if (type == ILevelConstraint.ILevelCMustLink) g.setColor(Color.green);
				else g.setColor(Color.red);
				g.drawLine((int)(t1.getDoubleVal(0)*dim.getWidth()/LOGSIZE), (int)(t1.getDoubleVal(1)*dim.getHeight()/LOGSIZE), (int)(t2.getDoubleVal(0)*dim.getWidth()/LOGSIZE), (int)(t2.getDoubleVal(1)*dim.getHeight()/LOGSIZE));
			}
		}

		public void addPoint(int x, int y) {
			int option = m_Combo.getSelectedIndex();
			Dimension dim = getSize();
			x = (int)Math.floor(LOGSIZE*x/dim.getWidth());
			y = (int)Math.floor(LOGSIZE*y/dim.getHeight());
			if (option == 0 && !isIn(x, y)) {
				DataTuple tuple = new DataTuple(m_Schema);
				tuple.setDoubleVal(x, 0);
				tuple.setDoubleVal(y, 1);
				tuple.setIntVal(getClassValue(), 0);
				m_Points.add(tuple);
			} else if (option == 1) {
				int sel = getClosestPoint(x, y);
				if (sel != -1) {
					m_Points.remove(sel);
				}
			} else if (option == 2 || option == 3) {
				int sel = getClosestPoint(x, y);
				if (m_FirstIndex == -1) {
					m_FirstIndex = sel;
				} else {
					DataTuple t2 = (DataTuple)m_Points.get(sel);
					DataTuple t1 = (DataTuple)m_Points.get(m_FirstIndex);
					ILevelConstraint cns = new ILevelConstraint(t1, t2, option == 2 ? ILevelConstraint.ILevelCMustLink : ILevelConstraint.ILevelCCannotLink);
					m_Constraints.add(cns);
					m_FirstIndex = -1;
				}
			}
			repaint();
		}
	}

	public class SaveListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			m_Canvas.save();
		}
	}

	public class LoadListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			m_Canvas.load();
		}
	}

	public class ClosureListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			m_Canvas.computeClosure();
		}
	}

	public class ILevelMouseListener extends MouseAdapter {

		public void mousePressed(MouseEvent e) {
			m_Canvas.addPoint((int)e.getPoint().getX(), (int)e.getPoint().getY());
		}
	}

	public static void main(String[] args) {
		try {
			ILevelCGUI gui = new ILevelCGUI();
			gui.setSize(new Dimension(800, 600));
			gui.setVisible(true);
		} catch (ClusException e) {
			System.out.println("Exception: "+e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: "+e);
			e.printStackTrace();
		}
	}
}
