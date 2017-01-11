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

package clus.algo.kNN.test.distancee;

import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;

// todo: implement distance for ordinary attributes

/**
 * @author Mitja Pugelj
 */

/**
 * EuclideanDistance works on all type of attributes.
 * For real attributes distance is defined as normal euclidean distance in normalized space.
 * For nominal attributes it is 1 if they are different and 0 otherwise.
 * Ordinary attributes are not yet implemented.
 */
public class EuclideanDistanceFaster extends SearchDistance{

	/**
	 * Return square of euclidean distance.
	 * @param t1
	 * @param t2
	 * @return
	 */
	public double calcDistance(DataTuple t1, DataTuple t2) {
		// @todo: use this Euclidean distance only where all attributes are numerical
		return t1.euclDistance(t2);
		// double dist = 0;
		// for( ClusAttrType attr : t1.getSchema().getAllAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE))
		// dist += Math.pow(this.calcDistanceOnAttr(t1, t2, attr),2);
		// return dist;
	}

	/**
	 * Returns square of distance between two tuples along dimension attr.
	 * @param t1
	 * @param t2
	 * @param attr
	 * @return
	 */
	public double calcDistanceOnAttr(DataTuple t1, DataTuple t2, ClusAttrType attr){
		if( attr instanceof NumericAttrType ){
			return attr.getNumeric(t2)- attr.getNumeric(t1);
		}else
			if( attr instanceof NominalAttrType ){
				return attr.getNominal(t2) == attr.getNominal(t1) ? 0 : 1;
			}else{
				throw new IllegalArgumentException(this.getClass().getName() + "calcDistanceOnAttr() - Distance is not supported!");
			}
	}
}
