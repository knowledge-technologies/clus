
package si.ijs.kt.clus.distance.complex.sets;

import si.ijs.kt.clus.data.type.complex.SetAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.complex.SetDistance;
import si.ijs.kt.clus.ext.structuredTypes.Set;
import si.ijs.kt.clus.main.settings.Settings;


public class HammingLossDistance extends SetDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private int numberOfTotalPossibleValues = 1;


    public HammingLossDistance(SetAttrType attr, ClusDistance innerDistance, int numberOfTotalPossibleValues) {
        super(attr, innerDistance);
        this.numberOfTotalPossibleValues = numberOfTotalPossibleValues;
    }


    public HammingLossDistance(ClusDistance innerDistance, int numberOfTotalPossibleValues) {
        super(innerDistance);
        this.numberOfTotalPossibleValues = numberOfTotalPossibleValues;
    }


    public HammingLossDistance(SetAttrType attr, ClusDistance innerDistance) {
        this(attr, innerDistance, 1);
    }


    @Override
    public double calcDistance(Set set1, Set set2) {
        ClusDistance clusDistance = m_ChildDistances[0];
        double distance = set1.getValues().length + set2.getValues().length;
        for (Object element1 : set1.getValues()) {
            double dist = 1;
            for (Object element2 : set2.getValues()) {
                if (clusDistance == null) {
                    dist = Math.abs((Double) element1 - (Double) element2);
                }
                else {
                    dist = clusDistance.calcDistance(element1, element2);
                }
                if (dist == 0) {
                    distance -= 2;
                    break;
                }
            }
        }

        return distance / numberOfTotalPossibleValues;
    }


    @Override
    public String getDistanceName() {
        return "Hamming loss (sets)";
    }
}
