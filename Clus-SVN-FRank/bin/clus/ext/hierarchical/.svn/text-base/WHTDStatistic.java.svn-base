/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

/*
 * Created on May 17, 2005
 */
package clus.ext.hierarchical;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import org.apache.commons.math.distribution.*;
import org.apache.commons.math.*;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.*;
import clus.data.type.*;
import clus.ext.timeseries.TimeSeries;
import clus.main.*;
import clus.statistic.*;
import clus.util.*;

import jeans.util.array.*;

public class WHTDStatistic extends RegressionStatBinaryNomiss {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	protected static DistributionFactory m_Fac = DistributionFactory.newInstance();

	protected ClassHierarchy m_Hier;
	protected boolean[] m_DiscrMean;
	protected WHTDStatistic m_Global, m_Validation, m_Training;
	protected double m_SigLevel;
	protected double m_Threshold = -1.0;
	protected int m_Compatibility;

	public WHTDStatistic(ClassHierarchy hier, int comp) {
		this(hier, false, comp);
	}

	public WHTDStatistic(ClassHierarchy hier, boolean onlymean, int comp) {
		super(hier.getDummyAttrs(), onlymean);
		m_Compatibility = comp;
		m_Hier = hier;
	}

	public int getCompatibility() {
		return m_Compatibility;
	}
	
	public void setTrainingStat(ClusStatistic train) {
		m_Training = (WHTDStatistic)train;
	}		

	public void setValidationStat(WHTDStatistic valid) {
		m_Validation = valid;
	}

	public void setGlobalStat(WHTDStatistic global) {
		m_Global = global;
	}
	
	public void setSigLevel(double sig) {
		m_SigLevel = sig;
	}

	public void setThreshold(double threshold) {
		m_Threshold = threshold;
	}

	public double getThreshold(){
		return m_Threshold;
	}

	public ClusStatistic cloneStat() {
		return new WHTDStatistic(m_Hier, false, m_Compatibility);
	}

	public ClusStatistic cloneSimple() {
		WHTDStatistic res = new WHTDStatistic(m_Hier, true, m_Compatibility);
		res.m_Threshold = m_Threshold;
		res.m_Training = m_Training;
		if (m_Validation != null) {
			res.m_Validation = (WHTDStatistic)m_Validation.cloneSimple();
			res.m_Global = m_Global;
			res.m_SigLevel = m_SigLevel;
		}
		return res;
	}

	public void copyAll(ClusStatistic other) {
		super.copy(other);
		WHTDStatistic my_other = (WHTDStatistic)other;
		m_Global = my_other.m_Global;
		m_Validation = my_other.m_Validation;
		m_SigLevel = my_other.m_SigLevel;
	}

	public void addPrediction(ClusStatistic other, double weight) {
		WHTDStatistic or = (WHTDStatistic)other;
		super.addPrediction(other, weight);
		if (m_Validation != null) {
			m_Validation.addPrediction(or.m_Validation, weight);
		}
	}

	public void updateWeighted(DataTuple tuple, double weight) {
		int sidx = m_Hier.getType().getArrayIndex();
		ClassesTuple tp = (ClassesTuple)tuple.getObjVal(sidx);
		m_SumWeight += weight;
		// Add one to the elements in the tuple, zero to the others
		for (int j = 0; j < tp.getNbClasses(); j++) {
			ClassesValue val = tp.getClass(j);
			int idx = val.getIndex();
			// if (Settings.VERBOSE > 10) System.out.println("idx = "+idx+" weight = "+weight);
			m_SumValues[idx] += weight;
		}
	}

	public final ClassHierarchy getHier() {
		return m_Hier;
	}

	public final void setHier(ClassHierarchy hier) throws ClusException {
		if (m_Hier != null && m_Hier.getTotal() != hier.getTotal()) {
			throw new ClusException("Different number of classes in new hierarchy: "+hier.getTotal()+" <> "+m_Hier.getTotal());
		}
		m_Hier = hier;
	}

	public int getNbPredictedClasses() {
		int count = 0;
		for (int i = 0; i < m_DiscrMean.length; i++) {
			if (m_DiscrMean[i]) {
				count++;
			}
		}
		return count;
	}

	public ClassesTuple computeMeanTuple() {
		return m_Hier.getTuple(m_DiscrMean);
	}
	
	public ClassesTuple computePrintTuple() {
		// Same tuple with intermediate elements indicated as such
		// Useful for printing the tree without the intermediate classes		
		ClassesTuple printTuple = m_Hier.getTuple(m_DiscrMean);
		ArrayList added = new ArrayList();
		boolean[] interms = new boolean[m_Hier.getTotal()];
		printTuple.addIntermediateElems(m_Hier, interms, added);		
		return printTuple;
	}	
	
	public void computePrediction() {
		ClassesTuple meantuple = m_Hier.getBestTupleMaj(m_Means, m_Threshold);
		m_DiscrMean = meantuple.getVectorBooleanNodeAndAncestors(m_Hier);
		performSignificanceTest();
	}
	
	public void calcMean(double[] means) {
		if (Settings.useMEstimate() && m_Training != null) {
			// Use m-estimate
			for (int i = 0; i < m_NbAttrs; i++) {			
				means[i] = (m_SumValues[i] + m_Training.m_Means[i]) / (m_SumWeight+1.0);
			}
		} else {
			// Use default definition (no m-estimate)
			for (int i = 0; i < m_NbAttrs; i++) {			
				means[i] = m_SumWeight != 0.0 ? m_SumValues[i] / m_SumWeight : 0.0;
			}			
		}
	}
	
	public double getMean(int i) {
		if (Settings.useMEstimate() && m_Training != null) {
			// Use m-estimate		
			return (m_SumValues[i] + m_Training.m_Means[i]) / (m_SumWeight+1.0);
		} else {
			// Use default definition (no m-estimate)
			return m_SumWeight != 0.0 ? m_SumValues[i] / m_SumWeight : 0.0;
		}
	}

	public void calcMean() {
		super.calcMean();
		computePrediction();
	}

	public int round(double value) {
		if (getCompatibility() == Settings.COMPATIBILITY_CMB05) {
			return (int)value;
		} else {
			return (int)Math.round(value);
		}
	}

	public void performSignificanceTest() {
		if (m_Validation != null) {
			for (int i = 0; i < m_DiscrMean.length; i++) {
				if (m_DiscrMean[i]) {
					/* Predicted class i, check sig? */
					int pop_tot = round(m_Global.getTotalWeight());
					int pop_cls = round(m_Global.getTotalWeight()*m_Global.m_Means[i]);
					int rule_tot = round(m_Validation.getTotalWeight());
					int rule_cls = round(m_Validation.getTotalWeight()*m_Validation.m_Means[i]);
					int upper = Math.min(rule_tot, pop_cls);
					int nb_other = pop_tot - pop_cls;
					int min_this = rule_tot - nb_other;
					int lower = Math.max(rule_cls, min_this);
					if (rule_cls < min_this || lower > upper) {
						System.err.println("BUG?");
						System.out.println("rule = "+m_Validation.getTotalWeight()*m_Validation.m_Means[i]);
						System.out.println("pop_tot = "+pop_tot+" pop_cls = "+pop_cls+" rule_tot = "+rule_tot+" rule_cls = "+rule_cls);
					}
					HypergeometricDistribution dist = m_Fac.createHypergeometricDistribution(pop_tot, pop_cls, rule_tot);
					try {
						double stat = dist.cumulativeProbability(lower, upper);
						if (stat >= m_SigLevel) {
							m_DiscrMean[i] = false;
						}
					} catch (MathException me) {
						System.err.println("Math error: "+me.getMessage());
					}
				}
			}
		}
	}
	
	public void setMeanTuple(ClassesTuple tuple) {
		setMeanTuple(tuple.getVectorBoolean(m_Hier));
	}

	public void setMeanTuple(boolean[] cls) {
		m_DiscrMean = new boolean[cls.length];
		System.arraycopy(cls, 0, m_DiscrMean, 0, cls.length);
		Arrays.fill(m_Means, 0.0);
		for (int i = 0; i < m_DiscrMean.length; i++) {
			if (m_DiscrMean[i]) m_Means[i] = 1.0;
		}
	}

	public boolean[] getDiscretePred() {
		return m_DiscrMean;
	}

	/*
	 * Compute squared Euclidean distance between tuple's target attributes and this statistic's mean.
	 **/
	public double getSquaredDistance(DataTuple tuple, ClusAttributeWeights weights) {
		double sum = 0.0;
		boolean[] actual = new boolean[m_Hier.getTotal()];
		ClassesTuple tp = (ClassesTuple)tuple.getObjVal(m_Hier.getType().getArrayIndex());
		tp.fillBoolArrayNodeAndAncestors(actual);
		for (int i = 0; i < m_Hier.getTotal(); i++) {
			NumericAttrType type = getAttribute(i);
			double actual_zo = actual[i] ? 1.0 : 0.0;
			double dist = actual_zo - m_Means[i];
			sum += dist * dist * weights.getWeight(type);
		}
		return sum / getNbAttributes();
	}

	public double getAbsoluteDistance(DataTuple tuple, ClusAttributeWeights weights, ClusStatManager statmanager) {
		double sum = 0.0;
		boolean[] actual = new boolean[m_Hier.getTotal()];
		ClassesTuple tp = (ClassesTuple)tuple.getObjVal(m_Hier.getType().getArrayIndex());
		tp.fillBoolArrayNodeAndAncestors(actual);
		for (int i = 0; i < m_Hier.getTotal(); i++) {
			NumericAttrType type = getAttribute(i);
			double actual_zo = actual[i] ? 1.0 : 0.0;
			double dist = actual_zo - m_Means[i];
			WHTDStatistic tstat = (WHTDStatistic) statmanager.getTrainSetStat(ClusAttrType.ATTR_USE_CLUSTERING);
			if (tstat.getVariance(i) != 0)
				dist = dist / Math.pow(tstat.getVariance(i), 0.5);
			sum += Math.abs(dist) * weights.getWeight(type);
		}
		return sum / getNbAttributes();
	}

	public void printTree() {
		m_Hier.print(ClusFormat.OUT_WRITER, m_SumValues);
		ClusFormat.OUT_WRITER.flush();
	}

	public String getString(StatisticPrintInfo info) {
		String pred = null;
		if (m_Threshold >= 0.0) {
			pred = computePrintTuple().toStringHuman(getHier());
			return pred+" ["+ClusFormat.TWO_AFTER_DOT.format(getTotalWeight())+"]";
		} else {
			NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
			StringBuffer buf = new StringBuffer();
			buf.append("[");
			for (int i = 0; i < getHier().getTotal(); i++) {
				if (i != 0) buf.append(",");
				if (m_SumWeight == 0.0) buf.append("?");
				else buf.append(fr.format(getMean(i)));
			}
			buf.append("]");
			if (info.SHOW_EXAMPLE_COUNT) {
				buf.append(": ");
				buf.append(fr.format(m_SumWeight));
			}
			return buf.toString();
		}
	}

	public String getPredictString() {
		return "["+computeMeanTuple().toStringHuman(getHier())+"]";
	}

	//public boolean isValidPrediction() {
	//	return !m_MeanTuple.isRoot();
	//}

	public void showRootInfo() {
		try {
			PrintWriter wrt = new PrintWriter(new OutputStreamWriter(new FileOutputStream("hierarchy.txt")));
			wrt.println("Hier #nodes: "+m_Hier.getTotal());
			wrt.println("Hier classes by level: "+MIntArray.toString(m_Hier.getClassesByLevel()));
			m_Hier.print(wrt, m_SumValues, null);
			wrt.close();
		} catch (IOException e) {
			System.out.println("IO Error: "+e.getMessage());
		}
	}

	public void printDistributionRec(PrintWriter out, ClassTerm node) {
		int idx = node.getIndex();
		ClassesValue val = new ClassesValue(node);
		out.println(val.toPathString()+", "+m_Means[idx]);
		for (int i = 0; i < node.getNbChildren(); i++) {
			printDistributionRec(out, (ClassTerm)node.getChild(i));
		}
	}

	public void printDistribution(PrintWriter wrt) throws IOException {
		wrt.println("Total: "+m_SumWeight);
		ClassTerm root = m_Hier.getRoot();
		for (int i = 0; i < root.getNbChildren(); i++) {
			printDistributionRec(wrt, (ClassTerm)root.getChild(i));
		}
	}

	public void getExtraInfoRec(ClassTerm node, double[] discrmean, StringBuffer out) {
		if (m_Validation != null) {
			int i = node.getIndex();
			if (discrmean[i] > 0.5) {
				/* Predicted class i, check sig? */
				int pop_tot = round(m_Global.getTotalWeight());
				int pop_cls = round(m_Global.getTotalWeight()*m_Global.m_Means[i]);
				int rule_tot = round(m_Validation.getTotalWeight());
				int rule_cls = round(m_Validation.getTotalWeight()*m_Validation.m_Means[i]);
				int upper = Math.min(rule_tot, pop_cls);
				int nb_other = pop_tot - pop_cls;
				int min_this = rule_tot - nb_other;
				int lower = Math.max(rule_cls, min_this);
				HypergeometricDistribution dist = m_Fac.createHypergeometricDistribution(pop_tot, pop_cls, rule_tot);
				try {
					double stat = dist.cumulativeProbability(lower, upper);
					out.append(node.toStringHuman(getHier())+":");
					out.append(" pop_tot = "+String.valueOf(pop_tot));
					out.append(" pop_cls = "+String.valueOf(pop_cls));
					out.append(" rule_tot = "+String.valueOf(rule_tot));
					out.append(" rule_cls = "+String.valueOf(rule_cls));
					out.append(" upper = "+String.valueOf(upper));
					out.append(" prob = "+ClusFormat.SIX_AFTER_DOT.format(stat));
					// out.append(" siglevel = "+m_SigLevel);
					out.append("\n");
				} catch (MathException me) {
					System.err.println("Math error: "+me.getMessage());
				}
			}
		}
		for (int i = 0; i < node.getNbChildren(); i++) {
			getExtraInfoRec((ClassTerm)node.getChild(i), discrmean, out);
		}
	}

	public String getExtraInfo() {
		StringBuffer res = new StringBuffer();
		ClassesTuple meantuple = m_Hier.getBestTupleMaj(m_Means, 50.0);
		double[] discrmean = meantuple.getVectorNodeAndAncestors(m_Hier);
		for (int i = 0; i < m_Hier.getRoot().getNbChildren(); i++) {
			getExtraInfoRec((ClassTerm)m_Hier.getRoot().getChild(i), discrmean, res);
		}
		return res.toString();
	}

	public void addPredictWriterSchema(String prefix, ClusSchema schema) {
		ClassHierarchy hier = getHier();
		for (int i = 0; i < m_NbAttrs; i++) {
			ClusAttrType type = m_Attrs[i].cloneType();
			ClassTerm term = hier.getTermAt(i);
			type.setName(prefix+"-p-"+term.toStringHuman(hier));
			schema.addAttrType(type);
		}
	}

	public void unionInit() {
		m_DiscrMean = new boolean[m_Means.length];
	}

	public void union(ClusStatistic other) {
		boolean[] discr_mean = ((WHTDStatistic)other).m_DiscrMean;
		for (int i = 0; i < m_DiscrMean.length; i++) {
				if (discr_mean[i]) m_DiscrMean[i] = true;
		}
	}

	public void unionDone() {		
	}

	public void vote(ArrayList votes) {
		reset();
		m_Means = new double[m_NbAttrs];
		WHTDStatistic vote;
		int nb_votes = votes.size();
		for (int j = 0; j < nb_votes; j++){
			vote = (WHTDStatistic) votes.get(j);
			for (int i = 0; i < m_NbAttrs; i++){
				m_Means[i] += vote.m_Means[i] / nb_votes;
			}
		}
		computePrediction();
	}

	/**
	 * Used for the hierarchical rules heuristic
	 */
	public double getDispersion(ClusAttributeWeights scale, RowData data) {
		return getSVarS(scale);
	}

	public String getDistanceName() {
		return "Hierarchical Weighted Euclidean Distance";
	}
}
