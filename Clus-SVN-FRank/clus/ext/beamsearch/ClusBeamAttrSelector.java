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
 * Created on Apr 25, 2005
 */
package clus.ext.beamsearch;

import clus.data.rows.*;
import clus.model.test.*;

public class ClusBeamAttrSelector {

	public RowData data;
	public boolean stopcrit;
	public NodeTest[] besttests;

	public final boolean hasEvaluations() {
		return besttests != null;
	}

	public final NodeTest[] getBestTests() {
		return besttests;
	}

	public final void setData(RowData data) {
		this.data = data;
	}

	public final RowData getData() {
		return data;
	}

	public final void setStopCrit(boolean stopcrit) {
		this.stopcrit = stopcrit;
	}

	public final boolean isStopCrit() {
		return stopcrit;
	}

	public final void newEvaluations(int nb) {
		besttests = new NodeTest[nb];
	}

	public final void setBestTest(int i, NodeTest test) {
		besttests[i] = test;
	}
}
