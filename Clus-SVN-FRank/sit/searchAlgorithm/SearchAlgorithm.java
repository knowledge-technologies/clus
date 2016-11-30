package sit.searchAlgorithm;

import clus.data.type.ClusAttrType;
import clus.main.Settings;
import sit.TargetSet;
import sit.mtLearner.MTLearner;

public interface SearchAlgorithm {


	public TargetSet search(ClusAttrType mainTarget,TargetSet candidates);

	public void setMTLearner(MTLearner learner);

	public String getName();

	public void setSettings(Settings s);

}
