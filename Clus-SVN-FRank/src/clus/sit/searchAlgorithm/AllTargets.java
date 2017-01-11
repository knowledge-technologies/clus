
package clus.sit.searchAlgorithm;

import clus.data.type.ClusAttrType;
import clus.main.Settings;
import clus.sit.TargetSet;
import clus.sit.mtLearner.MTLearner;


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
