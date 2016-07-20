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

package clus.ext.ensembles;

import java.io.IOException;
import java.util.ArrayList;
import java.io.*;

import jeans.resource.ResourceInfo;

import clus.Clus;
import clus.algo.*;
import clus.algo.tdidt.*;
import clus.main.*;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.model.modelio.ClusModelCollectionIO;
import clus.data.rows.RowData;
import clus.data.rows.TupleIterator;
import clus.data.type.*;
import clus.error.ClusErrorList;
import clus.selection.*;
import clus.tools.optimization.GDProbl;
import clus.util.ClusException;
import clus.util.ClusRandom;


public class ClusEnsembleInduce extends ClusInductionAlgorithm {
	Clus m_BagClus;
	static ClusAttrType[] m_RandomSubspaces;
	ClusForest m_OForest;//Forest with the original models
	ClusForest m_DForest;//Forest with stumps (default models)
	static int m_Mode;
	long m_SummTime = 0;
	
	//Memory optimization
	static boolean m_OptMode;
	ClusEnsembleInduceOptimization m_Optimization;
	
	//Output ensemble at different values
	int[] m_OutEnsembleAt;//sorted values (ascending)!
	static int m_NbMaxBags;

	//Out-Of-Bag Error Estimate
	ClusOOBErrorEstimate m_OOBEstimation;
	
	//Feature Ranking via Random Forests
	boolean m_FeatRank;
	ClusEnsembleFeatureRanking m_FeatureRanking;
	
    /** Random tree depths for different iterations, used for tree to rules optimization procedures.
     * This is static because we want different tree depths for different folds. */
//	static protected Random m_randTreeDepth = new Random(0);

	public ClusEnsembleInduce(ClusSchema schema, Settings sett, Clus clus) throws ClusException, IOException {
		super(schema, sett);
		initialize(schema, sett, clus);
	}

	public ClusEnsembleInduce(ClusInductionAlgorithm other, Clus clus) throws ClusException, IOException {
		super(other);
		initialize(getSchema(), getSettings(), clus);
	}

	public void initialize(ClusSchema schema, Settings sett, Clus clus) throws ClusException, IOException {
		m_BagClus = clus;
		m_Mode = getStatManager().getMode();
		//optimize if not XVAL and HMC
		m_OptMode = (Settings.shouldOptimizeEnsemble() && !Settings.IS_XVAL && ((m_Mode == ClusStatManager.MODE_HIERARCHICAL)||(m_Mode == ClusStatManager.MODE_REGRESSION) || (m_Mode == ClusStatManager.MODE_CLASSIFY)));
		m_OutEnsembleAt = sett.getNbBaggingSets().getIntVectorSorted();
		m_NbMaxBags = m_OutEnsembleAt[m_OutEnsembleAt.length-1];
		m_FeatRank = sett.shouldPerformRanking() && !Settings.IS_XVAL;
		if (m_FeatRank && !Settings.shouldEstimateOOB()){
			System.err.println("For Feature Ranking OOB estimate of error should also be performed.");
			System.err.println("OOB Error Estimate is set to true.");
			Settings.m_EnsembleOOBestimate.setValue(true);
		}
		if (Settings.shouldEstimateOOB())m_OOBEstimation = new ClusOOBErrorEstimate(m_Mode);
		if (m_FeatRank)	{
			m_FeatureRanking = new ClusEnsembleFeatureRanking();
			m_FeatureRanking.initializeAttributes(schema.getDescriptiveAttributes());
		}
	}

	/** Train a decision tree ensemble with an algorithm given in settings  */
	public void induceAll(ClusRun cr) throws ClusException, IOException {
		System.out.println("Memory And Time Optimization = " + m_OptMode);
		System.out.println("Out-Of-Bag Estimate of the error = " + Settings.shouldEstimateOOB());
		System.out.println("\tPerform Feature Ranking = " + m_FeatRank);
		switch (cr.getStatManager().getSettings().getEnsembleMethod()){
		case 0: {	//Bagging
			System.out.println("Ensemble Method: Bagging");
			induceBagging(cr);
			break;
			}
		case 1: {	//RForest
			System.out.println("Ensemble Method: Random Forest");
			induceBagging(cr);
			break;
			}
		case 2: {	//RSubspaces
			System.out.println("Ensemble Method: Random Subspaces");
			induceSubspaces(cr);
			break;
			}
		case 3: {	//Bagging Subspaces
			System.out.println("Ensemble Method: Bagging of Subspaces");
			induceBaggingSubspaces(cr);
			break;
			}
		case 5: {  //RForest without bagging (setting: RFeatSelection)
			System.out.println("Ensemble Method: Random Forest without Bagging");
			induceRForestNoBagging(cr);
			break;
		}
		case 6: {  //PERT in combination with bagging
			System.out.println("Ensemble Method: PERT (in combination with Bagging)");
			induceBagging(cr);
			break;
		}
		}
		if (m_FeatRank) {
			m_FeatureRanking.sortFeatureRanks();
			m_FeatureRanking.printRanking(cr.getStatManager().getSettings().getFileAbsolute(cr.getStatManager().getSettings().getAppName()));
			m_FeatureRanking.convertRanksByName();
		}
		postProcessForest(cr);

//		This section is for calculation of the similarity in the ensemble
//		ClusBeamSimilarityOutput bsimout = new ClusBeamSimilarityOutput(cr.getStatManager().getSettings());
//		bsimout.appendToFile(m_OForest.getModels(), cr);
	}

	
	public ClusModel induceSingleUnpruned(ClusRun cr) throws ClusException, IOException {
		ClusRun myRun = new ClusRun(cr);
		induceAll(myRun);
		ClusModelInfo info = myRun.getModelInfo(ClusModel.ORIGINAL);
		return info.getModel();
	}

	// this ensemble method builds random forests (i.e. chooses the best test from a subset of attributes at each node), 
	// but does not construct bootstrap replicates of the dataset
	public void induceRForestNoBagging(ClusRun cr) throws ClusException, IOException {
		m_OForest = new ClusForest(getStatManager());
		m_DForest = new ClusForest(getStatManager());
		long summ_time = 0; // = ResourceInfo.getTime();
		TupleIterator train_iterator = null; // = train set iterator
		TupleIterator test_iterator = null; // = test set iterator

		if (m_OptMode){
			train_iterator = cr.getTrainIter();
			if (m_BagClus.hasTestSet()){
				test_iterator = cr.getTestSet().getIterator();
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
					m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());
			} else {
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
					m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());	
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());
			}
			m_Optimization.initPredictions(m_OForest.getStat());
		}
		for (int i = 1; i <= m_NbMaxBags; i++) {
			long one_bag_time = ResourceInfo.getTime();
			if (Settings.VERBOSE > 0) System.out.println("Bag: " + i);
			ClusRun crSingle = new ClusRun(cr.getTrainingSet(), cr.getSummary());
			DepthFirstInduce ind;
			if (getSchema().isSparse()) {
				ind = new DepthFirstInduceSparse(this);
			}
			else {
				ind = new DepthFirstInduce(this);
			}
			ind.initialize();
			crSingle.getStatManager().initClusteringWeights();
			ClusModel model = ind.induceSingleUnpruned(crSingle);
			summ_time += ResourceInfo.getTime() - one_bag_time;
			if (m_OptMode){
				//for i == 1 [i.e. the first run] it will initialize the predictions
				if (i == 1) m_Optimization.initModelPredictionForTuples(model, train_iterator, test_iterator);
				else m_Optimization.addModelPredictionForTuples(model, train_iterator, test_iterator, i);
			}
			else{
				m_OForest.addModelToForest(model);
//				ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
//				m_DForest.addModelToForest(defmod);
			}
			
			//Valid only when test set is supplied
			if (m_OptMode && (i != m_NbMaxBags) && checkToOutEnsemble(i)){
				crSingle.setInductionTime(summ_time);
				postProcessForest(crSingle);
				crSingle.setTestSet(cr.getTestIter());
				crSingle.setTrainingSet(cr.getTrainingSet());
				outputBetweenForest(crSingle, m_BagClus, "_" + i +"_");
			}
			crSingle.deleteData();
			crSingle.setModels(new ArrayList());
		}
	}
	
	
	public void induceSubspaces(ClusRun cr) throws ClusException, IOException {
		m_OForest = new ClusForest(getStatManager());
		m_DForest = new ClusForest(getStatManager());
		long summ_time = 0; // = ResourceInfo.getTime();
		TupleIterator train_iterator = null; // = train set iterator
		TupleIterator test_iterator = null; // = test set iterator

		if (m_OptMode){
			train_iterator = cr.getTrainIter();
			if (m_BagClus.hasTestSet()){
				test_iterator = cr.getTestSet().getIterator();
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
					m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());

			} else {
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
					m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());
			}
			m_Optimization.initPredictions(m_OForest.getStat());
		}
		for (int i = 1; i <= m_NbMaxBags; i++) {
			long one_bag_time = ResourceInfo.getTime();
			if (Settings.VERBOSE > 0) System.out.println("Bag: " + i);
			ClusRun crSingle = new ClusRun(cr.getTrainingSet(), cr.getSummary());
			ClusEnsembleInduce.setRandomSubspaces(cr.getStatManager().getSchema().getDescriptiveAttributes(), cr.getStatManager().getSettings().getNbRandomAttrSelected());
			DepthFirstInduce ind;
			if (getSchema().isSparse()) {
				ind = new DepthFirstInduceSparse(this);
			}
			else {
				ind = new DepthFirstInduce(this);
			}
			ind.initialize();
			crSingle.getStatManager().initClusteringWeights();
			ClusModel model = ind.induceSingleUnpruned(crSingle);
			summ_time += ResourceInfo.getTime() - one_bag_time;
			if (m_OptMode){
				//for i == 1 [i.e. the first run] it will initialize the predictions
				if (i == 1) m_Optimization.initModelPredictionForTuples(model, train_iterator, test_iterator);
				else m_Optimization.addModelPredictionForTuples(model, train_iterator, test_iterator, i);
			}
			else{
				m_OForest.addModelToForest(model);
//				ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
//				m_DForest.addModelToForest(defmod);
			}
			//Valid only when test set is supplied
			if (m_OptMode && (i != m_NbMaxBags) && checkToOutEnsemble(i)){
				crSingle.setInductionTime(summ_time);
				postProcessForest(crSingle);
				crSingle.setTestSet(cr.getTestIter());
				crSingle.setTrainingSet(cr.getTrainingSet());
				outputBetweenForest(crSingle, m_BagClus, "_" + i +"_");
			}
			crSingle.deleteData();
			crSingle.setModels(new ArrayList());
		}
	}
	

	public void induceBagging(ClusRun cr) throws ClusException, IOException {
		int nbrows = cr.getTrainingSet().getNbRows();
		((RowData)cr.getTrainingSet()).addIndices(); // necessary to be able to print paths
		if ((RowData)cr.getTestSet() != null) {
			((RowData)cr.getTestSet()).addIndices();
		}
		
		m_OForest = new ClusForest(getStatManager());
		m_DForest = new ClusForest(getStatManager());
		TupleIterator train_iterator = null; // = train set iterator
		TupleIterator test_iterator = null; // = test set iterator
		OOBSelection oob_total = null; // = total OOB selection
		OOBSelection oob_sel = null; // = current OOB selection
		if (m_OptMode){
			train_iterator = cr.getTrainIter();
			if (m_BagClus.hasTestSet()){
				test_iterator = cr.getTestSet().getIterator();
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
					m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());
			}else {
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
					m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());
			}
			m_Optimization.initPredictions(m_OForest.getStat());
		}
		// We store the old maxDepth to this if needed. Thus we get the right depth to .out files etc.
		int origMaxDepth = -1;
		if (getSettings().isEnsembleRandomDepth()) {
			// Random depth for the ensembles
			// The original Max depth is used as the average
			origMaxDepth = getSettings().getTreeMaxDepth();
		}
		BaggingSelection msel = null;
		int[] bagSelections = getSettings().getBagSelection().getIntVectorSorted();
		// bagSelections is either -1, 0, a value in [1,Iterations], or 2 values in [1,Iterations]
		if (bagSelections[0] == -1) {
			// normal bagging procedure
			for (int i = 1; i <= m_NbMaxBags; i++) {
			    msel = new BaggingSelection(nbrows, getSettings().getEnsembleBagSize());
				if (Settings.shouldEstimateOOB()){		//OOB estimate is on
					oob_sel = new OOBSelection(msel);
					if (i == 1){ //initialization
						oob_total = new OOBSelection(msel);
					}else oob_total.addToThis(oob_sel);
				}
				induceOneBag(cr, i, origMaxDepth, oob_sel, oob_total, train_iterator, test_iterator, msel);
			}
		}
		else if (bagSelections[0] == 0) {
			// we assume that files _bagI.model exist, for I=1..m_NbMaxBags and build the forest from them
			makeForestFromBags(cr, train_iterator, test_iterator);
		}
		else {
			// only one or a range of bag needs to be computed (and the model output) e.g. because we want to run the forest in parallel,
			// or because we want to add a number of trees to an existing forest.
			for (int i=1; i<bagSelections[0]; i++) {
				// we eventually want the same bags as when computing them sequentially.
				msel = new BaggingSelection(nbrows,getSettings().getEnsembleBagSize());
			}
			for (int i=bagSelections[0]; i<=bagSelections[1]; i++) {
				msel = new BaggingSelection(nbrows,getSettings().getEnsembleBagSize());
				if (Settings.shouldEstimateOOB()){		//OOB estimate is on
					oob_sel = new OOBSelection(msel);
				}
				induceOneBag(cr, i, origMaxDepth, oob_sel, oob_total, train_iterator, test_iterator, msel);
			}
		}

		// Restore the old maxDepth
		if (origMaxDepth != -1) {
			getSettings().setTreeMaxDepth(origMaxDepth);
		}

	}

	public void induceOneBag(ClusRun cr, int i, int origMaxDepth, OOBSelection oob_sel, OOBSelection oob_total, TupleIterator train_iterator, TupleIterator test_iterator, BaggingSelection msel) throws ClusException, IOException {
		if (getSettings().isEnsembleRandomDepth()) {
			// Set random tree max depth
			getSettings().setTreeMaxDepth(GDProbl.randDepthWighExponentialDistribution(
//					m_randTreeDepth.nextDouble(),
					ClusRandom.nextDouble(ClusRandom.RANDOM_INT_RANFOR_TREE_DEPTH), origMaxDepth));
		}

		long one_bag_time = ResourceInfo.getTime();
		if (Settings.VERBOSE > 0) System.out.println("Bag: " + i);
		ClusRun crSingle = m_BagClus.partitionDataBasic(cr.getTrainingSet(),msel,cr.getSummary(),i);
		DepthFirstInduce ind;
		if (getSchema().isSparse()) {
			ind = new DepthFirstInduceSparse(this);
		}
		else {
			ind = new DepthFirstInduce(this);
		}
		ind.initialize();
		crSingle.getStatManager().initClusteringWeights();
		ClusModel model = ind.induceSingleUnpruned(crSingle);
		m_SummTime += ResourceInfo.getTime() - one_bag_time;

//		OOB estimate for the parallel implementation is done in makeForestFromBags method
		if (Settings.shouldEstimateOOB() && (getSettings().getBagSelection().getIntVectorSorted()[0] == -1)){		
			m_OOBEstimation.updateOOBTuples(oob_sel, (RowData)cr.getTrainingSet(), model);
		}
		
		if (m_FeatRank){
			ArrayList<String> attests = new ArrayList<String>();
			m_FeatureRanking.fillWithAttributesInTree((ClusNode)model, attests);
			RowData tdata = (RowData)((RowData)cr.getTrainingSet()).deepCloneData();
			double oob_err = m_FeatureRanking.calcAverageError((RowData)tdata.selectFrom(oob_sel), model);
			for (int z = 0; z < attests.size(); z++){//for the attributes that appear in the tree
				String current_attribute = (String)attests.get(z);
				double [] info = m_FeatureRanking.getAttributeInfo(current_attribute);
				double type = info[0];
				double position = info[1];
				RowData permuted = m_FeatureRanking.createRandomizedOOBdata(oob_sel, (RowData)tdata.selectFrom(oob_sel), (int)type, (int)position);
				if (ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION){//increase in error rate (oob_err -> MSError)
					info[2] += (m_FeatureRanking.calcAverageError((RowData)permuted, model) - oob_err)/oob_err;
				}else if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY){//decrease in accuracy (oob_err -> Accuracy)
					info[2] += (oob_err - m_FeatureRanking.calcAverageError((RowData)permuted, model))/oob_err;
				}
				m_FeatureRanking.putAttributeInfo(current_attribute, info);
				
			}
		}

		if (m_OptMode){
			if (i == 1) m_Optimization.initModelPredictionForTuples(model, train_iterator, test_iterator);
			else m_Optimization.addModelPredictionForTuples(model, train_iterator, test_iterator, i);
		}
		else{
			m_OForest.addModelToForest(model);
			
//			ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
//			m_DForest.addModelToForest(defmod);
		}
		
		// printing paths taken by each example in each tree (used in ICDM'11 paper on "Random forest based feature induction")				
		if (getSettings().isPrintEnsemblePaths()) {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("tree_"+i+".path")));		
		((ClusNode)model).numberCompleteTree();
		((ClusNode)model).printPaths(pw, "", "", (RowData)cr.getTrainingSet(), oob_sel, false);
		if ((RowData)cr.getTestSet() != null) ((ClusNode)model).printPaths(pw, "", "", (RowData)cr.getTestSet(), null, true);
		}
		
		//Valid only when test set is supplied
		if (checkToOutEnsemble(i) && (getSettings().getBagSelection().getIntVectorSorted()[0] == -1)){
			crSingle.setInductionTime(m_SummTime);
			postProcessForest(crSingle);
			if (Settings.shouldEstimateOOB())
				m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData)cr.getTrainingSet(), m_BagClus, "_"+ i +"_");
			if (m_OptMode && (i != m_NbMaxBags)){
				crSingle.setTestSet(cr.getTestIter());
				crSingle.setTrainingSet(cr.getTrainingSet());
				outputBetweenForest(crSingle, m_BagClus, "_"+ i +"_");
			}
		}
		if ((getSettings().getBagSelection().getIntVectorSorted()[0] != -1) || (getSettings().isPrintEnsembleModelFiles())) {
			ClusModelCollectionIO io = new ClusModelCollectionIO();
			ClusModelInfo orig_info = crSingle.addModelInfo("Original");
			orig_info.setModel(model);
			m_BagClus.saveModels(crSingle, io);
			io.save(m_BagClus.getSettings().getFileAbsolute(cr.getStatManager().getSettings().getAppName() + "_bag"+ i +".model"));
		}
		crSingle.deleteData();
		crSingle.setModels(new ArrayList());
	}
		
	
	
	public void makeForestFromBags(ClusRun cr, TupleIterator train_iterator, TupleIterator test_iterator) throws ClusException, IOException {
		try {
			m_OForest = new ClusForest(getStatManager());
			m_DForest = new ClusForest(getStatManager());
			OOBSelection oob_total = null; // = total OOB selection
			OOBSelection oob_sel = null; // = current OOB selection
			BaggingSelection msel = null;
			System.out.println("Start loading models");
			
			for (int i=1; i<=m_NbMaxBags; i++) {
				System.out.println("Loading model for bag " + i);			
				ClusModelCollectionIO io = ClusModelCollectionIO.load(m_BagClus.getSettings().getFileAbsolute(getSettings().getAppName() + "_bag" + i + ".model"));
				ClusModel orig_bag_model = io.getModel("Original");
				if (orig_bag_model == null) {
					throw new ClusException(cr.getStatManager().getSettings().getAppName() + "_bag" + i + ".model file does not contain model named 'Original'");
				}
				if (m_OptMode){
					//the first run will initialize the predictions
					if (i == 1) m_Optimization.initModelPredictionForTuples(orig_bag_model, train_iterator, test_iterator);
					else m_Optimization.addModelPredictionForTuples(orig_bag_model, train_iterator, test_iterator, i);
				}else{
					m_OForest.addModelToForest(orig_bag_model);					
				}
				if (Settings.shouldEstimateOOB()){		//OOB estimate is on
					// the same bags will be generated for the corresponding models!!!
					msel = new BaggingSelection(cr.getTrainingSet().getNbRows(), getSettings().getEnsembleBagSize());
					oob_sel = new OOBSelection(msel);
					if (i == 1){ //initialization
						oob_total = new OOBSelection(msel);
					}else oob_total.addToThis(oob_sel);
					m_OOBEstimation.updateOOBTuples(oob_sel, (RowData)cr.getTrainingSet(), orig_bag_model);
				}
				
				if (checkToOutEnsemble(i)){
					postProcessForest(cr);
					if (Settings.shouldEstimateOOB()){
						m_OOBEstimation.postProcessForestForOOBEstimate(cr, oob_total, (RowData)cr.getTrainingSet(), m_BagClus, "_"+ i +"_");
					}
					if (m_OptMode && i != m_NbMaxBags){
						outputBetweenForest(cr, m_BagClus, "_"+ i +"_");
					}
				}
				cr.setModels(new ArrayList());// do not store the models
							
// Dragi, IJS - we don't store the predictions of the default models 
/*				ClusModel def_bag_model = io.getModel("Default");
				if (def_bag_model == null) {
					throw new ClusException(cr.getStatManager().getSettings().getAppName() + "_bag" + i + ".model file does not contain model named 'Default'");
				}
				m_DForest.addModelToForest(def_bag_model);
*/				
			}
		}
		catch (ClassNotFoundException e) {
			throw new ClusException("Error: not all of the _bagX.model files were found");
		}
	}


	
	public void induceBaggingSubspaces(ClusRun cr) throws ClusException, IOException {
		int nbrows = cr.getTrainingSet().getNbRows();
		m_OForest = new ClusForest(getStatManager());
		m_DForest = new ClusForest(getStatManager());
		long summ_time = 0; // = ResourceInfo.getTime();
		TupleIterator train_iterator = null; // = train set iterator
		TupleIterator test_iterator = null; // = test set iterator
		OOBSelection oob_total = null; // = total OOB selection
		OOBSelection oob_sel = null; // = current OOB selection

		if (m_OptMode){
			train_iterator = cr.getTrainIter();
			if (m_BagClus.hasTestSet()){
				test_iterator = cr.getTestSet().getIterator();
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
				m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows()+cr.getTestSet().getNbRows());
			}else {
				if (m_Mode == ClusStatManager.MODE_HIERARCHICAL || m_Mode == ClusStatManager.MODE_REGRESSION)
				m_Optimization = new ClusEnsembleInduceOptRegHMLC(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());
				if (m_Mode == ClusStatManager.MODE_CLASSIFY)
					m_Optimization = new ClusEnsembleInduceOptClassification(train_iterator, test_iterator, cr.getTrainingSet().getNbRows());
			}
			m_Optimization.initPredictions(m_OForest.getStat());
		}
		for (int i = 1; i <= m_NbMaxBags; i++) {
			long one_bag_time = ResourceInfo.getTime();
			if (Settings.VERBOSE > 0) System.out.println("Bag: " + i);
			BaggingSelection msel = new BaggingSelection(nbrows, getSettings().getEnsembleBagSize());
			ClusRun crSingle = m_BagClus.partitionDataBasic(cr.getTrainingSet(),msel,cr.getSummary(),i);
			ClusEnsembleInduce.setRandomSubspaces(cr.getStatManager().getSchema().getDescriptiveAttributes(), cr.getStatManager().getSettings().getNbRandomAttrSelected());
			DepthFirstInduce ind;
			if (getSchema().isSparse()) {
				ind = new DepthFirstInduceSparse(this);
			}
			else {
				ind = new DepthFirstInduce(this);
			}
			ind.initialize();
			crSingle.getStatManager().initClusteringWeights();
			ind.initializeHeuristic();
			ClusModel model = ind.induceSingleUnpruned(crSingle);
			summ_time += ResourceInfo.getTime() - one_bag_time;

			if (Settings.shouldEstimateOOB()){		//OOB estimate is on
				oob_sel = new OOBSelection(msel);
				if (i == 1) oob_total = new OOBSelection(msel);
				else oob_total.addToThis(oob_sel);
				m_OOBEstimation.updateOOBTuples(oob_sel, (RowData)cr.getTrainingSet(), model);
			}

			if (m_OptMode){
				if (i == 1) m_Optimization.initModelPredictionForTuples(model, train_iterator, test_iterator);
				else m_Optimization.addModelPredictionForTuples(model, train_iterator, test_iterator, i);
			}
			else{
				m_OForest.addModelToForest(model);
				ClusModel defmod = ClusDecisionTree.induceDefault(crSingle);
				m_DForest.addModelToForest(defmod);
			}

			if (checkToOutEnsemble(i)){
				crSingle.setInductionTime(summ_time);
				postProcessForest(crSingle);
				if (Settings.shouldEstimateOOB())
					m_OOBEstimation.postProcessForestForOOBEstimate(crSingle, oob_total, (RowData)cr.getTrainingSet(), m_BagClus, "_" + i + "_");
				if (m_OptMode && (i != m_NbMaxBags)){
					crSingle.setTestSet(cr.getTestIter());
					crSingle.setTrainingSet(cr.getTrainingSet());
					outputBetweenForest(crSingle, m_BagClus, "_"+ i +"_");
				}
			}
			crSingle.deleteData();
			crSingle.setModels(new ArrayList());
		}
	}

	
	//Checks whether we reached a limit
	public boolean checkToOutEnsemble(int idx){
		for (int i = 0; i < m_OutEnsembleAt.length; i++)
			if  (m_OutEnsembleAt[i] == idx) return true;
		return false;
	}

	public void postProcessForest(ClusRun cr) throws ClusException{
		ClusModelInfo def_info = cr.addModelInfo("Default");
		if (m_OptMode) m_DForest = null;
		def_info.setModel(ClusDecisionTree.induceDefault(cr)); 

		ClusModelInfo orig_info = cr.addModelInfo("Original");
		orig_info.setModel(m_OForest);
	
		//Application of Thresholds for HMC
		if (cr.getStatManager().getMode() == ClusStatManager.MODE_HIERARCHICAL){
			double[] thresholds = cr.getStatManager().getSettings().getClassificationThresholds().getDoubleVector();
			//setting the printing preferences in the HMC mode
			m_OForest.setPrintModels(Settings.isPrintEnsembleModels());
			if (!m_OptMode) m_DForest.setPrintModels(Settings.isPrintEnsembleModels());
			if (thresholds != null){
				for (int i = 0; i < thresholds.length; i++){
					ClusModelInfo pruned_info = cr.addModelInfo("T("+thresholds[i]+")");
					ClusForest new_forest = m_OForest.cloneForestWithThreshold(thresholds[i]);
					new_forest.setPrintModels(Settings.isPrintEnsembleModels());
					pruned_info.setShouldWritePredictions(false);
					pruned_info.setModel(new_forest);
				}
			}
		}

		// If we want to convert trees to rules but not use
		// any of the rule learning tehniques anymore (if CoveringMethod != RulesFromTree)
		if (getSettings().rulesFromTree() != Settings.CONVERT_RULES_NONE && 
			getSettings().getCoveringMethod() != Settings.COVERING_METHOD_RULES_FROM_TREE) {
			m_OForest.convertToRules(cr, false);
		}
	}
	
	public void outputBetweenForest(ClusRun cr, Clus cl, String addname)	throws IOException, ClusException {
		Settings sett = cr.getStatManager().getSettings();
		ClusSchema schema = cr.getStatManager().getSchema();
		ClusOutput output = new ClusOutput(sett.getAppName() + addname +".out", schema, sett);
		ClusSummary summary = cr.getSummary();
		getStatManager().computeTrainSetStat((RowData)cr.getTrainingSet());
		cl.calcError(cr, null,null); // Calc error
		if (summary != null) {
			for (int i = ClusModel.ORIGINAL; i < cr.getNbModels(); i++) {
				ClusModelInfo summ_info = cr.getModelInfo(i);
				ClusErrorList test_err = summ_info.getTestError();
				summ_info.setTestError(test_err);
			}
		}
		cl.calcExtraTrainingSetErrors(cr);
		output.writeHeader();
		output.writeOutput(cr, true, getSettings().isOutTrainError());
		output.close();
		cl.getClassifier().saveInformation(sett.getAppName());
		ClusModelCollectionIO io = new ClusModelCollectionIO();
		cl.saveModels(cr, io);
		io.save(cl.getSettings().getFileAbsolute(cr.getStatManager().getSettings().getAppName() + addname+".model"));
	}

	/**
	 * Maximum number of bags for memory optimization
	 */
	public static int getMaxNbBags(){
		return ClusEnsembleInduce.m_NbMaxBags;
	}
	
	/** Selects random subspaces.
	 * @param attrs For which attributes
	 * @param select How many.
	 */
	public static ClusAttrType[] selectRandomSubspaces(ClusAttrType[] attrs, int select){
		int origsize = attrs.length;
		int[] samples = new int [origsize];
		int rnd;
		boolean randomize = true;
		int i = 0;
		while (randomize) {
			rnd = ClusRandom.nextInt(ClusRandom.RANDOM_SELECTION, origsize);
			if (samples[rnd] == 0) {
				samples[rnd]++;
				i++;
			}
			if ( i == select)
				randomize = false;
		}
		ClusAttrType[] result = new ClusAttrType[select];
		int res = 0;
		for (int k = 0; k < origsize; k++){
			if (samples[k] !=0){
				result[res] = attrs[k];
				res++;
			}
		}
//		System.out.println(java.util.Arrays.toString(samples));
		return result;
//		setRandomSubspaces(result);
	}

	public static ClusAttrType[] getRandomSubspaces(){
		return m_RandomSubspaces;
	}

	public static void setRandomSubspaces(ClusAttrType[] attrs, int select){
		m_RandomSubspaces = ClusEnsembleInduce.selectRandomSubspaces(attrs, select);
	}

	/** Memory optimization
	 */
	public static boolean isOptimized(){
		return m_OptMode;
	}
	
	public ClusEnsembleFeatureRanking getEnsembleFeatureRanking(){
		return m_FeatureRanking;
	}
}