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

import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.data.cols.attribute.*;

public abstract class SoftTest extends NodeTest {

	public abstract int softPredictNb(DataTuple tuple, int branch);

	public abstract int softPredict(RowData res, DataTuple tuple, int idx, int branch);

	public abstract int softPredictNb2(DataTuple tuple, int branch);

	public abstract int softPredict2(RowData res, DataTuple tuple, int idx, int branch);

	public int predict(ClusAttribute attr, int idx) {
		return ClusNode.NO;
	}

	public int predictWeighted(DataTuple tuple) {
		return ClusNode.NO;
	}

	public boolean equals(NodeTest test) {
		return false;
	}

	public boolean isSoft() {
		return true;
	}
}
