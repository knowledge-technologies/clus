
package clus.error;

import java.io.PrintWriter;
import java.text.NumberFormat;

import clus.data.attweights.ClusAttributeWeights;
import clus.data.rows.DataTuple;
import clus.data.type.primitive.NumericAttrType;
import clus.main.settings.Settings;
import clus.error.common.ClusError;
import clus.error.common.ClusErrorList;
import clus.statistic.ClusStatistic;


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


    public void reset() {
        super.reset();
    }


    public void add(ClusError other) {
        super.add(other);
        RRMSError castedOther = (RRMSError) other;
        for (int i = 0; i < m_Dim; i++) {
            m_SumTrueValues[i] += castedOther.m_SumTrueValues[i];
            m_SumSquaredTrueValues[i] += castedOther.m_SumSquaredTrueValues[i];
        }
    }


    public void addExample(double[] real, double[] predicted) {
        super.addExample(real, predicted, true);
    }


    public void addExample(double[] real, boolean[] predicted) {
        super.addExample(real, predicted, true);
    }


    public void addExample(DataTuple tuple, ClusStatistic pred) {
        super.addExample(tuple, pred, true);
    }


    public void addExample(DataTuple real, DataTuple pred) {
        super.addExample(real, pred, true);
    }


    public void showSummaryError(PrintWriter out, boolean detail) {
        showModelError(out, detail ? 1 : 0);
    }


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


    public String getName() {
        return "Root Relative Squared Error (RRMSE)";
    }


    public double getModelError() {
        double sum = 0.0;
        for (int i = 0; i < m_Attrs.length; i++) {
            sum += getModelErrorComponent(i);
        }
        return sum / m_Attrs.length;
    }


    public double getModelErrorComponent(int i) {
        double modelError = super.getModelErrorComponent(i);
        double defaultModelError = (m_SumSquaredTrueValues[i] - m_SumTrueValues[i] * m_SumTrueValues[i] / m_nbEx[i]) / m_nbEx[i];
        return Math.sqrt(modelError / defaultModelError);

    }


    public ClusError getErrorClone(ClusErrorList par) {
        return new RRMSError(par, m_Attrs, m_Weights, m_PrintAllComps);
    }

}
