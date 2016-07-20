package sit.searchAlgorithm;

import sit.TargetSet;
import sit.mtLearner.MTLearner;
import clus.data.type.ClusAttrType;
import clus.main.Settings;


/**
 * Fake learner which returns the maintarget.
 *
 * @author beau
 *
 */
public class OneTarget implements SearchAlgorithm{

	public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) {
		return new TargetSet(mainTarget);
	}

	public void setMTLearner(MTLearner learner) {
	}

	public String getName() {
		return "OneTarget";
	}

	public void setSettings(Settings s) {

	}

}
