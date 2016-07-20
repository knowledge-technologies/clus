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

package clus.model.test;

import jeans.util.*;

import clus.data.rows.*;
import clus.data.type.*;
import clus.main.Settings;

public class FakeTest extends NodeTest {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected MyArray m_Lines = new MyArray();
	protected String m_Line;

	public FakeTest() {
	}

	public void setLine(String line) {
		m_Line = line;
	}

	public void addLine(String line) {
		m_Lines.addElement(line);
	}

	public int predictWeighted(DataTuple tuple) {
		return -1;
	}

	public boolean equals(NodeTest test) {
		return false;
	}

	public ClusAttrType getType() {
		return null;
	}

	public void setType(ClusAttrType type) {
	}

	public String getString() {
		return m_Line;
	}

	public String getPythonString() {
		return getString();
	}
	
	public int getNbLines() {
		return m_Lines.size();
	}

	public String getLine(int i) {
		return (String)m_Lines.elementAt(i);
	}
}
