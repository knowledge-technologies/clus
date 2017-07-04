
package clus.addon.sit.searchAlgorithm;

import clus.addon.sit.TargetSet;
import clus.addon.sit.mtLearner.MTLearner;
import clus.data.type.ClusAttrType;
import clus.main.settings.Settings;


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
