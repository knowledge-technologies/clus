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

package clus.heuristic;

import clus.main.*;
import clus.util.*;
import jeans.math.*;

import java.util.*;

import org.apache.commons.math.*;
import org.apache.commons.math.distribution.*;

public class FTest {

	public static double[] FTEST_SIG = {1.0, 0.1, 0.05, 0.01, 0.005, 0.001, 0.0};
	public static double FTEST_LIMIT;
	public static double[] FTEST_VALUE;

	public final static FDistribution m_FDist = DistributionFactory.newInstance().createFDistribution(1,1);

	protected final static double critical_f_01[] = {
		39.8161, 8.5264, 5.5225, 4.5369, 4.0804, 3.7636,
		3.61, 3.4596, 3.3489, 3.2761, 3.24, 3.1684, 3.1329, 3.0976,
		3.0625, 3.0625, 3.0276, 2.9929, 2.9929, 2.9584
	};

	protected final static double critical_f_005[] = {
		161.0, 18.5, 10.1, 7.71, 6.61, 5.99, 5.59, 5.32, 5.12, 4.96,
		4.84, 4.75, 4.67, 4.6, 4.54, 4.49, 4.45, 4.41, 4.38, 4.35,
		4.32, 4.3, 4.28, 4.26, 4.24, 4.23, 4.21, 4.2, 4.18, 4.17
	};

	protected final static double critical_f_001[] = {
		4052.0, 98.5, 34.1, 21.2, 16.3, 13.7, 12.2, 11.3, 10.6, 10.0,
		9.65, 9.33, 9.07, 8.86, 8.68, 8.53, 8.40, 8.29, 8.18, 8.1,
		8.02, 7.95, 7.88, 7.82, 7.77, 7.72, 7.68, 7.64, 7.6, 7.56
	};

	// for 0.005: derived from t-table,  squared values (=> approximate!)
	protected final static double critical_f_0005[] = {
		15876, 198.81, 55.5025, 31.36, 22.7529, 18.6624, 16.2409, 14.6689, 13.6161, 12.8164,
		12.25, 11.7649, 11.2896, 11.0889, 10.8241, 10.5625, 10.3684, 10.24, 10.0489, 9.9225,
		9.8596, 9.7344, 9.61, 9.5481, 9.4864, 9.4249, 9.3636, 9.3025, 9.2416, 9.1809
	};

	// for 0.001: derived from t-table,  squared values (=> approximate!)
	protected final static double critical_f_0001[] = {
		405769, 998.56, 166.41, 74.1321, 47.0596, 35.5216, 29.16, 25.4016, 22.8484, 21.0681,
		19.7136, 18.6624, 17.8084, 17.1396, 16.5649, 16.0801, 15.6025, 15.3664, 15.0544, 14.8225,
		14.5924, 14.3641, 14.2129, 13.9876, 13.8384, 13.7641, 13.6161, 13.4689, 13.3956, 13.3225
	};

	public static int getLevelAndComputeArray(double significance) {
		int maxlevel = FTEST_SIG.length-1;
		for (int level = 0; level < maxlevel; level++) {
			if (Math.abs(significance - FTEST_SIG[level])/FTEST_SIG[level] < 0.01) {
				return level;
			}
		}
		FTEST_SIG[maxlevel] = significance;
		initializeFTable(significance);
		return maxlevel;
	}

	public static double getCriticalF(int level, int df) {
		switch (level) {
			case 1: // 0.1
				if (df <= 20) return critical_f_01[df-1];
				else if (df <= 30) return 2.9;
				else if (df <= 40) return 2.86;
				else if (df <= 120) return 2.79;
				else return 2.7;
			case 2:	// 0.05
				if (df <= 30) return critical_f_005[df-1];
				else if (df <= 40) return 4.08;
				else if (df <= 60) return 4.00;
				else if (df <= 120) return 3.92;
				else return 3.84;
			case 3:	// 0.01
				if (df <= 30) return critical_f_001[df-1];
				else if (df <= 40) return 7.31;
				else if (df <= 60) return 7.08;
				else if (df <= 120) return 6.85;
				else return 6.63;
			case 4: // 0.005
				if (df <= 30) return critical_f_0005[df-1];
				else if (df <= 40) return 8.82;
				else if (df <= 60) return 8.47;
				else if (df <= 120) return 8.18;
				else return 7.90;
			case 5: // 0.001
				if (df <= 30) return critical_f_0001[df-1];
				else if (df <= 40) return 12.60;
				else if (df <= 60) return 11.98;
				else if (df <= 120) return 11.36;
				else return 10.82;
			default:
				return df < FTEST_VALUE.length ? FTEST_VALUE[df] : FTEST_LIMIT;
		}
	}

	public static double getCriticalFCommonsMath(double sig, double df) {
		try {
			m_FDist.setDenominatorDegreesOfFreedom(df);
			return m_FDist.inverseCumulativeProbability(1-sig);
		} catch (MathException e) {
			System.err.println("F-Distribution error: "+e.getMessage());
			return 0.0;
		}
	}

	// Calling getCriticalFCommonsMath() is slow, so build a table
	public static void initializeFTable(double sig) {
		int df = 3;
		double value = 0.0;
		double limit = getCriticalFCommonsMath(sig, 1e5);
		ArrayList values = new ArrayList();
		do {
			value = getCriticalFCommonsMath(sig, df);
			values.add(new Double(value));
			df++;
		} while ((value - limit)/limit > 0.05);
		System.out.println("F-Test = "+sig+" limit = "+ClusFormat.TWO_AFTER_DOT.format(limit)+" values = "+values.size());
		FTEST_LIMIT = limit;
		FTEST_VALUE = new double[values.size()+3];
		for (int i = 0; i < values.size(); i++) {
			FTEST_VALUE[i+3] = ((Double)values.get(i)).doubleValue();
		}
	}

	// ftest: Signif, total SS, residual SS, 2nd DF (1st is 1)
	// (this implementation only works for F tests with 1 and n d.f., sorry)
	// ftest predicate succeeds iff H0 is rejected at Signif
	// only works correctly for signif 1.0, 0.1, 0.05, 0.01, 0.005, 0.001

	public static boolean ftest(int level, double sst, double ssr, int df) {
		if (level == 0) return true;
		if (sst <= 0.0) return false; // added to avoid 0/0; SST = 0 => no improvement possible
		if (ssr == 0.0) return true;  // avoid x/0; SSR = 0 => F is infinite
		double f = (double)df * (sst - ssr) / ssr;
		double cf = getCriticalF(level, df);
		return f > cf;
	}

	public static double getSettingSig() {
		return FTEST_SIG[Settings.FTEST_LEVEL];
	}

	public static double calcVarianceReductionHeuristic(double n_tot, double ss_tot, double ss_sum) {
		double value = ss_tot - ss_sum;
		if (value < MathUtil.C1E_9) {
			// Gain too small
			return Double.NEGATIVE_INFINITY;
		}
		if (Settings.FTEST_LEVEL == 0) {
			// No F-test -> just return value
			return value;
		}
		int n_2 = (int)Math.floor(n_tot - 2.0 + 0.5);
		if (n_2 <= 0) {
			return Double.NEGATIVE_INFINITY;
		} else {
			if (FTest.ftest(Settings.FTEST_LEVEL, ss_tot, ss_sum, n_2)) {
				return value;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		}
	}
}
