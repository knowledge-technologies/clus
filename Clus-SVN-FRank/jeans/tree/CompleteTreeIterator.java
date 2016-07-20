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


public class CompleteTreeIterator extends TreeIterator {

	private boolean busy = true;
	private boolean advanced = true;

	public CompleteTreeIterator(Node node) {
		super(node);
	}

	public void reset() {
		super.reset();
		busy = true;
		advanced = true;
	}

	public Node getNextNode() {
		if (!advanced) gotoNextNode();
		advanced = false;
		return getNode();
	}

	public boolean hasMoreNodes() {
		if (!advanced) gotoNextNode();
		advanced = true;
		return busy;
	}

	private void gotoNextNode() {
		if (!atBottomLevel()) {
			goDown(0);
		} else {
		    boolean done = false;
			while (!done) {
				if (atTopLevel()) {
				    busy = false;
					done = true;
				} else {
					int crindex = getIndex();
					goUp();
					int children = getNode().getNbChildren();
					if (crindex+1 < children) {
						goDown(crindex+1);
						done= true;
					}
				}
			}
		}
	}
}
