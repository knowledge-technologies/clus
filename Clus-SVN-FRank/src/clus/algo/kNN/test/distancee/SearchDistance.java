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

/**
 * @author Mitja Pugelj
 */


public abstract class SearchDistance {

	public abstract double calcDistance(DataTuple t1, DataTuple t2);
	public abstract double calcDistanceOnAttr(DataTuple t1, DataTuple t2, ClusAttrType attr);

	public double getValue(DataTuple t1, ClusAttrType attr) {
		if( attr instanceof NumericAttrType )
			return attr.getNumeric(t1);
		else if( attr instanceof NominalAttrType )
			return attr.getNominal(t1);
		else{
			System.out.println(attr);
			throw new IllegalArgumentException("Attribute type is not supported.");
		}
	}
}
