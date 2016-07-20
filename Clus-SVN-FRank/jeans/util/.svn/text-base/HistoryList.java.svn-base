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

import java.io.*;
import java.util.Vector;

import jeans.util.Operation;


/**
 * A class representing the History.
 *
 * @author	Kurt
 * @version	1.0
 */
public class HistoryList {

	private static HistoryList instance = null;

	private Vector ops = new Vector();
	private int firstNewOp;

	private HistoryList() {
	}

	/** Returns the one and only (Singleton) History. */
	public static HistoryList getInstance() {
		if (instance == null) instance = new HistoryList();
		return instance;
	}

	/**
	 * Add an operation to the History.
	 *
	 * @pre	op != null
	 */
	public void addOperation(Operation op) {
		if (firstNewOp < ops.size()) ops.setSize(firstNewOp);
		ops.addElement(op);
		firstNewOp++;
	}

	/**
	 * Undo the last executed (or redone) Operation. After this, the Board, including the Game if it has one, will be restored
	 * to the state it was in before the Operation was issued.
	 *
	 * @pre	canUndo()
	 */

	public Operation undoOperation() {
		firstNewOp--;
		Operation op = (Operation)(ops.elementAt(firstNewOp));
		op.undo();
		return op;
	}

	/**
	 * Redo the last undone Operation.
	 *
	 * @pre	canRedo().
	 */
	public Operation redoOperation() {
		Operation op = (Operation)(ops.elementAt(firstNewOp));
		op.redo();
		firstNewOp++;
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
		return (lastDoneOperation() != totalNumberOfOperations());
	}

	/**
	 * Returns a Vector with all Operations in the History. Earlier Operations have lower indices.
	 * Warning: only inspectors may be used on this Vector and its Elements.
	 */
	public Vector getOperations() {
		return ops;
	}

	public void undoOperations(int number) {
		for (int ctr = 0; ctr < number; ctr++)
			undoOperation();
	}

	public void redoOperations(int number) {
		for (int ctr = 0; ctr < number; ctr++)
			redoOperation();
	}

	public Object getState() {
		return new HistoryState(ops, firstNewOp);
	}

	public Object getEmptyState() {
		return new HistoryState();
	}

	public void setState(Object object) {
		HistoryState state = (HistoryState)object;
		ops = state.getOperations();
		firstNewOp = state.getFirstNewOp();
	}

}


class HistoryState implements Serializable {

	public final static long serialVersionUID = 1;

	private Vector ops;
	private int firstNewOp;

	HistoryState() {
		this(new Vector(), 0);
	}

	HistoryState(Vector ops, int firstNewOp) {
		this.ops = new Vector(ops);
		this.firstNewOp = firstNewOp;
	}

	int getFirstNewOp() {
		return firstNewOp;
	}

	Vector getOperations() {
		return ops;
	}

}
