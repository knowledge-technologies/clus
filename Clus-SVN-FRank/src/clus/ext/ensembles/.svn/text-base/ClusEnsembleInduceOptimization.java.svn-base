package clus.ext.ensembles;

import java.io.IOException;  
import java.util.ArrayList;

import clus.data.rows.DataTuple;
import clus.data.rows.TupleIterator;
import clus.main.ClusStatManager;
import clus.model.ClusModel;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusEnsembleInduceOptimization {

	static int[] m_HashCodeTuple;
	
	public ClusEnsembleInduceOptimization(TupleIterator train, TupleIterator test, int nb_tuples) throws IOException, ClusException{
		m_HashCodeTuple = new int[nb_tuples];
		int count = 0;
		if (train != null){
			train.init();
			DataTuple train_tuple = train.readTuple();
			while (train_tuple != null){
				m_HashCodeTuple[count] = train_tuple.hashCode();
				count++;
				train_tuple = train.readTuple();
			}
		}
		if (test != null){
			test.init();
			DataTuple test_tuple = test.readTuple();
			while (test_tuple != null){
				m_HashCodeTuple[count] = test_tuple.hashCode();
				count++;
				test_tuple = test.readTuple();
			}
		}
	}


	public ClusEnsembleInduceOptimization(TupleIterator train, TupleIterator test) throws IOException, ClusException{
		ArrayList<Integer> tuple_hash = new ArrayList<Integer>();
		if (train != null){
			train.init();
			DataTuple train_tuple = train.readTuple();
			while (train_tuple != null){
				tuple_hash.add(train_tuple.hashCode());
				train_tuple = train.readTuple();
			}
			train.init();
		}
		if (test != null){
			test.init();//restart the iterator
			DataTuple test_tuple = test.readTuple();
			while (test_tuple != null){
				tuple_hash.add(test_tuple.hashCode());
				test_tuple = test.readTuple();
			}
			test.init();//restart the iterator
		}
		int nb_tuples = tuple_hash.size();
		m_HashCodeTuple = new int[nb_tuples];
		for (int k = 0; k < nb_tuples; k++)
			m_HashCodeTuple[k] = tuple_hash.get(k);
	}

	public static int locateTuple(DataTuple tuple){ 
		int position = -1;
		boolean found  = false;
		int i = 0;
		//search for the tuple
		while (!found && i < m_HashCodeTuple.length){
			if (m_HashCodeTuple[i] == tuple.hashCode()) {
				position = i;
				found = true;
			}
			i++;
		}
		return position;
	}
	
	public void initPredictions(ClusStatistic stat){	 
	}

	public void initModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException{
	
	}

	public void addModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test, int nb_models) throws IOException, ClusException{

	}

	public static double[] incrementPredictions(double[] avg_predictions, double[] predictions, double nb_models){
		//the current averages are stored in the avg_predictions
		int plength = avg_predictions.length;
		double[] result = new double[plength];
		for (int i = 0; i < plength; i++)
			result[i] = avg_predictions[i]+(predictions[i]-avg_predictions[i])/nb_models;
		return result;
	}

	public static double[][] incrementPredictions(double[][] sum_predictions, double[][] predictions, int nb_models){
		//the current sums are stored in sum_predictions
		double[][] result = new double[sum_predictions.length][];
		for (int i = 0; i < sum_predictions.length; i++){
			result[i] = new double[sum_predictions[i].length];
			for (int j = 0; j < sum_predictions[i].length; j++){
				result[i][j] = sum_predictions[i][j] + (predictions[i][j] - sum_predictions[i][j])/nb_models;
			}
		}
		return result;
	}
	
	public static double[][] incrementPredictions(double[][] sum_predictions, double[][] predictions){
		//the current sums are stored in sum_predictions
		double[][] result = new double[sum_predictions.length][];
		for (int i = 0; i < sum_predictions.length; i++){
			result[i] = new double[sum_predictions[i].length];
			for (int j = 0; j < sum_predictions[i].length; j++){
				result[i][j] = sum_predictions[i][j] + predictions[i][j];
			}
		}
		return result;
	}
	
	//transform the class counts to majority vote (the one with max votes gets 1)
	public static double[][] transformToMajority(double[][] m_Counts){
		int[] maxPerTarget = new int[m_Counts.length];
		for (int i = 0; i < m_Counts.length; i++){
			maxPerTarget[i] = -1;
			double m_max = Double.NEGATIVE_INFINITY;
			for (int j = 0; j < m_Counts[i].length;j++){
				if (m_Counts[i][j]>m_max){
					maxPerTarget[i] = j;
					m_max = m_Counts[i][j];
				}
			}
		}
		double[][] result = new double[m_Counts.length][];//all values set to zero
		for (int m = 0; m < m_Counts.length; m++){
			result[m] = new double[m_Counts[m].length];
			result[m][maxPerTarget[m]] ++; //the positions of max class will be 1
		}
		return result;
	}

	//transform the class counts to probability distributions
	public static double[][] transformToProbabilityDistribution(double[][] m_Counts){
		double[] sumPerTarget = new double[m_Counts.length];
		for (int i = 0; i < m_Counts.length; i++)
			for (int j = 0; j < m_Counts[i].length;j++)
				sumPerTarget[i] += m_Counts[i][j];
		double[][] result = new double[m_Counts.length][];

		for (int m = 0; m < m_Counts.length; m++){
			result[m] = new double[m_Counts[m].length];
			for (int n = 0; n < m_Counts[m].length; n++){
				result[m][n] = m_Counts[m][n]/sumPerTarget[m];
			}
		}
		return result;
	}
	
	public static int getPredictionLength(int tuple){//i.e., get number of targets
		if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL || ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION)
		return ClusEnsembleInduceOptRegHMLC.getPredictionLength(tuple);
		if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY)
			return ClusEnsembleInduceOptClassification.getPredictionLength(tuple);
		return -1;
	}
	
	
	public static double getPredictionValue(int tuple, int attribute){
		if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL || ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION)
			return ClusEnsembleInduceOptRegHMLC.getPredictionValue(tuple, attribute);
		return -1;
	}

	public static double[] getPredictionValueClassification(int tuple, int attribute){
		if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY)
			return ClusEnsembleInduceOptClassification.getPredictionValueClassification(tuple, attribute);
		return null;
	}

	
}
