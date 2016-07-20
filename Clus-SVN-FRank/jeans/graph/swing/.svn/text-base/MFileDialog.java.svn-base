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

package jeans.graph.swing;

import jeans.resource.MediaCache;
import jeans.graph.PercentLayout;
import jeans.graph.WindowClosingListener;

import jeans.io.filesys.MFileSystem;
import jeans.io.filesys.MFileEntry;
import jeans.util.thread.MCallback;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.*;

public class MFileDialog extends JDialog implements MCallback {

	public final static long serialVersionUID = 1;

	protected JPanel m_hPanel;
	protected JLabel m_hTitleB;
	protected JButton m_hOkButton;
	protected JButton m_hCancelButton;
	protected JTextField m_hFile;
	protected MyList m_hDirList;
	protected MyTable m_hFileTable;
	protected Border m_hEtchedBorder = BorderFactory.createEtchedBorder();
        protected JScrollPane m_hDirScroll, m_hFileScroll;
	protected WindowClosingListener m_hWindowListener;
	protected MFileSystem m_hFileSystem;
	protected MyTableModel m_hTableModel;
	protected MyListModel m_hListModel;
	protected MyTableSorter m_hSorter;
	protected String m_EntrySequence;
	protected Hashtable m_hIcons = new Hashtable();
	protected ImageIcon m_hDefaultIcon;
	protected ActionListener m_hOnOK;

	public MFileDialog(JFrame parent, String titleA, String titleB, String titleOK) {
		super(parent, titleA, true);
		setContentPane(m_hPanel = createPanel(titleB, titleOK));
		pack();
	}

	public MFileDialog(JFrame parent) {
		this(parent, "", "", "");
	}

	public void setOKListener(ActionListener listener) {
		m_hOnOK = listener;
	}

	public String getFileName() {
		return m_hFile.getText().trim();
	}

	public MFileEntry getFileEntry() {
		String name = getFileName();
		return m_hFileSystem.getEntry(name);
	}

	public void addWindowClosingListener(WindowClosingListener listener) {
		m_hWindowListener = listener;
		addWindowListener(listener);
		m_hCancelButton.addActionListener(listener);
	}

	public void setStrings(String titleA, String titleB, String titleOK) {
		setTitle(titleA);
		m_hTitleB.setText(titleB);
		m_hOkButton.setText(titleOK);
	}

	public void addIcon(String ex, ImageIcon im) {
		m_hIcons.put(ex, im);
	}

	public void setDefaultIcon(ImageIcon icon) {
		m_hDefaultIcon = icon;
	}

	public void setFileSystem(MFileSystem sys) {
		m_hFileSystem = sys;
		m_hFileSystem.setCallback(this);
		m_hFileSystem.reload(MFileSystem.OP_RELOAD);
	}

	public MFileSystem getFileSystem() {
		return m_hFileSystem;
	}

	public void callBack(Object result, int type) {
		switch (type) {
			case MFileSystem.OP_RELOAD:
			case MFileSystem.OP_CHDIR:
				m_hListModel.fireDataChanged();
        			m_hTableModel.fireTableDataChanged();
				m_hDirList.setSelectedIndex(0);
				m_hFileTable.setRowSelectionInterval(0, 0);
				break;
		}
	}

	public void dispose() {
		for (int i = 0; i < 5; i++) {
			TableColumn column = m_hFileTable.getColumnModel().getColumn(i);
			System.out.println("Column width "+i+": "+column.getWidth());
		}
		super.dispose();
	}

	public void setSingleSelection(boolean sel) {
		if (sel) m_hFileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		else m_hFileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public JPanel createPanel(String titleB, String titleOK) {
		JPanel panel = new JPanel();
		panel.setLayout(new PercentLayout("p 2d 100% p p", 5, PercentLayout.ALL, true));
		panel.add(m_hTitleB = new JLabel(titleB));

		JPanel p1 = new JPanel();
		p1.setLayout(new PercentLayout("30% 70%", 5, 0, false));
		m_hDirList = new MyList(m_hListModel = new MyListModel());
		m_hDirList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_hDirList.setCellRenderer(new MImageCellRenderer(MediaCache.getInstance().getImage("dir.gif"), m_hDirList));
		m_hDirList.addKeyListener(new MyDirKeyListener());
		m_hDirList.addMouseListener(new MyDirMouseListener());
		m_hDirScroll = new JScrollPane(m_hDirList);
		p1.add(m_hDirScroll);
		m_hSorter = new MyTableSorter(m_hTableModel = new MyTableModel());
		m_hFileTable = new MyTable(m_hSorter);
		m_hSorter.addMouseListenerToHeaderInTable(m_hFileTable);
		m_hFileTable.setPreferredScrollableViewportSize(new Dimension(450, 200));
		m_hFileTable.addFocusListener(new MyFocusListener());
		m_hFileTable.getSelectionModel().addListSelectionListener(new MySelectionListener());
		m_hFileScroll = new JScrollPane(m_hFileTable);
		p1.add(m_hFileScroll);
		panel.add(p1);

		for (int i = 0; i < 5; i++) {
			TableColumn column = m_hFileTable.getColumnModel().getColumn(i);
                   	switch (i) {
                   		case 0: column.setPreferredWidth(20);
                   			break;
                   		case 1: column.setPreferredWidth(100);
                   			break;
				case 2:
                   		case 3: column.setPreferredWidth(40);
                   			break;
                   		case 4: column.setPreferredWidth(60);
                   			break;
                   	}
		}

		JPanel p2 = new JPanel();
		p2.setLayout(new PercentLayout("p 100%", 5, PercentLayout.ALL, false));
		p2.setBorder(m_hEtchedBorder);
		p2.add(new JLabel("File:"));
		p2.add(m_hFile = new JTextField());
		panel.add(p2);

		JPanel p3 = new JPanel();
		p3.setLayout(new PercentLayout("p 100%d p", 5, 0, false));
		MediaCache cache = MediaCache.getInstance();
		p3.add(m_hOkButton = new JButton(titleOK, new ImageIcon(cache.getImage("ok.gif"))));
		m_hOkButton.addActionListener(new OkListener());
		p3.add(m_hCancelButton = new JButton("Cancel", new ImageIcon(cache.getImage("cancel.gif"))));
		panel.add(p3);

		return panel;
	}

	public void requestInputFocus() {
		m_hFile.requestFocus();
	}

	public void selectFile(ListSelectionModel lsm) {
		if (lsm.isSelectionEmpty()) {
			m_hFile.setText("");
               	} else {
               		boolean isFirst = true;
               		String value = null;
               		int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {
					String name = m_hFileTable.getValueAt(i, 1).toString();
					if (isFirst) {
						value = name;
						isFirst = false;
					} else {
						value += ", " + name;
					}
				}
			}
			if (value != null) m_hFile.setText(value);
		}
	}

	public void chSelectedDir() {
		int idx = m_hDirList.getSelectedIndex();
		if (idx != -1) {
			String dir = m_hFileSystem.getDirectoryAt(idx);
			m_hFileSystem.chdir(dir, MFileSystem.OP_CHDIR);
			m_EntrySequence = null;
			return;
		}
	}

	public class MyDirMouseListener extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) chSelectedDir();
		}
	}

	public class MyDirKeyListener extends KeyAdapter {

		public void keyReleased(KeyEvent e) {
			int keycode = e.getKeyCode();
			if (keycode == KeyEvent.VK_RIGHT || keycode == KeyEvent.VK_ENTER) {
				chSelectedDir();
				return;
			}
			if (keycode == KeyEvent.VK_LEFT) {
				m_hFileSystem.chdir("..", MFileSystem.OP_CHDIR);
				m_EntrySequence = null;
				return;
			}
			if (keycode == KeyEvent.VK_ESCAPE) {
				dispose();
				return;
			}
			int ch = e.getKeyChar();
			if (ch >= 'A' && ch <= 'Z') ch = ch + 'a' - 'A';
			if (Character.isLetterOrDigit((char)ch) || ch == '-' || ch == '_') {
				if (m_EntrySequence == null) m_EntrySequence = String.valueOf((char)ch);
				else m_EntrySequence += String.valueOf((char)ch);
				int seqlen = m_EntrySequence.length();
				int len = m_hFileSystem.getNbDirectories();
				for (int i = 0; i < len; i++) {
					String dir = m_hFileSystem.getDirectoryAt(i);
					String cmp = dir.length() > seqlen ? dir.substring(0, seqlen) : dir;
					if (m_EntrySequence.equals(cmp.toLowerCase())) {
						m_hDirList.setSelectedValue(dir, true);
						return;
					}
				}
			}
			m_EntrySequence = null;
		}
	}

	public class OkListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			m_hOnOK.actionPerformed(e);
		}
	}

	public class MySelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) return;
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			selectFile(lsm);
        	}
	}

	public class MyFocusListener extends FocusAdapter {

		public void focusGained(FocusEvent e) {
			m_hFileTable.setColumnSelectionInterval(1, 1);
			ListSelectionModel lsm = m_hFileTable.getSelectionModel();
			selectFile(lsm);
		}
	}

	public class MyList extends JList {

		public final static long serialVersionUID = 1;

		public MyList(ListModel lm) {
			super(lm);
		}

		public boolean isManagingFocus() {
			return true;
		}
	}

	public class MyTable extends JTable {

		public final static long serialVersionUID = 1;

		public MyTable(TableModel tm) {
			super(tm);
		}

		public boolean isManagingFocus() {
			return false;
		}
	}

	public class MyListModel extends AbstractListModel {

		public final static long serialVersionUID = 1;

		protected int m_iPrevSize;

 		public Object getElementAt(int index) {
 			return m_hFileSystem.getDirectoryAt(index);
 		}

		public int getSize() {
			return m_hFileSystem == null ? 0 : m_hFileSystem.getNbDirectories();
		}

		public void fireDataChanged() {
			int size = getSize();
			m_hDirList.setSelectedIndex(0);
			if (size < m_iPrevSize) fireIntervalRemoved(this, size, m_iPrevSize-1);
			if (size > m_iPrevSize) fireIntervalAdded(this, m_iPrevSize, size-1);
			int max = Math.min(size, m_iPrevSize)-1;
			if (max > 0) fireContentsChanged(this, 0, max);
			m_iPrevSize = size;
		}
	}

	public class MyTableModel extends AbstractTableModel {

		public final static long serialVersionUID = 1;

		final String[] columnNames = {"", "Name", "Time", "Date", "Size"};

		public int getColumnCount() {
                      return columnNames.length;
		}

		public int getRowCount() {
                      return m_hFileSystem == null ? 0 : m_hFileSystem.getNbFiles();
		}

		public String getColumnName(int col) {
                      return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			MFileEntry entry = m_hFileSystem.getFileAt(row);
			if (col == 0) {
				String ex = entry.getExtension();
				ImageIcon ic = (ImageIcon)m_hIcons.get(ex);
				if (ic != null) return ic;
				else return m_hDefaultIcon;
			}
			if (col == 1) return entry.getName();
			if (col == 2) return entry.getTimeString();
			if (col == 3) return entry.getDateString();
			if (col == 4) return entry.getLengthString();
			return "";
		}

		public Class getColumnClass(int col) {
			if (col == 0) return ImageIcon.class;
                      	else return String.class;
		}

		public boolean isCellEditable(int row, int col) {
                      if (col == 1) return true;
                      else return false;
		}

		public void setValueAt(Object value, int row, int col) {
			//data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
	}

	public class MyTableSorter extends AbstractTableModel implements TableModelListener {

		public final static long serialVersionUID = 1;

		TableModel model;
		int indexes[];
    		int sortingColumn = 1;
    		boolean ascending = true;

		public MyTableSorter(TableModel model) {
			setModel(model);
		}

		public void setModel(TableModel model) {
			this.model = model;
			model.addTableModelListener(this);
			reallocateIndexes();
		}

		public int compareRowsByColumn(int row1, int row2, int column) {
			MFileEntry ent1 = m_hFileSystem.getFileAt(row1);
			MFileEntry ent2 = m_hFileSystem.getFileAt(row2);
			return ent1.compareTo(ent2, column);
		}

		public int compare(int row1, int row2) {
                	int result = compareRowsByColumn(row1, row2, sortingColumn);
                	return ascending ? result : -result;
                }

            	public void reallocateIndexes() {
			int rowCount = model.getRowCount();
		        indexes = new int[rowCount];
		        for(int row = 0; row < rowCount; row++)
		        	indexes[row] = row;
    		}

		public void tableChanged(TableModelEvent e) {
			reallocateIndexes();
			fireTableChanged(e);
    		}

		public void sort() {
        		shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
    		}

		public void shuttlesort(int from[], int to[], int low, int high) {
			if (high - low < 2) return;
			int middle = (low + high)/2;
			shuttlesort(to, from, low, middle);
			shuttlesort(to, from, middle, high);
			int p = low;
			int q = middle;
			if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
				for (int i = low; i < high; i++) to[i] = from[i];
				return;
			}
			for(int i = low; i < high; i++) {
				if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) to[i] = from[p++];
			    	else to[i] = from[q++];
			}
    		}

		public Object getValueAt(int aRow, int aColumn) {
			return model.getValueAt(indexes[aRow], aColumn);
		}

		public void setValueAt(Object aValue, int aRow, int aColumn) {
			model.setValueAt(aValue, indexes[aRow], aColumn);
		}

		public int getRowCount() {
			return (model == null) ? 0 : model.getRowCount();
		}

		public int getColumnCount() {
			return (model == null) ? 0 : model.getColumnCount();
		}

		public String getColumnName(int aColumn) {
			return model.getColumnName(aColumn);
		}

		public Class getColumnClass(int aColumn) {
			return model.getColumnClass(aColumn);
		}

		public boolean isCellEditable(int row, int column) {
			return model.isCellEditable(row, column);
		}

		public void sortByColumn(int column, boolean ascending) {
			this.ascending = ascending;
			sortingColumn = column;
        		sort();
			fireTableDataChanged();
		}

		public void addMouseListenerToHeaderInTable(JTable table) {
			m_hFileTable.getTableHeader().addMouseListener(new MyMouseListener());
		}

		public class MyMouseListener extends MouseAdapter {

			public void mouseClicked(MouseEvent e) {
				TableColumnModel columnModel = m_hFileTable.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = m_hFileTable.convertColumnIndexToModel(viewColumn);
				if(e.getClickCount() == 1 && column != -1) {
					int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
					boolean ascending = (shiftPressed == 0);
					sortByColumn(column, ascending);

				}
			}
		}
	}
}
