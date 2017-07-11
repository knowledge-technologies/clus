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

package clus.algo.kNN.distance.valentin;

import clus.data.type.ClusAttrType;
import clus.data.type.primitive.NumericAttrType;
import clus.data.rows.DataTuple;
import clus.main.settings.Settings;


/**
 * This class represents the distance between 2 values
 * of a certain Numerical Attribute type.
 */
public class NumericBasicDistance extends BasicDistance {

    public NumericBasicDistance(Settings sett) {
        super(sett);
    }


    /**
     * Returns the distance for given tuples for given (Numerical) Attribute
     * Require
     * type : must be a NumericAttrType object
     * 
     */
    public double getDistance(ClusAttrType type, DataTuple t1, DataTuple t2) {
        NumericAttrType at = (NumericAttrType) type;
        double x = at.getNumeric(t1); //returns the attribute value for given attribute in tuple t1

        //Check if missing value
        if (x == Double.NaN) {
            x = at.getStatistic().mean();
        }
        double y = at.getNumeric(t2); //same for t2
        //Check if missing value
        if (y == Double.NaN) {
            y = at.getStatistic().mean();
        }
        //normalize if wanted
        if (getSettings().getKNNTree().getKNNTNormalized()) {
            NumericStatistic numStat = at.getStatistic();
            double min = numStat.min();
            double max = numStat.max();
            double dif = max - min;
            x = (x - min) / dif;
            y = (y - min) / dif;
        }
        return Math.abs(x - y);
    }
}