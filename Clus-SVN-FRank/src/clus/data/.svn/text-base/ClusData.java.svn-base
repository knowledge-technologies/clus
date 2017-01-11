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

package clus.data;

import clus.util.*;
import clus.algo.tdidt.ClusNode;
import clus.data.rows.*;
import clus.error.*;
import clus.selection.*;
import clus.statistic.*;

public abstract class ClusData {

	protected int m_NbRows;

	public final int getNbRows() {
		return m_NbRows;
	}

	public final void setNbRows(int nb) {
		m_NbRows = nb;
	}

	public ClusData selectFrom(ClusSelection sel) {
		return null;
	}

	public abstract ClusData cloneData();

	public abstract ClusData select(ClusSelection sel);

	public abstract void insert(ClusData other, ClusSelection sel);

	public abstract void resize(int nbrows);

	public abstract void attach(ClusNode node);

	public abstract void calcTotalStat(ClusStatistic stat);

	public abstract void calcError(ClusNode node, ClusErrorList par);

	public abstract double[] getNumeric(int idx);

	public abstract int[] getNominal(int idx);

	public abstract void preprocess(int pass, DataPreprocs pps) throws ClusException;

	public void calcTotalStats(ClusStatistic[] stats) {
		for (int i = 0; i < stats.length; i++) {
			calcTotalStat(stats[i]);
		}
	}
}
