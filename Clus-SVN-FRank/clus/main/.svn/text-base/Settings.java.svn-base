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

package clus.main;

import jeans.io.ini.*;
import jeans.io.range.IntRangeCheck;
import jeans.util.cmdline.*;
import jeans.util.*;
import jeans.resource.*;

import java.io.*;
import java.util.*;

import clus.statistic.*;
import clus.heuristic.*;
import clus.model.ClusModel;
import clus.data.type.*;
import clus.ext.hierarchical.*;

/**
 * All the settings. Includes the command line parameters as boolean class attributes.
 * The settings file attributes are included by get* methods.
 * @author User
 *
 */
public class Settings implements Serializable {

	public final static long SERIAL_VERSION_ID = 1L;
	public final static long serialVersionUID = SERIAL_VERSION_ID;


/***********************************************************************
 * INI file structure                                                  *
 ***********************************************************************/

	protected INIFile m_Ini = new INIFile();

/***********************************************************************
 * Simple constants                                                    *
 ***********************************************************************/

	public final static String DEFAULT = "Default";
	public final static String NONE = "None";
	public final static String[] NONELIST = { "None" };
	public final static String[] EMPTY = {};
	public final static String[] INFINITY = { "Infinity" };
	public final static String INFINITY_STRING = "Infinity";
	public final static int INFINITY_VALUE = 0;
	public final static double[] FOUR_ONES = { 1.0, 1.0, 1.0, 1.0 };

/***********************************************************************
 * Generic information                                                 *
 ***********************************************************************/

	protected Date m_Date;
	protected String m_AppName;
	protected String m_DirName;
	protected String m_Suffix = "";

	public static int VERBOSE = 1;
	public static boolean EXACT_TIME = false;

	public Date getDate() {
		return m_Date;
	}

	public void setDate(Date date) {
		m_Date = date;
	}

	public String getAppName() {
		return m_AppName;
	}

	public String getAppNameWithSuffix() {
		return m_AppName + m_Suffix;
	}

	public void setSuffix(String suffix) {
		m_Suffix = suffix;
	}

	public void setAppName(String file) {
		file = StringUtils.removeSuffix(file, ".gz");
		file = StringUtils.removeSuffix(file, ".arff");
		file = StringUtils.removeSuffix(file, ".s");
		file = StringUtils.removeSuffix(file, ".");
		m_AppName = FileUtil.removePath(file);
		m_DirName = FileUtil.getPath(file);
	}

/***********************************************************************
 * Section: General                                                    *
 ***********************************************************************/

	protected INIFileInt m_Verbose;
	protected INIFileNominal m_Compatibility;
	protected INIFileString m_RandomSeed;
	protected INIFileNominal m_ResourceInfoLoaded;

	public static int enableVerbose(int talk) {
		int prev = VERBOSE;
		VERBOSE = talk;
		return prev;
	}

	public int getCompatibility() {
		return m_Compatibility.getValue();
	}

	public boolean hasRandomSeed() {
		//System.out.println(m_RandomSeed.getValue());
		return !StringUtils.unCaseCompare(m_RandomSeed.getValue(), NONE);
	}

	public int getRandomSeed() {
		return Integer.parseInt(m_RandomSeed.getValue());
	}

	public int getResourceInfoLoaded() {
		return m_ResourceInfoLoaded.getValue();
	}

/***********************************************************************
 * Section: General - Compatibility mode                               *
 ***********************************************************************/

	public final static String[] COMPATIBILITY = { "CMB05", "MLJ08", "Latest"};

	public final static int COMPATIBILITY_CMB05 = 0;
	public final static int COMPATIBILITY_MLJ08 = 1;
	public final static int COMPATIBILITY_LATEST = 2;

/***********************************************************************
 * Section: General - ResourceInfo loaded                              *
 ***********************************************************************/

	public final static String[] RESOURCE_INFO_LOAD = {"Yes", "No", "Test"};

	public final static int RESOURCE_INFO_LOAD_YES = 0;
	public final static int RESOURCE_INFO_LOAD_NO = 1;
	public final static int RESOURCE_INFO_LOAD_TEST = 2;

/***********************************************************************
 * Section: Data                                                       *
 ***********************************************************************/

	protected INIFileString m_DataFile;
	protected INIFileStringOrDouble m_TestSet;
	protected INIFileStringOrDouble m_PruneSet;
	protected INIFileStringOrInt m_PruneSetMax;
	/** How many folds are we having in xval OR gives a file that defines the used folds (in the data set)*/
	protected INIFileStringOrInt m_XValFolds;
	protected INIFileBool m_RemoveMissingTarget;

	// Gradient descent optimization algorithm
	/** Possible values for normalizeData */
	public final static String[] NORMALIZE_DATA_VALUES = {"None", "Numeric"};
	/** Do not normalize data. DEFAULT */
	public final static int NORMALIZE_DATA_NONE = 0;
	/** Normalize only numeric variables */
	public final static int NORMALIZE_DATA_NUMERIC = 1;
	/** Normalize all the variables Not implemented*/
//	public final static int NORMALIZE_DATA_ALL = 3;
	/** Do normalization for the data in the beginning. Done by dividing with the variance. */
	protected INIFileNominal m_NormalizeData;

	public String getDataFile() {
		return m_DataFile.getValue();
	}

	public boolean isNullFile() {
		return StringUtils.unCaseCompare(m_DataFile.getValue(), NONE);
	}

	public void updateDataFile(String fname) {
		if (isNullFile()) m_DataFile.setValue(fname);
	}

	public String getTestFile() {
		return m_TestSet.getValue();
	}

	public boolean isNullTestFile() {
		return m_TestSet.isDoubleOrNull(NONE);
	}

	public String getPruneFile() {
		return m_PruneSet.getValue();
	}

	public boolean isNullPruneFile() {
		return m_PruneSet.isDoubleOrNull(NONE);
	}

	public double getTestProportion() {
		if (!m_TestSet.isDouble()) return 0.0;
		return m_TestSet.getDoubleValue();
	}

	public double getPruneProportion() {
		if (!m_PruneSet.isDouble())	return 0.0;
		return m_PruneSet.getDoubleValue();
	}

	public int getPruneSetMax() {
		if (m_PruneSetMax.isString(INFINITY_STRING)) return Integer.MAX_VALUE;
		else return m_PruneSetMax.getIntValue();
	}

	public boolean isNullXValFile() {
		return m_XValFolds.isIntOrNull(NONE);
	}

	public boolean isLOOXVal() {
		return m_XValFolds.isString("LOO");
	}

	public String getXValFile() {
		return m_XValFolds.getValue();
	}

	public int getXValFolds() {
		return m_XValFolds.getIntValue();
	}

	public void setXValFolds(int folds) {
		m_XValFolds.setIntValue(folds);
	}

	public boolean isRemoveMissingTarget() {
		return m_RemoveMissingTarget.getValue();
	}

	/** Do we want to normalize the data */
	public int getNormalizeData() {
		return m_NormalizeData.getValue();
	}

/***********************************************************************
 * Section: Attribute                                                  *
 ***********************************************************************/

	protected INIFileString m_Target;
	protected INIFileString m_Clustering;
	protected INIFileString m_Descriptive;
	protected INIFileString m_Key;
	protected INIFileString m_Disabled;
	protected INIFileNominalOrDoubleOrVector m_Weights;
	protected INIFileNominalOrDoubleOrVector m_ClusteringWeights;
	protected INIFileBool m_ReduceMemoryNominal;

	public String getTarget() {
		return m_Target.getValue();
	}

	public void setTarget(String str) {
		m_Target.setValue(str);
	}

	public boolean isNullTarget() {
		return StringUtils.unCaseCompare(m_Target.getValue(), NONE);
	}

	public boolean isDefaultTarget() {
		return StringUtils.unCaseCompare(m_Target.getValue(), DEFAULT);
	}

	public String getClustering() {
		return m_Clustering.getValue();
	}

	public void setClustering(String str) {
		m_Clustering.setValue(str);
	}

	public String getDescriptive() {
		return m_Descriptive.getValue();
	}

	public void setDescriptive(String str) {
		m_Descriptive.setValue(str);
	}

	public String getKey() {
		return m_Key.getValue();
	}

	public String getDisabled() {
		return m_Disabled.getValue();
	}

	public void setDisabled(String str) {
		m_Disabled.setValue(str);
	}

	public INIFileNominalOrDoubleOrVector getNormalizationWeights() {
		return m_Weights;
	}

	public boolean hasNonTrivialWeights() {
		for (int i = 0; i < m_Weights.getVectorLength(); i++) {
			if (m_Weights.isNominal(i))
				return true;
			else if (m_Weights.getDouble(i) != 1.0)
				return true;
		}
		return false;
	}

	public INIFileNominalOrDoubleOrVector getClusteringWeights() {
		return m_ClusteringWeights;
	}

	public boolean getReduceMemoryNominalAttrs() {
		return m_ReduceMemoryNominal.getValue();
	}

/***********************************************************************
 * Section: Selective inductive transfer                               *
 ***********************************************************************/
	protected INIFileSection m_SectionSIT;
	protected INIFileString m_MainTarget;
	protected INIFileString m_Search;
	protected INIFileString m_Learner;
	protected INIFileBool m_Recursive;
	protected INIFileString m_Error;

	public String getError(){
		return m_Error.getValue();
	}

	public String getLearnerName(){
		return m_Learner.getValue();
	}

	//@deprecated
	public boolean getRecursive(){
		return m_Recursive.getValue();
	}

	public String getMainTarget() {
		return m_MainTarget.getValue();
	}

	public String getSearchName() {
		return m_Search.getValue();
	}

	public void setSearch(String b) {
		m_Search.setValue(b);
	}

	public void setMainTarget(String str) {
		m_MainTarget.setValue(str);
	}
	
	public void setSectionSITEnabled(boolean enable) {
		m_SectionSIT.setEnabled(enable);
	}

/***********************************************************************
 * Section: Attribute - Normalization                                  *
 ***********************************************************************/

	public final static String[] NORMALIZATIONS = { "Normalize" };
	public final static int NORMALIZATION_DEFAULT = 0;

/***********************************************************************
 * Section: Attribute - Target weights                                 *
 ***********************************************************************/

	public final static String[] NUM_NOM_TAR_NTAR_WEIGHTS = { "TargetWeight",
		"NonTargetWeight", "NumericWeight", "NominalWeight" };

	public final static int TARGET_WEIGHT = 0;
	public final static int NON_TARGET_WEIGHT = 1;
	public final static int NUMERIC_WEIGHT = 2;
	public final static int NOMINAL_WEIGHT = 3;

/***********************************************************************
 * Section: Nominal - Should move?                                     *
 ***********************************************************************/

	protected INIFileDouble m_MEstimate;

	public double getMEstimate() {
		return m_MEstimate.getValue();
	}

/***********************************************************************
 * Section: Model                                                      *
 ***********************************************************************/

	protected INIFileDouble m_MinW;
	protected INIFileDouble m_MinKnownW;
	protected INIFileInt m_MinNbEx;
	protected INIFileString m_TuneFolds;
	protected INIFileNominalOrDoubleOrVector m_ClassWeight;
	protected INIFileBool m_NominalSubsetTests;

	public double getMinimalWeight() {
		return m_MinW.getValue();
	}
	
	public double getMinimalKnownWeight() {
		return m_MinKnownW.getValue();
	}

	public int getMinimalNbExamples() {
		return m_MinNbEx.getValue();
	}

	public void setMinimalWeight(double val) {
		m_MinW.setValue(val);
	}

	public String getTuneFolds() {
		return m_TuneFolds.getValue();
	}

	public double[] getClassWeight() {
		return m_ClassWeight.getDoubleVector();
	}

	public boolean isNominalSubsetTests() {
		return m_NominalSubsetTests.getValue();
	}

/***********************************************************************
 * Section: Constraints                                                *
 ***********************************************************************/

	protected INIFileString m_SyntacticConstrFile;
	protected INIFileNominalOrIntOrVector m_MaxSizeConstr;
	protected INIFileNominalOrDoubleOrVector m_MaxErrorConstr;

	public boolean hasConstraintFile() {
		return !StringUtils.unCaseCompare(m_SyntacticConstrFile.getValue(),	NONE);
	}

	public String getConstraintFile() {
		return m_SyntacticConstrFile.getValue();
	}

	public int getMaxSize() {
		return getSizeConstraintPruning(0);
	}

	public int getSizeConstraintPruning(int idx) {
		if (m_MaxSizeConstr.isNominal(idx)) {
			return -1;
		} else {
			return m_MaxSizeConstr.getInt(idx);
		}
	}

	public int getSizeConstraintPruningNumber() {
		int len = m_MaxSizeConstr.getVectorLength();
		if (len == 1 && m_MaxSizeConstr.getNominal() == INFINITY_VALUE)	return 0;
		else return len;
	}

	public int[] getSizeConstraintPruningVector() {
		int size_nb = getSizeConstraintPruningNumber();
		int[] sizes = new int[size_nb];
		for (int i = 0; i < size_nb; i++) {
			sizes[i] = getSizeConstraintPruning(i);
		}
		return sizes;
	}

	public void setSizeConstraintPruning(int size) {
		m_MaxSizeConstr.setInt(size);
	}

	public double getMaxErrorConstraint(int idx) {
		if (m_MaxErrorConstr.isNominal(idx)) {
			return Double.POSITIVE_INFINITY;
		} else {
			return m_MaxErrorConstr.getDouble(idx);
		}
	}

	public int getMaxErrorConstraintNumber() {
		int len = m_MaxErrorConstr.getVectorLength();
		if (len == 1 && m_MaxErrorConstr.getDouble(0) == 0.0) return 0;
		else return len;
	}

	public double[] getMaxErrorConstraintVector() {
		int error_nb = getMaxErrorConstraintNumber();
		double[] max_error = new double[error_nb];
		for (int i = 0; i < error_nb; i++) {
			max_error[i] = getMaxErrorConstraint(i);
		}
		return max_error;
	}

/***********************************************************************
 * Section: Output                                                     *
 ***********************************************************************/

	protected INIFileInt m_SetsData;
	protected INIFileBool m_OutFoldErr;
	/** Print out data to .arff files for each fold, m_WritePredictions has to be given value to this to work */
	protected INIFileBool m_OutFoldData;
	protected INIFileBool m_OutFoldModels;
	protected INIFileBool m_OutTrainErr;
	protected INIFileBool m_OutValidErr;
	protected INIFileBool m_OutTestErr;
	protected INIFileBool m_ShowForest;
	protected INIFileBool m_ShowBrFreq;
	protected INIFileBool m_ShowUnknown;
	protected INIFileNominal m_ShowInfo;
	protected INIFileNominal m_ShowModels;
	protected INIFileBool m_PrintModelAndExamples;
	/** Write test/train predictions to files */
	protected INIFileNominal m_WritePredictions;
	protected INIFileBool m_WriteErrorFile;
	protected INIFileBool m_ModelIDFiles;
	protected INIFileBool m_OutputPythonModel;
	protected INIFileBool m_OutputDatabaseQueries;
	protected INIFileBool m_WriteCurves;	

	public boolean isOutTrainError() {
		return m_OutTrainErr.getValue();
	}
	
	public boolean isOutValidError() {
		return m_OutValidErr.getValue();
	}
	
	public boolean isOutTestError() {
		return m_OutTestErr.getValue();
	}

	public boolean isCalError() {
		return m_CalErr.getValue();
	}
	
	public boolean isShowBranchFreq() {
		return m_ShowBrFreq.getValue();
	}

	public boolean isShowUnknown() {
		return m_ShowUnknown.getValue();
	}

	public boolean isPrintModelAndExamples() {
		return m_PrintModelAndExamples.getValue();
	}

	public boolean isOutFoldError() {
		return m_OutFoldErr.getValue();
	}

	public boolean isOutFoldData() {
		return m_OutFoldData.getValue();
	}

	public boolean isOutputFoldModels() {
		return m_OutFoldModels.getValue();
	}

	public boolean isWriteTestSetPredictions() {
		return m_WritePredictions.contains(WRITE_PRED_TEST);
	}

	public boolean isWriteTrainSetPredictions() {
		return m_WritePredictions.contains(WRITE_PRED_TRAIN);
	}

	public boolean isWriteErrorFile() {
		return m_WriteErrorFile.getValue();
	}

	public boolean isWriteModelIDPredictions() {
		return m_ModelIDFiles.getValue();
	}

	public boolean isOutputPythonModel() {
		return m_OutputPythonModel.getValue();
	}

	public boolean isOutputDatabaseQueries() {
		return m_OutputDatabaseQueries.getValue();
	}

	public boolean isShowXValForest() {
		return m_ShowForest.getValue();
	}
	
	public boolean isWriteCurves() {
		return m_WriteCurves.getValue();
	}	

	public boolean getShowModel(int i) {
		return m_ShowModels.contains(i);
	}
	
	public boolean shouldShowModel(int model) {		
		boolean others = getShowModel(Settings.SHOW_MODELS_OTHERS);
		if (model == ClusModel.DEFAULT && getShowModel(Settings.SHOW_MODELS_DEFAULT)) return true;
		else if (model == ClusModel.ORIGINAL && getShowModel(Settings.SHOW_MODELS_ORIGINAL)) return true;
		else if (model == ClusModel.PRUNED && (getShowModel(Settings.SHOW_MODELS_PRUNED) || others)) return true;
		else if (others) return true;
		return false;
	}	

	public StatisticPrintInfo getStatisticPrintInfo() {
		StatisticPrintInfo info = new StatisticPrintInfo();
		info.SHOW_EXAMPLE_COUNT = m_ShowInfo.contains(0);
		info.SHOW_EXAMPLE_COUNT_BYTARGET = m_ShowInfo.contains(1);
		info.SHOW_DISTRIBUTION = m_ShowInfo.contains(2);
		info.SHOW_INDEX = m_ShowInfo.contains(3);
		info.INTERNAL_DISTR = m_ShowInfo.contains(4);
		return info;
	}

	public int getBaggingSets() {
		return m_SetsData.getValue();
	}

/***********************************************************************
 * Section: Output - Show info in .out file                            *
 ***********************************************************************/

	public final static String[] SHOW_MODELS = {"Default", "Original", "Pruned", "Others"};

	public final static int[] SHOW_MODELS_VALUES = {0,2,3};
	public final static int SHOW_MODELS_DEFAULT = 0;
	public final static int SHOW_MODELS_ORIGINAL = 1;
	public final static int SHOW_MODELS_PRUNED = 2;
	public final static int SHOW_MODELS_OTHERS = 3;

	public final static String[] SHOW_INFO = {"Count", "CountByTarget", "Distribution", "Index", "NodePrototypes"};

	public final static int[] SHOW_INFO_VALUES = {0};

	public final static String[] CONVERT_RULES = { "No", "Leaves", "AllNodes" };

	public final static int CONVERT_RULES_NONE = 0;
	public final static int CONVERT_RULES_LEAVES = 1;
	public final static int CONVERT_RULES_ALLNODES = 2;

	public static boolean SHOW_UNKNOWN_FREQ;
	public static boolean SHOW_BRANCH_FREQ;

/***********************************************************************
 * Section: Output - Write predictions to file                         *
 ***********************************************************************/

	public final static String[] WRITE_PRED = { "None", "Test", "Train" };

	public final static int[] WRITE_PRED_VALUES = {0};
	public final static int WRITE_PRED_NONE = 0;
	public final static int WRITE_PRED_TEST = 1;
	public final static int WRITE_PRED_TRAIN = 2;

/***********************************************************************
 * Section: Tree                                                       *
 ***********************************************************************/

	public final static String[] TREE_OPTIMIZE_VALUES = {"NoClusteringStats", "NoInodeStats"};
	public final static int[] TREE_OPTIMIZE_NONE = {};	
	public final static int TREE_OPTIMIZE_NO_CLUSTERING_STATS = 0;
	public final static int TREE_OPTIMIZE_NO_INODE_STATS = 1;
	
	// Added by Eduardo Costa - 06/06/2011
	
	public final static String[] INDUCTION_ORDER = { "DepthFirst", "BestFirst"};
	public final static int DEPTH_FIRST = 0;
	public final static int BEST_FIRST = 1;
	
	protected INIFileNominal m_InductionOrder;
	
	// end block added by Eduardo
	
	
	// Added by Eduardo Costa - 27/09/2011
	
	public final static String[] ENTROPY_TYPE = { "StandardEntropy", "ModifiedEntropy"};
	public final static int STANDARD_ENTROPY = 0;
	public final static int MODIFIED_ENTROPY = 1;
		
	protected INIFileNominal m_EntropyType;
	
	protected INIFileBool m_ConsiderUnlableInstancesInIGCalc;
	
	// end block added by Eduardo
	
	
	protected INIFileSection m_SectionTree;
	protected INIFileNominal m_Heuristic;
	protected INIFileInt m_TreeMaxDepth;
	protected INIFileBool m_BinarySplit;
	protected INIFileBool m_AlternativeSplits;	
	protected INIFileNominalOrDoubleOrVector m_FTest;
	protected INIFileNominal m_PruningMethod;
	protected INIFileBool m_1SERule;
	protected INIFileBool m_MSENominal;
	protected INIFileDouble m_M5PruningMult;
	/** Do we transform leaves or all nodes of tree to rules */
	protected INIFileNominal m_RulesFromTree;
	protected INIFileNominal m_TreeOptimize;
	/** Amount of datapoints to include for calculating split heuristic
	 *  Datapoints will be selected randomly **/
	protected INIFileInt m_TreeSplitSampling;

	public void setSectionTreeEnabled(boolean enable) {
		m_SectionTree.setEnabled(enable);
	}

	public int getHeuristic() {
		return m_Heuristic.getValue();
	}

	public void setHeuristic(int value) {
		m_Heuristic.setSingleValue(value);
	}

	public boolean checkHeuristic(String value) {
		return m_Heuristic.getStringSingle().equals(value);
	}

	// added by Eduardo
	public int getInductionOrder() {
		return m_InductionOrder.getValue();
	}

	public void setInductionOrder(int value) {
		m_InductionOrder.setSingleValue(value);
	}

	public boolean checkInductionOrder(String value) {
		return m_InductionOrder.getStringSingle().equals(value);
	}
	// end block added by Eduardo
	
	
	
	// added by Eduardo - 27/09/2011
	public int getEntropyType() {
		return m_EntropyType.getValue();
	}

	public void setEntropyType(int value) {
		m_EntropyType.setSingleValue(value);
	}

	public boolean checkEntropyType(String value) {
		return m_EntropyType.getStringSingle().equals(value);
	}
	
	public boolean considerUnlableInstancesInIGCalc() {
		return m_ConsiderUnlableInstancesInIGCalc.getValue();
	}
	
	// end block added by Eduardo
	
	
	
	public int getTreeMaxDepth() {
		return m_TreeMaxDepth.getValue();
	}

	/**
	 * To find the best split, heuristic can be calculated on a
	 * random sample of the training set to conserve time
	 * @return the size the random sample should be
	 */
	public int getTreeSplitSampling() {
		return m_TreeSplitSampling.getValue();
	}
	
	/**
	 * To find the best split, heuristic can be calculated on a
	 * random sample of the training set to conserve time
	 * @param value the size the random sample should be
	 */
	public void setTreeSplitSampling(int value) {
		m_TreeSplitSampling.setValue(value);
	}
	
	/** For tree to rules procedure, we want to induce a tree without maximum
	 * depth
	 */
	public void setTreeMaxDepth(int value) {
		m_TreeMaxDepth.setValue(value);
	}

	public boolean isBinarySplit() {
		return m_BinarySplit.getValue();
	}

	public boolean showAlternativeSplits() {
		return m_AlternativeSplits.getValue();
	}

	public INIFileNominalOrDoubleOrVector getFTestArray() {
		return m_FTest;
	}

	public double getFTest() {
		return m_FTest.getDouble();
	}

	public void setFTest(double ftest) {
		FTEST_VALUE = ftest;
		FTEST_LEVEL = FTest.getLevelAndComputeArray(ftest);
		m_FTest.setDouble(ftest);
	}

	public int getPruningMethod() {
		return m_PruningMethod.getValue();
	}

	public void setPruningMethod(int method) {
		m_PruningMethod.setSingleValue(method);
	}

	public String getPruningMethodName() {
		return m_PruningMethod.getStringValue();
	}

	public boolean get1SERule() {
		return m_1SERule.getValue();
	}

	public boolean isMSENominal() {
		return m_MSENominal.getValue();
	}

	public double getM5PruningMult() {
		return m_M5PruningMult.getValue();
	}

	/**
	 * If we transform the induced trees to rules.
	 */
	public int rulesFromTree() {
		return m_RulesFromTree.getValue();
	}
	
	public boolean hasTreeOptimize(int value) {
		return m_TreeOptimize.contains(value);
	}

/***********************************************************************
 * Section: Tree - Heuristic                                           *
 ***********************************************************************/

	public final static String[] HEURISTICS = { "Default", "ReducedError",
		"Gain", "GainRatio", "SSPD", "VarianceReduction", "MEstimate", "Morishita",
		"DispersionAdt", "DispersionMlt", "RDispersionAdt", "RDispersionMlt",
		"GeneticDistance", "SemiSupervised", "VarianceReductionMissing"};

	public final static int HEURISTIC_DEFAULT = 0;
	public final static int HEURISTIC_REDUCED_ERROR = 1;
	public final static int HEURISTIC_GAIN = 2;
	public final static int HEURISTIC_GAIN_RATIO = 3;

	public final static int HEURISTIC_SSPD = 4;
	/** Sum of Squared Distances, the default for ensemble tree regression learning */
	public final static int HEURISTIC_VARIANCE_REDUCTION = 5;
	public final static int HEURISTIC_MESTIMATE = 6;
	public final static int HEURISTIC_MORISHITA = 7;
	public final static int HEURISTIC_DISPERSION_ADT = 8;
	public final static int HEURISTIC_DISPERSION_MLT = 9;
	public final static int HEURISTIC_R_DISPERSION_ADT = 10;
	public final static int HEURISTIC_R_DISPERSION_MLT = 11;
	public final static int HEURISTIC_GENETIC_DISTANCE = 12;
	public final static int HEURISTIC_SEMI_SUPERVISED = 13;
	public final static int HEURISTIC_SS_REDUCTION_MISSING = 14;

	public static int FTEST_LEVEL;
	public static double FTEST_VALUE;
	public static double MINIMAL_WEIGHT;
	public static boolean ONE_NOMINAL = true;

/***********************************************************************
 * Section: Tree - Pruning method                                      *
 ***********************************************************************/

	public final static String[] PRUNING_METHODS = { "Default", "None", "C4.5",
		"M5", "M5Multi", "ReducedErrorVSB", "Garofalakis", "GarofalakisVSB",
		"CartVSB", "CartMaxSize", "EncodingCost", "CategoryUtility" };

	public final static int PRUNING_METHOD_DEFAULT = 0;
	public final static int PRUNING_METHOD_NONE = 1;
	public final static int PRUNING_METHOD_C45 = 2;
	public final static int PRUNING_METHOD_M5 = 3;
	public final static int PRUNING_METHOD_M5_MULTI = 4;
	public final static int PRUNING_METHOD_REDERR_VSB = 5;
	public final static int PRUNING_METHOD_GAROFALAKIS = 6;
	public final static int PRUNING_METHOD_GAROFALAKIS_VSB = 7;
	public final static int PRUNING_METHOD_CART_VSB = 8;
	public final static int PRUNING_METHOD_CART_MAXSIZE = 9;
	public final static int PRUNING_METHOD_ENCODING_COST = 10;
	public final static int PRUNING_METHOD_CATEGORY_UTILITY = 11;
	
/***********************************************************************
 * Section: Rules                                                      *
 ***********************************************************************/

	protected INIFileSection m_SectionRules;	
	public static INIFileBool m_PrintAllRules;
	
	public void setSectionRulesEnabled(boolean enable) {
		m_SectionRules.setEnabled(enable);
	}

	public static boolean isPrintAllRules(){
		return m_PrintAllRules.getValue();
	}

	public final static String[] COVERING_METHODS =	{"Standard", "WeightedMultiplicative",
		"WeightedAdditive", "WeightedError", "Union", "BeamRuleDefSet",
		"RandomRuleSet", "StandardBootstrap", "HeurOnly", "RulesFromTree"};

	// Standard covering: ordered rules (decision list)
	public final static int COVERING_METHOD_STANDARD = 0;

	// 'Weighted' coverings: unordered rules
	public final static int COVERING_METHOD_WEIGHTED_MULTIPLICATIVE = 1;
	public final static int COVERING_METHOD_WEIGHTED_ADDITIVE = 2;
	public final static int COVERING_METHOD_WEIGHTED_ERROR = 3;

	/**
	 *  In multi-label classification: predicted set of classes is union
	 *  of predictions of individual rules
	 */
	public final static int COVERING_METHOD_UNION = 4;

	// Evaluates rules in the context of complete rule set: unordered rules
	// public final static int COVERING_METHOD_RULE_SET = 5;

	// Evaluates rules in the context of complete rule set, checks all rules
	// in the beam: unordered rules
	// public final static int COVERING_METHOD_BEAM_RULE_SET = 6;

	/** Evaluates rules in the context of complete rule set, builds default
	 *  rule first, checks all rules in the beam: unordered rules
	 *  FIXME Obsolete - should be deleted!
	 */
	public final static int COVERING_METHOD_BEAM_RULE_DEF_SET = 5;

	/**
	 *  Evaluates rules in the context of complete rule set, separate rules
	 *  are constructed randomly: unordered rules.
	 *  This is set only if the amount of RandomRules is greater than 0.
	 */
	public final static int COVERING_METHOD_RANDOM_RULE_SET = 6;

	/**
	 * Repeated standard covering on bootstraped data
	 */
	public final static int COVERING_METHOD_STANDARD_BOOTSTRAP = 7;

	/**
	 *  No covering, only heuristic
	 */
	public final static int COVERING_METHOD_HEURISTIC_ONLY = 8;

	/**
	 *  No covering, rules transcribed from tree
	 */
	public final static int COVERING_METHOD_RULES_FROM_TREE = 9;

	public final static String[] RULE_PREDICTION_METHODS =
	{"DecisionList", "TotCoverageWeighted", "CoverageWeighted", "AccuracyWeighted",
		"AccCovWeighted", "EquallyWeighted", "Optimized", "Union", "GDOptimized", "GDOptimizedBinary" };

	public final static int RULE_PREDICTION_METHOD_DECISION_LIST = 0;

	/**
	 *  Each rule's prediction has a weight proportional to its coverage on the total learning set
	 */
	public final static int RULE_PREDICTION_METHOD_TOT_COVERAGE_WEIGHTED = 1;

	/**
	 *  Each rule's prediction has a weight proportional to its coverage on the current learning set
	 *  i.e., learning set on which the rule was learned
	 */
	public final static int RULE_PREDICTION_METHOD_COVERAGE_WEIGHTED = 2;

	/**
	 *  Each rule's prediction has a weight proportional to its accuracy on the total learning set
	 *  TODO Not yet implemented.
	 */
	public final static int RULE_PREDICTION_METHOD_ACCURACY_WEIGHTED = 3;

	/**
	 *  Each rule's prediction has a weight proportional a product of to its accuracy on
	 *  the total learning set and its coverage
	 */
	public final static int RULE_PREDICTION_METHOD_ACC_COV_WEIGHTED = 4;
	//  TODO Not yet implemented.
	public final static int RULE_PREDICTION_METHOD_EQUALLY_WEIGHTED = 5;
	/** Differential evolution optimization of rule weights */
	public final static int RULE_PREDICTION_METHOD_OPTIMIZED = 6;
	public final static int RULE_PREDICTION_METHOD_UNION = 7;
	/** Gradient descent optimization of rule weights */
	public final static int RULE_PREDICTION_METHOD_GD_OPTIMIZED = 8;
	/** Use external binary file for gradient descent optimization of rule weights */
	public final static int RULE_PREDICTION_METHOD_GD_OPTIMIZED_BINARY = 9;

	public final static String[] RULE_ADDING_METHODS =	{"Always", "IfBetter", "IfBetterBeam"};
	// Always adds a rule to the rule set
	public final static int RULE_ADDING_METHOD_ALWAYS = 0;
	// Only adds a rule to the rule set if it improves the rule set performance
	public final static int RULE_ADDING_METHOD_IF_BETTER = 1;
	// Only adds a rule to the rule set if it improves the rule set performance.
	// If not, it checks other rules in the beam
	public final static int RULE_ADDING_METHOD_IF_BETTER_BEAM = 2;

	public static boolean IS_RULE_SIG_TESTING = false;
	
	// ***************** WEIGHT OPTIMIZATION

	// Differential evolution algorithm
	/**Possible loss functions for evolutionary algorithm optimization */
	public final static String[] OPT_LOSS_FUNCTIONS = {"Squared", "01Error", "RRMSE", "Huber"};

	/**	Optimization Loss function type. Default for regression: Square of differences. */
	public final static int OPT_LOSS_FUNCTIONS_SQUARED = 0;
	/**	Optimization Loss function type. 0/1 error for classification. Zenko 2007, p. 26*/
	public final static int OPT_LOSS_FUNCTIONS_01ERROR = 1;
	/**	Optimization Loss function type. Relative root mean squared error */
	public final static int OPT_LOSS_FUNCTIONS_RRMSE = 2;
	/**	Optimization Loss function type. Huber 1962 error. Like squared but robust for outliers. Friedman&Popescu 2005, p. 7*/
	public final static int OPT_LOSS_FUNCTIONS_HUBER = 3;

	/** GD optimization. Possible values for combining gradient targets to single gradient value. */
	public final static String[] OPT_GD_MT_COMBINE_GRADIENTS = {"Avg", "Max", "MaxLoss", "MaxLossFast"};
	/**	GD optimization, combining of targets - combine by taking average. */
	public final static int OPT_GD_MT_GRADIENT_AVG = 0;
	/**	GD optimization, combining of targets - combine by taking max gradient. */
	public final static int OPT_GD_MT_GRADIENT_MAX_GRADIENT = 1;
	/**	GD optimization, combining of targets - combine by taking the gradient of target with maximal loss. */
	public final static int OPT_GD_MT_GRADIENT_MAX_LOSS_VALUE = 2;
	/**	GD optimization, combining of targets - combine by taking the gradient of target with maximal LINEAR loss.
	 * I.e. if the real loss is something else, we still use linear loss. NOT IMPLEMENTED!
	 * In fact was not any faster AND max loss is worse than avg. */
	public final static int OPT_GD_MT_GRADIENT_MAX_LOSS_VALUE_FAST = 3;

	/**For external GD binary, do we use GD or brute force method */
	public final static String[] GD_EXTERNAL_METHOD_VALUES = {"update", "brute"};
	public final static int GD_EXTERNAL_METHOD_GD = 0;
	public final static int GD_EXTERNAL_METHOD_BRUTE = 1;
	
	public final static String[] OPT_LINEAR_TERM_NORM_VALUES = {"No", "Yes", "YesAndConvert"};
	/**	Do not normalize linear terms at all */
	public final static int OPT_LIN_TERM_NORM_NO = 0;
	/**	Normalize linear terms for optimization and leave the 
	 *  normalization to the resulting rule ensemble. DEFAULT.*/
	public final static int OPT_LIN_TERM_NORM_YES = 1;
	/**	Normalize linear terms for optimization, but after optimization
	 * convert the linear terms to plain terms without normalization without changing the resulting
	 * prediction. */
	public final static int OPT_LIN_TERM_NORM_CONVERT = 2;
	
	public final static String[] OPT_GD_ADD_LINEAR_TERMS = {"No", "Yes", "YesSaveMemory"};
	/** Do not add linear terms. DEFAULT */
	public final static int OPT_GD_ADD_LIN_NO = 0;
	/** Add linear terms explicitly. May cause huge memory usage if lots of targets and descriptive attrs. */
	public final static int OPT_GD_ADD_LIN_YES = 1;
	/** Use linear terms in optimization, but add them explicitly after optimization (if weight is zero). */
	public final static int OPT_GD_ADD_LIN_YES_SAVE_MEMORY = 2;
	
	public final static String[] OPT_NORMALIZATION = {"No", "Yes", "OnlyScaling", "YesVariance"};
	/** Do not normalize*/
	public final static int OPT_NORMALIZATION_NO = 0;
	/** Normalize during optimization with 2*std dev and shifting with average. Default. */
	public final static int OPT_NORMALIZATION_YES = 1;
	/** Normalize during optimization with 2*std dev. Default. */
	public final static int OPT_NORMALIZATION_ONLY_SCALING = 2;
	/** Normalize during optimization with variance. */
	public final static int OPT_NORMALIZATION_YES_VARIANCE = 3;
	
	// Settings in the settings file.
	protected INIFileNominal m_CoveringMethod;
	protected INIFileNominal m_PredictionMethod;
	protected INIFileNominal m_RuleAddingMethod;
	protected INIFileDouble m_CoveringWeight;
	protected INIFileDouble m_InstCoveringWeightThreshold;
	protected INIFileInt m_MaxRulesNb;
	protected INIFileDouble m_HeurDispOffset;
	protected INIFileDouble m_HeurCoveragePar;
	protected INIFileDouble m_HeurRuleDistPar;
	protected INIFileDouble m_HeurPrototypeDistPar;
	protected INIFileDouble m_RuleSignificanceLevel;
	protected INIFileInt m_RuleNbSigAtts;
	protected INIFileBool m_ComputeDispersion;
	protected INIFileDouble m_VarBasedDispNormWeight;
	protected INIFileNominalOrDoubleOrVector m_DispersionWeights;
	/** How many random rules are wanted. If > 0 only random rules are generated */
	protected INIFileInt m_RandomRules;
	protected INIFileBool m_RuleWiseErrors;
	
	// Rule tests are constrained to the first possible attribute value
	protected INIFileBool m_constrainedToFirstAttVal;

	//	Differential evolution optimization
	/**	DE Number of individuals (population) during every iteration */
	protected INIFileInt m_OptDEPopSize;
	/**	Differential evolution, number of individual evaluations to be done. Divide this with m_OptDEPopSize
	 * to get the number of 'iterations' */
	protected INIFileInt m_OptDENumEval;
	/** DE Crossover probability */
	protected INIFileDouble m_OptDECrossProb;
	protected INIFileDouble m_OptDEWeight;
	protected INIFileInt m_OptDESeed;
	/** DE The power of regularization function. The default is 1, i.e. l1 norm. */
	protected INIFileDouble m_OptDERegulPower;
	/** DE A probability to mutate certain value to zero. Useful if zero weights are wanted */
	protected INIFileDouble m_OptDEProbMutationZero;
	/** DE A reverse for the zeroing. A probability to mutate certain value to nonzero random value.
	 * Could be used if zeroing is used. */
	protected INIFileDouble m_OptDEProbMutationNonZero;

	// For all the optimization
	/** Optimization regularization parameter */
	protected INIFileDouble m_OptRegPar;
	/** Optimization regularization parameter - number of zeroes. Especially useful for DE optimization. */
	protected INIFileDouble m_OptNbZeroesPar;
	/** The treshold for rule weights. If weight < this, rule is removed. */
	protected INIFileDouble m_OptRuleWeightThreshold;
	/** DE The loss function. The default is squared loss. */
	protected INIFileNominal m_OptLossFunction;
	/** Optimization For Huber 1962 loss function an alpha value for outliers has to be given. */
	protected INIFileDouble m_OptHuberAlpha;
	/** Shift RULE predictions according to the default prediction. Should increase the accuracy. Default true.
	 * The default prediction is based on statistical factors of TARGET ATTRIBUTES. 
	 * Linear terms are not touched (similar is done for them with m_OptNormalizeLinearTerms). */
	protected INIFileBool m_OptDefaultShiftPred;
	/** Do we add the descriptive attributes as linear terms to rule set. Default No. */
	protected INIFileNominal m_OptAddLinearTerms;
	/** If linear terms are added, are they scaled so that each variable has similar effect.
	 * The normalization is done via statistical factors of DESCRIPTIVE ATTRIBUTES.
	 * You get similar effect by normalizing the data. Default Yes. */
	protected INIFileNominal m_OptNormalizeLinearTerms;
	/** If linear terms are added, are truncated so that they do not predict values greater or lower
	 * than found in the training set. Default Yes. */
	protected INIFileBool m_OptLinearTermsTruncate;
	/** Do we omit the rule predictions such that the (single target) predictions are changed to 1. This does
	 * not do anything to the linear terms. Default Yes. */
	protected INIFileBool m_OptOmitRulePredictions;
	/** Do we scale the predictions for optimization based on the coverage
	 * This should put more weight to general rules. Default No.*/
	protected INIFileBool m_OptWeightGenerality;
	/** Do we normalize the targets of predictions and true values internally for optimization.
	 * The normalization is done with statistical factors of TARGET ATTRIBUTES.
	 * This normalization is inverted after the optimization. On default YES, because
	 * it should make at least GD optimization work better (covariance computing may not work otherwise).
	 * This makes the error function very similar to RRMSE.
	 * Alternatively YesVariance normalizes with variance.*/
//	protected INIFileBool m_OptNormalization;
	protected INIFileNominal m_OptNormalization;

	// Gradient descent optimization
	/** GD Maximum amount of iterations */
	protected INIFileInt m_OptGDMaxIter;
	/** GD Treshold [0,1] for changing the gradient. This portion of maximum gradients are affecting.
 	 * A value between [0,1].If 1 (default) this is simliar to L1 regularization (Lasso) and 0 similar to L2.*/
	protected INIFileDouble m_OptGDGradTreshold;
	/** GD Initial step size ]0,1] for each iteration. If m_OptGDIsDynStepsize is true, this is not used. */
	protected INIFileDouble m_OptGDStepSize;
	/** GD Compute lower limit of step size based on the predictions. Default Yes. */
	protected INIFileBool m_OptGDIsDynStepsize;
	/** GD Maximum number of nonzero weights. If the number reached, only old ones are altered.
	 * If = 0, no limit for nonzero weights.*/
	protected INIFileInt m_OptGDMaxNbWeights;
	/** GD User early stopping criteria for this amount of data. If 0, no early stopping used. */
	protected INIFileDouble m_OptGDEarlyStopAmount;
	/** GD Early stopping criteria treshold. Value should be greater than 1. Used at least
	 * for stopping GD optimization for single T value. However, if OptGDEarlyTTryStop true, also
	 * used for stopping to try new T values. */
	protected INIFileDouble m_OptGDEarlyStopTreshold;
	/** GD When early stopping is found, how many times we try to reduce the step size and try again
	 * Default is Infinity. In this case we use all the iterations by reducing step size. */
	protected INIFileStringOrInt m_OptGDNbOfStepSizeReduce;
	/** GD External binary, do we use GD or brute force method*/
	protected INIFileNominal m_OptGDExternalMethod;
	/** GD How to combine multiple targets to single gradient value for step taking */
	protected INIFileNominal m_OptGDMTGradientCombine;
	/** GD How many different parameter combinations we try for T. Values between [m_OptGDGradTreshold,1] */
	protected INIFileInt m_OptGDNbOfTParameterTry;
	/** GD When running from T=1 down, do we stop if the error starts to increase. Should make optimization
	 * a lot faster, but may decrease the accuracy. Default Yes.*/
	protected INIFileBool m_OptGDEarlyTTryStop;

	public INIFileNominalOrDoubleOrVector getDispersionWeights() {
		return m_DispersionWeights;
	}

	/**
	 * Returns if random rules are wanted. That is RandomRules in settings file is above 0.
	 */
	public boolean isRandomRules() {
	    return (m_RandomRules.getValue() > 0);
	}

	/**
	 * How many random rules are wanted by RandomRules in the settings file.
	 */
	public int nbRandomRules() {
	    return m_RandomRules.getValue();
	}

	public boolean isRuleWiseErrors() {
	  	return m_RuleWiseErrors.getValue();
	}

	public int getCoveringMethod() {
	    return m_CoveringMethod.getValue();
	}

	public void setCoveringMethod(int method) {
	    m_CoveringMethod.setSingleValue(method);
	}

	public int getRulePredictionMethod() {
	    return m_PredictionMethod.getValue();
	}

	public boolean isRulePredictionOptimized() {
		return (getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_OPTIMIZED ||
				getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED ||
				getRulePredictionMethod() == Settings.RULE_PREDICTION_METHOD_GD_OPTIMIZED_BINARY);
	}

	public void setRulePredictionMethod(int method) {
	    m_PredictionMethod.setSingleValue(method);
	}

	public int getRuleAddingMethod() {
	    return m_RuleAddingMethod.getValue();
	}

	public void setRuleAddingMethod(int method) {
	    m_RuleAddingMethod.setSingleValue(method);
	}

	public double getCoveringWeight() {
	    return m_CoveringWeight.getValue();
	}

	public void setCoveringWeight(double weight) {
	    m_CoveringWeight.setValue(weight);
	}

	public double getInstCoveringWeightThreshold() {
	    return m_InstCoveringWeightThreshold.getValue();
	}

	public void setInstCoveringWeightThreshold(double thresh) {
	  	m_InstCoveringWeightThreshold.setValue(thresh);
	}

	public int getMaxRulesNb() {
	    return m_MaxRulesNb.getValue();
	}

	public void setMaxRulesNb(int nb) {
	    m_MaxRulesNb.setValue(nb);
	}

	public double getRuleSignificanceLevel() {
	    return m_RuleSignificanceLevel.getValue();
	}

	public int getRuleNbSigAtt() {
	    return m_RuleNbSigAtts.getValue();
	}

	public boolean isRuleSignificanceTesting() {
		return m_RuleNbSigAtts.getValue() != 0;
	}

	public double getHeurDispOffset() {
		return m_HeurDispOffset.getValue();
	}

	public double getHeurCoveragePar() {
		return m_HeurCoveragePar.getValue();
	}

	public double getHeurRuleDistPar() {
		return m_HeurRuleDistPar.getValue();
	}

	public void setHeurRuleDistPar(double value) {
	    m_HeurRuleDistPar.setValue(value);
	}

	public boolean isHeurRuleDist() {
	    return m_HeurRuleDistPar.getValue() > 0;
	}

	public boolean isWeightedCovering() {
		if (m_CoveringMethod.getValue() == COVERING_METHOD_WEIGHTED_ADDITIVE ||
			m_CoveringMethod.getValue() == COVERING_METHOD_WEIGHTED_MULTIPLICATIVE ||
			m_CoveringMethod.getValue() == COVERING_METHOD_WEIGHTED_ERROR) {
			return true;
		} else {
			return false;
		}
	}

  	public double getHeurPrototypeDistPar() {
	  	return m_HeurPrototypeDistPar.getValue();
  	}

  	public void setHeurPrototypeDistPar(double value) {
		m_HeurPrototypeDistPar.setValue(value);
	}

	public boolean isHeurPrototypeDistPar() {
	 	return m_HeurPrototypeDistPar.getValue() > 0;
	}

	private boolean m_ruleInduceParamsDisabled = false;
	private double m_origHeurRuleDistPar = 0;
	private int m_origRulePredictionMethod = 0;
	private int m_origCoveringMethod = 0;
	
	/** For forest induction, disable rule parameters that interfere. If you need to return the originals
	 * use returnRuleInduceParams 
	 */
	public void disableRuleInduceParams() {
		if (!m_ruleInduceParamsDisabled) { // Make sure the original values are not lost.
			m_origHeurRuleDistPar = getHeurRuleDistPar();
			m_origRulePredictionMethod = getRulePredictionMethod();
			m_origCoveringMethod = getCoveringMethod();
			
			setHeurRuleDistPar(0.0);
			setRulePredictionMethod(RULE_PREDICTION_METHOD_DECISION_LIST);
			setCoveringMethod(COVERING_METHOD_RULES_FROM_TREE);
			m_ruleInduceParamsDisabled = true; // Mark that these are disabled
		}
	}

	/** For TreesToRules induction return the original parameters after forest induced */
	public void returnRuleInduceParams() {
		if (m_ruleInduceParamsDisabled) { // Only if they have been disabled previously
			setHeurRuleDistPar(m_origHeurRuleDistPar);
			setRulePredictionMethod(m_origRulePredictionMethod);
			setCoveringMethod(m_origCoveringMethod);
			m_ruleInduceParamsDisabled = false; // Mark that not anymore disabled
		}
	}

	
	public boolean computeDispersion() {
		return m_ComputeDispersion.getValue();
  	}

  	public double getVarBasedDispNormWeight() {
  		return m_VarBasedDispNormWeight.getValue();
  	}
  	
	public boolean isConstrainedToFirstAttVal() {
	 	return m_constrainedToFirstAttVal.getValue();
	}

	public double getOptDECrossProb() {
		return m_OptDECrossProb.getValue();
	}

	public int getOptDENumEval() {
		return m_OptDENumEval.getValue();
	}

	public int getOptDEPopSize() {
		return m_OptDEPopSize.getValue();
	}

	public int getOptDESeed() {
		return m_OptDESeed.getValue();
	}

	public double getOptDEWeight() {
		return m_OptDEWeight.getValue();
	}

	/** Optimization regularization parameter */
	public double getOptRegPar() {
		return m_OptRegPar.getValue();
	}

	/** Optimization regularization parameter */
	public void setOptRegPar(double newValue) {
		m_OptRegPar.setValue(newValue);
	}

	/** Optimization regularization parameter - number of zeroes */
	public double getOptNbZeroesPar() {
		return m_OptNbZeroesPar.getValue();
	}

	/** Optimization regularization parameter - number of zeroes */
	public void setOptNbZeroesPar(double newValue) {
		m_OptNbZeroesPar.setValue(newValue);
	}

	public double getOptRuleWeightThreshold() {
		return m_OptRuleWeightThreshold.getValue();
	}

	
	/** Shift predictions according to the default prediction. Should increase the accuracy */
	public boolean isOptDefaultShiftPred() {
		return m_OptDefaultShiftPred.getValue();
	}
	
	/** Do we add linear terms to rule set */
	public boolean isOptAddLinearTerms() {
	  	return (m_OptAddLinearTerms.getValue() != OPT_GD_ADD_LIN_NO);
	}

	/** How we add linear terms to rule set. Use memory saving? */
	public int getOptAddLinearTerms() {
	  	return m_OptAddLinearTerms.getValue();
	}

	/** Do we scale linear terms so that the attributes have similar effect */
	public boolean isOptNormalizeLinearTerms() {
		return (m_OptNormalizeLinearTerms.getValue() != OPT_LIN_TERM_NORM_NO);
	}

	/** What kind of normalization are we using */
	public int getOptNormalizeLinearTerms() {
		return m_OptNormalizeLinearTerms.getValue();
	}
	
	/** Are linear terms truncated so that they do not predict values greater or lower
	 * than found in the training set.*/
	public boolean isOptLinearTermsTruncate() {
		return m_OptLinearTermsTruncate.getValue();
	}
	/** Do we omit rule predictions */
	public boolean isOptOmitRulePredictions() {
		return m_OptOmitRulePredictions.getValue();
	}

	/** Do we scale the predictions of the rules with the generality. This puts more weight to general rules
	 */
	public boolean isOptWeightGenerality() {
	  	return m_OptWeightGenerality.getValue();
	}
	
	/** Do we normalize the predictions and true values internally for optimization.*/
	public boolean isOptNormalization() {
	  	return m_OptNormalization.getValue() != Settings.OPT_NORMALIZATION_NO;
	}

	/** How we normalize the predictions and true values internally for optimization.*/
	public int getOptNormalization() {
	  	return m_OptNormalization.getValue();
	}
	/** Type of Loss function for DE optimization */
	public int getOptDELossFunction() {
		return m_OptLossFunction.getValue();
	}

	/** Power for regularization parameter */
	public double getOptDERegulPower() {
		return m_OptDERegulPower.getValue();
	}

	/** DE A probability to mutate certain value to zero. Useful if zero weights are wanted */
	public double getOptDEProbMutationZero()	{
		return m_OptDEProbMutationZero.getValue();
	}

	/** DE A reverse for the zeroing. A probability to mutate certain value to nonzero random value.
	 * Could be used if zeroing is used. */
	public double getOptDEProbMutationNonZero() {
		return m_OptDEProbMutationNonZero.getValue();
	}

	/** For Huber 1962 loss function an alpha value for outliers has to be given. */
	public double getOptHuberAlpha() {
		return m_OptHuberAlpha.getValue();
	}

	/** GD Maximum amount of iterations */
	public int getOptGDMaxIter(){
		return m_OptGDMaxIter.getValue();
	}

	/** GD The used loss function */
	public int getOptGDLossFunction() {
		return m_OptLossFunction.getValue();
	}

	/** GD Treshold [0,1] for changing the gradient. This portion of maximum gradients are affecting.
	 * This can be considered as the regularization parameter, if this is 1 it is similar to L1 (Lasso) penalty.
	 */
	public double getOptGDGradTreshold() {
		return m_OptGDGradTreshold.getValue();
	}

	/** GD Treshold [0,1] for changing the gradient. This portion of maximum gradients are affecting.
	 * This can be considered as the regularization parameter, if this is 1 it is similar to L1 (Lasso) penalty.
	 */
	public void setOptGDGradTreshold(double newVal) {
		m_OptGDGradTreshold.setValue(newVal);
	}


	/** GD Step size ]0,1] for each iteration. */
	public double getOptGDStepSize(){
		return m_OptGDStepSize.getValue();
	}

	/** GD Step size ]0,1] for each iteration. */
	public boolean isOptGDIsDynStepsize(){
		return m_OptGDIsDynStepsize.getValue();
	}

	/** Amount of data used for early stopping check. If zero, not used. */
	public double getOptGDEarlyStopAmount() {
		return m_OptGDEarlyStopAmount.getValue();
	}

	/** Early stopping criterion treshold */
	public double getOptGDEarlyStopTreshold() {
		return m_OptGDEarlyStopTreshold.getValue();
	}

	/** GD Maximum number of nonzero weights. If the number reached, only old ones are altered.
	 * If = 0, no limit for nonzero weights.*/
	public int getOptGDMaxNbWeights(){
		return m_OptGDMaxNbWeights.getValue();
	}

	/** GD Maximum number of nonzero weights. If the number reached, only old ones are altered.
	 * If = 0, no limit for nonzero weights.*/
	public void setOptGDMaxNbWeights(int nbWeights) {
		m_OptGDMaxNbWeights.setValue(nbWeights);
	}

	/** GD When early stopping is found, how many times we try to reduce the step size and try again
	 * Default is 0, but can be Infinity. In this case we use all the iterations by reducing step size. */
	public int getOptGDNbOfStepSizeReduce() {
		if (m_OptGDNbOfStepSizeReduce.isString(INFINITY_STRING)) return Integer.MAX_VALUE;
		else return m_OptGDNbOfStepSizeReduce.getIntValue();
	}

	/** What method we use for external GD optimization algorithm */
	public int getOptGDExternalMethod(){
		return m_OptGDExternalMethod.getValue();
	}

	/** GD How to combine multiple targets to single gradient value for step taking */
	public int getOptGDMTGradientCombine() {
		return m_OptGDMTGradientCombine.getValue();
	}

	/** GD How many different parameter combinations we try for T. Values between [T,1] */
	public int getOptGDNbOfTParameterTry() {
		return m_OptGDNbOfTParameterTry.getValue();
	}
	
	/** GD When running from T=1 down, do we stop if the error starts to increase. Should make optimization
	 * a lot faster, but may decrease the accuracy.*/
	public boolean getOptGDEarlyTTryStop() {
		return m_OptGDEarlyTTryStop.getValue();
	}


/***********************************************************************
 * Section: Hierarchical multi-label classification                    *
 ***********************************************************************/

	// Hierarchical multi-classification now supports both trees and DAGS
	// This was required because Gene Ontology terms are organized in a partial order
	public final static String[] HIERTYPES = { "Tree", "DAG" };

	public final static int HIERTYPE_TREE = 0;
	public final static int HIERTYPE_DAG = 1;

    public final static String[] HIERWEIGHT = { "ExpSumParentWeight", "ExpAvgParentWeight", "ExpMinParentWeight", "ExpMaxParentWeight", "NoWeight" };

    public final static int HIERWEIGHT_EXP_SUM_PARENT_WEIGHT = 0;
    public final static int HIERWEIGHT_EXP_AVG_PARENT_WEIGHT = 1;
    public final static int HIERWEIGHT_EXP_MIN_PARENT_WEIGHT = 2;
    public final static int HIERWEIGHT_EXP_MAX_PARENT_WEIGHT = 3;
    public final static int HIERWEIGHT_NO_WEIGHT = 4;

    public final static String[] HIERDIST = { "WeightedEuclidean", "Jaccard" };

    public final static int HIERDIST_WEIGHTED_EUCLIDEAN = 0;
    public final static int HIERDIST_JACCARD = 1;

    public final static String[] HIERMEASURES = { "AverageAUROC", "AverageAUPRC", "WeightedAverageAUPRC", "PooledAUPRC" };

    public final static int HIERMEASURE_AUROC  = 0;
    public final static int HIERMEASURE_AUPRC  = 1;
    public final static int HIERMEASURE_WEIGHTED_AUPRC = 2;
    public final static int HIERMEASURE_POOLED_AUPRC = 3;

	INIFileSection m_SectionHierarchical;
	protected INIFileNominal m_HierType;
	protected INIFileNominal m_HierWType;
	protected INIFileNominal m_HierDistance;
	protected INIFileDouble m_HierWParam;
	protected INIFileString m_HierSep;
	protected INIFileString m_HierEmptySetIndicator;
	protected INIFileNominal m_HierOptimizeErrorMeasure;
	protected INIFileString m_DefinitionFile;
	protected INIFileBool m_HierNoRootPreds;
	protected INIFileBool m_HierSingleLabel;
	protected INIFileBool m_CalErr;
	protected INIFileDouble m_HierPruneInSig;
	protected INIFileBool m_HierUseBonferroni;
	protected INIFileNominalOrDoubleOrVector m_HierClassThreshold;
	protected INIFileNominalOrDoubleOrVector m_RecallValues;
	protected INIFileString m_HierEvalClasses;
	protected static INIFileBool m_HierUseMEstimate;

	public void setSectionHierarchicalEnabled(boolean enable) {
		m_SectionHierarchical.setEnabled(enable);
	}

	public boolean getHierSingleLabel() {
		return m_HierSingleLabel.getValue();
	}
	
	public int getHierType() {
		return m_HierType.getValue();
	}

	public int getHierDistance() {
		return m_HierDistance.getValue();
	}

	public int getHierWType() {
		return m_HierWType.getValue();
	}

	public double getHierWParam() {
		return m_HierWParam.getValue();
	}

	public INIFileNominalOrDoubleOrVector getClassificationThresholds() {
		return m_HierClassThreshold;
	}
	
	public INIFileNominalOrDoubleOrVector getRecallValues() {
		return m_RecallValues;
	}	

	public boolean isHierNoRootPreds() {
		return m_HierNoRootPreds.getValue();
	}

	public boolean isUseBonferroni() {
		return m_HierUseBonferroni.getValue();
	}

	public double getHierPruneInSig() {
		return m_HierPruneInSig.getValue();
	}

	public boolean hasHierEvalClasses() {
		return !StringUtils.unCaseCompare(m_HierEvalClasses.getValue(), NONE);
	}

	public String getHierEvalClasses() {
		return m_HierEvalClasses.getValue();
	}

	public int getHierOptimizeErrorMeasure() {
		return m_HierOptimizeErrorMeasure.getValue();
	}

	public boolean hasDefinitionFile() {
		return !StringUtils.unCaseCompare(m_DefinitionFile.getValue(), NONE);
	}

	public String getDefinitionFile() {
		return m_DefinitionFile.getValue();
	}

	public void initHierarchical() {
		ClassesValue.setHSeparator(m_HierSep.getValue());
		ClassesValue.setEmptySetIndicator(m_HierEmptySetIndicator.getValue());
	}
	
	public static boolean useMEstimate() {
		return m_HierUseMEstimate.getValue();
	}

/***********************************************************************
 * Section: Instance level constraints                                 *
 ***********************************************************************/

	protected INIFileSection m_SectionILevelC;
	protected INIFileString m_ILevelCFile;
	protected INIFileDouble m_ILevelCAlpha;
	protected INIFileInt m_ILevelNbRandomConstr;
	protected INIFileBool m_ILevelCCOPKMeans;
	protected INIFileBool m_ILevelCMPCKMeans;

	public boolean isSectionILevelCEnabled() {
		return m_SectionILevelC.isEnabled();
	}

	public boolean hasILevelCFile() {
		return !StringUtils.unCaseCompare(m_ILevelCFile.getValue(), NONE);
	}

	public String getILevelCFile() {
		return m_ILevelCFile.getValue();
	}

	public double getILevelCAlpha() {
		return m_ILevelCAlpha.getValue();
	}

	public int getILevelCNbRandomConstraints() {
		return m_ILevelNbRandomConstr.getValue();
	}

	public boolean isILevelCCOPKMeans() {
		return m_ILevelCCOPKMeans.getValue();
	}

	public boolean isILevelCMPCKMeans() {
		return m_ILevelCMPCKMeans.getValue();
	}

/***********************************************************************
 * Section: Beam search                                                *
 ***********************************************************************/

	public static int BEAM_WIDTH;
	public static double SIZE_PENALTY;
	public static double BEAM_SIMILARITY;
	public static boolean BEAM_SYNT_DIST_CONSTR;

	protected INIFileSection m_SectionBeam;
	protected INIFileDouble m_SizePenalty;
	protected INIFileInt m_BeamWidth;
	protected INIFileInt m_BeamBestN;
	protected INIFileInt m_TreeMaxSize;
	protected INIFileNominal m_BeamAttrHeuristic;
	protected INIFileBool m_FastBS;
	protected INIFileBool m_BeamPostPrune;
	protected INIFileBool m_BMRemoveEqualHeur;
	protected INIFileDouble m_BeamSimilarity;
	protected INIFileBool m_BSortTrainParameter;
	protected INIFileBool m_BeamToForest;
	protected INIFileString m_BeamSyntacticConstrFile;

	public void setSectionBeamEnabled(boolean enable) {
		m_SectionBeam.setEnabled(enable);
	}

	public int getBeamWidth() {
		return m_BeamWidth.getValue();
	}

	public double getSizePenalty() {
		return m_SizePenalty.getValue();
	}

	public boolean isBeamSearchMode(){
		return m_SectionBeam.isEnabled();
	}
	
	public int getBeamBestN() {
		return m_BeamBestN.getValue();
	}

	public int getBeamTreeMaxSize() {
		return m_TreeMaxSize.getValue();
	}

	public boolean getBeamRemoveEqualHeur() {
		return m_BMRemoveEqualHeur.getValue();
	}

	public boolean getBeamSortOnTrainParameter() {
		return m_BSortTrainParameter.getValue();
	}

	public double getBeamSimilarity(){
		return m_BeamSimilarity.getValue();
	}

	public boolean isBeamPostPrune() {
		return m_BeamPostPrune.getValue();
	}

	public int getBeamAttrHeuristic() {
		return m_BeamAttrHeuristic.getValue();
	}

	public boolean hasBeamConstraintFile() {
		return !StringUtils.unCaseCompare(m_BeamSyntacticConstrFile.getValue(),	NONE);
	}

	public String getBeamConstraintFile() {
		return m_BeamSyntacticConstrFile.getValue();
	}

	public boolean isBeamToForest(){
		return m_BeamToForest.getValue();
	}

	public boolean isFastBS() {
		return m_FastBS.getValue();
	}

/***********************************************************************
 * Section: Exhaustive search                                          *
 ***********************************************************************/

	protected INIFileSection m_SectionExhaustive;
	protected INIFileBool m_Exhaustive;
	protected INIFileInt m_StartTreeCpt;
	protected INIFileInt m_StartItemCpt;

	public void setSectionExhaustiveEnabled(boolean enable) {
		m_SectionExhaustive.setEnabled(enable);
	}

	public boolean isExhaustiveSearch() {
		return m_Exhaustive.getValue();
	}

	public int getStartTreeCpt() {
		return m_StartTreeCpt.getValue();
	}

	public int getStartItemCpt() {
		return m_StartItemCpt.getValue();
	}

/***********************************************************************
 * Section: Time series                                                *
 ***********************************************************************/

	public final static String[] TIME_SERIES_DISTANCE_MEASURE={"DTW","QDM","TSC"};

	public final static int TIME_SERIES_DISTANCE_MEASURE_DTW = 0;
	public final static int TIME_SERIES_DISTANCE_MEASURE_QDM = 1;
	public final static int TIME_SERIES_DISTANCE_MEASURE_TSC = 2;

	public final static String[] TIME_SERIES_PROTOTYPE_COMPLEXITY={"N2", "LOG", "LINEAR", "NPAIRS", "TEST"};

	INIFileSection m_SectionTimeSeries;
	public INIFileNominal m_TimeSeriesDistance;
	public INIFileNominal m_TimeSeriesHeuristicSampling;

	public boolean isSectionTimeSeriesEnabled() {
		return m_SectionTimeSeries.isEnabled();
	}

	public void setSectionTimeSeriesEnabled(boolean enable) {
		m_SectionTimeSeries.setEnabled(enable);
	}

	public boolean isTimeSeriesProtoComlexityExact() {
		if (m_TimeSeriesHeuristicSampling.getValue() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public int getTimeSeriesDistance() {
		return m_TimeSeriesDistance.getValue();
	}

	public int getTimeSeriesHeuristicSampling() {
		return m_TimeSeriesHeuristicSampling.getValue();
	}

/***********************************************************************
 * Section: Phylogeny                                             	   *
 ***********************************************************************/

	public final static String[] PHYLOGENY_DISTANCE_MEASURE={"PDist","Edit","JC","Kimura","AminoKimura"};

	public final static int PHYLOGENY_DISTANCE_MEASURE_PDIST = 0;
	public final static int PHYLOGENY_DISTANCE_MEASURE_EDIT = 1;
	public final static int PHYLOGENY_DISTANCE_MEASURE_JC = 2;
	public final static int PHYLOGENY_DISTANCE_MEASURE_KIMURA = 3;
	public final static int PHYLOGENY_DISTANCE_MEASURE_AMINOKIMURA = 4;

	public final static String[] PHYLOGENY_SEQUENCE={"DNA","Protein","Any"};

	public final static int PHYLOGENY_SEQUENCE_DNA = 0;
	public final static int PHYLOGENY_SEQUENCE_AMINO = 1;
	public final static int PHYLOGENY_SEQUENCE_ANY = 2;

	public final static String[] PHYLOGENY_CRITERION={"MinTotBranchLength", "MaxAvgPWDistance", "MaxMinPWDistance"};

	public final static int PHYLOGENY_CRITERION_BRANCHLENGTHS = 0;
	public final static int PHYLOGENY_CRITERION_MAXAVGPWDIST = 1;
	public final static int PHYLOGENY_CRITERION_MAXMINPWDIST = 2;

	INIFileSection m_SectionPhylogeny;
	public static INIFileNominal m_PhylogenyDM;
	public static INIFileNominal m_PhylogenyCriterion;
	public static INIFileNominal m_PhylogenySequence;
	public static INIFileString m_PhylogenyDistanceMatrix;
	public static INIFileDouble m_PhylogenyEntropyVsRootStop;
	public static INIFileDouble m_PhylogenyDistancesVsRootStop;
	public static INIFileDouble m_PhylogenyEntropyVsParentStop;
	public static INIFileDouble m_PhylogenyDistancesVsParentStop;

	public String getPhylogenyDistanceMatrix() {
		return m_PhylogenyDistanceMatrix.getValue();
	}
	
	public boolean isSectionPhylogenyEnabled() {
		return m_SectionPhylogeny.isEnabled();
	}

	public void setSectionPhylogenyEnabled(boolean enable) {
		m_SectionPhylogeny.setEnabled(enable);
	}
	
	public double getPhylogenyEntropyVsRootStop() {
		return m_PhylogenyEntropyVsRootStop.getValue();
	}
	
	public double getPhylogenyDistancesVsRootStop() {
		return m_PhylogenyDistancesVsRootStop.getValue();
	}
	
	public double getPhylogenyEntropyVsParentStop() {
		return m_PhylogenyEntropyVsParentStop.getValue();
	}
	
	public double getPhylogenyDistancesVsParentStop() {
		return m_PhylogenyDistancesVsParentStop.getValue();
	}
	

/***********************************************************************
 * Section: Ensemble methods                                           *
 ***********************************************************************/

	public final static String[] ENSEMBLE_TYPE = {"Bagging", "RForest", "RSubspaces", "BagSubspaces", "Boosting", "RFeatSelection", "Pert"};

	public final static int ENSEMBLE_BAGGING = 0;
	public final static int ENSEMBLE_RFOREST = 1; 
	/** Random subspaces */
	public final static int ENSEMBLE_RSUBSPACES = 2;
	/** Bagging of subspaces */
	public final static int ENSEMBLE_BAGSUBSPACES = 3;
	public final static int ENSEMBLE_BOOSTING = 4;
	public final static int ENSEMBLE_NOBAGRFOREST = 5;
	public final static int ENSEMBLE_PERT = 6;

	public final static String[] VOTING_TYPE={"Majority","ProbabilityDistribution"};

	public final static int VOTING_TYPE_MAJORITY = 0;
	public final static int VOTING_TYPE_PROBAB_DISTR = 1;

	INIFileSection m_SectionEnsembles;
	protected INIFileNominalOrIntOrVector m_NbBags;
	/** Used ensemble method */
	public static INIFileNominal m_EnsembleMethod;
	/** Voting type, for regression mean is always used, the options are for classification */
	public static INIFileNominal m_ClassificationVoteType;
	/** Size of the feature set used during tree induction. Used for random forests, random 
	 * subspaces and bagging of subspaces. If left to default 0, floor(log_2 #DescAttr) + 1 is used.*/
	protected INIFileString m_RandomAttrSelected;
	protected int m_SubsetSize;
	public static INIFileBool m_PrintAllModels;
	public static INIFileBool m_PrintAllModelFiles;
	public static INIFileBool m_PrintPaths;
	public static boolean m_EnsembleMode = false;
	/** Time & memory optimization */
	public static INIFileBool m_EnsembleShouldOpt;
	/** Estimate error with time & memory optimization */
	public static INIFileBool m_EnsembleOOBestimate;
	protected INIFileBool m_FeatureRanking;
	protected INIFileBool m_WriteEnsemblePredictions;
	protected INIFileNominalOrIntOrVector m_BagSelection;

	/** Do we want to use different random depth for different iterations of ensemble.
	 * Used in tree to rules optimization method. The MaxDepth of tree is used as average.
	 */
	protected INIFileBool m_EnsembleRandomDepth;

	
	protected INIFileInt m_EnsembleBagSize;

	public boolean isEnsembleMode() {
		return m_EnsembleMode;
	}

	public void setEnsembleMode(boolean value) {
		m_EnsembleMode = value;
	}

	/** Do we print ensemble settings to output files */
	public boolean isSectionEnsembleEnabled() {
		return m_SectionEnsembles.isEnabled();
	}

	/** Do we print ensemble settings to output files */
	public void setSectionEnsembleEnabled(boolean value) {
		m_SectionEnsembles.setEnabled(value);
	}

	public int getEnsembleMethod() {
		return m_EnsembleMethod.getValue();
	}

	public void setEnsembleMethod(String value){
		m_EnsembleMethod.setValue(value);
	}

	public void setEnsembleMethod(int value){
		m_EnsembleMethod.setSingleValue(value);
	}

	public boolean shouldPerformRanking(){
		return m_FeatureRanking.getValue();
	}

	public void setFeatureRanking(boolean value){
		m_FeatureRanking.setValue(value);
	}

	public INIFileNominalOrIntOrVector getNbBaggingSets(){
		if (!m_NbBags.isVector()&&(m_NbBags.getInt() == 0))m_NbBags.setInt(10);
		return m_NbBags;
	}

	public void setNbBags(int value){
		m_NbBags.setInt(value);
	}

	public int getNbRandomAttrSelected() {
		return m_SubsetSize;
	}

	public String getNbRandomAttrString() {
		return m_RandomAttrSelected.getValue();
	}
	
	public INIFileNominalOrIntOrVector getBagSelection() {
		return m_BagSelection;
	}

	public void updateNbRandomAttrSelected(ClusSchema schema){
		int fsize = -1;
		String value = getNbRandomAttrString();
		if (value.contains("-")){
			System.err.println("The number of subspaces can't be negative.");
			System.err.println("\t Setting this value to default (log2).");
			value = "0";
		}
		
		if (value.equalsIgnoreCase("SQRT")){
			fsize = (int) Math.ceil(Math.sqrt(schema.getNbDescriptiveAttributes()));//upper bound of sqrt
		} else if (value.equalsIgnoreCase("LOG") || value.equalsIgnoreCase("0")){// the 0 is to keep the previous setting
			fsize = (int) Math.ceil(Math.log(schema.getNbDescriptiveAttributes())/Math.log(2));//upper bound of log2
		} else {
			try {
				int val = Integer.parseInt(value);
				if (val > schema.getNbDescriptiveAttributes()){
					System.err.println("The size of the subset can't be larger than the number of descriptive attributes.");
					System.err.println("\t Setting this value to the number of attributes, i.e., to bagging.");
					val = schema.getNbDescriptiveAttributes();
				}
				fsize = val;
			}catch (NumberFormatException e) {//not an integer
				try{
				double val = Double.parseDouble(value);
				if (val > 1.0){
					System.err.println("The fraction of the features used can't be larger than 1.");
					System.err.println("\t Setting this value to 1, i.e., to bagging.");
					val = 1.0;
				}
				fsize = (int) Math.round(val*schema.getNbDescriptiveAttributes()); //upper bound on the fraction number;
				}catch (Exception e2) {
					System.err.println("Error while setting the feature subset size!");
					System.err.println("The set of possible values include:");
					System.err.println("\t 0 or log for taking the log2,");
					System.err.println("\t sqrt for taking the sqrt,");
					System.err.println("\t integer for taking the absolute number of attributes,");
					System.err.println("\t double (0,1) for taking the fraction values.");
					System.exit(-1);
				}
			}
		}
		
//		if (getNbRandomAttrSelected() == 0)
//			fsize = (int) (Math.log(schema.getNbDescriptiveAttributes())/Math.log(2) + 1);
//		else fsize = getNbRandomAttrSelected();
		setNbRandomAttrSelected(fsize);
	}

	public void setNbRandomAttrSelected(int value) {
		m_SubsetSize = value;
		m_RandomAttrSelected.setValue(value+"");
	}

	public void setBagSelection(int value) {
		m_BagSelection.setInt(value);
	}

	public static boolean isPrintEnsembleModels( ){
		return m_PrintAllModels.getValue();
	}
	
	public static boolean isPrintEnsembleModelFiles( ){
		return m_PrintAllModelFiles.getValue();
	}
	
	public boolean isPrintEnsemblePaths( ){
		return m_PrintPaths.getValue();
	}

	public static boolean shouldOptimizeEnsemble( ){
		return m_EnsembleShouldOpt.getValue();
	}

	public boolean shouldWritePredictionsFromEnsemble(){
		return m_WriteEnsemblePredictions.getValue();
	}

	public static boolean shouldEstimateOOB( ){
		return m_EnsembleOOBestimate.getValue();
	}

	public void setOOBestimate(boolean value){
		m_EnsembleOOBestimate.setValue(value);
	}

	/** Do we want to use different random depth for different iterations of ensemble.
	 * Used in tree to rules optimization method. The MaxDepth of tree is used as average.
	 */
	public boolean isEnsembleRandomDepth() {
		return m_EnsembleRandomDepth.getValue();
	}
	
	/**
	 * Gets the size of bags for ensembles in a bagging scheme
	 * @return the bag size
	 */
	public int getEnsembleBagSize() {
		return m_EnsembleBagSize.getValue();
	}
	
	/**
	 * Sets the size of bags for ensembles in a bagging scheme
	 * @param value the size of the training set for individual bags
	 */
	public void setEnsembleBagSize(int value) {
		m_EnsembleBagSize.setValue(value);
	}
	
	

/***********************************************************************
 * Section: KNN                                                        *
 ***********************************************************************/

	INIFileSection m_SectionKNN;
	public static INIFileString kNN_k;
    public static INIFileString kNN_distance;
    public static INIFileString kNN_method;
    public static INIFileString kNN_distanceWeight;
    public static INIFileString kNN_attrWeight;

    public void setSectionKNNEnabled(boolean enable) {
		m_SectionKNN.setEnabled(enable);
	}

    public boolean isKNN(){
    	return m_SectionKNN.isEnabled();
    }
    
    public String getKnnMethod(){
        return this.kNN_method.getStringValue();
    }
	
//	INIFileSection m_SectionKNN;
//	public final static String[] kNN_METHODS = {"VP-tree", "KD-tree", "BrutForce"};
//	public final static int kNN_VP = 0;
//	public final static int kNN_KD = 1;
//	public final static int kNN_BF = 2;
//	
//	public final static String[] kNN_DISTANCES = {"Euclidean", "Manhattan", "Chebyshev"};
//	public final static int kNN_EUCLIDEAN = 0;
//	public final static int kNN_MANHATTAN = 1;
//	public final static int kNN_CHEBYSHEV = 2;
//	
//	public final static String[] kNN_DIST_WEIGHTS = {"None", "1/d", "1-d"};
//	public final static int kNN_DIST_WEIGHT_NONE = 0;
//	public final static int kNN_DIST_WEIGHT_INVERSE = 1;
//	public final static int kNN_DIST_WEIGHT_SUBSTRACT = 2;
//	
//	public static INIFileString m_kNN_k;
//	public static INIFileNominal m_kNNdistance;
//	public static INIFileNominal m_kNNmethod;
//	public static INIFileNominal m_kNNdistanceWeight;
//	public static INIFileString m_kNNattrWeight;
//	
//	public void setSectionKNNEnabled(boolean enable) {
//		m_SectionKNN.setEnabled(enable);
//	}
//	
//	public void setKNNNbNeighbors(String value){
//		m_kNN_k.setValue(value);
//	}
//	
//   public String getKnnNbNeighbors(){
//		return m_kNN_k.getStringValue();
//  }
//    
//	
//	public void setKNNMethod(String value){
//		m_kNNmethod.setValue(value);
//	}
//	
//    public int getKnnMethod(){
//		return m_kNNmethod.getValue();
//    }
//    
//    public String getKnnMethodName(){
//		return m_kNNmethod.getStringValue();
//    }
//    
//    public boolean isKNN(){
//    	return m_SectionKNN.isEnabled();
//    }
//    
//    public void setkNNDistance(String value){
//    	m_kNNdistance.setValue(value);
//    }
//    
//    public int getkNNDistance(){
//    	return m_kNNdistance.getValue();
//    }
//    
//    public void setkNNDistanceWeighting(String value){
//    	m_kNNdistanceWeight.setValue(value);
//    }
//    
//    public String getkNNDistanceName(){
//    	return m_kNNdistance.getStringValue();
//    }
//    
//    public int getkNNDistanceWeighting(){
//    	return m_kNNdistanceWeight.getValue();
//    }
//
//    public void setkNNAttributeWeighting(String value){
//    	m_kNNattrWeight.setValue(value);
//    }
//    
//    public String getkNNAttributeWeighting(){
//    	return m_kNNattrWeight.getValue();
//    }
    
/***********************************************************************
 * Section: KNN Trees                                                  *
 ***********************************************************************/

	INIFileSection m_SectionKNNT;
	public static INIFileInt kNNT_k;
	public static INIFileString kNNT_vectDist;
	public static INIFileBool kNNT_distWeighted;
	public static INIFileBool kNNT_normalized;
	public static INIFileBool kNNT_attrWeighted;
	
	
	public void setSectionKNNTEnabled(boolean enable) {
		m_SectionKNNT.setEnabled(enable);
	}

/***********************************************************************
 * Cross-validaiton                                                    *
 ***********************************************************************/

	public static boolean SHOW_XVAL_FOREST;
	public static boolean XVAL_OVERLAP = true;
	public static boolean IS_XVAL = false;

/***********************************************************************
 * Create the settings structure                                       *
 ***********************************************************************/

	public void create() {
		INIFileSection settings = new INIFileSection("General");
		settings.addNode(m_Verbose = new INIFileInt("Verbose", 1));
		settings.addNode(m_Compatibility = new INIFileNominal("Compatibility", COMPATIBILITY, COMPATIBILITY_LATEST));
		settings.addNode(m_RandomSeed = new INIFileString("RandomSeed", "0"));
		settings.addNode(m_ResourceInfoLoaded = new INIFileNominal("ResourceInfoLoaded", RESOURCE_INFO_LOAD, 1));

		INIFileSection data = new INIFileSection("Data");
		data.addNode(m_DataFile = new INIFileString("File", NONE));
		data.addNode(m_TestSet = new INIFileStringOrDouble("TestSet", NONE));
		data.addNode(m_PruneSet = new INIFileStringOrDouble("PruneSet", NONE));
		data.addNode(m_PruneSetMax = new INIFileStringOrInt("PruneSetMax", INFINITY_STRING));
		data.addNode(m_XValFolds = new INIFileStringOrInt("XVal"));
		m_XValFolds.setIntValue(10);
		data.addNode(m_RemoveMissingTarget = new INIFileBool("RemoveMissingTarget", false));
		data.addNode(m_NormalizeData = new INIFileNominal("NormalizeData", NORMALIZE_DATA_VALUES, 0));

		INIFileSection attrs = new INIFileSection("Attributes");
		attrs.addNode(m_Target = new INIFileString("Target", DEFAULT));
		attrs.addNode(m_Clustering = new INIFileString("Clustering", DEFAULT));
		attrs.addNode(m_Descriptive = new INIFileString("Descriptive", DEFAULT));
		attrs.addNode(m_Key = new INIFileString("Key", NONE));
		attrs.addNode(m_Disabled = new INIFileString("Disable", NONE));
		attrs.addNode(m_Weights = new INIFileNominalOrDoubleOrVector("Weights",	NORMALIZATIONS));
		m_Weights.setNominal(NORMALIZATION_DEFAULT);
		attrs.addNode(m_ClusteringWeights = new INIFileNominalOrDoubleOrVector("ClusteringWeights", EMPTY));
		m_ClusteringWeights.setDouble(1.0);
		m_ClusteringWeights.setArrayIndexNames(NUM_NOM_TAR_NTAR_WEIGHTS);
		attrs.addNode(m_ReduceMemoryNominal = new INIFileBool("ReduceMemoryNominalAttrs", false));

		m_SectionSIT = new INIFileSection("SIT");
		m_SectionSIT.addNode(m_MainTarget = new INIFileString("Main_target", DEFAULT));
		m_SectionSIT.addNode(m_Recursive = new INIFileBool("Recursive",false));
		m_SectionSIT.addNode(m_Search = new INIFileString("Search","OneTarget"));
		m_SectionSIT.addNode(m_Learner = new INIFileString("Learner","ClusLearner"));
		m_SectionSIT.addNode(m_Error = new INIFileString("Error","MSE"));
		m_SectionSIT.setEnabled(false);

		INIFileSection constr = new INIFileSection("Constraints");
		constr.addNode(m_SyntacticConstrFile = new INIFileString("Syntactic",	NONE));
		constr.addNode(m_MaxSizeConstr = new INIFileNominalOrIntOrVector("MaxSize", INFINITY));
		constr.addNode(m_MaxErrorConstr = new INIFileNominalOrDoubleOrVector("MaxError", INFINITY));
		constr.addNode(m_TreeMaxDepth = new INIFileInt("MaxDepth", -1));
		m_MaxSizeConstr.setNominal(0);
		m_MaxErrorConstr.setDouble(0);

		INIFileSection output = new INIFileSection("Output");
		output.addNode(m_ShowModels = new INIFileNominal("ShowModels", SHOW_MODELS, SHOW_MODELS_VALUES));
		output.addNode(m_OutTrainErr = new INIFileBool("TrainErrors", true));
		output.addNode(m_OutValidErr = new INIFileBool("ValidErrors", true));
		output.addNode(m_OutTestErr = new INIFileBool("TestErrors", true));
		output.addNode(m_OutFoldModels = new INIFileBool("AllFoldModels", true));
		output.addNode(m_OutFoldErr = new INIFileBool("AllFoldErrors", false));
		output.addNode(m_OutFoldData = new INIFileBool("AllFoldDatasets", false));
		output.addNode(m_ShowUnknown = new INIFileBool("UnknownFrequency", false));
		output.addNode(m_ShowBrFreq = new INIFileBool("BranchFrequency", false));
		output.addNode(m_ShowInfo = new INIFileNominal("ShowInfo", SHOW_INFO, SHOW_INFO_VALUES));
		output.addNode(m_PrintModelAndExamples = new INIFileBool("PrintModelAndExamples", false));
		output.addNode(m_WriteErrorFile = new INIFileBool("WriteErrorFile", false));
		output.addNode(m_WritePredictions = new INIFileNominal("WritePredictions", WRITE_PRED, WRITE_PRED_VALUES));
		// If this option name is to be changed, it must also be changed in testsets/iris-classify.s
		//output.addNode(m_ModelIDFiles = new INIFileBool("WriteModelIDFiles", false));
		output.addNode(m_ModelIDFiles = new INIFileBool("ModelIDFiles", false));
		output.addNode(m_WriteCurves = new INIFileBool("WriteCurves", false));
		output.addNode(m_OutputPythonModel = new INIFileBool("OutputPythonModel", false));
		output.addNode(m_OutputDatabaseQueries = new INIFileBool("OutputDatabaseQueries", false));

		INIFileSection nominal = new INIFileSection("Nominal");
		nominal.addNode(m_MEstimate = new INIFileDouble("MEstimate", 1.0));

		INIFileSection model = new INIFileSection("Model");
		model.addNode(m_MinW = new INIFileDouble("MinimalWeight", 2.0));
		model.addNode(m_MinNbEx = new INIFileInt("MinimalNumberExamples", 0));
		model.addNode(m_MinKnownW = new INIFileDouble("MinimalKnownWeight", 0));
		model.addNode(m_TuneFolds = new INIFileString("ParamTuneNumberFolds", "10"));
		model.addNode(m_ClassWeight = new INIFileNominalOrDoubleOrVector("ClassWeights", EMPTY));
		model.addNode(m_NominalSubsetTests = new INIFileBool("NominalSubsetTests", true));

		m_SectionTree = new INIFileSection("Tree");
		m_SectionTree.addNode(m_Heuristic = new INIFileNominal("Heuristic", HEURISTICS, 0));
		m_SectionTree.addNode(m_PruningMethod = new INIFileNominal("PruningMethod", PRUNING_METHODS, 0));
		m_SectionTree.addNode(m_M5PruningMult = new INIFileDouble("M5PruningMult", 2.0));
		m_SectionTree.addNode(m_1SERule = new INIFileBool("1-SE-Rule", false));
		m_SectionTree.addNode(m_FTest = new INIFileNominalOrDoubleOrVector("FTest", NONELIST));
		m_FTest.setDouble(1.0);
		m_SectionTree.addNode(m_BinarySplit = new INIFileBool("BinarySplit", true));
		m_SectionTree.addNode(m_RulesFromTree = new INIFileNominal("ConvertToRules", CONVERT_RULES, 0));
		m_SectionTree.addNode(m_AlternativeSplits = new INIFileBool("AlternativeSplits", false));
		m_SectionTree.addNode(m_TreeOptimize = new INIFileNominal("Optimize", TREE_OPTIMIZE_VALUES, TREE_OPTIMIZE_NONE));
		m_SectionTree.addNode(m_MSENominal = new INIFileBool("MSENominal", false));
		m_SectionTree.addNode(m_TreeSplitSampling = new INIFileInt("SplitSampling",0));
		m_TreeSplitSampling.setValueCheck(new IntRangeCheck(0,Integer.MAX_VALUE));
		
		// added by Eduardo Costa 06/06/2011
		m_SectionTree.addNode(m_InductionOrder = new INIFileNominal("InductionOrder", INDUCTION_ORDER, 0));
		
		// added by Eduardo Costa 29/09/2011
		m_SectionTree.addNode(m_EntropyType = new INIFileNominal("EntropyType", ENTROPY_TYPE, 0));
		m_SectionTree.addNode(m_ConsiderUnlableInstancesInIGCalc = new INIFileBool("ConsiderUnlableInstancesInIGCalc", false));		
		
		 
		
		
		
		m_SectionRules = new INIFileSection("Rules");
		m_SectionRules.addNode(m_CoveringMethod = new INIFileNominal("CoveringMethod", COVERING_METHODS, 0));
		m_SectionRules.addNode(m_PredictionMethod = new INIFileNominal("PredictionMethod", RULE_PREDICTION_METHODS, 0));
		m_SectionRules.addNode(m_RuleAddingMethod = new INIFileNominal("RuleAddingMethod", RULE_ADDING_METHODS, 0));
		m_SectionRules.addNode(m_CoveringWeight = new INIFileDouble("CoveringWeight", 0.1));
		m_SectionRules.addNode(m_InstCoveringWeightThreshold = new INIFileDouble("InstCoveringWeightThreshold", 0.1));
		m_SectionRules.addNode(m_MaxRulesNb = new INIFileInt("MaxRulesNb", 1000));
	    m_SectionRules.addNode(m_HeurDispOffset = new INIFileDouble("HeurDispOffset", 0.0));
		m_SectionRules.addNode(m_HeurCoveragePar = new INIFileDouble("HeurCoveragePar", 1.0));
		m_SectionRules.addNode(m_HeurRuleDistPar = new INIFileDouble("HeurRuleDistPar", 0.0));
		m_SectionRules.addNode(m_HeurPrototypeDistPar = new INIFileDouble("HeurPrototypeDistPar", 0.0));
		m_SectionRules.addNode(m_RuleSignificanceLevel = new INIFileDouble("RuleSignificanceLevel", 0.05));
		m_SectionRules.addNode(m_RuleNbSigAtts = new INIFileInt("RuleNbSigAtts", 0));
		m_SectionRules.addNode(m_ComputeDispersion = new INIFileBool("ComputeDispersion", false));
		m_SectionRules.addNode(m_VarBasedDispNormWeight = new INIFileDouble("VarBasedDispNormWeight", 4.0));
		m_SectionRules.addNode(m_DispersionWeights = new INIFileNominalOrDoubleOrVector("DispersionWeights", EMPTY));
		m_DispersionWeights.setArrayIndexNames(NUM_NOM_TAR_NTAR_WEIGHTS);
		m_DispersionWeights.setDoubleArray(FOUR_ONES);
		m_DispersionWeights.setArrayIndexNames(true);
		m_SectionRules.addNode(m_RandomRules = new INIFileInt("RandomRules", 0));
		m_SectionRules.addNode(m_RuleWiseErrors = new INIFileBool("PrintRuleWiseErrors", false));
		m_SectionRules.addNode(m_PrintAllRules = new INIFileBool("PrintAllRules", true));
		m_SectionRules.addNode(m_constrainedToFirstAttVal = new INIFileBool("ConstrainedToFirstAttVal", false));
		m_SectionRules.addNode(m_OptDEPopSize = new INIFileInt("OptDEPopSize", 500));
		m_SectionRules.addNode(m_OptDENumEval = new INIFileInt("OptDENumEval", 10000));
		m_SectionRules.addNode(m_OptDECrossProb = new INIFileDouble("OptDECrossProb", 0.3));
		m_SectionRules.addNode(m_OptDEWeight = new INIFileDouble("OptDEWeight", 0.5));
		m_SectionRules.addNode(m_OptDESeed = new INIFileInt("OptDESeed", 0));
		m_SectionRules.addNode(m_OptDERegulPower = new INIFileDouble("OptDERegulPower", 1.0));
		m_SectionRules.addNode(m_OptDEProbMutationZero = new INIFileDouble("OptDEProbMutationZero", 0.0));
		m_SectionRules.addNode(m_OptDEProbMutationNonZero = new INIFileDouble("OptDEProbMutationNonZero", 0.0));
		m_SectionRules.addNode(m_OptRegPar = new INIFileDouble("OptRegPar", 0.0));
		m_SectionRules.addNode(m_OptNbZeroesPar = new INIFileDouble("OptNbZeroesPar", 0.0));
		m_SectionRules.addNode(m_OptRuleWeightThreshold = new INIFileDouble("OptRuleWeightThreshold", 0.1));
		m_SectionRules.addNode(m_OptLossFunction = new INIFileNominal("OptDELossFunction",OPT_LOSS_FUNCTIONS, 0));
		m_SectionRules.addNode(m_OptDefaultShiftPred = new INIFileBool("OptDefaultShiftPred", true));
		m_SectionRules.addNode(m_OptAddLinearTerms = new INIFileNominal("OptAddLinearTerms", OPT_GD_ADD_LINEAR_TERMS, OPT_GD_ADD_LIN_NO));
		m_SectionRules.addNode(m_OptNormalizeLinearTerms = new INIFileNominal("OptNormalizeLinearTerms", OPT_LINEAR_TERM_NORM_VALUES, OPT_LIN_TERM_NORM_YES));
		m_SectionRules.addNode(m_OptLinearTermsTruncate = new INIFileBool("OptLinearTermsTruncate", true));
		m_SectionRules.addNode(m_OptOmitRulePredictions = new INIFileBool("OptOmitRulePredictions", true));
		m_SectionRules.addNode(m_OptWeightGenerality = new INIFileBool("OptWeightGenerality", false));
//		m_SectionRules.addNode(m_OptNormalization = new INIFileBool("OptNormalization", true));
		m_SectionRules.addNode(m_OptNormalization = new INIFileNominal("OptNormalization", OPT_NORMALIZATION, OPT_NORMALIZATION_YES));
		m_SectionRules.addNode(m_OptHuberAlpha = new INIFileDouble("OptHuberAlpha", 0.9));
		m_SectionRules.addNode(m_OptGDMaxIter = new INIFileInt("OptGDMaxIter", 1000));
//		m_SectionRules.addNode(m_OptGDLossFunction = new INIFileNominal("OptGDLossFunction", GD_LOSS_FUNCTIONS, 0));
		m_SectionRules.addNode(m_OptGDGradTreshold = new INIFileDouble("OptGDGradTreshold", 1));
		m_SectionRules.addNode(m_OptGDStepSize = new INIFileDouble("OptGDStepSize", 0.1));
		m_SectionRules.addNode(m_OptGDIsDynStepsize = new INIFileBool("OptGDIsDynStepsize", true));
		m_SectionRules.addNode(m_OptGDMaxNbWeights = new INIFileInt("OptGDMaxNbWeights", 0));
		m_SectionRules.addNode(m_OptGDEarlyStopAmount = new INIFileDouble("OptGDEarlyStopAmount", 0.0));
		m_SectionRules.addNode(m_OptGDEarlyStopTreshold = new INIFileDouble("OptGDEarlyStopTreshold", 1.1));
		m_SectionRules.addNode(m_OptGDNbOfStepSizeReduce = new INIFileStringOrInt("OptGDNbOfStepSizeReduce", INFINITY_STRING));
		m_SectionRules.addNode(m_OptGDExternalMethod = new INIFileNominal("OptGDExternalMethod",GD_EXTERNAL_METHOD_VALUES, 0));
		m_SectionRules.addNode(m_OptGDMTGradientCombine = new INIFileNominal("OptGDMTGradientCombine",OPT_GD_MT_COMBINE_GRADIENTS, 0));
		m_SectionRules.addNode(m_OptGDNbOfTParameterTry = new INIFileInt("OptGDNbOfTParameterTry",1));
		m_SectionRules.addNode(m_OptGDEarlyTTryStop = new INIFileBool("OptGDEarlyTTryStop",true));
		m_SectionRules.setEnabled(false);

		m_SectionHierarchical = new INIFileSection("Hierarchical");
		m_SectionHierarchical.addNode(m_HierType = new INIFileNominal("Type", HIERTYPES, 0));
		m_SectionHierarchical.addNode(m_HierDistance = new INIFileNominal("Distance", HIERDIST, 0));
		m_SectionHierarchical.addNode(m_HierWType = new INIFileNominal("WType", HIERWEIGHT, 0));
		m_SectionHierarchical.addNode(m_HierWParam = new INIFileDouble("WParam", 0.75));
		m_SectionHierarchical.addNode(m_HierSep = new INIFileString("HSeparator", "."));
		m_SectionHierarchical.addNode(m_HierEmptySetIndicator = new INIFileString("EmptySetIndicator", "n"));
		m_SectionHierarchical.addNode(m_HierOptimizeErrorMeasure = new INIFileNominal("OptimizeErrorMeasure", HIERMEASURES, HIERMEASURE_POOLED_AUPRC));
		m_SectionHierarchical.addNode(m_DefinitionFile = new INIFileString("DefinitionFile", NONE));
		m_SectionHierarchical.addNode(m_HierNoRootPreds = new INIFileBool("NoRootPredictions", false));
		m_SectionHierarchical.addNode(m_HierPruneInSig = new INIFileDouble("PruneInSig", 0.0));
		m_SectionHierarchical.addNode(m_HierUseBonferroni = new INIFileBool("Bonferroni", false));
		m_SectionHierarchical.addNode(m_HierSingleLabel = new INIFileBool("SingleLabel", false));
		m_SectionHierarchical.addNode(m_CalErr = new INIFileBool("CalculateErrors", true));
		m_SectionHierarchical.addNode(m_HierClassThreshold = new INIFileNominalOrDoubleOrVector("ClassificationThreshold", NONELIST));
		m_HierClassThreshold.setNominal(0);
		m_SectionHierarchical.addNode(m_RecallValues = new INIFileNominalOrDoubleOrVector("RecallValues", NONELIST));
		m_RecallValues.setNominal(0);		
		m_SectionHierarchical.addNode(m_HierEvalClasses = new INIFileString("EvalClasses", NONE));
		m_SectionHierarchical.addNode(m_HierUseMEstimate = new INIFileBool("MEstimate", false));
		m_SectionHierarchical.setEnabled(false);
		
		
		m_SectionILevelC = new INIFileSection("ILevelC");
		m_SectionILevelC.addNode(m_ILevelCAlpha = new INIFileDouble("Alpha", 0.5));
		m_SectionILevelC.addNode(m_ILevelCFile = new INIFileString("File", NONE));
		m_SectionILevelC.addNode(m_ILevelNbRandomConstr = new INIFileInt("NbRandomConstraints", 0));
		m_SectionILevelC.addNode(m_ILevelCCOPKMeans = new INIFileBool("RunCOPKMeans", false));
		m_SectionILevelC.addNode(m_ILevelCMPCKMeans = new INIFileBool("RunMPCKMeans", false));
		m_SectionILevelC.setEnabled(false);

		m_SectionBeam = new INIFileSection("Beam");
		m_SectionBeam.addNode(m_SizePenalty = new INIFileDouble("SizePenalty", 0.1));
		m_SectionBeam.addNode(m_BeamWidth = new INIFileInt("BeamWidth", 10));
		m_SectionBeam.addNode(m_BeamBestN = new INIFileInt("BeamBestN", 5));
		m_SectionBeam.addNode(m_TreeMaxSize = new INIFileInt("MaxSize", -1));
		m_SectionBeam.addNode(m_BeamAttrHeuristic = new INIFileNominal("AttributeHeuristic", HEURISTICS, 0));
		m_SectionBeam.addNode(m_FastBS = new INIFileBool("FastSearch", true));
		m_SectionBeam.addNode(m_BeamPostPrune = new INIFileBool("PostPrune", false));
		m_SectionBeam.addNode(m_BMRemoveEqualHeur = new INIFileBool("RemoveEqualHeur", false));
		m_SectionBeam.addNode(m_BeamSimilarity = new INIFileDouble("BeamSimilarity", 0.0));
		m_SectionBeam.addNode(m_BSortTrainParameter = new INIFileBool("BeamSortOnTrainParameteres", false));
		m_SectionBeam.addNode(m_BeamSyntacticConstrFile = new INIFileString("DistSyntacticConstr",NONE));
		m_SectionBeam.addNode(m_BeamToForest = new INIFileBool("BeamToForest", false));
		m_SectionBeam.setEnabled(false);

		//added by elisa 1/08/2006
		m_SectionExhaustive = new INIFileSection("Exhaustive");
		m_SectionExhaustive.addNode(m_Exhaustive = new INIFileBool("Exhaustive", true));
		m_SectionExhaustive.addNode(m_StartTreeCpt = new INIFileInt("StartTreeCpt", 0));
		m_SectionExhaustive.addNode(m_StartItemCpt = new INIFileInt("StartItemCpt", 0));
		m_SectionExhaustive.setEnabled(false);

		m_SectionTimeSeries = new INIFileSection("TimeSeries");
		m_SectionTimeSeries.addNode(m_TimeSeriesDistance=new INIFileNominal("DistanceMeasure", TIME_SERIES_DISTANCE_MEASURE,0));
		m_SectionTimeSeries.addNode(m_TimeSeriesHeuristicSampling=new INIFileNominal("PrototypeComlexity", TIME_SERIES_PROTOTYPE_COMPLEXITY,0));
		m_SectionTimeSeries.setEnabled(false);

		m_SectionPhylogeny = new INIFileSection("Phylogeny");
		m_SectionPhylogeny.addNode(m_PhylogenyDM=new INIFileNominal("DistanceMeasure", PHYLOGENY_DISTANCE_MEASURE,0));
		m_SectionPhylogeny.addNode(m_PhylogenyCriterion=new INIFileNominal("OptimizationCriterion", PHYLOGENY_CRITERION,0));
		m_SectionPhylogeny.addNode(m_PhylogenySequence=new INIFileNominal("Sequence", PHYLOGENY_SEQUENCE,0));
		m_SectionPhylogeny.addNode(m_PhylogenyDistanceMatrix = new INIFileString("DistanceMatrix", "dist"));
		m_SectionPhylogeny.addNode(m_PhylogenyEntropyVsRootStop = new INIFileDouble("EntropyVsRootStopCriterion", 0));
		m_SectionPhylogeny.addNode(m_PhylogenyDistancesVsRootStop = new INIFileDouble("SumPWDistancesVsRootStopCriterion", 0));
		m_SectionPhylogeny.addNode(m_PhylogenyEntropyVsParentStop = new INIFileDouble("EntropyVsParentStopCriterion", 0));
		m_SectionPhylogeny.addNode(m_PhylogenyDistancesVsParentStop = new INIFileDouble("SumPWDistancesVsParentStopCriterion", 0));
		m_SectionPhylogeny.setEnabled(false);

		m_SectionEnsembles = new INIFileSection("Ensemble");
		m_SectionEnsembles.addNode(m_NbBags = new INIFileNominalOrIntOrVector("Iterations", NONELIST));
		m_SectionEnsembles.addNode(m_EnsembleMethod =new INIFileNominal("EnsembleMethod", ENSEMBLE_TYPE,0));
		m_SectionEnsembles.addNode(m_ClassificationVoteType =new INIFileNominal("VotingType", VOTING_TYPE,0));
		m_SectionEnsembles.addNode(m_RandomAttrSelected = new INIFileString("SelectRandomSubspaces", "0"));
		m_SectionEnsembles.addNode(m_PrintAllModels = new INIFileBool("PrintAllModels", false));
		m_SectionEnsembles.addNode(m_PrintAllModelFiles = new INIFileBool("PrintAllModelFiles", false));
		m_SectionEnsembles.addNode(m_PrintPaths = new INIFileBool("PrintPaths", false));
		m_SectionEnsembles.addNode(m_EnsembleShouldOpt = new INIFileBool("Optimize", false));
		m_SectionEnsembles.addNode(m_EnsembleOOBestimate = new INIFileBool("OOBestimate", false));
		m_SectionEnsembles.addNode(m_FeatureRanking = new INIFileBool("FeatureRanking", false));
		m_SectionEnsembles.addNode(m_WriteEnsemblePredictions = new INIFileBool("WriteEnsemblePredictions", false));
		m_SectionEnsembles.addNode(m_EnsembleRandomDepth = new INIFileBool("EnsembleRandomDepth", false));
		m_SectionEnsembles.addNode(m_BagSelection = new INIFileNominalOrIntOrVector("BagSelection", NONELIST));
		m_BagSelection.setInt(-1);
		m_SectionEnsembles.addNode(m_EnsembleBagSize = new INIFileInt("BagSize", 0));
		m_EnsembleBagSize.setValueCheck(new IntRangeCheck(0, Integer.MAX_VALUE));
		m_SectionEnsembles.setEnabled(false);

//		m_SectionKNN = new INIFileSection("kNN");
//		m_SectionKNN.addNode(kNN_k = new INIFileInt("k", 3));
//		m_SectionKNN.addNode(kNN_vectDist = new INIFileString("VectorDistance", "Euclidian"));
//		m_SectionKNN.addNode(kNN_distWeighted = new INIFileBool("DistanceWeighted", false));
//		m_SectionKNN.addNode(kNN_normalized = new INIFileBool("Normalizing", true));
//		m_SectionKNN.addNode(kNN_attrWeighted = new INIFileBool("AttributeWeighted", false));
//		m_SectionKNN.setEnabled(false);

		m_SectionKNN = new INIFileSection("kNN");
		m_SectionKNN.addNode(kNN_k = new INIFileString("k", "1,3"));
		m_SectionKNN.addNode(kNN_method = new INIFileString("method","kd-tree"));
		m_SectionKNN.addNode(kNN_distance = new INIFileString("distance","euclidean"));
		m_SectionKNN.addNode(kNN_distanceWeight = new INIFileString("distanceWeighting","none"));
		m_SectionKNN.addNode(kNN_attrWeight = new INIFileString("attributeWeighting","none"));
		m_SectionKNN.setEnabled(false);
		
//		m_SectionKNN = new INIFileSection("kNN");
//		m_SectionKNN.addNode(m_kNN_k = new INIFileString("k", "1,3"));
//		m_SectionKNN.addNode(m_kNNmethod = new INIFileNominal("SearchMethod", kNN_METHODS, 0));
//		m_SectionKNN.addNode(m_kNNdistance = new INIFileNominal("Distance", kNN_DISTANCES, 0));
//		m_SectionKNN.addNode(m_kNNdistanceWeight = new INIFileNominal("DistanceWeighting", kNN_DIST_WEIGHTS, 0));
//		m_SectionKNN.addNode(m_kNNattrWeight = new INIFileString("attributeWeighting","none"));

		m_SectionKNNT = new INIFileSection("kNNTree");
		m_SectionKNNT.addNode(kNNT_k = new INIFileInt("k", 3));
		m_SectionKNNT.addNode(kNNT_vectDist = new INIFileString("VectorDistance", "Euclidian"));
		m_SectionKNNT.addNode(kNNT_distWeighted = new INIFileBool("DistanceWeighted", false));
		m_SectionKNNT.addNode(kNNT_normalized = new INIFileBool("Normalizing", true));
		m_SectionKNNT.addNode(kNNT_attrWeighted = new INIFileBool("AttributeWeighted", false));
		m_SectionKNNT.setEnabled(false);
		
		INIFileSection exper = new INIFileSection("Experimental");
		exper.addNode(m_SetsData = new INIFileInt("NumberBags", 25));
		exper.addNode(m_ShowForest = new INIFileBool("XValForest", false));
		exper.setEnabled(false);

		m_Ini.addNode(settings);
		m_Ini.addNode(data);
		m_Ini.addNode(attrs);
		m_Ini.addNode(constr);
		m_Ini.addNode(output);
		m_Ini.addNode(nominal);
		m_Ini.addNode(model);
		m_Ini.addNode(m_SectionTree);
		m_Ini.addNode(m_SectionRules);
		m_Ini.addNode(m_SectionHierarchical);
		m_Ini.addNode(m_SectionILevelC);
		m_Ini.addNode(m_SectionBeam);
		m_Ini.addNode(m_SectionExhaustive);
		m_Ini.addNode(m_SectionTimeSeries);
		m_Ini.addNode(m_SectionPhylogeny);
		m_Ini.addNode(m_SectionEnsembles);
		m_Ini.addNode(m_SectionKNN);
		m_Ini.addNode(m_SectionKNNT);
		m_Ini.addNode(exper);
		m_Ini.addNode(m_SectionSIT);
	}

	public void initNamedValues() {
		m_TreeMaxDepth.setNamedValue(-1, "Infinity");
		m_TreeMaxSize.setNamedValue(-1, "Infinity");
		m_TreeSplitSampling.setNamedValue(0, "None");
	}

	public void updateTarget(ClusSchema schema) {
		if (checkHeuristic("SSPD")) {
			schema.addAttrType(new IntegerAttrType("SSPD"));
			int nb = schema.getNbAttributes();
			m_Target.setValue(String.valueOf(nb));
		}
	}

	public void initialize(CMDLineArgs cargs, boolean loads) throws IOException {
		create();
		initNamedValues();
		if (cargs != null) preprocess(cargs);
		if (loads) {
			try {
				String fname = getFileAbsolute(getAppName() + ".s");
				m_Ini.load(fname, '%');
			} catch (FileNotFoundException e) {
				System.out.println("No settings file found");
			}
		}
		if (cargs != null) process(cargs);
		updateDataFile(getAppName() + ".arff");
		initHierarchical();
	}

	public void preprocess(CMDLineArgs cargs) {
	}

	public void process(CMDLineArgs cargs) {
		if (cargs.hasOption("target")) {
			m_Target.setValue(cargs.getOptionValue("target"));
		}
		if (cargs.hasOption("disable")) {
			String disarg = cargs.getOptionValue("disable");
			String orig = m_Disabled.getValue();
			if (StringUtils.unCaseCompare(orig, NONE)) {
				m_Disabled.setValue(disarg);
			} else {
				m_Disabled.setValue(orig + "," + disarg);
			}
		}
		if (cargs.hasOption("silent")) {
			VERBOSE = 0;
		}
	}

	public void update(ClusSchema schema) {
		setFTest(getFTest());
		MINIMAL_WEIGHT = getMinimalWeight();
		SHOW_UNKNOWN_FREQ = isShowUnknown();
		SHOW_XVAL_FOREST = isShowXValForest();
		SHOW_BRANCH_FREQ = isShowBranchFreq();
		ONE_NOMINAL = (schema.getNbNominalTargetAttributes() == 1 && schema.getNbNumericTargetAttributes() == 0);
		SIZE_PENALTY = getSizePenalty();
		BEAM_WIDTH = m_BeamWidth.getValue();
		BEAM_SIMILARITY = getBeamSimilarity();
		BEAM_SYNT_DIST_CONSTR = hasBeamConstraintFile();
		VERBOSE = m_Verbose.getValue();
		if (isEnsembleMode())updateNbRandomAttrSelected(schema);
	}

	public int getVerbose() {
		return m_Verbose.getValue();
	}

	public void updateDisabledSettings() {
		int pruning = getPruningMethod();
		m_M5PruningMult.setEnabled(pruning == PRUNING_METHOD_M5 || pruning == PRUNING_METHOD_M5_MULTI);
		m_PruneSetMax.setEnabled(!m_PruneSet.isString(NONE));
		m_1SERule.setEnabled(pruning == PRUNING_METHOD_GAROFALAKIS_VSB);
		int heur = getHeuristic();
		m_FTest.setEnabled(heur == HEURISTIC_SSPD || heur == HEURISTIC_VARIANCE_REDUCTION);
		if (ResourceInfo.isLibLoaded()) m_ResourceInfoLoaded.setSingleValue(RESOURCE_INFO_LOAD_YES);
		else m_ResourceInfoLoaded.setSingleValue(RESOURCE_INFO_LOAD_NO);
	}

	public void show(PrintWriter where) throws IOException {
		updateDisabledSettings();
		//For TreeToRules PredictionMethod might have been temporarily put to DecisionList instead of some other
		boolean tempInduceParamNeeded = m_ruleInduceParamsDisabled; // They were changed in the first place
		if (getCoveringMethod() == Settings.COVERING_METHOD_RULES_FROM_TREE && tempInduceParamNeeded)
			returnRuleInduceParams();
		m_Ini.save(where);
		if (getCoveringMethod() == Settings.COVERING_METHOD_RULES_FROM_TREE && tempInduceParamNeeded)
			disableRuleInduceParams(); 
	}

	public String getFileAbsolute(String fname) {
		// System.out.println("Dir name: '"+m_DirName+"'");
		// System.out.println("File name: '"+fname+"'");
		if (m_DirName == null) {
			return fname;
		} else {
			if (FileUtil.isAbsolutePath(fname)) {
				return fname;
			} else {
				return m_DirName + File.separator + fname;
			}
		}
	}

	public PrintWriter getFileAbsoluteWriter(String fname) throws FileNotFoundException {
		String path = getFileAbsolute(fname);
		return new PrintWriter(new OutputStreamWriter(new FileOutputStream(path)));
	}

}