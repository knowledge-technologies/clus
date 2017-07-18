
package si.ijs.kt.clus.addon.sit.searchAlgorithm;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.addon.sit.mtLearner.MTLearner;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;


/**
 * Fake learner which returns the maintarget.
 *
 * @author beau
 *
 */
public class OneTarget implements SearchAlgorithm {

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
