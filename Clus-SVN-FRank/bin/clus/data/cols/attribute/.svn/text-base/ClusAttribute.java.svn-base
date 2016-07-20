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

package clus.data.cols.attribute;

import jeans.util.*;

import clus.io.*;
import clus.main.*;
import clus.data.type.*;
import clus.data.cols.*;
import clus.selection.*;

public abstract class ClusAttribute extends ClusSerializable {

	protected boolean m_Split;

	public void resize(int rows) {
	}

	public void setSplit(boolean split) {
		m_Split = split;
	}

	public boolean isSplit() {
		return m_Split;
	}

	public String getName() {
		return getType().getName();
	}

	public abstract ClusAttrType getType();

	public void prepare() {
	}

	public void unprepare() {
	}

	public void findBestTest(MyArray leaves, ColTarget target, ClusStatManager smanager) {
	}

	public void split(ColTarget target) {
	}

	public ClusAttribute select(ClusSelection sel, int nbsel) {
		return null;
	}

	public void insert(ClusAttribute attr, ClusSelection sel, int nb_new) {
	}
}
