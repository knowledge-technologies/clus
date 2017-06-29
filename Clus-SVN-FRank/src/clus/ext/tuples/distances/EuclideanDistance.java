
package clus.ext.tuples.distances;

import clus.data.type.TupleAttrType;
import clus.ext.tuples.Tuple;
import clus.ext.tuples.TupleDistance;
import clus.main.Settings;
import clus.statistic.ClusDistance;


public class EuclideanDistance extends TupleDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public EuclideanDistance(TupleAttrType attr, ClusDistance[] distances) {
        super(attr);
        this.m_ChildDistances = distances;
    }


    public EuclideanDistance(ClusDistance[] distances) {
        super(null);
        this.m_ChildDistances = distances;
    }


    @Override
    public double calcDistance(Tuple t1, Tuple t2) {
        return new MinkowskiDistance(m_Attr, 2, this.m_ChildDistances).calcDistance(t1, t2);
    }

}
