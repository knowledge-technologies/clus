
package si.ijs.kt.clus.addon.sit.searchAlgorithm;

import java.util.ArrayList;
import java.util.Iterator;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.exception.ClusException;


public class TC extends SearchAlgorithmImpl {

    @Override
    public String getName() {
        return "TC";
    }


    @Override
    public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) throws ClusException {

        Iterator i = candidates.iterator();
        Iterator i2 = candidates.iterator();
        while (i.hasNext()) {
            ClusAttrType target = (ClusAttrType) i.next();
            i2 = candidates.iterator();

            TargetSet base = new TargetSet(target);
            double base_err = eval(base, target);

            ArrayList l = new ArrayList();
            while (i2.hasNext()) {

                TargetSet test = new TargetSet(target);
                ClusAttrType from = (ClusAttrType) i2.next();
                test.add(from);
                // ClusLogger.info("Transfer from:"+from+" to: "+target);
                double err = eval(test, target);
                l.add((err - base_err) / base_err);

            }
            System.out.print("[");
            for (int c = (l.size() - 1); c > 0; c--) {
                System.out.print(l.get(c) + ",");
            }
            ClusLogger.info(l.get(0) + "],");

        }

        return candidates;
    }
}
