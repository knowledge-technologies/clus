package si.ijs.kt.clus.algo.kNN.methods.bfMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import si.ijs.kt.clus.main.settings.section.SettingsKNN;
import si.ijs.kt.clus.util.exception.ClusException;

public class OracleBruteForce extends BruteForce {
	
	private HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>> m_NearestNeighbours;
	private DataTuple[] m_ChosenInstancesTrain;
	private DataTuple[] m_ChosenInstancesTest;
	
	
	public OracleBruteForce(ClusRun run, SearchDistance dist) {
		super(run, dist);
		m_NearestNeighbours = new HashMap<Integer, HashMap<Integer, NearestNeighbour[][]>>();
		m_NearestNeighbours.put(SaveLoadNeighbours.DUMMY_TARGET, new HashMap<Integer, NearestNeighbour[][]>());
		
	}
	
	/**
	 * Keep those of the training examples with missing values that are in the chosen training instances and build the model.
	 * @param k
	 * @param trainingExamplesWithMissing
	 * @param sett
	 */
	public void buildForMissingTargetImputation(int k, int[] trainingExamplesWithMissing, SettingsKNN sett) {
		try {
			m_ListTrain = getRun().getDataSet(ClusModelInfoList.TRAINSET).getData();
		} catch (ClusException | IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
		if (trainingExamplesWithMissing != null) {
			// filter the candidate training instances
			int[] chosenTrainingInstances = sett.getChosenIntancesTrain(m_ListTrain.length);
			Arrays.sort(chosenTrainingInstances);
			ArrayList<Integer> kept = new ArrayList<>();
			int iWithMissing = 0, iTraining = 0;
			while (iWithMissing < trainingExamplesWithMissing.length && iTraining < chosenTrainingInstances.length) {
				int missing = trainingExamplesWithMissing[iWithMissing];
				int training = chosenTrainingInstances[iTraining];
				if (missing == training) {
					kept.add(missing);
					iWithMissing++;
					iTraining++;
				}
				else if (missing < training) {
					iWithMissing++;
				} else {
					iTraining++;
				}
			}
			int[] filtered = new int[kept.size()];
			for (int i = 0; i < filtered.length; i++) {
				filtered[i] = kept.get(i);
			}
			sett.setChosenIntancesTrain(filtered);
		}
		try {
			build(k, true);
		} catch (ClusException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void build(int k) throws ClusException, IOException, InterruptedException {
		build(k, false);
	}
   
   
   public void build(int k, boolean skipFirstNeighbour) throws ClusException, IOException, InterruptedException {
	   if (m_ListTrain == null) {
		   m_ListTrain = getRun().getDataSet(ClusModelInfoList.TRAINSET).getData();
	   }
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
	   int actualK = skipFirstNeighbour ? k + 1: k;
	   if (sett.getKNN().shouldLoadNeighbours()) {
		   ClusReliefFeatureRanking.printMessage("Loading nearest neighbours from file(s)", 1, sett.getGeneral().getVerbose());
		   SaveLoadNeighbours nnLoader = new SaveLoadNeighbours(sett.getKNN().getLoadNeighboursFiles(), null);
		   m_NearestNeighbours = nnLoader.loadNeighboursFromFiles();
		   SaveLoadNeighbours.assureIsFlatNearestNeighbours(m_NearestNeighbours);
	   } else {
		   ClusReliefFeatureRanking.printMessage("Computing nearest neighbours", 1, sett.getGeneral().getVerbose());
		   int counter = 0;
		   int nInstances = m_ChosenInstancesTrain.length + m_ChosenInstancesTest.length;
		   int percentStep = 10;
		   int percents = percentStep;
		   for(DataTuple tuple : new ArrayOfArraysIterator<>(new DataTuple[][] {m_ChosenInstancesTrain, m_ChosenInstancesTest})) {
			   counter++;
			   NN[] temp = super.returnPureNNs(tuple, actualK);
			   temp = Arrays.copyOfRange(temp, temp.length - k, temp.length);  // skip first if necessary
			   NearestNeighbour[] nns = new NearestNeighbour[temp.length];
			   for(int n = 0; n < nns.length; n++) {
				   nns[n] = new NearestNeighbour(temp[n].getTuple().getDatasetIndex(), temp[n].getDistance());
			   }
			   m_NearestNeighbours.get(SaveLoadNeighbours.DUMMY_TARGET).put(getModifiedIndex(tuple), new NearestNeighbour[][] {nns});
			   int percentsNow = 100 * counter / nInstances;
			   if (percentsNow >= percents) {
				   percentsNow -= Math.floorMod(percentsNow, percentStep);
				   ClusReliefFeatureRanking.printMessage(String.format("Computed %d percents of nearest neighbours.", percentsNow), 1, sett.getGeneral().getVerbose());
				   percents = percentsNow + percentStep;
			   }
		   }
		   if (percents <= 100) {
			   ClusReliefFeatureRanking.printMessage("Computed 100 percents of nearest neighbours.", 1, sett.getGeneral().getVerbose());
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
