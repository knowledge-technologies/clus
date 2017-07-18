package si.ijs.kt.clus.algo.rules.probabilistic;

//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;

//import org.apache.commons.math3.distribution.PoissonDistribution;

//import si.ijs.kt.clus.Clus;
//import si.ijs.kt.clus.algo.rules.ClusRule;
//import si.ijs.kt.clus.algo.rules.ClusRuleFromTreeInduce;
//import si.ijs.kt.clus.algo.rules.ClusRuleSet;
//import si.ijs.kt.clus.data.rows.DataTuple;
//import si.ijs.kt.clus.data.rows.RowData;
//import si.ijs.kt.clus.data.rows.TupleIterator;
//import si.ijs.kt.clus.data.type.ClusAttrType;
//import si.ijs.kt.clus.data.ClusSchema;
//import si.ijs.kt.clus.error.common.ClusErrorList;
//import si.ijs.kt.clus.error.RMSError;
//import si.ijs.kt.clus.main.ClusRun;
//import si.ijs.kt.clus.main.ClusStatManager;
//import si.ijs.kt.clus.model.ClusModel;
//import si.ijs.kt.clus.model.ClusModelInfo;
//import si.ijs.kt.clus.model.processor.ModelProcessorCollection;
//import si.ijs.kt.clus.statistic.ClusStatistic;
//import si.ijs.kt.clus.util.ClusException;
//import si.ijs.kt.clus.util.ClusFormat;


public class ClusRuleProbabilisticRuleSetInduceOLD //extends ClusRuleFromTreeInduce 
{
//	// parameters
//	long m_seed = 1;
//	
//	HashMap<Integer, ArrayList<Integer>> m_cardinalityCounts;
//	
//	PoissonDistribution m_poissonRuleSetLength = null;
//	PoissonDistribution m_poissonCardinality = null;
//	PoissonDistribution m_poissonRuleSampler = null;
//	Random m_randGen;
//	 
//	int m_maxRuleCardinality = 1;
//	int maxRuleSetLength = -1;
//	int numberOfPoissonIterations = -1;
//	ClusRuleSet m_initialRuleSet = null;
//	
//	RowData m_trainingData = null;
//	RowData m_validationData = null;
//	
//	ClusRun m_mainClusRun = null;
//	
//	// debug
//	String m_folderName = "rules.debug";
//	
//	
//	public ClusRuleProbabilisticRuleSetInduceOLD(ClusSchema schema, Settings sett, Clus clus)
//			throws ClusException, IOException {
//		super(schema, sett, clus);
//		
//		if (sett.hasRandomSeed())
//		{
//			m_seed = (long)sett.getRandomSeed();
//		}
//		
//		m_randGen = new Random(m_seed);
//	}
// 	
//	void initializeRuleParameters()
//	{
//		// get parameters
//		maxRuleSetLength = getSettings().getMaxRulesNb();
//		m_maxRuleCardinality = getSettings().getMaxRuleCardinality();
//		numberOfPoissonIterations = getSettings().getMaxPoissonIterations();
//		
//		// calculate counts of cardinality for rule set
//		m_cardinalityCounts = CalculateCounts(m_initialRuleSet);
//
//		System.out.println(String.format("Maximum rule set length: %s", maxRuleSetLength == Integer.MAX_VALUE ? "Maximum available" : maxRuleSetLength));
//
//		if (maxRuleSetLength == Integer.MAX_VALUE)
//		{
//			// default max number of rules in rule set
//			maxRuleSetLength = (int)(m_initialRuleSet.getModelSize() / getSettings().getNbBaggingSets().getInt());
//			
//			System.err.println(String.format("Defaulted to (number_of_initial_rules/ensemble_size): %s", maxRuleSetLength));
//		}
//		
//		System.out.println(String.format("Maximum rule cardinality: %s", m_maxRuleCardinality == Integer.MAX_VALUE ? "Maximum available" : m_maxRuleCardinality));
//		if (m_maxRuleCardinality == Integer.MAX_VALUE)
//		{
//			// take cardinality of rules that are most frequent
//			Object maxKey=null;
//			Integer maxValue = Integer.MIN_VALUE; 
//			//Integer highestKey = Integer.MIN_VALUE;
//			for(Map.Entry<Integer, ArrayList<Integer>> entry : m_cardinalityCounts.entrySet()) {
//				if(entry.getValue().size() > maxValue) {
//			         maxValue = entry.getValue().size();
//			         maxKey = entry.getKey();
//			     }
//			}
//			
//			m_maxRuleCardinality = (int)maxKey;
//			
//			System.err.println(String.format("Defaulted to most frequent cardinality available: %s", m_maxRuleCardinality));
//		}
//		
//		
//		// get Poisson samplers
//		m_poissonRuleSetLength = new PoissonDistribution(maxRuleSetLength, numberOfPoissonIterations);
//		m_poissonCardinality = new PoissonDistribution(m_maxRuleCardinality, numberOfPoissonIterations);
//		
//		m_poissonRuleSetLength.reseedRandomGenerator(m_seed);
//		m_poissonCardinality.reseedRandomGenerator(m_seed);
//	}
//	
//	public int randInt(int min_allowed, int max_allowed) 
//	{
//	    int randomNum = m_randGen.nextInt((max_allowed - min_allowed) + 1) + min_allowed;
//
//	    return randomNum;
//	}
//	
//	// validationRatio = validation_data/all_data
//	void SplitData(ClusRun cr, double validationRatio)
//	{
//		// splitting initial dataset into two parts 
//		RowData all_data = (RowData) cr.getTrainingSet();
//		
//		int validationSize = (int)(all_data.getNbRows() * validationRatio);
//		int trainSize = all_data.getNbRows() - validationSize;
//		
//		if (trainSize < 1.0) System.err.println("Validation set is too big!");		
//		System.out.println(String.format("Splitting learning data: Train set: %s examples | Validation set: %s examples", trainSize, validationSize));
//		
//		
//		RowData validationData = new RowData(getSchema());
//		RowData trainData = new RowData(getSchema());
//		
//		// randomly take examples from dataset ant place them into validation set
//		int newIndex = -1;
//		ArrayList<Integer> validationExamples = new ArrayList<Integer>();
//		ArrayList<Integer> trainingExamples = new ArrayList<Integer>();
//
//		while(validationData.getNbRows() < validationSize)
//		{
//			newIndex = randInt(0, all_data.getNbRows() - 1);
//			if (!validationExamples.contains(newIndex))
//			{
//				validationData.add(all_data.getTuple(newIndex).cloneTuple());
//				validationExamples.add(newIndex);
//			}
//		}
//
//		for (int i = 0; i < all_data.getNbRows(); i++)
//		{
//			if (!trainingExamples.contains(i) && !validationExamples.contains(i))
//			{
//				trainData.add(all_data.getTuple(i).cloneTuple());
//				trainingExamples.add(i);
//			}
//		}
//
//		m_trainingData = trainData;
//		m_validationData = validationData;
//	}
//	
// 	ClusRuleSet induceInitialRuleSet() throws ClusException, IOException
//	{
//		int tmpVerbose = Settings.enableVerbose(0);
//		
//		// induce ensemble of trees, convert to rules (from FIRE)	
//		if (getSettings().getVerbose() >= 1) 
//		{
//			System.out.println("Inducing random forest for initial rule set");
//			System.out.println("-----------------------------------");
//		}
//
//		// create new run so that it doesn't mix with rule model
//		ClusRun forestRun = new ClusRun(m_mainClusRun);
//		
//		// induce ensemble
//		super.induceAll(forestRun);
//		
//		if (getSettings().getVerbose() >= 1) 
//		{
//			System.out.println("-----------------------------------");
//		}
//		
//		// get default and PCT ensemble original model and add them to initial Clus run
//		ClusModelInfo ensemble_model_info_default = forestRun.getModelInfo(ClusModel.DEFAULT);
//		m_mainClusRun.addModelInfo(ensemble_model_info_default);
//		
//		ClusModelInfo ensemble_model_info_original = forestRun.getModelInfo(ClusModel.ORIGINAL);
//		m_mainClusRun.addModelInfo(ensemble_model_info_original);
//
//		// get initial rule set
//		ClusModelInfo rules_model_info = forestRun.getModelInfo(ClusModel.RULES);
//		ClusRuleSet rules_model = (ClusRuleSet)rules_model_info.getModel();
//
//		// return VERBOSE to its initial value
//		Settings.enableVerbose(tmpVerbose);
//		
//		if (rules_model.getModelSize() == 0)
//		{
//			System.err.println(String.format("No rules have been induced! Decrease validation set size (current value: %s).", getSettings().getValidationSetPercentage()));
//		}
//		
//		return rules_model;
//	}
//	
//	public void induceAll(ClusRun mainClusRun) throws ClusException, IOException
//	{
//		m_mainClusRun = mainClusRun;
//		
//		// split data into train and validation sets
//		SplitData(mainClusRun, getSettings().getValidationSetPercentage());
//		
//		// set training data for all: ensemble creation and for final evaluation
//		m_mainClusRun.setTrainingSet(m_trainingData);
//		
//		// get initial rules
//		m_initialRuleSet = induceInitialRuleSet(); 
//		
//
//		// save to disk
//		debug_RemoveExistingData(m_folderName);
//		debug_PrintInitialRules(m_initialRuleSet, m_folderName); 
//		
//		// initialize variables
//		initializeRuleParameters();				
//		
//		// sample rule sets
//		ClusRun sampledRuleSetsRun = sampleRuleSets();
//		ClusModelInfo bestRuleModel = evaluateRuleSets(sampledRuleSetsRun);
//		
//		bestRuleModel.setName("Rules");
//		
//		// so that we dont get validation errors 
//		bestRuleModel.setTrainError(null);
//		ClusRuleSet model = (ClusRuleSet)bestRuleModel.getModel();
//		model.setError(null, ClusModel.TRAIN);
//		
//		m_mainClusRun.addModelInfo(bestRuleModel);
//	}
//	
//	ClusModelInfo evaluateRuleSets(ClusRun sampledRuleSetsRun)
//	{
//		// select the best rule set by means of RMSE
//		
//		int bestModel = 0;
//		
//		// add error measures to all models
//		for (int i = 0; i < sampledRuleSetsRun.getNbModels(); i++) 
//		{
//			ClusModelInfo info = sampledRuleSetsRun.getModelInfo(i);
//			ClusRuleSet ruleset = (ClusRuleSet) info.getModel();
//			
//			
//			// create error to evaluate the target attributes predictions in the rule sets
//			ClusErrorList error = new ClusErrorList();
//			error.addError(new RMSError(error, getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET)));
//
//			// only interested in the test error but using TRAIN structure so that we dont have to implement setTestSet(ClusData data) later in the evaluation
//			ruleset.setError(error, ClusModel.TRAIN);
//			info.setTrainError(error);
//		}
//		
//		bestModel = calcError(sampledRuleSetsRun);
//		
//		System.out.println("Best rule set: " + sampledRuleSetsRun.getModelName(bestModel));
//		
//		return sampledRuleSetsRun.getModelInfo(bestModel);
//	}
//	
//	int calcError(ClusRun sampledRuleSetsRun)
//	{
//		int bestModel = 0;
//		double bestModelError = Integer.MAX_VALUE;
//		double tmp;
//		
//		sampledRuleSetsRun.setTrainingSet(m_validationData);
//
//		if (Settings.VERBOSE > 0) System.out.println("Computing validation error for all rule sets");
//		
//		sampledRuleSetsRun.copyAllModelsMIs();
//
//		calcError(sampledRuleSetsRun.getTrainIter(), ClusModelInfo.TRAIN_ERR, sampledRuleSetsRun);
//		
//		// find the model with the lowest RMSE
//		for (int i = 0; i < sampledRuleSetsRun.getNbModels(); i++)
//		{
//			RMSError rset_error = (RMSError) sampledRuleSetsRun.getModelInfo(i).getTrainingError().getError(0); // RMSE
//			if ((tmp = rset_error.getModelError()) < bestModelError)
//			{
//				bestModel = i;
//				bestModelError = tmp;
//			}
//		}
//		
//		return bestModel;
//	}
//
//	
//	/** Compute either test or train error */
//	void calcError(TupleIterator iter, int type, ClusRun cr) 
//	{
//		try
//		{	
//			iter.init();
//			ClusSchema mschema = iter.getSchema();
//			cr.initModelProcessors(type, mschema);
//	
//			
//			ModelProcessorCollection allcoll = cr.getAllModelsMI().getAddModelProcessors(type);
//			DataTuple tuple = iter.readTuple();
//			while (tuple != null) {
//				allcoll.exampleUpdate(tuple);
//				for (int i = 0; i < cr.getNbModels(); i++) {
//					ClusModelInfo mi = cr.getModelInfo(i);
//					if (mi != null && mi.getModel() != null) {
//						ClusModel model = mi.getModel();
//						ClusStatistic pred = model.predictWeighted(tuple);
//						ClusErrorList err = mi.getError(type);
//						
//						if (err != null)
//							err.addExample(tuple, pred);
//						ModelProcessorCollection coll = mi.getModelProcessors(type);
//						if (coll != null) {
//							if (coll.needsModelUpdate()) {
//								model.applyModelProcessors(tuple, coll);
//								coll.modelDone();
//							}
//							coll.exampleUpdate(tuple, pred);
//						}
//					}
//				}
//				allcoll.exampleDone();
//				tuple = iter.readTuple();
//			}
//			iter.close();
//			cr.termModelProcessors(type);		
//		}
//		catch (Exception ex)
//		{
//			System.err.println(ex.getMessage());
//		}
//	}
//
//	ClusRun sampleRuleSets()
//	{
//		ClusRun samplingRun = new ClusRun(m_mainClusRun);
//		
//		// sample rule lists
//		for (int i = 0; i < getSettings().getNumberOfSampledRuleSets(); i++)
//		{
//			ClusRuleSet sampledRuleSet = sampleSingleRuleSet(m_mainClusRun.getStatManager(), i);
//		
//			// add model to run
//			ClusModelInfo tmpModelInfo = samplingRun.addModelInfo(i);
//			tmpModelInfo.setModel(sampledRuleSet);
//			tmpModelInfo.setName("Rule Set " + (i+1));
//			
//			//write sampledDecisionList to file rules.debug/rules_i.out
//			debug_PrintRuleSet(sampledRuleSet, m_folderName, String.format("unordered_list_%s.txt", (i + 1)), true);
//		}
//		
//		System.out.println("Number of rule sets sampled: " + samplingRun.getNbModels());
//		
//		return samplingRun;
//	}
//	
//	ClusRuleSet sampleSingleRuleSet(ClusStatManager statManager, int currentSamplingCount)
//	{		
//		// sample decision list length and truncate so that the decision list cannot be larger than the number of rules in initial rule set
//		int dlLength = Integer.MAX_VALUE;
//		while (dlLength > m_initialRuleSet.getModelSize() || dlLength < 1)
//		{
//			dlLength = m_poissonRuleSetLength.sample();
//		}
//
//		ClusRuleSet tmpRuleSet = new ClusRuleSet(statManager); 				// Create new rule set
//		ArrayList<ClusRule> tmpRuleList = new ArrayList<ClusRule>();		// Create new temporary list of rules
//		ArrayList<Integer> alreadyUsedRules = new ArrayList<Integer>();		// Using this in order not to repeat the rules
//		HashMap<Integer, ArrayList<Integer>> tmpCurrentCardinalityCounts = (HashMap<Integer, ArrayList<Integer>>) m_cardinalityCounts.clone(); // Create a copy of available cardinalities
//		
//		// sample #dlLength rules from initialRuleSet
//		int tmpCardinality = -1;
//		int tmpRuleWithCardinality = -1;
//		
//		while (tmpRuleList.size() < dlLength) // sample rules until the rule set length is as sampled 
//		{
//			// sample a rule cardinality
//			tmpCardinality = m_poissonCardinality.sample();
//			
//			if (tmpCurrentCardinalityCounts.containsKey(tmpCardinality))
//			{
//				// cardinality exists; sample a rule with this cardinality from existing initial rules
//				ArrayList<Integer> rulesWithSampledCardinality = m_cardinalityCounts.get(tmpCardinality);
//				
//				// initialize Poisson rule sampler for the sampled cardinality with the average number of rules with it (cardinality)
//				m_poissonRuleSampler = new PoissonDistribution(rulesWithSampledCardinality.size()/2, numberOfPoissonIterations); 
//				m_poissonRuleSampler.reseedRandomGenerator(m_seed + currentSamplingCount);
//				
//
//				
//				tmpRuleWithCardinality = Integer.MAX_VALUE; // sample the rule
//				int stoppingCriterion = numberOfPoissonIterations;
//				while (
//						(tmpRuleWithCardinality >= rulesWithSampledCardinality.size() 
//						|| tmpRuleWithCardinality < 0 
//						|| alreadyUsedRules.contains(rulesWithSampledCardinality.get(tmpRuleWithCardinality)))
//						&& stoppingCriterion > 0
//					  )
//				{
//					 tmpRuleWithCardinality = m_poissonRuleSampler.sample();
//					 stoppingCriterion--;
//				}
//				
//				// the while loop has been forcefully terminated so we don't add anything
//				if (stoppingCriterion > 0) {
//					tmpRuleList.add(m_initialRuleSet.getRule(rulesWithSampledCardinality.get(tmpRuleWithCardinality)).cloneRule()); // add rule to list of rules for later use
//					alreadyUsedRules.add(rulesWithSampledCardinality.get(tmpRuleWithCardinality)); // mark rule as used
//				}
//			}
//		}
//
//		// Make UNORDERED rule set. 
//		// Ordered rule set does not make sense because the rules were sampled according to Poisson's distribution and we have no interpretation ground for ordered rule sets
//		// Rule set should use coverage weighted predictions
//		int i = 0;
//		RowData trainData = (RowData) m_trainingData.cloneData();
//		RowData uncoveredData = (RowData) trainData.cloneData();
//		
//		while ((uncoveredData.getNbRows() > 0) && (i < dlLength)) { 		// If there is nothing more to cover, we do not use any more rules -> can result in rule set smaller than the number of sampled rules
//			ClusRule tRule = tmpRuleList.get(i);
//			
//			RowData coveredData = tRule.computeCovered(trainData); 			// get covered data
//			tRule.setVisitor(coveredData);
//			tRule.setTargetStat(createTotalTargetStat(coveredData));		// calculate target statistic
//			tRule.setClusteringStat(createTotalClusteringStat(coveredData));// calculate clustering statistic
//			tRule.setVisitor(null);
//
//			tRule.computePrediction(); 										// calculate predictions for each rule
//			
//			tmpRuleSet.add(tRule);
//			
//			uncoveredData = tRule.removeCovered(uncoveredData);				// default rule will cover only uncovered data
//			i++;
//		}
//		
//		// The default rule
//		ClusStatistic uncoveredDataStatistic;
//		if (uncoveredData.getNbRows() > 0) {
//			uncoveredDataStatistic = createTotalTargetStat(uncoveredData);
//			uncoveredDataStatistic.calcMean();
//		} 
//		else 
//		{
//			
//			tmpRuleSet.m_Comment = new String(" (on entire training set)");
//			
//			uncoveredDataStatistic = statManager.getTrainSetStat(ClusAttrType.ATTR_USE_TARGET).cloneStat();
//			uncoveredDataStatistic.copy(statManager.getTrainSetStat(ClusAttrType.ATTR_USE_TARGET));
//			uncoveredDataStatistic.calcMean();
//			
//			if (getSettings().getVerbose() > 1)
//			{
//				System.err.println("All training examples covered - default rule on entire training set! " + uncoveredDataStatistic.toString());
//			}
//		}
//		tmpRuleSet.setTargetStat(uncoveredDataStatistic);
//		
//		return tmpRuleSet;
//	}
//	
//	 
//	
//	
//	/* HELPER METHODS */
//	void debug_RemoveExistingData(String folderName)
// 	{
//		try
//		{
//	 		File f = new File(folderName);
//	 		File[] fls = f.listFiles();
//	 		for (File ff : fls)
//	 		{
//	 			ff.delete();
//	 		}
//		}
//		catch (Exception ex)
//		{}
// 	}
//	
//	void PrintToFileDebug(String data, String folderName, String fileName) throws FileNotFoundException
//	{
//		PrintWriter pw = new PrintWriter(folderName + "/" + fileName);
//		
//		pw.print(data);
//		
//		pw.flush();
//		pw.close();
//	}
//	
//	void debug_PrintInitialRules(ClusRuleSet ruleSet, String folderName) throws FileNotFoundException
//	{
//		debug_PrintRuleSet(ruleSet, folderName, "initial_rules.txt", false);
//	}
//	
//	void debug_PrintEverything(ClusRun cr, String folderName, String fileName) throws FileNotFoundException
//	{	
//		// write debug info
//		ClusModelInfo rules_model = cr.getModelInfo(ClusModel.RULES);
//		ClusModel model = rules_model.getModel();
//		ClusRuleSet ruleSet = (ClusRuleSet)model;
//		
//		debug_PrintRuleSet(ruleSet, folderName, fileName, true);
//	}
//	
//	void debug_PrintRuleSet(ClusRuleSet ruleSet, String folderName, String fileName, boolean printDefaultRule)
//	{	
//		try
//		{
//			File f = new File(folderName);
//			boolean cancel = false;
//			if (!f.exists() || 
//			   (f.exists() && !f.isDirectory())) {
//			   if (f.mkdir())
//			   {
//				   System.out.println("Created rules.debug directory.");
//			   }
//			   else
//			   {
//				   System.out.println("Unable to create rules.debug directory.\nWill not write rule debug info.");
//				   cancel = true;
//			   }
//			}
//			
//			if (!cancel)
//			{	
//				PrintWriter pw = new PrintWriter(folderName + "/" + fileName);
//				
//				if (printDefaultRule)
//				{
//					ruleSet.getSettings().m_PrintAllRules.setValue(true);
//					ruleSet.printModel(pw);
//					ruleSet.getSettings().m_PrintAllRules.setValue(false);
//				}
//				else
//				{
//					for (int i=0;i<ruleSet.getModelSize();i++) {
//						ruleSet.getRule(i).printModel(pw);
//						
//						pw.print("\r\n\r\n");
//					}	
//				}
//				
//				pw.flush();
//				pw.close();
//			}				
//		}
//		catch (Exception ex)
//		{
//			System.err.println(ex.toString());
//		}
//	}
//	
// 
//	
//	HashMap<Integer, ArrayList<Integer>> CalculateCounts(ClusRuleSet rs)
//	{
//		HashMap<Integer, ArrayList<Integer>> hm = new HashMap<Integer, ArrayList<Integer>>();
//		
//		for (int i = 0; i < rs.getModelSize(); i++)
//		{
//			ArrayList<Integer> indicesOfRulesWithCardinality_i = hm.getOrDefault(rs.getRule(i).getModelSize(), new ArrayList<Integer>());
//			indicesOfRulesWithCardinality_i.add(i);
//
//			hm.put(rs.getRule(i).getModelSize(),  indicesOfRulesWithCardinality_i);
//		}
//		
//		return hm;
//	}
}