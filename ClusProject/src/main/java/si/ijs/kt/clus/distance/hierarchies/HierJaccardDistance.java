
package si.ijs.kt.clus.distance.hierarchies;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.hierarchies.ClassesAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;


public class HierJaccardDistance extends ClusDistance {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClassesAttrType m_Attr;


    public HierJaccardDistance(ClassesAttrType attr) {
        m_Attr = attr;
    }


    @Override
    public double calcDistance(DataTuple t1, DataTuple t2) {
        ClassesTuple cl1 = m_Attr.getValue(t1);
        ClassesTuple cl2 = m_Attr.getValue(t2);

        ClusLogger.info("Computing Jaccard Distance:");
        ClusLogger.info("Tuple 1: " + cl1.toString());
        ClusLogger.info("Tuple 2: " + cl2.toString());
        throw new RuntimeException("Math.random() is not supposed to be returned, is it?");
        // return Math.random();
    }


    @Override
    public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
        throw new RuntimeException("Double.POSITIVE_INFINITY is not supposed to be returned, is it?");
        // return Double.POSITIVE_INFINITY;
    }


    @Override
    public String getDistanceName() {
        return "Hierarchical Jaccard Distance";
    }
}
