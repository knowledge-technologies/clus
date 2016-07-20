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

package jeans.graph.awt;

import java.awt.*;
import java.awt.event.*;

public class SwitchButton extends Button implements ActionListener {

	public final static long serialVersionUID = 1;

	private boolean state;
	private String normal, selected;

	public SwitchButton(String normal, String selected) {
		super(normal);
		this.normal = normal;
		this.selected = selected;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent evt) {
		setState(!state);
	}

	public void setState(boolean mystate) {
		state = mystate;
		if (state) setLabel(selected);
		else setLabel(normal);
	}

	public boolean getState() {
		return state;
	}

}
