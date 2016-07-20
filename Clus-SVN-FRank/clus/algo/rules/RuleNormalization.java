/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2010                                                    *
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
 * Created on March 8, 2010
 */
package clus.algo.rules;

/** Information about rule normalization. Is used in rule optimization and linear term normalization.
 * @author Timo Aho
 */
public class RuleNormalization {

	/** Class variable. Means of descriptive attributes */
	static private double[] C_descMeans = null;
	/** Class variable. Standard deviation values of descriptive attributes */
	static private double[] C_descStdDevs = null;
	/** Class variable. Means of target attributes */
	static private double[] C_targMeans = null;
	/** Class variable. Standard deviation values of target attributes */
	static private double[] C_targStdDevs = null;

	/** Initializes the statistical information about the data. Index 0 includes means, index 1 std dev
	 * for both the parameters. 
	 * @param descMeanAndStdDev Mean and std dev for descriptive attributes.
	 * @param targMeanAndStdDev Mean and std dev for target attributes.   
	 */
	public static void initialize(double[][] descMeanAndStdDev, double[][] targMeanAndStdDev) {
		C_descStdDevs = descMeanAndStdDev[1];
		C_descMeans = descMeanAndStdDev[0];
		
		C_targStdDevs = targMeanAndStdDev[1];
		C_targMeans = targMeanAndStdDev[0];
	}

	public static double getDescMean(int iDescriptiveAttr) {
		return C_descMeans[iDescriptiveAttr];
	}
	
	public static double getTargMean(int iTargetAttr) {
		return C_targMeans[iTargetAttr];
	}
	
	public static double getDescStdDev(int iDescriptiveAttr) {
		return C_descStdDevs[iDescriptiveAttr];
	}
	
	public static double getTargStdDev(int iTargetAttr) {
		return C_targStdDevs[iTargetAttr];
	}

}
