
package clus.distance.complex.tuples;

import clus.data.type.ClusAttrType;
import clus.data.type.complex.TupleAttrType;
import clus.data.type.primitive.NumericAttrType;
import clus.distance.ClusDistance;
import clus.distance.complex.TupleDistance;
import clus.ext.structuredTypes.Tuple;
import clus.main.settings.Settings;


public class MinkowskiDistance extends TupleDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private int m_Order = 2; //euclidean by default


    public MinkowskiDistance(TupleAttrType attr, int order, ClusDistance[] distances) {
        super(attr);
        this.m_Order = order;
        this.m_ChildDistances = distances;
    }


    public MinkowskiDistance(int order, ClusDistance[] distances) {
        super(null);
        this.m_Order = order;
        this.m_ChildDistances = distances;
    }


    public double calcDistance(Object t1, Object t2) {
        return calcDistance((Tuple) t1, (Tuple) t2);
    }


    public double calcDistance(Tuple t1, Tuple t2) {
        double distance = 0;
        if (!Double.isInfinite(m_Order)) {
            int i = 0;
            ClusAttrType[] types = this.m_Attr.getInnerTypes();
            for (Object element : t1.getValues()) {
                //if (element!=null && !element.isMissing_value()){
                Object element1 = t2.getValue(i);
                if (types[i] instanceof NumericAttrType) {
                    distance += Math.pow((Double) element - (Double) element1, m_Order);
                }
                else if (element != null && element1 != null) {
                    distance += Math.pow(m_ChildDistances[i].calcDistance(element, element1), m_Order);
                }
                i++;
            }
            distance = Math.pow(distance, 1.0 / m_Order);
        }
        else {
            //TODO calculate chebishev...max
            throw new UnsupportedOperationException("clus.ext.tuples.distances.MinkowskiDistance.calcDistance(Tuple, Tuple); m_Order == Infinity");
        }
        return distance;
    }


    @Override
    public String getDistanceName() {
        return "Minkowski distance (tuples)";
    }

}
