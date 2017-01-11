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

package clus.ext.ilevelc;

import java.util.ArrayList;

public class ILevelCUtil {

	public static int[][] createConstraintsIndex(int nbtrain, ArrayList constr) {
		/* create index as array lists */
		ArrayList[] crIndex = new ArrayList[nbtrain];
		for (int i = 0; i < constr.size(); i++) {
			ILevelConstraint ic = (ILevelConstraint)constr.get(i);
			int t1 = ic.getT1().getIndex();
			int t2 = ic.getT2().getIndex();
			if (crIndex[t1] == null) crIndex[t1] = new ArrayList();
			if (crIndex[t2] == null) crIndex[t2] = new ArrayList();
			crIndex[t1].add(new Integer(i));
			crIndex[t2].add(new Integer(i));
		}
		/* copy it to final int matrix */
		int[][] index = new int[nbtrain][];
		for (int i = 0; i < nbtrain; i++) {
			if (crIndex[i] != null) {
				int nb = crIndex[i].size();
				index[i] = new int[nb];
				for (int j = 0; j < nb; j++) {
					Integer value = (Integer)crIndex[i].get(j);
					index[i][j] = value.intValue();
				}
			}
		}
		return index;
	}

}
