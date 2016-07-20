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

package jeans.io.ini;

import java.io.*;
import java.util.*;

public class INIFileSectionGroup extends INIFileNode {

	public final static long serialVersionUID = 1;

	protected Vector m_hSections = new Vector();
	protected INIFileSection m_hPrototype;

	public INIFileSectionGroup(String name) {
		super(name);
	}

	public boolean isSectionGroup() {
		return true;
	}

	public boolean isSection() {
		return false;
	}

	public void setPrototype(INIFileSection sec) {
		m_hPrototype = sec;
	}

	public INIFileSection getPrototype() {
		return m_hPrototype;
	}

	public INIFileNode cloneNode() {
		INIFileSectionGroup sec =  new INIFileSectionGroup(getName());
		sec.setPrototype((INIFileSection)getPrototype().cloneNode());
		return sec;
	}

	public int getNbSections() {
		return m_hSections.size();
	}

	public INIFileSection getSectionAt(int idx) {
		return (INIFileSection)m_hSections.elementAt(idx);
	}

	public void addSection(INIFileSection section) {
		m_hSections.addElement(section);
		section.setParent(this);
	}

	public void save(PrintWriter writer) throws IOException {
		String groupName = getName();
		for (int idx = 0; idx < getNbSections(); idx++) {
			INIFileSection section = (INIFileSection)getSectionAt(idx);
			section.save(groupName, writer);
		}
	}
}
