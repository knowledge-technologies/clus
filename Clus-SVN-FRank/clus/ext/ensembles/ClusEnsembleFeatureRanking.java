package clus.ext.ensembles;

import java.io.*; 
import java.nio.file.Files;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.TimeSeriesAttrType;
import clus.error.Accuracy;
import clus.error.ClusErrorList;
import clus.error.MSError;
import clus.error.RMSError;
import clus.error.RelativeError;
import clus.ext.hierarchical.HierErrorMeasures;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.selection.OOBSelection;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusEnsembleFeatureRanking {

	HashMap m_AllAttributes;//key is the AttributeName, and the value is array with the order in the file and the rank
//	boolean m_FeatRank;
	TreeMap m_FeatureRanks;//sorted by the rank
	HashMap m_FeatureRankByName;
	
	public ClusEnsembleFeatureRanking(){
		m_AllAttributes = new HashMap();
		m_FeatureRankByName = new HashMap();
		m_FeatureRanks = new TreeMap();
	}
	
	public void initializeAttributes(ClusAttrType[] descriptive){
		int num = -1;
		int nom = -1;
//		System.out.println("NB = "+descriptive.length);
		for (int i = 0; i < descriptive.length; i++) {
			ClusAttrType type = descriptive[i];
			if (!type.isDisabled()) {
				double[] info = new double[3];
				if (type.getTypeIndex() == 0){
					nom ++;
					info[0] = 0; //type
					info[1] = nom; //order in nominal attributes
				}
				if (type.getTypeIndex() == 1){
					num ++;
					info[0] = 1; //type
					info[1] = num; //order in numeric attributes
				}
				info[2] = 0; //current rank
//					System.out.print(type.getName()+": "+info[1]+"\t");
				m_AllAttributes.put(type.getName(),info);
			}
		}
	}
	
	
	public void sortFeatureRanks(){
		Iterator iter = m_AllAttributes.keySet().iterator();
		while (iter.hasNext()){
			String attr = (String)iter.next();
			double score = ((double[])m_AllAttributes.get(attr))[2]/ClusEnsembleInduce.getMaxNbBags();
//			double score = ((double[])m_AllAttributes.get(attr))[2];
			ArrayList attrs = new ArrayList();
			if (m_FeatureRanks.containsKey(score))
				attrs = (ArrayList)m_FeatureRanks.get(score);
			attrs.add(attr);
			m_FeatureRanks.put(score, attrs);
		}
	}

	public void convertRanksByName(){
		TreeMap sorted = (TreeMap)m_FeatureRanks.clone();
		while (!sorted.isEmpty()){
			double score = (Double)sorted.lastKey();
			ArrayList attrs = new ArrayList();
			attrs = (ArrayList) sorted.get(sorted.lastKey());
			for (int i = 0; i < attrs.size(); i++)
				m_FeatureRankByName.put(attrs.get(i), score);
			sorted.remove(sorted.lastKey());
		}
	}

	public void writeRanking(String fname) throws IOException{
		TreeMap ranking = (TreeMap)m_FeatureRanks.clone();
		File franking = new File(fname+".fimp");
		FileWriter wrtr = new FileWriter(franking);
		wrtr.write("Ranking via Random Forests\n");
		wrtr.write("--------------------------\n");
		while (!ranking.isEmpty()){
//			wrtr.write(sorted.get(sorted.lastKey()) + "\t" + sorted.lastKey()+"\n");
			wrtr.write(writeRow((ArrayList)ranking.get(ranking.lastKey()),(Double)ranking.lastKey()));
			ranking.remove(ranking.lastKey());
		}
		wrtr.flush();
		wrtr.close();
		System.out.println("Feature importances written to: " + franking.getName());
	}

	
	public String writeRow(ArrayList attributes, double value){
		String output = "";
		for (int i = 0; i < attributes.size(); i++){
			String attr = (String)attributes.get(i);
			attr = attr.replaceAll("\\[", "");
			attr = attr.replaceAll("\\]", "");
			output += attr +"\t"+value+"\n";
		}
		return output;
	}


	public void writeRankingByAttributeName(String fname, ClusAttrType[] descriptive) throws IOException{
		File franking = new File(fname+".fimp");
		FileWriter wrtr = new FileWriter(franking);
		wrtr.write("Ranking via Random Forests\n");
		wrtr.write("--------------------------\n");
		for (int i = 0; i < descriptive.length; i++){
			String attribute = descriptive[i].getName();
			double value = ((double[])m_AllAttributes.get(attribute))[2]/ClusEnsembleInduce.getMaxNbBags();
			wrtr.write(attribute +"\t"+value+"\n");
			wrtr.flush();
		}
		
/*		Iterator iter = m_AllAttributes.keySet().iterator();
		while (iter.hasNext()){
			String attr = (String)iter.next();
			double value = ((double[])m_AllAttributes.get(attr))[2]/ClusEnsembleInduce.getMaxNbBags();
			wrtr.write(attr +"\t"+value+"\n");
			wrtr.flush();
		}*/
		
		wrtr.flush();
		wrtr.close();
		System.out.println("Feature importances written to: " + franking.getName());
	}
	
	public void writeJSON(ClusRun cr) throws IOException{
		Gson jsonBuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		JsonObject functionOutputJSON = new JsonObject();	
		ClusSchema schema = cr.getStatManager().getSchema();

		// data specification
		JsonObject dataSpec = new JsonObject();
		JsonArray attributes = new JsonArray();
		JsonArray attributesTarget = new JsonArray();
		JsonArray attributesClustering = new JsonArray();
		JsonArray attributesDescriptive = new JsonArray();
		JsonObject task = new JsonObject();

		for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_ALL)) attributes.add(a.getAttributeJSON());
		for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET)) attributesTarget.add(new JsonPrimitive(a.getName()));
		for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_CLUSTERING)) attributesClustering.add(new JsonPrimitive(a.getName()));
		for (ClusAttrType a : schema.getAllAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE)) attributesDescriptive.add(new JsonPrimitive(a.getName()));

		String taskTypeString = (schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET).length > 1) ? "MT " : "ST ";
		//taskTypeString += (m_Schema.getAllAttrUse(ClusAttrType.ATTR_USE_TARGET)[0].getTypeName()) ? "MT" : "ST";
		if (ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION){
			taskTypeString += "Regression";
		}else if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY){
			taskTypeString += "Classification";
		}
		JsonElement taskType = new JsonPrimitive(taskTypeString);

		task.add("taskType", taskType);
		task.add("taskDescriptiveAttributes", attributesDescriptive);
		task.add("taskTargetAttributes", attributesTarget);
		task.add("taskClusteringAttributes", attributesTarget);
		
		String queryValue ="Unable to get query details.";
		try {
			String fname = "query.param";
			File f = new File(fname);
			if (f.exists() && f.isFile()) 
			{
				queryValue = new String(Files.readAllBytes(f.toPath()));
			}			
		}
		catch(Exception ex){}	
		
		dataSpec.add("attributes", attributes);
		dataSpec.add("task", task);
		dataSpec.addProperty("query", queryValue);
		functionOutputJSON.add("dataSpecification", dataSpec);


		JsonObject algorithmSpec = new JsonObject();
		JsonElement algorithmName;
		int ens_method = cr.getStatManager().getSettings().getEnsembleMethod();
		int fr_method = cr.getStatManager().getSettings().getRankingMethod();
		if (ens_method == Settings.ENSEMBLE_EXTRA_TREES){
			algorithmName = new JsonPrimitive("ExtraTrees/GENIE3");
		}else if ((ens_method == Settings.ENSEMBLE_RFOREST) && (fr_method  == Settings.RANKING_RFOREST)){
			algorithmName = new JsonPrimitive("RandomForestRanking");			
		} else if ((ens_method == Settings.ENSEMBLE_RFOREST) && (fr_method == Settings.RANKING_GENIE3)){
			algorithmName = new JsonPrimitive("RandomForest/GENIE3");			
		} else {
			algorithmName = new JsonPrimitive("Ranking method specified incorrectly!");		
		}
		
		int ens_size = cr.getStatManager().getSettings().getNbBaggingSets().getInt();
		String feat_size = cr.getStatManager().getSettings().getNbRandomAttrString();
		
		JsonElement parameters = new JsonPrimitive("Iterations: " + ens_size + "; SelectRandomSubspaces: " + feat_size);
		
		algorithmSpec.add("name", algorithmName);
		algorithmSpec.add("parameters", parameters);
		algorithmSpec.addProperty("version", "1.0");
		functionOutputJSON.add("algorithmSpecification", algorithmSpec);

		JsonArray rankingResults = new JsonArray();
		TreeMap sorted = (TreeMap)m_FeatureRanks.clone();
		Iterator iter = sorted.keySet().iterator();

		int count = 1;
		while(!sorted.isEmpty()){
			double score = (Double)sorted.lastKey();
			ArrayList attrs = (ArrayList)sorted.get(score);
			for (int i = 0; i < attrs.size(); i++){
				JsonObject elm = new JsonObject();
				elm.addProperty("attributeName", (String)attrs.get(i));
				elm.addProperty("ordering", count);
				elm.addProperty("importance", score);
				count++;
				rankingResults.add(elm);
			}
			sorted.remove(sorted.lastKey());
		}

		functionOutputJSON.add("ranking", rankingResults);

		File jsonfile = new File(cr.getStatManager().getSettings().getAppName() + ".json");
		FileWriter json = new FileWriter(jsonfile);
		json.write(jsonBuilder.toJson(functionOutputJSON));
		json.flush();
		json.close();
		System.out.println("JSON model written to: " + cr.getStatManager().getSettings().getAppName() + ".json");

	}
	
	
	/*
	 * @param selection
	 * @param data
	 * @param type    -> 0 nominal, 1 numeric
	 * @param position -> at which position
	 * @return
	 */
	public RowData createRandomizedOOBdata(OOBSelection selection, RowData data, int type, int position){
		RowData result = data;
		Random rndm = new Random(data.getNbRows());
		for (int i = 0; i < result.getNbRows(); i++){
//			int rnd = i + ClusRandom.nextInt(ClusRandom.RANDOM_ALGO_INTERNAL, result.getNbRows()- i);
			int rnd = i + rndm.nextInt(result.getNbRows()- i);
			DataTuple first = result.getTuple(i);
			DataTuple second = result.getTuple(rnd);
			if (type == 0){//nominal
				int swap = first.getIntVal(position);
				first.setIntVal(second.getIntVal(position), position);
				second.setIntVal(swap, position);
			}else if (type == 1){//numeric
				double swap = first.getDoubleVal(position);
				first.setDoubleVal(second.getDoubleVal(position), position);
				second.setDoubleVal(swap, position);
			}else {
				System.err.println("Error while making the random permutations for feature ranking!");
				System.exit(-1);
			}
		}
		return result;
	}

	public static void fillWithAttributesInTree(ClusNode node, ArrayList attributes){
		for (int i = 0; i < node.getNbChildren(); i++){
			String att = node.getTest().getType().getName();
			if (!attributes.contains(att))attributes.add(att);
			fillWithAttributesInTree((ClusNode)node.getChild(i), attributes);
		}
	}

	public double calcAverageError(RowData data, ClusModel model, ClusRun cr) throws ClusException{
		ClusSchema schema = data.getSchema();
		/* create error measure */
		ClusErrorList error = new ClusErrorList();
		NumericAttrType[] num = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY) {
			error.addError(new Accuracy(error, nom));
		} else if (ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION) {
//			error.addError(new MSError(error, num));
//			error.addError(new RelativeError(error, num));
			error.addError(new RMSError(error, num));
		} else if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
			error.addError(new HierErrorMeasures(error, cr.getStatManager().getHier(), cr.getStatManager().getSettings().getRecallValues().getDoubleVector(), cr.getStatManager().getSettings().getCompatibility(), Settings.HIERMEASURE_POOLED_AUPRC, false));
		} else {
			System.err.println("Feature ranking with Random Forests is supported only for:");
			System.err.println("- multi-target classification (multi-label classification)");
			System.err.println("- multi-target regression");
			System.err.println("- hierarchical multi-label classification");
			System.exit(-1);
		}
		
		/* attach model to given schema */
		schema.attachModel(model);
		/* iterate over tuples and compute error */
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			ClusStatistic pred = model.predictWeighted(tuple);
			error.addExample(tuple, pred);
		}
		/* return the average error */
		double err = error.getFirstError().getModelError();
		return err;
	}

	//	returns sorted feature ranking
	public TreeMap getFeatureRanks(){
		return m_FeatureRanks;
	}

	//	returns feature ranking
	public HashMap getFeatureRanksByName(){ 
		return m_FeatureRankByName;
	}
	
	public double[] getAttributeInfo(String attribute){
		return (double[])m_AllAttributes.get(attribute);
	}
	
	public void putAttributeInfo(String attribute, double[]info){
		m_AllAttributes.put(attribute, info);
	}
	
	public void calculateRFimportance(ClusModel model, ClusRun cr, OOBSelection oob_sel) throws ClusException{
		System.out.println("   Matej: calculateRFimportance");
		ArrayList<String> attests = new ArrayList<String>();
		fillWithAttributesInTree((ClusNode)model, attests);
		RowData tdata = (RowData)((RowData)cr.getTrainingSet()).deepCloneData();
		double oob_err = calcAverageError((RowData)tdata.selectFrom(oob_sel), model, cr);
		for (int z = 0; z < attests.size(); z++){//for the attributes that appear in the tree
			String current_attribute = (String)attests.get(z);
			double [] info = getAttributeInfo(current_attribute);
			double type = info[0];
			double position = info[1];
			RowData permuted = createRandomizedOOBdata(oob_sel, (RowData)tdata.selectFrom(oob_sel), (int)type, (int)position);
			if (ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION){//increase in error rate (oob_err -> MSError)
				info[2] += (calcAverageError((RowData)permuted, model, cr) - oob_err)/oob_err;
			}else if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY){//decrease in accuracy (oob_err -> Accuracy)
				info[2] += (oob_err - calcAverageError((RowData)permuted, model, cr))/oob_err;
			}else if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL){//decrease in accuracy (oob_err -> AU(avgPRC))
				info[2] += (oob_err - calcAverageError((RowData)permuted, model, cr))/oob_err;
			}
			putAttributeInfo(current_attribute, info);
		}
	}
	
	
	public void calculateGENIE3importance(ClusNode node, ClusRun cr){
		if (!node.atBottomLevel()){
			String attribute = node.getTest().getType().getName();
			double [] info = getAttributeInfo(attribute);
			info[2] += calculateGENI3value(node, cr);//variable importance
			putAttributeInfo(attribute, info);
			for (int i = 0; i < node.getNbChildren(); i++)
				calculateGENIE3importance((ClusNode)node.getChild(i),cr);
		}//if it is a leaf - do nothing
	}
	
	public double calculateGENI3value(ClusNode node, ClusRun cr){
		ClusStatistic total = node.getClusteringStat();
		double total_variance = total.getSVarS(cr.getStatManager().getClusteringWeights());
		double summ_variances = 0.0;
		for (int j = 0; j < node.getNbChildren(); j++){
			ClusNode child = (ClusNode)node.getChild(j); 
			summ_variances += child.getClusteringStat().getSVarS(cr.getStatManager().getClusteringWeights());
		}
		return total_variance - summ_variances;
	}
	
	/**
	 * Recursively computes the symbolic importance of attributes, importance(attribute) = importance(attribute, {@code node}), where <p>
	 * importance(attribute, {@code node}) = (0.0 : 1.0 ? {@code node} has attribute as a test) + sum_subnodes {@code weight} * importance(attribute, subnode).
	 * @param node
	 * @param cr
	 * @param weight The value of {@code Settings.m_SymbolicWeight}
	 * @param depth Depth of {@code node}, root's depth is 0
	 */
	public void calculateSYMBOLICimportance(ClusNode node, ClusRun cr, double weight, int depth){
		if (!node.atBottomLevel()){
			String attribute = node.getTest().getType().getName();
			double [] info = getAttributeInfo(attribute);
			info[2] += Math.pow(weight, depth);//variable importance
			putAttributeInfo(attribute, info);
			for (int i = 0; i < node.getNbChildren(); i++)
				calculateSYMBOLICimportance((ClusNode)node.getChild(i), cr, weight, depth + 1);
		}//if it is a leaf - do nothing
	}

}
