/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.distance.primitive;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;


/**
 * @author Mitja Pugelj, Matej Petkovic
 */
public class SearchDistance extends ClusDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private ClusDistance m_Distance;
    public double[] m_MinValues; // minimal values of numeric attributes
    public double[] m_NormalizationWeights; // for attribute i: weight = 1 / (max value - min value)


    public SearchDistance() {
        m_MinValues = null;
        m_NormalizationWeights = null;
    }


    public void setDistance(ClusDistance dist) {
        m_Distance = dist;
    }


    /**
     * Minimal and maximal values of numeric attributes are converted to normalization weights.
     * 
     * @param min_values
     * @param max_values
     */
    public void setNormalizationWeights(double[] min_values, double[] max_values) {
        m_MinValues = new double[min_values.length];
        m_NormalizationWeights = new double[min_values.length];
        for (int i = 0; i < m_NormalizationWeights.length; i++) {
            m_MinValues[i] = min_values[i];
            if (max_values[i] != min_values[i]) { // at least two different values
                m_NormalizationWeights[i] = 1.0 / (max_values[i] - min_values[i]);
            }
            else {
                m_NormalizationWeights[i] = 3.14159; // does not matter which value, if there is only one different
                                                     // value
            }
        }
    }


    /**
     * A wrapper method that returns distance between two tuples based on
     * given (constructor) distance (ClusDistance).
     * 
     * @param t1
     * @param t2
     * @return
     * @throws ClusException 
     */
    @Override
    public double calcDistance(DataTuple t1, DataTuple t2) throws ClusException {
        return m_Distance.calcDistance(t1, t2);
    }


    /**
     * Calculates distance between tuples based only on a a given attribute.
     * In case of numeric values this is |t1-t2| * normalization weight of the attr.<br>
     * 
     * When on of the values is missing max(t,1-t) is taken, where<br>
     * <br>
     * 
     * t = (attr.getNumeric(tuple) - min value of attr) * normalization weight of attr.<br>
     * <br>
     * 
     * When both are missing 1 is returned.
     * For nominal values: if both are non missing and same 0 is returned, 1 otherwise.
     * This function just helps to define different distances.
     * 
     * @param t1
     * @param t2
     * @param attr
     * @return
     */
    public double calcDistanceOnAttr(DataTuple t1, DataTuple t2, ClusAttrType attr) {
        if (attr instanceof NumericAttrType) {
            if (attr.isMissing(t2))
                if (attr.isMissing(t1)) { // both missing
                    return 1.0; // m_AttrWeighting.getWeight(attr); // * 1.0
                }
                else { // t2 missing
                    double t = (attr.getNumeric(t1) - m_MinValues[attr.getIndex()]) * m_NormalizationWeights[attr.getIndex()];
                    return Math.max(t, 1 - t); // * m_AttrWeighting.getWeight(attr);
                }
            else if (attr.isMissing(t1)) { // t1 missing
                double t = (attr.getNumeric(t2) - m_MinValues[attr.getIndex()]) * m_NormalizationWeights[attr.getIndex()];
                return Math.max(t, 1 - t); // * m_AttrWeighting.getWeight(attr);
            }
            else {// both present
                return Math.abs(attr.getNumeric(t2) - attr.getNumeric(t1)) * m_NormalizationWeights[attr.getIndex()];// *
                                                                                                                     // m_AttrWeighting.getWeight(attr);
            }
        }
        else if (attr instanceof NominalAttrType) {
            /*
             * If both values are present end share same value, return 0.
             * Otherwise return 1 (weighted).
             */
            return attr.getNominal(t2) == attr.getNominal(t1) && !attr.isMissing(t2) && !attr.isMissing(t1) ? 0 : 1.0; // m_AttrWeighting.getWeight(attr);
        }
        else {
            throw new IllegalArgumentException(this.getClass().getName() + ":calcDistanceOnAttr() - Distance not supported!");
        }
    }


    /**
     * Returns value for a given tuple and attribute. Nominal values (1..k) are
     * casted to double and returned, numeric values are simply returned.
     * Method provides unified access to value - this is actually needed only by KD tree.
     * 
     * @param t1
     * @param attr
     * @return
     */
    public double getValue(DataTuple t1, ClusAttrType attr) {
        throw new RuntimeException("We do not trust SearchDistance.getValue method yet.");
        // if( attr instanceof NumericAttrType )
        // return attr.getNumeric(t1) * m_Distance.m_AttrWeighting.getWeight(attr);
        // else if( attr instanceof NominalAttrType )
        // return attr.getNominal(t1) * m_Distance.m_AttrWeighting.getWeight(attr);
        // else{
        // throw new IllegalArgumentException("Attribute type " + attr + " is not supported.");
        // }
    }


    @Override
    public ClusDistance getBasicDistance() {
        return m_Distance;
    }


    @Override
    public String getDistanceName() {
        return "Search distance";
    }

}
