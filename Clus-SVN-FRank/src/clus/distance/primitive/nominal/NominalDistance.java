
package clus.distance.primitive.nominal;

import clus.distance.ClusDistance;
import clus.main.settings.Settings;


public class NominalDistance extends ClusDistance {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


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
