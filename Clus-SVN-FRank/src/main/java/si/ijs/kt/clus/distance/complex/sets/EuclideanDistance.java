
package si.ijs.kt.clus.distance.complex.sets;

import si.ijs.kt.clus.data.type.complex.SetAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.complex.SetDistance;
import si.ijs.kt.clus.distance.primitive.timeseries.TimeSeriesDist;
import si.ijs.kt.clus.ext.structuredTypes.Set;
import si.ijs.kt.clus.ext.timeseries.TimeSeries;
import si.ijs.kt.clus.main.settings.Settings;


public class EuclideanDistance extends SetDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public EuclideanDistance(ClusDistance innerDistance) {
        super(innerDistance);
    }


    public EuclideanDistance(SetAttrType attr, ClusDistance innerDistance) {
        super(attr, innerDistance);
    }


    @Override
    public double calcDistance(Set set1, Set set2) {
        //return square root of hamming if sets of nominal ... or ... 

        ClusDistance clusDistance = m_ChildDistances[0];
        double distance = set1.getValues().length + set2.getValues().length;
        for (Object element1 : set1.getValues()) {
            double dist = 1;
            for (Object element2 : set2.getValues()) {
                if (clusDistance == null) {
                    dist = Math.abs((Double) element1 - (Double) element2);
                }
                else {
                    if (element1 instanceof TimeSeries) {
                        TimeSeries ts1 = (TimeSeries) element1;
                        TimeSeries ts2 = (TimeSeries) element2;
                        TimeSeriesDist tsd = (TimeSeriesDist) clusDistance;
                        dist = tsd.calcDistance(ts1, ts2);
                    }
                    //@todo implement for the other types as well...
                }
                if (dist == 0) {
                    distance -= 2;
                    break;
                }
            }
        }

        return Math.sqrt(distance);
    }


    @Override
    public String getDistanceName() {
        return "Euclidean distance (sets)";
    }
}
