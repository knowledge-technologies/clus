
package si.ijs.kt.clus.error;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNumericError;
import si.ijs.kt.clus.error.common.ComponentError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;


public class RelativeError extends ClusNumericError implements ComponentError {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    protected double[] m_SumRelErr;


    public RelativeError(ClusErrorList par, NumericAttrType[] num) {
        this(par, num, "");
    }


    public RelativeError(ClusErrorList par, NumericAttrType[] num, String info) {
        this(par, num, null, true);
        m_SumRelErr = new double[m_Dim];
    }


    public RelativeError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights) {
        this(par, num, weights, true);
        m_SumRelErr = new double[m_Dim];
    }


    public RelativeError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall) {
        this(par, num, weights, printall, "");
    }


    public RelativeError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall, String info) {
        super(par, num);
        m_SumRelErr = new double[m_Dim];

        setAdditionalInfo(info);
    }


    @Override
    public void addExample(double[] real, double[] predicted) {
        for (int i = 0; i < m_Dim; i++) {
            double err = (real[i] - predicted[i]) / real[i];
            m_SumRelErr[i] += err;

        }
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        double[] predicted = pred.getNumericPred();
        for (int i = 0; i < m_Dim; i++) {
            double err = Math.abs(getAttr(i).getNumeric(tuple) - predicted[i]) / getAttr(i).getNumeric(tuple);
            m_SumRelErr[i] += err;
        }
    }


    @Override
    public void addExample(DataTuple real, DataTuple pred) {
        for (int i = 0; i < m_Dim; i++) {
            double real_i = getAttr(i).getNumeric(real);
            double predicted_i = getAttr(i).getNumeric(pred);
            double err = Math.abs(real_i - predicted_i) / real_i;
            ClusLogger.info(real_i);

            m_SumRelErr[i] += err;

        }
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        
        // should also take into account the getAdditionalError() method!
        return null;
    }


    @Override
    public double getModelErrorComponent(int i) {
        int nb = getNbExamples();
        // ClusLogger.info(m_SumRelErr[i]);
        double err = nb != 0.0 ? m_SumRelErr[i] / nb : 0.0;
        ClusLogger.info(err);

        return err;
    }


    @Override
    public String getName() {

        return "Relative Error" + getAdditionalInfoFormatted();
    }


    @Override
    public boolean shouldBeLow() {
        return true;
    }

}
