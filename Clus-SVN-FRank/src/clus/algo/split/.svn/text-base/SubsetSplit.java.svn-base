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
import clus.algo.rules.ClusRuleHeuristicDispersion;
import clus.data.type.*;
import clus.model.test.*;
import clus.statistic.*;
import clus.heuristic.*;

import java.util.*;

public class SubsetSplit extends NominalSplit {

	ClusStatistic m_PStat, m_CStat, m_MStat;
	ClusStatManager m_StatManager;

	public void initialize(ClusStatManager manager) {
		m_PStat = manager.createClusteringStat();
		m_CStat = m_PStat.cloneStat();
		m_MStat = m_PStat.cloneStat();
		m_StatManager = manager;
	}

	public void setSDataSize(int size) {
		m_PStat.setSDataSize(size);
		m_CStat.setSDataSize(size);
		m_MStat.setSDataSize(size);
	}

	public ClusStatManager getStatManager() {
		return m_StatManager;
	}

	public void showTest(NominalAttrType type, boolean[] isin, int add, double mheur, ClusStatistic tot, ClusStatistic pos) {
		int count = 0;
		System.out.print(type.getName()+ " in {");
		for (int i = 0; i < type.getNbValues(); i++) {
			if (isin[i] || i == add) {
				if (count != 0) System.out.print(",");
				System.out.print(type.getValue(i));
				count++;
			}
		}
		tot.calcMean(); pos.calcMean();
		// System.out.println("}: "+mheur+" "+tot+" "+pos);
		System.out.println("}: "+mheur);
	}

	public void findSplit(CurrentBestTestAndHeuristic node, NominalAttrType type) {
		//System.out.println("find split for attr: " + type);
		double unk_freq = 0.0;
		int nbvalues = type.getNbValues();
		boolean isin[] = new boolean[nbvalues];
		boolean acceptable = true; // can only be changed for phylogenetic trees
		// If has missing values?
		if (type.hasMissing()) {
			ClusStatistic unknown = node.m_TestStat[nbvalues];
			m_MStat.copy(node.m_TotStat);
			m_MStat.subtractFromThis(unknown);
			unk_freq = unknown.m_SumWeight / node.m_TotStat.m_SumWeight;
		} else {
			m_MStat.copy(node.m_TotStat);
		}
		int card = 0;
		double pos_freq = 0.0;
		double bheur = Double.NEGATIVE_INFINITY;
		// Not working for rules except if constraint of tests to '1' is desired!
		if (nbvalues == 2 && (!getStatManager().isRuleInduceOnly()
				|| getStatManager().getSettings().isConstrainedToFirstAttVal())) {
			// Handle binary splits efficiently
			card = 1;
			isin[0] = true;
			ClusStatistic CStat = node.m_TestStat[0];
			bheur = node.calcHeuristic(m_MStat, CStat);
			//showTest(type, isin, -1, bheur, m_MStat, m_CStat);
			pos_freq = CStat.m_SumWeight / m_MStat.m_SumWeight;
		}
		else if ((getStatManager().getMode() == ClusStatManager.MODE_PHYLO) && (Settings.m_PhylogenySequence.getValue() == Settings.PHYLOGENY_SEQUENCE_DNA)) {
		// for phylogenetic trees with DNA sequences, we use an optimization method: tests like pos10={A,t} are based on the results for tests pos10=A and pos10=T
		// we do not do this for protein sequences, since there the alphabet is much larger, which would complicate things
		//else if (false) {
			boolean[] valid = new boolean[nbvalues];
			// we try all subsets of size 1 (e.g. pos10=A) and 2 (e.g. pos10={AT}) (nbvalues = 5)
			// first we try all subsets of size 1
			for (int j = 0; j < nbvalues; j++) {
				m_PStat.reset();
				m_PStat.add(node.m_TestStat[j]);
				double mheur = node.calcHeuristic(m_MStat, m_PStat);
				if (mheur > bheur) {
					bheur = mheur;
					isin = new boolean[nbvalues];
					isin[j] = true;
					card = 1;
					// Calculate pos freq (of current best one)
					pos_freq = m_PStat.m_SumWeight / m_MStat.m_SumWeight;
				}
				if (mheur > Double.NEGATIVE_INFINITY) {
					valid[j] = true;
				}
			}
			// then we try all subsets of size 2 (if both of the singleton subsets returned a valid heuristic value)
			for (int j = 0; j < nbvalues; j++) {
				if (valid[j])
					for (int k = j+1; k < nbvalues; k++) {
						if (valid[k]) {
							m_PStat.reset();
							m_CStat.copy(m_PStat);
							m_CStat.add(node.m_TestStat[j]);
							m_PStat.add(node.m_TestStat[j]);
							m_PStat.add(node.m_TestStat[k]);
							//double mheur = node.calcHeuristic(m_MStat, m_PStat); // use this if you want to compute the heuristic only in the case both singletons were valid
							ClusStatistic[] csarray = new ClusStatistic[2];
							csarray[0] = m_PStat;
							csarray[1] = m_CStat;
							double mheur = node.calcHeuristic(m_MStat, csarray, 2); // use this if you want to compute the heuristic only in the case both singletons were valid AND you want to reuse their computations
							//System.out.println("mheur: " + mheur);
							if (mheur > bheur) {
								bheur = mheur;
								isin = new boolean[nbvalues];
								isin[j] = true;
								isin[k] = true;
								card = 2;
								// Calculate pos freq (of current best one)
								pos_freq = m_PStat.m_SumWeight / m_MStat.m_SumWeight;
							}
						}
					}			
			}
		}
		else {
			// Try to add values to subsets
			// Each iteration the cardinality increases by at most one
			m_PStat.reset();
			int bvalue = 0;
			if ((m_PStat instanceof CombStat) &&
					((CombStat)m_PStat).getSettings().isHeurRuleDist()) {
				((ClusRuleHeuristicDispersion)node.m_Heuristic).setDataIndexes(new int[0]);
			}
			boolean allowSubsetSplits = getStatManager().getSettings().isNominalSubsetTests();
			while ((bvalue != -1) && ((card+1) < nbvalues)) {
				bvalue = -1;
				for (int j = 0; j < nbvalues; j++) {
					if (!isin[j]) {
						// Try to add this one to the positive stat
						m_CStat.copy(m_PStat);
						m_CStat.add(node.m_TestStat[j]);
						if ((m_PStat instanceof CombStat) &&
								((CombStat)m_PStat).getSettings().isHeurRuleDist()) {
							boolean isin_current[] = new boolean[nbvalues];
							for (int k = 0; k < nbvalues; k++) {
								isin_current[k] = isin[k];
							}
							isin_current[j] = true;
							((ClusRuleHeuristicDispersion)node.m_Heuristic).setDataIndexes(isin_current);
						}
						// Calc heuristic
						
						boolean isin_current[] = new boolean[nbvalues];
						for (int k = 0; k < nbvalues; k++) {
							isin_current[k] = isin[k];
						}
						isin_current[j] = true;								
						double mheur = node.calcHeuristic(m_MStat, m_CStat);
						//showTest(type, isin_current, -1, mheur, m_MStat, m_CStat);			
						//System.out.println("mheur: " + mheur);
						if (mheur > bheur) {
							bheur = mheur;
							bvalue = j;
							// Calculate pos freq (of current best one)
							pos_freq = m_CStat.m_SumWeight / m_MStat.m_SumWeight;
							node.checkAcceptable(m_MStat, m_CStat);
						}
					}
				}
				if (bvalue != -1) {
					card++;
					isin[bvalue] = true;
					m_PStat.add(node.m_TestStat[bvalue]);
				}
				if (!allowSubsetSplits) {
					// just generate equality tests for nominal attributes
					break;
				}
			}
		}
		//System.out.println("attr: " + type + "  best heur: " + bheur);
		if (bheur > node.m_BestHeur + ClusHeuristic.DELTA) {
			node.m_UnknownFreq = unk_freq;
			node.m_BestHeur = bheur;
			node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
			node.m_BestTest = new SubsetTest(type, card, isin, pos_freq);
			node.resetAlternativeBest();
//			System.out.println("attr: " + type + "  best test: " + node.m_BestTest.getString());
		} else if (getStatManager().getSettings().showAlternativeSplits() && (((bheur > node.m_BestHeur - ClusHeuristic.DELTA) && (bheur < node.m_BestHeur + ClusHeuristic.DELTA)) || (bheur == Double.POSITIVE_INFINITY))) {
			// if same heuristic: add to alternatives (list will later be pruned to remove those tests that do
			// not yield exactly the same subsets)
			node.addAlternativeBest(new SubsetTest(type, card, isin, pos_freq));
		}
	}

	
  public void findRandomSplit(CurrentBestTestAndHeuristic node, NominalAttrType type, Random rn) {
    double unk_freq = 0.0;
    int nbvalues = type.getNbValues();
    boolean isin[] = new boolean[nbvalues];
    // If has missing values?
    if (type.hasMissing()) {
      ClusStatistic unknown = node.m_TestStat[nbvalues];
      m_MStat.copy(node.m_TotStat);
      m_MStat.subtractFromThis(unknown);
      unk_freq = unknown.m_SumWeight / node.m_TotStat.m_SumWeight;
    } else {
      m_MStat.copy(node.m_TotStat);
    }
    int card = 0;
    double pos_freq = 0.0;
    // Generate non-empty and non-full subset
    while (true) {
      for (int i = 0; i < isin.length; i++) {
        isin[i] = rn.nextBoolean();
      }
      int sum = 0;
      for (int i = 0; i < isin.length; i++) {
        if (isin[i]) {
          sum++;
        }
      }
      if (!((sum == 0) || (sum == nbvalues))) {
        card = sum;
        break;
      }
    }
    // Calculate statistics ...
    m_PStat.reset();
    for (int j = 0; j < nbvalues; j++) {
      if (isin[j]) {
         	m_PStat.add(node.m_TestStat[j]);
      }
    }
    pos_freq = m_PStat.m_SumWeight / m_MStat.m_SumWeight;
    node.m_UnknownFreq = unk_freq;
    node.m_BestHeur = node.calcHeuristic(m_MStat, m_PStat);
    node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
    node.m_BestTest = new SubsetTest(type, card, isin, pos_freq);
  }
  
  // makes a random subsetsplit for a given attribute, where the subset includes one value and excludes another value
  // used in PERT trees
  public void findRandomPertSplit(CurrentBestTestAndHeuristic node, NominalAttrType type, Random rn, int valueincl, int valueexcl) {
	    int nbvalues = type.getNbValues();
	    boolean isin[] = new boolean[nbvalues];

	    //	Generate non-empty and non-full subset
	    int card = 0;
	    double pos_freq = 0.0;
	    while (true) {
	      for (int i = 0; i < isin.length; i++) {
	        isin[i] = rn.nextBoolean();
	      }
	      isin[valueincl] = true;
	      isin[valueexcl] = false;
	      int sum = 0;
	      for (int i = 0; i < isin.length; i++) {
	        if (isin[i]) {
	          sum++;
	        }
	      }
	      if (!((sum == 0) || (sum == nbvalues))) {
	        card = sum;
	        break;
	      }
	    }
	    // Calculate statistics ...
	    m_PStat.reset();
	    for (int j = 0; j < nbvalues; j++) {
	      if (isin[j]) {
	         	m_PStat.add(node.m_TestStat[j]);
	      }
	    }
	    node.m_TestType = CurrentBestTestAndHeuristic.TYPE_TEST;
	    node.m_BestTest = new SubsetTest(type, card, isin, pos_freq);
	  }
  
}
