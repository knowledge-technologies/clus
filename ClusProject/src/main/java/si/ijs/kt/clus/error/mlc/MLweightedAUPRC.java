
package si.ijs.kt.clus.error.mlc;

import java.io.PrintWriter;
import java.text.NumberFormat;

import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.format.ClusFormat;


public class MLweightedAUPRC extends MLROCAndPRCurve {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    
    protected final int m_Measure = weightedAUPRC;


    public MLweightedAUPRC(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
    }


    @Override
    public double getModelError() {
        return getModelError(m_Measure);
    }


    @Override
    public String getName() {
        return "weightedAUPRC";
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        NumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
        computeAll();
        out.println(fr1.format(m_WAvgAUPRC));
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new MLweightedAUPRC(par, m_Attrs);
    }
}
