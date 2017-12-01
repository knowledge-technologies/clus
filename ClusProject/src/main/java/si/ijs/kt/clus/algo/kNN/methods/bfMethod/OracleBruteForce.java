package si.ijs.kt.clus.algo.kNN.methods.bfMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import si.ijs.kt.clus.algo.kNN.methods.NN;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.distance.primitive.SearchDistance;
import si.ijs.kt.clus.ext.featureRanking.relief.ClusReliefFeatureRanking;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.NearestNeighbour;
import si.ijs.kt.clus.ext.featureRanking.relief.nearestNeighbour.SaveLoadNeighbours;
import si.ijs.kt.clus.main.ClusModelInfoList;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;

public class OracleBruteForce extends BruteForce {
	
	HashMap<Integer, NearestNeighbour[]> m_NearestNeighbours;
	
	
	public OracleBruteForce(ClusRun run, SearchDistance dist) {
		super(run, dist);
		m_NearestNeighbours = new HashMap<Integer, NearestNeighbour[]>();
	}

   
   @Override
   public void build(int k) throws ClusException, IOException, InterruptedException {
	   m_ListTrain = getRun().getDataSet(ClusModelInfoList.TRAINSET).getData(); // Must not be null ...
	   for(DataTuple tuple : m_ListTrain) {
		   tuple.setTraining(true);
	   }
	   if(getRun().getDataSet(ClusModelInfoList.TESTSET) != null) {
		   m_ListTest = getRun().getDataSet(ClusModelInfoList.TESTSET).getData();
		   for(DataTuple tuple : m_ListTest) {
			   tuple.setTesting(true);
		   }  
	   } else {
		   m_ListTest = new DataTuple[0];
	   }
	   
	   Settings sett =  getRun().getStatManager().getSettings();
	   if (sett.getKNN().shouldLoadNeighbours()) {
		   ClusReliefFeatureRanking.printMessage("Loading nearest neighbours from file(s)", 1, sett.getGeneral().getVerbose());
		   SaveLoadNeighbours nnLoader = new SaveLoadNeighbours(sett.getKNN().getLoadNeighboursFiles(), null);
		   m_NearestNeighbours = SaveLoadNeighbours.flattenNearestNeighbours(nnLoader.loadNeighboursFromFiles());
	   } else {
		   ClusReliefFeatureRanking.printMessage("Computing nearest neighbours from file(s)", 1, sett.getGeneral().getVerbose());
		   for(DataTuple tuple : new ArrayOfArraysIterator<>(new DataTuple[][] {m_ListTrain, m_ListTest})) {
			   NN[] temp = super.returnPureNNs(tuple, k);
			   NearestNeighbour[] nns = new NearestNeighbour[temp.length];
			   for(int n = 0; n < nns.length; n++) {
				   nns[n] = new NearestNeighbour(temp[n].getTuple().getDatasetIndex(), temp[n].getDistance());
			   }
			   m_NearestNeighbours.put(getModifiedIndex(tuple), nns);
		   }
	   }
       

//		if(m_ShouldSaveNeighbours) {
//			SaveLoadNeighbours nnSaver = new SaveLoadNeighbours(null, m_SaveNearestNeigbhoursFile);
//			nnSaver.saveNeighboursToFile(m_NearestNeighbours);
//		}
	   
	   
       
       // compute everything
   }


   @Override
   public LinkedList<DataTuple> returnNNs(DataTuple tuple, int k) throws ClusException {
	   LinkedList<DataTuple> nns = new LinkedList<DataTuple>();
	   int index = getModifiedIndex(tuple);
	   for(int i = 0; i < k; i++) {
		   int nn_index = m_NearestNeighbours.get(index)[i].getIndexInDataset();
		   nns.add(m_ListTrain[nn_index]);
	   }
	   return nns;
   }
   
   private int getModifiedIndex(DataTuple tuple) {
	   if(tuple.isTraining()) {
		   return tuple.getDatasetIndex();
	   } else if(tuple.isTesting()) {
		   return m_ListTrain.length + tuple.getDatasetIndex();
	   } else {
		   throw new RuntimeException("The tuple is neither training nor testing instance.");
	   }
   }
}
