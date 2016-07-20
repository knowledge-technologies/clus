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

import clus.algo.tdidt.*;
import clus.data.attweights.*;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.error.ClusError;
import clus.error.ClusErrorList;
import clus.error.ClusSumError;
import clus.error.MSError;
import clus.error.MSNominalError;
import clus.error.MisclassificationError;
import clus.util.*;

public class CartPruning extends PruneTree {

	protected int[] m_MaxSize;
	protected ClusAttributeWeights m_Weights;
	protected double m_U1, m_U2;
	protected boolean m_IsMSENominal;
	protected ClusError m_ErrorMeasure;

	public CartPruning(ClusAttributeWeights weights, boolean isMSENominal) {
		m_Weights = weights;
		m_IsMSENominal = isMSENominal;
	}

	public CartPruning(int[] maxsize, ClusAttributeWeights weights) {
		m_Weights = weights;
		m_MaxSize = maxsize;
	}

	public int getNbResults() {
		return Math.max(1, m_MaxSize.length);
	}

	public void prune(int result, ClusNode node) throws ClusException {
		int size = m_MaxSize[result];
		TreeErrorComputer.recursiveInitialize(node, new CartVisitor());
		internalInitialize(node);
		while (node.getNbNodes() > size) {
			internalSequenceNext(node);
		}
	}

	public ClusErrorList createErrorMeasure(RowData data, ClusAttributeWeights weights) {
		ClusSchema schema = data.getSchema();
		ClusErrorList parent = new ClusErrorList();
		NumericAttrType[] num = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_CLUSTERING);
		if (nom.length != 0 && num.length != 0) {
			MSError numErr = new MSError(parent, num, weights);
			MSNominalError nomErr = new MSNominalError(parent, nom, weights);
			ClusSumError error = new ClusSumError(parent);
			error.addComponent(numErr);
			error.addComponent(nomErr);
			parent.addError(m_ErrorMeasure = error);
		} else {
			if (nom.length != 0) {
				if (m_IsMSENominal) parent.addError(m_ErrorMeasure = new MSNominalError(parent, nom, weights));
				else parent.addError(m_ErrorMeasure = new MisclassificationError(parent, nom));
			}
			if (num.length != 0) {
				parent.addError(m_ErrorMeasure = new MSError(parent, num, weights));
			}
		}
		parent.setWeights(weights);
		return parent;
	}

	public void sequenceInitialize(ClusNode node) {
		TreeErrorComputer.recursiveInitialize(node, new CartVisitor());
		setOriginalTree(node);
	}

	public void sequenceReset() {
		setCurrentTree(null);
	}

	public ClusNode sequenceNext() {
		ClusNode result = getCurrentTree();
		if (result == null) {
			result = getOriginalTree().cloneTreeWithVisitors();
			internalInitialize(result);
		} else {
			if (result.atBottomLevel()) {
				return null;
			} else {
				internalSequenceNext(result);
			}
		}
		setCurrentTree(result);
		return result;
	}

	public void sequenceToElemK(ClusNode node, int k) {
		internalInitialize(node);
		for (int i = 0; i < k; i++) {
			internalSequenceNext(node);
		}
	}

	public void initU(ClusNode node) {
		CartVisitor cart = (CartVisitor)node.getVisitor();
		m_U1 = 1 + cart.delta_u1;
		m_U2 = m_ErrorMeasure.computeLeafError(node.getClusteringStat()) + cart.delta_u2;
		// System.out.println("Leaves: "+m_U1+" error: "+m_U2);
	}

	public final static double getLambda(ClusNode node) {
		CartVisitor cart = (CartVisitor)node.getVisitor();
		return cart.lambda;
	}

	public final static double getLambdaMin(ClusNode node) {
		CartVisitor cart = (CartVisitor)node.getVisitor();
		return cart.lambda_min;
	}

	public final static void updateLambdaMin(ClusNode node) {
		CartVisitor cart = (CartVisitor)node.getVisitor();
		cart.lambda_min = cart.lambda;
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClusNode ch = (ClusNode)node.getChild(i);
			cart.lambda_min = Math.min(cart.lambda_min, getLambdaMin(ch));
		}
	}

	public final static void updateLambda(ClusNode node) {
		CartVisitor cart = (CartVisitor)node.getVisitor();
		cart.lambda = - cart.delta_u2 / cart.delta_u1;
	}

	public final static void subtractDeltaU(ClusNode node, double d_u1, double d_u2) {
		CartVisitor cart = (CartVisitor)node.getVisitor();
		cart.delta_u1 -= d_u1;
		cart.delta_u2 -= d_u2;
	}

	public void internalSequenceNext(ClusNode node) {
		ClusNode cr_node_t = node;
		double lambda_min_t0 = getLambdaMin(node);
		// Find node "t" for which getLambda(t) == lambda_min_t0
		while (getLambda(cr_node_t) > lambda_min_t0) {
			ClusNode ch1 = (ClusNode)cr_node_t.getChild(0);
			ClusNode ch2 = (ClusNode)cr_node_t.getChild(1);
			if (getLambdaMin(ch1) == lambda_min_t0) {
				cr_node_t = ch1;
			} else {
				cr_node_t = ch2;
			}
		}
		// Prune node "t"
		cr_node_t.makeLeaf();
		CartVisitor cart_t = (CartVisitor)cr_node_t.getVisitor();
		double delta_u1 = cart_t.delta_u1;
		double delta_u2 = cart_t.delta_u2;
		cart_t.lambda_min = Double.POSITIVE_INFINITY;
		// Update nodes on path to parent
		while (!cr_node_t.atTopLevel()) {
			cr_node_t = (ClusNode)cr_node_t.getParent();
			subtractDeltaU(cr_node_t, delta_u1, delta_u2);
			updateLambda(cr_node_t);
			updateLambdaMin(cr_node_t);
		}
		m_U1 -= delta_u1; m_U2 -= delta_u2;
		// System.out.println("Leaves: "+m_U1+" error: "+m_U2);
	}

	public void internalInitialize(ClusNode node) {
		internalRecursiveInitialize(node);
		initU(node);
	}

	public void internalRecursiveInitialize(ClusNode node) {
		int nb_c = node.getNbChildren();
		for (int i = 0; i < nb_c; i++) {
			internalRecursiveInitialize((ClusNode)node.getChild(i));
		}
		CartVisitor cart = (CartVisitor)node.getVisitor();
		if (nb_c == 0) {
			cart.delta_u1 = 0; // number of leaves
			cart.delta_u2 = 0; // training set error
			cart.lambda_min = Double.POSITIVE_INFINITY;
		} else {
			cart.delta_u1 = node.getNbLeaves() - 1;
			double leaf_err = m_ErrorMeasure.computeLeafError(node.getClusteringStat());
			double tree_err = m_ErrorMeasure.computeTreeErrorClusteringAbsolute(node);
			cart.delta_u2 = tree_err - leaf_err;
			updateLambda(node);
			updateLambdaMin(node);
		}
	}
}

