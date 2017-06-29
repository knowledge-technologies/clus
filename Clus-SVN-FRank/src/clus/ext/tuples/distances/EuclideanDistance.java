package clus.ext.tuples.distances;

import clus.data.type.TupleAttrType;
import clus.ext.tuples.Tuple;
import clus.ext.tuples.TupleDistance;
import clus.statistic.ClusDistance;

public class EuclideanDistance extends TupleDistance{

	public EuclideanDistance(TupleAttrType attr, ClusDistance[] distances) {		
		super(attr);
		this.childDistances = distances; 
	}

	public EuclideanDistance(ClusDistance[] distances) {		
		super(null);
		this.childDistances = distances; 
	}
	
	@Override
	public double calcDistance(Tuple t1, Tuple t2) {
		return new MinkowskiDistance(m_Attr,2,this.childDistances).calcDistance(t1, t2);
	}

}
