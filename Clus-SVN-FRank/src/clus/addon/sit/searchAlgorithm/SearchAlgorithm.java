
package clus.addon.sit.searchAlgorithm;

import clus.addon.sit.TargetSet;
import clus.addon.sit.mtLearner.MTLearner;
import clus.data.type.ClusAttrType;
import clus.main.Settings;


public interface SearchAlgorithm {

    public TargetSet search(ClusAttrType mainTarget, TargetSet candidates);


    public void setMTLearner(MTLearner learner);


    public String getName();


    public void setSettings(Settings s);

}
