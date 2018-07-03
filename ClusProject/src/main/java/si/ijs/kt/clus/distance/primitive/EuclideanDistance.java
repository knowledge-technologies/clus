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

import java.util.HashSet;
import java.util.Set;

import si.ijs.kt.clus.algo.kNN.distance.attributeWeighting.NoWeighting;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.SparseDataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.main.settings.Settings;


// todo: implement distance for ordinary attributes

/**
 * @author Mitja Pugelj and matejp
 */

/**
 * EuclideanDistance works on all type of attributes.
 * It takes sqrt of sum over all differences in attributes values. It depends on
 * difference defined in SearchDistance.calcDistanceOnAttr().
 */
public class EuclideanDistance extends ClusDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private SearchDistance m_Search;


    public EuclideanDistance(SearchDistance search, boolean isSparse, ClusAttrType[] necessaryDescriptiveAttributes) {
        m_Search = search;
        m_AttrWeighting = new NoWeighting();
        m_IsSparse = isSparse;
        m_Attributes = necessaryDescriptiveAttributes;
    }


    /**
     * Returns the Euclidean distance between given tuples.
     * 
     * @param t1
     *        The first tuple
     * @param t2
     *        The second tuple
     * @return
     */
    @Override
    public double calcDistance(DataTuple t1, DataTuple t2) {
        double dist = 0;
        if (m_IsSparse) {
        	// nominal
        	for(ClusAttrType attr : m_Attributes) {
        		dist += Math.pow(m_Search.calcDistanceOnAttr(t1, t2, attr), 2) * m_AttrWeighting.getWeight(attr);
        	}
        	// non-zero numeric
        	Set<Integer> inds1 = ((SparseDataTuple) t1).getAttributeIndicesSet();
        	Set<Integer> inds2 = ((SparseDataTuple) t2).getAttributeIndicesSet();
        	HashSet<Integer> inds = new HashSet<>(inds1);
        	inds.addAll(inds2);
        	ClusAttrType attr; 
        	for(int ind : inds) {
        		attr = t1.getSchema().getAttrType(ind);
        		dist += Math.pow(m_Search.calcDistanceOnAttr(t1, t2, attr), 2) * m_AttrWeighting.getWeight(attr);
        	}
        }
        else {
	        for (ClusAttrType attr : m_Attributes) {
	            dist += Math.pow(m_Search.calcDistanceOnAttr(t1, t2, attr), 2) * m_AttrWeighting.getWeight(attr);
	        }
        }
        return Math.sqrt(dist);
    }


    @Override
    public String getDistanceName() {
        return "Euclidean distance";
    }
}
