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

public abstract class INIFileNode implements Serializable {

	protected String m_hName;
	protected INIFileNode m_hParent;
	protected boolean m_Enabled = true;

	public INIFileNode(String name) {
		m_hName = name;
	}

	public void setName(String name) {
		m_hName = name;
	}

	public String getName() {
		return m_hName;
	}

	public void setEnabled(boolean enable) {
		m_Enabled = enable;
	}

	public boolean isEnabled() {
		return m_Enabled;
	}

	public int getDepth() {
		int depth = 0;
		INIFileNode node = getParent();
		while (node != null) {
			if (!node.isSectionGroup()) depth++;
			node = node.getParent();
		}
		return depth;
	}

	public String getPathName(String my_name) {
		String name = my_name;
		INIFileNode node = this;
		while (node != null) {
			if (!node.isSectionGroup()) {
				INIFileNode parent = node.getParent();
				if (parent != null && parent.isSectionGroup()) {
					 name = parent.getName() + "." + name;
				} else {
					 name = node.getName() + "." + name;
				}
			}
			node = node.getParent();
		}
		return name;
	}

	public void setParent(INIFileNode node) {
		m_hParent = node;
	}

	public INIFileNode getParent() {
		return m_hParent;
	}

	public boolean hasParent() {
		return m_hParent != null;
	}

	public abstract INIFileNode cloneNode();

	public abstract boolean isSectionGroup();

	public abstract boolean isSection();

	public abstract void save(PrintWriter writer) throws IOException;
}
