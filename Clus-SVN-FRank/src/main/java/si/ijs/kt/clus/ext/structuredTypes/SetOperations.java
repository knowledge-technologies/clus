package si.ijs.kt.clus.ext.structuredTypes;

import si.ijs.kt.clus.distance.ClusDistance;

public class SetOperations {

	public static int getCardinality(Set s){
		return s.size();
	}
	public  static int getIntersectionCardinality(Set s1, Set s2, ClusDistance d){
		int intersection = 0;
		for (Object o1 : s1.getValues()) {
			for (Object o2 : s2.getValues()) {
				Double dist = Double.MAX_VALUE;
				if (d==null){					
					dist=Math.abs((Double)o1-(Double)o2);					
				}else{
					dist = d.calcDistance(o1, o2);
				}
				if (dist==0){
					intersection++;
				}
			}			
		}
		return intersection; 
	}
	
	public static int getUnionCardinality(Set s1, Set s2, ClusDistance d){
		return s1.size()+s2.size()-getIntersectionCardinality(s1, s2, d);
	}
	
	public static int getDifferenceCardinality(Set s1, Set s2, ClusDistance d){
		return s1.size()+s2.size()-2*getIntersectionCardinality(s1, s2, d);
	}
	
	
}
