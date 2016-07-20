package clus.ext.hierarchical;

import clus.data.rows.DataTuple;
import clus.main.Settings;
import clus.statistic.*;

public class HierJaccardDistance extends ClusDistance {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected ClassesAttrType m_Attr;

	public HierJaccardDistance(ClassesAttrType attr) {
		m_Attr = attr;
	}

	public double calcDistance(DataTuple t1, DataTuple t2) {
		ClassesTuple cl1 = m_Attr.getValue(t1);
		ClassesTuple cl2 = m_Attr.getValue(t2);

		System.out.println("Computing Jaccard Distance:");
		System.out.println("Tuple 1: "+ cl1.toString());
		System.out.println("Tuple 2: "+ cl2.toString());

		return Math.random();
	}

	public double calcDistanceToCentroid(DataTuple t1, ClusStatistic s2) {
		return Double.POSITIVE_INFINITY;
	}

	public String getDistanceName() {
		return "Hierarchical Jaccard Distance";
	}
}
