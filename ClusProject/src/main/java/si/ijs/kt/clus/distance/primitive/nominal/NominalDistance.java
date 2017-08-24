
package si.ijs.kt.clus.distance.primitive.nominal;

import si.ijs.kt.clus.distance.ClusDistance;
import si.ijs.kt.clus.main.settings.Settings;


public class NominalDistance extends ClusDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    @Override
    public double calcDistance(Object nominal1, Object nominal2) {
        if (nominal1.toString().equalsIgnoreCase(nominal2.toString())) {
            return 0;
        }
        else {
            return 1;
        }
    }


    @Override
    public String getDistanceName() {
        return "Nominal distance"; // 
    }
}
