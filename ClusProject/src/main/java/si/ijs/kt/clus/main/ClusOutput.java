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

package si.ijs.kt.clus.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.ext.ensemble.ClusForest;
import si.ijs.kt.clus.ext.ensemble.ClusOOBErrorEstimate;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsOutput.PythonModelType;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.statistic.StatisticPrintInfo;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusUtil;
import si.ijs.kt.clus.util.ResourceInfo;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.jeans.util.FileUtil;
import si.ijs.kt.clus.util.jeans.util.StringUtils;

/**
 * Class for outputting the training and testing results to .out file. All the
 * information during the process is gathered here.
 *
 */
public class ClusOutput {

	protected ClusSchema m_Schema;
	protected Settings m_Sett;
	protected PrintWriter m_Writer;
	protected String m_Fname;
	protected Settings m_Sett2;
	protected StringWriter m_StrWrt;

	public ClusOutput(String fname, ClusSchema schema, Settings sett) throws IOException {
		m_Schema = schema;
		m_Sett = sett;
		m_Sett2 = sett;
		m_Fname = fname;
		m_Writer = sett.getGeneric().getFileAbsoluteWriter(fname);
	}

	public ClusOutput(ClusSchema schema, Settings sett) throws IOException {
		m_Schema = schema;
		m_Sett = sett;
		m_Sett2 = sett;
		m_StrWrt = new StringWriter();
		m_Writer = new PrintWriter(m_StrWrt);
	}

	public void print(String str) {
		m_Writer.print(str);
	}

	public String getString() {
		return m_StrWrt.toString();
	}

	public Settings getSettings() {
		return m_Sett;
	}

	public void writeHeader() throws IOException {
		String relname = m_Schema.getRelationName();
		m_Writer.println("Clus run " + relname);
		m_Writer.println(StringUtils.makeString('*', 9 + relname.length()));
		m_Writer.println();
		Date date = m_Schema.getSettings().getGeneric().getDate();
		m_Writer.println("Date: " + DateFormat.getInstance().format(date));
		m_Writer.println("File: " + m_Fname);
		int a_tot = m_Schema.getNbAttributes();
		int a_in = m_Schema.getNbDescriptiveAttributes();
		int a_out = m_Schema.getNbTargetAttributes();
		m_Writer.println("Attributes: " + a_tot + " (input: " + a_in + ", output: " + a_out + ")");
		m_Writer.println("Missing values: " + (m_Schema.hasMissing() ? "Yes" : "No"));
		if (ResourceInfo.isLibLoaded()) {
			m_Writer.println("Memory usage: " + ClusStat.m_InitialMemory + " kB (initial), " + ClusStat.m_LoadedMemory
			        + " kB (data loaded)");
		}
		m_Writer.println();
		m_Sett.show(m_Writer);
		m_Writer.flush();
	}

	public void writeBrief(ClusRun cr) throws IOException {
		String ridx = cr.getIndexString();
		m_Writer.println("Run: " + ridx);
		ClusErrorList te_err = cr.getTestError();
		if (te_err != null) {
			te_err.showErrorBrief(cr, ClusModelInfo.TEST_ERR, m_Writer);
		}
		ClusErrorList tr_err = cr.getTrainError();
		if (m_Sett.getOutput().isOutTrainError() && tr_err != null) {
			tr_err.showErrorBrief(cr, ClusModelInfo.TRAIN_ERR, m_Writer);
		}
		m_Writer.println();
	}

	public void writeOutput(ClusRun cr, boolean detail) throws IOException, ClusException, InterruptedException {
		writeOutput(cr, detail, false);
	}

	public void writeOutput(ClusRun cr, boolean detail, boolean outputtrain)
	        throws IOException, ClusException, InterruptedException {
		ArrayList<ClusModel> models = new ArrayList<ClusModel>();
		String ridx = cr.getIndexString();

		if (getSettings().getHMTR().isSectionHMTREnabled()) {
			m_Writer.println("HMTR Tree");
			m_Writer.println("---------");
			m_Writer.println();
			m_Writer.println(m_Schema.getHMTRHierarchy().printHierarchyTree());
		}

		m_Writer.println("Run: " + ridx);
		m_Writer.println(StringUtils.makeString('*', 5 + ridx.length()));
		m_Writer.println();
		m_Writer.println("Statistics");
		m_Writer.println("----------");
		m_Writer.println();
		m_Writer.println("FTValue (FTest): " + m_Sett.getTree().getFTest());

		if (!ClusOOBErrorEstimate.isOOBCalculation()) {
			double tsec = cr.getInductionTime() / 1000.0;
			double tpru = cr.getPruneTime() / 1000.0;
			long tpred = cr.getPredictionTime();
			long tpredavg = cr.getPredictionTimeAverage();

			String parallelTime = "";
			if (getSettings().getEnsemble().isEnsembleWithParallelExecution()) {
				parallelTime = " (sequential "
				        + ClusFormat.FOUR_AFTER_DOT.format((cr.getInductionTimeSequential() / 1000.0)) + " sec)";
			}

			// Compute statistics
			String cpu = ResourceInfo.isLibLoaded() ? " (CPU)" : "";
			m_Writer.println("Induction Time: " + ClusFormat.FOUR_AFTER_DOT.format(tsec) + " sec" + parallelTime + cpu);
			m_Writer.println("Pruning Time: " + ClusFormat.FOUR_AFTER_DOT.format(tpru) + " sec" + cpu);
			m_Writer.println("Prediction Time (total for ClusModel.Original): " + System.lineSeparator() + "\t"
			        + ClusFormat.FOUR_AFTER_DOT.format(tpred / Math.pow(10, 3)) + " microsecs" + System.lineSeparator()
			        + "\t" + ClusFormat.FOUR_AFTER_DOT.format(tpred / Math.pow(10, 6)) + " millisecs"
			        + System.lineSeparator() + "\t" + ClusFormat.FOUR_AFTER_DOT.format(tpred / Math.pow(10, 9))
			        + " secs");
			m_Writer.println("Prediction Time (average for ClusModel.Original): "
			        + ClusFormat.FOUR_AFTER_DOT.format(tpredavg / Math.pow(10, 3)) + " microsecs");
		}

		// Prepare models for printing if required
		for (int i = 0; i < cr.getNbModels(); i++) {
			ClusModelInfo mi = cr.getModelInfo(i);
			if (mi != null) {
				ClusModel root = mi.getModel();
				if (mi.shouldPruneInvalid()) {
					root = root.prune(ClusModel.PRUNE_INVALID);
				}
				models.add(root);
			} else {
				models.add(null);
			}
		}

		m_Writer.println("Model information:");
		for (int i = 0; i < cr.getNbModels(); i++) {
			ClusModelInfo mi = cr.getModelInfo(i);
			if (mi != null) {
				ClusModel model = models.get(i);
				// A model info without an actual model is possible
				// E.g., to report error measures in HMCAverageSingleClass8
				if (model != null) {
					m_Writer.print("     " + mi.getName() + ": ");
					String info_str = model.getModelInfo();
					String[] info = info_str.split("\\s*\\,\\s*");
					for (int j = 0; j < info.length; j++) {
						if (j > 0)
							m_Writer.print(StringUtils.makeString(' ', mi.getName().length() + 7));
						m_Writer.println(info[j]);
					}
				}
			}
		}

		// print multi-label thresholds
		if (cr.getStatManager().getSettings().getMLC().shouldShowThresholds()) {
			String mlThresholdsTitle = "MultiLabelThresholds:";
			m_Writer.println(System.lineSeparator() + mlThresholdsTitle);
			m_Writer.println(StringUtils.makeString('-', mlThresholdsTitle.length()));
			for (int i = 0; i < cr.getNbModels(); i++) {
				ClusModel root = models.get(i);
				String modelName = cr.getModelInfo(i).getName();
				if (cr.getStatManager().getSettings().getMLC().shouldShowThresholds(modelName)) {
					if (root instanceof ClusForest) {
						m_Writer.println(modelName);
						ClusForest forest = (ClusForest) root;
						int forestSize = forest.getModelSize();
						for (int tree = 0; tree < forestSize; tree++) {
							((ClusNode) forest.getModel(tree)).printMultiLabelThresholds(m_Writer, tree);
						}
						m_Writer.println();
					} else if (root instanceof ClusNode) {
						m_Writer.println(modelName);
						((ClusNode) root).printMultiLabelThresholds(m_Writer, -1);
						m_Writer.println();
					}
				}
			}
		}

		// Compute basename - not needed
		String bName = FileUtil.getName(m_Fname);
		m_Writer.println();
		ClusErrorList te_err = cr.getTestError();
		if (m_Sett.getOutput().isOutFoldError() || detail) {
			if (outputtrain) {
				ClusErrorList tr_err = cr.getTrainError();
				if (tr_err != null) {
					if (ClusOOBErrorEstimate.isOOBCalculation()) {
						m_Writer.println("Out-Of-Bag Estimate of Error");
						m_Writer.println("----------------------------");
					} else {
						m_Writer.println("Training error");
						m_Writer.println("--------------");
					}
					m_Writer.println();
					tr_err.showError(cr, ClusModelInfo.TRAIN_ERR, bName + ".train", m_Writer, m_Sett);
					// tr_err.showError(cr, ClusModelInfo.TRAIN_ERR, m_Writer);
					m_Writer.println();
				}
				ClusErrorList.printExtraError(cr, ClusModelInfo.TRAIN_ERR, m_Writer);
			}
			ClusErrorList va_err = cr.getValidationError();
			if (!ClusOOBErrorEstimate.isOOBCalculation() && va_err != null && m_Sett.getOutput().isOutValidError()) {
				m_Writer.println("Validation error");
				m_Writer.println("----------------");
				m_Writer.println();
				va_err.showError(cr, ClusModelInfo.VALID_ERR, bName + ".valid", m_Writer, m_Sett);
				// va_err.showError(cr, ClusModelInfo.VALID_ERR, m_Writer);
				m_Writer.println();
			}
			if (!ClusOOBErrorEstimate.isOOBCalculation() && te_err != null && m_Sett.getOutput().isOutTestError()) {
				m_Writer.println("Testing error");
				m_Writer.println("-------------");
				m_Writer.println();
				te_err.showError(cr, ClusModelInfo.TEST_ERR, bName + ".test", m_Writer, m_Sett);
				// te_err.showError(cr, ClusModelInfo.TEST_ERR, m_Writer);
				m_Writer.println();
			}
		}
		StatisticPrintInfo info = m_Sett.getOutput().getStatisticPrintInfo();
		if (!ClusOOBErrorEstimate.isOOBCalculation()) {

			for (int i = 0; i < cr.getNbModels(); i++) {
				if (cr.getModelInfo(i) != null && models.get(i) != null && m_Sett.getOutput().shouldShowModel(i)) {
					ClusModelInfo mi = cr.getModelInfo(i);
					ClusModel root = models.get(i);
					String modelname = mi.getName() + " Model";
					m_Writer.println(modelname);
					m_Writer.println(StringUtils.makeString('*', modelname.length()));
					m_Writer.println();
					if (m_Sett.getOutput().isPrintModelAndExamples()) {
						RowData pex = (RowData) cr.getTrainingSet();
						// ClusLogger.info(te_err);
						if (te_err != null)
							pex = cr.getTestSet();
						root.printModelAndExamples(m_Writer, info, pex);
					} else {
						root.printModel(m_Writer, info);
					}
					m_Writer.println();
				}
			}
		}

		if (!ClusOOBErrorEstimate.isOOBCalculation() && getSettings().getOutput().isOutputPythonModel()) {
			String appName = m_Fname.substring(0, m_Fname.lastIndexOf(".out"));
			PythonModelType pyModelType = cr.getStatManager().getSettings().getOutput().getPythonModelType();
			if (pyModelType == PythonModelType.Object) {
				// copy the files from resources
				String[] fileNames = new String[] { "tree_as_object.py", "prediction_aggregators.py" };

				for (String fileName : fileNames) {
					try {
						InputStream is = si.ijs.kt.clus.Clus.class.getResourceAsStream("/" + fileName);
						String fullFileName = getSettings().getGeneric().getFileAbsolute(fileName);
						Files.copy(is, Paths.get(fullFileName), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException ex) {
						System.err.println("Error while copying " + fileName + " to the output folder.");
						ex.printStackTrace();
					}

				}
			}
			String pyName = appName + "_models.py";
			File pyscript = new File(getSettings().getGeneric().getFileAbsolute(pyName));
			PrintWriter wrtr = new PrintWriter(new FileOutputStream(pyscript));
			if (getSettings().getEnsemble().isEnsembleMode()) {
				// If in ensemble mode, we create two files:
				// - <app name>_models.py with ensemble methods which use the imported methods
				// from
				// - <app name>_trees.py where the trees are defined
				String treeFile = ClusForest.getTreeFile(appName);
				// write ensemble file
				ClusForest.writePythonAggregation(wrtr, treeFile, cr.getStatManager().getTargetType());
				int maxSize = -1;
				int maxSizedForest = -1;
				for (int i = 0; i < cr.getNbModels(); i++) {
					ClusModel root = models.get(i);
					if (root instanceof ClusForest) {
						int trees = ((ClusForest) root).getNbModels();
						if (trees > maxSize) {
							maxSize = trees;
							maxSizedForest = i;
						}
						((ClusForest) root).writePythonEnsembleFile(wrtr, treeFile, pyModelType);
					}
				}
				ClusLogger.info(String.format("Python ensemble aggregation code written to: %s", pyName));
				// print only max sized forest trees, since it contains the other forests
				ClusForest root = (ClusForest) models.get(maxSizedForest);
				if (getSettings().getEnsemble().shouldOptimizeEnsemble()) {
					root.joinPythonForestInOneFile(cr);
				} else {
					root.printForestToPython(pyModelType);
				}
			} else {
				// Otherwise, we create only the <app name>_models.py file
				// Tested for clus.jar something.s case
				if (pyModelType == PythonModelType.Object) {
					wrtr.println("from tree_as_object import *\n\n");
				}
				HashMap<Integer, String> pythonNames = new HashMap<Integer, String>();
				pythonNames.put(ClusModel.DEFAULT, "default");
				pythonNames.put(ClusModel.ORIGINAL, "original");
				pythonNames.put(ClusModel.PRUNED, "pruned");
				String defPattern = "def %s(xs):";
				HashMap<String, Integer> descrIndices = ClusUtil.getDescriptiveAttributesIndices(cr.getStatManager());
				for (int i = 0; i < cr.getNbModels(); i++) {
					ClusModel root = models.get(i);
					if (pythonNames.containsKey(i)) {
						switch (pyModelType) {
						case Function:
							wrtr.println(String.format(defPattern, pythonNames.get(i)));
							root.printModelToPythonScript(wrtr, descrIndices);
							break;
						case Object:
							root.printModelToPythonScript(wrtr, descrIndices, pythonNames.get(i));
							break;
						default:
							throw new RuntimeException("Wrong PythonModelType: "
							        + cr.getStatManager().getSettings().getOutput().getPythonModelType());
						}
					} else {
						System.err.println(
						        "Warning: this is not DEFAULT/ORIGINAL/PRUNED model and will not be printed. Extend the pythonNames hash map!");
					}
				}
			}
			wrtr.close();
		}

		if (!ClusOOBErrorEstimate.isOOBCalculation() && getSettings().getOutput().isOutputDatabaseQueries()) {
			int starttree = getSettings().getExhaustiveSearch().getStartTreeCpt();
			int startitem = getSettings().getExhaustiveSearch().getStartItemCpt();
			ClusModel root = models.get(cr.getNbModels() - 1);
			// use the following lines for creating a SQL file that will put the tree into a
			// database
			String out_database_name = m_Sett2.getGeneric().getAppName() + ".txt";
			PrintWriter database_writer = m_Sett2.getGeneric().getFileAbsoluteWriter(out_database_name);
			root.printModelToQuery(database_writer, cr, starttree, startitem,
			        getSettings().getExhaustiveSearch().isExhaustiveSearch());
			database_writer.close();
			ClusLogger.info("The queries are in " + out_database_name);
		}

		if (!ClusOOBErrorEstimate.isOOBCalculation() && getSettings().getOutput().isOutputClowdFlowsJSON()) {
			JsonObject output = new JsonObject();
			StringWriter settingsStringWriter = new StringWriter();
			PrintWriter settingsWriter = new PrintWriter(settingsStringWriter);
			m_Sett.show(settingsWriter);
			output.addProperty("settings", settingsStringWriter.toString());
			JsonArray outputModels = new JsonArray();
			output.add("models", outputModels);
			for (int i = 0; i < cr.getNbModels(); i++) {
				if (cr.getModelInfo(i) != null && models.get(i) != null && m_Sett.getOutput().shouldShowModel(i)) {
					ClusModelInfo mi = cr.getModelInfo(i);
					ClusModel root = models.get(i);
					String modelname = mi.getName();
					JsonObject currentModel = new JsonObject();
					currentModel.addProperty("name", modelname);
					RowData pex = (RowData) cr.getTrainingSet();
					// ClusLogger.info(te_err);
					if (te_err != null)
						pex = cr.getTestSet();
					currentModel.add("representation", root.getModelJSON(info, pex));
					outputModels.add(currentModel);
				}
			}

			String jsonFileName = m_Sett2.getGeneric().getAppName() + ".json";
			PrintWriter jsonWriter = m_Sett2.getGeneric().getFileAbsoluteWriter(jsonFileName);
			jsonWriter.write(output.toString());
			// System.out.print(output.toString());
			jsonWriter.close();
		}
		m_Writer.flush();
	}

	public String getQuotient(int a, int b) {
		double val = b == 0 ? 0.0 : (double) a / b;
		return ClusFormat.ONE_AFTER_DOT.format(val);
	}

	public void writeSummary(ClusSummary summary) throws IOException {
		m_Writer.println("Summary");
		m_Writer.println("*******");
		m_Writer.println();

		int runs = summary.getNbRuns();
		m_Writer.println("Runs: " + runs);

		double tsec = summary.getInductionTime() / 1000.0;
		m_Writer.println("Induction time: " + ClusFormat.FOUR_AFTER_DOT.format(tsec) + " sec");

		double psec = summary.getPrepareTime() / 1000.0;
		m_Writer.println("Preprocessing time: " + ClusFormat.ONE_AFTER_DOT.format(psec) + " sec");

		long tpred = summary.getPredictionTime();
		long tpredavg = summary.getPredictionTimeAverage();
		m_Writer.println("Prediction Time (total for ClusModel.Original, " + runs + " runs, "
		        + summary.getPredictionTimeNbExamples() + " examples): " + System.lineSeparator() + "\t"
		        + ClusFormat.FOUR_AFTER_DOT.format(tpred / Math.pow(10, 3)) + " microsecs" + System.lineSeparator()
		        + "\t" + ClusFormat.FOUR_AFTER_DOT.format(tpred / Math.pow(10, 6)) + " millisecs"
		        + System.lineSeparator() + "\t" + ClusFormat.FOUR_AFTER_DOT.format(tpred / Math.pow(10, 9)) + " secs");
		m_Writer.println("Prediction Time (average for ClusModel.Original): "
		        + ClusFormat.FOUR_AFTER_DOT.format(tpredavg / Math.pow(10, 3)) + " microsecs");

		m_Writer.println("Mean number of tests");
		for (int i = ClusModel.ORIGINAL; i <= ClusModel.PRUNED; i++) {
			ClusModelInfo mi = summary.getModelInfo(i);
			if (mi != null)
				m_Writer.println("     " + mi.getName() + ": " + getQuotient(mi.getModelSize(), runs));
		}
		m_Writer.println();
		String bName = FileUtil.getName(m_Fname);
		ClusErrorList tr_err = summary.getTrainError();
		if (m_Sett.getOutput().isOutTrainError() && tr_err != null) {
			m_Writer.println("Training error");
			m_Writer.println("--------------");
			m_Writer.println();
			tr_err.showError(summary, ClusModelInfo.TRAIN_ERR, bName + ".train", m_Writer, m_Sett);
			// tr_err.showError(summary, ClusModelInfo.TRAIN_ERR, m_Writer);
			m_Writer.println();
		}
		ClusErrorList va_err = summary.getValidationError();
		if (va_err != null) {
			m_Writer.println("Validation error");
			m_Writer.println("----------------");
			m_Writer.println();
			va_err.showError(summary, ClusModelInfo.VALID_ERR, bName + ".valid", m_Writer, m_Sett);
			// va_err.showError(summary, ClusModelInfo.VALID_ERR, m_Writer);
			m_Writer.println();
		}
		ClusErrorList te_err = summary.getTestError();
		if (te_err != null) {
			m_Writer.println("Testing error");
			m_Writer.println("-------------");
			m_Writer.println();
			te_err.showError(summary, ClusModelInfo.TEST_ERR, bName + ".test", m_Writer, m_Sett);
			// te_err.showError(summary, ClusModelInfo.TEST_ERR, m_Writer);
		}
		m_Writer.println();
		m_Writer.flush();
	}

	public PrintWriter getWriter() {
		return m_Writer;
	}

	public void close() {
		if (m_Fname != null && getSettings().getGeneral().getVerbose() >= 1) {
			ClusLogger.info("Output written to: " + m_Fname);
		}
		m_Writer.close();
	}

}
