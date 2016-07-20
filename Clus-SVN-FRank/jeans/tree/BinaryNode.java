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

public class BinaryNode implements Node {

	String m_Name;
	Object m_Cargo;
	BinaryNode m_Left, m_Right;
	Node m_Parent;
	boolean m_bFakeLeaf;

	public void setName(String name) {
		m_Name = name;
	}

	public int getLevel() {
		int depth = 0;
		BinaryNode node = this;
		while (!node.atTopLevel()) {
			node = (BinaryNode)node.getParent();
			depth++;
		}
		return depth;
	}

	public void setFakeLeaf(boolean fake) {
		m_bFakeLeaf = fake;
	}

	public boolean isFakeLeaf() {
		return m_bFakeLeaf;
	}

	public String getName() {
		return m_Name;
	}

	public void setCargo(Object cargo) {
		m_Cargo = cargo;
	}

	public Object getCargo() {
		return m_Cargo;
	}

	public void setLeft(BinaryNode node) {
		m_Left = node;
		node.setParent(this);
	}

	public BinaryNode getLeft() {
		return m_Left;
	}

	public void setRight(BinaryNode node) {
		m_Right = node;
		node.setParent(this);
	}

	public BinaryNode getRight() {
		return m_Right;
	}

	public Node getChild(int idx) {
		if (idx == 0) return m_Left;
		else return m_Right;
	}

	public int getNbChildren() {
		return atBottomLevel() ? 0 : 2;
	}

	public void addChild(Node node) {
	}

	public void removeChild(Node node) {
		if (node == m_Left) m_Left = null;
		if (node == m_Right) m_Right = null;
	}

	public void setParent(Node parent) {
		m_Parent = parent;
	}

	public Node getParent() {
		return m_Parent;
	}

	public boolean atTopLevel() {
		return m_Parent == null;
	}

	public boolean atBottomLevel() {
		return m_bFakeLeaf || atRealBottomLevel();
	}

	public boolean atRealBottomLevel() {
		return m_Left == null && m_Right == null;
	}

	public static int maxHeight(BinaryNode node) {
		if (node.atRealBottomLevel()) {
			return 0;
		} else {
			int ld = maxHeight(node.getLeft());
			int rd = maxHeight(node.getRight());
			return Math.max(ld, rd)+1;
		}
	}

	public static int getHeight(BinaryNode node) {
		if (node.atBottomLevel()) {
			return 0;
		} else {
			int ld = getHeight(node.getLeft());
			int rd = getHeight(node.getRight());
			return Math.max(ld, rd)+1;
		}
	}
}
