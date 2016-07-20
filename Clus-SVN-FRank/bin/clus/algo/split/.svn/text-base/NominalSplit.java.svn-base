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

package clus.algo.split;

import clus.main.*;
import clus.data.type.*;
import clus.statistic.*;

import java.util.*;

public abstract class NominalSplit {

	public double[] createFreqList(double n_tot, ClusStatistic[] s_set, int nbvalues) {
		double[] res = new double[nbvalues];
		for (int i = 0; i < nbvalues; i++)
			res[i] = s_set[i].m_SumWeight / n_tot;
		return res;
	}

	public abstract void initialize(ClusStatManager manager);

	public abstract void setSDataSize(int size);

	public abstract void findSplit(CurrentBestTestAndHeuristic node, NominalAttrType type);

  public abstract void findRandomSplit(CurrentBestTestAndHeuristic node, NominalAttrType type, Random rn);
}
