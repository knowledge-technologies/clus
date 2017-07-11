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

package clus.distance.primitive;

import clus.algo.kNN.distance.attributeWeighting.NoWeighting;
import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.distance.ClusDistance;
import clus.main.settings.Settings;


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


    public EuclideanDistance(SearchDistance search) {
        m_Search = search;
        m_AttrWeighting = new NoWeighting();
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
    public double calcDistance(DataTuple t1, DataTuple t2) {
        double dist = 0;
        for (ClusAttrType attr : t1.getSchema().getAllAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE)) {
            dist += Math.pow(m_Search.calcDistanceOnAttr(t1, t2, attr), 2) * m_AttrWeighting.getWeight(attr);
        }
        return Math.sqrt(dist);
    }


    @Override
    public String getDistanceName() {
        return "Euclidean distance";
    }
}
