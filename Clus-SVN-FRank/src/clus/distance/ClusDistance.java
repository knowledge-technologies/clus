
package clus.distance;

import java.io.Serializable;

import clus.algo.kNN.distance.attributeWeighting.AttributeWeighting;
import clus.data.rows.DataTuple;
import clus.main.settings.Settings;
import clus.statistic.ClusStatistic;



public abstract class ClusDistance implements Serializable {

    public AttributeWeighting m_AttrWeighting;

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public double calcDistance(DataTuple t1, DataTuple t2) {
        return Double.POSITIVE_INFINITY;
    }


    public double calcDistance(Object t1, Object t2) {
        System.err.println("Method unimplemented ... ");
        new Exception("Method unimplemented ... ").printStackTrace();
        System.exit(1);
        return Double.POSITIVE_INFINITY;
    }


    public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
        return Double.POSITIVE_INFINITY;
    }


    public ClusDistance getBasicDistance() {
        return this;
    }


    public abstract String getDistanceName();


    public AttributeWeighting getWeighting() {
        return m_AttrWeighting;
    }


    public void setWeighting(AttributeWeighting weighting) {
        m_AttrWeighting = weighting;
    }
}
