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

package clus.error;

import java.io.PrintWriter;
import java.util.ArrayList;

import clus.data.rows.DataTuple;
import clus.data.type.NominalAttrType;
import clus.main.Settings;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.util.ClusFormat;

/**
 * @author matejp
 *
 * Ranking loss is used in multi-label classification scenario.
 */
public class RankingLoss extends ClusNominalError{
	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_NonnormalisedLoss;// sum over samples sample_i of the terms t_i = |D_i| / (|Y_i| |L \ Y_i|), where L is the set of labels, Y_i is the predicted set of labels,
										 // and D_i is the set of pairs (l1, l2), such that l1 is falsely positive and l2 is falsely negative.
										 // If Y_i is either empty set or it equals L, then t_i = 0
	protected int m_NbKnown;// number of the examples seen 
	
	public RankingLoss(ClusErrorList par, NominalAttrType[] nom) {
		super(par, nom);
		m_NonnormalisedLoss = 0.0;
		m_NbKnown = 0;
	}

	public boolean shouldBeLow() {
		return true;
	}

	public void reset() {
		m_NonnormalisedLoss = 0.0;
		m_NbKnown = 0;
	}

	public void add(ClusError other) {
		RankingLoss rl = (RankingLoss)other;
		m_NonnormalisedLoss += rl.m_NonnormalisedLoss;
		m_NbKnown += rl.m_NbKnown;
	}
	//NEDOTAKNJENO
	public void showSummaryError(PrintWriter out, boolean detail) {
		showModelError(out, detail ? 1 : 0);
	}
//	// A MA TO SPLOH SMISU?
//	public double getRankingLoss(int i) {
//		return getModelErrorComponent(i);
//	}

	public double getModelError() {
		return m_NonnormalisedLoss/ m_NbKnown;
	}
	
	public void showModelError(PrintWriter out, int detail){
		out.println(ClusFormat.FOUR_AFTER_DOT.format(getModelError()));
	}

	public String getName() {
		return "RankingLoss";
	}

	public ClusError getErrorClone(ClusErrorList par) {
		return new RankingLoss(par, m_Attrs);
	}
	// TODO: Optimise this to O(n log n) ...
	public void addExample(DataTuple tuple, ClusStatistic pred) {
		int[] predicted = pred.getNominalPred(); // Codomain is {"1", "0"} - see clus.data.type.NominalAtterType constructor
		double[] scores = ((ClassificationStat) pred).calcScores();
		int wrongPairs = 0;
		ArrayList<Integer> relevantIndices = new ArrayList<Integer>();
		ArrayList<Integer> irrelevantIndices = new ArrayList<Integer>();
		NominalAttrType attr;
		for(int i = 0; i< m_Dim; i++){
			attr = getAttr(i);
			if(!attr.isMissing(tuple)){
				if(attr.getNominal(tuple) == 0){
					relevantIndices.add(i);
				} else{
					irrelevantIndices.add(i);
				}
			}
		}
		
		if(!relevantIndices.isEmpty() && !irrelevantIndices.isEmpty()){
			for(int relevantInd : relevantIndices){
				for(int irrelevantInd : irrelevantIndices){
					if(scores[irrelevantInd] > scores[relevantInd]){
						wrongPairs++;
					}
				}
			}
			m_NonnormalisedLoss += ((double) wrongPairs) / (relevantIndices.size() * irrelevantIndices.size());
		}
		m_NbKnown++;
	}

	public void addExample(DataTuple tuple, DataTuple pred){
		try {
			throw new Exception("RankingLoss.addExample(DataTuple tuple, DataTuple pred) not implemented yet.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//NEDOTAKNJENO
	public void addInvalid(DataTuple tuple) {
	}

}
