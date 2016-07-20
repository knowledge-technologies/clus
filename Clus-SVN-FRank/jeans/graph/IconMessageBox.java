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

package jeans.graph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class IconMessageBox extends Dialog {

	public final static long serialVersionUID = 1;

	public final static int MB_OK = 1;
	public final static int MB_CANCEL = 2;

	protected int m_action = MB_CANCEL;
	protected ActionListener m_listener = null;

	public IconMessageBox(Frame parent, String title, String lines, int buttons) {
		this(parent, title, lines, null, buttons);
	}

	public IconMessageBox(Frame parent, String title, String lines, Image icon, int buttons) {
		super(parent, title, true);
		add(makePanel(icon, lines, buttons));
		addWindowListener(new WindowClosingListener(this, false));
		pack();
	}

	public IconMessageBox(Frame parent, String title) {
		super(parent, title, true);
	}

	public void setCenter() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension mySize = getSize();
		setLocation(screenSize.width/2-mySize.width/2, screenSize.height/2-mySize.height/2);
	}

	public void setActionListener(ActionListener listener) {
		m_listener = listener;
	}

	public void performAction(int action) {
		m_action = action;
		if (m_listener != null)
			m_listener.actionPerformed(new ActionEvent(this,m_action,""));
	}

	public int getAction() {
		return m_action;
	}

	protected int getNbButtons(int buttons) {
		int nb = 0;
		int ps = 0;
		while (ps < 5 && nb < 5) {
			int and = 1 << ps;
			if ((buttons & and) != 0) nb++;
			ps++;
		}
		return nb;
	}

	protected Vector getLines(String lines) {
		Vector res = new Vector();
		StringTokenizer tokens = new StringTokenizer(lines, "\n");
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			res.addElement(token);
		}
		return res;
	}

	protected Panel makePanel(Image icon, String lines, int buttons) {
		Panel panel = new Panel();
		panel.setBackground(SystemColor.control);
		if (icon == null) {
			panel.setLayout(new PercentLayout("100% p", 3, PercentLayout.ALL, false));
			panel.add(makeStringPanel(lines));
			panel.add(makeButtonPanel(buttons));
		} else {
			panel.setLayout(new PercentLayout("p 100% p", 3, PercentLayout.ALL, false));
			panel.add(new ImageViewer(icon, ImageViewer.SUNKEN3D));
			panel.add(makeStringPanel(lines));
			panel.add(makeButtonPanel(buttons));
		}
		return panel;
	}

	protected Panel makeStringPanel(String lines) {
		Panel panel = new Panel();
		String layout = "";
		Vector msgs = getLines(lines);
		int nb = msgs.size();
		for (int ctr = 0; ctr < nb; ctr++)
			layout += "p ";
		panel.setLayout(new PercentLayout(layout+"100%d", 3, 0, true));
		for (int ctr = 0; ctr < nb; ctr++) {
			String string = (String)msgs.elementAt(ctr);
			Label label = new Label(string);
			panel.add(label);
		}
		return panel;
	}

	protected Panel makeButtonPanel(int buttons) {
		Panel panel = new Panel();
		String layout = "";
		int nb = getNbButtons(buttons);
		for (int ctr = 0; ctr < nb; ctr++)
			layout += "p ";
		panel.setLayout(new PercentLayout(layout+"100%d", 3, 0, true));
		if ((buttons & MB_OK) != 0) {
			Button m_okButton = new Button("OK");
			ButtonListener listener = new ButtonListener(MB_OK);
			m_okButton.addActionListener(listener);
			panel.add(m_okButton);
		}
		if ((buttons & MB_CANCEL) != 0) {
			Button m_cancelButton = new Button("CANCEL");
			ButtonListener listener = new ButtonListener(MB_CANCEL);
			m_cancelButton.addActionListener(listener);
			panel.add(m_cancelButton);
		}
		return panel;
	}

	protected class ButtonListener implements ActionListener {

		protected int m_id = 0;

		protected ButtonListener(int id) {
			m_id = id;
		}

		public void actionPerformed(ActionEvent evt) {
			performAction(m_id);
			dispose();
		}

	}

}











