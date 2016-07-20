package clus.ext.ensembles;

import java.util.ArrayList;
import java.util.Arrays;

import jeans.util.array.MDoubleArrayComparator;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.DataTuple;
import clus.ext.hierarchical.WHTDStatistic;
import clus.main.ClusStatManager;
import clus.model.ClusModel;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStat;

public class ClusBoostingForest extends ClusForest {

	protected ArrayList m_BetaI = new ArrayList();
	protected transient MDoubleArrayComparator m_Compare = new MDoubleArrayComparator(0);

	public ClusBoostingForest() {
		super();
	}

	public ClusBoostingForest(ClusStatManager statmgr) {
		super(statmgr);
	}

	public void addModelToForest(ClusModel model, double beta) {
		super.addModelToForest(model);
		m_BetaI.add(new Double(beta));
	}

	public double getBetaI(int i) {
		return ((Double)m_BetaI.get(i)).doubleValue();
	}

	public double getMedianThreshold() {
		double sum = 0.0;
		for (int i = 0; i < m_BetaI.size(); i++) {
			sum += Math.log(1 / getBetaI(i));
		}
		return 0.5*sum;
	}

	public ClusStatistic predictWeighted(DataTuple tuple) {
		ClusStatistic predicted = m_Stat.cloneSimple();
		//predictWeightedRegression((RegressionStat)predicted, tuple);
		for (int i = 0; i < getNbModels(); i++) {
			predicted.addPrediction(getModel(i).predictWeighted(tuple), 1.0/getNbModels());
		}
		predicted.computePrediction();
		return predicted;
	}

	public void predictWeightedRegression(RegressionStat predicted,	DataTuple tuple) {
		double[] result = predicted.getNumericPred();
		double[][] treePredictions = new double[getNbModels()][];
		for (int i = 0; i < treePredictions.length; i++) {
			RegressionStat pred = (RegressionStat)getModel(i).predictWeighted(tuple);
			treePredictions[i] = pred.getNumericPred();
		}
		double medianThr = getMedianThreshold();
		double[][] preds = new double[getNbModels()][2];
		int nbAttr = predicted.getNbAttributes();
		for (int i = 0; i < nbAttr; i++) {
			// compute weighted median of predictions of individual trees
			// weight of tree = log(1/beta)
			for (int j = 0; j < getNbModels(); j++) {
				preds[j][0] = treePredictions[j][i];
				preds[j][1] = Math.log(1 / getBetaI(j));
			}
			Arrays.sort(preds, m_Compare);
			int j = 0;
			double sum = 0.0;
			while (true) {
				sum += preds[j][1];
				if (sum >= medianThr) break;
				j++;
			}
			result[i] = preds[j][0];
		}
	}

	public ClusBoostingForest cloneBoostingForestWithThreshold(double threshold) {
		ClusBoostingForest clone = new ClusBoostingForest();
		clone.setModels(getModels());
		clone.m_BetaI = m_BetaI;
		WHTDStatistic stat = (WHTDStatistic)getStat().cloneStat();
		stat.copyAll(getStat());
		stat.setThreshold(threshold);
		clone.setStat(stat);
		return clone;
	}
}
