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

/*
 * Created on Apr 6, 2005
 */
package clus.ext.beamsearch;

import java.util.*;

public class ClusBeamTreeElemOld {

	protected Comparable m_Object;
	protected TreeMap m_Others;
	protected Collection m_OthersList;
	protected int m_Count;

	public ClusBeamTreeElemOld(ClusBeamModel model) {
		m_Object = model;
		m_Count = 1;
	}

	public boolean hasList() {
		return m_Others != null;
	}

	public int getCount() {
		return m_Count;
	}

	public Object getObject() {
		return m_Object;
	}

	public Iterator getOthersIterator() {
		return m_OthersList.iterator();
	}

	public Object getAnObject() {
		if (m_Others == null) {
			return m_Object;
		} else {
			Integer key = (Integer)m_Others.firstKey();
			ArrayList list = (ArrayList)m_Others.get(key);
			return list.get(0);
		}
	}

	public void addAll(Collection lst) {
		if (m_Others == null) {
			lst.add(m_Object);
		} else {
			Iterator iter = m_OthersList.iterator();
			while (iter.hasNext()) {
				ArrayList arr = (ArrayList)iter.next();
				for (int i = 0; i < arr.size(); i++) {
					lst.add(arr.get(i));
				}
			}
		}
	}

	public void looseOthers() {
		Integer key = (Integer)m_Others.firstKey();
		ArrayList list = (ArrayList)m_Others.get(key);
		if (list.size() != 1) {
			throw new Error("ClusBeamTreeElem::removeFirst(): count is inconsistent");
		}
		m_Object = (Comparable)list.get(0);
		m_Others = null;
		m_OthersList = null;
	}

	public void addModelToSet(Comparable cmp) {
		Integer key = new Integer(cmp.hashCode());
		ArrayList list = (ArrayList)m_Others.get(key);
		if (list == null) {
			ArrayList nlist = new ArrayList();
			nlist.add(cmp);
			m_Others.put(key, nlist);
		} else {
			list.add(cmp);
		}
	}

	public void removeFirst() {
		Integer key = (Integer)m_Others.firstKey();
		ArrayList list = (ArrayList)m_Others.get(key);
		if (list.size() == 1) {
			m_Others.remove(key);
		} else {
			list.remove(list.size()-1);
		}
		// System.out.print("["+m_Count+"]");
		m_Count--;
		if (m_Count == 1) looseOthers();
	}

	public int addIfNotIn(Comparable cmp) {
		if (m_Others == null) {
			if (cmp.equals(m_Object)) {
				return 0;
			} else {
				m_Others = new TreeMap();
				m_OthersList = m_Others.values();
				addModelToSet(m_Object);
				addModelToSet(cmp);
				m_Object = null;
				m_Count++;
				return 1;
			}
		} else {
			Integer key = new Integer(cmp.hashCode());
			ArrayList list = (ArrayList)m_Others.get(key);
			if (list == null) {
				ArrayList nlist = new ArrayList();
				nlist.add(cmp);
				m_Others.put(key, nlist);
				m_Count++;
				return 1;
			} else {
				for (int i = 0; i < list.size(); i++) {
					if (cmp.equals(list.get(i))) {
						return 0;
					}
				}
				list.add(cmp);
				m_Count++;
				return 1;
			}
		}
	}
}
