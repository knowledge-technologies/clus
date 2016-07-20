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

/* Created on May 1, 2005
 */
package clus.algo.rules;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import clus.Clus;
import clus.algo.*;
import clus.algo.split.*;
import clus.algo.tdidt.*;
import clus.main.*;
import clus.model.*;
import clus.model.test.*;
import clus.heuristic.*;
import clus.selection.*;
import clus.statistic.*;
import clus.data.attweights.ClusNormalizedAttributeWeights;
import clus.data.rows.*;
import clus.data.type.*;
import clus.ext.beamsearch.*;
import clus.ext.ilevelc.DerivedConstraintsComputer;
import clus.ext.ilevelc.ILevelCHeurStat;
import clus.ext.ilevelc.ILevelCStatistic;
import clus.ext.ilevelc.ILevelConstraint;
import clus.util.*;
import clus.tools.optimization.GDAlg;
import clus.tools.optimization.OptAlg;
import clus.tools.optimization.OptProbl;
import clus.tools.optimization.de.*;

public class ClusRuleConstraintInduce extends ClusInductionAlgorithm {

	protected boolean m_BeamChanged;
	protected FindBestTestRules m_FindBestTest;
	protected ClusHeuristic m_Heuristic;
	protected ArrayList<ILevelConstraint> m_Constraints;
	protected int size;
	protected ClusRuleConstraintInduceTest m_BestTest;
	protected double m_BestHeur;
	protected ArrayList<ILevelConstraint> m_BestConstraints;
	protected ClusNormalizedAttributeWeights m_Scale;
	protected RowData m_Data;
	private double m_Global_Var;
	protected double m_Alfa = 0.1;
	protected double m_Gamma = 0.5;
	private int m_MaxNbClasses;

	public ClusRuleConstraintInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
		super(schema, sett);
		m_FindBestTest = new FindBestTestRules(getStatManager());
	}

	void resetAll() {
		m_BeamChanged = false;
	}

	public void setHeuristic(ClusHeuristic heur) {
		m_Heuristic = heur;
	}
	
	//recalculate heuristic
	public double estimateBeamMeasure(ClusRule rule) {
		ClusStatistic cs = rule.m_ClusteringStat;
		double var = cs.getSVarS(m_Scale)/m_Global_Var;
		double con1 = rule.getNumberOfViolatedConstraintsRCCC();
		double con2 = rule.getConstraints().size();
		double con;
		if(con2 != 0)
			 con = con1/con2;
		else
			con = 0;
		double cov1 = ((RowData) rule.getVisitor()).toArrayList().size();
		double cov2 = m_Data.getNbRows();
		double cov = cov1/cov2;
		double gamma = m_Gamma;
		double alfa = m_Alfa;
		double result = (1-((1-gamma)*var+gamma*con))*(Math.pow(cov, alfa));
		return result;
	}

	public boolean isBeamChanged() {
		return m_BeamChanged;
	}

	public void setBeamChanged(boolean change) {
		m_BeamChanged = change;
	}

	ClusBeam initializeBeam(RowData data) {
		Settings sett = getSettings();
		ClusBeam beam = new ClusBeam(sett.getBeamWidth(), sett.getBeamRemoveEqualHeur());
		ClusStatistic stat = createTotalClusteringStat(data);
		ClusRule rule = new ClusRule(getStatManager());
		rule.setClusteringStat(stat);
		rule.setVisitor(data);
		//add constraints to rule
		Iterator<ILevelConstraint> i = m_Constraints.iterator();
		ArrayList<ILevelConstraint> c = new ArrayList<ILevelConstraint>();
		ArrayList<DataTuple> ds = data.toArrayList();
		while(i.hasNext()){
			ILevelConstraint ilc = i.next();
			if(ds.contains(ilc.getT1())||ds.contains(ilc.getT2()))
				c.add(ilc);
		}
		rule.setConstraints(c);
		double value = estimateBeamMeasure(rule);
		beam.addModel(new ClusBeamModel(value, rule));
		return beam;
	}

	public void refineModel(ClusBeamModel model, ClusBeam beam, int model_idx) {
		ClusRule rule = (ClusRule)model.getModel();
		RowData data = (RowData)rule.getVisitor();
		ArrayList<ILevelConstraint> constraints = (ArrayList<ILevelConstraint>) rule.getConstraints();
		if (m_FindBestTest.initSelectorAndStopCrit(rule.getClusteringStat(), data)) {
			model.setFinished(true);
			return;
		}
		//dataset is too small
		if(((RowData)rule.getVisitor()).getNbRows() <= 2){
			model.setFinished(true);
			return;
		}
		ClusAttrType[] attrs = data.getSchema().getDescriptiveAttributes();
		for (int i = 0; i < attrs.length; i++) {
			double beam_min_value = beam.getMinValue();
			ClusAttrType at = attrs[i];
			if (at instanceof NominalAttrType)
				findNominal((NominalAttrType)at, data, constraints);
			else
				try {
					findNumeric((NumericAttrType)at, data, constraints, rule, size);
				} catch (ClusException e) {
					// do nothing
				}
				System.out.println("Best test: "+m_BestTest);
			if(m_BestTest != null && m_BestHeur != Double.NEGATIVE_INFINITY){
				ClusRuleConstraintInduceTest test = m_BestTest;if (Settings.VERBOSE > 0) System.out.println("  Test: "+test.getString()+" -> "+m_BestHeur);
				RowData subset;
				if(test.isSmallerThanTest())
					subset = data.applyConstraint(test, ClusNode.NO);
				else
					subset = data.applyConstraint(test, ClusNode.YES);
				ClusRule ref_rule = rule.cloneRule();
				ref_rule.addTest(test);
				ref_rule.setVisitor(subset);
				ref_rule.setClusteringStat(createTotalClusteringStat(subset));
				ref_rule.setConstraints(m_BestConstraints);
				if (getSettings().isHeurRuleDist()) {
					int[] subset_idx = new int[subset.getNbRows()];
					for (int j = 0; j < subset_idx.length; j++) {
						subset_idx[j] = subset.getTuple(j).getIndex();
					}
					((ClusRuleHeuristicDispersion)m_Heuristic).setDataIndexes(subset_idx);
				}
				double new_heur = sanityCheck(m_BestHeur, ref_rule);
				// Check for sure if _strictly_ better!
				if (new_heur > beam_min_value) {
					ClusBeamModel new_model = new ClusBeamModel(new_heur, ref_rule);
					new_model.setParentModelIndex(model_idx);
					beam.addModel(new_model);
					setBeamChanged(true);
				}
			}
		}
	}

	public void refineBeam(ClusBeam beam) {
		setBeamChanged(false);
		ArrayList models = beam.toArray();
		m_BestTest = null;
		m_BestHeur = Double.NEGATIVE_INFINITY;
		m_BestConstraints = null;
		for (int i = 0; i < models.size(); i++) {
			ClusBeamModel model = (ClusBeamModel)models.get(i);
			if (!(model.isRefined() || model.isFinished())) {
				//System.out.println("Refine "+model.toString());
				// if (Settings.VERBOSE > 0) System.out.println("  Refine: model " + i);
				refineModel(model, beam, i);
				model.setRefined(true);
				model.setParentModelIndex(-1);
			}
		}
	}

	public ClusRule learnOneRule(RowData data) {
		ClusBeam beam = initializeBeam(data);
		int i = 0;
		while (true) {
			if (Settings.VERBOSE > 0) {
				System.out.println("Step: " + i);
			} else {
				if (i != 0) {
					System.out.print(",");
				}
				System.out.print(i);
			}
			System.out.flush();
			refineBeam(beam);
			System.out.println();
			if (!isBeamChanged()) {
				break;
			}
			i++;
		}
		System.out.println();
		double best = beam.getBestModel().getValue();
		double worst = beam.getWorstModel().getValue();
		System.out.println("Worst = "+worst+" Best = "+best);
		ClusRule result = (ClusRule)beam.getBestAndSmallestModel().getModel();
		// Create target statistic for rule
		RowData rule_data = (RowData)result.getVisitor();
		result.setTargetStat(createTotalTargetStat(rule_data));
		//result.setVisitor(null);
		return result;
	}

	public ClusRule learnEmptyRule(RowData data) {
		ClusRule result = new ClusRule(getStatManager());
		// Create target statistic for rule
		// RowData rule_data = (RowData)result.getVisitor();
		// result.setTargetStat(m_Induce.createTotalTargetStat(rule_data));
		// result.setVisitor(null);
		return result;
	}

	/**
	 * Returns all the rules in the beam, not just the best one.
	 * @param data
	 * @return array of rules
	 */
	public ClusRule[] learnBeamOfRules(RowData data) {
		ClusBeam beam = initializeBeam(data);
		int i = 0;
		System.out.print("Step: ");
		while (true) {
			if (Settings.VERBOSE > 0) {
				System.out.println("Step: " + i);
			} else {
				if (i != 0) {
					System.out.print(",");
				}
				System.out.print(i);
			}
			System.out.flush();
			refineBeam(beam);
			if (!isBeamChanged()) {
				break;
			}
			i++;
		}
		System.out.println();
		double best = beam.getBestModel().getValue();
		double worst = beam.getWorstModel().getValue();
		System.out.println("Worst = "+worst+" Best = "+best);
		ArrayList beam_models = beam.toArray();
		ClusRule[] result = new ClusRule[beam_models.size()];
		for (int j = 0; j < beam_models.size(); j++) {
			// Put better models first
			int k = beam_models.size()-j-1;
			ClusRule rule = (ClusRule)((ClusBeamModel)beam_models.get(k)).getModel();
			// Create target statistic for this rule
			RowData rule_data = (RowData)rule.getVisitor();
			rule.setTargetStat(createTotalTargetStat(rule_data));
			rule.setVisitor(null);
			rule.simplify();
			result[j] = rule;
		}
		return result;
	}

	public void separateAndConquor(ClusRuleSet rset, RowData data) {
		while (data.getNbRows() > 0) {
			ClusRule rule = learnOneRule(data);
			if (rule.isEmpty()) {
				break;
			} else {
				rule.computePrediction();
				rule.printModel();
				System.out.println();
				rset.add(rule);
				data = rule.removeCovered(data);
			}
		}
		ClusStatistic left_over = createTotalTargetStat(data);
		left_over.calcMean();
		System.out.println("Left Over: "+left_over);
		rset.setTargetStat(left_over);
	}

	/**
	 * separateAndConquor method which uses re-weighting
	 * @param rset
	 * @param data
	 * @throws ClusException
	 */
	public void separateAndConquorWeighted(ClusRuleSet rset, RowData data) throws ClusException {
		int max_rules = getSettings().getMaxRulesNb();
		int i = 0;
		RowData data_copy = (RowData)data.deepCloneData(); // Probably not nice
		ArrayList<boolean[]> bit_vect_array = new ArrayList<boolean[]>();
		while ((data.getNbRows() > 0) && (i < max_rules)) {
			ClusRule rule = learnOneRule(data);
			if (rule.isEmpty()) {
				break;
			} else {
				rule.computePrediction();
				rule.printModel();
				System.out.println();
				rset.add(rule);
				data = rule.reweighCovered(data);
				i++;
				if (getSettings().isHeurRuleDist()) {
					boolean[] bit_vect = new boolean[data_copy.getNbRows()];
					for (int j = 0; j < bit_vect.length; j++) {
						if (!bit_vect[j]) {
							for (int k = 0; k < rset.getModelSize(); k++) {
								if (rset.getRule(k).covers(data_copy.getTuple(j))) {
									bit_vect[j] = true;
									break;
								}
							}
						}
					}
					bit_vect_array.add(bit_vect);
					((ClusRuleHeuristicDispersion)m_Heuristic).setCoveredBitVectArray(bit_vect_array);
				}
			}
		}
		ClusStatistic left_over = createTotalTargetStat(data);
		left_over.calcMean();
		System.out.println("Left Over: "+left_over);
		rset.setTargetStat(left_over);
	}

	public double sanityCheck(double value, ClusRule rule) {
		double expected = estimateBeamMeasure(rule);
		if (Math.abs(value-expected) > 1e-6) {
			System.out.println("Bug in heurisitc: "+value+" <> "+expected);
			PrintWriter wrt = new PrintWriter(System.out);
			rule.printModel(wrt);
			wrt.close();
			System.out.flush();
			System.exit(1);
		}
		return expected;
	}

	public ClusModel induce(ClusRun run) throws ClusException, IOException {
		int method = getSettings().getCoveringMethod();
		int add_method = getSettings().getRuleAddingMethod();
		RowData data = (RowData)run.getTrainingSet();
		ClusStatistic stat = createTotalClusteringStat(data);
		m_FindBestTest.initSelectorAndSplit(stat);
		setHeuristic(m_FindBestTest.getBestTest().getHeuristic());
		ClusRuleSet rset = new ClusRuleSet(getStatManager());
		if (method == Settings.COVERING_METHOD_STANDARD) {
			separateAndConquor(rset, data);
		} else {
			separateAndConquorWeighted(rset, data);
		}
		rset.postProc();
		// Optimizing rule set
		if (getSettings().getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_OPTIMIZED) {
			rset = optimizeRuleSet(rset, data);
		}
		// Computing dispersion
		if (getSettings().computeDispersion()) {
			rset.addDataToRules(data);
			rset.computeDispersion(ClusModel.TRAIN);
			rset.removeDataFromRules();
			if (run.getTestIter() != null) {
				RowData testdata = (RowData)run.getTestSet();
				rset.addDataToRules(testdata);
				rset.computeDispersion(ClusModel.TEST);
				rset.removeDataFromRules();
			}
		}
		// Number rules (for output prupose in WritePredictions)
		rset.numberRules();
		return rset;
	}

	public ClusRuleSet optimizeRuleSet(ClusRuleSet rset, RowData data) throws ClusException, IOException {
		String fname = getSettings().getDataFile();
		PrintWriter wrt_pred = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname+".r-pred")));
		
		OptAlg optAlg = null;
		
		OptProbl.OptParam param = rset.giveFormForWeightOptimization(wrt_pred, data);
		
		// Find the rule weights with optimization algorithm.
		if (getSettings().getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED) {	
			optAlg = (OptAlg) new GDAlg(getStatManager(), param, rset);
		} else {
			optAlg = (OptAlg) new DeAlg(getStatManager(), param, rset);
		}

		ArrayList weights = optAlg.optimize();
		
		// Print weights of rules
		System.out.print("The weights for rules:");
		for (int j = 0; j < rset.getModelSize(); j++) {
			rset.getRule(j).setOptWeight(((Double)weights.get(j)).doubleValue());
			System.out.print(((Double)weights.get(j)).doubleValue()+ "; ");
		}
		System.out.print("\n");
		rset.removeLowWeightRules();
		RowData data_copy = (RowData)data.cloneData();
		updateDefaultRule(rset, data_copy);
		// TODO: Should I update all the rules also, rerun the optimization?
		return rset;
	}


/*			try {
				// Generate pathseeker input
				ClusStatistic tar_stat = rset.m_StatManager.getStatistic(ClusAttrType.ATTR_USE_TARGET);
				int nb_tar = tar_stat.getNbNominalAttributes();
				boolean classification = false;
				if (rset.m_TargetStat instanceof ClassificationStat) {
					classification = true;
				}
				String fname = getSettings().getDataFile();
				PrintWriter wrt_pred = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname+".pred.dat")));
				PrintWriter wrt_resp = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname+".resp.dat")));
				PrintWriter wrt_train = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname+".train.txt")));
				for (int i = 0; i < data.getNbRows(); i++) {
					DataTuple tuple = data.getTuple(i);
					for (int j = 0; j < rset.getModelSize(); j++) {
						ClusRule rule = rset.getRule(j);
						if (rule.covers(tuple)) {
							if (classification) {
								// TODO: Don't look just at the first target attribute!
								wrt_pred.write("" + ((ClassificationStat)rule.predictWeighted(tuple)).
										                 getNominalPred()[0]);
							} else {
								// TODO: Don't look just at the first target attribute!
								wrt_pred.write("" + ((RegressionStat)rule.predictWeighted(tuple)).
										                 getNumericPred()[0]);
							}
						}
						if ((j+1) < rset.getModelSize()) {
							wrt_pred.write(",");
						}
					}
					wrt_pred.println();
					if (classification) {
						// TODO: Don't look just at the first target attribute!
						wrt_resp.print("" + tuple.getIntVal(0));
						if ((i+1) < data.getNbRows()) {
							wrt_resp.write(",");
						}
					} else {
						// TODO: Don't look just at the first target attribute!
						wrt_resp.print("" + tuple.getDoubleVal(0));
						if ((i+1) < data.getNbRows()) {
							wrt_resp.write(",");
						}
					}
				}
				wrt_resp.println();
				if (classification) {
					wrt_train.println("@mode=class");
				} else {
					wrt_train.println("@mode=regres");
				}
				wrt_train.println("@model_file=" + fname + ".model.pth");
				wrt_train.println("@coeffs_file=" + fname + ".coeffs.pth");
				wrt_train.println("@nvar=" + rset.getModelSize());
				wrt_train.println("@nobs=" + data.getNbRows());
				wrt_train.println("@format=csv");
				wrt_train.println("@response_data=" + fname + ".resp.dat");
				wrt_train.println("@pred_data=" + fname + ".pred.dat");
				wrt_train.println("@org=by_obs");
				wrt_train.println("@missing=9.9e35");
				wrt_train.println("@obs_weights=equal");
				wrt_train.println("@var_weights=equal");
				wrt_train.println("@quantile=0.025 ");
				wrt_train.println("@numspect=0");
				wrt_train.println("@constraints=all");
				wrt_train.println("@nfold=3");
				wrt_train.println("@start=0.0");
				wrt_train.println("@end=1.0");
				wrt_train.println("@numval=6");
				wrt_train.println("@alpha=0.8");
				wrt_train.println("@modsel=a_roc");
				wrt_train.println("@delnu=0.01");
				wrt_train.println("@maxstep=20000");
				wrt_train.println("@kfreq=100");
				wrt_train.println("@convfac=1.1");
				wrt_train.println("@fast=no");
				wrt_train.println("@impl=auto");
				wrt_train.println();
				wrt_pred.close();
				wrt_resp.close();
				wrt_train.close();
				// Run pathseeker
				// Read pathseeker weights
			} catch (Exception e) {
				// TODO: handle exception
			}
*/

	public void updateDefaultRule(ClusRuleSet rset, RowData data) {
		for (int i = 0; i < rset.getModelSize(); i++) {
			data = rset.getRule(i).removeCovered(data);
		}
		ClusStatistic left_over = createTotalTargetStat(data);
		left_over.calcMean();
		System.out.println("Left Over: "+left_over);
		rset.setTargetStat(left_over);
	}

	/**
	 * Method that induces a specified number of random rules.
	 * @param cr ClusRun
	 * @return RuleSet
	 */
	public ClusModel induceRandomly(ClusRun run) throws ClusException, IOException {
		int number = getSettings().nbRandomRules();
		RowData data = (RowData)run.getTrainingSet();
		ClusStatistic stat = createTotalClusteringStat(data);
		m_FindBestTest.initSelectorAndSplit(stat);
		setHeuristic(m_FindBestTest.getBestTest().getHeuristic()); // ???
		ClusRuleSet rset = new ClusRuleSet(getStatManager());
		Random rn = new Random(42);
		for (int i = 0; i < number; i++) {
			ClusRule rule = generateOneRandomRule(data,rn);
			rule.computePrediction();
			rule.printModel();
			System.out.println();
			if (!rset.addIfUnique(rule)) {
				i--;
			}
		}
		ClusStatistic left_over = createTotalTargetStat(data);
		left_over.calcMean();
		System.out.println("Left Over: "+left_over);
		rset.setTargetStat(left_over);
		rset.postProc();
		// Computing dispersion
		if (getSettings().computeDispersion()) {
			rset.addDataToRules(data);
			rset.computeDispersion(ClusModel.TRAIN);
			rset.removeDataFromRules();
			if (run.getTestIter() != null) {
				RowData testdata = (RowData)run.getTestSet();
				rset.addDataToRules(testdata);
				rset.computeDispersion(ClusModel.TEST);
				rset.removeDataFromRules();
			}
		}
		return rset;
	}

	/**
	 * Generates one random rule.
	 * @param data
	 * @param rn
	 * @return
	 */
	private ClusRule generateOneRandomRule(RowData data, Random rn) {
		// TODO: Remove/change the beam stuff!!!
		// Jans: Removed beam stuff (because was more difficult to debug)
		ClusStatManager mgr = getStatManager();
		ClusRule result = new ClusRule(mgr);
		ClusAttrType[] attrs = data.getSchema().getDescriptiveAttributes();
		// Pointer to the complete data set
		RowData orig_data = data;
		// Generate number of tests
		int nb_tests;
		if (attrs.length > 1) {
			nb_tests = rn.nextInt(attrs.length - 1) + 1;
		} else {
			nb_tests = 1;
		}
		// Generate attributes in these tests
		int[] test_atts = new int[nb_tests];
		for (int i = 0; i < nb_tests; i++) {
			while (true) {
				int att_idx = rn.nextInt(attrs.length);
				boolean unique = true;
				for (int j = 0; j < i; j++) {
					if (att_idx == test_atts[j]) {
						unique = false;
					}
				}
				if (unique) {
					test_atts[i] = att_idx;
					break;
				}
			}
		}
		CurrentBestTestAndHeuristic sel = m_FindBestTest.getBestTest();
		for (int i = 0; i < test_atts.length; i++) {
			result.setClusteringStat(createTotalClusteringStat(data));
			if (m_FindBestTest.initSelectorAndStopCrit(result.getClusteringStat(), data)) {
				// Do not add test if stop criterion succeeds (???)
				break;
			}
			sel.resetBestTest();
			sel.setBestHeur(Double.NEGATIVE_INFINITY);
			ClusAttrType at = attrs[test_atts[i]];
			if (at instanceof NominalAttrType) {
				m_FindBestTest.findNominalRandom((NominalAttrType)at, data, rn);
			} else {
				m_FindBestTest.findNumericRandom((NumericAttrType)at, data, orig_data, rn);
			}
			if (sel.hasBestTest()) {
				NodeTest test = sel.updateTest();
				if (Settings.VERBOSE > 0) System.out.println("  Test: "+test.getString()+" -> "+sel.m_BestHeur);
				result.addTest(test);
				// data = data.applyWeighted(test, ClusNode.YES);
				data = data.apply(test, ClusNode.YES);           // ???
			}
		}
		// Create target and clustering statistic for rule
		result.setTargetStat(createTotalTargetStat(data));
		result.setClusteringStat(createTotalClusteringStat(data));
		return result;
	}

	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		// ClusRulesForAttrs rfa = new ClusRulesForAttrs();
		// return rfa.constructRules(cr);
		resetAll();
		if (!getSettings().isRandomRules()) {
			return induce(cr);
		} else {
			return induceRandomly(cr);
		}
	}

	public void induceAll(ClusRun cr) throws ClusException, IOException {
		//import constraints
		ClusRandom.reset(ClusRandom.RANDOM_ALGO_INTERNAL);
		RowData data = (RowData)cr.getTrainingSet();
		int nbRows = data.getNbRows();
		/* add in test data! */
		RowData test = (RowData)cr.getTestSet();
		if (test != null) {
			ArrayList allData = new ArrayList();
			data.addTo(allData);
			test.addTo(allData);
			data = new RowData(allData, data.getSchema());
		}
		System.out.println("All data: "+data.getNbRows());
		size = data.getNbRows();
		data.addIndices();
		ArrayList points = data.toArrayList();
		/* load constraints from file */
		if (getSettings().hasILevelCFile()) {
			String fname = getSettings().getILevelCFile();
			m_Constraints = ILevelConstraint.loadConstraints(fname, points);
			ClusAttrType type = getSchema().getAttrType(getSchema().getNbAttributes()-1);
			if (type.getTypeIndex() == NominalAttrType.THIS_TYPE) {
				NominalAttrType cls = (NominalAttrType)type;
				m_MaxNbClasses = cls.getNbValues();
			}
		} else {
			m_Constraints = createConstraints(data, nbRows);
		}
		//add extra constraints => performancekiller
		//ML(a,b) + ML(b,c) => ML(a,c)
		//ML(a,b) + CL(b,c) => CL(a,c)
		//createExtraConstraints(m_Constraints, data.toArrayList());
		System.out.println("All constraints: "+m_Constraints.size());
		//end import
		m_Scale = (ClusNormalizedAttributeWeights)getStatManager().getClusteringWeights();
		m_Data = data;
		ClusStatistic allStat = getStatManager().createStatistic(ClusAttrType.ATTR_USE_CLUSTERING);
		data.calcTotalStat(allStat);
		m_Global_Var = allStat.getSVarS(m_Scale);
		System.out.println("Global Variance: "+m_Global_Var);
		ClusModel model = induceSingleUnpruned(cr);
		// FIXME: implement cloneModel();
		// cr.getModelInfo(ClusModels.ORIGINAL).setModel(model);
		// ClusModel pruned = model.cloneModel();
		// label rules
		ClusRuleSet crs = (ClusRuleSet) model;
		// combine rules using heuristic
		labelRules(crs);
		// other label possibilities:
		// labelRulesSimple(crs);
		// labelRulesKMeansInit(crs);
		ClusModelInfo pruned_model = cr.addModelInfo(ClusModel.ORIGINAL);
		pruned_model.setModel(model);
		pruned_model.setName("Original");
	}

	private void createExtraConstraints(
			ArrayList<ILevelConstraint> constraints, ArrayList points) {
		DerivedConstraintsComputer comp = new DerivedConstraintsComputer(points, m_Constraints);
		comp.compute();
	}
	
	/*
	 * each rule represents a cluster
	 */
	private void labelRulesSimple(ClusRuleSet crs) {
		for(int i = 0; i < crs.getModelSize();i++){
			ClusRule rule = crs.getRule(i);
			((ILevelCStatistic)rule.getTargetStat()).setClusterID(i);
		}
	}
	
	/*
	 * cluster rules using k-means
	 */
	private void labelRulesKMeansInit(ClusRuleSet crs) {
		int iterations = 50;
		ArrayList<int[]> labels = new ArrayList<int[]>();
		for(int i = 0; i < iterations; i++){
			int[] l = labelRulesKMeans(crs);
			labels.add(l);
		}
		//search most frequent labeling
		int bestfreq = 0;
		int[] bestLabel = new int[crs.getModelSize()];
		for(int i = 0; i < iterations; i++){
			int[] l = labels.get(i);
			int freq = 1;
			for(int j = i+1; j < iterations; j++){
				int[] c = labels.get(j);
				if(sameLabeling(c,l))
					freq++;
			}
			if(bestfreq < freq){
				bestfreq = freq;
				bestLabel = l;
			}
		}
		for(int i = 0; i < crs.getModelSize();i++){
			ClusRule rule = crs.getRule(i);
			((ILevelCStatistic)rule.getTargetStat()).setClusterID(bestLabel[i]);
		}
	}
	
	private boolean sameLabeling(int[] c, int[] l) {
		for(int i = 0; i < c.length; i++){
			if(c[i] != l[i])
				return false;
		}
		return true;
	}
	
	/*
	 * applies k-means on rules
	 */
	private int[] labelRulesKMeans(ClusRuleSet crs){
		ArrayList<ArrayList<Double>> averages = new ArrayList<ArrayList<Double>>();
		Hashtable<ClusRule, ArrayList<Double>> hash = new Hashtable<ClusRule, ArrayList<Double>>();
		Hashtable<ArrayList<Double>, int[]> weights = new Hashtable<ArrayList<Double>, int[]>();
		Hashtable<ArrayList<Double>, ArrayList<Double>> assign = new Hashtable<ArrayList<Double>, ArrayList<Double>>();
		for(int i = 0; i < crs.getModelSize();i++){
			ClusRule rule = crs.getRule(i);
			int[] w = new int[1];
			w[0] = ((RowData)rule.getVisitor()).toArrayList().size();
			ArrayList<Double> av = computeAverage(rule);
			weights.put(av, w);
			averages.add(av);
			hash.put(rule,av);
		}
		ArrayList<Double> min = getMinima(averages);
		ArrayList<Double> max = getMaxima(averages);
		//creates clustercenters as random points
		ArrayList<ArrayList<Double>> centers = createCenters(min,max,averages,crs);
		//creates clustercenters as rules which covers most points
		//ArrayList<ArrayList<Double>> centers = createCentersFrequencyBased(averages, crs);
		for(int i = 0; i < averages.size();i++){
			assign.put(averages.get(i), centers.get(0));
		}
		boolean changed = true;
		while(changed){
			changed = false;
			for(int i = 0; i < averages.size(); i++){
				ArrayList<Double> closest = getClosestCenter(averages.get(i),centers,min,max);
				if(!(assign.get(averages.get(i))).equals(closest)){
					changed = true;
					assign.remove(averages.get(i));
					assign.put(averages.get(i), closest);
				}
			}
			if(changed)
				centers = computeAverage(averages, centers, assign, weights);
		}
		//assign labels
		int[] labeling = new int[crs.getModelSize()];
		for(int i = 0; i < crs.getModelSize();i++){
			ClusRule rule = crs.getRule(i);
			ArrayList<Double> av = hash.get(rule);
			//System.out.println(av);
			ArrayList<Double> center = assign.get(av);
			labeling[i] = centers.indexOf(center)+1;
		}
		//order labeling
		//System.out.println("next");
//		int expectedLabel = 1;
//		for(int i = 0; i < labeling.length; i++){
////			for(int j = 0; j < labeling.length;j++){
////				System.out.print(labeling[j]);
////			}
////			System.out.println();
//			int label = labeling[i];
//			if(label >= expectedLabel){
//				if(label > expectedLabel){
//					//System.out.println("replace "+label+" by "+expectedLabel);
//					for(int j = i; j < labeling.length; j++){
//						if(labeling[j] == label)
//							labeling[j] = expectedLabel;
//						else if(labeling[j] == expectedLabel)
//							labeling[j] = label;
//					}
//				}
//				expectedLabel++;
//			}
//		}
		return labeling;
	}

	private ArrayList<ArrayList<Double>> computeAverage(
			ArrayList<ArrayList<Double>> averages, ArrayList<ArrayList<Double>> centers, Hashtable<ArrayList<Double>, ArrayList<Double>> assign, Hashtable<ArrayList<Double>, int[]> weights) {
		ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
		for(int i = 0; i < centers.size(); i++){
			ArrayList<Double> average = new ArrayList<Double>();
			//int count = 0;
			int totalWeight = 0;
			ArrayList<Double> center = centers.get(i);
			for (int z = 0; z < center.size(); z++) {
				average.add(0.0);
			}
			for(int j = 0; j < averages.size(); j++){
				ArrayList<Double> c = assign.get(averages.get(j));
				if(center.equals(c)){
					//count++;
					//int weight = weights.get(averages.get(j))[0];
					//totalWeight += weight;
					totalWeight++;
					for (int x = 0; x < center.size(); x++) {
						double y = average.get(x);
						y += averages.get(j).get(x);//*weight;
						average.set(x, y);
					}
				}
			}
			if(totalWeight > 0){
				for (int x = 0; x < center.size(); x++) {
					double y = average.get(x);
					y = y/totalWeight;
					average.set(x, y);
				}
				result.add(average);
			}
			else
				result.add(centers.get(i));
		}
		return result;
	}

	private ArrayList<Double> getClosestCenter(ArrayList<Double> point,
			ArrayList<ArrayList<Double>> centers, ArrayList<Double> min, ArrayList<Double> max) {
		ArrayList<Double> closest = new ArrayList<Double>();
		double distance = Double.POSITIVE_INFINITY;
		for(int i = 0; i < centers.size(); i++){
			double d = calculateDistance(point,centers.get(i),min,max);
			if(d < distance){
				distance = d;
				closest = centers.get(i);
			}
		}
		return closest;
	}

	private double calculateDistance(ArrayList<Double> point,
			ArrayList<Double> center, ArrayList<Double> min, ArrayList<Double> max) {
		double distance = 0.0;
		for(int i = 0; i < center.size(); i++){
			double norm = max.get(i) - min.get(i);
			double s = point.get(i) - center.get(i);
			distance += (Math.pow((s/norm), 2));//Math.pow(norm, 2));
		}
		return Math.sqrt(distance);
	}

	private ArrayList<ArrayList<Double>> createCenters(ArrayList<Double> min,
			ArrayList<Double> max, ArrayList<ArrayList<Double>> averages, ClusRuleSet crs) {
		ArrayList<ArrayList<Double>> centers = new ArrayList<ArrayList<Double>>();
		for(int i = 0; i < m_MaxNbClasses; i++){
			ArrayList<Double> c = new ArrayList<Double>();
			for(int j = 0; j < min.size(); j++){
				double p = ClusRandom.nextDouble(ClusRandom.RANDOM_ALGO_INTERNAL) * (max.get(j) - min.get(j)) + min.get(j);
				c.add(p);
			}
			centers.add(c);
		}
		return centers;
	}
	
	private ArrayList<ArrayList<Double>> createCentersFrequencyBased(ArrayList<ArrayList<Double>> averages, ClusRuleSet crs) {
		ArrayList<ArrayList<Double>> centers = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> clone = (ArrayList<ArrayList<Double>>) averages.clone();
		double m = Double.POSITIVE_INFINITY;
		for(int i = 0; i < m_MaxNbClasses; i++){
			int c = getMostFrequentClone(clone,crs,m);
			ClusRule rule = crs.getRule(c);
			m = ((RowData)rule.getVisitor()).toArrayList().size();
			centers.add(clone.get(c));
		}
		return centers;
	}

	private int getMostFrequentClone(
			ArrayList<ArrayList<Double>> clone, ClusRuleSet crs,double m) {
		int highestFreq = 0;
		int c = -1;
		System.out.println("max"+m);
		for(int i = 0; i < clone.size(); i++){
			ClusRule rule = crs.getRule(i);
			int freq = ((RowData)rule.getVisitor()).toArrayList().size();
			System.out.println(freq);
			if(freq > highestFreq && freq < m){
				c = i;
				highestFreq = freq;
			}
		}
		System.out.println("freq:"+highestFreq);
		System.out.println(c);
		return c;
	}

	private ArrayList<Double> getMinima(ArrayList<ArrayList<Double>> averages) {
		ArrayList<Double> min = new ArrayList<Double>();
		Iterator<ArrayList<Double>> i = averages.iterator();
		ArrayList<Double> l1 = i.next();
		for(int j = 0; j < l1.size(); j++){
			double a = l1.get(j);
			min.add(a);
		}
		while(i.hasNext()){
			ArrayList<Double> l = i.next();
			for(int j = 0; j < l.size(); j++){
				double a = l.get(j);
				if(a < min.get(j))
					min.set(j, a);
			}
		}
		return min;
	}
	
	private ArrayList<Double> getMaxima(ArrayList<ArrayList<Double>> averages) {
		ArrayList<Double> max = new ArrayList<Double>();
		Iterator<ArrayList<Double>> i = averages.iterator();
		ArrayList<Double> l1 = i.next();
		for(int j = 0; j < l1.size(); j++){
			double a = l1.get(j);
			max.add(a);
		}
		while(i.hasNext()){
			ArrayList<Double> l = i.next();
			for(int j = 0; j < l.size(); j++){
				double a = l.get(j);
				if(a > max.get(j))
					max.set(j, a);
			}
		}
		return max;
	}

	private ArrayList<Double> computeAverage(ClusRule rule) {
		RowData data = (RowData)rule.getVisitor();
		ClusAttrType[] attrs = data.getSchema().getDescriptiveAttributes();
		ArrayList<Double> average = new ArrayList<Double>();
		for (int i = 0; i < attrs.length; i++) {
			average.add(0.0);
		}
		ArrayList<DataTuple> tuples = ((RowData)rule.getVisitor()).toArrayList();
		Iterator<DataTuple> i = tuples.iterator();
		while(i.hasNext()){
			DataTuple t = i.next();
			for (int j = 0; j < attrs.length; j++) {
				Double a = average.get(j);
				a += t.getDoubleVal(j);
				average.set(j, a);
			}
		}
		for (int j = 0; j < attrs.length; j++) {
			Double a = average.get(j);
			a = (a/(tuples.size()));
			average.set(j, a);
		}
		return average;
	}
	
	/*
	 * label rules using heuristic
	 */
	private void labelRules(ClusRuleSet crs) {
		ArrayList<ArrayList<ClusRule>> clusters = new ArrayList<ArrayList<ClusRule>>();
		for(int i = 0; i < crs.getModelSize();i++){
			ArrayList<ClusRule> cr = new ArrayList<ClusRule>();
			ClusRule rule = crs.getRule(i);
			cr.add(rule);
			clusters.add(cr);
		}
		//combining rules
		while(clusters.size() > m_MaxNbClasses){
			int best_I = -1;
			int best_J = -1;
			double best_score = Double.NEGATIVE_INFINITY;
			for(int i = 0; i < clusters.size(); i++){
				for(int j = 0; j < i; j++){
					ClusRule rule1 = getRule(clusters.get(i));
					ClusRule rule2 = getRule(clusters.get(j));
					ArrayList<ClusRule> c = new ArrayList<ClusRule>();
					c.add(rule1);
					c.add(rule2);
					ClusRule combo = getRule(c);
					//double score1 = calcNewHeur(rule1);
					//double score2 = calcNewHeur(rule2);
					double scoreC = calcNewHeur(combo);
					//double score = scoreC - score1 - score2;
					if(scoreC > best_score){
						best_I = i;
						best_J = j;
						best_score = scoreC;
					}
				}
			}
			//update clusters
			ArrayList<ClusRule> r = clusters.get(best_I);
			int size_i = r.size();
			int size_j = clusters.get(best_J).size();
			clusters.get(best_J).addAll(r);
			int size = clusters.get(best_J).size();
			clusters.remove(best_I);
		}
		//assign labels
		for(int i = 0; i < clusters.size(); i++){
			ArrayList<ClusRule> c = clusters.get(i);
			for(int j = 0; j < c.size(); j++){
				ClusRule rule = c.get(j);
				((ILevelCStatistic)rule.getTargetStat()).setClusterID(i+1);
			}
		}
	}
	
	/*
	 * creates a new rule which is a combination of the rules in c
	 */
	private ClusRule getRule(ArrayList<ClusRule> c){
		ClusRule n = c.get(0);
		for(int i = 1; i < c.size(); i++){
			ClusRule combo = new ClusRule(getStatManager());
			ClusRule rule = c.get(i);
		    RowData data = new RowData(getSchema());
		    data.addAll((RowData) rule.getVisitor(), (RowData) n.getVisitor());
		    ClusStatistic stat = createTotalClusteringStat(data );
		    combo.setClusteringStat(stat);
		    combo.setVisitor(data);
		    ArrayList<ILevelConstraint> cons = (ArrayList<ILevelConstraint>) n.getConstraints().clone();
		    Iterator<ILevelConstraint> it = rule.getConstraints().iterator();
		    while(it.hasNext()){
		            ILevelConstraint ilc = it.next();
		            if(!cons.contains(ilc))
		            	cons.add(ilc);
		    }
		    combo.setConstraints(cons);
		    n = combo;
		}
		return n;
	}
	
	private double calcNewHeur(ClusRule rule) {
		ClusStatistic cs = rule.m_ClusteringStat;
		double var = cs.getSVarS(m_Scale)/m_Global_Var;
		double con1 = rule.getNumberOfViolatedConstraintsRCCC();
		double con2 = rule.getConstraints().size();
		double con;
		if(con2 != 0)
			 con = con1/con2;
		else
			con = 0;
		double cov1 = ((RowData) rule.getVisitor()).toArrayList().size();
		double cov2 = m_Data.getNbRows();
		double cov = cov1/cov2;
		double gamma = m_Gamma;
		double alfa = m_Alfa;
		double result = (1-((1-gamma)*var+gamma*con))*(Math.pow(cov, alfa));
		//double result = (1-((1-gamma)*var+gamma*con));
		//double result = con;
		//double result = con1;
		return result;
	}

	public ArrayList<ILevelConstraint> createConstraints(RowData data, int nbRows) {
		ArrayList<ILevelConstraint> constr = new ArrayList<ILevelConstraint>();
		ClusAttrType type = getSchema().getAttrType(getSchema().getNbAttributes()-1);
		if (type.getTypeIndex() == NominalAttrType.THIS_TYPE) {
			NominalAttrType cls = (NominalAttrType)type;
			m_MaxNbClasses = cls.getNbValues();
			int nbConstraints = getSettings().getILevelCNbRandomConstraints();
			for (int i = 0; i < nbConstraints; i++) {
				int t1i = 0;
				int t2i = 0;
				while(t1i==t2i){
					t1i = ClusRandom.nextInt(ClusRandom.RANDOM_ALGO_INTERNAL, nbRows);
					t2i = ClusRandom.nextInt(ClusRandom.RANDOM_ALGO_INTERNAL, nbRows);
				}
				DataTuple t1 = data.getTuple(t1i);
				DataTuple t2 = data.getTuple(t2i);
				if (cls.getNominal(t1) == cls.getNominal(t2)) {
					constr.add(new ILevelConstraint(t1, t2, ILevelConstraint.ILevelCMustLink));
				} else {
					constr.add(new ILevelConstraint(t1, t2, ILevelConstraint.ILevelCCannotLink));
				}
			}
		}
		return constr;
	}
	
	public void findNominal(NominalAttrType type, RowData data, ArrayList<ILevelConstraint> constraints) {
		// TODO Auto-generated method stub
		
	}


	public void findNumeric(NumericAttrType at, RowData data, ArrayList<ILevelConstraint> constraints, ClusRule rule, int size) throws ClusException {
		//sort data on attribute
		if (at.isSparse()) {
		      //data.sortSparse(at);
		      // FIXME: input voor sortSparse aangepast!
		      System.exit(1);
		} else {
		      data.sort(at);
		}
		if (at.hasMissing()) {
		      throw new ClusException("Does not support attributes with missing values: "+at.getName());
		}
		//m_BestTest.reset(2);
		//m_BestTest.copyTotal();
		//1: initialise S, ML, CL, h∗ , r∗
		//2: initialise Dtot , Dcov , Ctot and Cviol
		m_BestHeur = Double.NEGATIVE_INFINITY;
		ILevelCStatistic cs = (ILevelCStatistic)rule.getClusteringStat();
		RegressionStat s = new RegressionStat(cs.m_Attrs);
		RegressionStat s_I = new RegressionStat(cs.m_Attrs);
	    //s.setIndices(m_ConstraintsIndex, m_Constraints, ie, clusters);
	    /* initially all data is in negative statistic */
	    for (int i = 0; i < data.getNbRows(); i++) {
	  		DataTuple tuple = data.getTuple(i);
	  		s.updateWeighted(tuple, tuple.getWeight());
	  		s_I.updateWeighted(tuple, tuple.getWeight());
	  	}
		int idx = at.getArrayIndex();
		int Cviol = 0;
		//create hashtable of data points and a list of constraints that apply to it
		Hashtable<DataTuple,ArrayList<ILevelConstraint>> hash = new Hashtable<DataTuple,ArrayList<ILevelConstraint>>(data.getNbRows());
		MemoryTupleIterator dataIterator = data.getIterator();
		for(int a = 0; a < dataIterator.getNbExamples(); a++){
			DataTuple d = dataIterator.readTuple();
			hash.put(d, new ArrayList<ILevelConstraint>());
		}
		java.util.Iterator<ILevelConstraint> it = constraints.iterator();
		ArrayList<ILevelConstraint> ML = new ArrayList<ILevelConstraint>();
		ArrayList<ILevelConstraint> CL = new ArrayList<ILevelConstraint>();
		ArrayList<DataTuple> d = data.toArrayList();
		while(it.hasNext()){
			ILevelConstraint c = it.next();
			if(c.getType()== 0){
				ML.add(c);
				DataTuple t1 = c.getT1();
				DataTuple t2 = c.getT2();
				if(hash.containsKey(t1)){
					ArrayList<ILevelConstraint> cons = hash.get(t1);
					cons.add(c);
					hash.put(t1, cons);
				}
				if(hash.containsKey(t2)){
					ArrayList<ILevelConstraint> cons = hash.get(t2);
					cons.add(c);
					hash.put(t2, cons);
				}
				if(!(d.contains(t1) && d.contains(t2)))
					Cviol++;
				if(t1.equals(null) && t2.equals(null))
					System.out.println("ML should have been removed");
			}
			else {
				CL.add(c);
				DataTuple t1 = c.getT1();
				DataTuple t2 = c.getT2();
				if(hash.containsKey(t1)){
					ArrayList<ILevelConstraint> cons = hash.get(t1);
					cons.add(c);
					hash.put(t1, cons);
				}
				if(hash.containsKey(t2)){
					ArrayList<ILevelConstraint> cons = hash.get(t2);
					cons.add(c);
					hash.put(t2, cons);
				}
				if(d.contains(t1) && d.contains(t2))
					Cviol++;
				else if(!d.contains(t1) && !d.contains(t2))
					System.out.println("CL should have been removed");;
			}
		}		
		ArrayList<ILevelConstraint> ML_I = (ArrayList<ILevelConstraint>) ML.clone();
		ArrayList<ILevelConstraint> CL_I = (ArrayList<ILevelConstraint>) CL.clone();
		Hashtable<DataTuple,ArrayList<ILevelConstraint>> hash_I = (Hashtable<DataTuple,ArrayList<ILevelConstraint>>) hash.clone();
		int Dtot = size;
		int Dcov = data.getNbRows();
		int Ctot = constraints.size();
		int Dcov_I = Dcov;
		int Cviol_I = Cviol;
		int Ctot_I = Ctot;
		findNumericNormal(at, data, s, idx, Cviol, hash, ML, CL, Dtot, Dcov, Ctot);
		findNumericInverse(at, data, s_I, idx, Cviol_I, hash_I, ML_I, CL_I, Dtot, Dcov_I, Ctot_I);
	}

	private void findNumericInverse(NumericAttrType at, RowData data,
			RegressionStat s, int idx, int Cviol,
			Hashtable<DataTuple, ArrayList<ILevelConstraint>> hash,
			ArrayList<ILevelConstraint> ML, ArrayList<ILevelConstraint> CL,
			int Dtot, int Dcov, int Ctot) {
		//3: a prev = ∞
		double prev = Double.NaN;
		//4: for each i ∈ instances(data) sorted by a from large to small do
		for(int i = data.getNbRows()-1; i >= 0;i--){
			//5: 	if i[a] =/= aprev ∧ aprev =/= ∞ then
	  		DataTuple tuple = data.getTuple(i);
	  		double value = tuple.getDoubleVal(idx);
	  		if (value != prev && prev > Double.NEGATIVE_INFINITY && Dcov > 1){
				//6:              x = (i[a] + aprev )/2
				//7:			  t = "a>=x"
				//8:              h = Heuristic(S,Dtot ,Dcov ,Ctot ,Cviol )
				//9:              if h > h∗ then
				//10:			h∗ = h; r∗ = “refine rule using t”
				//11:             aprev = i[a]
	  			double heuristic = computeHeuristic(Dtot,Dcov,Ctot,Cviol,s,ML,CL,hash);
	  			if (heuristic > m_BestHeur) {
	  				m_BestHeur = heuristic;
	  				double splitpoint = (value + prev) / 2.0;
	  				m_BestTest = new ClusRuleConstraintInduceTest(at, splitpoint, false);
	  				m_BestConstraints = (ArrayList<ILevelConstraint>) ML.clone();
	  				m_BestConstraints.addAll((ArrayList<ILevelConstraint>) CL.clone());
	  				Iterator<ILevelConstraint> ilcon = m_BestConstraints.iterator();
	  			}
	  		}
	  		prev = value;
			//12:     Update(S, i, -1)
	  		s.updateWeighted(tuple, -1.0*tuple.getWeight());
			//13:     for each il ∈ ML do
			//14:     	if i ∈ il then
			//15:     		if il is satisfied then
			//16:                     	Cviol + = 1
			//17:                     else
			//18:                         	delete il from ML; Ctot − = 1;
			//19: 							Cviol − = 1
	  		//20:  	  for each il ∈ CL do
			//21:  	        if i ∈ il then
			//22:  	        	if il is satisfied then
			//23:  	                	delete il from CL; Ctot − = 1
			//24:                     else
			//25:                         	Cviol − = 1
			//26:   	Dcov − = 1
	  		java.util.Iterator<ILevelConstraint> m = hash.get(tuple).iterator();
	  		while(m.hasNext()){
	  			ILevelConstraint ilc = m.next();
				DataTuple toCheck;
				if(tuple.getIndex()== ilc.getT1().getIndex())
					toCheck = ilc.getT2();
				else
					toCheck = ilc.getT1();
	  			//constraints is ML
	  			if(ilc.getType()== 0){
	  				if(hash.containsKey(toCheck)){
	  					Cviol += 1;
	  				} else {
	  					ML.remove(ilc);
	  					Cviol -= 1;
	  					Ctot -= 1;
	  				}
	  			} else { //constraint is CL
	  				if(hash.containsKey(toCheck)){
	  					Cviol -= 1;
	  				} else {
	  					CL.remove(ilc);
	  					Ctot -= 1;
	  				}
	  			}
	  		}
	  		hash.remove(tuple);
	  		Dcov--;
		}		
	}

	private void findNumericNormal(NumericAttrType at, RowData data,
			RegressionStat s, int idx, int Cviol,
			Hashtable<DataTuple, ArrayList<ILevelConstraint>> hash,
			ArrayList<ILevelConstraint> ML, ArrayList<ILevelConstraint> CL,
			int Dtot, int Dcov, int Ctot) {
		//3: a prev = ∞
		double prev = Double.NaN;
		//4: for each i ∈ instances(data) sorted by a from large to small do
		for(int i = 0; i < data.getNbRows();i++){
			//5: 	if i[a] =/= aprev ∧ aprev =/= ∞ then
	  		DataTuple tuple = data.getTuple(i);
	  		double value = tuple.getDoubleVal(idx);
	  		if (value != prev && prev > Double.NEGATIVE_INFINITY && Dcov > 1) { //prev != Double.NaN does not work!
				//6:              x = (i[a] + aprev )/2
				//7:			  t = "a>=x"
				//8:              h = Heuristic(S,Dtot ,Dcov ,Ctot ,Cviol )
				//9:              if h > h∗ then
				//10:			h∗ = h; r∗ = “refine rule using t”
				//11:             aprev = i[a]
	  			double heuristic = computeHeuristic(Dtot,Dcov,Ctot,Cviol,s,ML,CL,hash);
	  			if (heuristic > m_BestHeur) {
	  				m_BestHeur = heuristic;
	  				double splitpoint = (value + prev) / 2.0;
	  				m_BestTest = new ClusRuleConstraintInduceTest(at, splitpoint, true);
	  				m_BestConstraints = (ArrayList<ILevelConstraint>) ML.clone();
	  				m_BestConstraints.addAll((ArrayList<ILevelConstraint>) CL.clone());
	  			}
	  		}
	  		prev = value;
			//12:     Update(S, i, -1)
	  		s.updateWeighted(tuple, -1.0*tuple.getWeight());
			//13:     for each il ∈ ML do
			//14:     	if i ∈ il then
			//15:     		if il is satisfied then
			//16:                     	Cviol + = 1
			//17:                     else
			//18:                         	delete il from ML; Ctot − = 1;
			//19: 							Cviol − = 1
	  		//20:  	  for each il ∈ CL do
			//21:  	        if i ∈ il then
			//22:  	        	if il is satisfied then
			//23:  	                	delete il from CL; Ctot − = 1
			//24:                     else
			//25:                         	Cviol − = 1
			//26:   	Dcov − = 1
	  		java.util.Iterator<ILevelConstraint> m = hash.get(tuple).iterator();
	  		while(m.hasNext()){
	  			ILevelConstraint ilc = m.next();
	  			DataTuple toCheck;
				if(tuple.getIndex()== ilc.getT1().getIndex())
					toCheck = ilc.getT2();
				else
					toCheck = ilc.getT1();
	  			//constraints is ML
	  			if(ilc.getType()== 0){
	  				if(hash.containsKey(toCheck)){
	  					Cviol += 1;
	  				} else {
	  					ML.remove(ilc);
	  					Cviol -= 1;
	  					Ctot -= 1;
	  				}
	  			} else { //constraint is CL
	  				if(hash.containsKey(toCheck)){
	  					Cviol -= 1;
	  				} else {
	  					CL.remove(ilc);
	  					Ctot -= 1;
	  				}
	  			}
	  		}
	  		hash.remove(tuple);
	  		Dcov--;
		}
	}

	private double computeHeuristic(double dtot, double dcov, double ctot, double cviol, RegressionStat s, 
				ArrayList<ILevelConstraint> ml, ArrayList<ILevelConstraint> cl, Hashtable<DataTuple, ArrayList<ILevelConstraint>> hash) {	
		double var = s.getSVarS(m_Scale)/m_Global_Var;
		double con;
		if(ctot == 0)
			con = 0;
		else{
			con = cviol/ctot;
		}
		double data = dcov/dtot;
		double g = m_Gamma;
		double a = m_Alfa;
		double h = (1-((1-g)*var + g*con))*(Math.pow(data, a));
		return h;
	}
}
