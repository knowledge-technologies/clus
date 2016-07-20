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

import java.io.*;
import jeans.util.*;
import jeans.graph.*;
import jeans.io.*;

import clus.main.*;
import clus.algo.tdidt.ClusNode;
import clus.data.type.*;
import clus.model.modelio.tilde.*;

public class SimpleTreeFrame extends JFrame {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int INITIAL_WD = 800;
	public final static int INITIAL_HI = 600;

	TreePanel m_TreePanel;

	public SimpleTreeFrame(String title, TreePanel tpanel) {
		super(title);
		m_TreePanel = tpanel;
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(3,3));
		panel.add(m_TreePanel, BorderLayout.CENTER);
		setContentPane(panel);
	}

	public void init() {
		pack();
		setSize(INITIAL_WD, INITIAL_HI);
	}

	public static SimpleTreeFrame createFrame(String title, ClusNode root) {
		String[] lines = new String[0];
		TreePanel tpanel = new TreePanel(root, lines);
		SimpleTreeFrame frame = new SimpleTreeFrame(title, tpanel);
		frame.init();
		return frame;
	}

	public static SimpleTreeFrame loadTree(InputStream strm) throws IOException, ClassNotFoundException {
		ObjectLoadStream open = new ObjectLoadStream(strm);
		ClusSchema schema = (ClusSchema)open.readObject();
		ClusNode root = (ClusNode)open.readObject();
		open.close();
		return createFrame(schema.getRelationName(), root);
	}

	public static SimpleTreeFrame loadTildeTree(InputStream strm) throws IOException, ClassNotFoundException {
		TildeOutReader reader = new TildeOutReader(strm);
		reader.doParse();
		ClusNode root = reader.getTree();
		reader.close();
		return createFrame("TildeTree", root);
	}

	public static SimpleTreeFrame showTree(String fname) throws IOException, ClassNotFoundException {
		SimpleTreeFrame frame;
		if (FileUtil.getExtension(fname).equals("out")) {
			frame = loadTildeTree(new FileInputStream(fname));
		} else {
			frame = loadTree(new FileInputStream(fname));
		}
		frame.addWindowListener(new WindowClosingListener(frame, WindowClosingListener.TYPE_EXIT));
		frame.setVisible(true);
		return frame;
	}
}
