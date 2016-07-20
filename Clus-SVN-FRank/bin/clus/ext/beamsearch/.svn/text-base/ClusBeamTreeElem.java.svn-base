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

public class ClusBeamTreeElem {

	protected Comparable m_Object;
	protected ArrayList m_Others;

	protected static Random m_Random = new Random(0);

	public ClusBeamTreeElem(ClusBeamModel model) {
		m_Object = model;
	}

	public boolean hasList() {
		return m_Others != null;
	}

	public ArrayList getOthers() {
		return m_Others;
	}

	public int getCount() {
		return m_Others == null ? 1 : m_Others.size();
	}

	public void setObject(Comparable obj) {
		m_Object = obj;
	}

	public Object getObject() {
		return m_Object;
	}


	public Iterator getOthersIterator() {
		return m_Others.iterator();
	}

	public Object getAnObject() {
		if (m_Others == null) {
			return m_Object;
		} else {
			return m_Others.get(m_Random.nextInt(m_Others.size()));
		}
	}

	public void addAll(Collection lst) {
		if (m_Others == null) {
			lst.add(m_Object);
		} else {
			for (int i = 0; i < m_Others.size(); i++) {
				lst.add(m_Others.get(i));
			}
		}
	}

	public void looseOthers() {
		m_Object = (Comparable)m_Others.get(0);
		m_Others = null;
	}

	public void removeFirst() {
		m_Others.remove(m_Random.nextInt(m_Others.size()));
		if (m_Others.size() == 1) looseOthers();
	}

	public int addIfNotIn(Comparable cmp) {
		if (m_Others == null) {
			if (cmp.equals(m_Object)) {
				//System.out.println("Already in the Beam 1");
				return 0;
			} else {
				m_Others = new ArrayList();
				m_Others.add(m_Object);
				m_Others.add(cmp);
				m_Object = null;
				return 1;
			}
		} else {
			for (int i = 0; i < m_Others.size(); i++) {
				Comparable cmp_other = (Comparable)m_Others.get(i);
				if (cmp.equals(cmp_other)) {
					//System.out.println("Already in the Beam 2");
					return 0;
				}
			}
			m_Others.add(cmp);
			return 1;
		}
	}
}
