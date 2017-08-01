
package si.ijs.kt.clus.addon.sit.searchAlgorithm;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.addon.sit.mtLearner.MTLearner;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;


public interface SearchAlgorithm {

    public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) throws ClusException;


    public void setMTLearner(MTLearner learner);


    public String getName();


    public void setSettings(Settings s);

}
