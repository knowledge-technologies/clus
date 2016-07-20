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

package jeans.util;

import java.util.Vector;

/**
 * A class representing the History.
 *
 * @author	Kurt
 * @version	1.0
 */
public class TerminalHistoryList {

	private Vector ops = new Vector();
	private int firstNewOp;

	public TerminalHistoryList() {
	}

	/**
	 * Add an operation to the History.
	 *
	 * @pre	op != null
	 */
	public void addOperation(Object op) {
		ops.addElement(op);
		firstNewOp = ops.size();
	}

	public boolean containsOperation(Object op) {
		return ops.contains(op);
	}

	public void removeOperation(Object op) {
		ops.removeElement(op);
		if (firstNewOp > ops.size()) firstNewOp = ops.size();
	}

	/**
	 * Undo the last executed (or redone) Operation. After this, the Board, including the Game if it has one, will be restored
	 * to the state it was in before the Operation was issued.
	 *
	 * @pre	canUndo()
	 */

	public Object undoOperation() {
		firstNewOp--;
		return ops.elementAt(firstNewOp);
	}

	/**
	 * Redo the last undone Operation.
	 *
	 * @pre	canRedo().
	 */
	public Object redoOperation() {
		firstNewOp++;
		Object op = ops.elementAt(firstNewOp);
		return op;
	}

	/**
	 * Removes all operations from the History.
	 *
	 * @effect !canUndo() && !canRedo()
	 */
	public void clearHistory() {
		ops.setSize(0);
		firstNewOp = 0;
	}

	/**
	 * Returns the number of the last excuted or redone Operation.
	 * Numbering:	The first executed Operation since History startup or since the last
	 *		History cleanup, is numbered 1.
	 */
	public int lastDoneOperation() {
		return firstNewOp;
	}

	/**
	 * Returns the total number of Operations currently in the History, including undone Ops.
	 */
	public int totalNumberOfOperations() {
		return ops.size();
	}

	/**
	 * There are Operations in the History, which were not already undone,
	 * and which were not trashed by a clearHistory().
	 */
	public boolean canUndo() {
		return (lastDoneOperation()>0);
	}

	public boolean canRedo() {
		return (lastDoneOperation() < totalNumberOfOperations()-1);
	}

	/**
	 * Returns a Vector with all Operations in the History. Earlier Operations have lower indices.
	 * Warning: only inspectors may be used on this Vector and its Elements.
	 */
	public Vector getOperations() {
		return ops;
	}
}
