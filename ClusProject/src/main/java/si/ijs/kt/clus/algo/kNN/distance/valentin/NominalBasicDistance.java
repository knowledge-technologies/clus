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

package si.ijs.kt.clus.algo.kNN.distance.valentin;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.main.settings.Settings;


/**
 * This class represents the distance between 2 values
 * of a certain Nominal Attribute type.
 */
public class NominalBasicDistance extends BasicDistance {

    public NominalBasicDistance(Settings sett) {
        super(sett);
    }


    /**
     * Returns the distance for given tuples for given (Nominal) Attribute
     * Require
     * type : must be a NominalAttrType object
     */
    @Override
    public double getDistance(ClusAttrType type, DataTuple t1, DataTuple t2) {
        NominalAttrType at = (NominalAttrType) type;
        int x = at.getNominal(t1); //returns the attribute value for given attribute in tuple t1
        //Check if missing value
        if (x == at.getNbValues()) {
            x = at.getStatistic().mean();
        }
        int y = at.getNominal(t2); //same for t2
        //Check if missing value
        if (y == at.getNbValues()) {
            y = at.getStatistic().mean();
        }
        if (x != y)
            return 1;
        else
            return 0;
    }

}
