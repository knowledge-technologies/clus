
package si.ijs.kt.clus.addon.sit.searchAlgorithm;

import java.util.Iterator;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;


public class NoStopSearch extends SearchAlgorithmImpl {

    @Override
    public String getName() {
        return "NoStop";
    }


    @Override
    public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) throws ClusException {

        TargetSet best_set = new TargetSet(mainTarget);
        double best_err = eval(best_set, mainTarget);

        ClusLogger.info("Best set = " + best_set + " MSE " + (best_err - 1) * -1);

        TargetSet overal_best_set = new TargetSet(mainTarget);
        double overal_best_err = eval(best_set, mainTarget);

        boolean c = true;
        while (c) {

            double tmp_best_err = Double.MAX_VALUE * -1;
            TargetSet tmp_best_set = best_set;
            ClusLogger.info("Trying to improve this set:" + best_set);
            Iterator i = candidates.iterator();
            while (i.hasNext()) {
                TargetSet test = (TargetSet) best_set.clone();

                ClusAttrType cat = (ClusAttrType) i.next();
                if (!test.contains(cat)) {
                    test.add(cat);

                    double err = eval(test, mainTarget);
                    ClusLogger.info("Eval:" + test + "->" + (err - 1) * -1);

                    if (err > tmp_best_err) {// && test.size() != best_set.size()){
                        tmp_best_err = err;
                        tmp_best_set = test;
                        ClusLogger.info("-->improvement ");
                    }
                }
            }

            best_err = tmp_best_err;
            best_set = tmp_best_set;

            if (best_err > overal_best_err) {
                overal_best_err = best_err;
                overal_best_set = best_set;
                ClusLogger.info("-->OVERAL improvement");
            }
            else {
                ClusLogger.info("-->NO overal improvement...");
            }
            if (tmp_best_set.size() == candidates.size()) {
                c = false;
            }
            ClusLogger.info("Best set found:" + best_set + " correlation " + best_err);
        }

        ClusLogger.info("Overal best set found:" + overal_best_set + " correlation " + overal_best_err);

        return overal_best_set;
    }

}
