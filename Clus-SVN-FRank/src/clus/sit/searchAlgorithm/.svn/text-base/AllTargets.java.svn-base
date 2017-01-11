package sit.searchAlgorithm;

import java.util.ArrayList;
import java.util.Collection;

import sit.TargetSet;
import sit.mtLearner.MTLearner;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NumericAttrType;
import clus.error.ClusErrorList;
import clus.error.PearsonCorrelation;
import clus.error.RMSError;
import clus.error.SpearmanRankCorrelation;
import clus.main.Settings;

public class AllTargets implements SearchAlgorithm {

	protected MTLearner m_MTLearner;

	/**
	 * This class will always return the full target candidates set.
	 */
	public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) {
		/*
		 * By design returns back the full candidates set
		 */
		return new TargetSet(candidates);
	}

	public void setMTLearner(MTLearner learner) {
		this.m_MTLearner = learner;
	}

	public String getName() {
		return "AllTargets";
	}

	public void setSettings(Settings s) {
	}
}
