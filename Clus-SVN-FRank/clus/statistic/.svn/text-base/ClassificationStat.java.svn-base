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

package clus.statistic;

import jeans.math.*;
import jeans.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.*;

import clus.main.*;
import clus.util.*;
import clus.data.cols.*;
import clus.data.rows.*;
import clus.data.type.*;
import clus.data.attweights.*;
import clus.ext.hierarchical.WHTDStatistic;

/**
 * Classification statistics about the data. A child of ClusStatistic.
 *
 */
public class ClassificationStat extends ClusStatistic {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public int m_NbTarget;
	public NominalAttrType[] m_Attrs;
	public ClassificationStat m_Training;

	//* Class counts with [Target index][Class]
	public double[][] m_ClassCounts;
	public double[] m_SumWeights;
	public int[] m_MajorityClasses;

	/**
	 * Constructor for this class.
	 */
	public ClassificationStat(NominalAttrType[] nomAtts) {
		m_NbTarget = nomAtts.length;
		m_SumWeights = new double[m_NbTarget];
		m_ClassCounts = new double[m_NbTarget][];
		for (int i = 0; i < m_NbTarget; i++) {
			m_ClassCounts[i] = new double[nomAtts[i].getNbValues()];
		}
		m_Attrs = nomAtts;
	}
	
	public void setTrainingStat(ClusStatistic train) {
		m_Training = (ClassificationStat)train;
	}

	public int getNbNominalAttributes() {
		return m_NbTarget;
	}

	public NominalAttrType[] getAttributes() {
		return m_Attrs;
	}	
	
	public ClusStatistic cloneStat() {
		ClassificationStat res = new ClassificationStat(m_Attrs);
		res.m_Training = m_Training;
		return res;
	}

	public void initSingleTargetFrom(double[] distro) {
		m_ClassCounts[0] = distro;
		m_SumWeight = 0.0;
		for (int i = 0; i < distro.length; i++) {
			m_SumWeight += distro[i];
		}
		Arrays.fill(m_SumWeights, m_SumWeight);
	}

	/**
	 * Returns the nominal attribute.
	 * Added because RegressionStat has this method.
	 * @param idx index of attribute.
	 * @return NominalAttrType
	 */
	public NominalAttrType getAttribute(int idx) {
		return m_Attrs[idx];
	}

	public void reset() {		
		m_NbExamples = 0;
		m_SumWeight = 0.0;
		Arrays.fill(m_SumWeights, 0.0);
		for (int i = 0; i < m_NbTarget; i++) {
			Arrays.fill(m_ClassCounts[i], 0.0);
		}
	}

	/** Resets the SumWeight and majority class count to weight and all other
	 *  class counts to zero.
	 */
	public void resetToSimple(double weight) {		
		m_NbExamples = 0;
		m_SumWeight = weight;
		Arrays.fill(m_SumWeights, weight);
		for (int i = 0; i < m_NbTarget; i++) {
			double[] clcts = m_ClassCounts[i];
			for (int j = 0; j < clcts.length; j++) {
				if (j == m_MajorityClasses[i]) {
					clcts[j] = weight;
				} else {
					clcts[j] = 0.0;
				}
			}
		}
	}

	public void copy(ClusStatistic other) {
		ClassificationStat or = (ClassificationStat)other;
		m_SumWeight = or.m_SumWeight;
		m_NbExamples = or.m_NbExamples;
		System.arraycopy(or.m_SumWeights, 0, m_SumWeights, 0, m_NbTarget);
		for (int i = 0; i < m_NbTarget; i++) {
			double[] my = m_ClassCounts[i];			
			System.arraycopy(or.m_ClassCounts[i], 0, my, 0, my.length);
		}
	}

	/**
	 * Used for combining weighted predictions.
	 */
	public ClassificationStat normalizedCopy() {
		ClassificationStat copy = (ClassificationStat)cloneStat();
		copy.copy(this);
		for (int i = 0; i < m_NbTarget; i++) {
			for (int j = 0; j < m_ClassCounts[i].length; j++) {
				copy.m_ClassCounts[i][j] /= m_SumWeights[i];
			}
		}
		Arrays.fill(copy.m_SumWeights, 1.0);
		copy.m_SumWeight = 1.0;
		return copy;
	}

	public boolean samePrediction(ClusStatistic other) {
		ClassificationStat or = (ClassificationStat)other;
		for (int i = 0; i < m_NbTarget; i++)
			if (m_MajorityClasses[i] != or.m_MajorityClasses[i])
				return false;
		return true;
	}

	public void addPrediction(ClusStatistic other, double weight) {
		ClassificationStat or = (ClassificationStat)other;
		m_SumWeight += weight*or.m_SumWeight;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] my = m_ClassCounts[i];
			double[] your = or.m_ClassCounts[i];
			for (int j = 0; j < my.length; j++) my[j] += weight*your[j];
			m_SumWeights[i] += weight*or.m_SumWeights[i];
		}
	}

	public void add(ClusStatistic other) {
		ClassificationStat or = (ClassificationStat)other;
		m_SumWeight += or.m_SumWeight;
		m_NbExamples += or.m_NbExamples;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] my = m_ClassCounts[i];
			double[] your = or.m_ClassCounts[i];
			for (int j = 0; j < my.length; j++) my[j] += your[j];
			m_SumWeights[i] += or.m_SumWeights[i];
		}
	}

	public void addScaled(double scale, ClusStatistic other) {
		ClassificationStat or = (ClassificationStat)other;
		m_SumWeight += scale*or.m_SumWeight;
		m_NbExamples += or.m_NbExamples;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] my = m_ClassCounts[i];
			double[] your = or.m_ClassCounts[i];
			for (int j = 0; j < my.length; j++) my[j] += scale*your[j];
			m_SumWeights[i] += scale*or.m_SumWeights[i];
		}
	}

	public void subtractFromThis(ClusStatistic other) {
		ClassificationStat or = (ClassificationStat)other;
		m_SumWeight -= or.m_SumWeight;
		m_NbExamples -= or.m_NbExamples;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] my = m_ClassCounts[i];
			double[] your = or.m_ClassCounts[i];
			for (int j = 0; j < my.length; j++) my[j] -= your[j];
			m_SumWeights[i] -= or.m_SumWeights[i];
		}
	}

	public void subtractFromOther(ClusStatistic other) {
		ClassificationStat or = (ClassificationStat)other;
		m_SumWeight = or.m_SumWeight - m_SumWeight;
		m_NbExamples = or.m_NbExamples - m_NbExamples;
		for (int i = 0; i < m_NbTarget; i++) {
			double[] my = m_ClassCounts[i];
			double[] your = or.m_ClassCounts[i];
			for (int j = 0; j < my.length; j++) my[j] = your[j] - my[j];
			m_SumWeights[i] = or.m_SumWeights[i] - m_SumWeights[i];
		}
	}

	public void updateWeighted(DataTuple tuple, int idx) {
		updateWeighted(tuple, tuple.getWeight());
	}

	public void updateWeighted(DataTuple tuple, double weight) {
		m_NbExamples++;
		m_SumWeight += weight;
		for (int i = 0; i < m_NbTarget; i++) {
			int val = m_Attrs[i].getNominal(tuple);
//			System.out.println("val: "+ val);
			if (val != m_Attrs[i].getNbValues()) {
				m_ClassCounts[i][val] += weight;
				m_SumWeights[i] += weight;
			}
		}
	}

	public int getMajorityClass(int attr) {
		int m_class = -1;
		double m_max = Double.NEGATIVE_INFINITY;
		double[] clcts = m_ClassCounts[attr];
		for (int i = 0; i < clcts.length; i++) {
			if (clcts[i] > m_max) {
				m_class = i;
				m_max = clcts[i];
			}
		}
		if (m_max <= MathUtil.C1E_9 && m_Training != null) {
			// no examples covered -> m_max = null -> use whole training set majority class
			return m_Training.getMajorityClass(attr);
		}
		return m_class;
	}

	public int getMajorityClassDiff(int attr, ClassificationStat other) {
		int m_class = -1;
		double m_max = Double.NEGATIVE_INFINITY;
		double[] clcts1 = m_ClassCounts[attr];
		double[] clcts2 = other.m_ClassCounts[attr];
		for (int i = 0; i < clcts1.length; i++) {
			double diff = clcts1[i] - clcts2[i];
			if (diff > m_max) {
				m_class = i;
				m_max = diff;
			}
		}
		if (m_max <= MathUtil.C1E_9 && m_Training != null) {
			// no examples covered -> m_max = null -> use whole training set majority class
			return m_Training.getMajorityClass(attr);
		}		
		return m_class;
	}

	public double entropy() {
		double sum = 0.0;
		for (int i = 0; i < m_NbTarget; i++) {
			if(getAttribute(i).getSchema().getSettings().checkEntropyType("StandardEntropy")){
			
				sum += entropy(i);
			
			}else{
				
				sum += modifiedEntropy(i);
				
			}
		}
		return sum;
	}

	public double entropy(int attr) {
		double total = m_SumWeights[attr];
		
		
		
		if (total < 1e-6) {
			return 0.0;
		} else {
			double acc = 0.0;
			double[] clcts = m_ClassCounts[attr];
			for (int i = 0; i < clcts.length; i++) {
				if (clcts[i] != 0.0) {
					double prob = clcts[i]/total;
					acc += prob*Math.log(prob);
				}
			}
			return -acc/MathUtil.M_LN2;
		}
	}
	
	
	// The idea here is ....
	public double modifiedEntropy(int attr) {
		
		double total = m_SumWeights[attr];
		
		//System.out.print("Class has "+total+" examples\n");
		
		if (total < 1e-6) {
			return 0.0;
		} else {
			double acc = 0.0;
			double[] clcts = m_ClassCounts[attr];
			for (int i = 0; i < clcts.length; i++) {
				if (clcts[i] != 0.0) {
					 
					double prob = (clcts[i]+1)/(total+clcts.length);
					acc += prob*Math.log(prob);
				}
			}
			return -acc/MathUtil.M_LN2;
		}
	}
	

	public double entropyDifference(ClassificationStat other) {
		double sum = 0.0;		
		for (int i = 0; i < m_NbTarget; i++) {			
			if(other.getAttribute(i).getSchema().getSettings().checkEntropyType("StandardEntropy")){
				sum += entropyDifference(i, other);
			}else{
				sum += modifiedEntropyDifference(i, other);				
			}
		}
		return sum;
	}

	public double entropyDifference(int attr, ClassificationStat other) {
		
		double acc = 0.0;
		double[] clcts = m_ClassCounts[attr];
		double[] otcts = other.m_ClassCounts[attr];
		double total = m_SumWeights[attr] - other.m_SumWeights[attr];
		for (int i = 0; i < clcts.length; i++) {
			double diff = clcts[i] - otcts[i];
			if (diff != 0.0) acc += diff/total*Math.log(diff/total);
		}
		return -acc/MathUtil.M_LN2;
	}
	
	
	public double modifiedEntropyDifference(int attr, ClassificationStat other) {
			
		double acc = 0.0;
		double[] clcts = m_ClassCounts[attr];
		double[] otcts = other.m_ClassCounts[attr];
		double total = m_SumWeights[attr] - other.m_SumWeights[attr];
		for (int i = 0; i < clcts.length; i++) {
			double diff = clcts[i] - otcts[i];
			if (diff != 0.0) acc += ((diff+1)/(total+clcts.length))*Math.log((diff+1)/(total+clcts.length));
		}
		return -acc/MathUtil.M_LN2;
	}
	
	
	
	public double getSumWeight(int attr) {
		return m_SumWeights[attr];
	}
	
	public double getProportion(int attr, int cls) {
		double total = m_SumWeights[attr];
		if (total <= MathUtil.C1E_9) {
			// no examples -> assume training set distribution
			return m_Training.getProportion(attr, cls);
		} else {
			return m_ClassCounts[attr][cls] / total;
		}
	}

	public double gini(int attr) {
		double total = m_SumWeights[attr];
		if (total <= MathUtil.C1E_9) {
			// no examples -> assume training set distribution			
			// return m_Training.gini(attr); // gives error with HSC script
			return 0.0;
		} else {
			//System.err.println(": here2");
			double sum = 0.0;
			double[] clcts = m_ClassCounts[attr];
			for (int i = 0; i < clcts.length; i++) {
				double prob = clcts[i]/total;
				sum += prob*prob;
			}
			return 1.0 - sum;
		}
	}

	public double giniDifference(int attr, ClassificationStat other) {
		double wDiff = m_SumWeights[attr] - other.m_SumWeights[attr];
		if (wDiff <= MathUtil.C1E_9) {
			// no examples -> assume training set distribution
			return m_Training.gini(attr);
		} else {
			double sum = 0.0;
			double[] clcts = m_ClassCounts[attr];
			double[] otcts = other.m_ClassCounts[attr];
			for (int i = 0; i < clcts.length; i++) {
				double diff = clcts[i] - otcts[i];
				sum += (diff/wDiff)*(diff/wDiff);
			}
			return 1.0 - sum;
		}
	}

	public static double computeSplitInfo(double sum_tot, double sum_pos, double sum_neg) {
		if (sum_pos == 0.0)
			return -1.0*sum_neg/sum_tot*Math.log(sum_neg/sum_tot)/MathUtil.M_LN2;
		if (sum_neg == 0.0)
			return -1.0*sum_pos/sum_tot*Math.log(sum_pos/sum_tot)/MathUtil.M_LN2;
		return -(sum_pos/sum_tot*Math.log(sum_pos/sum_tot) +
				sum_neg/sum_tot*Math.log(sum_neg/sum_tot))/MathUtil.M_LN2;
	}

	public boolean isCalcMean() {
		return m_MajorityClasses != null;
	}

	public void calcMean() {
		m_MajorityClasses = new int[m_NbTarget];
		for (int i = 0; i < m_NbTarget; i++) {
			m_MajorityClasses[i] = getMajorityClass(i);
		}
	}

	/**
	 * Currently only used to compute the default dispersion within rule heuristics.
	 */
	public double getDispersion(ClusAttributeWeights scale, RowData data) {
		System.err.println(getClass().getName()+": getDispersion(): Not yet implemented!");
		return Double.POSITIVE_INFINITY;
	}

  /**
   * Computes a G statistic and returns the p-value of a G test.
   * G = 2*SUM(obs*ln(obs/exp))
   * @param att attribute index
   * @return p-value
   * @throws MathException
   */
  public double getGTestPValue(int att, ClusStatManager stat_manager) throws MathException {
    double global_n = ((CombStat)stat_manager.getTrainSetStat()).getTotalWeight();
    double local_n = getTotalWeight();
    double ratio = local_n / global_n;
    double global_counts[] =((CombStat)stat_manager.getTrainSetStat()).m_ClassStat.getClassCounts(att);
    double local_counts[] = getClassCounts(att);
    double g = 0;
    for (int i = 0; i < global_counts.length; i++) {
      if ((local_counts[i] > 0) && (global_counts[i] > 0)) {
        g += 2 * local_counts[i] * Math.log(local_counts[i]/(global_counts[i] * ratio));
      }
    }
    double degreesOfFreedom = ((double)global_counts.length) - 1;
    DistributionFactory distributionFactory = DistributionFactory.newInstance();
    ChiSquaredDistribution chiSquaredDistribution = distributionFactory.createChiSquareDistribution(degreesOfFreedom);
    return 1 - chiSquaredDistribution.cumulativeProbability(g);
  }

  /**
   * Computes a G statistic and returns the result of a G test.
   * G = 2*SUM(obs*ln(obs/exp))
   * @param att attribute index
   * @return p-value
   * @throws MathException
   */
  public boolean getGTest(int att, ClusStatManager stat_manager) {
    double global_n = ((CombStat)stat_manager.getTrainSetStat()).getTotalWeight();
    double local_n = getTotalWeight();
    double ratio = local_n / global_n;
    double global_counts[] =((CombStat)stat_manager.getTrainSetStat()).m_ClassStat.getClassCounts(att);
    double local_counts[] = getClassCounts(att);
    double g = 0;
    for (int i = 0; i < global_counts.length; i++) {
      if ((local_counts[i] > 0) && (global_counts[i] > 0)) {
        g += 2 * local_counts[i] * Math.log(local_counts[i]/(global_counts[i] * ratio));
      }
    }
    int df = global_counts.length - 1;
    double chi2_crit = stat_manager.getChiSquareInvProb(df);
    return (g > chi2_crit);
  }

	public double[] getNumericPred() {
		return null;
	}

	public int[] getNominalPred() {
		return m_MajorityClasses;
	}

	public String getString2() {
		StringBuffer buf = new StringBuffer();
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		buf.append(fr.format(m_SumWeight));
		buf.append(" ");
		buf.append(super.toString());
		return buf.toString();
	}

	public String getArrayOfStatistic(){
		StringBuffer buf = new StringBuffer();
		if (m_MajorityClasses != null) {
			buf.append("[");
			for (int i = 0; i < m_NbTarget; i++) {
				if (i != 0) buf.append(",");
				buf.append(m_Attrs[i].getValue(m_MajorityClasses[i]));
			}
			buf.append("]");
		}
		return buf.toString();

	}
	/**
	 * Prints the statistics - predictions : weighted sum of all examples
	 */
	public String getString(StatisticPrintInfo info) {
		StringBuffer buf = new StringBuffer();
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		if (m_MajorityClasses != null) {//print the name of the majority class
			buf.append("[");
			for (int i = 0; i < m_NbTarget; i++) {
				if (i != 0) buf.append(",");
				buf.append(m_Attrs[i].getValue(m_MajorityClasses[i]));
			}
			buf.append("]");
		}
		else {
			buf.append("?");
		}
		if (info.SHOW_DISTRIBUTION) {
			for (int j = 0; j < m_NbTarget; j++) {
				buf.append(" [");
				for (int i = 0; i < m_ClassCounts[j].length; i++) {
					if (i != 0) buf.append(",");
					buf.append(m_Attrs[j].getValue(i));
					buf.append(":");
					buf.append(fr.format(m_ClassCounts[j][i]));
				}
				buf.append("]");
			}//end for
			if (info.SHOW_EXAMPLE_COUNT) {
				buf.append(":");
				buf.append(fr.format(m_SumWeight));
			}
		}//end if show distribution
		else {
			// print stat on the majority classes
			if (m_MajorityClasses != null) {
				buf.append(" [");
				for (int i = 0; i < m_NbTarget; i++) {
					if (i != 0) buf.append(",");
					buf.append(m_ClassCounts[i][m_MajorityClasses[i]]);
				}
				// added colon here to make trees print correctly
				buf.append("]: ");
				buf.append(fr.format(m_SumWeight));
			}
		}
		return buf.toString();
	}

	public void addPredictWriterSchema(String prefix, ClusSchema schema) {
		for (int i = 0; i < m_NbTarget; i++) {
			ClusAttrType type = m_Attrs[i].cloneType();
			type.setName(prefix+"-p-"+type.getName());
			schema.addAttrType(type);
		}
		for (int i = 0; i < m_NbTarget; i++) {
			for (int j = 0; j < m_ClassCounts[i].length; j++) {
				String value = m_Attrs[i].getValue(j);
				ClusAttrType type = new NumericAttrType(prefix+"-p-"+m_Attrs[i].getName()+"-"+value);
				schema.addAttrType(type);
			}
		}
	}

	public String getPredictWriterString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_NbTarget; i++) {
			if (i != 0) buf.append(",");
			if (m_MajorityClasses != null) {
				buf.append(m_Attrs[i].getValue(m_MajorityClasses[i]));
			} else {
				buf.append("?");
			}
		}
		for (int i = 0; i < m_NbTarget; i++) {
			for (int j = 0; j < m_ClassCounts[i].length; j++) {
				buf.append(",");
				buf.append(""+m_ClassCounts[i][j]);
			}
		}
		return buf.toString();
	}

	public int getNbClasses(int idx) {
		return m_ClassCounts[idx].length;
	}

	public double getCount(int idx, int cls) {
		return m_ClassCounts[idx][cls];
	}

	//changed elisa 13/06/2007, changed back by celine 16/12/2008
	public String getPredictedClassName(int idx) {
		//return m_Attrs[idx].getName()+" = "+m_Attrs[idx].getValue(m_MajorityClasses[idx]);
		return m_Attrs[idx].getValue(getMajorityClass(idx));
	}


	public String getClassName(int idx, int cls) {
		return m_Attrs[idx].getValue(cls);
	}

	public void setCount(int idx, int cls, double count) {
		m_ClassCounts[idx][cls] = count;
	}

	public String getSimpleString() {
		return getClassString() + " : " + super.getSimpleString();
	}

	public String getClassString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_NbTarget; i++) {
			if (i != 0) buf.append(",");
			buf.append(m_Attrs[i].getValue(m_MajorityClasses[i]));
		}
		return buf.toString();
	}

	public double getError(ClusAttributeWeights scale) {
		double result = 0.0;
		for (int i = 0; i < m_NbTarget; i++) {
			int maj = getMajorityClass(i);
			result += m_SumWeights[i] - m_ClassCounts[i][maj];
		}
		return result / m_NbTarget;
	}

	public double getErrorRel() {
		//System.out.println("ClassificationStat getErrorRel");
		// System.out.println("ClassificationStat nb example in the leaf "+m_SumWeight);
		return getError() / getTotalWeight();
	}

	public double getErrorDiff(ClusAttributeWeights scale, ClusStatistic other) {
		double result = 0.0;
		ClassificationStat or = (ClassificationStat)other;		
		for (int i = 0; i < m_NbTarget; i++) {
			int maj = getMajorityClassDiff(i, or);
			double diff_maj = m_ClassCounts[i][maj] - or.m_ClassCounts[i][maj];
			double diff_total = m_SumWeights[i] - or.m_SumWeights[i];
			result += diff_total - diff_maj;
		}
		return result / m_NbTarget;
	}

	public int getNbPseudoTargets() {
		int nbTarget = 0;
		for (int i = 0; i < m_NbTarget; i++) {
			nbTarget += m_Attrs[i].getNbValues();
		}
		return nbTarget;
	}

	public double getSVarS(ClusAttributeWeights scale) {
		//System.err.println(": here");
		double result = 0.0;
		double sum = m_SumWeight;
		for (int i = 0; i < m_NbTarget; i++) {
			// System.out.println(gini(i) + " " + scale.getWeight(m_Attrs[i]) + " " + sum);
			result += gini(i) * scale.getWeight(m_Attrs[i]) * sum;
		}
		return result / m_NbTarget;
	}

	public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
		double result = 0.0;
		double sum = m_SumWeight - other.m_SumWeight;
		ClassificationStat cother = (ClassificationStat)other;
		for (int i = 0; i < m_NbTarget; i++) {
			result += giniDifference(i, cother) * scale.getWeight(m_Attrs[i]) * sum;
		}
		return result / m_NbTarget;
	}

	
	public void initNormalizationWeights(ClusAttributeWeights weights, boolean[] shouldNormalize) {
		for (int i = 0; i < m_NbTarget; i++) {
			int idx = m_Attrs[i].getIndex();
			if (shouldNormalize[idx]) {
				double var = gini(i);
				double norm = var > 0 ? 1/var : 1; // No normalization if variance = 0;
				//if (m_NbTarget < 15) System.out.println("  Normalization for: "+m_Attrs[i].getName()+" = "+norm);
				weights.setWeight(m_Attrs[i], norm);
			}
		}
	}

	/**
	 * Returns class counts for given target
	 * @param i Target index
	 * @return [Class index]
	 */
	public double[] getClassCounts(int i) {
		return m_ClassCounts[i];
	}

	/**
	 * Returns class counts for all targets
	 * @return [Target index][Class index]
	 */
	public double[][] getClassCounts() {
		return m_ClassCounts;
	}

	public String toString() {
		return getString();
	}

	public void printDistribution(PrintWriter wrt) throws IOException {
		NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
		for (int i = 0; i < m_Attrs.length; i++) {
			wrt.print(StringUtils.printStr(m_Attrs[i].getName(), 35));
			wrt.print(" [");
			double sum = 0.0;
			for (int j = 0; j < m_ClassCounts[i].length; j++) {
				if (j != 0) wrt.print(",");
				wrt.print(m_Attrs[i].getValue(j)+":");
				wrt.print(fr.format(m_ClassCounts[i][j]));
				sum += m_ClassCounts[i][j];
			}
			wrt.println("]: "+fr.format(sum));
		}
	}

	public void predictTuple(DataTuple prediction) {
		for (int i = 0; i < m_NbTarget; i++) {
			NominalAttrType type = m_Attrs[i];
			type.setNominal(prediction, m_MajorityClasses[i]);
		}
	}

	public void vote(ArrayList votes) {
		switch (Settings.m_ClassificationVoteType.getValue()){
			case 0: voteMajority(votes);break;
			case 1: voteProbDistr(votes);break;
			default: voteMajority(votes);
		}
	}

	public void voteMajority(ArrayList votes) {
		reset();		 
		int nb_votes = votes.size();
		m_SumWeight = nb_votes;
		Arrays.fill(m_SumWeights, nb_votes);
		for (int j = 0; j < nb_votes; j++){
			ClassificationStat vote = (ClassificationStat)votes.get(j);
			for (int i = 0; i < m_NbTarget; i++){
				m_ClassCounts[i][vote.getNominalPred()[i]]++;				
			}
		}
		calcMean();
	}

	public void voteProbDistr(ArrayList votes) {
		reset();
		int nb_votes = votes.size();
		for (int j = 0; j < nb_votes; j++){
			ClassificationStat vote = (ClassificationStat) votes.get(j);
			for (int i = 0; i < m_NbTarget; i++){
				addVote(vote);
			}
		}
		calcMean();
	}

	public void addVote(ClusStatistic vote) {
		ClassificationStat or = (ClassificationStat)vote;
		m_SumWeight += or.m_SumWeight;
		for (int i = 0; i < m_NbTarget; i++) {			
			m_SumWeights[i] += or.m_SumWeights[i];
			double[] my = m_ClassCounts[i];
			for (int j = 0; j < my.length; j++) my[j] += or.getProportion(i, j);
		}
	}

	public ClassificationStat getClassificationStat() {
		return this;
	}

	public double[][] getProbabilityPrediction(){
		double[][] result = new double[m_NbTarget][];
		for (int i = 0; i < m_NbTarget; i++) {//for each target
			double total = 0.0;
			for (int k = 0; k < m_ClassCounts[i].length; k++)
				total += m_ClassCounts[i][k]; //get the number of instances
			result[i] = new double[m_ClassCounts[i].length];
			for (int j = 0; j < result[i].length; j++)
				result[i][j] = m_ClassCounts[i][j]/total;//store the frequencies
		}
		return result;
	}
	
	public double getSquaredDistance(ClusStatistic other) {
		double[][] these = getProbabilityPrediction();
		ClassificationStat o = (ClassificationStat)other;
		double[][] others = o.getProbabilityPrediction();
		double result = 0.0;
		for (int i = 0; i < m_NbTarget; i++){//for each target
			double distance = 0.0;
			for (int j = 0; j < these[i].length; j++){//for each class from the target
				distance += (these[i][j] - others[i][j]) * (these[i][j] - others[i][j]);
			}
			result += distance / these[i].length;
		}
		return result/m_NbTarget;
	}
	
	public double getSumWeights(int attr){
		return (m_SumWeights[attr]);
	}
	

}
