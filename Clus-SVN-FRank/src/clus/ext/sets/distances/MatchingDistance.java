package clus.ext.sets.distances;

import clus.data.type.SetAttrType;
import clus.ext.sets.Set;
import clus.ext.sets.SetDistance;
import clus.main.settings.Settings;
import clus.statistic.ClusDistance;

public class MatchingDistance extends SetDistance {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public MatchingDistance(SetAttrType attr, ClusDistance innerDistance) {
		super(attr,innerDistance);
	}

	@Override
	public double calcDistance(Set s1, Set s2) {
		
		return 0;
	}

}
