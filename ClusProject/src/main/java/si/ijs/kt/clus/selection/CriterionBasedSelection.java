
package si.ijs.kt.clus.selection;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.util.ClusLogger;


public class CriterionBasedSelection {

    public static final boolean isMissing(DataTuple tuple, ClusAttrType[] attrs) {
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isMissing(tuple))
                return true;
        }
        return false;
    }


    public static final void clearMissingFlagTargetAttrs(ClusSchema schema) {
        ClusAttrType[] targets = schema.getAllAttrUse(AttributeUseType.Target);
        for (int i = 0; i < targets.length; i++) {
            targets[i].setNbMissing(0);
        }
    }


    public static final RowData removeMissingTarget(RowData data) {
        int nbrows = data.getNbRows();
        ClusAttrType[] targets = data.getSchema().getAllAttrUse(AttributeUseType.Target);
        BitMapSelection sel = new BitMapSelection(nbrows);
        for (int i = 0; i < nbrows; i++) {
            DataTuple tuple = data.getTuple(i);
            if (!isMissing(tuple, targets)) {
                sel.select(i);
            }
        }
        if (sel.getNbSelected() != nbrows) {
            ClusLogger.info("Tuples with missing target: " + (nbrows - sel.getNbSelected()));
            return (RowData) data.selectFrom(sel, null); // no problem, parallelism comes later
        }
        else {
            return data;
        }
    }
}
