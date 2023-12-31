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

package si.ijs.kt.clus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import si.ijs.kt.clus.addon.network.PredictionAnalyzer;
import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.kNN.KnnClassifier;
import si.ijs.kt.clus.algo.rules.ClusCalcRuleErrorProc;
import si.ijs.kt.clus.algo.rules.ClusRuleClassifier;
import si.ijs.kt.clus.algo.rules.ClusRuleSet;
import si.ijs.kt.clus.algo.rules.tune.FIREROSTune;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.algo.tdidt.ClusSITDecisionTree;
import si.ijs.kt.clus.algo.tdidt.ConstraintDFInduce;
import si.ijs.kt.clus.algo.tdidt.processor.NodeExampleCollector;
import si.ijs.kt.clus.algo.tdidt.processor.NodeIDWriter;
import si.ijs.kt.clus.algo.tdidt.tune.CDTTuneFTest;
import si.ijs.kt.clus.algo.tdidt.tune.CDTuneSizeConstrPruning;
import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.ClusSchemaInitializer;
import si.ijs.kt.clus.data.io.ARFFFile;
import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.io.ClusView;
import si.ijs.kt.clus.data.rows.DataPreprocs;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.DiskTupleIterator;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.rows.TupleIterator;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.IntegerAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.data.type.primitive.TimeSeriesAttrType;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.CorrelationMatrixComputer;
import si.ijs.kt.clus.error.PearsonCorrelation;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusErrorOutput;
import si.ijs.kt.clus.error.common.multiscore.MultiScore;
import si.ijs.kt.clus.ext.beamsearch.ClusBeamSearch;
import si.ijs.kt.clus.ext.beamsearch.ClusFastBeamSearch;
import si.ijs.kt.clus.ext.constraint.ClusConstraintFile;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleClassifier;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.ext.exhaustivesearch.ClusExhaustiveDFSearch;
import si.ijs.kt.clus.ext.featureRanking.relief.Relief;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.ext.hierarchical.HierMatrixOutput;
import si.ijs.kt.clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import si.ijs.kt.clus.ext.optiontree.ClusOptionTree;
import si.ijs.kt.clus.ext.semisupervised.ClusSemiSupervisedClassifier;
import si.ijs.kt.clus.main.ClusOutput;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStat;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.ClusSummary;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsData.NormalizeDataValues;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSAlgorithmType;
import si.ijs.kt.clus.main.settings.section.SettingsExperimental;
import si.ijs.kt.clus.main.settings.section.SettingsGeneral.ResourceInfoLoad;
import si.ijs.kt.clus.main.settings.section.SettingsGeneric;
import si.ijs.kt.clus.main.settings.section.SettingsHMTR.HierarchyTypesHMTR;
import si.ijs.kt.clus.main.settings.section.SettingsSSL.SSLMethod;
import si.ijs.kt.clus.main.settings.section.SettingsTree.Heuristic;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.model.io.ClusModelCollectionIO;
import si.ijs.kt.clus.model.io.ClusTreeReader;
import si.ijs.kt.clus.model.processor.ClusEnsemblePredictionWriter;
import si.ijs.kt.clus.model.processor.ModelProcessorCollection;
import si.ijs.kt.clus.model.processor.PredictionWriter;
import si.ijs.kt.clus.pruning.PruneTree;
import si.ijs.kt.clus.selection.BagSelection;
import si.ijs.kt.clus.selection.ClusSelection;
import si.ijs.kt.clus.selection.CriterionBasedSelection;
import si.ijs.kt.clus.selection.OverSample;
import si.ijs.kt.clus.selection.RandomSelection;
import si.ijs.kt.clus.selection.XValDataSelection;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.CombStat;
import si.ijs.kt.clus.statistic.RegressionStat;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusMisc;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.DebugFile;
import si.ijs.kt.clus.util.ResourceInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;
import si.ijs.kt.clus.util.jeans.io.MyFile;
import si.ijs.kt.clus.util.jeans.io.ObjectLoadStream;
import si.ijs.kt.clus.util.jeans.io.ObjectSaveStream;
import si.ijs.kt.clus.util.jeans.util.FileUtil;
import si.ijs.kt.clus.util.jeans.util.IntervalCollection;
import si.ijs.kt.clus.util.jeans.util.StringUtils;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgsProvider;

public class Clus implements CMDLineArgsProvider {

	// debug flags - should be refactored
	public static final boolean HIER_DEBUG = false;

	public enum DebugType {
		None, General, Hierarchy
	};

	static EnumSet<DebugType> Debugging = EnumSet.of(DebugType.None);

	public final static boolean m_UseHier = true;

	public final static String[] OPTION_ARGS = { "relief", "exhaustive", "xval", "oxval", "target", "disable", "silent",
			"lwise", "c45", "info", "sample", "debug", "tuneftest", "load", "soxval", "bag", "obag", "show", "knn",
			"knnTEST", "knnTree", "beam", "gui", "fillin", "rules", "weka", "corrmatrix", "tunesize", "out2model",
			"test", "normalize", "tseries", "writetargets", "fold", "forest", "copying", "sit", "tc", "ssl",
			"clowdflows", "option", "unittest" };

	public final static int[] OPTION_ARITIES = { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };

	private Settings m_Sett = new Settings();
	private ClusSummary m_Summary = new ClusSummary();
	private ClusSchema m_Schema;
	private MultiScore m_Score;
	private ClusInductionAlgorithmType m_Classifier;
	private ClusInductionAlgorithm m_Induce;
	private RowData m_Data;
	private boolean isxval = false;
	private CMDLineArgs m_CmdLine;
	private ClusHMTRHierarchy m_HMTRHierarchy;

	public final void initialize(CMDLineArgs cargs, ClusInductionAlgorithmType clss) throws IOException, ClusException {
		m_CmdLine = cargs;
		m_Classifier = clss;
		// Load resource info (this measures among others CPU time on Linux)
		boolean test = m_Sett.getGeneral().getResourceInfoLoadedValue().equals(ResourceInfoLoad.Test);
		ResourceInfo.loadLibrary(test);

		// Load settings file
		initializeHMTRHierarchy(); // creates the hierarchy for hierarchical MTR if the section HMTR is present
		ARFFFile arff = null;
		if (m_Sett.getGeneral().getVerbose() > 0) {
			ClusLogger.info("Loading '" + m_Sett.getGeneric().getAppName() + "'");
		}

		ClusRandom.initialize(m_Sett);

		ClusReader reader = new ClusReader(m_Sett.getData().getDataFile(), m_Sett);
		if (cargs.hasOption("c45")) {
			if (m_Sett.getGeneral().getVerbose() > 0) {
				ClusLogger.info("Reading C45 .names/.data");
			}
		} else {
			if (m_Sett.getGeneral().getVerbose() > 0) {
				ClusLogger.info("Reading ARFF Header");
			}
			arff = new ARFFFile(reader);
			m_Schema = arff.read(m_Sett);
		}
		// Count rows and move to data segment
		if (m_Sett.getGeneral().getVerbose() > 0) {
			ClusLogger.info("Reading CSV Data");
		}

		// Update schema based on settings
		m_Sett.updateTarget(m_Schema);
		m_Sett.getHMTR().addHMTRTargets(m_Schema, m_HMTRHierarchy); // only if HMTR is enabled
		m_Schema.initializeSettings(m_Sett);

		m_Sett.getAttribute().setTarget(m_Schema.getTarget().toString());
		m_Sett.getAttribute().setDisabled(m_Schema.getDisabled().toString());
		m_Sett.getAttribute().setClustering(m_Schema.getClustering().toString());
		m_Sett.getAttribute().setDescriptive(m_Schema.getDescriptive().toString());

		// Load data from file
		if (ResourceInfo.isLibLoaded()) {
			ClusStat.m_InitialMemory = ResourceInfo.getMemory();
		}
		ClusView view = m_Schema.createNormalView();
		if (getSettings().getHMTR().isHMTREnabled()) {
			m_Data = view.readDataHMTR(reader, m_Schema, m_HMTRHierarchy, m_Sett);
		} else {
			m_Data = view.readData(reader, m_Schema);
		}

		reader.close();
		if (m_Sett.getGeneral().getVerbose() > 0) {
			ClusLogger.info("Found " + m_Data.getNbRows() + " rows");
		}

		if (!getSettings().getData().getNormalizeData().equals(NormalizeDataValues.None)) {
			if (m_Sett.getGeneral().getVerbose() > 0) {
				ClusLogger.info("Normalizing numerical data");
			}
			m_Data = returnNormalizedData(m_Data);
		}

		m_Schema.printInfo();
		if (ResourceInfo.isLibLoaded()) {
			ClusStat.m_LoadedMemory = ResourceInfo.getMemory();
		}
		if (getSettings().getData().isRemoveMissingTarget()) {
			m_Data = CriterionBasedSelection.removeMissingTarget(m_Data);
			CriterionBasedSelection.clearMissingFlagTargetAttrs(m_Schema);
		}

		// Set XVal field in Settings
		if (isxval) {
			SettingsExperimental.IS_XVAL = true;
		}

		// Create induce
		m_Induce = clss.createInduce(m_Schema, m_Sett, cargs);

		// Preprocess and initialize induce
		m_Sett.update(m_Schema);

		/**
		 * If not rule induction, reset some settings just to be sure in case rules from
		 * trees are used. I.e. this is used if the command line parameter is for
		 * decision trees but the transformation for rules is used. It is also possible
		 * to use command line parameter -rules and use trees as a covering method.
		 */
		if (!m_Induce.getStatManager().isRuleInduceOnly()) {
			getSettings().getRules().disableRuleInduceParams();
		}

		/**
		 * Necessary in order to link the labels to the class hierarchy in HMC.
		 * This needs to be before m_Induce.initialize().
		 */
		preprocess();

		// TODO: does this really need to be here? martinb
		m_Schema.setHMTRHierarchy(m_HMTRHierarchy);

		// validate settings
		m_Sett.validateSettings(m_Schema);

		m_Induce.initialize();
		initializeAttributeWeights(m_Data);
		m_Induce.initializeHeuristic();
		loadConstraintFile();
		initializeSummary(clss);

		// Sample data
		if (cargs.hasOption("sample")) {
			String svalue = cargs.getOptionValue("sample");
			sample(svalue);
		}

		if (m_Sett.getGeneral().getVerbose() > 0) {
			ClusLogger.info("Has missing values: " + m_Schema.hasMissing());
		}

		if (ResourceInfo.isLibLoaded()) {
			ClusLogger.info(
					"Memory usage: loading data took " + (ClusStat.m_LoadedMemory - ClusStat.m_InitialMemory) + " kB");
		}
	}

	private void initializeHMTRHierarchy() {

		if (m_Sett.getHMTR().isSectionHMTREnabled()) {

			m_HMTRHierarchy = new ClusHMTRHierarchy();

			if (m_Sett.getGeneral().getVerbose() > 0) {
				ClusLogger.info("Creating hierarchy for HMTR" + System.lineSeparator());
			}

			m_HMTRHierarchy.initialize(m_Sett);// m_Sett.getHMTRHierarchyString().getStringValue(),
												// m_Sett.getHMTRHierarchyWeight().getValue());

			if (m_Sett.getGeneral().getVerbose() > 0) {
				m_HMTRHierarchy.printHierarchy();

				if (m_Sett.getHMTR().getHMTRType().equals(HierarchyTypesHMTR.Tree)) {
					ClusLogger.info(m_HMTRHierarchy.printHierarchyTree());
				}

				m_HMTRHierarchy.printDepth();
				m_HMTRHierarchy.printWeights();
			}

		}
	}

	/**
	 * Condensed version of
	 * {@code clus.Clus.initialize(CMDLineArgs, ClusInductionAlgorithmType)} that
	 * more or less prepares the dataset. Used for unit testing.
	 * 
	 * @param cargs
	 * @throws ClusException
	 * @throws IOException
	 */
	public final void initialize(CMDLineArgs cargs) throws ClusException, IOException {
		ARFFFile arff = null;
		ClusReader reader = new ClusReader(m_Sett.getData().getDataFile(), m_Sett);
		arff = new ARFFFile(reader);
		m_Schema = arff.read(m_Sett);

		m_Sett.updateTarget(m_Schema);
		m_Schema.initializeSettings(m_Sett);
		m_Sett.getAttribute().setTarget(m_Schema.getTarget().toString());
		m_Sett.getAttribute().setDisabled(m_Schema.getDisabled().toString());
		m_Sett.getAttribute().setClustering(m_Schema.getClustering().toString());
		m_Sett.getAttribute().setDescriptive(m_Schema.getDescriptive().toString());

		// Load data from file
		if (ResourceInfo.isLibLoaded()) {
			ClusStat.m_InitialMemory = ResourceInfo.getMemory();
		}
		ClusView view = m_Schema.createNormalView();
		m_Data = view.readData(reader, m_Schema, cargs.hasOption("unittest"));
		reader.close();

		if (!getSettings().getData().getNormalizeData().equals(NormalizeDataValues.None)) {
			if (m_Sett.getGeneral().getVerbose() > 0)
				ClusLogger.info("Normalizing numerical data");
			m_Data = returnNormalizedData(m_Data);
		}

		if (ResourceInfo.isLibLoaded()) {
			ClusStat.m_LoadedMemory = ResourceInfo.getMemory();
		}
		if (getSettings().getData().isRemoveMissingTarget()) {
			m_Data = CriterionBasedSelection.removeMissingTarget(m_Data);
			CriterionBasedSelection.clearMissingFlagTargetAttrs(m_Schema);
		}
	}

	public void initialize(RowData data, ClusSchema schema, Settings sett, ClusInductionAlgorithmType clss)
			throws IOException, ClusException {
		m_Data = data;
		m_Sett = sett;
		m_Classifier = clss;
		m_Schema = schema;
		m_CmdLine = new CMDLineArgs(this);
	}

	/***
	 * Method for recreating ClusInduce and most other instance variables Useful for
	 * running Clus on different data sets with a similar schema
	 */
	public void recreateInduce(CMDLineArgs cargs, ClusInductionAlgorithmType clss, ClusSchema schema, RowData data)
			throws ClusException, IOException {
		m_Summary = new ClusSummary();
		m_Schema = schema;
		m_Induce = clss.createInduce(schema, m_Sett, cargs);
		m_Data = data;
		m_Classifier = clss;
		data.setSchema(schema);
		m_Induce.initialize();
		initializeAttributeWeights(data);
		m_Induce.initializeHeuristic();
		initializeSummary(clss);
	}

	/***
	 * Easy to use initialization method to be used from inside add-on applications
	 * supporting Clus (e.g., applications converting data)
	 **/
	public void initializeAddOn(String appname) throws ClusException, IOException {
		SettingsGeneric sett = getSettings().getGeneric();
		sett.setDate(new Date());
		sett.setAppName(appname);
		initSettings(null);
		ClusDecisionTree clss = new ClusDecisionTree(this);
		initialize(new CMDLineArgs(this), clss);
	}

	public final void loadConstraintFile() throws IOException {
		if (m_Sett.getConstraints().hasConstraintFile()) {
			ClusConstraintFile constr = ClusConstraintFile.getInstance();
			constr.load(m_Sett.getConstraints().getConstraintFile(), m_Schema);
		}
	}

	public final void initSettings(CMDLineArgs cargs) throws IOException {
		m_Sett.initialize(cargs, true);
	}

	public final void initializeSummary(ClusInductionAlgorithmType clss) throws ClusException {
		ClusStatManager mgr = m_Induce.getStatManager();
		ClusErrorList error = mgr.createErrorMeasure(m_Score);
		m_Summary.resetAll();
		m_Summary.setStatManager(mgr);
		if (m_Sett.getOutput().isOutTrainError()) {
			m_Summary.setTrainError(error);
		}

		if ((hasTestSet() || m_Sett.getSSL().isSemiSupervisedMode()) && m_Sett.getOutput().isOutTestError()) {
			m_Summary.setTestError(error);
		}
		if (hasPruneSet() && m_Sett.getOutput().isOutValidError()) {
			m_Summary.setValidationError(error);
		}
	}

	public final void sample(String svalue) {
		ClusSelection sel;
		int nb_rows = m_Data.getNbRows();
		int ps_perc = svalue.indexOf('%');
		if (ps_perc != -1) {
			// FIXME parse string ok?
			double val = Double.parseDouble(svalue.substring(0, ps_perc + 1)) / 100.0;
			if (val < 1.0) {
				sel = new RandomSelection(nb_rows, val);
			} else {
				sel = new OverSample(nb_rows, val);
			}
		} else {
			sel = new RandomSelection(nb_rows, Integer.parseInt(svalue));
		}
		m_Data = (RowData) m_Data.selectFrom(sel, null); // no problem, parallelism comes later
		int nb_sel = m_Data.getNbRows();
		ClusLogger.info("Sample (" + svalue + ") " + nb_rows + " -> " + nb_sel);
		ClusLogger.info();
	}

	/**
	 * Train the model for given algorithm type.
	 *
	 * @param cr   Information about this run.
	 * @param clss Type of learning algorithm used.
	 * @throws Exception
	 */
	public final void induce(ClusRun cr, ClusInductionAlgorithmType clss) throws Exception {
		ClusLogger.info(
				"Time: " + (new SimpleDateFormat("dd. MM. yyyy HH:mm:ss")).format(Calendar.getInstance().getTime()));
		ClusLogger.info("Run: " + cr.getIndexString());
		clss.printInfo();

		clss.induceAll(cr);
	}

	// public final void showTree(String fname) throws ClusException, IOException,
	// ClassNotFoundException {
	// TreeFrame.showTree(getSettings().getGeneric().getFileAbsolute(fname));
	// }
	//
	//
	// public final void gui(String lok) throws ClusException, IOException,
	// ClassNotFoundException {
	// ClusSchema schema = new ClusSchema("Clus");
	// ClusStatManager mgr = new ClusStatManager(schema, m_Sett, false);
	// TreeFrame.start(mgr, lok);
	// }

	// public final void postprocModel(ClusModel model, TupleIterator iter,
	// ModelProcessorCollection coll) throws
	// IOException, ClusException {
	// iter.init();
	// ClusSchema mschema = iter.getSchema();
	// if (iter.shouldAttach()) {
	// ClusLogger.info("Effect of should_attach not implemented in postprocModel");
	// }
	// coll.initialize(model, mschema);
	// DataTuple tuple = iter.readTuple();
	// while (tuple != null) {
	// model.applyModelProcessors(tuple, coll);
	// coll.modelDone();
	// tuple = iter.readTuple();
	// }
	// iter.close();
	// coll.terminate(model);
	// }

	public final int getNbRows() {
		return m_Data.getNbRows();
	}

	public final RowData getData() {
		return m_Data;
	}

	public final RowData getRowDataClone() {
		return (RowData) m_Data.cloneData();
	}

	public final MultiScore getMultiScore() {
		return m_Score;
	}

	public final ClusInductionAlgorithm getInduce() {
		return m_Induce;
	}

	public final ClusInductionAlgorithmType getClassifier() {
		return m_Classifier;
	}

	public final ClusStatManager getStatManager() {
		return m_Induce.getStatManager();
	}

	public final MultiScore getScore() {
		return m_Score;
	}

	public final ClusSchema getSchema() {
		return m_Schema;
	}

	public final Settings getSettings() {
		return m_Sett;
	}

	public final ClusSummary getSummary() {
		return m_Summary;
	}

	public final DataPreprocs getPreprocs(boolean single) {
		DataPreprocs pps = new DataPreprocs();
		m_Schema.getPreprocs(pps, single);
		m_Induce.getPreprocs(pps);
		return pps;
	}

	public final void initializeAttributeWeights(ClusData data) throws IOException, ClusException {
		ClusStatManager mgr = getInduce().getStatManager();
		ClusStatistic allStat = mgr.createStatistic(AttributeUseType.All);
		ClusStatistic[] stats = new ClusStatistic[1];
		stats[0] = allStat;
		/*
		 * if (!m_Sett.isNullTestFile()) { ClusLogger.info("Loading: " +
		 * m_Sett.getTestFile()); updateStatistic(m_Sett.getTestFile(), stats); } if
		 * (!m_Sett.isNullPruneFile()) { ClusLogger.info("Loading: " +
		 * m_Sett.getPruneFile()); updateStatistic(m_Sett.getPruneFile(), stats); }
		 */
		mgr.initNormalizationWeights(allStat, data);
		mgr.initClusteringWeights();
		mgr.initDispersionWeights();
		mgr.initHeuristic();
		mgr.initStopCriterion();
		mgr.initSignifcanceTestingTable();
	}

	public final void preprocess(ClusData data) throws ClusException {
		DataPreprocs pps = getPreprocs(false);
		int nb = pps.getNbPasses();
		for (int i = 0; i < nb; i++) {
			data.preprocess(i, pps);
			pps.done(i);
		}
		if (Clus.HIER_DEBUG) {
			HierMatrixOutput.writeExamples((RowData) data, m_Induce.getStatManager().getHier());
		}
	}

	public final void preprocSingle(RowData data) throws ClusException {
		DataPreprocs pps = getPreprocs(true);
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			pps.preprocSingle(tuple);
		}
	}

	public final void preprocess() throws ClusException {
		preprocess(m_Data);
		/*
		 * ClusTarget target = m_Data.getTarget(); target.initTransformation(); if
		 * (m_Sett.shouldNormalize()) { ClusLogger.info("Normalizing data");
		 * target.normalize(); }
		 */
	}

	public final boolean hasTestSet() {
		if (!m_Sett.getData().isNullTestFile())
			return true;
		if (m_Sett.getData().getTestProportion() != 0.0)
			return true;
		if (isxval)
			return true;
		return false;
	}

	public final boolean hasPruneSet() {
		if (!m_Sett.getData().isNullPruneFile())
			return true;
		if (m_Sett.getData().getPruneProportion() != 0.0)
			return true;
		return false;
	}

	public final RowData loadDataFile(String fname) throws IOException, ClusException {
		ClusReader reader = new ClusReader(fname, m_Sett);
		if (getSettings().getGeneral().getVerbose() > 0)
			ClusLogger.info("Reading: " + fname);
		ARFFFile arff = new ARFFFile(reader);
		// FIXME - test if schema equal
		arff.read(m_Sett); // Read schema, but ignore :-)
		// FIXME - hack with number of rows
		ClusView view = m_Schema.createNormalView();
		RowData data = view.readData(reader, m_Schema);
		reader.close();
		if (getSettings().getGeneral().getVerbose() > 0)
			ClusLogger.info("Found " + data.getNbRows() + " rows");
		preprocSingle(data);
		return data;
	}

	public final ClusRun partitionData() throws IOException, ClusException, InterruptedException {
		boolean testfile = false;
		boolean writetest = false;
		ClusSelection sel = null;
		if (!m_Sett.getData().isNullTestFile()) {
			testfile = true;
			writetest = true;
		} else {
			double test = m_Sett.getData().getTestProportion();
			if (test != 0.0) {
				int nbtot = m_Data.getNbRows();
				sel = new RandomSelection(nbtot, test);
				writetest = true;
			}
		}
		return partitionData(m_Data, sel, testfile, writetest, m_Summary, 1);
	}

	public final ClusRun partitionData(ClusSelection sel, int idx)
			throws IOException, ClusException, InterruptedException {
		return partitionData(m_Data, sel, false, false, m_Summary, idx);
	}

	public final ClusRun partitionData(ClusData data, ClusSelection sel, boolean testfile, boolean writetest,
			ClusSummary summary, int idx) throws IOException, ClusException, InterruptedException {
		// cloning the data is done in partitionDataBasic()
		String test_fname = m_Sett.getGeneric().getAppName();
		ClusRun cr = partitionDataBasic(data, sel, summary, idx);
		boolean hasMissing = m_Schema.hasMissing();
		if (testfile) {
			test_fname = m_Sett.getData().getTestFile();
			MyClusInitializer init = new MyClusInitializer();
			TupleIterator iter = new DiskTupleIterator(test_fname, init, getPreprocs(true), m_Sett);
			iter.setShouldAttach(true);
			cr.setTestSet(iter);
		}
		if (!m_Sett.getSSL().isNullUnlabeledFile()) {
			MyClusInitializer initUnlabeled = new MyClusInitializer();
			TupleIterator iterUnlabeled = new DiskTupleIterator(m_Sett.getSSL().getUnlabeledFile(), initUnlabeled,
					getPreprocs(true), m_Sett);
			iterUnlabeled.setShouldAttach(true);
			cr.setUnlabeledSet(iterUnlabeled);
		}
		if (writetest) {
			if (m_Sett.getOutput().isWriteModelIDPredictions()) {
				ClusModelInfo mi = cr.addModelInfo(ClusModel.ORIGINAL);
				String ts_name = m_Sett.getGeneric().getAppNameWithSuffix() + ".test.id";
				mi.addModelProcessor(ClusModelInfo.TEST_ERR, new NodeIDWriter(ts_name, hasMissing, m_Sett));
			}
			if (m_Sett.getOutput().isWriteTestSetPredictions()) {
				ClusModelInfo allmi = cr.getAllModelsMI();
				String ts_name = m_Sett.getGeneric().getAppNameWithSuffix() + ".test.pred.arff";
				allmi.addModelProcessor(ClusModelInfo.TEST_ERR, new PredictionWriter(ts_name, m_Sett,
						getStatManager().createStatistic(AttributeUseType.Target)));
			}
		}
		if (m_Sett.getOutput().isWriteTrainSetPredictions()) {
			ClusModelInfo allmi = cr.getAllModelsMI();
			String tr_name = m_Sett.getGeneric().getAppNameWithSuffix() + ".train." + idx + ".pred.arff";
			allmi.addModelProcessor(ClusModelInfo.TRAIN_ERR,
					new PredictionWriter(tr_name, m_Sett, getStatManager().createStatistic(AttributeUseType.Target)));
		}
		if (m_Sett.getOutput().isWriteModelIDPredictions()) {
			ClusModelInfo mi = cr.addModelInfo(ClusModel.ORIGINAL);
			String id_tr_name = m_Sett.getGeneric().getAppNameWithSuffix() + ".train." + idx + ".id";
			mi.addModelProcessor(ClusModelInfo.TRAIN_ERR, new NodeExampleCollector(id_tr_name, hasMissing, m_Sett));
		}
		return cr;
	}

	public final ClusRun partitionDataBasic(RowData train) throws IOException, ClusException, InterruptedException {
		ClusSummary summary = new ClusSummary();
		return partitionDataBasic(train, null, null, summary, 1);
	}

	public synchronized final ClusRun partitionDataBasic(ClusData data, ClusSelection sel, ClusSummary summary, int idx)
			throws IOException, ClusException, InterruptedException {
		return partitionDataBasic(data, sel, null, summary, idx);
	}

	public final ClusRun partitionDataBasic(ClusData data, ClusSelection sel, ClusData prunefile, ClusSummary summary,
			int idx) throws IOException, ClusException, InterruptedException {
		ClusRun cr = new ClusRun(data.cloneData(), summary);
		if (sel != null) {
			if (sel.changesDistribution()) {
				((RowData) cr.getTrainingSet()).update(sel);
			} else {
				ClusData val = cr.getTrainingSet().select(sel);
				cr.setTestSet(((RowData) val).getIterator());
			}
		}
		int pruning_max = m_Sett.getData().getPruneSetMax();
		double vsb = m_Sett.getData().getPruneProportion();
		if (vsb != 0.0) {
			ClusData train = cr.getTrainingSet();
			int nbtot = train.getNbRows();
			int nbsel = (int) Math.round(vsb * nbtot);
			if (nbsel > pruning_max)
				nbsel = pruning_max;
			RandomSelection prunesel = new RandomSelection(nbtot, nbsel);
			cr.setPruneSet(train.select(prunesel), prunesel);
			if (getSettings().getGeneral().getVerbose() > 0)
				ClusLogger.info("Selecting pruning set: " + nbsel);
		}
		if (!m_Sett.getData().isNullPruneFile()) {
			String prset = m_Sett.getData().getPruneFile();
			if (prunefile != null) {
				cr.setPruneSet(prunefile, null);
			} else {
				ClusData prune = loadDataFile(prset);
				cr.setPruneSet(prune, null);
				if (getSettings().getGeneral().getVerbose() > 0)
					ClusLogger.info("Selecting pruning set: " + prset);
			}
		}
		cr.setIndex(idx);
		cr.copyTrainingData();
		return cr;
	}

	public final void attachModels(ClusSchema schema, ClusRun cr) throws ClusException {
		for (int i = 0; i < cr.getNbModels(); i++) {
			ClusModel model = cr.getModel(i);
			if (model != null)
				schema.attachModel(model);
		}
	}

	public final static double calcModelError(ClusStatManager mgr, RowData data, ClusModel model)
			throws ClusException, IOException, InterruptedException {
		ClusSchema schema = data.getSchema();
		/* create error measure */
		ClusErrorList error = new ClusErrorList();
		NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.Target);
		NominalAttrType[] nom = schema.getNominalAttrUse(AttributeUseType.Target);
		TimeSeriesAttrType[] ts = schema.getTimeSeriesAttrUse(AttributeUseType.Target);
		if (nom.length != 0) {
			error.addError(new Accuracy(error, nom));
		} else if (num.length != 0) {
			error.addError(new PearsonCorrelation(error, num));
		} else if (ts.length != 0) {
			throw new RuntimeException(
					"There are some time series targets, no numeric targets, but we try to perform the step\nerror.addError(new PearsonCorrelation(error, num)).");
		}

		/* attach model to given schema */
		schema.attachModel(model);
		/* iterate over tuples and compute error */
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			ClusStatistic pred = model.predictWeighted(tuple);
			error.addExample(tuple, pred);
		}
		/* return the error */
		double err = error.getFirstError().getModelError();
		// ClusLogger.info("Error: "+err);
		return err;
	}

	public final void calcError(TupleIterator iter, int type, ClusRun cr)
			throws IOException, ClusException, InterruptedException {
		calcError(iter, type, cr, null);
	}

	/**
	 * Compute either test or train error
	 * 
	 * @throws InterruptedException
	 */
	public final void calcError(TupleIterator iter, int type, ClusRun cr, ClusEnsemblePredictionWriter ens_pred)
			throws IOException, ClusException, InterruptedException {
		iter.init();
		ClusSchema mschema = iter.getSchema();
		if (iter.shouldAttach())
			attachModels(mschema, cr);
		cr.initModelProcessors(type, mschema);

		boolean wr_ens_tr_preds = (!SettingsExperimental.IS_XVAL)
				|| (SettingsExperimental.IS_XVAL && cr.getTestSet() == null);
		wr_ens_tr_preds = wr_ens_tr_preds && (type == ClusModelInfo.TRAIN_ERR)
				&& (getSettings().getEnsemble().shouldWritePredictionsFromEnsemble());

		boolean wr_ens_te_preds = (!SettingsExperimental.IS_XVAL && cr.getTestSet() != null);
		wr_ens_te_preds = wr_ens_te_preds && (type == ClusModelInfo.TEST_ERR)
				&& (getSettings().getEnsemble().shouldWritePredictionsFromEnsemble());
		// boolean wr_ens_xval_preds =
		// (getSettings().shouldWritePredictionsFromEnsemble() && getSettings().IS_XVAL
		// &&
		// type == ClusModelInfo.TEST_ERR);
		// wr_ens_xval_preds = wr_ens_xval_preds && cr.getIndex() ;
		if (wr_ens_tr_preds || wr_ens_te_preds)
			cr.initEnsemblePredictionsWriter(type);
		// if (wr_ens_xval_preds && (cr.getIndex() == 1))
		// cr.initEnsemblePredictionsWriter(ClusModelInfo.XVAL_PREDS);//initialize only
		// for the first fold

		ModelProcessorCollection allcoll = cr.getAllModelsMI().getAddModelProcessors(type);
		DataTuple tuple = iter.readTuple();

		int cnt = 0;

		while (tuple != null) {
			cnt++;

			allcoll.exampleUpdate(tuple);
			for (int i = 0; i < cr.getNbModels(); i++) {
				ClusModelInfo mi = cr.getModelInfo(i);
				if (mi != null && mi.getModel() != null) {
					ClusModel model = mi.getModel();
					ClusStatistic pred = model.predictWeighted(tuple);
					ClusErrorList err = mi.getError(type);
					if (err != null)
						err.addExample(tuple, pred);
					ModelProcessorCollection coll = mi.getModelProcessors(type);
					if (coll != null) {
						if (coll.needsModelUpdate()) {
							model.applyModelProcessors(tuple, coll);
							coll.modelDone();
						}
						coll.exampleUpdate(tuple, pred);
					}
					if ((wr_ens_tr_preds || wr_ens_te_preds) && i == ClusModel.ORIGINAL)
						(mi.getEnsemblePredictionWriter(type)).writePredictionsForTuple(tuple, pred);
					if ((ens_pred != null) && (i == ClusModel.ORIGINAL) && (type == ClusModelInfo.TEST_ERR))
						ens_pred.writePredictionsForTuple(tuple, pred);
				}
			}
			allcoll.exampleDone();
			tuple = iter.readTuple();

			if (cnt % 1000 == 0) {
				allcoll.flushWriter();
				cnt = 0;
			}
		}
		iter.close();
		cr.termModelProcessors(type);
		if (wr_ens_tr_preds || wr_ens_te_preds) {
			cr.termEnsemblePredictionsWriter(type);
		}
	}

	public void addModelErrorMeasures(ClusRun cr) throws ClusException {
		for (int i = 0; i < cr.getNbModels(); i++) {
			ClusModelInfo info = cr.getModelInfo(i);
			// Compute rule-wise error measures
			if (info != null && info.getModel() instanceof ClusRuleSet && m_Sett.getRules().isRuleWiseErrors()) {
				ClusRuleSet ruleset = (ClusRuleSet) info.getModel();
				ruleset.setError(info.getTrainingError(), ClusModel.TRAIN);
				ruleset.setError(info.getTestError(), ClusModel.TEST);
				info.addModelProcessor(ClusModelInfo.TRAIN_ERR,
						new ClusCalcRuleErrorProc(ClusModel.TRAIN, info.getTrainingError()));
				info.addModelProcessor(ClusModelInfo.TEST_ERR,
						new ClusCalcRuleErrorProc(ClusModel.TEST, info.getTestError()));
			}
		}
	}

	public final void calcExtraTrainingSetErrors(ClusRun cr) throws ClusException, InterruptedException {
		ClusErrorList parent = getStatManager().createExtraError(ClusModelInfo.TRAIN_ERR);
		for (int i = 0; i < cr.getNbModels(); i++) {
			ClusModelInfo info = cr.getModelInfo(i);
			if (info != null) {
				ClusErrorList parent_cl = parent.getErrorClone();
				parent_cl.compute((RowData) cr.getTrainingSet(), info);
				info.setExtraError(ClusModelInfo.TRAIN_ERR, parent_cl);
				if (info.getModel() instanceof ClusRuleSet && m_Sett.getRules().isRuleWiseErrors()) {
					ClusRuleSet ruleset = (ClusRuleSet) info.getModel();
					for (int j = 0; j < ruleset.getModelSize(); j++) {
						ClusErrorList rule_error = parent.getErrorClone();
						rule_error.compute((RowData) cr.getTrainingSet(), ruleset.getRule(j));
						ruleset.getRule(j).addError(rule_error, ClusModelInfo.TRAIN_ERR);
					}
				}
			}
		}
	}

	public final void calcError(ClusRun cr, ClusSummary summary)
			throws IOException, ClusException, InterruptedException {
		calcError(cr, summary, null);
	}

	public final void calcError(ClusRun cr, ClusSummary summary, ClusEnsemblePredictionWriter ens_pred)
			throws IOException, ClusException, InterruptedException {
		cr.copyAllModelsMIs();
		for (int i = 0; i < cr.getNbModels(); i++) {
			if (cr.getModelInfo(i) != null && !m_Sett.getOutput().shouldShowModel(i)) {
				// If don't show model, then don't compute error
				ClusModelInfo inf = cr.getModelInfo(i);
				if (inf.getTrainingError() != null) {
					inf.getTrainingError().clear();
				}
				if (inf.getTestError() != null) {
					inf.getTestError().clear();
				}
				if (inf.getValidationError() != null) {
					inf.getValidationError().clear();
				}
			}
		}
		if (m_Sett.getOutput().isOutTrainError()) {
			if (getSettings().getGeneral().getVerbose() > 0)
				ClusLogger.info("Computing training error");
			calcError(cr.getTrainIter(), ClusModelInfo.TRAIN_ERR, cr, ens_pred);
		}
		if (m_Sett.getOutput().isOutTestError() && cr.getTestIter() != null) {
			cr.getTestSet(); // this properly initialises the test set iterator if this was not done
								// previously
			if (getSettings().getGeneral().getVerbose() > 0)
				ClusLogger.info("Computing testing error");
			calcError(cr.getTestIter(), ClusModelInfo.TEST_ERR, cr, ens_pred);
		}
		if (m_Sett.getOutput().isOutValidError() && cr.getPruneSet() != null) {
			if (getSettings().getGeneral().getVerbose() > 0)
				ClusLogger.info("Computing validation error");
			calcError(cr.getPruneIter(), ClusModelInfo.VALID_ERR, cr, ens_pred);
		}
		if (summary != null) {
			summary.addSummary(cr);
		}
	}

	public final void out2model(String fname) throws Exception {
		String model_name = FileUtil.getName(fname) + ".model";
		ClusTreeReader rdr = new ClusTreeReader();
		ClusNode node = rdr.loadOutTree(fname, m_Schema, "Original Model");
		if (node == null) {
			// If original model not in .out, go with pruned version
			node = rdr.loadOutTree(fname, m_Schema, "Pruned Model");
		}
		if (node == null) {
			throw new ClusException("Unable to find original tree in .out file");
		}
		ClusRun cr = partitionData();
		ConstraintDFInduce induce = new ConstraintDFInduce(m_Induce);
		ClusNode orig = induce.fillInInduce(cr, node, getScore());
		orig.numberTree();
		PruneTree pruner = induce.getStatManager().getTreePruner(cr.getPruneSet());
		pruner.setTrainingData((RowData) cr.getTrainingSet());
		ClusNode pruned = (ClusNode) orig.cloneTree();
		pruner.prune(pruned);
		pruned.numberTree();
		ClusLogger.info();
		ClusLogger.info("Tree read from .out:");
		orig.printTree();
		ClusLogger.info();
		if (rdr.getLineAfterTree() != null) {
			ClusLogger.info("First line after tree: '" + rdr.getLineAfterTree() + "'");
			ClusLogger.info();
		}
		ClusModelCollectionIO io = new ClusModelCollectionIO();
		ClusModelInfo pruned_info = new ClusModelInfo("Pruned");
		pruned_info.setModel(pruned);
		io.addModel(pruned_info);
		ClusModelInfo orig_info = new ClusModelInfo("Original");
		orig_info.setModel(orig);
		io.addModel(orig_info);
		io.save(model_name);
	}

	/**
	 * Compute standard deviation and mean for each of the given attributes.
	 * 
	 * @param data     All the data
	 * @param numTypes Attributes we are computing these for.
	 * @return Index 0 includes means, index 1 std dev.
	 */
	static public double[][] calcStdDevsForTheSet(RowData data, NumericAttrType[] numTypes) {

		// ** Some of the values are not valid. These should not be used for
		// computing variance etc. *//
		int[] nbOfValidValues = new int[numTypes.length];

		// First means
		double[] means = new double[numTypes.length];

		/**
		 * Floating point computation is not to be trusted. It is important to track if
		 * variance = 0, (we are otherwise getting huge factors with 1/std dev).
		 */
		boolean[] varIsNonZero = new boolean[numTypes.length];

		// Check if variance = 0 i.e. all values are the same
		double[] prevAcceptedValue = new double[numTypes.length];

		for (int i = 0; i < prevAcceptedValue.length; i++) {
			prevAcceptedValue[i] = Double.NaN;
		}

		// Computing the means
		for (int iRow = 0; iRow < data.getNbRows(); iRow++) {
			DataTuple tuple = data.getTuple(iRow);

			for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
				double value = numTypes[jNumAttrib].getNumeric(tuple);
				if (!Double.isNaN(value) && !Double.isInfinite(value)) { // Value not given

					// Check if variance is zero
					if (!Double.isNaN(prevAcceptedValue[jNumAttrib]) && prevAcceptedValue[jNumAttrib] != value) {
						varIsNonZero[jNumAttrib] = true;
					}

					prevAcceptedValue[jNumAttrib] = value;
					means[jNumAttrib] += value;
					nbOfValidValues[jNumAttrib]++;
				}
			}
		}

		// Divide with the number of examples
		for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
			if (nbOfValidValues[jNumAttrib] == 0) {
				nbOfValidValues[jNumAttrib] = 1; // Do not divide with zero
			}

			if (!varIsNonZero[jNumAttrib]) {// if variance = 0, do not do any floating point computation
				means[jNumAttrib] = prevAcceptedValue[jNumAttrib];
			} else {
				means[jNumAttrib] /= nbOfValidValues[jNumAttrib];
			}
		}

		/** Variance for each of the attributes */
		double[] variance = new double[numTypes.length];

		// Computing the variances
		for (int iRow = 0; iRow < data.getNbRows(); iRow++) {
			DataTuple tuple = data.getTuple(iRow);

			for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
				double value = numTypes[jNumAttrib].getNumeric(tuple);
				if (!Double.isNaN(value) && !Double.isInfinite(value)) { // Value not given
					variance[jNumAttrib] += Math.pow(value - means[jNumAttrib], 2.0);
				}
			}
		}

		double[] stdDevs = new double[numTypes.length];

		// Divide with the number of examples
		for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++) {
			// if (variance[jNumAttrib] == 0) {
			if (!varIsNonZero[jNumAttrib]) {
				// If variance is zero, all the values are the same, so division
				// is not needed.
				variance[jNumAttrib] = 0.25; // And the divider will be
												// 2*1/sqrt(4)= 1
				ClusLogger.info("Warning: Variance of attribute " + jNumAttrib + " is zero.");
			} else {
				variance[jNumAttrib] /= nbOfValidValues[jNumAttrib];
			}

			stdDevs[jNumAttrib] = Math.sqrt(variance[jNumAttrib]);
		}

		double[][] meanAndStdDev = new double[2][];
		meanAndStdDev[0] = means;
		meanAndStdDev[1] = stdDevs;
		return meanAndStdDev;
	}

	/**
	 * Normalize the data so that most of the variables are within [-0.5,0.5] range.
	 * Using Zenko, 2007 suggestion. Should not be used with GD optimization - it
	 * has an internal normalization.
	 */
	public static final RowData returnNormalizedData(RowData data) {

		NumericAttrType[] numTypes = data.getSchema().getNumericAttrUse(AttributeUseType.All);
		double[][] meanAndStdDev = calcStdDevsForTheSet(data, numTypes);

		// Variance and mean for numeric types.
		double[] stdDevs = meanAndStdDev[1];
		double[] mean = meanAndStdDev[0];

		ArrayList<DataTuple> normalized = new ArrayList<DataTuple>();

		for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++)
			numTypes[jNumAttrib].setSparse(false); // Sparsity assumes that all values are > 0

		// Make the alterations
		for (int iRow = 0; iRow < data.getNbRows(); iRow++) {
			DataTuple tuple = data.getTuple(iRow).deepCloneTuple();
			int doublesLen = tuple.getDoubles().length;
			for (int jNumAttrib = 0; jNumAttrib < doublesLen; jNumAttrib++) {
				NumericAttrType type = numTypes[jNumAttrib];

				double value = type.getNumeric(tuple);
				if (!Double.isNaN(value) && !Double.isInfinite(value)) {// Value not given
					value -= mean[jNumAttrib]; // Putting the mean to 0

					// Suggestion by Zenko 2007 p. 22 4*standard deviation.
					// However we are also moving the data to
					// zero mean. Thus we take only 2 * standard deviation.
					value /= 2 * stdDevs[jNumAttrib];
					// value += 0.5; // Putting the mean to 0.5

					// After this transformation the mean should be about 0.0
					// and variance about 0.25
					// (and standard deviation 0.5). Thus 95% of values should
					// be between [-1,1]
				}
				type.setNumeric(tuple, value);
				// The following should be done only once for every numtype but
				// it does not
				// take very much time
			}
			normalized.add(tuple);
		}

		RowData normalized_data = new RowData(normalized, data.getSchema());
		ClusLogger.info("Normalized number of examples: " + normalized_data.getNbRows());

		// //DEBUG
		//
		//
		// variance = new double[numTypes.length];
		// mean = new double[numTypes.length];
		// //** Some of the values are not valid. These should not be used for
		// computing variance etc. *//
		// nbOfValidValues = new double[numTypes.length];
		//
		//
		//
		//
		// // Computing the means
		// for (int iRow = 0; iRow < normalized_data.getNbRows(); iRow++) {
		// DataTuple tuple = normalized_data.getTuple(iRow);
		//
		// for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++)
		// {
		// double value = numTypes[jNumAttrib].getNumeric(tuple);
		// if (!Double.isNaN(value) && !Double.isInfinite(value)) {// Value not
		// given
		// mean[jNumAttrib] += value;
		// nbOfValidValues[jNumAttrib]++;
		// }
		// }
		// }
		//
		// // Divide with the number of examples
		// for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++)
		// {
		// if (nbOfValidValues[jNumAttrib] == 0) {
		// nbOfValidValues[jNumAttrib] = 1; // Do not divide with zero
		// }
		// mean[jNumAttrib] /= nbOfValidValues[jNumAttrib];
		// }
		//
		// // Computing the variances
		// for (int iRow = 0; iRow < normalized_data.getNbRows(); iRow++) {
		// DataTuple tuple = normalized_data.getTuple(iRow);
		//
		// for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++)
		// {
		// double value = numTypes[jNumAttrib].getNumeric(tuple);
		// if (!Double.isNaN(value) && !Double.isInfinite(value)) // Value not
		// given
		// variance[jNumAttrib] += Math.pow(value - mean[jNumAttrib], 2.0);
		// }
		// }
		//
		// // Divide with the number of examples
		// for (int jNumAttrib = 0; jNumAttrib < numTypes.length; jNumAttrib++)
		// {
		//
		// variance[jNumAttrib] /= nbOfValidValues[jNumAttrib];
		//
		//
		// }

		return normalized_data;
	}

	/**
	 * Does not necessarily do the same as returnNormalizedData. However, stores the
	 * normalized data to file. Kept (and not modified) for compatibility purposes.
	 */
	public final void normalizeDataAndWriteToFile() throws IOException, ClusException {
		RowData data = m_Data;
		CombStat cmb = (CombStat) getStatManager().getTrainSetStat(AttributeUseType.All);
		RegressionStat rstat = cmb.getRegressionStat();
		NumericAttrType[] numtypes = getSchema().getNumericAttrUse(AttributeUseType.All);
		int tcnt = 0;
		for (int j = 0; j < numtypes.length; j++) {
			NumericAttrType type = numtypes[j];
			if (type.isTarget()) {
				tcnt++;
				ClusNumberFormat format = ClusFormat.THREE_AFTER_DOT;
				System.out.print(StringUtils.printStr("T" + tcnt + " ", 5));
				System.out.print(StringUtils.printStr(type.getName() + " ", 30));
				System.out.print(StringUtils.printStr(format.format(rstat.getMean(j)), 10));
				System.out.print(StringUtils.printStr(format.format(Math.sqrt(rstat.getVariance(j))), 10));
				ClusLogger.info();
			}
		}
		ArrayList<DataTuple> normalized = new ArrayList<DataTuple>();
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i).deepCloneTuple();
			for (int j = 0; j < numtypes.length; j++) {
				NumericAttrType type = numtypes[j];
				if (type.isTarget()) {
					double value = type.getNumeric(tuple);
					value -= rstat.getMean(j);
					value /= Math.sqrt(rstat.getVariance(j));
					type.setNumeric(tuple, value);
				}
			}
			normalized.add(tuple);
		}
		RowData normalized_data = new RowData(normalized, getSchema());
		ClusLogger.info("Size: " + normalized_data.getNbRows());

		String fname = m_Sett.getGeneric()
				.getFileAbsolute(FileUtil.getName(m_Sett.getData().getDataFile()) + "_norm.arff");
		ARFFFile.writeArff(fname, normalized_data);
	}

	public final void testModel(String fname)
			throws IOException, ClusException, ClassNotFoundException, InterruptedException {
		ClusModelCollectionIO io = ClusModelCollectionIO.load(fname);
		ClusModel res = io.getModel("Original");
		String test_name = m_Sett.getGeneric().getAppName() + ".test";
		ClusOutput out = new ClusOutput(test_name, m_Schema, m_Sett);
		ClusRun cr = partitionData();
		for (int i = io.getNbModels() - 1; i >= 0; i--) {
			ClusModelInfo mod_info = io.getModelInfo(i);
			ClusModelInfo def_info = cr.addModelInfo(mod_info.getModelInfo());
			def_info.setModel(io.getModel(i));
		}

		getStatManager().updateStatistics(res);
		getSchema().attachModel(res);
		// getClassifier().pruneAll(cr);
		getClassifier().postProcess(cr);
		calcError(cr, null, null);
		out.writeHeader();
		out.writeOutput(cr, true, m_Sett.getOutput().isOutTrainError());
		out.close();
	}

	public final void showModel(String fname) throws IOException, ClusException, ClassNotFoundException {
		ClusModelCollectionIO io = ClusModelCollectionIO.load(fname);
		ClusNode res = (ClusNode) io.getModel("Pruned");
		ClusLogger.info("Tree read from .model:");
		ClusLogger.info();
		res.inverseTests();
		res.printTree();
	}

	public final void saveModels(ClusRun models, ClusModelCollectionIO io) throws IOException, ClusException {
		if (getInduce().isModelWriter()) {
			getInduce().writeModel(io);
		}
		int pos = 0;
		for (int i = models.getNbModels() - 1; i >= 0; i--) {
			ClusModelInfo info = models.getModelInfo(i);
			if (info != null && info.shouldSave()) {
				io.insertModel(pos++, info);
			}
		}
	}

	public ClusRun train(RowData train) throws Exception {
		m_Induce = getClassifier().createInduce(train.getSchema(), m_Sett, m_CmdLine);
		ClusRun cr = partitionDataBasic(train);
		m_Induce.initialize();
		initializeAttributeWeights(m_Data);
		m_Induce.initializeHeuristic();
		getStatManager().computeTrainSetStat((RowData) cr.getTrainingSet());
		induce(cr, getClassifier());
		return cr;
	}

	public final void singleRun(ClusInductionAlgorithmType clss) throws Exception {
		ClusModelCollectionIO io = new ClusModelCollectionIO();
		m_Summary.setTotalRuns(1);
		ClusRun run = singleRunMain(clss, null);
		if (getSettings().getOutput().isWriteModelFile()) {
			if (!getSettings().getKNN().isKNN() && !getSettings().getRelief().isRelief()) {
				saveModels(run, io);
			}
			// io.save(getSettings().getFileAbsolute(m_Sett.getAppName() +
			// ".model"));
			if (ClusEnsembleInduce.isOptimized() && (m_Sett.getEnsemble().getNbBaggingSets().getVectorLength() > 1)
					&& (m_Sett.getEnsemble().getBagSelection().getIntVectorSorted()[0] < 1)) {
				io.save(getSettings().getGeneric().getFileAbsolute(
						m_Sett.getGeneric().getAppName() + "_" + ClusEnsembleInduce.getMaxNbBags() + "_.model"));
			} else {
				io.save(getSettings().getGeneric().getFileAbsolute(m_Sett.getGeneric().getAppName() + ".model"));
			}
		}
	}

	/*
	 * Run the prediction algorithm session once: train and possibly test and exit.
	 */
	public final ClusRun singleRunMain(ClusInductionAlgorithmType clss, ClusSummary summ) throws Exception {
		ClusOutput output = new ClusOutput(m_Sett.getGeneric().getAppName() + ".out", m_Schema, m_Sett);

		ClusRun cr = partitionData();

		// Compute statistic on training data
		getStatManager().computeTrainSetStat((RowData) cr.getTrainingSet());

		// Induce model
		induce(cr, clss);
		if (summ == null) {
			// E.g., rule-wise error measures
			addModelErrorMeasures(cr);
		}

		// Calculate error
		calcError(cr, null, null);

		if (summ != null) {
			for (int i = 0; i < cr.getNbModels(); i++) {
				ClusModelInfo info = cr.getModelInfo(i);
				ClusModelInfo summ_info = summ.getModelInfo(i);
				ClusErrorList test_err = summ_info.getTestError();
				info.setTestError(test_err);
			}
		}

		calcExtraTrainingSetErrors(cr);
		output.writeHeader();
		output.writeOutput(cr, true, m_Sett.getOutput().isOutTrainError());
		output.close();
		clss.saveInformation(m_Sett.getGeneric().getAppName());
		return cr;
	}

	public final XValMainSelection getXValSelection() throws IOException, ClusException {
		if (m_Sett.getData().isLOOXVal()) {
			return new XValRandomSelection(m_Data.getNbRows(), m_Data.getNbRows());
		} else if (m_Sett.getData().isNullXValFile()) {
			return m_Schema.getXValSelection(m_Data);
		} else {
			return XValDataSelection.readFoldsFile(m_Sett.getData().getXValFile(), m_Data.getNbRows());
		}
	}

	public final void combineAllFoldRuns(ClusInductionAlgorithmType clss) throws Exception {
		ClusOutput output = new ClusOutput(m_Sett.getGeneric().getAppName() + ".xval", m_Schema, m_Sett);
		output.writeHeader();
		XValMainSelection sel = getXValSelection();
		m_Summary.setTotalRuns(sel.getNbFolds());
		for (int fold = 0; fold < sel.getNbFolds(); fold++) {
			String dat_fname = "folds/" + m_Sett.getGeneric().getAppName() + ".fold." + fold;
			ClusLogger.info("Reading: " + dat_fname);
			ObjectLoadStream strm = new ObjectLoadStream(new FileInputStream(dat_fname));
			try {
				m_Summary.addSummary((ClusRun) strm.readObject());
				output.print((String) strm.readObject());
			} catch (ClassNotFoundException e) {
			}
			strm.close();
		}
		PrintWriter wrt = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(m_Sett.getGeneric().getAppName() + ".test.pred")));
		for (int fold = 0; fold < sel.getNbFolds(); fold++) {
			String pw_fname = "folds/" + m_Sett.getGeneric().getAppName() + ".test.pred." + fold;
			ClusLogger.info("Combining: " + pw_fname);
			LineNumberReader rdr = new LineNumberReader(new InputStreamReader(new FileInputStream(pw_fname)));
			String line = rdr.readLine();
			if (fold != 0) {
				// Only copy header from fold 0
				while (line != null && !line.equals("@DATA")) {
					line = rdr.readLine();
				}
				line = rdr.readLine();
			}
			while (line != null) {
				wrt.println(line);
				line = rdr.readLine();
			}
			rdr.close();
		}
		wrt.close();
		output.writeSummary(m_Summary);
		output.close();
		/* Cross-validation now includes a single run */
		ClusRandom.initialize(m_Sett);
		singleRunMain(clss, m_Summary);
	}

	public final void oneFoldRun(ClusInductionAlgorithmType clss, int fold) throws Exception {
		if (fold == 0) {
			combineAllFoldRuns(clss);
		} else {
			fold = fold - 1;
			FileUtil.mkdir("folds");
			ClusOutput output = new ClusOutput(m_Schema, m_Sett);
			ClusStatistic target = getStatManager().createStatistic(AttributeUseType.Target);
			String pw_fname = "folds/" + m_Sett.getGeneric().getAppName() + ".test.pred." + fold;
			PredictionWriter wrt = new PredictionWriter(pw_fname, m_Sett, target);
			wrt.globalInitialize(m_Schema);
			XValMainSelection sel = getXValSelection();
			ClusModelCollectionIO io = new ClusModelCollectionIO();
			m_Summary.setTotalRuns(sel.getNbFolds());
			ClusRun cr = doOneFold(fold, clss, sel, io, wrt, output, null, null);
			wrt.close();
			output.close();
			// Write summary of this run to a file
			// cr.deleteDataAndModels();
			cr.deleteData();
			String dat_fname = "folds/" + m_Sett.getGeneric().getAppName() + ".fold." + fold;
			ObjectSaveStream strm = new ObjectSaveStream(new FileOutputStream(dat_fname));
			strm.writeObject(cr);
			strm.writeObject(output.getString());
			strm.close();
		}
	}

	public final ClusRun doOneFold(int fold, ClusInductionAlgorithmType clss, XValMainSelection sel,
			ClusModelCollectionIO io, PredictionWriter wrt, ClusOutput output, ClusErrorOutput errOutput,
			ClusEnsemblePredictionWriter ens_pred) throws Exception {
		XValSelection msel = new XValSelection(sel, fold);
		ClusRun cr = partitionData(msel, fold + 1);
		// Create statistic for the training set
		getStatManager().computeTrainSetStat((RowData) cr.getTrainingSet());
		if (wrt != null) {
			wrt.println("! Fold = " + fold);
			cr.getAllModelsMI().addModelProcessor(ClusModelInfo.TEST_ERR, wrt);

			if (m_Sett.getOutput().isOutFoldData()) {
				ClusLogger.info("Writing train and test ARFF files for fold " + fold);

				ARFFFile.writeArff(m_Sett.getGeneric().getAppName() + "-test-" + fold + ".arff", cr.getTestSet());
				ARFFFile.writeArff(m_Sett.getGeneric().getAppName() + "-train-" + fold + ".arff",
						(RowData) cr.getTrainingSet());
			}
		}

		// Used for exporting data to CN2 and Orange formats

		/*
		 * ARFFFile.writeCN2Data("test-"+i+".exs", cr.getTestSet());
		 * ARFFFile.writeCN2Data("train-"+i+".exs", (RowData)cr.getTrainingSet());
		 * ARFFFile.writeOrangeData("test-"+fold+".tab", cr.getTestSet());
		 * ARFFFile.writeOrangeData("train-"+fold+".tab", (RowData)cr.getTrainingSet());
		 */
		// ARFFFile.writeRData("trainDataForR."+ fold + ".data",
		// (RowData)cr.getTrainingSet()); ARFFFile.writeRData("testDataForR."+
		// fold + ".data", (RowData)cr.getTestSet());
		// ARFFFile.writeRDataNominalLabels("nominalLabels.data",
		// (RowData)cr.getTrainingSet());
		// System.err.println("CHANGING DATA TO R FORMAT, REMOVE THIS CODE");

		if (m_Sett.getGeneral().isDoNotInduce()) {
			ClusLogger.info("Skipping model induction");
			return cr;
		}
		// Induce tree
		induce(cr, clss);
		if (m_Sett.getRules().isRuleWiseErrors()) {
			addModelErrorMeasures(cr);
		}
		// Calc error
		calcError(cr, m_Summary, ens_pred);
		if (errOutput != null) {
			// Write errors to ARFF file
			errOutput.writeOutput(cr, false, false, getStatManager().getClusteringWeights().m_Weights);
		}
		if (m_Sett.getOutput().isOutputFoldModels()) {
			// Write output to file and also store in .model file
			output.writeOutput(cr, false);
			if (!getSettings().getEnsemble().isEnsembleMode()) {
				ClusModelInfo mi = cr.getModelInfo(ClusModel.PRUNED);
				if (mi != null)
					io.addModel(mi);
			}
		} else {
			output.writeBrief(cr);
		}
		return cr;
	}

	public final void xvalRun(ClusInductionAlgorithmType clss) throws Exception {
		ClusErrorOutput errFileOutput = null;
		if (getSettings().getOutput().isWriteErrorFile()) {
			errFileOutput = new ClusErrorOutput(m_Sett.getGeneric().getAppName() + ".err", m_Schema, m_Sett);

			errFileOutput.writeHeader();
		}
		PredictionWriter testPredWriter = null;
		if (getSettings().getOutput().isWriteTestSetPredictions()) {
			ClusStatistic target = getStatManager().createStatistic(AttributeUseType.Target);
			testPredWriter = new PredictionWriter(m_Sett.getGeneric().getAppName() + ".test.pred.arff", m_Sett, target);
			testPredWriter.globalInitialize(m_Schema);
		}
		/* Perform cross-validation */
		ClusOutput output = new ClusOutput(m_Sett.getGeneric().getAppName() + ".xval", m_Schema, m_Sett);
		output.writeHeader();
		XValMainSelection sel = getXValSelection();
		ClusModelCollectionIO io = new ClusModelCollectionIO();
		ClusEnsemblePredictionWriter ens_pred = null;
		if (getSettings().getEnsemble().shouldWritePredictionsFromEnsemble())
			ens_pred = new ClusEnsemblePredictionWriter(
					getStatManager().getSettings().getGeneric().getAppName() + ".ens.xval.preds",
					getStatManager().getSchema(), getStatManager().getSettings());

		for (int fold = 0; fold < sel.getNbFolds(); fold++) {
			doOneFold(fold, clss, sel, io, testPredWriter, output, errFileOutput, ens_pred);
		}
		if (getSettings().getEnsemble().shouldWritePredictionsFromEnsemble())
			ens_pred.closeWriter();
		output.writeSummary(m_Summary);
		output.close();
		if (testPredWriter != null)
			testPredWriter.close();
		if (errFileOutput != null)
			errFileOutput.close();
		/* Cross-validation now includes a single run */
		ClusRandom.initialize(m_Sett);
		ClusRun run = singleRunMain(clss, m_Summary);
		// this should not be on for KNN
		if (!getSettings().getKNN().isKNN() && getSettings().getOutput().isWriteModelFile()) {
			saveModels(run, io);
			io.save(getSettings().getGeneric().getFileAbsolute(m_Sett.getGeneric().getAppName() + ".model"));
		}
	}

	public final void baggingRun(ClusInductionAlgorithmType clss) throws Exception {
		ClusOutput output = new ClusOutput(m_Sett.getGeneric().getAppName() + ".bag", m_Schema, m_Sett);
		output.writeHeader();
		ClusStatistic target = getStatManager().createStatistic(AttributeUseType.Target);
		PredictionWriter wrt = new PredictionWriter(m_Sett.getGeneric().getAppName() + ".test.pred.arff", m_Sett,
				target);
		wrt.globalInitialize(m_Schema);
		int nbsets = m_Sett.getExperimental().getBaggingSets();
		int nbrows = m_Data.getNbRows();
		for (int i = 0; i < nbsets; i++) {
			BagSelection msel = new BagSelection(nbrows, getSettings().getEnsemble().getEnsembleBagSize(), null);
			ClusRun cr = partitionData(msel, i + 1);
			ClusModelInfo mi = cr.getModelInfo(ClusModel.PRUNED);
			mi.addModelProcessor(ClusModelInfo.TEST_ERR, wrt);
			induce(cr, clss); // Induce tree
			calcError(cr, m_Summary, null); // Calc error
			if (m_Sett.getOutput().isOutputFoldModels())
				output.writeOutput(cr, false); // Write output to file
		}
		wrt.close();
		output.writeSummary(m_Summary);
		output.close();
	}

	/*
	 * clss is the object on which the induce methode is called :in our case it is a
	 * ClusDecisionTree Modify to have more than one model as an output !
	 */
	public final void exhaustiveRun(ClusInductionAlgorithmType clss) throws Exception {
		ClusOutput output = new ClusOutput(m_Sett.getGeneric().getAppName() + ".all", m_Schema, m_Sett);
		output.writeHeader();
		ClusRun cr = partitionData();
		induce(cr, clss); // Induce model
		output.writeOutput(cr, false); // Write output to file
		output.writeSummary(m_Summary);
		output.close();
	}

	private class MyClusInitializer implements ClusSchemaInitializer {

		@Override
		public void initSchema(ClusSchema schema) throws ClusException, IOException {
			if (getSettings().getTree().getHeuristic().equals(Heuristic.SSPD)) {
				schema.addAttrType(new IntegerAttrType("SSPD"));
			}
			schema.setTarget(new IntervalCollection(m_Sett.getAttribute().getTarget()));
			schema.setDisabled(new IntervalCollection(m_Sett.getAttribute().getDisabled()));
			schema.setClustering(new IntervalCollection(m_Sett.getAttribute().getClustering()));
			schema.setDescriptive(new IntervalCollection(m_Sett.getAttribute().getDescriptive()));
			schema.setKey(new IntervalCollection(m_Sett.getAttribute().getKey()));
			schema.updateAttributeUse();
			schema.initializeFrom(m_Schema);
		}
	}

	public void writeTargets() throws ClusException, IOException {
		ClassHierarchy hier = getStatManager().getHier();
		if (hier != null) {
			hier.writeTargets(m_Data, m_Schema, m_Sett.getGeneric().getAppName());
		}
	}

	public void showInfo() throws ClusException, IOException {
		RowData data = m_Data;
		ClusLogger.info("Name            #Rows      #Missing  #Nominal #Numeric #Target  #Classes");
		System.out.print(StringUtils.printStr(m_Sett.getGeneric().getAppName(), 16));
		System.out.print(StringUtils.printInt(data.getNbRows(), 11));
		// double perc = -1; //
		// (double)m_Schema.getTotalInputNbMissing()/data.getNbRows()/m_Schema.getNbNormalAttr()*100.0;
		double perc = m_Schema.getTotalInputNbMissing() / data.getNbRows() / m_Schema.getNbDescriptiveAttributes()
				* 100.0;
		System.out.print(StringUtils.printStr(ClusFormat.TWO_AFTER_DOT.format(perc) + "%", 10));
		System.out.print(StringUtils.printInt(m_Schema.getNbNominalDescriptiveAttributes(), 9));
		System.out.print(StringUtils.printInt(m_Schema.getNbNumericDescriptiveAttributes(), 9));
		System.out.print(StringUtils.printInt(m_Schema.getNbAllAttrUse(AttributeUseType.Target), 9));
		NominalAttrType[] tarnom = m_Schema.getNominalAttrUse(AttributeUseType.Target);
		if (tarnom != null && tarnom.length >= 1) {
			if (tarnom.length == 1)
				ClusLogger.info(tarnom[0].getNbValues());
			else
				ClusLogger.info("M:" + tarnom.length);
		} else {
			ClusLogger.info("(num)");
		}
		ClusLogger.info();
		m_Schema.showDebug();
		if (getStatManager().hasClusteringStat()) {
			ClusStatistic[] stats = new ClusStatistic[2];
			stats[0] = getStatManager().createClusteringStat();
			stats[1] = getStatManager().createStatistic(AttributeUseType.All);
			m_Data.calcTotalStats(stats);
			if (!m_Sett.getData().isNullTestFile()) {
				ClusLogger.info("Loading: " + m_Sett.getData().getTestFile());
				updateStatistic(m_Sett.getData().getTestFile(), stats);
			}
			if (!m_Sett.getData().isNullPruneFile()) {
				ClusLogger.info("Loading: " + m_Sett.getData().getPruneFile());
				updateStatistic(m_Sett.getData().getPruneFile(), stats);
			}
			ClusStatistic.calcMeans(stats);
			MyFile statf = new MyFile(getSettings().getGeneric().getAppName() + ".distr");
			statf.log("** Target:");
			stats[0].printDistribution(statf.getWriter());
			statf.log("** All:");
			stats[1].printDistribution(statf.getWriter());
			statf.close();
		}
	}

	public void updateStatistic(String fname, ClusStatistic[] stats) throws ClusException, IOException {
		MyClusInitializer init = new MyClusInitializer();
		TupleIterator iter = new DiskTupleIterator(fname, init, getPreprocs(true), m_Sett);
		iter.init();
		DataTuple tuple = iter.readTuple();
		while (tuple != null) {
			for (int i = 0; i < stats.length; i++) {
				stats[i].updateWeighted(tuple, 1.0);
			}
			tuple = iter.readTuple();
		}
		iter.close();
	}

	public void setFolds(int folds) {
		m_Sett.getData().setXValFolds(folds);
	}

	public void showDebug() {
		m_Schema.showDebug();
	}

	@Override
	public void showHelp() {
		ClusMisc.showHelp();
	}

	@Override
	public String[] getOptionArgs() {
		return OPTION_ARGS;
	}

	@Override
	public int[] getOptionArgArities() {
		return OPTION_ARITIES;
	}

	@Override
	public int getNbMainArgs() {
		return 1;
	}

	public String getAppName() {
		return m_Sett.getGeneric().getAppName();
	}

	private static void tryAnalyzePredictions(Clus clus, Settings sett) {
		// daniela only form 1 target
		int ts_size = clus.getNbRows();
		double b = clus.getSettings().getTree().getBandwidth();
		String ts_name = sett.getGeneric().getAppNameWithSuffix() + ".test.pred.arff";
		try {
			// NumericAttrType[] t =
			// clus.m_Schema.getNumericAttrUse(AttributeUseType.Target);
			NominalAttrType[] t = clus.m_Schema.getNominalAttrUse(AttributeUseType.Target);
			// ClusLogger.info(t.length); // matejp commented this out
			if (t.length == 1) {
				PredictionAnalyzer.calculateI(ts_name, ts_size, b);
			} else {
				PredictionAnalyzer.calculateBI(ts_name, ts_size, b);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Prediction analysis failed.");
		}
		// end daniela
	}

	public static void main(String[] args) {
		try {

			Clus clus = new Clus();
			Settings sett = clus.getSettings();
			CMDLineArgs cargs = new CMDLineArgs(clus);
			cargs.process(args);

			if (cargs.hasOption("copying")) {
				ClusMisc.printGPL();
				System.exit(0);
			} else if (cargs.getNbMainArgs() == 0) {
				clus.showHelp();
				System.exit(0);
			}

			ClusMisc.printHeader(cargs.hasOption("silent"));

			if (cargs.allOK()) {
				sett.getGeneric().setDate(new Date());
				sett.getGeneric().setAppName(cargs.getMainArg(0));

				clus.initSettings(cargs);

				ClusLogger.initialize(sett.getGeneral()); // initialization of logging.

				ClusInductionAlgorithmType clss = null;

				/**
				 * There are two groups of command line parameters of type -<parameter>. From
				 * both of them at most one can be used. The first group is the learning method.
				 * Options are Relief, knn, knnTree, rules, weka (for Weka workbench),
				 * tuneftest, tunesize, beam (beam search induction tree), exhaustive, sit
				 * (inductive transfer learning), forest (ensemble trees). If the parameter is
				 * not given, a single decision tree is used. TODO What do the other parameter
				 * values mean? (e.g. tuneftest, exhaustive) TODO There should be a command line
				 * help for these. For example with -help.
				 */
				if (cargs.hasOption("relief")) {
					clus.getSettings().getRelief().setSectionReliefEnabled(true);
					clus.getSettings().getKNN().setSectionKNNEnabled(true); // Relief and kNN settings have a non-empty
																			// intersection
					clus.getSettings().getOutput().setOutValidError(false);
					clus.getSettings().getOutput().setOutTrainError(false);
					clus.getSettings().getOutput().setOutTestError(false);
					clss = new Relief(clus);
				} else if (cargs.hasOption("knn")) {
					clus.getSettings().getKNN().setSectionKNNEnabled(true);
					clus.getSettings().getTree().setMissingAttrHandlingToTrainingSet();
					clss = new KnnClassifier(clus);
				} else if (cargs.hasOption("rules")) {
					clus.getSettings().getBeamSearch().setSectionBeamEnabled(true);
					clus.getSettings().getRules().setSectionRulesEnabled(true);
					clss = new ClusRuleClassifier(clus);
					if (!sett.getEnsemble().getEnsembleROSAlgorithmType().equals(EnsembleROSAlgorithmType.Disabled)
							&& sett.getRules().isROSParameterAutoTune()) {
						// ROS parameter tuning
						clss = new FIREROSTune(clss, new String[] { "0.25", "0.5", "0.75", "Random" });
					}

				} else if (cargs.hasOption("weka")) {
					// clss = new ClusWekaClassifier(clus,
					// cargs.getOptionValue("weka"));
				} else if (cargs.hasOption("tuneftest")) {
					clss = new ClusDecisionTree(clus);
					clss = new CDTTuneFTest(clss);
				} else if (cargs.hasOption("tunesize")) {
					clss = new ClusDecisionTree(clus);
					clss = new CDTuneSizeConstrPruning(clss);
				} else if (cargs.hasOption("beam")) {
					clus.getSettings().getBeamSearch().setSectionBeamEnabled(true);
					clss = sett.getBeamSearch().isFastBS() ? new ClusFastBeamSearch(clus) : new ClusBeamSearch(clus);
				} else if (cargs.hasOption("exhaustive")) {
					// new part added by elisa 1/08/2006
					clus.getSettings().getExhaustiveSearch().setSectionExhaustiveEnabled(true);
					clss = new ClusExhaustiveDFSearch(clus);
				} else if (cargs.hasOption("sit")) {
					// new part by beau
					clus.getSettings().getSIT().setSectionSITEnabled(true);
					clss = new ClusDecisionTree(clus);
					clss = new ClusSITDecisionTree(clss);
				} else if (cargs.hasOption("forest")) {
					sett.getEnsemble().setEnsembleMode(true);
					clss = new ClusEnsembleClassifier(clus);
					if (sett.getTree().getFTestArray().isVector()) {
						clss = new CDTTuneFTest(clss, sett.getTree().getFTestArray().getDoubleVector());
					}
				} else if (cargs.hasOption("option")) {
					clus.getSettings().getOptionTree().setSectionOptionEnabled(true);
					clss = new ClusOptionTree(clus);
					if (sett.getTree().getFTestArray().isVector())
						clss = new CDTTuneFTest(clss, sett.getTree().getFTestArray().getDoubleVector());
				} else {
					clss = new ClusDecisionTree(clus);
					if (sett.getTree().getFTestArray().isVector()) {
						// F-Test tuning
						clss = new CDTTuneFTest(clss, sett.getTree().getFTestArray().getDoubleVector());
					}
				}

				// check if semi-supervised is turned on
				if (cargs.hasOption("ssl")) {
					sett.getSSL().setSemiSupervisedMode(true);

					// by default SSL-PCTs takes all descriptive attributes as clustering (if
					// clustering atts are not
					// explicitly set)
					if (sett.getSSL().getSemiSupervisedMethod().equals(SSLMethod.PCT)) {
						if (sett.getAttribute().getClustering().equals("Default")) // Clustering attributes are not set,
																					// use Descriptive+Target as
																					// Clustering
							sett.getAttribute().setClustering(
									sett.getAttribute().getDescriptive() + "," + sett.getAttribute().getTarget());
					}

					clss = new ClusSemiSupervisedClassifier(clus, clss);
				}

				if (cargs.hasOption("clowdflows")) {
					clus.getSettings().getOutput().setOutputClowdFlows(true);
				}

				/**
				 * The second group of command line parameters is for miscellaneous action.
				 */
				if (cargs.hasOption("corrmatrix")) {
					clus.initialize(cargs, clss);
					CorrelationMatrixComputer cmp = new CorrelationMatrixComputer();
					cmp.compute(clus.m_Data);
					cmp.printMatrixTeX();
				} else if (cargs.hasOption("info")) {
					clus.initialize(cargs, clss);
					clus.showInfo();
				} else if (cargs.hasOption("writetargets")) {
					clus.initialize(cargs, clss);
					clus.writeTargets();
				} else if (cargs.hasOption("out2model")) {
					clus.initialize(cargs, clss);
					clus.out2model(cargs.getOptionValue("out2model"));
				} else if (cargs.hasOption("test")) {
					clus.initialize(cargs, clss);
					clus.testModel(cargs.getOptionValue("test"));
				} else if (cargs.hasOption("normalize")) {
					clus.initialize(cargs, clss);
					clus.normalizeDataAndWriteToFile();
				} else if (cargs.hasOption("debug")) {
					clus.initialize(cargs, clss);
					clus.showDebug();
				} else if (cargs.hasOption("xval")) { // cross validation
					clus.isxval = true;
					clus.initialize(cargs, clss);
					clus.xvalRun(clss);
				} else if (cargs.hasOption("fold")) {
					clus.isxval = true;
					clus.initialize(cargs, clss);

					List<Integer> f = cargs.getOptionList("fold");
					for (int i : f) {
						clus.oneFoldRun(clss, i);
					}
				} else if (cargs.hasOption("bag")) {
					clus.isxval = true;
					clus.initialize(cargs, clss);
					clus.baggingRun(clss);
				} else if (cargs.hasOption("tseries")) {
					clus.getSettings().getTimeSeries().setSectionTimeSeriesEnabled(true);
					clus.initialize(cargs, clss);
					clus.singleRun(clss);
				} else {
					clus.initialize(cargs, clss);
					clus.singleRun(clss);
				}
			}
			if (Clus.isDebug()) {
				ClusStat.show();
			}
			if (Clus.isDebug())
				ClusStat.show();
			DebugFile.close();
			if (!sett.getAttribute().isNullGIS()) {
				tryAnalyzePredictions(clus, sett);
			}

			ClusLogger.info("Done.");
		} catch (ClusException e) {
			System.err.println("Error: " + e);
			e.printStackTrace();
			System.exit(-1);
		} catch (IllegalArgumentException e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(-2);
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e);
			e.printStackTrace();
			System.exit(-3);
		} catch (IOException e) {
			System.err.println("IO Error: " + e);
			e.printStackTrace();
			System.exit(-4);
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found" + e);
			e.printStackTrace();
			System.exit(-5);
		} catch (InterruptedException e) {
			System.err.println("Error: " + e);
			e.printStackTrace();
			System.exit(-6);
		} catch (NotImplementedException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-7);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-100);
		}
	}

	/**
	 * Checks if global debugging flags are set for the input flag. The method
	 * returns false if global debugging flags contain DebugType.None.
	 * 
	 * @param flag This flag will be checked.
	 */
	public static boolean isDebug(DebugType flag) {

		if (Debugging.contains(DebugType.None))
			return false;

		return Debugging.contains(flag);
	}

	/**
	 * A convenience method. Checks for general debugging status.
	 */
	public static boolean isDebug() {
		return isDebug(DebugType.General);
	}

	/**
	 * This sets global debugging flags.
	 * 
	 * @param flags Use EnumSet.of({@literal <}your debug types{@literal >}) to set the desired debugging
	 *              flags
	 */

	public static void setDebug(EnumSet<DebugType> flags) {
		Debugging = flags;
	}
}