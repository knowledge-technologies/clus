
package si.ijs.kt.clus.error.mlc;

import java.io.PrintWriter;

import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.format.ClusNumberFormat;


public class MLaverageAUPRC extends MLROCAndPRCurve {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    
    protected final int m_Measure = averageAUPRC;


    public MLaverageAUPRC(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
    }


    @Override
    public double getModelError() {
        return getModelError(m_Measure);
    }


    @Override
    public String getName() {
        return "averageAUPRC";
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
    	ClusNumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
        computeAll();
        out.println(fr1.format(m_AverageAUPRC));
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new MLaverageAUPRC(par, m_Attrs); // TO DO: preveriti
    }
}
