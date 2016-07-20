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

package clus.ext.optxval;

import java.util.*;
import jeans.tree.*;

import clus.main.Settings;
import clus.model.test.*;

public class OptXValSplit extends MyNode {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected int[] m_Folds;
	protected NodeTest m_Test;

	public int init(int[] folds, NodeTest test) {
		m_Test = test;
		int mnb = folds.length;
		m_Folds = new int[mnb];
		System.arraycopy(folds, 0, m_Folds, 0, mnb);
		int arity = test.getNbChildren();
		setNbChildren(arity);
		return arity;
	}

	public int[] getFolds() {
		return m_Folds;
	}

	public void setTest(NodeTest test) {
		m_Test = test;
	}

	public NodeTest getTest() {
		return m_Test;
	}

	public boolean contains(int fold) {
		return Arrays.binarySearch(m_Folds, fold) >= 0;
	}
}
