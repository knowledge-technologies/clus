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

import java.util.Random;

import clus.main.*;
import clus.data.type.*;
import clus.model.test.*;
import clus.statistic.*;
import clus.util.ClusException;
import clus.heuristic.*;

public class NArySplit extends NominalSplit {

	ClusStatistic m_MStat;

	public void initialize(ClusStatManager manager) {
		m_MStat = manager.createClusteringStat();
	}

	public void setSDataSize(int size) {
		m_MStat.setSDataSize(size);
	}

	public void findSplit(CurrentBestTestAndHeuristic node, NominalAttrType type) {
		double unk_freq = 0.0;
		int nbvalues = type.getNbValues();
		// If has missing values?
		if (type.hasMissing()) {
			ClusStatistic unknown = node.m_TestStat[nbvalues];
			m_MStat.copy(node.m_TotStat);
			m_MStat.subtractFromThis(unknown);
			unk_freq = unknown.m_SumWeight / node.getTotWeight();
		} else {
			m_MStat.copy(node.m_TotStat);
		}
		// Calculate heuristic
		double mheur = node.calcHeuristic(m_MStat, node.m_TestStat, nbvalues);
		if (mheur > node.m_BestHeur + ClusHeuristic.DELTA) {
			node.m_UnknownFreq = unk_freq;
			node.m_BestHeur = mheur;
			node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
			double[] freq = createFreqList(m_MStat.m_SumWeight, node.m_TestStat, nbvalues);
			node.m_BestTest = new NominalTest(type, freq);
		}
	}

  public void findRandomSplit(CurrentBestTestAndHeuristic node, NominalAttrType type, Random rn) {
    try {
      throw new ClusException("Not implemented yet!");
    } catch (ClusException e) {
      e.printStackTrace();
    }
  }
}
