package clus.statistic;

import java.util.Random;
import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.main.Settings;

public class SumPairwiseDistancesStat extends BitVectorStat {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int linearParameter = 10;
	public final static Random m_Random = new Random(0);

	protected double m_SVarS;
	protected ClusDistance m_Distance;
	protected int m_Efficiency = 2;

	public SumPairwiseDistancesStat(ClusDistance dist) {
		m_Distance = dist;
	}

	public SumPairwiseDistancesStat(ClusDistance dist, int efflvl) {
		m_Distance = dist;
		m_Efficiency = efflvl;
	}

	public ClusStatistic cloneStat() {
		return new SumPairwiseDistancesStat(m_Distance, m_Efficiency);
	}

	public double getSVarS(ClusAttributeWeights scale, RowData data) {
		optimizePreCalc(data);
		return m_SVarS;
	}

	public int getEfficiencyLevel() {
		return m_Efficiency;
	}

	public void optimizePreCalc(RowData data) {
		if (!m_Modified) return;
		switch (getEfficiencyLevel()) {
			case 1:
				optimizeLogPreCalc(data);
				break;
			case 2:
				optimizeLinearPreCalc(data);
				break;
			default :
				optimizePreCalcExact(data);
				break;
		}
		m_Modified = false;
	}

	public double calcDistance(DataTuple t1, DataTuple t2) {
		return m_Distance.calcDistance(t1, t2);
	}

	public double calcDistanceToCentroid(DataTuple t1) {
		return m_Distance.calcDistanceToCentroid(t1, this);
	}

	public void optimizePreCalcExact(RowData data) {
		m_SVarS = 0.0;
		double sumWiDiag = 0.0;
		double sumWiTria = 0.0;
		int nb = m_Bits.size();
		for (int i = 0; i < nb; i++) {
			if (m_Bits.getBit(i)) {
				DataTuple a = data.getTuple(i);
				double a_weight = a.getWeight();
				// sum up elements in upper triangle of matrix (and give double weights)
				for (int j = 0; j < i; j++) {
					if (m_Bits.getBit(j)) {
						DataTuple b = data.getTuple(j);
						double wi = a_weight*b.getWeight();
						double d = calcDistance(a, b);
						m_SVarS += wi * d;
						sumWiTria += wi;
					}
				}
				// sum up weights for elements on diagonal (with corresponding zero distances)
				sumWiDiag += a_weight*a_weight;
			}
		}
		m_SVarS = getTotalWeight() * m_SVarS / (2 * sumWiTria + sumWiDiag);
	}

	public final static int Sampling_K_Random(int a, int b) {
		/* return value in interval a ... b (inclusive) */
		return a + m_Random.nextInt(b + 1);
	}

	public void optimizeLinearPreCalc(RowData data) {
		optimizeLinearPreCalc(data, linearParameter);
	}

	//linear random
	public void optimizeLinearPreCalc(RowData data, int samplenb) {
		//long t = Calendar.getInstance().getTimeInMillis();
		/* reset value */
		m_SVarS = 0.0;
		int nb = m_Bits.size();
		/* create index */
		int nb_total = 0;
		int[] indices = new int[nb];
		for (int i = 0; i < nb; i++) {
			if (m_Bits.getBit(i)) indices[nb_total++] = i;
		}
		if (nb_total < samplenb) {
			/* less examples than sample size, use default method */
			optimizePreCalcExact(data);
			return;
		}
		/* compute SSPD */
		double sumWi = 0.0;
		for (int i = 0; i < nb; i++) {
			if (m_Bits.getBit(i)) {
				DataTuple a = data.getTuple(i);
				double a_weight = a.getWeight();
				/* Knuth's SAMPLING_K */
				int T = 0;
				int M = 0;
				while (M < samplenb) {
					if (Sampling_K_Random(0, nb_total - T - 1) < samplenb - M) {
						DataTuple b = data.getTuple(indices[T]);
						double wi = a_weight*b.getWeight();
						double d = calcDistance(a, b);
						m_SVarS += wi * d;
						sumWi += wi;
						M++;
					}
					T++;
				}
			}
		}
		m_SVarS = getTotalWeight() * m_SVarS / sumWi / 2.0;
	}

	public void optimizePairwiseLinearPreCalc(RowData data) {
		/* reset value */
		m_SVarS = 0.0;
		int nb = m_Bits.size();
		/* create index */
		int nb_total = 0;
		int[] indices = new int[nb];
		for (int i = 0; i < nb; i++) {
			if (m_Bits.getBit(i)) indices[nb_total++] = i;
		}
		/* compute SSPD */
		double sumWi = 0.0;
		for (int i = 0; i < nb_total; i++) {
			/* get first tuple */
			int a = Sampling_K_Random(0, nb_total-1);
			DataTuple dt1 = data.getTuple(indices[a]);
			/* get second tuple */
			int b = Sampling_K_Random(0, nb_total-1);
			DataTuple dt2 = data.getTuple(indices[b]);
			/* update sspd formula */
			double wi = dt1.getWeight()*dt2.getWeight();
			m_SVarS += wi * calcDistance(dt1, dt2);
			sumWi += wi;
		}
		m_SVarS = getTotalWeight() * m_SVarS / sumWi;
	}

	// N*LogN random
	public void optimizeLogPreCalc(RowData data) {
		int nb = getNbTuples();
		int lognb = (int)Math.floor(Math.log(nb)/Math.log(2))+1;
		optimizeLinearPreCalc(data, lognb);
	}

	public void copy(ClusStatistic other) {
		super.copy(other);
		SumPairwiseDistancesStat or = (SumPairwiseDistancesStat)other;
		m_SVarS = or.m_SVarS;
	}

	/*
	 * [Aco]
	 * this is executed in the end
	 * @see clus.statistic.ClusStatistic#calcMean()
	 */
	public void calcMean() {
	}

	public ClusDistance getDistance() {
		return m_Distance;
	}

	public String getDistanceName() {
		return getDistance().getDistanceName();
	}
}
