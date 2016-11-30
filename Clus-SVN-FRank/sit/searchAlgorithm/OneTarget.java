package sit.searchAlgorithm;

import clus.data.type.ClusAttrType;
import clus.main.Settings;
import sit.TargetSet;
import sit.mtLearner.MTLearner;


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
