package clus.statistic;

import java.io.Serializable;

import clus.algo.kNN.distance.attributeWeighting.AttributeWeighting;
import clus.data.rows.DataTuple;
import clus.main.Settings;

public class ClusDistance implements Serializable {
	public AttributeWeighting m_AttrWeighting;

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public double calcDistance(DataTuple t1, DataTuple t2) {
		return Double.POSITIVE_INFINITY;
	}

	public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
		return Double.POSITIVE_INFINITY;
	}

	public ClusDistance getBasicDistance() {
		return this;
	}

	public String getDistanceName() {
		return "UnknownDistance";
	}
	public AttributeWeighting getWeighting(){
		return m_AttrWeighting;
	}
	public void setWeighting(AttributeWeighting weighting){
		m_AttrWeighting = weighting;
	}
}
