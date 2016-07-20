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

import java.io.Serializable;

import java.io.*;

public class MyNode implements Node, Serializable {

	public final static long serialVersionUID = 1;

	protected MyArray m_Children = new MyArray();
	protected Node m_Parent;

	public MyNode() {
	}

	public MyNode(Node parent) {
		m_Parent = parent;
	}

	public MyNode getRoot() {
		if (m_Parent == null) return this;
		else return ((MyNode)m_Parent).getRoot();
	}

	public final int[] getPath() {
		Node node = this;
		int d = getLevel();
		int[] path = new int[d];
		for (int i = 0; i < d; i++) {
			Node parent = node.getParent();
			path[i] = ((MyNode)parent).indexOf(node);
			node = parent;
		}
		return path;
	}


	public final MyNode fromPath(int[] path, int skip) {
		MyNode crnode = this;
		for (int i = path.length-1-skip; i >= 0; i--) {
			int pos = path[i];
			if (pos < 0 || pos >= crnode.getNbChildren()) {
				return null;
			} else {
				crnode = (MyNode)crnode.getChild(pos);
			}
		}
		return crnode;
	}

	public final int indexOf(Node child) {
		for (int i = 0; i < getNbChildren(); i++) {
			if (child == getChild(i)) return i;
		}
		return -1;
	}

	public boolean equalsPath(int[] path) {
		int[] mypath = getPath();
		if (mypath.length != path.length) return false;
		for (int i = 0; i < path.length; i++)
			if (mypath[i] != path[i]) return false;
		return true;
	}

	public static void showPath(int[] path, PrintWriter out) {
		for (int i = 0; i < path.length; i++) {
			if (i != 0) out.print(",");
			out.print(path[i]);
		}
	}

	public static void showPath(int[] path) {
		for (int i = 0; i < path.length; i++) {
			if (i != 0) System.out.print(",");
			System.out.print(path[i]);
		}
	}

	public MyNode cloneNode() {
		return new MyNode();
	}


	public final void addChild(Node node) {
		node.setParent(this);
		m_Children.addElement(node);
	}

	public final void setChild(Node node, int idx) {
		node.setParent(this);
		m_Children.setElementAt(node, idx);
	}

	public final void removeChild(Node node) {
		node.setParent(null);
		m_Children.removeElement(node);
	}

	public final void removeChild(int idx) {
		MyNode child = (MyNode)getChild(idx);
		if (child != null) child.setParent(null);
		m_Children.removeElementAt(idx);
	}

	public final void removeAllChildren() {
		int nb = getNbChildren();
		for (int i = 0; i < nb; i++) {
			Node node = getChild(i);
			node.setParent(null);
		}
		m_Children.removeAllElements();
	}

	public final Node getParent() {
		return m_Parent;
	}

	public final void setParent(Node parent) {
		m_Parent = parent;
	}

	public final Node getChild(int idx) {
		return (Node)m_Children.elementAt(idx);
	}

	public final int getNbChildren() {
		return m_Children.size();
	}

	public final void setNbChildren(int nb) {
		m_Children.setSize(nb);
	}

	public final boolean atTopLevel() {
		return m_Parent == null;
	}

	public final boolean atBottomLevel() {
		return m_Children.size() == 0;
	}

	public final MyNode cloneTree() {
		MyNode clone = cloneNode();
		int arity = getNbChildren();
		clone.setNbChildren(arity);
		for (int i = 0; i < arity; i++) {
			MyNode node = (MyNode)getChild(i);
			clone.setChild(node.cloneTree(), i);
		}
		return clone;
	}

	public final MyNode cloneTree(MyNode n1, MyNode n2) {
		if (n1 == this) {
			return n2;
		} else {
			MyNode clone = cloneNode();
			int arity = getNbChildren();
			clone.setNbChildren(arity);
			for (int i = 0; i < arity; i++) {
				MyNode node = (MyNode)getChild(i);
				clone.setChild(node.cloneTree(n1, n2), i);
			}
			return clone;
		}
	}

	public final int getLevel() {
		int depth = 0;
		Node node = getParent();
		while (node != null) {
			depth++;
			node = node.getParent();
		}
		return depth;
	}

	public final int getMaxLeafDepth() {
		int nb = getNbChildren();
		if (nb == 0) {
			return 1;
		} else {
			int max = 0;
			for (int i = 0; i < nb; i++) {
				MyNode node = (MyNode)getChild(i);
				max = Math.max(max, node.getMaxLeafDepth());
			}
			return max + 1;
		}
	}

	public final int getNbNodes() {
		int count = 1;
		int nb = getNbChildren();
		for (int i = 0; i < nb; i++) {
			MyNode node = (MyNode)getChild(i);
			count += node.getNbNodes();
		}
		return count;
	}

	public final int getNbLeaves() {
		int nb = getNbChildren();
		if (nb == 0) {
			return 1;
		} else {
			int count = 0;
			for (int i = 0; i < nb; i++) {
				MyNode node = (MyNode)getChild(i);
				count += node.getNbLeaves();
			}
			return count;
		}
	}
}
