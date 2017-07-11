
package clus.distance.sets.seemsRedundantRemoveAfterMerge;

import java.util.Arrays;

import clus.data.type.structured.SetAttrType;
import clus.distance.ClusDistance;
import clus.distance.SetDistance;
import clus.ext.structuredDataTypes.Set;


public class GSMDistanceBitVector extends SetDistance {

    private int maximal_possible_distance = 1;


    /*
     * public GSMDistance(SetAttrType attr, ClusDistance innerDistance) {
     * super(attr,innerDistance);
     * }
     */

    public GSMDistanceBitVector(SetAttrType attr, ClusDistance innerDistance, int maximal_possible_distance) {
        super(attr, innerDistance);
    }


    @Override
    public double calcDistance(Set set1, Set set2) {
        ClusDistance clusDistance = m_ChildDistances[0];
       
        double distance = 0;
        int m = set1.size();
        int n = set2.size();
        int maxCostFlow = Math.abs(m - n);
        int flow = Math.min(m, n);

        double[][] distances = new double[m + 1][n + 1];
        int i = 0, j = 0;

        if (flow > 0)
            for (Object element1 : set1.getValues()) {
                j = 0;
                for (Object element2 : set2.getValues()) {
                    if (clusDistance == null) {
                        distances[i][j] = Math.abs((Double) element1 - (Double) element2);
                    }
                    else {
                        distances[i][j] = clusDistance.calcDistance(element1, element2);
                    }
                    j++;
                }
                i++;
            }

        int mini = -1;
        int minj = -1;
        while (flow > 0) {
            double minCost = Double.MAX_VALUE;
            for (i = 0; i < m; i++) {
                for (j = 0; j < n; j++) {
                    if (distances[i][j] < minCost) {
                        minCost = distances[i][j];
                        mini = i;
                        minj = j;
                    }
                }
            }
            flow--;
            distance += minCost;
            //there is no more flow capacity there so put cost to max
            Arrays.fill(distances[mini], Double.MAX_VALUE);
            for (i = 0; i < m; i++)
                distances[i][minj] = Double.MAX_VALUE;
        }

        //it does matter if the sets are not same cardinality
        //we will put this as separate distance measure, and we will learn 
        //when we do metric learning
        distance += (maxCostFlow * maximal_possible_distance / 2);

       
        return distance;
    }


    @Override
    public String getDistanceName() {
        return "GSMDistanceBitVector distance (sets)";
    }
}
