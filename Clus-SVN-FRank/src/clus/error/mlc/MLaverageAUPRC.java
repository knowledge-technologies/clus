
package clus.error.mlc;

import java.io.PrintWriter;
import java.text.NumberFormat;

import clus.data.type.NominalAttrType;
import clus.error.common.ClusError;
import clus.error.common.ClusErrorList;
import clus.main.settings.Settings;
import clus.util.ClusFormat;


public class MLaverageAUPRC extends MLROCAndPRCurve {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    
    protected final int m_Measure = averageAUPRC;


    public MLaverageAUPRC(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
    }


    public double getModelError() {
        return getModelError(m_Measure);
    }


    public String getName() {
        return "averageAUPRC";
    }


    public void showModelError(PrintWriter out, int detail) {
        NumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
        computeAll();
        out.println(fr1.format(m_AverageAUPRC));
    }


    public ClusError getErrorClone(ClusErrorList par) {
        return new MLaverageAUPRC(par, m_Attrs); // TO DO: preveriti
    }
}
