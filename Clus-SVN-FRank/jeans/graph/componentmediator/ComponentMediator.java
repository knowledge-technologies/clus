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
import java.awt.event.*;

public class ComponentMediator {

	private static ComponentMediator instance;

	private Hashtable groups = new Hashtable();
	private Hashtable components = new Hashtable();
	private ComponentInterface componentInterface;
	private boolean enabledAll = true;
	private ActionListener myListener = new MyActionListener();

	protected ComponentMediator() {
		//Singleton pattern
	}

	public static ComponentMediator getInstance() {
		if (instance == null)
			instance = new ComponentMediator();
		return instance;
	}

	public void resetAll() {
		if (!components.isEmpty()) {
			groups = new Hashtable();
			components = new Hashtable();
		}
	}

	public Enumeration getGroupNames() {
		return groups.keys();
	}

	public GroupWrapper getGroup(String name) {
		return (GroupWrapper)groups.get(name);
	}

	public ComponentWrapper getWrapper(Component comp) {
		return (ComponentWrapper)components.get(comp);
	}

	public Enumeration getComponents() {
		return components.elements();
	}

	public ComponentWrapper addComponent(Component component) {
		ComponentWrapper wrapper = null;
		if (!components.containsKey(component)) {
			wrapper = new ComponentWrapper(component);
			components.put(component, wrapper);
		} else {
			wrapper = getWrapper(component);
		}
		if (enabledAll == false) {
			setInGroupEnabled(wrapper, enabledAll);
		}
		return wrapper;
	}

	public ComponentWrapper addComponent(String group, ComponentWrapper wrapper) {
		if (!groups.containsKey(group)) {
			GroupWrapper newList = new GroupWrapper();
			newList.addElement(wrapper);
			groups.put(group, newList);
		} else {
			GroupWrapper list = (GroupWrapper)groups.get(group);
			list.addElement(wrapper);
		}
		return wrapper;
	}

	public ComponentWrapper addComponent(String group, Component component) {
		ComponentWrapper wrapper = addComponent(component);
		addComponent(group, wrapper);
		return wrapper;
	}

	public void addSpecialComponent(String group, ComponentWrapper wrapper) {
		components.put(wrapper, wrapper);
		addComponent(group,wrapper);
	}

	public void setEnabled(String group, boolean enable) {
		GroupWrapper mygroup = getGroup(group);
		if (mygroup.shouldSetEnabled(enable)) {
			for (Enumeration e = mygroup.elements(); e.hasMoreElements(); ) {
				ComponentWrapper comp = (ComponentWrapper)e.nextElement();
				setInGroupEnabled(comp, enable);
			}
		}
	}

	public void setEnabled(ComponentWrapper comp, boolean enable) {
		if (comp.shouldSetEnabled(enable))
			componentInterface.setEnabled(comp, enable);
	}

	private void setInGroupEnabled(ComponentWrapper comp, boolean enable) {
		if (comp.groupShouldSetEnabled(enable))
			componentInterface.setEnabled(comp, enable);
	}

	public void onCheckSetEnabled(ComponentWrapper source, String group, boolean invert) {
		if (!source.hasEvents())
			componentInterface.addActionListener(source, myListener);
		OnCheckSetEnabled action = new OnCheckSetEnabled(source, group, invert);
		source.addAction(action);
	}

	public void setEnabledAll(boolean enable) {
		if (enabledAll != enable) {
			enabledAll = enable;
			for (Enumeration e = getComponents(); e.hasMoreElements(); ) {
				ComponentWrapper comp = (ComponentWrapper)e.nextElement();
				setInGroupEnabled(comp, enable);
			}
		}
	}

	public void setCheck(ComponentWrapper comp, boolean check) {
		componentInterface.setCheck(comp, check);
	}

	public void setCheck(String group, boolean check) {
		for (Enumeration e = getGroup(group).elements(); e.hasMoreElements(); ) {
			ComponentWrapper comp = (ComponentWrapper)e.nextElement();
			setCheck(comp, check);
		}
	}

	public boolean isCheck(ComponentWrapper comp) {
		return componentInterface.isCheck(comp);
	}

	public void setComponentInterface(ComponentInterface componentInterface) {
		this.componentInterface = componentInterface;
	}

	public void performActions(ComponentWrapper wrapper) {
		for (Enumeration e = wrapper.getActions(); e.hasMoreElements(); ) {
			ComponentAction action = (ComponentAction)e.nextElement();
			action.execute(this);
		}
	}

	private class MyActionListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			Component source = (Component)evt.getSource();
			ComponentWrapper wrapper = getWrapper(source);
			if (wrapper != null) performActions(wrapper);
		}

	}

}

class GroupWrapper extends Vector {

	public final static long serialVersionUID = 1;

	private boolean enabled = true;

	public boolean shouldSetEnabled(boolean enable) {
		if (enabled == enable) return false;
		enabled = enable;
		return true;
	}

}


