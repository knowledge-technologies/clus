/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.algo.tdidt;

import java.io.IOException;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.algo.rules.ClusRulesFromTree;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.bestfirst.BestFirstInduce;
import si.ijs.kt.clus.ext.ilevelc.ILevelCInduce;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsOutput.ConvertRules;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.pruning.PruneTree;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;

public class ClusDecisionTree extends ClusInductionAlgorithmType {

	public final static int LEVEL_WISE = 0;
	public final static int DEPTH_FIRST = 1;

	public ClusDecisionTree(Clus clus) {
		super(clus);
	}

	@Override
	public void printInfo() {
		System.out.println("TDIDT");
		System.out.println("Heuristic: " + getStatManager().getHeuristicName());
	}

	@Override
	public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs)
			throws ClusException, IOException {

		if (sett.getConstraints().hasConstraintFile()) {
			boolean fillin = cargs.hasOption("fillin");
			return new ConstraintDFInduce(schema, sett, fillin);
		} else if (sett.getILevelC().isSectionILevelCEnabled()) {
			return new ILevelCInduce(schema, sett);
		} else if (schema.isSparse()) {
			return new DepthFirstInduceSparse(schema, sett);
		} else {
			switch (sett.getTree().getInductionOrder()) {
			case DepthFirst:
				return new DepthFirstInduce(schema, sett);
			case BestFirst:
			default:
				return new BestFirstInduce(schema, sett);
			}
		}
	}

	/*
	 * @Deprecated // this should not be used for default model induction public
	 * final static ClusNode pruneToRoot(ClusNode orig) { ClusNode pruned =
	 * (ClusNode) orig.cloneNode(); pruned.makeLeaf(); return pruned; }
	 */

	public static ClusModel induceDefault(ClusRun cr) throws ClusException, InterruptedException {
		ClusNode node = new ClusNode();
		RowData data = (RowData) cr.getTrainingSet();
		node.initTargetStat(cr.getStatManager(), data);
		node.computePrediction();
		node.makeLeaf();
		return node;
	}

	/**
	 * Convert the tree to rules
	 * 
	 * @param cr
	 * @param model
	 *            ClusModelInfo to convert to rules (default, pruned, original).
	 * @throws ClusException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void convertToRules(ClusRun cr, ClusModelInfo model)
			throws ClusException, IOException, InterruptedException {
		ClusNode tree_root = (ClusNode) model.getModel();
		ClusRulesFromTree rft = new ClusRulesFromTree(true, getSettings().getTree().rulesFromTree());
		ClusRuleSet rule_set = null;
		boolean compDis = getSettings().getRules().computeDispersion(); // Do we want to compute dispersion

		rule_set = rft.constructRules(cr, tree_root, getStatManager(), compDis,
				getSettings().getRules().getRulePredictionMethod());
		rule_set.addDataToRules((RowData) cr.getTrainingSet());

		ClusModelInfo rules_info = cr.addModelInfo("Rules-" + model.getName());
		rules_info.setModel(rule_set);
	}

	@Override
	public void pruneAll(ClusRun cr) throws ClusException, IOException, InterruptedException {
		ClusNode orig = (ClusNode) cr.getModel(ClusModel.ORIGINAL);
		orig.numberTree();
		PruneTree pruner = getStatManager().getTreePruner(cr.getPruneSet());
		pruner.setTrainingData((RowData) cr.getTrainingSet());
		int nb = pruner.getNbResults();
		for (int i = 0; i < nb; i++) {
			ClusModelInfo pruned_info = pruner.getPrunedModelInfo(i, orig);
			cr.addModelInfo(pruned_info);
		}
	}

	@Override
	public final ClusModel pruneSingle(ClusModel orig, ClusRun cr) throws ClusException, InterruptedException {
		ClusNode pruned = (ClusNode) ((ClusNode) orig).cloneTree();
		PruneTree pruner = getStatManager().getTreePruner(cr.getPruneSet());
		pruner.setTrainingData((RowData) cr.getTrainingSet());
		pruner.prune(pruned);
		return pruned;
	}

	/**
	 * Post processing decision tree. E.g. converting to rules.
	 * 
	 * @throws InterruptedException
	 *
	 */
	@Override
	public void postProcess(ClusRun cr) throws ClusException, IOException, InterruptedException {
		if (!getSettings().getTree().rulesFromTree().equals(ConvertRules.No)) {
			ClusModelInfo model = cr.getModelInfoFallback(ClusModel.PRUNED, ClusModel.ORIGINAL);
			convertToRules(cr, model);
		}
	}
}
