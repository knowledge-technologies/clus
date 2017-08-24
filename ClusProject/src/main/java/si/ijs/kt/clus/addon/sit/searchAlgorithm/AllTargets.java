
package si.ijs.kt.clus.addon.sit.searchAlgorithm;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.addon.sit.mtLearner.MTLearner;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;


public class AllTargets implements SearchAlgorithm {

    protected MTLearner m_MTLearner;


    /**
     * This class will always return the full target candidates set.
     */
    @Override
    public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) {
        /*
         * By design returns back the full candidates set
         */
        return new TargetSet(candidates);
    }


    @Override
    public void setMTLearner(MTLearner learner) {
        this.m_MTLearner = learner;
    }


    @Override
    public String getName() {
        return "AllTargets";
    }


    @Override
    public void setSettings(Settings s) {
    }
}
