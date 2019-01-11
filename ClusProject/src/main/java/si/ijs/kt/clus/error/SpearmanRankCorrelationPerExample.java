
package si.ijs.kt.clus.error;

import java.util.ArrayList;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusNumericError;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.ClusUtil;


/**
 * This class computes the average spearman rank correlation over all target attributes.
 * Ties are not taken into account.
 *
 *
 * The spearman rank correlation is a measure for how well the rankings of the real values
 * correspond to the rankings of the predicted values.
 * 
 * This version of coefficient computes the correlation between multiple true/predicted target values
 * for a given example, and returns the average over all examples.
 * 
 * Some methods are missing.
 *
 * @author beau, matejp
 *
 */
public class SpearmanRankCorrelationPerExample extends ClusNumericError {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected ArrayList<Double> RankCorrelations = new ArrayList<Double>();


    public SpearmanRankCorrelationPerExample(final ClusErrorList par, final NumericAttrType[] num) {
        this(par, num, "");
    }


    public SpearmanRankCorrelationPerExample(final ClusErrorList par, final NumericAttrType[] num, String info) {
        super(par, num);

        setAdditionalInfo(info);
    }


    @Override
    public void addExample(final double[] real, final double[] predicted) {
        // calculate the rank correlation
        double rank = getSpearmanRankCorrelation(real, predicted);
        // add rannk to ranklist
        RankCorrelations.add(rank);
    }


    @Override
    public void addExample(DataTuple real, DataTuple pred) {
        double[] double_real = new double[m_Dim];
        double[] double_pred = new double[m_Dim];
        for (int i = 0; i < m_Dim; i++) {
            double_real[i] = getAttr(i).getNumeric(real);
            double_pred[i] = getAttr(i).getNumeric(pred);

        }
        addExample(double_real, double_pred);
    }


    @Override
    public double getModelErrorComponent(int i) {
        throw new RuntimeException("SpearmanRankCorrelationPerExample does not have multiple components (it's a measure over all dimensions)");
    }


    /**
     * Gives the average (=arithmetic mean) spearman rank correlation over all examples.
     * 
     * @return average spearman rank correlation
     */
    public double getAvgRankCorr() {
        double total = 0;
        for (int i = 0; i < RankCorrelations.size(); i++) {
            total += RankCorrelations.get(i);
        }
        return total / RankCorrelations.size();
    }


    /**
     * Gives the average (=arithmetic mean) spearman rank correlation over all examples.
     * 
     * @return harmonic mean of spearman rank correlations for each example
     */
    public double getHarmonicAvgRankCorr() {
        double total = 0;
        for (int i = 0; i < RankCorrelations.size(); i++) {
            total += 1 / RankCorrelations.get(i);
        }
        return RankCorrelations.size() / total;
    }


    /**
     * Gives the variance of the arithmetic mean of the rank correlation over all examples
     * 
     * @return variance of the average rank correlation
     */
    public double getRankCorrVariance() {
        double avg = getAvgRankCorr();
        double total = 0;
        for (int i = 0; i < RankCorrelations.size(); i++) {
            total += (RankCorrelations.get(i) - avg) * (RankCorrelations.get(i) - avg);
        }
        return total / RankCorrelations.size();
    }


    /**
     * Gives the variance of the harmonic mean of the rank correlation over all examples
     * 
     * @return variance of the average rank correlation
     */
    public double getHarmonicRankCorrVariance() {
        double avg = getHarmonicAvgRankCorr();
        double total = 0;
        for (int i = 0; i < RankCorrelations.size(); i++) {
            total += (RankCorrelations.get(i) - avg) * (RankCorrelations.get(i) - avg);
        }
        return total / RankCorrelations.size();
    }


    @Override
    public String getName() {
        return "Spearman Rank Correlation" + getAdditionalInfoFormatted();
    }

    public double getSpearmanRankCorrelation(double[] a, double[] b) {

        int n = a.length;
        // get the rankings
        double[] ra = ClusUtil.getRanks(a);
        double[] rb = ClusUtil.getRanks(b);
        // substract rankings
        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            d[i] = ra[i] - rb[i];
        }

        // sum the squares of d
        double sum_ds = 0;
        for (int i = 0; i < n; i++) {
            sum_ds += d[i] * d[i];
        }
        // compute the rank: this is wrong: ignores ties ...
        double rank = 1 - (6 * sum_ds) / (n * (n * n - 1));

        return rank;
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        
        // should take into account the getAdditionalInfo() method!
        return null;
    }


    @Override
    public boolean shouldBeLow() {// previously, this method was in ClusError and returned true
        return false;
    }

}
