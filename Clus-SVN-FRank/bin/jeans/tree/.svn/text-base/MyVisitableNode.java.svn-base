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

package jeans.tree;

import jeans.util.*;

public class MyVisitableNode extends MyNode {

	public final static long serialVersionUID = 1;

	protected MyVisitorParent m_VisParent;
	protected MyArray m_Visitors;

	public MyVisitableNode() {
		m_VisParent = new MyVisitorParent();
	}

	public MyVisitableNode(MyVisitorParent vpar) {
		m_VisParent = vpar;
	}

	public MyVisitorParent getVisParent() {
		return m_VisParent;
	}

	public int addVisitor() {
		return m_VisParent.addVisitor();
	}

	public void setVisitor(Object visitor, int pos) {
		if (m_Visitors == null) m_Visitors = new MyArray();
		if (pos >= m_Visitors.size()) m_Visitors.setSize(pos + 1);
		m_Visitors.setElementAt(visitor, pos);
	}

	public Object getVisitor(int pos) {
		return m_Visitors.elementAt(pos);
	}

	public void removeVisitor(int pos) {
		m_VisParent.removeVisitor(pos);
		recursiveRemoveVisitor(pos);
	}

	protected void recursiveRemoveVisitor(int pos) {

	}
}
