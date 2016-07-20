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

package clus.algo.kNN.distance;

import clus.algo.kNN.distance.attributeWeighting.AttributeWeighting;
import clus.algo.kNN.distance.attributeWeighting.NoWeighting;
import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.main.Settings;
import clus.statistic.ClusDistance;

/**
 * @author Mitja Pugelj
 */
public class SearchDistance extends ClusDistance{
	private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
	private ClusDistance m_Distance;
	private AttributeWeighting m_AttrWeighting;

	public SearchDistance(){
		m_AttrWeighting = new NoWeighting();
	}

	public void setDistance(ClusDistance dist){
		m_Distance = dist;
	}

	public void setWeighting(AttributeWeighting weighting){
		m_AttrWeighting = weighting;
	}

	/**
	 * Returns weighting used for distance calculation.
	 * @return
	 */
	public AttributeWeighting getWeighting(){
		return m_AttrWeighting;
	}

	/**
	 * A wrapper method that returns distance between two tuples based on
	 * given (constructor) distance (ClusDistance).
	 * @param t1
	 * @param t2
	 * @return
	 */
	public double calcDistance(DataTuple t1, DataTuple t2) {
		return m_Distance.calcDistance(t1, t2);
	}


	/**
	 * Calculates distance between tuples based only on a a given attribute. 
	 * In case of numeric values this is |t1-t2|. When on of the values is missing
	 * max(t,1-t) is taken. When both are missing 1 is returned.
	 * For nominal values: if both are non missing and same 0 is returned, 1 otherwise.
	 * This function just helps to define different distances.
	 * @param t1
	 * @param t2
	 * @param attr
	 * @return
	 */
	public double calcDistanceOnAttr(DataTuple t1, DataTuple t2, ClusAttrType attr){
		if( attr instanceof NumericAttrType ){
			/*
			 * If one of values is missing, return value 1-(others value) to
			 * ensure maximum difference.
			 * If both are missing, return 1.
			 * If both are present, return absolute value.
			 */
			if( attr.isMissing(t2) )
				if( attr.isMissing(t1) ) // both missing
					return m_AttrWeighting.getWeight(attr);
				else // t2 missing
					return Math.max(attr.getNumeric(t1), 1-attr.getNumeric(t1))*m_AttrWeighting.getWeight(attr);
			else
				if( attr.isMissing(t1) ) // t1 missing
					return Math.max(attr.getNumeric(t2), 1-attr.getNumeric(t2))*m_AttrWeighting.getWeight(attr);
				else // both present
					return Math.abs(attr.getNumeric(t2)- attr.getNumeric(t1))*m_AttrWeighting.getWeight(attr);
		}else
			if( attr instanceof NominalAttrType ){
				/*
				 * If both values are present end share same value, return 0.
				 * Otherwise return 1 (weighted).
				 */
				return attr.getNominal(t2) == attr.getNominal(t1) &&
						!attr.isMissing(t2) &&
						!attr.isMissing(t1)
						? 0 : m_AttrWeighting.getWeight(attr);
			}else{
				throw new IllegalArgumentException(this.getClass().getName() + ":calcDistanceOnAttr() - Distance not supported!");
			}
	}


	/**
	 * Returns value for a given tuple and attribute. Nominal values (1..k) are
	 * casted to double and returned, numeric values are simply returned.
	 * Method provides unified access to value - this is actually needed only by KD tree.
	 * @param t1
	 * @param attr
	 * @return
	 */
	public double getValue(DataTuple t1, ClusAttrType attr) {
		if( attr instanceof NumericAttrType )
			return attr.getNumeric(t1)*m_AttrWeighting.getWeight(attr);
		else if( attr instanceof NominalAttrType )
			return attr.getNominal(t1)*m_AttrWeighting.getWeight(attr);
		else{
			throw new IllegalArgumentException("Attribute type " + attr + " is not supported.");
		}
	}

	public ClusDistance getBasicDistance() {
		return m_Distance;
	}



}
