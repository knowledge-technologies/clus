package si.ijs.kt.clus.ext.semisupervised;

import java.io.IOException;
import java.io.PrintWriter;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.ClusAttrType.Status;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsSSL;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.pruning.PruneTree;
import si.ijs.kt.clus.selection.RandomSelection;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;

public class ClusSemiSupervisedPCTs extends ClusSemiSupervisedInduce {

	ClusInductionAlgorithm m_Induce;
	ClusModel m_Model;
	double[] m_ParameterValues; // Which values to try
	int m_InternalXValFolds; // Number of internal XVal folds
	boolean m_IsInternalXVal; // Do we run internal xval?
	boolean m_Pruning; // Pruning when determining parameter score or not?
	boolean m_IsEnsemble;
	String m_ScoresPath; // Where to save weight scores
	boolean m_SaveScores; // Should the scores be saved?
	PrintWriter writer;
	
	int[] m_InternalFolds; // which folds 
	boolean m_InduceMain;

	public ClusSemiSupervisedPCTs(ClusInductionAlgorithm clss_induce) throws ClusException, IOException {
		super(clss_induce);
		m_Induce = clss_induce;

		// maybe initialize settings parameters
		initialize(getSchema(), getSettings());
	}

	/**
	 * * Initialize parameters
	 *
	 * @param schema
	 * @param sett
	 */
	public void initialize(ClusSchema schema, Settings settx) {
		SettingsSSL sett = settx.getSSL();
		m_ParameterValues = sett.getSSLPossibleWeights();
		m_InternalXValFolds = sett.getSSLInternalFolds();
		m_Pruning = sett.getSSLPruningWhenTuning();
		m_ScoresPath = sett.getSSLWeightScoresFile();
		m_SaveScores = (!m_ScoresPath.equalsIgnoreCase("NO"));   // slow clap for hard-coded values for Tomaz :)
		m_IsInternalXVal = sett.shouldForceInternalXVal() || m_ParameterValues.length > 1;
		if (m_IsInternalXVal && !m_SaveScores) {
			ClusLogger.info("It does not make any sense to force running the internal XVAL and not saving the score. Just saying.");
		}
		m_PercentageLabeled = sett.getPercentageLabeled() / 100.0;
		
		m_InternalFolds = sett.getInternalFoldIndices();
		m_InduceMain = sett.shouldInduceMain();
<<<<<<< Upstream, based on origin/master
		//n_IsEnsemble = settx.n_IsEnsemble()
=======
		m_IsEnsemble = settx.getEnsemble().isEnsembleMode();
>>>>>>> a854ed4 Fixed error with ClusSemiSupervisedPCTs.java.
	}

	@Override
	public void initialize() throws ClusException, IOException {
		m_Induce.initialize();
	}

	@Override
	public ClusModel induceSingleUnpruned(ClusRun crOriginal) throws Exception {

		ClusRun cr = new ClusRun(crOriginal);
		cr.setTrainingSet(crOriginal.getTrainingSet().cloneData());
		partitionData(cr); // set training set, unlabeled set and testing set (if needed)

		if (m_SaveScores)
			writer = new PrintWriter(m_ScoresPath);
		ClusModel model;
		ClusRun foldRun;

		// FIXME: use si.ijs.kt.clus.error.common.ClusError.shouldBeLow() for this;
		// martinb
		boolean lowerIsBetter = false;
		double bestError = 0;
		// ClusRun myClusRun = new ClusRun(cr);

		NumericAttrType[] num = m_Schema.getNumericAttrUse(AttributeUseType.Target);
		NominalAttrType[] nom = m_Schema.getNominalAttrUse(AttributeUseType.Target);
		if (nom.length != 0) {
			// FIXME: IMPORTANT!!! Tomaz, this is dangerous, also in classification we maybe
			// want to
			// optimize lowerisBetter=true (e.g., Hamming loss), this needs to be made
			// modular, i.e.,
			// metric which should be optimized needs to be parameter in .s file

			lowerIsBetter = false;
			bestError = Double.MIN_VALUE;
		} else if (num.length != 0) {
			lowerIsBetter = true;
			bestError = Double.MAX_VALUE;
		}

		ClusAttrType[] clustering = m_Schema.getAllAttrUse(AttributeUseType.Clustering);

		double bestWeight = 0;

		if (m_IsInternalXVal) {
			XValMainSelection xvalmain = new XValRandomSelection(cr.getTrainingSet().getNbRows(), m_InternalXValFolds);

			for (double weight : m_ParameterValues) {
				
				double error = 0;

				for (int fold : m_InternalFolds) {
					ClusLogger.info(String.format("Inducing fold %d for weight %f", fold, weight));
					// Set train and test sets for the fold ClusRun
					XValSelection sel = new XValSelection(xvalmain, fold);
					foldRun = new ClusRun(cr);
					foldRun.setTrainingSet(foldRun.getTrainingSet().cloneData());
					ClusData val = foldRun.getTrainingSet().select(sel);
					foldRun.setTestSet(((RowData) val).getIterator());

					// merge unlabeled data with the training set
					RowData trainingSet = (RowData) foldRun.getTrainingSet();

					if (weight != 1) { // if w=1 supervised learning is performed, and we don't need unlabeled data.
										// Actually, we must not provide unlabeled data in this case because it changes
										// the results!
						for (int i = 0; i < m_UnlabeledData.getNbRows(); i++) {
							trainingSet.add(m_UnlabeledData.getTuple(i));
						}
						foldRun.setTrainingSet(trainingSet);
					}
					
					// take care of number of trees in xval: may have 0 influence when building a single tree
					adjustNumberOfTrees(foldRun);

					// Initialize normalization weights
					foldRun.getStatManager().initNormalizationWeights(
							foldRun.getStatManager().createStatistic(AttributeUseType.All), foldRun.getTrainingSet());

					// FIXME: not sure if for w=1, i.e., supervised learning, we
					// need to change Clustering=Target? If it stays as
					// Clustering=Descriptive+Target this changes normalization
					// in getSVarS, not sure if this will change something or
					// no?
					ClusAttributeWeights clusteringWeights = foldRun.getStatManager().getClusteringWeights(); // this
																												// should
																												// be
																												// clusteringWeights
																												// with
																												// all 1

					setWeights(clusteringWeights, clustering, weight);

					model = m_Induce.induceSingleUnpruned(foldRun);

					// pruning
					if (m_Pruning && !m_IsEnsemble) {
						ClusNode orig = (ClusNode) model;
						orig.numberTree();
						PruneTree pruner = m_Induce.getStatManager().getTreePruner(trainingSet);
						pruner.setTrainingData(trainingSet);
						pruner.prune(orig);
					}
					error += calculateError(model, foldRun.getTestSet(), foldRun.getTestSet().getNbRows())
							.getModelError(); // see ClusSemiSupervisedInduce.calculateError for an example how you can
												// calculate error, I think getModelError should give you RMSE

				}

				if (m_SaveScores) {
					writer.print(weight);
					writer.print(',');
					writer.println(error);
				}

				if ((lowerIsBetter && error <= bestError) || (!lowerIsBetter && error >= bestError)) {
					bestError = error;
					bestWeight = weight;
				}

			}

			if (m_SaveScores) {
				writer.println();
				writer.print(bestWeight);
				writer.print(',');
				writer.println(bestError);
				writer.close();
			}

			// merge unlabeled data with the training set, if paramOpt was not performed,
			// merging was already performed in partitionData
			
			// Matej and Tomaz deduced that actually, merging is not necessary if w = 1,
			// since the unlabeled examples are given weight 0 later on ...
			if (bestWeight != 1) {
				RowData trainingSet = (RowData) cr.getTrainingSet();

				for (int i = 0; i < m_UnlabeledData.getNbRows(); i++) {
					trainingSet.add(m_UnlabeledData.getTuple(i));
				}

				cr.setTrainingSet(trainingSet);
			}

			ClusLogger.info();
			ClusLogger.info("Weight parameter w = " + bestWeight + " for SSL-PCT algorithm was selected via "
					+ m_InternalXValFolds + "-fold internal cross validation");
			ClusLogger.info();

		} else {
			bestWeight = m_ParameterValues[0];
		}

		// learn the model with the best parameters
		// put the best weights into ClusRun
		// myClusRun.setClusteringWeights(clusteringWeights);
		cr.getStatManager().initNormalizationWeights(cr.getStatManager().createStatistic(AttributeUseType.All),
				cr.getTrainingSet());

		// now repeat the exercise from the above, but with the best parameter
		// and re-initialize weights with normalization weights for the whole training
		// set
		// update w parameter values
		ClusAttributeWeights clusteringWeights = cr.getStatManager().getClusteringWeights();

		setWeights(clusteringWeights, clustering, bestWeight);

		if (!m_InduceMain) {
			ClusLogger.info("Internal folds computed. Exiting now.");
			ClusLogger.info("Done.");
			System.exit(0);
			
		}
		// learn model with new training set and best w parameter
		return m_Induce.induceSingleUnpruned(cr);

	}

	public void setWeights(ClusAttributeWeights clusteringWeights, ClusAttrType[] clustering, double weight)
			throws ClusException {

		int nbClustering = clustering.length;
		double sslweight;
		double nbTarget = m_Schema.getNbTargetAttributes();
		double nbOther = nbClustering - nbTarget;
		ClusAttrType attrType;

		if (m_StatManager.getClusterMode() == ClusStatManager.Mode.HIER_CLASS_AND_REG) {
			int nbHierClasses = m_StatManager.getHier().getTotal();

			for (int i = 0; i < nbClustering; i++) {
				attrType = clustering[i];

				if (attrType.getStatus().equals(Status.Target)) { // Hierarchy
					sslweight = weight * (nbClustering / nbTarget);
					clusteringWeights.setWeight(i, 0); // dummy attribute, represents hierarchy
					for (int j = 1; j <= nbHierClasses; j++) {
						clusteringWeights.setWeight(i + j, sslweight); // weight for each node in hierarchy
					}
				} else {
					sslweight = (1 - weight) * (nbClustering / nbOther);
					clusteringWeights.setWeight(i, sslweight);
				}

			}
		} else {
			// update w parameter values
			for (int i = 0; i < nbClustering; i++) {
				attrType = clustering[i];
				sslweight = attrType.getStatus().equals(Status.Target) ? weight * (nbClustering / nbTarget)
						: (1 - weight) * (nbClustering / nbOther);
				clusteringWeights.setWeight(attrType, sslweight);
			}
		}
	}

	@Override
	/**
	 * Set training set and unlabeled set
	 */
	public void partitionData(ClusRun cr) throws IOException, ClusException, InterruptedException {
		m_UnlabeledData = cr.getUnlabeledSet();
		m_TrainingSet = new RowData(cr.getStatManager().getSchema());

		RowData tempTestSet = new RowData(cr.getStatManager().getSchema());
		RowData tempTrainingSet = (RowData) cr.getTrainingSet();

		DataTuple t;

		/**
		 * No unlabeled data was provided, it will be selected from the training set. If
		 * test set is not set, unlabeled data will be used as test set.
		 */
		if (m_UnlabeledData == null && tempTrainingSet.getNbUnlabeled() == 0) {
			m_UnlabeledData = new RowData(cr.getStatManager().getSchema());

			ClusLogger.info(
					"UnlabeledData not set. Unlabeled examples will be selected from training set (Percentage labeled = "
							+ m_PercentageLabeled + ")");

			RandomSelection randomSelection = new RandomSelection(tempTrainingSet.getNbRows(), m_PercentageLabeled,
					cr.getStatManager().getSettings().getGeneral().getRandomSeed());

			/**
			 * add selected instances from training set to unlabeled set we have to remove
			 * from training set instances which will be in the unlabeled set, because if we
			 * just set their weights to 0, they will anyway be selected in bootstrapping
			 * process
			 */
			if (m_IsInternalXVal) {
				/*
				 * we should do parameter optimization, we need unlabeled data in a separate set
				 */
				for (int i = 0; i < tempTrainingSet.getNbRows(); i++) {
					if (!randomSelection.isSelected(i)) {
						t = tempTrainingSet.getTuple(i).deepCloneTuple();
						t.makeUnlabeled();
						m_UnlabeledData.add(t);
						tempTestSet.add(tempTrainingSet.getTuple(i).deepCloneTuple());
					} else {
						m_TrainingSet.add(tempTrainingSet.getTuple(i).deepCloneTuple());
					}
				}
				// replace training set with the new one
				cr.setTrainingSet(m_TrainingSet);

			} else {
				/*
				 * we should not do parameter optimization, we don't need unlabeled data in a
				 * separate set
				 */
				for (int i = 0; i < tempTrainingSet.getNbRows(); i++) {
					if (!randomSelection.isSelected(i)) {
						tempTestSet.add(tempTrainingSet.getTuple(i).deepCloneTuple());
						t = tempTrainingSet.getTuple(i).deepCloneTuple();
						t.makeUnlabeled();
						tempTrainingSet.setTuple(t, i);
					}
				}
				cr.setTrainingSet(tempTrainingSet);
			}

			if (cr.getTestSet() == null) {
				ClusLogger.info("Testing data not set. Semi-supervised learning will be evaluated on unlabeled data.");
				cr.setTestSet(tempTestSet.getIterator());
			}

			return;
		}

		if (m_UnlabeledData != null) {
			/* if unlabeled examples are in the separate file */

			/**
			 * FIXME: if this is used with HMLC, slightly different results may occur, than
			 * if unlabeled data are provided in train set, I did not manage to find out why
			 */
			if (m_IsInternalXVal) {
				/*
				 * we should do parameter optimization, we need unlabeled data in a separate set
				 */
				for (int i = 0; i < m_UnlabeledData.getNbRows(); i++) {
					t = m_UnlabeledData.getTuple(i);
					if (!t.isUnlabeled()) { // just in any case, check if all tuples are unlabeled
						t.makeUnlabeled();
						m_UnlabeledData.setTuple(t, i);
					}
				}
			} else {
				/*
				 * we should not do parameter optimization, we don't need unlabeled data in a
				 * separate set
				 */
				for (int i = 0; i < m_UnlabeledData.getNbRows(); i++) {
					t = m_UnlabeledData.getTuple(i);
					if (!t.isUnlabeled()) { // just in any case, check if all tuples are unlabeled
						t.makeUnlabeled();
					}
					tempTrainingSet.add(t.deepCloneTuple());
				}
				cr.setTrainingSet(tempTrainingSet);
				m_UnlabeledData = null; // we don't need this anymore
			}
		}

		if (tempTrainingSet.getNbUnlabeled() > 0) {
			/* if unlabeled examples are in the training set */
			if (m_IsInternalXVal) {
				/*
				 * we should do parameter optimization, we need unlabeled data in a separate set
				 */
				if (m_UnlabeledData == null)
					m_UnlabeledData = new RowData(cr.getStatManager().getSchema());
				for (int i = 0; i < tempTrainingSet.getNbRows(); i++) {
					t = tempTrainingSet.getTuple(i);
					if (t.isUnlabeled())
						m_UnlabeledData.add(t.deepCloneTuple());
					else
						m_TrainingSet.add(t.deepCloneTuple());
				}
				cr.setTrainingSet(m_TrainingSet);
			}
		}
	}

	/**
	 * Notifies the foldRun (used in ClusEnsembleInduce) that this is an internal xval run, so different number of trees may be built.
	 * 
	 * Due to a stupid implementation of n_Bags being a static variable, we are forced to use a workaround.
	 * @param foldRun
	 */
	private void adjustNumberOfTrees(ClusRun foldRun) {
		// This is what we would like to do:
//		int[] ts_in_general = m_Schema.getSettings().getEnsemble().getNbBaggingSets().getIntVectorSorted();
//		int t_in_general = ts_in_general[ts_in_general.length - 1];
//		int n_trees_xval = m_Schema.getSettings().getSSL().getNumberOfTreesSupervisionOptimisation(t_in_general);
//		foldRun.getStatManager().getSettings().getEnsemble().setNbBags(n_trees_xval);
		// This is what we actually do:
		foldRun.setIsInternalXValRun(true);
	}
}
