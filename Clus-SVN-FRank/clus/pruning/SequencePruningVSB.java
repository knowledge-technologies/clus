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

package clus.pruning;

import java.util.*;

import jeans.io.MyFile;

import clus.algo.tdidt.*;
import clus.data.attweights.*;
import clus.data.rows.*;
import clus.error.*;

public class SequencePruningVSB extends PruneTree {

	protected RowData m_VSB;
	protected ClusErrorList m_Error;
	protected ClusAttributeWeights m_Weights;
	protected PruneTree m_SeqPruner;
	protected boolean m_1SERule;
	protected boolean m_HasMissing;
	protected String m_Output;

	public SequencePruningVSB(RowData data, ClusAttributeWeights weights) {
			m_VSB = data;
			m_Weights = weights;
			m_HasMissing = true;
	}

	public int getNbResults() {
		return 1;
	}

	public void setSequencePruner(PruneTree pruner) {
		m_SeqPruner = pruner;
		m_Error = pruner.createErrorMeasure(m_VSB, m_Weights);
	}

	public PruneTree getSequencePruner() {
		return m_SeqPruner;
	}

	public static void resize(ArrayList list, int size) {
		if (size > list.size()) {
			list.ensureCapacity(size);
			int nb_add = size - list.size();
			for (int i = 0; i < nb_add; i++) {
				list.add(null);
			}
		}
	}

	public void setOutputFile(String output) {
		m_Output = output;
	}

	public void set1SERule(boolean enable) {
		m_1SERule = enable;
	}

	public void setHasMissing(boolean missing) {
		m_HasMissing = missing;
	}

	public void prune(ClusNode node) {
		PruneTree pruner = getSequencePruner();
		pruner.sequenceInitialize(node);
		// Optimization if data set has missing values
		// if (!m_HasMissing) {
		//	try {
		//		ClusError err = m_Error.getFirstError();
		//		TreeErrorComputer.initializeTestErrorsData(node, m_VSB, err);
		//	} catch (IOException e) { /* Can never occur */ }
		//}
		// Create list of error measures
		ArrayList vsb_errors = new ArrayList();
		ArrayList train_errors = new ArrayList();
		ArrayList sizes = new ArrayList();
		// Compute the error for all sizes
		int max_idx = -1;
		boolean done = false;
		pruner.sequenceReset();
		while (!done) {
			ClusNode pruned = pruner.sequenceNext();
			if (pruned == null) {
				done = true;
			} else {
				max_idx++;
				resize(vsb_errors, max_idx+1); resize(train_errors, max_idx+1); resize(sizes, max_idx+1);
				ClusError vsb_err = TreeErrorComputer.computeClusteringErrorStandard(pruned, m_VSB, m_Error);
				vsb_errors.set(max_idx, vsb_err);
				train_errors.set(max_idx, new Double(pruned.estimateClusteringSS(m_Weights)));
				sizes.set(max_idx, new Integer(pruned.getNbNodes()));
			}
		}
		// Find the size corresponding to the smallest error
		int best_idx = 0;
		double min_error = Double.POSITIVE_INFINITY;
		for (int idx = 0; idx <= max_idx; idx++) {
			ClusError error = (ClusError)vsb_errors.get(idx);
			double value = error.getModelError();
			if (value <= min_error) {
				min_error = value;
				best_idx = idx;
			}
		}
		// Find smallest tree within 1-SE.
		ClusError best_error = (ClusError)vsb_errors.get(best_idx);
		double se = best_error.getModelErrorStandardError();
		int se1_idx = 0;
		for (int idx = 0; idx <= max_idx; idx++) {
			ClusError error = (ClusError)vsb_errors.get(idx);
			double value = error.getModelError();
			if (value <= min_error + se) {
				se1_idx = idx;
			}
		}
		if (se1_idx == -1) se1_idx = best_idx;
		ClusError leaf_error = (ClusError)vsb_errors.get(max_idx);
		/*System.out.println("Best size: "+sizes.get(best_idx)+" 1-SE size: "+sizes.get(se1_idx)+
				           " Best MSE: "+min_error+" SE: "+se+" Leaf MSE: "+leaf_error.getModelError());*/
		// Write output to file
		if (m_Output != null) {
			MyFile log = new MyFile(m_Output);
			for (int idx = 0; idx <= max_idx; idx++) {
				ClusError error = (ClusError)vsb_errors.get(idx);
				double value = error.getModelError();
				log.log(sizes.get(idx).toString()+"\t"+train_errors.get(idx)+"\t"+String.valueOf(value));
			}
			log.close();
		}
		// Finally prune given tree to size specified
		if (m_1SERule) {
			pruner.sequenceToElemK(node, se1_idx);
		} else {
			pruner.sequenceToElemK(node, best_idx);
		}
	}
}
