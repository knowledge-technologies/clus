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


public class BottomLevelIterator {

	private TreeIterator iterator;
	private int cr_level;
	private boolean busy = true;

	public BottomLevelIterator(TreeIterator iterator) {
		this.iterator = iterator;
		this.cr_level = 0;
	}

	public Node getNextNode() {
		descentTillBottom();
		Node node = iterator.getNode();
		climbTillDescision();
		return node;
	}

	public boolean hasMoreAreas() {
		return busy;
	}

	public int getLevel() {
		return cr_level;
	}

	private void descentTillBottom() {
		while (!iterator.atBottomLevel()) {
			iterator.goDown(1);
			cr_level++;
		}
	}

	private void climbTillDescision() {
		while (true) {
			if (cr_level <= 0) {
				busy = false;
				return;
			}
			int num = iterator.getIndex();
			iterator.goUp();
			cr_level--;
			if (num < iterator.getNode().getNbChildren()) {
				iterator.goDown(num+1);
				cr_level++;
				return;
			}

		}
	}

}
