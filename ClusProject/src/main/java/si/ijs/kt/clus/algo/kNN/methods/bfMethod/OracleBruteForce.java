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
	
	private HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> m_NearestNeighbours;
	private DataTuple[] m_ChosenInstancesTrain;
	private DataTuple[] m_ChosenInstancesTest;
	
	
	public OracleBruteForce(ClusRun run, SearchDistance dist) {
		super(run, dist);
		m_NearestNeighbours = new HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>>();
		m_NearestNeighbours.put(SaveLoadNeighbours.DUMMY_TARGET, new HashMap<Integer, NearestNeighbour[][]>());
		
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
	   // get chosen instances
	   int[] tmp;
	   tmp = sett.getKNN().getChosenIntancesTrain(m_ListTrain.length);
	   m_ChosenInstancesTrain = new DataTuple[tmp.length];
	   for(int i = 0; i < m_ChosenInstancesTrain.length; i++) {
		   m_ChosenInstancesTrain[i] = m_ListTrain[tmp[i]];
	   }
	   tmp = sett.getKNN().getChosenIntancesTest(m_ListTest.length);
	   m_ChosenInstancesTest = new DataTuple[tmp.length];
	   for(int i = 0; i < m_ChosenInstancesTest.length; i++) {
		   m_ChosenInstancesTest[i] = m_ListTest[tmp[i]];
	   }
	   
	   // obtaining neighbours
	   if (sett.getKNN().shouldLoadNeighbours()) {
		   ClusReliefFeatureRanking.printMessage("Loading nearest neighbours from file(s)", 1, sett.getGeneral().getVerbose());
		   SaveLoadNeighbours nnLoader = new SaveLoadNeighbours(sett.getKNN().getLoadNeighboursFiles(), null);
		   m_NearestNeighbours = nnLoader.loadNeighboursFromFiles();
		   SaveLoadNeighbours.assureIsFlatNearestNeighbours(m_NearestNeighbours);		   
	   } else {
		   ClusReliefFeatureRanking.printMessage("Computing nearest neighbours from file(s)", 1, sett.getGeneral().getVerbose());
		   for(DataTuple tuple : new ArrayOfArraysIterator<>(new DataTuple[][] {m_ChosenInstancesTrain, m_ChosenInstancesTest})) {
			   NN[] temp = super.returnPureNNs(tuple, k);
			   NearestNeighbour[] nns = new NearestNeighbour[temp.length];
			   for(int n = 0; n < nns.length; n++) {
				   nns[n] = new NearestNeighbour(temp[n].getTuple().getDatasetIndex(), temp[n].getDistance());
			   }
			   m_NearestNeighbours.get(SaveLoadNeighbours.DUMMY_TARGET).put(getModifiedIndex(tuple), new NearestNeighbour[][] {nns});
		   }
	   }
	   // saving neighbours
	   if(sett.getKNN().shouldSaveNeighbours()) {
		   SaveLoadNeighbours nnSaver = new SaveLoadNeighbours(null, sett.getKNN().getSaveNeighboursFile());
		   nnSaver.saveNeighboursToFile(m_NearestNeighbours);
	   }
   }


   @Override
   public LinkedList<DataTuple> returnNNs(DataTuple tuple, int k) throws ClusException {
	   LinkedList<DataTuple> nns = new LinkedList<DataTuple>();
	   int index = getModifiedIndex(tuple);
	   for(int i = 0; i < k; i++) {
		   int nn_index = m_NearestNeighbours.get(SaveLoadNeighbours.DUMMY_TARGET).get(index)[0][i].getIndexInDataset();
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
