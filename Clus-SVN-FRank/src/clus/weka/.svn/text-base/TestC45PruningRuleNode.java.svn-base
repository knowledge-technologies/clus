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

package clus.weka;

import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.main.Settings;
import clus.model.test.*;
import weka.core.*;
import weka.classifiers.trees.j48.*;

public class TestC45PruningRuleNode extends C45PruneableClassifierTree {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public TestC45PruningRuleNode(Instances all) throws Exception {
		 super(new BinC45ModelSelection(2, all), true, 0.25f, false, true);
	}

	public TestC45PruningRuleNode(ModelSelection sel) throws Exception {
		 super(sel, true, 0.25f, false, true);
	}

	public static void createC45RuleNodeRecursive(TestC45PruningRuleNode result, ClusNode node, RowData data, ClusToWekaData cnv) throws Exception {
		result.m_train = cnv.convertData(data);
		result.m_isEmpty = data.getNbRows() == 0;
		result.m_isLeaf = node.atBottomLevel();
		NodeTest tst = node.getTest();
		int idx = tst == null ? -1 : cnv.getIndex(tst.getType().getName());
		MyBinC45Split split = new MyBinC45Split(idx, 2, result.m_train.sumOfWeights());
		result.m_localModel = split;
		Distribution distr = null;
		if (tst != null) {
			distr = new Distribution(2, result.m_train.numClasses());
			if (tst instanceof NumericTest) split.setSplitPoint(((NumericTest)tst).getBound());
		} else {
			distr = new Distribution(result.m_train);
		}
		split.setDistribution(distr);
		if (node.getNbChildren() != 0) {
			result.m_sons = new ClassifierTree[2];
			for (int i = 0; i < 2; i++) {
				TestC45PruningRuleNode child = new TestC45PruningRuleNode(result.m_toSelectModel);
				RowData subset = data.applyWeighted(tst, i);
				Instances subset_inst = cnv.convertData(subset);
				for (int j = 0; j < subset_inst.numInstances(); j++) {
					Instance inst = subset_inst.instance(j);
					distr.add(i, inst);
				}
				createC45RuleNodeRecursive(child, (ClusNode)node.getChild(i), subset, cnv);
				result.m_sons[i] = child;
			}
		}
	}

	public static TestC45PruningRuleNode createC45RuleNode(ClusNode node, RowData data, ClusToWekaData cnv) throws Exception {
		Instances all = cnv.convertData(data);
		TestC45PruningRuleNode result = new TestC45PruningRuleNode(all);
		createC45RuleNodeRecursive(result, node, data, cnv);
		return result;
	}

	public static void performTest(ClusNode original, ClusNode pruned, RowData data) {
		ClusToWekaData cnv = new ClusToWekaData(data.getSchema());
		try {
			TestC45PruningRuleNode tree = createC45RuleNode(original, data, cnv);
			System.out.println("Original tree:");
			System.out.println(tree.toString());
			System.out.println("***");
			tree.collapse();
			tree.prune();
			System.out.println("Resulting tree:");
			System.out.println(tree.toString());
			System.out.println("***");
		} catch (Exception e) {
			System.err.println("Exception: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static class MyBinC45Split extends BinC45Split {

			public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

			public MyBinC45Split(int attIndex,int minNoObj,double sumOfWeights) {
				super(attIndex, minNoObj, sumOfWeights);
			}

			public void setDistribution(Distribution d) {
				m_distribution = d;
			}

			public void setSplitPoint(double v) {
				// m_splitPoint = v;
			}
	}
}
