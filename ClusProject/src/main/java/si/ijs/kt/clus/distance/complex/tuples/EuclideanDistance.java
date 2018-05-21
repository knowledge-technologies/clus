
package si.ijs.kt.clus.distance.complex.tuples;

import si.ijs.kt.clus.data.type.complex.TupleAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.complex.TupleDistance;
import si.ijs.kt.clus.ext.structuredTypes.Tuple;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;


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
    public double calcDistance(Tuple t1, Tuple t2) throws ClusException {
        return new MinkowskiDistance(m_Attr, 2, this.m_ChildDistances).calcDistance(t1, t2);
    }


    @Override
    public String getDistanceName() {
        return "Euclidean distance (tuples)";
    }

}
