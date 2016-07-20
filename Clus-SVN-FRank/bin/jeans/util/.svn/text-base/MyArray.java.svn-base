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

import java.util.*;

import java.io.Serializable;

public class MyArray implements Serializable {

	public final static long serialVersionUID = 1;

	private Object[] m_Objects;
	private int m_Size;

	public MyArray() {
		m_Objects = new Object[0];
		m_Size = 0;
	}

	public MyArray(int cap) {
		m_Objects = new Object[cap];
		m_Size = 0;
	}

	public final void sort() {
		Arrays.sort(m_Objects, 0, m_Size);
	}

	public final void setSize(int size) {
		m_Objects = new Object[size];
	}

	public final void addElement(Object element) {
		Object[] newObjects;
		if (m_Size == m_Objects.length) {
			newObjects = new Object[2*m_Objects.length + 1];
			System.arraycopy(m_Objects, 0, newObjects, 0, m_Size);
			m_Objects = newObjects;
		}
		m_Objects[m_Size++] = element;
	}

	public final Object[] getObjects() {
		return m_Objects;
	}

	public final Object elementAt(int index) {
		return m_Objects[index];
	}

	public final void insertElementAt(Object element, int index) {
		Object[] nObjs;
		if (m_Size < m_Objects.length) {
			System.arraycopy(m_Objects, index, m_Objects, index + 1, m_Size - index);
			m_Objects[index] = element;
		} else {
			nObjs = new Object[2*m_Objects.length + 1];
			System.arraycopy(m_Objects, 0, nObjs, 0, index);
			nObjs[index] = element;
			System.arraycopy(m_Objects, index, nObjs, index + 1, m_Size - index);
			m_Objects = nObjs;
		}
		m_Size++;
	}

	public final void removeElementAt(int index) {
		System.arraycopy(m_Objects, index + 1, m_Objects, index, m_Size - index - 1);
		m_Objects[--m_Size] = null;
	}

	public final void removeAllElements() {
		for (int i = 0; i < m_Size; i++) m_Objects[i] = null;
		m_Size = 0;
	}

    //Implemented by bert van rillaer for thesis
	public final void removeElement(Object element) {
        int index = 0;
        for(int i=0;i<m_Size;i++){
            if(m_Objects[i].equals(element))
                index = i;
        }
        removeElementAt(index);
	}

	public final void setElementAt(Object element, int index) {
		m_Objects[index] = element;
		if (index >= m_Size) m_Size = index+1;
	}

	public final int size() {
		return m_Size;
	}

	public final void setCapacity(int cap) {
		Object[] newObjects = new Object[cap];
		System.arraycopy(m_Objects, 0, newObjects, 0, Math.min(cap, m_Size));
		m_Objects = newObjects;
		if (m_Objects.length < m_Size) m_Size = m_Objects.length;
	}

	public final void swap(int first, int second) {
		Object help = m_Objects[first];
		m_Objects[first] = m_Objects[second];
		m_Objects[second] = help;
	}

	public final void trimToSize() {
		Object[] newObjects = new Object[m_Size];
		System.arraycopy(m_Objects, 0, newObjects, 0, m_Size);
		m_Objects = newObjects;
	}
}
