package si.ijs.kt.clus.algo.rules.tune;

import java.io.IOException;
import java.util.Random;

import si.ijs.kt.clus.algo.ClusInductionAlgorithm;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.rules.ClusRuleClassifier;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.RMSError;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.ClusSummary;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSVotingType;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.selection.ClusSelection;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.ClusRandom;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.math.MathUtil;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;

public class FIREROSTune extends ClusRuleClassifier {

	protected ClusInductionAlgorithmType m_Class;
	protected String[] m_ROSSubspaceSizes;

	public FIREROSTune(ClusInductionAlgorithmType clss) {
		super(clss.getClus());
		m_Class = clss;
	}

	public FIREROSTune(ClusInductionAlgorithmType clss, String[] ROSSubspaceSizes) {
		this(clss);
		m_ROSSubspaceSizes = ROSSubspaceSizes;
	}

	@Override
	public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs)
			throws ClusException, IOException {
		return m_Class.createInduce(schema, sett, cargs);
	}

	@Override
	public void printInfo() {
		ClusLogger.info("Fitted Rule Ensemble with Random Output Selections (FIRE-ROS) (Tuning)");
		ClusLogger.info("Heuristic: " + getStatManager().getHeuristicName());
	}

	private final void showFold(int i) {
		if (i != 0) {
			ClusLogger.fine(" ");
		}
		ClusLogger.fine(String.valueOf(i + 1));
	}

	public ClusErrorList createTuneError(ClusStatManager mgr) {
		ClusErrorList parent = new ClusErrorList();
		NumericAttrType[] num = mgr.getSchema().getNumericAttrUse(AttributeUseType.Target);
		NominalAttrType[] nom = mgr.getSchema().getNominalAttrUse(AttributeUseType.Target);
		if (nom.length != 0) {
			parent.addError(new Accuracy(parent, nom));
		}
		if (num.length != 0) {
			parent.addError(new RMSError(parent, num));
		}
		return parent;
	}

	public final ClusRun partitionDataBasic(ClusData data, ClusSelection sel, ClusSummary summary, int idx)
			throws IOException, ClusException, InterruptedException {
		ClusRun cr = new ClusRun(data.cloneData(), summary);
		if (sel != null) {
			if (sel.changesDistribution()) {
				((RowData) cr.getTrainingSet()).update(sel);
			} else {
				ClusData val = cr.getTrainingSet().select(sel);
				cr.setTestSet(((RowData) val).getIterator());
			}
		}
		cr.setIndex(idx);
		cr.copyTrainingData();
		return cr;
	}

	public double doParamXVal(RowData trset, RowData pruneset) throws Exception {
		int prevVerb = getSettings().getGeneral().enableVerbose(0);
		ClusStatManager mgr = getStatManager();
		ClusSummary summ = new ClusSummary();
		summ.setStatManager(getStatManager());
		summ.addModelInfo(ClusModel.ORIGINAL).setTestError(createTuneError(mgr));
		ClusRandom.initialize(getSettings());

		if (pruneset != null) {
			ClusRun cr = new ClusRun(trset.cloneData(), summ);
			ClusModel model = m_Class.induceSingleUnpruned(cr);

			cr.addModelInfo(ClusModel.ORIGINAL).setModel(model);
			cr.addModelInfo(ClusModel.ORIGINAL).setTestError(createTuneError(mgr));
			m_Clus.calcError(pruneset.getIterator(), ClusModelInfo.TEST_ERR, cr, null);
			summ.addSummary(cr);
		} else {
			// Next does not always use same partition!
			// Random random = ClusRandom.getRandom(ClusRandom.RANDOM_PARAM_TUNE);
			Random random = new Random(0);
			int nbfolds = Integer.parseInt(getSettings().getModel().getTuneFolds());
			XValMainSelection sel = new XValRandomSelection(trset.getNbRows(), nbfolds, random);

			ClusModel dummy = null;

			for (int i = 0; i < nbfolds; i++) {
				showFold(i);
				XValSelection msel = new XValSelection(sel, i);
				ClusRun cr = partitionDataBasic(trset, msel, summ, i + 1);
				ClusModelInfo def_info = cr.addModelInfo(ClusModel.DEFAULT);

				// this is needed just to fill the placeholder for ClusModel.DEFAULT
				if (dummy == null) {
					dummy = ClusDecisionTree.induceDefault(cr);
				}
				def_info.setModel(dummy);

				ClusModel model = m_Class.induceSingleUnpruned(cr);
				cr.addModelInfo(ClusModel.ORIGINAL).setModel(model);
				cr.addModelInfo(ClusModel.ORIGINAL).setTestError(createTuneError(mgr));
				m_Clus.calcError(cr.getTestIter(), ClusModelInfo.TEST_ERR, cr, null);
				summ.addSummary(cr);
			}
			ClusLogger.fine();
		}
		ClusModelInfo mi = summ.getModelInfo(ClusModel.ORIGINAL);
		getSettings().getGeneral().enableVerbose(prevVerb);
		ClusError err = mi.getTestError().getFirstError();

		return err.getModelError();
	}

	public void findBestROSParameters(RowData trset, RowData pruneset) throws Exception {
		Integer idxBestSubspace = null;

		boolean errorShouldBeLow = createTuneError(getStatManager()).getFirstError().shouldBeLow();
		double bestError = errorShouldBeLow ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		EnsembleROSVotingType bestVotingType = null;

		// remember original settings
		int originalMaxIter = getSettings().getRules().getOptGDMaxIter();
		int originalEnsembleSize = getSettings().getEnsemble().getNbBaggingSets().getInt();

		// change original settings to something more tractable
		getSettings().getEnsemble().setNbBags(10);
		getSettings().getRules().setOptGDMaxIter(4000);

		for (EnsembleROSVotingType vt : EnsembleROSVotingType.values()) {
			for (int i = 0; i < m_ROSSubspaceSizes.length; i++) {

				getSettings().getEnsemble().setEnsembleROSVotingType(vt);
				getSettings().getEnsemble().setNbRandomTargetAttrString(m_ROSSubspaceSizes[i]);
				ClusLogger.fine("Try for ROS subspace size = " + m_ROSSubspaceSizes[i] + " with voting: " + vt);

				double err = doParamXVal(trset, pruneset);
				ClusLogger.fine("-> " + err);

				if ((errorShouldBeLow && err < bestError - MathUtil.C1E_16)
						|| (!errorShouldBeLow && err > bestError + MathUtil.C1E_16)) {
					bestError = err;
					idxBestSubspace = i;
					bestVotingType = vt;

					ClusLogger.fine(" *");
				}
			}
		}

		getSettings().getEnsemble().setNbRandomTargetAttrString(m_ROSSubspaceSizes[idxBestSubspace]);
		getSettings().getEnsemble().setEnsembleROSVotingType(bestVotingType);

		ClusLogger.fine("Best FIRE-ROS setting is: " + m_ROSSubspaceSizes[idxBestSubspace] + " with " + bestVotingType);

		// return original settings
		getSettings().getEnsemble().setNbBags(originalEnsembleSize);
		getSettings().getRules().setOptGDMaxIter(originalMaxIter);
	}

	@Override
	public void induceAll(ClusRun cr) throws ClusException, IOException {
		try {
			// Find optimal F-test value
			RowData valid = (RowData) cr.getPruneSet();
			RowData train = (RowData) cr.getTrainingSet();
			findBestROSParameters(train, valid);
			ClusLogger.info();

			// Induce final model
			cr.combineTrainAndValidSets();
			ClusRandom.initialize(getSettings());
			m_Class.induceAll(cr);
		} catch (Exception e) {
			ClusLogger.severe(e.toString());
		}
	}
}
