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

package clus.selection;

import java.util.*;

import jeans.util.array.*;

import clus.util.*;

// "almost linear" algorithm for randomisation
// algorithm divide2 chooses random nr. of bucket
// if bucket full then goes to right in search of non-full bucket
// divide proceeds in 2 steps:
// first randomly throws elements in buckets with max capacity #Models/#Buckets
// then remaining models are thrown onto buckets randomly

public class XValRandomSelection extends XValMainSelection {

	protected int[] m_Selection;
	protected Random m_Random;

	public XValRandomSelection(int nbtot, int folds) throws ClusException {
		this(nbtot, folds, ClusRandom.getRandom(ClusRandom.RANDOM_SELECTION));
	}

	public XValRandomSelection(int nbtot, int folds, Random random) throws ClusException {
		super(folds, nbtot);
		if (folds == nbtot) {
			createLeaveOneOutXVAL(nbtot);
		} else {
			createRegularXVAL(nbtot, folds, random);
		}
	}

	public int getFold(int row) {
		return m_Selection[row];
	}

	public void printDebug() {
		System.out.println("XVAL: "+MyIntArray.print(m_Selection));
	}

	public void createLeaveOneOutXVAL(int nbtot) {
		m_Selection = new int[nbtot];
		for (int i = 0; i < nbtot; i++) {
			m_Selection[i] = i;
		}
	}

	// TODO: - put in (stratified) cross-validation partition code from "csvconvert.exe"
	public void createRegularXVAL(int nbtot, int folds, Random random) throws ClusException {
		m_Random = random;
		int max = nbtot/folds;
		XValGroup[] grps = new XValGroup[folds];
		for (int i = 0; i < folds; i++) grps[i] = new XValGroup(max+1);
		int from = devide2(grps, 0, nbtot, max);
		if (from != -1) {
			int ok = devide2(grps, from, nbtot, max+1);
			if (ok != -1) throw new ClusException("Error partitioning xval data");
		}
		m_Selection = new int[nbtot];
		for (int i = 0; i < folds; i++) {
			XValGroup gr = grps[i];
			for (int j = 0; j < gr.getNbElements(); j++) {
				m_Selection[gr.getElement(j)] = i;
			}
		}
	}

	/** Selects randomly the data for all the folds fold.
	 *  The order of the data inside folds is kept!
	 */
	public int devide2(XValGroup[] grps, int from, int till, int max) {
		while (from < till) {
			int grp = m_Random.nextInt(grps.length);
			if (add_to_group(from, grps, grp, max)) {
				from++;
			} else {
				return from;
			}
		}
		return -1;
	}

	public boolean add_to_group(int from, XValGroup[] grps, int grp, int max) {
		int nbg = grps.length;
		int ctr = 0;
		while (ctr < nbg) {
			XValGroup gr = grps[grp];
			if (gr.add(from, max)) return true;
			grp = (grp + 1) % nbg;
			ctr++;
		}
		return false;
	}
}
