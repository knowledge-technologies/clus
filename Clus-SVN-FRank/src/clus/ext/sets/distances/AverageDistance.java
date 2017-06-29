package clus.ext.sets.distances;

import clus.data.type.SetAttrType;
import clus.ext.sets.Set;
import clus.ext.sets.SetDistance;
import clus.main.Settings;
import clus.statistic.ClusDistance;

public class AverageDistance extends SetDistance {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public AverageDistance(SetAttrType attr, ClusDistance childDistance) {
		super(attr,childDistance);
	}

	public double calcDistance(Object s1, Object s2) {
		return calcDistance((Set)s1,(Set)s2);
	}
	
	@Override
	public double calcDistance(Set s1, Set s2) {
		ClusDistance clusDistance  = childDistances[0];
		double distance=0;
		s1.getValues();
		s2.getValues();
		for (Object object1 : s1.getValues()) {
			for (Object object2 : s2.getValues()) {		
				if (clusDistance==null){
					distance+=Math.abs((Double)object1-(Double)object2);
				}else{
					distance+=clusDistance.calcDistance(object1, object2);
				}
			}
		}
		distance = distance/(s1.size())*(s2.size());
		return distance;
	}

}
