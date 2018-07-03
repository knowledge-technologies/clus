
package si.ijs.kt.clus.distance;

import java.io.Serializable;

import org.apache.commons.lang.NotImplementedException;

import si.ijs.kt.clus.algo.kNN.distance.attributeWeighting.AttributeWeighting;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.exception.ClusException;



public abstract class ClusDistance implements Serializable {

    protected AttributeWeighting m_AttrWeighting;

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;
    /** Attribute indices that must be included in distance computations: typically, all descriptive attributes, or nominal descriptive attributes (for sparse data). */
    protected ClusAttrType[] m_Attributes;
    
    protected boolean m_IsSparse;


    public double calcDistance(DataTuple t1, DataTuple t2) throws ClusException {
        return Double.POSITIVE_INFINITY;
    }


    public double calcDistance(Object t1, Object t2) throws ClusException {
        throw new NotImplementedException();
    }


    public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) throws ClusException {
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
