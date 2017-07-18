
package si.ijs.kt.clus.ext.hierarchical;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;


public class HierJaccardDistance extends ClusDistance {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ClassesAttrType m_Attr;


    public HierJaccardDistance(ClassesAttrType attr) {
        m_Attr = attr;
    }


    public double calcDistance(DataTuple t1, DataTuple t2) {
        ClassesTuple cl1 = m_Attr.getValue(t1);
        ClassesTuple cl2 = m_Attr.getValue(t2);

        System.out.println("Computing Jaccard Distance:");
        System.out.println("Tuple 1: " + cl1.toString());
        System.out.println("Tuple 2: " + cl2.toString());
        throw new RuntimeException("Math.random() is not supposed to be returned, is it?");
        // return Math.random();
    }


    public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
        return Double.POSITIVE_INFINITY;
    }


    public String getDistanceName() {
        return "Hierarchical Jaccard Distance";
    }
}
