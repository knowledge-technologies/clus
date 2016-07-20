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

package jeans.graph.componentmediator;

import java.awt.*;
import java.awt.event.*;

public abstract class AWTComponentInterface implements ComponentInterface {

	public void setEnabled(Component comp, boolean enable) {
		comp.setEnabled(enable);
	}

	public void setCheck(Component comp, boolean check) {
		if (comp instanceof Checkbox)
			((Checkbox)comp).setState(check);
	}

	public boolean isCheck(Component comp) {
		if (comp instanceof Checkbox)
			return ((Checkbox)comp).getState();
		else
			return false;
	}

	public void addActionListener(ComponentWrapper cw, ActionListener listener) {
		Component comp = cw.getComponent();
		if (comp instanceof Checkbox)
			((Checkbox)comp).addItemListener(new MyItemListener(listener));
		else if  (comp instanceof Button)
			((Button)comp).addActionListener(listener);
	}

	private class MyItemListener implements ItemListener {

		private ActionListener mylistener;

		private MyItemListener(ActionListener listener) {
			mylistener = listener;
		}

		public void itemStateChanged(ItemEvent evt) {
			mylistener.actionPerformed(new ActionEvent(evt.getSource(), evt.getID(), ""));
		}

	}
}





