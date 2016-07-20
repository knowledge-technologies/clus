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

import clus.data.rows.*;
import clus.main.Settings;

public class ILevelCHeurStat extends ILevelCStatistic {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public static final int EXT = 0;
	public static final int POS = 1;
	public static final int NEG = 2;

	protected int m_NbClass;
	protected ArrayList m_Constraints;
	protected int[][] m_ConstraintIndex;
	protected int[] m_IE;
	protected int[] m_Clusters;
	protected int[] m_CL;
	protected int[] m_ML;

	public ILevelCHeurStat(ILevelCStatistic stat, int nbclass) {
		super(stat.m_Numeric);
		m_NbClass = nbclass;
		m_CL = new int[nbclass];
		m_ML = new int[nbclass];
	}

	public void updateWeighted(DataTuple tuple, double weight) {
		super.updateWeighted(tuple, weight);
		int tidx = tuple.getIndex();
		int[] cidx = m_ConstraintIndex[tidx];
		if (cidx == null) return;
		for (int i = 0; i < cidx.length; i++) {
			ILevelConstraint cons = (ILevelConstraint)m_Constraints.get(cidx[i]);
			int otidx = cons.getOtherTupleIdx(tuple);
			if (m_IE[otidx] == EXT) {
				int oclass = m_Clusters[otidx];
				if (cons.getType() == ILevelConstraint.ILevelCMustLink) m_ML[oclass]++;
				else m_CL[oclass]++;
			}
		}
	}

	public void removeWeighted(DataTuple tuple, double weight) {
		super.updateWeighted(tuple, -1.0*weight);
		int tidx = tuple.getIndex();
		int[] cidx = m_ConstraintIndex[tidx];
		if (cidx == null) return;
		for (int i = 0; i < cidx.length; i++) {
			ILevelConstraint cons = (ILevelConstraint)m_Constraints.get(cidx[i]);
			int otidx = cons.getOtherTupleIdx(tuple);
			if (m_IE[otidx] == EXT) {
				int oclass = m_Clusters[otidx];
				if (cons.getType() == ILevelConstraint.ILevelCMustLink) m_ML[oclass]--;
				else m_CL[oclass]--;
			}
		}
	}

	public void setIndices(int[][] considx, ArrayList constr, int[] ie, int[] clusters) {
		m_IE = ie;
		m_Constraints = constr;
		m_ConstraintIndex = considx;
		m_Clusters = clusters;
	}

	public int computeMinimumExtViolated(int ig1, int ig2, boolean allownew) {
		int totalml = 0;
		for (int i = 0; i < m_NbClass; i++) {
			totalml += m_ML[i];
		}
		int best = -2;
		int bestviolated = Integer.MAX_VALUE;
		for (int i = 0; i < m_NbClass; i++) {
			if (i != ig1 && i != ig2 && m_ML[i] > 0) {
				int violated = m_CL[i] + (totalml - m_ML[i]);
				if (violated < bestviolated) {
					bestviolated = violated;
					best = i;
				}
			}
		}
		if (allownew && totalml < bestviolated) {
			bestviolated = totalml;
			best = -1;
		}
		setClusterID(best);
		return best == -2 ? -1 : bestviolated;
	}
}
