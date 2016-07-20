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

package addon.hmc.HMCAverageSingleClass;

/*
 * Created on Dec 22, 2005
 */

import java.util.*;

import java.io.*;

import addon.hmc.HMCAverageSingleClass.HMCAverageTreeModel;

import jeans.io.ini.INIFileNominalOrDoubleOrVector;
import jeans.util.array.*;
import jeans.util.cmdline.*;
import jeans.util.*;

import clus.*;
import clus.algo.tdidt.*;
import clus.data.rows.*;
import clus.data.io.*;
import clus.main.*;
import clus.util.*;
import clus.statistic.*;
import clus.data.type.*;
import clus.model.*;
import clus.model.modelio.*;
import clus.ext.hierarchical.*;
import clus.error.*;

public class HMCAverageSingleClass implements CMDLineArgsProvider {

	private static String[] g_Options = {"models", "hsc", "stats","loadPredictions"};
	private static int[] g_OptionArities = {1, 0, 0, 1};

	protected Clus m_Clus;
	protected StringTable m_Table = new StringTable();

	// added: keeps prediction results for each threshold
	protected ClusErrorList[][] m_EvalArray;
	protected double[][][] m_PredProb;
	protected int m_NbModels;
	protected int m_TotSize;

	public void run(String[] args) throws IOException, ClusException, ClassNotFoundException {
		m_Clus = new Clus();
		Settings sett = m_Clus.getSettings();
		CMDLineArgs cargs = new CMDLineArgs(this);
		cargs.process(args);
		if (cargs.allOK()) {
			sett.setDate(new Date());
			sett.setAppName(cargs.getMainArg(0));
			m_Clus.initSettings(cargs);
			ClusDecisionTree clss = new ClusDecisionTree(m_Clus);
			m_Clus.initialize(cargs, clss);
			ClusStatistic target = createTargetStat();
			target.calcMean();
			if (cargs.hasOption("stats")) {
				computeStats();
				System.exit(0);
			}
			if (cargs.hasOption("models") || cargs.hasOption("hsc")) { 
				if (cargs.hasOption("hsc")) {
					m_Clus.getSettings().setSuffix(".hsc.combined");
				} else {
					m_Clus.getSettings().setSuffix(".sc.combined");
				}
				ClusRun cr = m_Clus.partitionData();
				// Don't want separate prune set now
				cr.combineTrainAndValidSets();
				// Cell with predicted probability for each example in the train and test sets
				ClassHierarchy hier = getStatManager().getHier();
				m_PredProb = new double[2][][];
				for (int i = ClusModelInfoList.TRAINSET; i <= ClusModelInfoList.TESTSET; i++) {
					int size = cr.getDataSet(i).getNbRows();
					m_PredProb[i] = new double[size][hier.getTotal()];
					for (int k = 0; k < size; k++) {
						Arrays.fill(m_PredProb[i][k], Double.MAX_VALUE);
					}
				}
				// Array with error measures for each threshold
				INIFileNominalOrDoubleOrVector class_thr = getSettings().getClassificationThresholds();
				if (class_thr.isVector()) {
					HierClassTresholdPruner pruner = (HierClassTresholdPruner)getStatManager().getTreePruner(null);
					m_EvalArray = new ClusErrorList[2][pruner.getNbResults()];
					for (int i = 0; i < pruner.getNbResults(); i++) {
						for (int j = ClusModelInfoList.TRAINSET; j <= ClusModelInfoList.TESTSET; j++) {
							m_EvalArray[j][i] = new ClusErrorList();
							m_EvalArray[j][i].addError(new HierClassWiseAccuracy(m_EvalArray[j][i], hier));
							m_EvalArray[j][i].addError(null);
						}
					}
				}
				// Load models and update statistics
				if (cargs.hasOption("hsc")) {
					HMCAverageNodeWiseModels avg = new HMCAverageNodeWiseModels(this, m_PredProb);
					avg.processModels(cr);
					m_NbModels = avg.getNbModels();
					m_TotSize = avg.getTotalSize();
					if (m_EvalArray != null) avg.updateErrorMeasures(cr);
				} else {
					loadModelPerModel(cargs.getOptionValue("models"), cr);
				}
				// Write output
				ClusOutput output = new ClusOutput(sett.getAppNameWithSuffix() + ".out", m_Clus.getSchema(), sett);
				// Create default model
				ClusModelInfo def_model = cr.addModelInfo(ClusModel.DEFAULT);
				def_model.setModel(ClusDecisionTree.induceDefault(cr));
				// Create original model
				ClusModelInfo orig_model_inf = cr.addModelInfo(ClusModel.ORIGINAL);
				HMCAverageTreeModel orig_model = new HMCAverageTreeModel(target, m_PredProb, m_NbModels, m_TotSize);
				orig_model_inf.setModel(orig_model);
				// Calculate error measures
				cr.copyAllModelsMIs();
				RowData train = (RowData)cr.getTrainingSet();
				train.addIndices();
				orig_model.setDataSet(ClusModelInfoList.TRAINSET);
				m_Clus.calcError(train.getIterator(), ClusModelInfo.TRAIN_ERR, cr);
				RowData test = (RowData)cr.getTestSet();
				if (test != null) {
					test.addIndices();
					orig_model.setDataSet(ClusModelInfoList.TESTSET);
					m_Clus.calcError(test.getIterator(), ClusModelInfo.TEST_ERR, cr);
				}
				// Add model for each threshold to clusrun
				if (class_thr.isVector()) {
					HierClassTresholdPruner pruner = (HierClassTresholdPruner)getStatManager().getTreePruner(null);
					for (int i = 0; i < pruner.getNbResults(); i++) {
						ClusModelInfo pruned_info = cr.addModelInfo(pruner.getPrunedName(i));
						// pruned_info.setModel(new ClusNode());
						pruned_info.setShouldWritePredictions(false);
						pruned_info.setTrainError(m_EvalArray[ClusModelInfoList.TRAINSET][i]);
						pruned_info.setTestError(m_EvalArray[ClusModelInfoList.TESTSET][i]);
					}
				}
				output.writeHeader();
				output.writeOutput(cr, true, getSettings().isOutTrainError());
				output.close();
			}
			else if (cargs.hasOption("loadPredictions")){ //coded by Leander 13/5/2009
		      //initializations
			  m_Clus.getSettings().setSuffix(".evaluatedPredictions");
			  ClusRun cr = m_Clus.partitionData();
		      // Don't want separate prune set now
		      //cr.combineTrainAndValidSets(); //not necessary
			  // Cell with predicted probability for each example in the train and test sets
			  ClassHierarchy hier = getStatManager().getHier();
			  int size = cr.getDataSet(ClusModelInfoList.TESTSET).getNbRows();
			  //only for testset necessary!
			  m_PredProb = new double[1][size][hier.getTotal()];

		      //read predictions in from a file
		      String file = cargs.getOptionValue("loadPredictions");
			  RowData rw = ARFFFile.readArff(file);
			  ClusSchema schema = rw.getSchema();
			  NumericAttrType[] na = schema.getNumericAttrUse(0); //0 = alle attributen
		   
			  // make mapping between classes
			  int[] mapping_classes = new int[schema.getNbAttributes()];
			  for (int y=0;y<na.length;y++) {
				String label = na[y].getName();
				//System.out.println("Label: "+label);
				boolean found = false;
			    for (int a=0;a<hier.getTotal();a++) {
			      if (hier.getTermAt(a).toStringHuman(hier).equals(label)) {
			        mapping_classes[y]=a;
			        found = true;
			      }
			    }
			    if (!found) {
			      throw new ClusException("Error: class "+label+" not found.");
			    }
			  }
			  
			  RowData testset = cr.getDataSet(ClusModelInfoList.TESTSET);
			  
			  /*//make mapping between examples
			  int[] mapping_examples = new int[rw.getNbRows()];
			  StringAttrType ex_key = (StringAttrType)rw.m_Schema.getAttrType(0);
			  for (int x=0;x<rw.getNbRows();x++) {
				  DataTuple tuple = rw.getTuple(x);
				  boolean found = false;
				  for (int b=0;b<testset.getNbRows();b++) {
					  DataTuple tuple2 = testset.getTuple(b);
					  if (ex_key.getString(tuple).equals(ex_key.getString(tuple2))) {
					    mapping_examples[x]=b;
					    found = true;
					  }
				  }
				  if (!found) {
				      throw new ClusException("Error: example "+ex_key.getString(tuple)+" not found.");
				    }
			  }*/
			  
			  //check			  
			  System.out.println("Number of rows in predictions-file: "+rw.getNbRows());
			  System.out.println("Number of rows in test-file: "+testset.getNbRows());
			  
			  //m_NbModels = 1;
			  System.out.println("Number of classes: "+hier.getTotal());
			  
			  //store predictions in predProb-matrix
			  //ClusAttrType[] keys = schema.getAllAttrUse(ClusAttrType.ATTR_USE_KEY);
			  /* weggedaan 25/2 
			  StringAttrType key = (StringAttrType)schema.getAttrType(0);
			  */
			  //System.out.println("Lengte van key array: "+keys.length);
			  //StringAttrType key = (StringAttrType)keys[0];
			  for (int x=0;x<rw.getNbRows();x++) {
				//for (int y=0;y<schema.getNbAttributes();y++) {
			    DataTuple tuple = rw.getTuple(x);
			    /*Leander 25/2/2011 int z = mapping_examples[x];
			    DataTuple tuple_test = testset.getTuple(z);*/
			    DataTuple tuple_test = testset.getTuple(x);
			    //System.out.println("Tuple: "+tuple_test.toString());
			    /*if (!key.getString(tuple).equals(key.getString(tuple_test))) {
			    	throw new ClusException("Key attributes do not match: "+key.getString(tuple)+" <> "+key.getString(tuple_test)+" at line "+x);
			    } weggedaan 25/2*/
				for (int y=0;y<na.length;y++) {
			      int a = mapping_classes[y];
			      //System.out.println("Storing "+na[y].getNumeric(tuple)+" in example "+z+" for class "+a);
			      /*m_PredProb[0][z][a] = na[y].getNumeric(tuple); //na[0]: eerste attribuut (klasse);*/
			      m_PredProb[0][x][a] = na[y].getNumeric(tuple);
			    }
			  }
			  //m_predProb: datasets i, example j, klasse k (dataset moet enkel testset zijn)
			  
			  
    		// Array with error measures for each threshold
	 			INIFileNominalOrDoubleOrVector class_thr = getSettings().getClassificationThresholds();
				//System.out.println("Bool is "+class_thr.isVector());
				if (class_thr.isVector()) {
					HierClassTresholdPruner pruner = (HierClassTresholdPruner)getStatManager().getTreePruner(null);
					m_EvalArray = new ClusErrorList[2][pruner.getNbResults()];
					for (int i = 0; i < pruner.getNbResults(); i++) {
						//for (int j = ClusModelInfoList.TRAINSET; j <= ClusModelInfoList.TESTSET; j++) {
							m_EvalArray[ClusModelInfoList.TESTSET][i] = new ClusErrorList();
							m_EvalArray[ClusModelInfoList.TESTSET][i].addError(new HierClassWiseAccuracy(m_EvalArray[ClusModelInfoList.TESTSET][i], hier));
							m_EvalArray[ClusModelInfoList.TESTSET][i].addError(null);  //why is this necessary again?
							//System.out.println("Evalarray: "+m_EvalArray[ClusModelInfoList.TESTSET][i].getNbTotal());
						//}
					}
				}
			  
//			 Write output
				ClusOutput output = new ClusOutput(sett.getAppNameWithSuffix() + ".out", m_Clus.getSchema(), sett);
				// Create default model
				ClusModelInfo def_model = cr.addModelInfo(ClusModel.DEFAULT);
				def_model.setModel(ClusDecisionTree.induceDefault(cr));
				// Create original model
				ClusModelInfo orig_model_inf = cr.addModelInfo(ClusModel.ORIGINAL);
				//System.out.println("Number of models: "+m_NbModels);
				HMCAverageTreeModel orig_model = new HMCAverageTreeModel(target, m_PredProb, m_NbModels, m_TotSize);
				orig_model_inf.setModel(orig_model);
				// Calculate error measures
				cr.copyAllModelsMIs();
				//RowData train = (RowData)cr.getTrainingSet();
				//train.addIndices();
				//orig_model.setDataSet(ClusModelInfoList.TRAINSET);
				//m_Clus.calcError(train.getIterator(), ClusModelInfo.TRAIN_ERR, cr);
				RowData test = (RowData)cr.getTestSet();
				if (test != null) {
					test.addIndices();
					//orig_model.setDataSet(ClusModelInfoList.TESTSET);
					m_Clus.calcError(test.getIterator(), ClusModelInfo.TEST_ERR, cr);					
				}
				// Add model for each threshold to clusrun
				if (class_thr.isVector()) {
					HierClassTresholdPruner pruner = (HierClassTresholdPruner)getStatManager().getTreePruner(null);
					for (int i = 0; i < pruner.getNbResults(); i++) {
						ClusModelInfo pruned_info = cr.addModelInfo(pruner.getPrunedName(i));
						// pruned_info.setModel(new ClusNode());
						pruned_info.setShouldWritePredictions(false);
						//pruned_info.setTrainError(m_EvalArray[ClusModelInfoList.TRAINSET][i]);
						pruned_info.setTestError(m_EvalArray[ClusModelInfoList.TESTSET][i]);
						//System.out.println("Evalarray: "+m_EvalArray[ClusModelInfoList.TESTSET][i].getNbExamples());
					}
				}
				output.writeHeader();
				output.writeOutput(cr, true, getSettings().isOutTrainError());
				output.close();
			}
			else {
				throw new ClusException("Must specify e.g., -models dirname");
			}
		}
	}

	public ClusStatManager getStatManager() {
		return m_Clus.getStatManager();
	}

	public Settings getSettings() {
		return m_Clus.getSettings();
	}

	public Clus getClus() {
		return m_Clus;
	}

	public ClusErrorList getEvalArray(int traintest, int j) {
		return m_EvalArray[traintest][j];
	}

	public WHTDStatistic createTargetStat() {
		return (WHTDStatistic)m_Clus.getStatManager().createStatistic(ClusAttrType.ATTR_USE_TARGET);
	}

	public String getClassStr(String file) {
		StringBuffer result = new StringBuffer();
		String value = FileUtil.getName(FileUtil.removePath(file));
		String[] cmps = value.split("_");
		String[] elems = cmps[cmps.length-1].split("-");
		for (int i = 0; i < elems.length; i++) {
			if (i != 0) result.append("/");
			result.append(elems[i]);
		}
		return result.toString();
	}

	public int getClassIndex(String file) throws ClusException {
		String class_str = getClassStr(file);
		ClassHierarchy hier = getStatManager().getHier();
		ClassesValue val = new ClassesValue(class_str, hier.getType().getTable());
		return hier.getClassTerm(val).getIndex();
	}

	public ClusModel loadModel(String file) throws IOException, ClusException, ClassNotFoundException {
		String class_str = getClassStr(file);
		System.out.println("Loading: "+file+" class: "+class_str);
		ClusModelCollectionIO io = ClusModelCollectionIO.load(file);
		ClusModel sub_model = io.getModel("Original");
		if (sub_model == null) {
			throw new ClusException("Error: .model file does not contain model named 'Original'");
		}
		m_NbModels++;
		m_TotSize += sub_model.getModelSize();
		return sub_model;
	}

	public void loadModelPerModel(String dir, ClusRun cr) throws IOException, ClusException, ClassNotFoundException {
		String[] files = FileUtil.dirList(dir, "model");
		for (int i = 0; i < files.length; i++) {
			ClusModel model = loadModel(FileUtil.cmbPath(dir, files[i]));
			int class_idx = getClassIndex(files[i]);
			for (int j = ClusModelInfoList.TRAINSET; j <= ClusModelInfoList.TESTSET; j++) {
				evaluateModelAndUpdateErrors(j, class_idx, model, cr);
			}
		}
		INIFileNominalOrDoubleOrVector class_thr = getSettings().getClassificationThresholds();
		if (class_thr.isVector()) {
			HierClassTresholdPruner pruner = (HierClassTresholdPruner)getStatManager().getTreePruner(null);
			for (int j = 0; j < pruner.getNbResults(); j++) {
				for (int traintest = ClusModelInfoList.TRAINSET; traintest <= ClusModelInfoList.TESTSET; traintest++) {
					RowData data = cr.getDataSet(traintest);
					ClusErrorList error = getEvalArray(traintest, j);
					error.setNbExamples(data.getNbRows(), data.getNbRows());
				}
			}
		}
	}

	// evaluate tree for one class on all examples and update errors
	public void evaluateModelAndUpdateErrors(int train_or_test, int class_idx, ClusModel model, ClusRun cr) throws ClusException, IOException {
		RowData data = cr.getDataSet(train_or_test);
		m_Clus.getSchema().attachModel(model);
		INIFileNominalOrDoubleOrVector class_thr = getSettings().getClassificationThresholds();
		if (class_thr.isVector()) {
			HierClassTresholdPruner pruner = (HierClassTresholdPruner)getStatManager().getTreePruner(null);
			for (int i = 0; i < data.getNbRows(); i++) {
				DataTuple tuple = data.getTuple(i);
				ClusStatistic prediction = model.predictWeighted(tuple);
				double[] predicted_distr = prediction.getNumericPred();
				ClassesTuple tp = (ClassesTuple)tuple.getObjVal(0);
				boolean actually_has_class = tp.hasClass(class_idx);
				for (int j = 0; j < pruner.getNbResults(); j++) {
					// update corresponding hierclasswiseacc
					boolean predicted_class = predicted_distr[0] >= pruner.getThreshold(j)/100.0;
					HierClassWiseAccuracy acc = (HierClassWiseAccuracy)m_EvalArray[train_or_test][j].getError(0);
					acc.nextPrediction(class_idx, predicted_class, actually_has_class);
				}
			}
		}
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			ClusStatistic prediction = model.predictWeighted(tuple);
			double[] predicted_distr = prediction.getNumericPred();
			m_PredProb[train_or_test][i][class_idx] = predicted_distr[0];
		}
	}

	public String[] getOptionArgs() {
		return g_Options;
	}

	public int[] getOptionArgArities() {
		return g_OptionArities;
	}

	public int getNbMainArgs() {
		return 1;
	}

	public void showHelp() {
	}

	public void countClasses(RowData data, double[] counts) {
		ClassHierarchy hier = getStatManager().getHier();
		int sidx = hier.getType().getArrayIndex();
		boolean[] arr = new boolean[hier.getTotal()];
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			ClassesTuple tp = (ClassesTuple)tuple.getObjVal(sidx);
			// count with parents
			Arrays.fill(arr, false);
			tp.fillBoolArrayNodeAndAncestors(arr);
			for (int j = 0; j < arr.length; j++) {
				if (arr[j]) counts[0] += 1.0;
			}
			// count without parents
			hier.removeParentNodes(arr);
			for (int j = 0; j < arr.length; j++) {
				if (arr[j]) counts[1] += 1.0;
			}
		}
	}

	public void computeStats() throws ClusException, IOException {
		ClusRun cr = m_Clus.partitionData();
		RegressionStat stat = (RegressionStat)getStatManager().createStatistic(ClusAttrType.ATTR_USE_TARGET);
		RowData train = (RowData)cr.getTrainingSet();
		RowData valid = (RowData)cr.getPruneSet();
		RowData test = (RowData)cr.getTestSet();
		train.calcTotalStat(stat);
		if (valid != null) valid.calcTotalStat(stat);
		if (test != null) test.calcTotalStat(stat);
		stat.calcMean();
		ClassHierarchy hier = getStatManager().getHier();
		PrintWriter wrt = getSettings().getFileAbsoluteWriter(getSettings().getAppName() + "-hmcstat.arff");
		ClusSchema schema = new ClusSchema("HMC-Statistics");
		schema.addAttrType(new StringAttrType("Class"));
		schema.addAttrType(new NumericAttrType("Weight"));
		schema.addAttrType(new NumericAttrType("MinDepth"));
		schema.addAttrType(new NumericAttrType("MaxDepth"));
		schema.addAttrType(new NumericAttrType("Frequency"));
		double total = stat.getTotalWeight();
		double[] classCounts = new double[2];
		countClasses(train, classCounts);
		if (valid != null) countClasses(valid, classCounts);
		if (test != null) countClasses(test, classCounts);
		int nbDescriptiveAttrs = m_Clus.getSchema().getNbDescriptiveAttributes();
		wrt.println();
		wrt.println("% Number of examples: "+total);
		wrt.println("% Number of descriptive attributes: "+nbDescriptiveAttrs);
		wrt.println("% Number of classes: "+hier.getTotal());
		wrt.println("% Avg number of labels/example: "+ (classCounts[0]/total)+" (most specific: "+(classCounts[1]/total)+")");
		wrt.println("% Hierarchy depth: "+hier.getDepth());
		wrt.println();
		ARFFFile.writeArffHeader(wrt, schema);
		wrt.println("@DATA");
		for (int i = 0; i < hier.getTotal(); i++) {
			ClassTerm term = hier.getTermAt(i);
			int index = term.getIndex();
			wrt.print(term.toStringHuman(hier));
			wrt.print(","+hier.getWeight(index));
			wrt.print(","+term.getMinDepth());
			wrt.print(","+term.getMaxDepth());
			wrt.print(","+stat.getSumValues(index));
			wrt.println();
		}
		wrt.close();
	}

	public static void main(String[] args) {
		try {
			HMCAverageSingleClass avg = new HMCAverageSingleClass();
			avg.run(args);
		} catch (IOException io) {
			System.out.println("IO Error: "+io.getMessage());
		} catch (ClusException cl) {
			System.out.println("Error: "+cl.getMessage());
		} catch (ClassNotFoundException cn) {
			System.out.println("Error: "+cn.getMessage());
		}
	}
}
