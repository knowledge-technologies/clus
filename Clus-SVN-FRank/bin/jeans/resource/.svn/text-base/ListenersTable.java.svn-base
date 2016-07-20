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

package jeans.resource;

import java.util.*;
import java.awt.event.*;

public class ListenersTable {

	protected static ListenersTable instance = null;
	protected Hashtable listeners = new Hashtable();

	public static ListenersTable getInstance() {
		if (instance == null)
			instance = new ListenersTable();
		return instance;
	}

	protected ListenersTable() {
	}

	public void addActionListener(String key, ActionListener listener) {
		listeners.put(key,listener);
	}

	public ActionListener getActionListener(String key) {
		return (ActionListener)listeners.get(key);
	}

	public void addItemListener(String key, ItemListener listener) {
		listeners.put(key,listener);
	}

	public ItemListener getItemListener(String key) {
		return (ItemListener)listeners.get(key);
	}

}
