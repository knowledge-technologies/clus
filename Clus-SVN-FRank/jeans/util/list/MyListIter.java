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

package jeans.util.list;

public class MyListIter extends MyList {

	protected MyList m_Prev;
	protected MyList m_Curr;
	protected MyList m_Last;

	public MyListIter() {
		m_Prev = this;
		m_Last = this;
	}

	public MyListIter(MyList first) {
		m_Prev = this;
		m_Next = m_Curr = first;
	}

	public final void reset() {
		m_Prev = this;
		m_Curr = this;
	}

	public final MyList getFirst() {
		m_Prev = this;
		m_Curr = m_Next;
		return m_Curr;
	}

	public final MyList getNext() {
		MyList next = m_Curr.m_Next;
		if (next != null) {
			m_Prev = m_Curr;
			m_Curr = next;
		}
		return next;
	}

	public final boolean hasNext() {
		return m_Curr.m_Next != null;
	}

	public final boolean isEmpty() {
		return m_Last == this;
	}

	public final void addEnd(MyList elem) {
		m_Last.m_Next = elem;
		m_Last = elem;
	}

	public final void insertBefore(MyList elem) {
		m_Prev.m_Next = elem;
		elem.m_Next = m_Curr;
		m_Prev = elem;
	}

	public final MyList deleteElement() {
		m_Curr = m_Curr.m_Next;
		m_Prev.m_Next = m_Curr;
		return m_Curr;
	}
}
