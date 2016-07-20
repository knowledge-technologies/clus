package clus.error;

import java.io.Serializable;
import java.util.*;

import clus.main.Settings;

import jeans.math.MathUtil;
import jeans.util.compound.DoubleBoolean;
import jeans.util.compound.DoubleBooleanCount;

public class ROCAndPRCurve implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected double m_AreaROC, m_AreaPR;
	protected double[] m_Thresholds;	
	
	protected transient boolean m_ExtendPR;
	protected transient int m_PrevTP, m_PrevFP;
	protected transient ArrayList m_ROC;
	protected transient ArrayList m_PR;
	protected transient BinaryPredictionList m_Values;
	protected transient double[] m_PrecisionAtRecall;

	public ROCAndPRCurve(BinaryPredictionList list) {
		m_Values = list;
	}

	public void clear() {
		m_ROC.clear();
		m_PR.clear();
	}
	
	public ArrayList getROCCurve() {
		return m_ROC;
	}
	
	public ArrayList getPRCurve() {
		return m_PR;
	}	
	
	public double getAreaROC() {
		return m_AreaROC;
	}

	public double getAreaPR() {
		return m_AreaPR;
	}

	public void computeCurves() {
		// Create new curves
		m_ROC = new ArrayList();
		m_PR = new ArrayList();
		m_AreaPR = 0.0;
		m_AreaROC = 0.5;
		if (m_Values.getNbPos() != 0) {
			enumerateThresholds();
			m_AreaPR = computeArea(m_PR);
			if (m_Values.getNbNeg() != 0) {
				m_AreaROC = computeArea(m_ROC);
			}
		}
	}

	public void setThresholds(double[] thr) {
		m_Thresholds = thr;
	}

	public void enumerateThresholds() {
		if (m_Thresholds == null) enumerateThresholdsAll();
		else enumerateThresholdsSelected(m_Thresholds);
	}

	public void enumerateThresholdsAll() {
		// Should extend PR curve to recall zero?
		m_ExtendPR = true;
		// Point (0,0) does not help building PR curve
		addOutputROC(0, 0);
		boolean first = true;
		int TP_cnt = 0, FP_cnt = 0;
		double prev = Double.NaN;
		for (int i = 0; i < m_Values.size(); i++) {
			DoubleBooleanCount val = m_Values.get(i);
			//System.out.println("********Val= "+val);
			if (val.getDouble() != prev && !first) {
				// System.out.println("Thr: "+((val.getDouble()+prev)/2)+" TP: "+TP_cnt+" FP: "+FP_cnt);
				addOutput(TP_cnt, FP_cnt);
			}
			if (val.getBoolean()) {
				TP_cnt += val.getCount();
			} else {
				FP_cnt += val.getCount();
			}
			prev = val.getDouble();
			first = false;
		}
		// System.out.println("Thr: 0.0 TP: "+TP_cnt+" FP: "+FP_cnt);
		// addOutput(TP_cnt, FP_cnt) -> curve will always include point with recall = 1.0
		addOutput(TP_cnt, FP_cnt);
	}

	public void enumerateThresholdsSelected(double[] thr) {
		// Should extend PR curve to recall zero?
		m_ExtendPR = true;
		// Point (0,0) does not help building PR curve
		addOutputROC(0, 0);
		int idx = 0;
		int TP_cnt = 0, FP_cnt = 0;
		int prevTP_cnt = 0, prevFP_cnt = 0;
		for (int i = thr.length-1; i >= 0; i--) {
			DoubleBooleanCount val = null;
			while (idx < m_Values.size() && (val = m_Values.get(idx)).getDouble() >= thr[i]) {
				if (val.getBoolean()) {
					TP_cnt += val.getCount();
				} else {
					FP_cnt += val.getCount();
				}
				idx++;
			}
			if (TP_cnt != prevTP_cnt || FP_cnt != prevFP_cnt) {
				addOutput(TP_cnt, FP_cnt);
			}
			prevTP_cnt = TP_cnt;
			prevFP_cnt = FP_cnt;
		}
		// addOutput(TP_cnt, FP_cnt) -> curve will always include point with recall = 1.0
		addOutput(TP_cnt, FP_cnt);
	}

	public double computeArea(ArrayList curve) {
		double area = 0.0;
		// System.out.println("Computing areas");
		if (curve.size() > 0) {
			double[] prev = (double[])curve.get(0);
			// System.out.println("PT: "+prev[0]+","+prev[1]);
			for (int i = 1; i < curve.size(); i++) {
				double[] pt = (double[])curve.get(i);
				// System.out.println("PT: "+pt[0]+","+pt[1]);
				area += 0.5*(pt[1]+prev[1])*(pt[0]-prev[0]);
				prev = pt;
			}
		}
		return area;
	}

	public void addOutput(int TP, int FP) {
		addOutputROC(TP, FP);
		addOutputPR(TP, FP);
	}

	public void addOutputROC(int TP, int FP) {
		double[] point = new double[2];
		point[0] = (double)FP / m_Values.getNbNeg();
		point[1] = (double)TP / m_Values.getNbPos();
		m_ROC.add(point);
	}

	public void addOutputPR(int TP, int FP) {
		int P = TP + FP;
		if (P != 0) {
			double prec = (double)TP / P;
			double recall = (double)TP / m_Values.getNbPos();
			if (m_ExtendPR) {
				// First "real" point on curve -> extend to zero recall
				addPointPR(prec, 0.0);
				m_ExtendPR = false;
			} else {
				for (int crTP = m_PrevTP+1; crTP < TP; crTP++) {
					double crFP = (double)m_PrevFP + ((double)FP-m_PrevFP)/(TP-m_PrevTP)*(crTP-m_PrevTP);
					double crPrec = (double)crTP / (crTP + crFP);
					double crRecall = (double)crTP / m_Values.getNbPos();
					addPointPROptimized(crPrec, crRecall);
				}
			}
			addPointPROptimized(prec, recall);
			m_PrevTP = TP;
			m_PrevFP = FP;
		}
	}

	public void addPointPR(double prec, double recall) {
		double[] point = new double[2];
		point[0] = recall;
		point[1] = prec;
		m_PR.add(point);
	}

	public void addPointPROptimized(double prec, double recall) {
		int size = m_PR.size();
		double[] prev = (double[])m_PR.get(size-1);
		if (!(prev[0] == recall && prev[1] == prec)) {
			if (size <= 1) {
				addPointPR(prec, recall);
			} else {
				double[] prev2 = (double[])m_PR.get(size-2);
				if (Math.abs(prev[1]-prec) < 1e-15 && Math.abs(prev2[1]-prec) < 1e-15) {
					// Constant precision (horizontal line)
					prev[0] = recall;
				} else if (Math.abs(prev[0]-recall) < 1e-15 && Math.abs(prev2[0]-recall) < 1e-15) {
					// Constant recall (vertical line)
					prev[1] = prec;
				} else {
					addPointPR(prec, recall);
				}
			}
		}
	}

	public double getPrecisionAtRecall(int j) {
		return m_PrecisionAtRecall[j];
	}

	public void computePrecisions(double[] recallValues) {
		if (recallValues == null) return;
		int nbRecalls = recallValues.length;
		m_PrecisionAtRecall = new double[nbRecalls];
		for (int i = 0; i < nbRecalls; i++) {
			m_PrecisionAtRecall[i] = computePrecision(recallValues[i]);
		}		
	}
	
	public double computePrecision(double recall) {
		return MathUtil.interpolate(recall, m_PR);
	}	
}
