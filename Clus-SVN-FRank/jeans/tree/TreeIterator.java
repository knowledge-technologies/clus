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

import jeans.util.IntegerStack;

public class TreeIterator {

	private Node node;
	private IntegerStack stack;
	private int index;

	public TreeIterator(Node node) {
		this.index = 0;
		this.node = node;
		this.stack = new IntegerStack();
	}

	public TreeIterator(TreeIterator iter) {
		this.index = iter.index;
		this.node = iter.node;
		this.stack = new IntegerStack(iter.stack);
	}

	public void reset() {
		stack.clear();
		index = 0;
	}

	public boolean atBottomLevel() {
		return node.atBottomLevel();
	}

	public boolean atTopLevel() {
		return stack.isEmpty();
	}

	public Node getNode() {
		return node;
	}

	public int getIndex() {
		return index;
	}

	public int getLevel() {
		return stack.getSize();
	}

	public void goUp() {
		node = node.getParent();
		index = stack.pop();
	}

	public void goDown(int child) {
		stack.push(index);
		node = node.getChild(child);
		index = child;
	}

}
