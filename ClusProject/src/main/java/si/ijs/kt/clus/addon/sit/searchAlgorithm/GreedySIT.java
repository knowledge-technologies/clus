
package si.ijs.kt.clus.addon.sit.searchAlgorithm;

import java.util.Iterator;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.addon.sit.mtLearner.MTLearner;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.util.ClusException;


public class GreedySIT extends SearchAlgorithmImpl {

    protected MTLearner learner;


    @Override
    public String getName() {
        return "GreedySIT";
    }


    @Override
    public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) throws ClusException {

        TargetSet best_set = new TargetSet(mainTarget);
        double best_err = eval(best_set, mainTarget);
        System.out.println("Best set = " + best_set + " with correlation " + best_err);

        boolean improvement = true;
        while (improvement) {

            improvement = false;
            double tmp_best_err = best_err;
            TargetSet tmp_best_set = best_set;
            System.out.println("Trying to improve this set:" + best_set);
            Iterator i = candidates.iterator();
            while (i.hasNext()) {
                TargetSet test = (TargetSet) best_set.clone();
                test.add(i.next());
                System.out.println("Eval:" + test);
                double err = eval(test, mainTarget);
                if (err > tmp_best_err) {
                    tmp_best_err = err;
                    tmp_best_set = test;
                    improvement = true;
                    System.out.println("-->improvement: " + err);
                }
            }

            best_err = tmp_best_err;
            best_set = tmp_best_set;
            System.out.println("Best set found:" + best_set + " correlation " + best_err);
        }

        return best_set;
    }

}
