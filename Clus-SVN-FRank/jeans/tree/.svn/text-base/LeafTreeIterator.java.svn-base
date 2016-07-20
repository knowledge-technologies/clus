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

// FIXME -- less instance vars possible?

public class LeafTreeIterator extends TreeIterator {

	private boolean busy = true;
	private boolean advanced = false;
	private int cr_level = 0;
	private Node cr_node = null;

	public LeafTreeIterator(Node node) {
		super(node);
	}

	public LeafTreeIterator(LeafTreeIterator other) {
		super(other);
		this.busy = other.busy;
		this.advanced = other.advanced;
		this.cr_level = other.cr_level;
		this.cr_node = other.cr_node;
	}

	public void reset() {
		super.reset();
		busy = true;
		advanced = false;
	}

	public Node getNextNode() {
		if (!advanced) gotoNextNode();
		advanced = false;
		return cr_node;
	}

	public boolean hasMoreNodes() {
		if (!advanced) gotoNextNode();
		advanced = true;
		return cr_node != null;
	}

	public void gotoNextNode() {
		if (busy) {
			descentTillBottom();
			cr_node = getNode();
			climbTillDescision();
		} else {
			cr_node = null;
		}
	}

	public int getLevel() {
		return cr_level;
	}

	private void descentTillBottom() {
		while (!atBottomLevel()) {
			goDown(0);
			cr_level++;
		}
	}

	private void climbTillDescision() {
		while (true) {
			if (cr_level <= 0) {
				busy = false;
				return;
			}
			int num = getIndex();
			goUp();
			cr_level--;
			if (num < getNode().getNbChildren()-1) {
				goDown(num+1);
				cr_level++;
				return;
			}

		}
	}

}
