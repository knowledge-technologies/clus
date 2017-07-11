
package clus.ext.tuples.distances;

import clus.data.type.structured.TupleAttrType;
import clus.distance.ClusDistance;
import clus.ext.tuples.Tuple;
import clus.ext.tuples.TupleDistance;
import clus.main.settings.Settings;


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

    
    public double calcDistance(Tuple t1, Tuple t2) {
        return new MinkowskiDistance(m_Attr, 2, this.m_ChildDistances).calcDistance(t1, t2);
    }


    @Override
    public String getDistanceName() {
        return "Euclidean distance (tuples)";
    }

}
