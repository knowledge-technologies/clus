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

import java.util.*;
import java.awt.*;

public class ComponentWrapper {

	private Component comp;
	private int enable_cnt;
	private Vector my_events;
	private boolean my_enable = true;

	public ComponentWrapper(Component comp) {
		this.comp = comp;
	}

	public Component getComponent() {
		return comp;
	}

	public boolean groupShouldSetEnabled(boolean enable) {
		if (enable) {
			if (enable_cnt > 0 && (--enable_cnt == 0)) return true;
		} else {
			if (enable_cnt++ == 0) return true;
		}
		return false;
	}

	public boolean shouldSetEnabled(boolean enable) {
		if (my_enable == enable) return false;
		my_enable = enable;
		return groupShouldSetEnabled(enable);
	}

	public boolean hasEvents() {
		return my_events != null;
	}

	public void addAction(ComponentAction action) {
		if (!hasEvents()) my_events = new Vector();
		my_events.addElement(action);
	}

	public Enumeration getActions() {
		return my_events.elements();
	}

	public void output() {
		System.out.println("Component: "+comp+" Count: "+enable_cnt);
	}

}
