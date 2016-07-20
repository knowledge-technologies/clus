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

// FIXME -- grow like MyArray :-)

public class IntegerStack {

	protected int[] stack;
	protected int pos, grow, size;

	public IntegerStack() {
		this(10);
	}

	public IntegerStack(int grow) {
		this.grow = grow;
		this.size = 0;
	}

	public IntegerStack(IntegerStack stack) {
		this.pos = stack.pos;
		this.grow = stack.grow;
		this.size = pos;
		if (pos > 0) {
			this.stack = new int[pos];
			System.arraycopy(stack.stack, 0, this.stack, 0, pos);
		}
	}

	public void push(int value) {
		if (pos+1 > size) grow();
		stack[pos++] = value;
	}

	public int pop() {
		int res = stack[--pos];
		if (pos < size-grow) shrink();
		return res;
	}

	public boolean isEmpty() {
		return pos == 0;
	}

	public int getTop() {
		return stack[pos-1];
	}

	public int getElementAt(int pos) {
		return stack[pos];
	}

	public void clear() {
		size = 0;
		stack = null;
	}

	public int getSize() {
		return pos;
	}

	private void grow() {
		int[] newStack = new int[size+grow];
		for (int ctr = 0; ctr < size; ctr++)
			newStack[ctr] = stack[ctr];
		stack = newStack;
		size += grow;
	}

	private void shrink() {
		size -= grow;
		int[] newStack = new int[size];
		for (int ctr = 0; ctr < size; ctr++)
			newStack[ctr] = stack[ctr];
		stack = newStack;
	}
}
