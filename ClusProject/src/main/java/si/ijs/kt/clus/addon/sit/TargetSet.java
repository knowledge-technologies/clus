
package si.ijs.kt.clus.addon.sit;

import java.util.Iterator;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.jeans.util.IntervalCollection;


public class TargetSet extends java.util.TreeSet {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public TargetSet(ClusAttrType MainTarget) {
        super();
        this.add(MainTarget);
    }


    public TargetSet(TargetSet Other) {
        super();
        this.addAll(Other);
    }


    public TargetSet() {
        super();
    }


    /**
     * Creates a targetset from a ClusSchema and an IntervalCollection
     *
     * @param schema
     *        The schema containing all targets
     * @param targets
     *        IntervalCollection of the targets that should be added to this TargetSet
     */
    public TargetSet(ClusSchema schema, IntervalCollection targets) {
        super();
        targets.reset();
        while (targets.hasMoreInts()) {
            this.add(schema.getAttrType(targets.nextInt() - 1));// "the interval counts from 1"
        }
    }


    public int getIndex(ClusAttrType target) {
        Object[] set = this.toArray();
        for (int i = 0; i < set.length; i++) {
            if (set[i].equals(target)) { return i; }
        }
        return -1;

    }


    @Override
    public String toString() {
        Iterator targets = this.iterator();

        String result = "";
        while (targets.hasNext()) {
            ClusAttrType target = (ClusAttrType) targets.next();
            int idx = target.getIndex();
            result = result.concat(" ");
            result = result.concat(Integer.toString(idx + 1));
        }

        return result;

    }
}
