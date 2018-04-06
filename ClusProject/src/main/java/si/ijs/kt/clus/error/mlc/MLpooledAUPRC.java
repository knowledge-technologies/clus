
package si.ijs.kt.clus.error.mlc;

import java.io.PrintWriter;
import java.text.NumberFormat;

import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.format.ClusFormat;


public class MLpooledAUPRC extends MLROCAndPRCurve {
 
    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    private final int m_Measure = pooledAUPRC;


    public MLpooledAUPRC(ClusErrorList par, NominalAttrType[] nom) {
        super(par, nom);
    }


    @Override
    public double getModelError() {
        return getModelError(m_Measure);
    }


    @Override
    public String getName() {
        return "pooledAUPRC";
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        NumberFormat fr1 = ClusFormat.SIX_AFTER_DOT;
        computeAll();
        out.println(fr1.format(m_PooledAUPRC));
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new MLpooledAUPRC(par, m_Attrs);
    }
}
