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

public class HorizTreeIterator {

	private int childps[];
	private Node stack[];
	private int pos, level, rootdepth;
	private boolean tonext = true;
	private boolean done = false;

	//Level relative to root depth
	public HorizTreeIterator(Node tree, int depth, int level) {
		this.level = level;
		childps = new int[level+1];
		stack = new Node[level+1];
		rootdepth = depth;
		pos = 0;
		push(0, tree);
		climbTillLevel();
	}

	public int getRootDepth() {
		return rootdepth;
	}

	public Node getNextNode() {
		if (!tonext) gotoNextNode();
		tonext = false;
		return prevNode();
	}

	public boolean isDone() {
		if (!tonext) gotoNextNode();
		return done;
	}

	public Node getParent() {
		if (pos < 2) return null;
		else return stack[pos-2];
	}

	//Absolute level
	public Node getNodeAtLevel(int level) {
		int mylevel = level-getRootDepth();
		if (mylevel < 0) return null;
		return stack[mylevel];
	}

	//Absolute level
	public int getChildAtLevel(int level) {
		return childps[level-getRootDepth()];
	}

	private void gotoNextNode() {
		if (backTrackTillChild())
			climbTillLevel();
		tonext = true;
	}

	private boolean backTrackTillChild() {
		boolean res = true;
		pop();
		if (isEmpty()) {
			res = false;
		} else {
			Node node = prevNode();
			int nbchild = node.getNbChildren();
			int child = prevNum()+1;
			pop();
			while (!isEmpty() && (child >= nbchild)) {
				node = prevNode();
				nbchild = node.getNbChildren();
				child = prevNum()+1;
				pop();
			}
			push(child, node);
			if (child < nbchild) push(0, node.getChild(child));
			else res = false;
		}
		if (!res) done = true;
		return res;
	}

	private void climbTillLevel() {
		Node parent = prevNode();
		while (pos <= level && parent != null) {
			if (!parent.atBottomLevel()) {
				parent = parent.getChild(0);
				push(0, parent);
			} else {
				parent = null;
			}
		}
		if (parent == null) gotoNextNode();
	}

	private boolean isEmpty() {
		return pos == 0;
	}

	private void push(int num, Node node) {
		childps[pos] = num;
		stack[pos] = node;
		pos++;
	}

	private Node prevNode() {
		return stack[pos-1];
	}

	private int prevNum() {
		return childps[pos-1];
	}

	private void pop() {
		pos--;
	}
}
