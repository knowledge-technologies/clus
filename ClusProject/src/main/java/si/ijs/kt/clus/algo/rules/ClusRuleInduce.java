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

/*
 * Created on May 1, 2005
 */

package si.ijs.kt.clus.algo.rules;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.split.CurrentBestTestAndHeuristic;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.beamsearch.ClusBeam;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamModel;
import si.ijs.kt.clus.heuristic.ClusHeuristic;
import si.ijs.kt.clus.heuristic.rules.ClusRuleHeuristicDispersion;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsRules.CoveringMethod;
import si.ijs.kt.clus.main.settings.section.SettingsRules.OptimizationGDAddLinearTerms;
import si.ijs.kt.clus.main.settings.section.SettingsRules.OptimizationLinearTermNormalizeValues;
import si.ijs.kt.clus.main.settings.section.SettingsRules.RuleAddingMethod;
import si.ijs.kt.clus.main.settings.section.SettingsRules.RulePredictionMethod;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.selection.BagSelection;
import si.ijs.kt.clus.statistic.ClassificationStat;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.tools.optimization.CallExternGD;
import si.ijs.kt.clus.util.tools.optimization.GDAlg;
import si.ijs.kt.clus.util.tools.optimization.OptAlg;
import si.ijs.kt.clus.util.tools.optimization.OptProbl;
import si.ijs.kt.clus.util.tools.optimization.de.DeAlg;


public class ClusRuleInduce extends ClusInductionAlgorithm {

    protected boolean m_BeamChanged;
    protected FindBestTestRules m_FindBestTest;
    protected ClusHeuristic m_Heuristic;


    public ClusRuleInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
        super(schema, sett);
        m_FindBestTest = new FindBestTestRules(getStatManager());
    }


    void resetAll() {
        m_BeamChanged = false;
    }


    public void setHeuristic(ClusHeuristic heur) {
        m_Heuristic = heur;
    }


    public double estimateBeamMeasure(ClusRule rule) throws ClusException {
        return m_Heuristic.calcHeuristic(null, rule.getClusteringStat(), null);
    }


    public boolean isBeamChanged() {
        return m_BeamChanged;
    }


    public void setBeamChanged(boolean change) {
        m_BeamChanged = change;
    }


    ClusBeam initializeBeam(RowData data) throws ClusException {
        Settings sett = getSettings();
        ClusBeam beam = new ClusBeam(sett.getBeamSearch().getBeamWidth(), sett.getBeamSearch().getBeamRemoveEqualHeur());
        ClusStatistic stat = createTotalClusteringStat(data);
        ClusRule rule = new ClusRule(getStatManager());
        rule.setClusteringStat(stat);
        rule.setVisitor(data);
        double value = estimateBeamMeasure(rule);
        beam.addModel(new ClusBeamModel(value, rule));
        return beam;
    }


    public void refineModel(ClusBeamModel model, ClusBeam beam, int model_idx) throws ClusException {
        ClusRule rule = (ClusRule) model.getModel();
        RowData data = (RowData) rule.getVisitor();
        if (m_FindBestTest.initSelectorAndStopCrit(rule.getClusteringStat(), data)) {
            model.setFinished(true);
            return;
        }
        CurrentBestTestAndHeuristic sel = m_FindBestTest.getBestTest();
        ClusAttrType[] attrs = data.getSchema().getDescriptiveAttributes();
        for (int i = 0; i < attrs.length; i++) {
            // if (getSettings().getGeneral().getVerbose() > 1) System.out.print("\n Ref.Attribute: " +
            // attrs[i].getName() + ": ");
            sel.resetBestTest();
            double beam_min_value = beam.getMinValue();
            sel.setBestHeur(beam_min_value);
            ClusAttrType at = attrs[i];
            if (at instanceof NominalAttrType)
                m_FindBestTest.findNominal((NominalAttrType) at, data);
            else
                m_FindBestTest.findNumeric((NumericAttrType) at, data);
            if (sel.hasBestTest()) {
                NodeTest test = sel.updateTest();
                if (getSettings().getGeneral().getVerbose() > 0)
                    ClusLogger.info("  Test: " + test.getString() + " -> " + sel.m_BestHeur);
                // RowData subset = data.applyWeighted(test, ClusNode.YES);
                RowData subset = data.apply(test, ClusNode.YES);
                ClusRule ref_rule = rule.cloneRule();
                ref_rule.addTest(test);
                ref_rule.setVisitor(subset);
                ref_rule.setClusteringStat(createTotalClusteringStat(subset));
                // if (getSettings().getGeneral().getVerbose() > 0) ClusLogger.info(" Sanity.check.val: " +
                // sel.m_BestHeur);
                if (getSettings().getRules().isHeurRuleDist()) {
                    int[] subset_idx = new int[subset.getNbRows()];
                    for (int j = 0; j < subset_idx.length; j++) {
                        subset_idx[j] = subset.getTuple(j).getIndex();
                    }
                    ((ClusRuleHeuristicDispersion) m_Heuristic).setDataIndexes(subset_idx);
                }
                double new_heur;
                // Do a sanity check only for exact (non-approximative) heuristics
                if (getSettings().getTimeSeries().isTimeSeriesProtoComlexityExact()) {
                    new_heur = sanityCheck(sel.m_BestHeur, ref_rule);
                    // if (getSettings().getGeneral().getVerbose() > 0) ClusLogger.info(" Sanity.check.exp: " +
                    // new_heur);
                }
                else {
                    new_heur = sel.m_BestHeur;
                }
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


    public void refineBeam(ClusBeam beam) throws ClusException {
        setBeamChanged(false);
        ArrayList models = beam.toArray();
        for (int i = 0; i < models.size(); i++) {
            ClusBeamModel model = (ClusBeamModel) models.get(i);
            if (!(model.isRefined() || model.isFinished())) {
                // if (getSettings().getGeneral().getVerbose() > 0) ClusLogger.info(" Refine: model " + i);
                refineModel(model, beam, i);
                model.setRefined(true);
                model.setParentModelIndex(-1);
            }
        }
    }


    public ClusRule learnOneRule(RowData data) throws ClusException {
        ClusBeam beam = initializeBeam(data);
        int i = 0;
        System.out.print("Step: ");
        while (true) {
            if (getSettings().getGeneral().getVerbose() > 0) {
                ClusLogger.info("Step: " + i);
            }
            else {
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
        ClusLogger.info();
        double best = beam.getBestModel().getValue();
        double worst = beam.getWorstModel().getValue();
        ClusLogger.info("Worst = " + worst + " Best = " + best);
        ClusRule result = (ClusRule) beam.getBestAndSmallestModel().getModel();
        // Create target statistic for rule
        RowData rule_data = (RowData) result.getVisitor();
        result.setTargetStat(createTotalTargetStat(rule_data));
        result.setVisitor(null);
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
     * 
     * @param data
     * @return array of rules
     * @throws ClusException
     */
    public ClusRule[] learnBeamOfRules(RowData data) throws ClusException {
        ClusBeam beam = initializeBeam(data);
        int i = 0;
        System.out.print("Step: ");
        while (true) {
            if (getSettings().getGeneral().getVerbose() > 0) {
                ClusLogger.info("Step: " + i);
            }
            else {
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
        ClusLogger.info();
        double best = beam.getBestModel().getValue();
        double worst = beam.getWorstModel().getValue();
        ClusLogger.info("Worst = " + worst + " Best = " + best);
        ArrayList beam_models = beam.toArray();
        ClusRule[] result = new ClusRule[beam_models.size()];
        for (int j = 0; j < beam_models.size(); j++) {
            // Put better models first
            int k = beam_models.size() - j - 1;
            ClusRule rule = (ClusRule) ((ClusBeamModel) beam_models.get(k)).getModel();
            // Create target statistic for this rule
            RowData rule_data = (RowData) rule.getVisitor();
            rule.setTargetStat(createTotalTargetStat(rule_data));
            rule.setVisitor(null);
            rule.simplify();
            result[j] = rule;
        }
        return result;
    }


    /**
     * Standard covering algorithm for learning ordered rules. Data is removed
     * when it is covered. Default rule is added at the end.
     * 
     * @throws ClusException
     */
    public void separateAndConquor(ClusRuleSet rset, RowData data) throws ClusException {
        int max_rules = getSettings().getRules().getMaxRulesNb();
        int i = 0;
        // Learn the rules
        while ((data.getNbRows() > 0) && (i < max_rules)) {
            ClusRule rule = learnOneRule(data);
            if (rule.isEmpty()) {
                break;
            }
            else {
                rule.computePrediction();
                rule.printModel();
                ClusLogger.info();
                rset.add(rule);
                data = rule.removeCovered(data);
                i++;
            }
        }
        // The default rule
        // TODO: Investigate different possibilities for the default rule
        ClusStatistic left_over;
        if (data.getNbRows() > 0) {
            left_over = createTotalTargetStat(data);
            left_over.calcMean();
        }
        else {
            ClusLogger.info("All training examples covered - default rule on entire training set!");
            rset.m_Comment = new String(" (on entire training set)");
            left_over = getStatManager().getTrainSetStat(AttributeUseType.Target).cloneStat();
            left_over.copy(getStatManager().getTrainSetStat(AttributeUseType.Target));
            left_over.calcMean();
            // left_over.setSumWeight(0);
            System.err.println(left_over.toString());
        }
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
    }


    /**
     * Weighted covering algorithm for learning unordered rules.
     * Maximum number of rules can be given. Data is re-weighted when it is covered,
     * and removed when it is 'covered enough'. Default rule is added at the end.
     */

    public void separateAndConquorWeighted(ClusRuleSet rset, RowData data) throws ClusException {
        int max_rules = getSettings().getRules().getMaxRulesNb();
        int i = 0;
        RowData data_copy = data.deepCloneData(); // Taking copy of data. Probably not nice
        ArrayList<boolean[]> bit_vect_array = new ArrayList<>();
        // Learn the rules
        while ((data.getNbRows() > 0) && (i < max_rules)) {
            ClusRule rule = learnOneRule(data);
            if (rule.isEmpty()) {
                break;
            }
            else {
                rule.computePrediction();
                if (getSettings().getRules().isPrintAllRules())
                    rule.printModel();
                ClusLogger.info();
                rset.add(rule);
                data = rule.reweighCovered(data);
                i++;
                if (getSettings().getRules().isHeurRuleDist()) {
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
                    ((ClusRuleHeuristicDispersion) m_Heuristic).setCoveredBitVectArray(bit_vect_array);
                }
            }
        }
        // The default rule
        // TODO: Investigate different possibilities for the default rule
        ClusStatistic left_over;
        if (data.getNbRows() > 0) {
            left_over = createTotalTargetStat(data);
            left_over.calcMean();
        }
        else {
            ClusLogger.info("All training examples covered - default rule on entire training set!");
            rset.m_Comment = new String(" (on entire training set)");
            left_over = getStatManager().getTrainSetStat(AttributeUseType.Target).cloneStat();
            left_over.copy(getStatManager().getTrainSetStat(AttributeUseType.Target));
            left_over.calcMean();
            // left_over.setSumWeight(0);
            System.err.println(left_over.toString());
        }
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
    }


    /**
     * Modified separateAndConquorWeighted method: Adds a rule to the rule set
     * only if it improves the rule set performance
     * 
     * @param rset
     * @param data
     */
    public void separateAndConquorAddRulesIfBetter(ClusRuleSet rset, RowData data) throws ClusException {
        int max_rules = getSettings().getRules().getMaxRulesNb();
        int i = 0;
        RowData data_copy = data.deepCloneData();
        ArrayList bit_vect_array = new ArrayList();
        ClusStatistic left_over = createTotalTargetStat(data_copy);
        left_over.calcMean();
        ClusStatistic new_left_over = left_over;
        rset.setTargetStat(left_over);
        double err_score = rset.computeErrorScore(data_copy);
        while ((data.getNbRows() > 0) && (i < max_rules)) {
            ClusRule rule = learnOneRule(data);
            if (rule.isEmpty()) {
                break;
            }
            else {
                rule.computePrediction();
                ClusRuleSet new_rset = rset.cloneRuleSet();
                new_rset.add(rule);
                data = rule.reweighCovered(data);
                left_over = new_left_over;
                new_left_over = createTotalTargetStat(data);
                new_left_over.calcMean();
                new_rset.setTargetStat(new_left_over);
                double new_err_score = new_rset.computeErrorScore(data_copy);
                if ((err_score - new_err_score) > 1e-6) {
                    i++;
                    rule.printModel();
                    ClusLogger.info();
                    err_score = new_err_score;
                    rset.add(rule);
                    if (getSettings().getRules().isHeurRuleDist()) {
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
                        ((ClusRuleHeuristicDispersion) m_Heuristic).setCoveredBitVectArray(bit_vect_array);
                    }
                }
                else {
                    break;
                }
            }
        }
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
    }


    /**
     * Modified separateAndConquorWeighted method: Adds a rule to the rule set
     * only if it improves the rule set performance or if they cover default class.
     * If not, it checks other rules in the beam.
     * 
     * @param rset
     * @param data
     */
    public void separateAndConquorAddRulesIfBetterFromBeam(ClusRuleSet rset, RowData data) throws ClusException {
        int max_rules = getSettings().getRules().getMaxRulesNb();
        int i = 0;
        RowData data_copy = data.deepCloneData();
        ArrayList bit_vect_array = new ArrayList();
        ClusStatistic left_over = createTotalTargetStat(data);
        ClusStatistic new_left_over = left_over;
        left_over.calcMean();
        rset.setTargetStat(left_over);
        int nb_tar = left_over.getNbAttributes();
        boolean cls_task = false;
        if (left_over instanceof ClassificationStat) {
            cls_task = true;
        }
        int[] def_maj_class = new int[nb_tar];
        if (cls_task) {
            for (int t = 0; t < nb_tar; t++) {
                def_maj_class[t] = left_over.getNominalPred()[t];
            }
        }
        double err_score = rset.computeErrorScore(data);
        while ((data_copy.getNbRows() > 0) && (i < max_rules)) {
            ClusRule[] rules = learnBeamOfRules(data_copy);
            left_over = new_left_over;
            int rule_added = -1;
            // Check all rules in the beam
            for (int j = 0; j < rules.length; j++) {
                if (rules[j].isEmpty()) {
                    continue;
                }
                else {
                    rules[j].computePrediction();
                    ClusRuleSet new_rset = rset.cloneRuleSet();
                    new_rset.add(rules[j]);
                    RowData data_copy2 = data_copy.deepCloneData();
                    data_copy2 = rules[j].reweighCovered(data_copy2);
                    ClusStatistic new_left_over2 = createTotalTargetStat(data_copy2);
                    new_left_over2.calcMean();
                    new_rset.setTargetStat(new_left_over2);
                    double new_err_score = new_rset.computeErrorScore(data);
                    // Add the rule anyway if classifies to the default class
                    boolean add_anyway = false;
                    if (cls_task) {
                        for (int t = 0; t < nb_tar; t++) {
                            if (def_maj_class[t] == rules[j].getTargetStat().getNominalPred()[t]) {
                                add_anyway = true;
                            }
                        }
                    }
                    if (((err_score - new_err_score) > 1e-6) || add_anyway) {
                        err_score = new_err_score;
                        rule_added = j;
                        data_copy = data_copy2;
                        new_left_over = new_left_over2;
                    }
                }
            }
            if (rule_added != -1) {
                i++;
                rules[rule_added].printModel();
                ClusLogger.info();
                rset.add(rules[rule_added]);
                if (getSettings().getRules().isHeurRuleDist()) {
                    boolean[] bit_vect = new boolean[data.getNbRows()];
                    for (int j = 0; j < bit_vect.length; j++) {
                        if (!bit_vect[j]) {
                            for (int k = 0; k < rset.getModelSize(); k++) {
                                if (rset.getRule(k).covers(data.getTuple(j))) {
                                    bit_vect[j] = true;
                                    break;
                                }
                            }
                        }
                    }
                    bit_vect_array.add(bit_vect);
                    ((ClusRuleHeuristicDispersion) m_Heuristic).setCoveredBitVectArray(bit_vect_array);
                }
            }
            else {
                break;
            }
        }
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
    }


    /**
     * separateAndConquor method that induces rules on several bootstrapped data subsets
     * 
     * @param rset
     * @param data
     * @throws ClusException
     */
    public void separateAndConquorBootstraped(ClusRuleSet rset, RowData data) throws ClusException {
        int nb_sets = 10; // TODO: parameter?
        int nb_rows = data.getNbRows();
        int max_rules = getSettings().getRules().getMaxRulesNb();
        max_rules /= nb_sets;
        RowData data_not_covered = (RowData) data.cloneData();
        for (int z = 0; z < nb_sets; z++) {
            // Select the data using bootstrap
            RowData data_sel = (RowData) data.cloneData();
            BagSelection msel = new BagSelection(nb_rows, getSettings().getEnsemble().getEnsembleBagSize(), null);
            data_sel.update(msel);
            // Reset tuple indexes used in heuristic
            if (getSettings().getRules().isHeurRuleDist()) {
                int[] data_idx = new int[data_sel.getNbRows()];
                for (int j = 0; j < data_sel.getNbRows(); j++) {
                    data_sel.getTuple(j).setIndex(j);
                    data_idx[j] = j;
                }
                ((ClusRuleHeuristicDispersion) m_Heuristic).setDataIndexes(data_idx);
                ((ClusRuleHeuristicDispersion) m_Heuristic).initCoveredBitVectArray(data_sel.getNbRows());
            }
            // Induce the rules
            int i = 0;
            RowData data_sel_copy = (RowData) data_sel.cloneData(); // No need for deep clone here
            ArrayList bit_vect_array = new ArrayList();
            while ((data_sel.getNbRows() > 0) && (i < max_rules)) {
                ClusRule rule = learnOneRule(data_sel);
                if (rule.isEmpty()) {
                    break;
                }
                else {
                    rule.computePrediction();
                    rule.printModel();
                    ClusLogger.info();
                    rset.addIfUnique(rule);
                    data_sel = rule.removeCovered(data_sel);
                    data_not_covered = rule.removeCovered(data_not_covered);
                    i++;
                    if (getSettings().getRules().isHeurRuleDist()) {
                        boolean[] bit_vect = new boolean[data_sel_copy.getNbRows()];
                        for (int j = 0; j < bit_vect.length; j++) {
                            if (!bit_vect[j]) {
                                for (int k = 0; k < rset.getModelSize(); k++) {
                                    if (rset.getRule(k).covers(data_sel_copy.getTuple(j))) {
                                        bit_vect[j] = true;
                                        break;
                                    }
                                }
                            }
                        }
                        bit_vect_array.add(bit_vect);
                        ((ClusRuleHeuristicDispersion) m_Heuristic).setCoveredBitVectArray(bit_vect_array);
                    }
                }
            }
        }
        ClusStatistic left_over = createTotalTargetStat(data_not_covered);
        left_over.calcMean();
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
    }


    /**
     * Evaluates each rule in the context of a complete rule set.
     * Generates random rules that are added if they improve the performance.
     * Maybe only candidate rules are generated randomly. Then the best one
     * of the candidate rules could be selected.
     * Is put on with CoveringMethod = RandomRuleSet in settings file.
     * 
     * @param rset
     * @param data
     */
    public void separateAndConquorRandomly(ClusRuleSet rset, RowData data) throws ClusException {
        int nb_rules = 100; // TODO: parameter?
        int max_def_rules = 10; // TODO: parameter?
        ClusRule[] rules = new ClusRule[nb_rules];
        Random rn = new Random(42);
        for (int k = 0; k < nb_rules; k++) {
            ClusRule rule = generateOneRandomRule(data, rn);
            rule.computePrediction();
            rules[k] = rule;
        }
        int max_rules = getSettings().getRules().getMaxRulesNb();
        int i = 0;
        RowData data_copy = data.deepCloneData();
        ClusStatistic left_over = createTotalTargetStat(data);
        ClusStatistic new_left_over = left_over;
        left_over.calcMean();
        rset.setTargetStat(left_over);
        int nb_tar = left_over.getNbAttributes();
        boolean cls_task = false;
        if (left_over instanceof ClassificationStat) {
            cls_task = true;
        }
        int[] def_maj_class = new int[nb_tar];
        if (cls_task) {
            for (int t = 0; t < nb_tar; t++) {
                def_maj_class[t] = left_over.getNominalPred()[t];
            }
        }
        double err_score = rset.computeErrorScore(data);
        int nb_def_rules = 0;
        boolean add_anyway = false;
        while (i < max_rules) {
            left_over = new_left_over;
            int rule_added = -1;
            // Check all random rules
            for (int j = 0; j < rules.length; j++) {
                if ((rules[j] == null) || (rules[j].isEmpty())) {
                    continue;
                }
                else {
                    rules[j].computePrediction();
                    ClusRuleSet new_rset = rset.cloneRuleSet();
                    new_rset.add(rules[j]);
                    RowData data_copy2 = data_copy.deepCloneData();
                    data_copy2 = rules[j].reweighCovered(data_copy2);
                    ClusStatistic new_left_over2 = createTotalTargetStat(data_copy2);
                    new_left_over2.calcMean();
                    new_rset.setTargetStat(new_left_over2);
                    double new_err_score = new_rset.computeErrorScore(data);
                    // Add the rule anyway if classifies to the default class
                    add_anyway = false;
                    if (cls_task) {
                        for (int t = 0; t < nb_tar; t++) {
                            if (def_maj_class[t] == rules[j].getTargetStat().getNominalPred()[t]) {
                                add_anyway = true;
                            }
                        }
                    }
                    double err_d = err_score - new_err_score;
                    if ((err_d > 1e-6) || (nb_def_rules < max_def_rules)) {
                        if (add_anyway) {
                            nb_def_rules++;
                        }
                        err_score = new_err_score;
                        rule_added = j;
                        // System.err.println(err_score + " " + add_anyway + " " + j + " " + err_d);
                        data_copy = data_copy2;
                        new_left_over = new_left_over2;
                    }
                }
            }
            if (rule_added != -1) {
                i++;
                rules[rule_added].printModel();
                ClusLogger.info();
                rset.addIfUnique(rules[rule_added]);
                rules[rule_added] = null;
            }
            else {
                break;
            }
        }
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
    }


    /**
     * Modified separateAndConquorWeighted method: evaluates each rule in
     * the context of a complete rule set, it builds the default rule first.
     * If first learned rule is no good, it checks next rules in the beam.
     * 
     * @param rset
     * @param data
     */
    public void separateAndConquorAddRulesIfBetterFromBeam2(ClusRuleSet rset, RowData data) throws ClusException {
        int max_rules = getSettings().getRules().getMaxRulesNb();
        int i = 0;
        RowData data_copy = data.deepCloneData();
        ClusStatistic left_over = createTotalTargetStat(data);
        // ClusStatistic new_left_over = left_over;
        left_over.calcMean();
        rset.setTargetStat(left_over);
        ClusRule empty_rule = learnEmptyRule(data_copy);
        empty_rule.setTargetStat(left_over);
        data_copy = empty_rule.reweighCovered(data_copy);
        double err_score = rset.computeErrorScore(data);
        while ((data.getNbRows() > 0) && (i < max_rules)) {
            ClusRule[] rules = learnBeamOfRules(data_copy);
            // left_over = new_left_over;
            int rule_added = -1;
            // Check all rules in the beam
            for (int j = 0; j < rules.length; j++) {
                if (rules[j].isEmpty()) {
                    continue;
                }
                else {
                    rules[j].computePrediction();
                    ClusRuleSet new_rset = rset.cloneRuleSet();
                    new_rset.add(rules[j]);
                    RowData data_copy2 = data_copy.deepCloneData();
                    data_copy2 = rules[j].reweighCovered(data_copy2);
                    // ClusStatistic new_left_over2 = m_Induce.createTotalTargetStat(data_copy2);
                    // new_left_over2.calcMean();
                    // new_rset.setTargetStat(new_left_over2);
                    new_rset.setTargetStat(left_over);
                    double new_err_score = new_rset.computeErrorScore(data);
                    if ((err_score - new_err_score) > 1e-6) {
                        err_score = new_err_score;
                        rule_added = j;
                        data_copy = data_copy2;
                        // new_left_over = new_left_over2;
                    }
                }
            }
            if (rule_added != -1) {
                i++;
                rules[rule_added].printModel();
                ClusLogger.info();
                rset.add(rules[rule_added]);
            }
            else {
                break;
            }
        }
        ClusLogger.info("Left Over: " + left_over);
        // rset.setTargetStat(left_over);
    }


    public void separateAndConquorWithHeuristic(ClusRuleSet rset, RowData data) throws ClusException {
        int max_rules = getSettings().getRules().getMaxRulesNb();
        ArrayList bit_vect_array = new ArrayList();
        int i = 0;
        /*
         * getSettings().setCompHeurRuleDistPar(0.0);
         * while (i < max_rules) {
         * ClusRule rule = learnOneRule(data);
         * if (rule.isEmpty()) {
         * break;
         * } else if (!rset.unique(rule)) {
         * i++;
         * double val = getSettings().getCompHeurRuleDistPar();
         * val += 1;
         * getSettings().setCompHeurRuleDistPar(val);
         * continue;
         * } else {
         * getSettings().setCompHeurRuleDistPar(1.0);
         */
        while (i < max_rules) {
            /*
             * ClusRule rule = learnOneRule(data);
             * if (rule.isEmpty() || !rset.unique(rule)) {
             * break;
             */
            ClusRule[] rules = learnBeamOfRules(data);
            ClusRule rule = rules[0];
            for (int l = 0; l < rules.length - 1; l++) {
                rule = rules[l + 1];
                if (rset.unique(rule)) {
                    break;
                }
            }
            if (rule.isEmpty() || !rset.unique(rule)) {
                break;
            }
            else {
                rule.computePrediction();
                rule.printModel();
                ClusLogger.info();
                rset.add(rule);
                i++;
                boolean[] bit_vect = new boolean[data.getNbRows()];
                for (int j = 0; j < bit_vect.length; j++) {
                    if (!bit_vect[j]) {
                        for (int k = 0; k < rset.getModelSize(); k++) {
                            if (rset.getRule(k).covers(data.getTuple(j))) {
                                bit_vect[j] = true;
                                break;
                            }
                        }
                    }
                }
                bit_vect_array.add(bit_vect);
                ((ClusRuleHeuristicDispersion) m_Heuristic).setCoveredBitVectArray(bit_vect_array);
            }
        }
        updateDefaultRule(rset, data);
    }


    public double sanityCheck(double value, ClusRule rule) throws ClusException {
        double expected = estimateBeamMeasure(rule);
        if (Math.abs(value - expected) > 1e-6) {
            ClusLogger.info("Bug in heurisitc: " + value + " <> " + expected);
            PrintWriter wrt = new PrintWriter(System.out);
            rule.printModel(wrt);
            wrt.close();
            System.out.flush();
            throw new ClusException("sanity check failed");
        }
        return expected;
    }


    /**
     * Calls the appropriate rule learning method (e.g. standard covering, weighted covering, ...)
     * depending on parameters .s file. Default is weighted covering algorithm with unordered rules.
     * 
     * @param run
     *        The information about this run. Parameters etc.
     * 
     * @throws ClusException
     * @throws IOException
     * @throws InterruptedException
     */
    public ClusModel induce(ClusRun run) throws ClusException, IOException, InterruptedException {
        CoveringMethod method = getSettings().getRules().getCoveringMethod();
        RuleAddingMethod add_method = getSettings().getRules().getRuleAddingMethod();
        RowData data = (RowData) run.getTrainingSet();
        ClusStatistic stat = createTotalClusteringStat(data);
        m_FindBestTest.initSelectorAndSplit(stat);
        setHeuristic(m_FindBestTest.getBestTest().getHeuristic());
        if (getSettings().getRules().isHeurRuleDist()) {
            int[] data_idx = new int[data.getNbRows()];
            for (int i = 0; i < data.getNbRows(); i++) {
                data.getTuple(i).setIndex(i);
                data_idx[i] = i;
            }
            ((ClusRuleHeuristicDispersion) m_Heuristic).setDataIndexes(data_idx);
            ((ClusRuleHeuristicDispersion) m_Heuristic).initCoveredBitVectArray(data.getNbRows());
        }

        /// Actual rule learning is called here with the given method.
        // Settings.* check if the given option is given on the command line
        ClusRuleSet rset = new ClusRuleSet(getStatManager());
        if (method.equals(CoveringMethod.Standard)) {
            separateAndConquor(rset, data);
        }
        else if (method.equals(CoveringMethod.BeamRuleDefSet)) { // Obsolete!
            separateAndConquorAddRulesIfBetterFromBeam2(rset, data);

            /**
             * Can be put on by CoveringMethod = RandomRuleSet
             */
        }
        else if (method.equals(CoveringMethod.RandomRuleSet)) {
            separateAndConquorRandomly(rset, data);
        }
        else if (method.equals(CoveringMethod.StandardBootstrap)) {
            separateAndConquorBootstraped(rset, data);
        }
        else if (method.equals(CoveringMethod.HeurOnly)) {
            separateAndConquorWithHeuristic(rset, data);
        }
        else if (add_method.equals(RuleAddingMethod.IfBetter)) {
            separateAndConquorAddRulesIfBetter(rset, data);
        }
        else if (add_method.equals(RuleAddingMethod.IfBetterBeam)) {
            separateAndConquorAddRulesIfBetterFromBeam(rset, data);
        }
        else {
            separateAndConquorWeighted(rset, data);
        }

        // The rule set was altered. Compute the means (predictions?) for rules again.
        rset.postProc();

        // Optimizing rule set
        if (getSettings().getRules().isRulePredictionOptimized()) {
            rset = optimizeRuleSet(rset, data);
        }
        rset.setTrainErrorScore(); // Not always needed?
        rset.addDataToRules(data);
        // Computing dispersion
        if (getSettings().getRules().computeDispersion()) {
            rset.computeDispersion(ClusModel.TRAIN);
            rset.removeDataFromRules();
            if (run.getTestIter() != null) {
                RowData testdata = run.getTestSet();
                rset.addDataToRules(testdata);
                rset.computeDispersion(ClusModel.TEST);
                rset.removeDataFromRules();
            }
        }
        // Number rules (for output purpose in WritePredictions)
        rset.numberRules();
        return rset;
    }


    /**
     * Optimize the weights of rules.
     * 
     * @param rset
     *        Rule set to be optimized
     * @param data
     * @return Optimized rule set
     * @throws ClusException
     * @throws IOException
     */
    public ClusRuleSet optimizeRuleSet(ClusRuleSet rset, RowData data) throws ClusException, IOException {
        PrintWriter wrt_pred = null;

        OptAlg optAlg = null;
        OptProbl.OptParam param = rset.giveFormForWeightOptimization(wrt_pred, data);
        ArrayList<Double> weights = null;

        ClusLogger.info("Preparing for optimization.");

        // Find the rule weights with optimization algorithm.
        switch (getSettings().getRules().getRulePredictionMethod()) {
            case GDOptimized:
                optAlg = new GDAlg(getStatManager(), param, rset);
                break;

            case Optimized:
                optAlg = new DeAlg(getStatManager(), param, rset);
                break;

            case GDOptimizedBinary:
                weights = CallExternGD.main(getStatManager(), param, rset);
                break;

            default: // do nothing
                break;
        }

        if (!getSettings().getRules().getRulePredictionMethod().equals(RulePredictionMethod.GDOptimizedBinary)) {
            ClusLogger.info("Preparations ended. Starting optimization.");
        }

        if (!getSettings().getRules().getRulePredictionMethod().equals(RulePredictionMethod.GDOptimizedBinary)) {
            // If using external binary, optimization is already done.

            if (getSettings().getRules().getRulePredictionMethod().equals(RulePredictionMethod.GDOptimized) && getSettings().getRules().getOptGDNbOfTParameterTry() > 1) {

                // Running optimization multiple times and selecting the best one.
                GDAlg gdalg = (GDAlg) optAlg;
                double firstTVal = 1.0;
                double lastTVal = getSettings().getRules().getOptGDGradTreshold();
                // What is the difference for intervals so that we get enough runs
                double interTVal = (lastTVal - firstTVal) / (getSettings().getRules().getOptGDNbOfTParameterTry() - 1);

                double minFitness = Double.POSITIVE_INFINITY;
                for (int iRun = 0; iRun < getSettings().getRules().getOptGDNbOfTParameterTry(); iRun++) {

                    // To make sure the last value is accurate (not rounded imprecisely)
                    if (iRun == getSettings().getRules().getOptGDNbOfTParameterTry() - 1) {
                        getSettings().getRules().setOptGDGradTreshold(lastTVal);
                    }
                    else {
                        getSettings().getRules().setOptGDGradTreshold(firstTVal + iRun * interTVal);
                    }

                    gdalg.initGDForNewRunWithSamePredictions();

                    ArrayList<Double> newWeights = gdalg.optimize();

                    String s = "The T value " + (firstTVal + iRun * interTVal) + " has a test fitness: " + gdalg.getBestFitness();
                    if (gdalg.getBestFitness() < minFitness) {
                        // Fitness is smaller than previously, store these weights
                        weights = newWeights;
                        minFitness = gdalg.getBestFitness();

                        rset.m_optWeightBestTValue = firstTVal + iRun * interTVal;
                        rset.m_optWeightBestFitness = minFitness;

                        ClusLogger.finer(s + " - best so far!");

                        // If fitness increasing, check if we are stopping early
                    }
                    else if (getSettings().getRules().getOptGDEarlyTTryStop() && gdalg.getBestFitness() > getSettings().getRules().getOptGDEarlyStopTreshold() * minFitness) {
                        ClusLogger.finer(s + " - early T value stop reached.");
                        break;
                    }
                }

                getSettings().getRules().setOptGDGradTreshold(lastTVal);
            }
            else {
                // Running only once
                weights = optAlg.optimize();
            }
        }

        for (int j = 0; j < rset.getModelSize(); j++) {
            rset.getRule(j).setOptWeight(((Double) weights.get(j)).doubleValue()); // Set the RULE weights
        }

        // Postprocessing if needed. -- Undo inside normalization on rule set if needed
        if (!getSettings().getRules().getRulePredictionMethod().equals(RulePredictionMethod.GDOptimizedBinary))
            optAlg.postProcess(rset);

        // Print weights of all terms
        if (getSettings().getGeneral().getVerbose() > 0) {
            System.out.print("\nThe weights for rules:");
            for (int j = 0; j < weights.size(); j++) {
                System.out.print(((Double) weights.get(j)).doubleValue() + "; ");
            }
            System.out.print("\n");
        }
        int indexOfLastHandledWeight = rset.removeLowWeightRules() + 1;

        // if needed, add implicit linear terms explicitly. this has to be done after removing low weight rules
        if (getSettings().getRules().getOptAddLinearTerms().equals(OptimizationGDAddLinearTerms.YesSaveMemory)) {
            rset.addImplicitLinearTermsExplicitly(weights, indexOfLastHandledWeight);
        }

        // If linear terms are used, include the normalizing to their weights and the all covering rule
        if (getSettings().getRules().isOptAddLinearTerms() && getSettings().getRules().getOptNormalizeLinearTerms().equals(OptimizationLinearTermNormalizeValues.YesAndConvert)) {
            rset.convertToPlainLinearTerms();
        }

        RowData data_copy = (RowData) data.cloneData();
        updateDefaultRule(rset, data_copy); // Just to show that the default rule is void.
        return rset;
    }


    /**
     * Updates the default rule. This is the rule that is used if no other rule covers the example.
     * For rule ensembles this is, in effect, void rule.
     * 
     * @throws ClusException
     */
    public void updateDefaultRule(ClusRuleSet rset, RowData data) throws ClusException {
        for (int i = 0; i < rset.getModelSize(); i++) {
            data = rset.getRule(i).removeCovered(data);
        }
        ClusStatistic left_over = createTotalTargetStat(data);
        left_over.calcMean();
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
    }


    /**
     * Method that induces a specified number of random rules.
     * That is, the random attributes are chosen with random split points.
     * However, the predictions are the means of covered examples.
     * 
     * @param cr
     *        ClusRun
     * @return RuleSet
     * @throws InterruptedException
     */
    public ClusModel induceRandomly(ClusRun run) throws ClusException, IOException, InterruptedException {
        int number = getSettings().getRules().nbRandomRules();
        RowData data = (RowData) run.getTrainingSet();
        ClusStatistic stat = createTotalClusteringStat(data);
        m_FindBestTest.initSelectorAndSplit(stat);
        setHeuristic(m_FindBestTest.getBestTest().getHeuristic()); // ???
        ClusRuleSet rset = new ClusRuleSet(getStatManager());
        Random rn = new Random(42);
        for (int i = 0; i < number; i++) {
            ClusRule rule = generateOneRandomRule(data, rn);
            rule.computePrediction();
            rule.printModel();
            ClusLogger.info();
            if (!rset.addIfUnique(rule)) {
                i--;
            }
        }

        // The rule set changed, compute predictions (?)
        // This same may have been done on the rule.computePrediction() but not sure
        // For this rule set, postProc is not implemented (abstract function)
        // rset.postProc();

        // Optimizing rule set
        if (getSettings().getRules().isRulePredictionOptimized()) {
            rset = optimizeRuleSet(rset, data);
        }

        ClusStatistic left_over = createTotalTargetStat(data);
        left_over.calcMean();
        ClusLogger.info("Left Over: " + left_over);
        rset.setTargetStat(left_over);
        rset.postProc();
        rset.setTrainErrorScore(); // Not always needed?
        // Computing dispersion
        if (getSettings().getRules().computeDispersion()) {
            rset.addDataToRules(data);
            rset.computeDispersion(ClusModel.TRAIN);
            rset.removeDataFromRules();
            if (run.getTestIter() != null) {
                RowData testdata = run.getTestSet();
                rset.addDataToRules(testdata);
                rset.computeDispersion(ClusModel.TEST);
                rset.removeDataFromRules();
            }
        }
        return rset;
    }


    /**
     * Generates one random rule.
     * 
     * @param data
     * @param rn
     * 
     * @throws ClusException
     */
    private ClusRule generateOneRandomRule(RowData data, Random rn) throws ClusException {
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
        }
        else {
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
                m_FindBestTest.findNominalRandom((NominalAttrType) at, data, rn);
            }
            else {
                m_FindBestTest.findNumericRandom((NumericAttrType) at, data, orig_data, rn);
            }
            if (sel.hasBestTest()) {
                NodeTest test = sel.updateTest();
                if (getSettings().getGeneral().getVerbose() > 0)
                    ClusLogger.info("  Test: " + test.getString() + " -> " + sel.m_BestHeur);
                result.addTest(test);
                // data = data.applyWeighted(test, ClusNode.YES);
                data = data.apply(test, ClusNode.YES); // ???
            }
        }
        // Create target and clustering statistic for rule
        result.setTargetStat(createTotalTargetStat(data));
        result.setClusteringStat(createTotalClusteringStat(data));
        return result;
    }


    @Override
    public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException, InterruptedException, Exception {
        // ClusRulesForAttrs rfa = new ClusRulesForAttrs();
        // return rfa.constructRules(cr);
        resetAll();
        if (!getSettings().getRules().isRandomRules()) { // Is the random rules given in the .s file
            return induce(cr);
        }
        else {
            return induceRandomly(cr);
        }
    }


    @Override
    public void induceAll(ClusRun cr) throws Exception {
        // Set the defaults for heuristic
        RowData trainData = (RowData) cr.getTrainingSet();
        getStatManager().getHeuristic().setTrainData(trainData);
        ClusStatistic trainStat = getStatManager().getTrainSetStat(AttributeUseType.Clustering);
        double value = trainStat.getDispersion(getStatManager().getClusteringWeights(), trainData);
        getStatManager().getHeuristic().setTrainDataHeurValue(value);
        // Induce the model
        ClusModel model = induceSingleUnpruned(cr);
        // FIXME: implement cloneModel();
        // cr.getModelInfo(ClusModels.ORIGINAL).setModel(model);
        // ClusModel pruned = model.cloneModel();
        ClusModelInfo pruned_model = cr.addModelInfo(ClusModel.ORIGINAL);
        pruned_model.setModel(model);
        pruned_model.setName("Original");
    }
}
