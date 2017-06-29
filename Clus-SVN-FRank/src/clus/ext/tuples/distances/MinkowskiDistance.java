package clus.ext.tuples.distances;

import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.NumericAttrType;
import clus.data.type.TupleAttrType;
import clus.ext.tuples.Tuple;
import clus.ext.tuples.TupleDistance;
import clus.statistic.ClusDistance;

public class MinkowskiDistance extends TupleDistance{
	
	private int order = 2; //euclidean by default
	
	public MinkowskiDistance(TupleAttrType attr, int order, ClusDistance[] distances) {
		super(attr);
		this.order=order;
		this.childDistances = distances; 
	}
	
	public MinkowskiDistance(int order, ClusDistance[] distances) {
		super(null);
		this.order=order;
		this.childDistances = distances; 
	}

	public double calcDistance(Object t1, Object t2) {
		return calcDistance((Tuple)t1,(Tuple)t2);	
	}

	public double calcDistance(Tuple t1, Tuple t2) {
		int n = t1.length();
		double distance=0;
		if (!Double.isInfinite(order)){			
			int i=0;
			ClusAttrType[] types = this.m_Attr.getInnerTypes();
			for (Object element : t1.getValues()) {				
				//if (element!=null && !element.isMissing_value()){
				Object element1=t2.getValue(i);
				if (types[i] instanceof NumericAttrType){
					distance+=Math.pow((Double)element-(Double)element1,order);
				}else if (element!=null && element1!=null){
					distance += Math.pow(childDistances[i].calcDistance(element, element1),order);
				}
				i++;
			}
			distance = Math.pow(distance,1.0/order);		
		}else{
			//TODO calculate chebishev...max
		}
		return distance;
	}
	

}
