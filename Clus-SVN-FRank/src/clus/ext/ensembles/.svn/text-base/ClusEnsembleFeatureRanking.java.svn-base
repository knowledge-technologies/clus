package clus.ext.ensembles;

import java.io.*;
import java.util.*;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.error.Accuracy;
import clus.error.ClusErrorList;
import clus.error.MSError;
import clus.error.RelativeError;
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
//		m_AllAttributes = new HashMap();
//		m_FeatureRanks = new TreeMap();
//		m_FeatureRankByName = new HashMap();

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
		Set attributes = m_AllAttributes.keySet();
		Iterator iter = attributes.iterator();
		while (iter.hasNext()){
			String attr = (String)iter.next();
			double score = ((double[])m_AllAttributes.get(attr))[2]/ClusEnsembleInduce.getMaxNbBags();
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

	public void printRanking(String fname) throws IOException{
		TreeMap sorted = (TreeMap)m_FeatureRanks.clone();
		File franking = new File(fname+".fimp");
		FileWriter wrtr = new FileWriter(franking);
		wrtr.write("Ranking via Random Forests\n");
		wrtr.write("--------------------------\n");
		while (!sorted.isEmpty()){
//			wrtr.write(sorted.get(sorted.lastKey()) + "\t" + sorted.lastKey()+"\n");
			wrtr.write(writeRow((ArrayList)sorted.get(sorted.lastKey()),(Double)sorted.lastKey()));
			sorted.remove(sorted.lastKey());
		}
		wrtr.flush();
		wrtr.close();
		System.out.println("Feature Ranking via Random Forests written in " + franking.getName());
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
			}else System.err.println("Error at the Random Permutations");
		}
		return result;
	}

	public void fillWithAttributesInTree(ClusNode node, ArrayList attributes){
		for (int i = 0; i < node.getNbChildren(); i++){
			String att = node.getTest().getType().getName();
			if (!attributes.contains(att)){
//				System.out.println("Appending..."+att);
				attributes.add(att);
			}
			fillWithAttributesInTree((ClusNode)node.getChild(i), attributes);
		}
	}

	public double calcAverageError(RowData data, ClusModel model) throws ClusException{
		ClusSchema schema = data.getSchema();
		/* create error measure */
		ClusErrorList error = new ClusErrorList();
		NumericAttrType[] num = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NominalAttrType[] nom = schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
		if (nom.length != 0) {
			error.addError(new Accuracy(error, nom));
		} else if (num.length != 0) {
//			error.addError(new MSError(error, num));
			error.addError(new RelativeError(error, num));
		} else System.err.println("Supported only nominal or numeric targets!");
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
	
}
