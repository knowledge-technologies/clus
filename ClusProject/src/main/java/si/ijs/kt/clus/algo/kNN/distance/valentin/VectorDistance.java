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

package si.ijs.kt.clus.algo.kNN.distance.valentin;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;

/**
 * This class represents distances between DataTuples
 */

public abstract class VectorDistance {

    private ClusAttrType[] m_attrs;
    private double[] m_weights;

    public VectorDistance(ClusAttrType[] attrs,double[] weights){
        setAttribs(attrs);
        m_weights = weights;
    }



    public int amountAttribs(){
        return m_attrs.length;
    }
    public void setAttribs(ClusAttrType[] attrs){
        m_attrs = attrs;
    }
    /**
     * Returns index'th attribute.
     * Require index :  0 <= index < amountAttribs()
     */

    public ClusAttrType getAttrib(int idx){
        return m_attrs[idx];
    }

    public double getWeight(int idx){
        return m_weights[idx];
    }

    /**
     * Returns the distance between the 2 given tuples
     */
    public abstract double getDistance(DataTuple a,DataTuple b);

    @Override
    public abstract String toString();
}