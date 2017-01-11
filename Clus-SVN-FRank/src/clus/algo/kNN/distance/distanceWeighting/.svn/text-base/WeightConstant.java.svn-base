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


package clus.algo.kNN.distance.distanceWeighting;


import clus.algo.kNN.methods.SearchAlgorithm;
import clus.data.rows.DataTuple;
import java.util.LinkedList;

/**
 *
 * @author Mitja Pugelj
 */
public class WeightConstant extends DistanceWeighting{

    /**
     * Inicialize new distance weigthing schem for set of tuples, selected algorithm and interested tuple.
     * @param nearest k nearest tuples to tuple
     * @param search algorithm used
     * @param tuple for which nearest tuples are found
     */
    public WeightConstant(LinkedList<DataTuple> nearest, SearchAlgorithm search, DataTuple tuple){
        super(nearest,search,tuple);
    }

    @Override
    public double weight(DataTuple el) {
        return 1;
    }




}
