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

package si.ijs.kt.clus.addon.hmc.ClusAmandaRules;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/*
 * Created on Dec 22, 2005
 */

import java.util.Date;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.rules.ClusRule;
import si.ijs.kt.clus.algo.rules.ClusRuleClassifier;
import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.main.ClusOutput;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.test.InverseNumericTest;
import si.ijs.kt.clus.model.test.NodeTest;
import si.ijs.kt.clus.model.test.NumericTest;
import si.ijs.kt.clus.model.test.SubsetTest;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.WHTDStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.MStreamTokenizer;
import si.ijs.kt.clus.util.jeans.util.StringUtils;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgsProvider;


public class ClusAmandaRules implements CMDLineArgsProvider {

    private static String[] g_Options = { "sgene" };
    private static int[] g_OptionArities = { 1 };

    protected Clus m_Clus;


    public void run(String[] args) throws IOException, ClusException, InterruptedException {
        m_Clus = new Clus();
        Settings sett = m_Clus.getSettings();
        CMDLineArgs cargs = new CMDLineArgs(this);
        cargs.process(args);
        if (cargs.allOK()) {
            sett.getGeneric().setDate(new Date());
            sett.getGeneric().setAppName(cargs.getMainArg(0));
            m_Clus.initSettings(cargs);
            // m_Clus.setExtension(new DefaultExtension());
            ClusInductionAlgorithmType clss = new ClusRuleClassifier(m_Clus);
            m_Clus.initialize(cargs, clss);
            ClusRuleSet set = loadRules(cargs.getMainArg(1));
            ClusRun cr = m_Clus.partitionData();
            pruneInsignificantRules(cr, set);
            if (cargs.hasOption("sgene")) {
                showValuesForGene(cr, set, cargs.getOptionValue("sgene"));
            }
            else {
                evaluateRuleSet(cr, set);
            }
        }
    }


    public ClusRuleSet loadRules(String file) throws IOException, ClusException {
        ClusRuleSet set = new ClusRuleSet(m_Clus.getStatManager());

        ClusStatistic default_stat = m_Clus.getStatManager().createStatistic(AttributeUseType.Target);
        default_stat.calcMean();
        set.setTargetStat(default_stat);

        MStreamTokenizer tokens = new MStreamTokenizer(file);
        while (tokens.hasMoreTokens()) {
            String token = tokens.getToken();
            if (token.equalsIgnoreCase("RULE")) {
                String number = tokens.getToken();
                if (StringUtils.isInteger(number) && tokens.isNextToken(':')) {
                    ClusLogger.info("Reading rule: " + number);
                    ClusRule rule = loadRule(tokens, number);
                    set.add(rule);
                    rule.printModel();
                }
            }
        }
        return set;
    }


    public ClusRule loadRule(MStreamTokenizer tokens, String number) throws IOException, ClusException {
        ClusRule rule = new AmandaRule(m_Clus.getStatManager());
        ClusSchema schema = m_Clus.getSchema();
        while (tokens.hasMoreTokens()) {
            String attrname = tokens.getToken();
            if (attrname.equals("->")) {
                if (!tokens.getToken().equalsIgnoreCase("CLASS")) { throw new ClusException("'Class' expected after '->' while reading rule " + number); }
                addClass(rule, tokens.getToken());
                break;
            }
            ClusAttrType type = schema.getAttrType(attrname);
            if (type == null) { throw new ClusException("Can't find attribute: '" + attrname + "' while reading rule " + number); }
            NodeTest test = null;
            if (type instanceof NumericAttrType) {
                String compare = tokens.getToken();
                String bound_str = tokens.getToken();
                try {
                    double bound = Double.parseDouble(bound_str);
                    if (compare.equals(">")) {
                        test = new NumericTest(type, bound, 0.0);
                    }
                    else {
                        test = new InverseNumericTest(type, bound, 0.0);
                    }
                }
                catch (NumberFormatException e) {
                    throw new ClusException("Error reading numeric bound: '" + bound_str + "' in test on '" + type.getName() + "' while reading rule " + number);
                }
            }
            else {
                if (tokens.isNextToken("=")) {
                    NominalAttrType nominal = (NominalAttrType) type;
                    boolean[] isin = new boolean[nominal.getNbValues()];
                    String value = tokens.getToken();
                    Integer res = nominal.getValueIndex(value);
                    if (res == null) { throw new ClusException("Value '" + value + "' not in domain of '" + type.getName() + "' while reading rule " + number); }
                    isin[res.intValue()] = true;
                    test = new SubsetTest(nominal, 1, isin, 0.0);
                }
                else {
                    throw new ClusException("Expected '=' after nominal attribute '" + type.getName() + "' while reading rule " + number);
                }
            }
            rule.addTest(test);
        }
        return rule;
    }


    void addClass(ClusRule rule, String classstr) throws IOException, ClusException {
        WHTDStatistic stat = (WHTDStatistic) m_Clus.getStatManager().createStatistic(AttributeUseType.Target);
        stat.calcMean();
        ClassHierarchy hier = stat.getHier();
        ClassesTuple tuple = new ClassesTuple(classstr, hier.getType().getTable());
        tuple.addHierarchyIndices(hier);
        stat.setMeanTuple(tuple);
        rule.setTargetStat(stat);
    }


    void pruneInsignificantRules(ClusRun cr, ClusRuleSet rules) throws IOException, ClusException {
        RowData prune = (RowData) cr.getPruneSet();
        if (prune == null)
            return;
        WHTDStatistic stat = (WHTDStatistic) m_Clus.getStatManager().createStatistic(AttributeUseType.Target);
        WHTDStatistic global = (WHTDStatistic) stat.cloneStat();
        prune.calcTotalStat(global);
        global.calcMean();
        Settings sett = m_Clus.getSettings();
        boolean useBonferroni = sett.getHMLC().isUseBonferroni();
        double sigLevel = sett.getHMLC().getHierPruneInSig();
        if (sigLevel == 0.0)
            return;
        for (int i = 0; i < rules.getModelSize(); i++) {
            ClusRule rule = rules.getRule(i);
            RowData data = rule.computeCovered(prune);
            WHTDStatistic orig = (WHTDStatistic) rule.getTargetStat();
            WHTDStatistic valid = (WHTDStatistic) orig.cloneStat();
            for (int j = 0; j < data.getNbRows(); j++) {
                DataTuple tuple = data.getTuple(j);
                valid.updateWeighted(tuple, j);
            }
            valid.calcMean();
            WHTDStatistic pred = (WHTDStatistic) orig.cloneStat();
            pred.copy(orig);
            pred.calcMean();
            pred.setValidationStat(valid);
            pred.setGlobalStat(global);
            if (useBonferroni) {
                pred.setSigLevel(sigLevel / rules.getModelSize());
            }
            else {
                pred.setSigLevel(sigLevel);
            }
            pred.setMeanTuple(orig.getDiscretePred());
            pred.performSignificanceTest();
            rule.setTargetStat(pred);
        }
    }


    void evaluateRuleSet(ClusRun cr, ClusRuleSet rules) throws IOException, ClusException, InterruptedException {
        Settings sett = m_Clus.getSettings();
        ClusOutput output = new ClusOutput(sett.getGeneric().getAppName() + ".rules.out", m_Clus.getSchema(), sett);
        ClusModelInfo info = cr.addModelInfo(ClusModel.DEFAULT);
        info.setStatManager(m_Clus.getStatManager());
        info.setModel(rules);
        info.setName("Rules");
        m_Clus.addModelErrorMeasures(cr);
        m_Clus.calcError(cr, null); // Calc error
        output.writeHeader();
        output.writeOutput(cr, true, sett.getOutput().isOutTrainError());
        output.close();
    }


    public void showValuesForGene(ClusRun cr, ClusRuleSet rules, String gene) throws IOException, ClusException, InterruptedException {
        DataTuple tuple = null;
        if (cr.getTrainingSet() != null) {
            ClusLogger.info("Searching for gene in training set");
            tuple = ((RowData) cr.getTrainingSet()).findTupleByKey(gene);
        }
        if (tuple == null && cr.getPruneSet() != null) {
            ClusLogger.info("Searching for gene in validation set");
            tuple = ((RowData) cr.getPruneSet()).findTupleByKey(gene);
        }
        if (tuple == null && cr.getTestSet() != null) {
            ClusLogger.info("Searching for gene in test set");
            tuple = cr.getTestSet().findTupleByKey(gene);
        }
        if (tuple == null) {
            ClusLogger.info("Can't find gene in data set");
        }
        else {
            Settings sett = m_Clus.getSettings();
            PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(sett.getGeneric().getAppName() + ".sgene")));
            for (int i = 0; i < rules.getModelSize(); i++) {
                AmandaRule rule = (AmandaRule) rules.getRule(i);
                rule.printModel(wrt);
                wrt.println();
                if (rule.covers(tuple))
                    wrt.println("Rule covers gene: " + gene);
                else
                    wrt.println("Rule does not cover: " + gene);
                wrt.println();
                for (int j = 0; j < rule.getModelSize(); j++) {
                    NodeTest test = rule.getTest(j);
                    ClusAttrType type = test.getType();
                    wrt.println("Test " + j + ": " + test.getString() + " -> value for " + gene + " = " + type.getString(tuple) + " Covers: " + rule.doTest(test, tuple));
                }
                wrt.println();
            }
            wrt.close();
        }
    }


    @Override
    public String[] getOptionArgs() {
        return g_Options;
    }


    @Override
    public int[] getOptionArgArities() {
        return g_OptionArities;
    }


    @Override
    public int getNbMainArgs() {
        return 2;
    }


    @Override
    public void showHelp() {
    }


    public static void main(String[] args) {
        try {
            ClusAmandaRules rules = new ClusAmandaRules();
            rules.run(args);
        }
        catch (Exception io) {
            ClusLogger.info(io.toString());
        }
    }
}
