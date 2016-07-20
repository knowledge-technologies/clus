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

package addon.hmc.ClusAmandaRules;

/*
 * Created on Dec 22, 2005
 */

import java.util.Date;

import java.io.*;

import clus.*;
import jeans.util.cmdline.*;
import jeans.util.*;
import clus.algo.*;
import clus.algo.rules.*;
import clus.main.*;
import clus.util.*;
import clus.statistic.*;
import clus.data.type.*;
import clus.model.*;
import clus.model.test.*;
import clus.ext.hierarchical.*;
import clus.data.rows.*;

public class ClusAmandaRules implements CMDLineArgsProvider {

	private static String[] g_Options = {"sgene"};
	private static int[] g_OptionArities = {1};

	protected Clus m_Clus;

	public void run(String[] args) throws IOException, ClusException {
		m_Clus = new Clus();
		Settings sett = m_Clus.getSettings();
		CMDLineArgs cargs = new CMDLineArgs(this);
		cargs.process(args);
		if (cargs.allOK()) {
			sett.setDate(new Date());
			sett.setAppName(cargs.getMainArg(0));
			m_Clus.initSettings(cargs);
			// m_Clus.setExtension(new DefaultExtension());
			ClusInductionAlgorithmType clss = new ClusRuleClassifier(m_Clus);
			m_Clus.initialize(cargs, clss);
			ClusRuleSet set = loadRules(cargs.getMainArg(1));
			ClusRun cr = m_Clus.partitionData();
			pruneInsignificantRules(cr, set);
			if (cargs.hasOption("sgene")) {
				showValuesForGene(cr, set, cargs.getOptionValue("sgene"));
			} else {
				evaluateRuleSet(cr, set);
			}
		}
	}

	public ClusRuleSet loadRules(String file) throws IOException, ClusException {
		ClusRuleSet set = new ClusRuleSet(m_Clus.getStatManager());

		ClusStatistic default_stat = m_Clus.getStatManager().createStatistic(ClusAttrType.ATTR_USE_TARGET);
		default_stat.calcMean();
		set.setTargetStat(default_stat);

		MStreamTokenizer tokens = new MStreamTokenizer(file);
		while (tokens.hasMoreTokens()) {
			String token = tokens.getToken();
			if (token.equalsIgnoreCase("RULE")) {
				String number = tokens.getToken();
			  if (StringUtils.isInteger(number) && tokens.isNextToken(':')) {
			  	System.out.println("Reading rule: "+number);
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
	  		if (!tokens.getToken().equalsIgnoreCase("CLASS")) {
	  		  throw new ClusException("'Class' expected after '->' while reading rule "+number);
	  		}
	  		addClass(rule, tokens.getToken());
	  		break;
	  	}
	  	ClusAttrType type = schema.getAttrType(attrname);
	  	if (type == null) {
	  		throw new ClusException("Can't find attribute: '"+attrname+"' while reading rule "+number);
	  	}
	  	NodeTest test = null;
	  	if (type instanceof NumericAttrType) {
	  		String compare = tokens.getToken();
	  		String bound_str = tokens.getToken();
	  		try {
	  			double bound = Double.parseDouble(bound_str);
	  			if (compare.equals(">")) {
	  				test = new NumericTest(type, bound, 0.0);
	  			} else {
	  				test = new InverseNumericTest(type, bound, 0.0);
	  			}
	  		} catch (NumberFormatException e) {
	  			throw new ClusException("Error reading numeric bound: '"+bound_str+"' in test on '"+type.getName()+"' while reading rule "+number);
	  		}
	  	} else {
	  		if (tokens.isNextToken("=")) {
	  			NominalAttrType nominal = (NominalAttrType)type;
	  			boolean[] isin = new boolean[nominal.getNbValues()];
	  			String value = tokens.getToken();
					Integer res = nominal.getValueIndex(value);
					if (res == null) {
						throw new ClusException("Value '"+value+"' not in domain of '"+type.getName()+"' while reading rule "+number);
					}
					isin[res.intValue()] = true;
	  			test = new SubsetTest(nominal, 1, isin, 0.0);
	  		} else {
	  			throw new ClusException("Expected '=' after nominal attribute '"+type.getName()+"' while reading rule "+number);
	  		}
	  	}
	  	rule.addTest(test);
	  }
	  return rule;
	}

	void addClass(ClusRule rule, String classstr) throws IOException, ClusException {
		WHTDStatistic stat = (WHTDStatistic)m_Clus.getStatManager().createStatistic(ClusAttrType.ATTR_USE_TARGET);
		stat.calcMean();
		ClassHierarchy hier = stat.getHier();
		ClassesTuple tuple = new ClassesTuple(classstr, hier.getType().getTable());
		tuple.addHierarchyIndices(hier);
		stat.setMeanTuple(tuple);
	  rule.setTargetStat(stat);
	}

	void pruneInsignificantRules(ClusRun cr, ClusRuleSet rules) throws IOException, ClusException {
		RowData prune = (RowData)cr.getPruneSet();
		if (prune == null) return;
		WHTDStatistic stat = (WHTDStatistic)m_Clus.getStatManager().createStatistic(ClusAttrType.ATTR_USE_TARGET);
		WHTDStatistic global = (WHTDStatistic)stat.cloneStat();
		prune.calcTotalStat(global);
		global.calcMean();
		Settings sett = m_Clus.getSettings();
		boolean useBonferroni = sett.isUseBonferroni();
		double sigLevel = sett.getHierPruneInSig();
		if (sigLevel == 0.0) return;
		for (int i = 0; i < rules.getModelSize(); i++) {
				ClusRule rule = rules.getRule(i);
				RowData data = rule.computeCovered(prune);
				WHTDStatistic orig = (WHTDStatistic)rule.getTargetStat();
				WHTDStatistic valid = (WHTDStatistic)orig.cloneStat();
				for (int j = 0; j < data.getNbRows(); j++) {
					DataTuple tuple = data.getTuple(j);
					valid.updateWeighted(tuple, j);
				}
				valid.calcMean();
				WHTDStatistic pred = (WHTDStatistic)orig.cloneStat();
				pred.copy(orig);
				pred.calcMean();
				pred.setValidationStat(valid);
				pred.setGlobalStat(global);
				if (useBonferroni) {
					pred.setSigLevel(sigLevel/rules.getModelSize());
				} else {
					pred.setSigLevel(sigLevel);
				}
				pred.setMeanTuple(orig.getDiscretePred());
				pred.performSignificanceTest();
				rule.setTargetStat(pred);
		}
	}

	void evaluateRuleSet(ClusRun cr, ClusRuleSet rules) throws IOException, ClusException {
		Settings sett = m_Clus.getSettings();
		ClusOutput output = new ClusOutput(sett.getAppName() + ".rules.out", m_Clus.getSchema(), sett);
		ClusModelInfo info = cr.addModelInfo(ClusModel.DEFAULT);
		info.setStatManager(m_Clus.getStatManager());
		info.setModel(rules);
		info.setName("Rules");
		m_Clus.addModelErrorMeasures(cr);
		m_Clus.calcError(cr, null); // Calc error
		output.writeHeader();
		output.writeOutput(cr, true, sett.isOutTrainError());
		output.close();
	}

	public void showValuesForGene(ClusRun cr, ClusRuleSet rules, String gene) throws IOException, ClusException {
		DataTuple tuple = null;
		if (cr.getTrainingSet() != null) {
			System.out.println("Searching for gene in training set");
			tuple = ((RowData)cr.getTrainingSet()).findTupleByKey(gene);
		}
		if (tuple == null && cr.getPruneSet() != null) {
			System.out.println("Searching for gene in validation set");
			tuple = ((RowData)cr.getPruneSet()).findTupleByKey(gene);
		}
		if (tuple == null && cr.getTestSet() != null) {
			System.out.println("Searching for gene in test set");
			tuple = ((RowData)cr.getTestSet()).findTupleByKey(gene);
		}
		if (tuple == null) {
			System.out.println("Can't find gene in data set");
		} else {
			Settings sett = m_Clus.getSettings();
			PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream(sett.getAppName() + ".sgene")));
			for (int i = 0; i < rules.getModelSize(); i++) {
				AmandaRule rule = (AmandaRule)rules.getRule(i);
				rule.printModel(wrt);
				wrt.println();
				if (rule.covers(tuple)) wrt.println("Rule covers gene: "+gene);
				else wrt.println("Rule does not cover: "+gene);
				wrt.println();
				for (int j = 0; j < rule.getModelSize(); j++) {
					NodeTest test = rule.getTest(j);
					ClusAttrType type = test.getType();
					wrt.println("Test "+j+": "+test.getString()+" -> value for "+gene+" = "+type.getString(tuple)+" Covers: "+rule.doTest(test, tuple));
				}
				wrt.println();
			}
			wrt.close();
		}
	}

	public String[] getOptionArgs() {
		return g_Options;
	}

	public int[] getOptionArgArities() {
		return g_OptionArities;
	}

	public int getNbMainArgs() {
		return 2;
	}

	public void showHelp() {
	}

	public static void main(String[] args) {
		try {
			ClusAmandaRules rules = new ClusAmandaRules();
			rules.run(args);
		} catch (IOException io) {
			System.out.println("IO Error: "+io.getMessage());
		} catch (ClusException cl) {
			System.out.println("Error: "+cl.getMessage());
		}
	}
}
