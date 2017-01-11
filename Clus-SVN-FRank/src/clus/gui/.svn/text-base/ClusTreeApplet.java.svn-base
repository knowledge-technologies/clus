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
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

import clus.main.Settings;

public class ClusTreeApplet extends JApplet {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected JFrame m_Frame;
	protected JButton m_Launch;

	public void init() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EtchedBorder());
		String name = getParameter("Label");
		panel.add(m_Launch = new JButton(name == null ? "Show" : name));
		m_Launch.addActionListener(new MyClick());
		setContentPane(panel);
	}

	private class MyClick implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			if (m_Frame == null) {
				m_Launch.setText("Loading Tree...");
				m_Launch.setEnabled(false);
				MyThread thread = new MyThread();
				thread.start();
			} else {
				m_Frame.setVisible(true);
			}
		}
	}

	private class MyWindowListener extends WindowAdapter {

		public void windowClosing(WindowEvent e) {
			m_Frame.setVisible(false);
			m_Launch.setEnabled(true);
			String name = getParameter("Label");
			m_Launch.setText(name == null ? "Show" : name);
		}
	}

	private class MyThread extends Thread {

		public void run() {
			try {
				URL url = new URL(getDocumentBase(), getParameter("Tree"));
//				String type = getParameter("Type");
//				if (type != null && type.equals("tilde")) {
					m_Frame = SimpleTreeFrame.loadTildeTree(url.openStream());
//				} else {
//					m_Frame = TreeFrame.loadTree(url.openStream());
//				}
				m_Frame.addWindowListener(new MyWindowListener());
				m_Frame.setVisible(true);
				m_Launch.setText("Done !");
			} catch (MalformedURLException e) {
				m_Launch.setText("URL Error: "+e.getMessage());
			} catch (IOException e) {
				m_Launch.setText("IO Error: "+e);
			} catch (ClassNotFoundException e) {
				m_Launch.setText("Class Not Found Error: "+e.getMessage());
			}
		}
	}
}
