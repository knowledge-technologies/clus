
package si.ijs.kt.clus.error;

import java.io.PrintWriter;
import java.text.NumberFormat;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.statistic.ClusStatistic;


/**
 * Relative root mean squared error. If the number of targets is grater than 1, the average over RRMSEs over the targets
 * is additionally computed.
 * The error value is made relative by dividing it by the error of the default model which predicts the average target
 * values on the given
 * training or test data.
 * 
 * @author matejp
 *
 */
public class RRMSError extends MSError {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public RRMSError(ClusErrorList par, NumericAttrType[] num) {
        super(par, num);
    }


    public RRMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights) {
        super(par, num, weights);
    }


    public RRMSError(ClusErrorList par, NumericAttrType[] num, ClusAttributeWeights weights, boolean printall) {
        super(par, num, weights, printall);

    }


    @Override
    public void reset() {
        super.reset();
    }


    @Override
    public void add(ClusError other) {
        super.add(other);
        RRMSError castedOther = (RRMSError) other;
        for (int i = 0; i < m_Dim; i++) {
            m_SumTrueValues[i] += castedOther.m_SumTrueValues[i];
            m_SumSquaredTrueValues[i] += castedOther.m_SumSquaredTrueValues[i];
        }
    }


    @Override
    public void addExample(double[] real, double[] predicted) {
        super.addExample(real, predicted, true);
    }


    @Override
    public void addExample(double[] real, boolean[] predicted) {
        super.addExample(real, predicted, true);
    }


    @Override
    public void addExample(DataTuple tuple, ClusStatistic pred) {
        super.addExample(tuple, pred, true);
    }


    @Override
    public void addExample(DataTuple real, DataTuple pred) {
        super.addExample(real, pred, true);
    }


    @Override
    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }


    @Override
    public void showModelError(PrintWriter out, int detail) {
        NumberFormat fr = getFormat();
        StringBuffer buf = new StringBuffer();
        if (m_PrintAllComps) {
            buf.append("[");
            for (int i = 0; i < m_Dim; i++) {
                if (i != 0)
                    buf.append(",");
                buf.append(fr.format(getModelErrorComponent(i)));
            }
            if (m_Dim > 1)
                buf.append("]: ");
            else
                buf.append("]");
        }
        if (m_Dim > 1 || !m_PrintAllComps) {
            buf.append(fr.format(getModelError()));
        }
        out.println(buf.toString());
    }


    @Override
    public String getName() {
        return "Root Relative Squared Error (RRMSE)";
    }


    @Override
    public double getModelError() {
        double sum = 0.0;
        for (int i = 0; i < m_Attrs.length; i++) {
            sum += getModelErrorComponent(i);
        }
        return sum / m_Attrs.length;
    }


    @Override
    public double getModelErrorComponent(int i) {
        double modelError = super.getModelErrorComponent(i);
        double defaultModelError = (m_SumSquaredTrueValues[i] - m_SumTrueValues[i] * m_SumTrueValues[i] / m_nbEx[i]) / m_nbEx[i];
        
        if (defaultModelError == 0 )
        {
            System.out.println("DASDASD");
        }
        return Math.sqrt(modelError / defaultModelError);

    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        return new RRMSError(par, m_Attrs, m_Weights, m_PrintAllComps);
    }

}
