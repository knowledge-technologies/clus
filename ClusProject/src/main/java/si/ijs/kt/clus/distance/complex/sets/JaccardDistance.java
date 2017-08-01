
package si.ijs.kt.clus.distance.complex.sets;

import si.ijs.kt.clus.data.type.complex.SetAttrType;
import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.distance.complex.SetDistance;
import si.ijs.kt.clus.ext.structuredTypes.Set;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusException;


public class JaccardDistance extends SetDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public JaccardDistance(SetAttrType attr, ClusDistance innerDistance) {
        super(attr, innerDistance);
    }


    public JaccardDistance(ClusDistance innerDistance) {
        super(innerDistance);
    }


    @Override
    public double calcDistance(Set set1, Set set2) throws ClusException {
        ClusDistance clusDistance = m_ChildDistances[0];
        double union = set1.getValues().length + set2.getValues().length;
        double intersection = 0;
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
                    intersection++;
                    union--;
                }
            }
        }

        return 1 - (intersection / union);
    }


    @Override
    public String getDistanceName() {
        return "Jaccard distance (sets)";
    }
}
