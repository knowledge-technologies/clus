package clus.ext.ensembles;

import java.io.IOException;    

import clus.data.rows.DataTuple;
import clus.data.rows.TupleIterator;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.statistic.ClassificationStat;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusEnsembleInduceOptClassification extends ClusEnsembleInduceOptimization{

	static double[][][] m_AvgPredictions;
	
	public ClusEnsembleInduceOptClassification(TupleIterator train, TupleIterator test, int nb_tuples) throws IOException, ClusException{
		super(train, test, nb_tuples);
	}

	public ClusEnsembleInduceOptClassification(TupleIterator train, TupleIterator test) throws IOException, ClusException{
		super(train,test);
	}
	
	public void initPredictions(ClusStatistic stat){
		ClassificationStat nstat = (ClassificationStat) stat;
		m_AvgPredictions = new double[m_HashCodeTuple.length][nstat.getNbAttributes()][];
		for (int i = 0; i < nstat.getNbAttributes(); i++){
			m_AvgPredictions[m_HashCodeTuple.length-1][i] = new double[nstat.getNbClasses(i)];			
		}
	}

	public void initModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test) throws IOException, ClusException{
		if (train != null){
			train.init();
			DataTuple train_tuple = train.readTuple();
			while (train_tuple != null){
				int position = locateTuple(train_tuple);
				ClassificationStat stat = (ClassificationStat) model.predictWeighted(train_tuple);
				double[][] counts = stat.getClassCounts().clone();
				switch (Settings.m_ClassificationVoteType.getValue()){//default is Majority Vote
				case 0: counts = transformToMajority(counts);break;
				case 1: counts = transformToProbabilityDistribution(counts);break;
				default: counts = transformToMajority(counts);
				}
				m_AvgPredictions[position] = counts;
//				System.out.println(train_tuple.toString());
//				System.out.println(stat.getString());
//				System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " + m_AvgPredictions[position][0][2]);
				train_tuple = train.readTuple();
			}
			train.init();
		}
		if (test != null){
			test.init();
			DataTuple test_tuple = test.readTuple();
			while (test_tuple != null){
				int position = locateTuple(test_tuple);
				ClassificationStat stat = (ClassificationStat) model.predictWeighted(test_tuple);
				double[][] counts = stat.getClassCounts().clone();
				switch (Settings.m_ClassificationVoteType.getValue()){//default is Majority Vote
				case 0: counts = transformToMajority(counts);break;
				case 1: counts = transformToProbabilityDistribution(counts);break;
				default: counts = transformToMajority(counts);
				}	
				m_AvgPredictions[position] = counts;
//				System.out.println(test_tuple.toString());
//				System.out.println(stat.getString());
//				System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " + m_AvgPredictions[position][0][2]);
				test_tuple = test.readTuple();
			}
			test.init();
		}
	}

	public void addModelPredictionForTuples(ClusModel model, TupleIterator train, TupleIterator test, int nb_models) throws IOException, ClusException{
		if (train != null){
			train.init();
			DataTuple train_tuple = train.readTuple();
			while (train_tuple != null){
				int position = locateTuple(train_tuple);
				ClassificationStat stat = (ClassificationStat)model.predictWeighted(train_tuple);
				double[][] counts = stat.getClassCounts().clone();
				switch (Settings.m_ClassificationVoteType.getValue()){//default is Majority Vote
				case 0: counts = transformToMajority(counts);break;
				case 1: counts = transformToProbabilityDistribution(counts);break;
				default: counts = transformToMajority(counts);
				}	
				m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], counts, nb_models);
//				System.out.println(train_tuple.toString());
//				System.out.println(stat.getString());
//				System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " + m_AvgPredictions[position][0][2]);
				
				train_tuple = train.readTuple();
			}
			train.init();
		}
		if (test != null){
			test.init();
			DataTuple test_tuple = test.readTuple();
			while (test_tuple != null){
				int position = locateTuple(test_tuple);
				ClassificationStat stat = (ClassificationStat)model.predictWeighted(test_tuple);
				double[][] counts = stat.getClassCounts().clone();
				switch (Settings.m_ClassificationVoteType.getValue()){//default is Majority Vote
				case 0: counts = transformToMajority(counts);break;
				case 1: counts = transformToProbabilityDistribution(counts);break;
				default: counts = transformToMajority(counts);
				}
				m_AvgPredictions[position] = incrementPredictions(m_AvgPredictions[position], counts, nb_models);				
//				System.out.println(test_tuple.toString());
//				System.out.println(stat.getString());
//				System.out.println(m_AvgPredictions[position][0][0]+ " " + m_AvgPredictions[position][0][1] + " " + m_AvgPredictions[position][0][2]);

				test_tuple = test.readTuple();
			}
			test.init();
		}
	}

	
	public static int getPredictionLength(int tuple){
		return m_AvgPredictions[tuple].length;
	}
	
	public static double[] getPredictionValueClassification(int tuple, int attribute){
		return m_AvgPredictions[tuple][attribute];
	}

}
