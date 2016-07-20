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

import java.awt.event.*;
import java.awt.*;

public class WindowClosingListener extends WindowAdapter implements ActionListener {

	public final static int TYPE_EXIT = 0;
	public final static int TYPE_DISPOSE = 1;
	public final static int TYPE_INVISIBLE = 2;

	private int type;
	private Window frame;

	public WindowClosingListener() {
		this.type = TYPE_EXIT;
	}

	public WindowClosingListener(Window frame, boolean hide) {
		this.type = hide ? TYPE_INVISIBLE : TYPE_DISPOSE;
		this.frame = frame;
	}

	public WindowClosingListener(Window frame, int type) {
		this.type = type;
		this.frame = frame;
	}

	public void doClose() {
		if (type == TYPE_EXIT) System.exit(0);
		else if (type == TYPE_DISPOSE) frame.dispose();
		else if (type == TYPE_INVISIBLE) frame.setVisible(false);
	}

	public void windowClosing(WindowEvent e) {
		doClose();
	}

	public void actionPerformed(ActionEvent e) {
		doClose();
	}

}
