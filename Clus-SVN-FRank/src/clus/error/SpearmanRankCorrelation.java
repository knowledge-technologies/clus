
package clus.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import clus.data.rows.DataTuple;
import clus.data.type.NumericAttrType;
import clus.error.common.ClusError;
import clus.error.common.ClusErrorList;
import clus.error.common.ClusNumericError;
import clus.main.Settings;


/**
 * This class computes the average spearman rank correlation over all target attributes.
 * Ties are not taken into account.
 *
 *
 * The spearman rank correlation is a measure for how well the rankings of the real values
 * correspond to the rankings of the predicted values.
 *
 * @author beau, matejp
 *
 */
public class SpearmanRankCorrelation extends ClusNumericError {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    
    protected ArrayList<Double> RankCorrelations = new ArrayList<Double>();


    public SpearmanRankCorrelation(final ClusErrorList par, final NumericAttrType[] num) {
        super(par, num);
    }


    @Override
    public void addExample(final double[] real, final double[] predicted) {
        // calculate the rank correlation
        double rank = getSpearmanRankCorrelation(real, predicted);
        // add rannk to ranklist
        RankCorrelations.add(rank);
    }


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
        throw new RuntimeException("SpearmanRankCorrelation does not have multiple components (it's a measure over all dimensions)");
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
        return "Spearman Rank Correlation";
    }


    /**
     * Computes the rank of each value in the given array
     * @param values
     * @return an array with the corresponding ranking
     */
    private double[] getRanks(double[] values) {
        double[] result = new double[values.length];
        // brute force! O(n*n) should be re-implemented
        //int rank = values.length;
        //for (int v = 0; v < values.length; v++) {
        //    for (int i = 0; i < values.length; i++) {
        //        if (values[i] < values[v]) {
        //            rank--;
        //        }
        //    }
        //    result[v] = rank;
        //    rank = values.length;
        //}
        
        // new implementation: we assume that the previous was OK:
        // the new was tested vs. the old-one and it gives the same results on the tests from 
        // double[][] tests = new double[][]{new double[]{1.0, 1.0, 1.0, 1.00}, new double[]{1.0, 2.0, 4.0, 2.0, 4.0, 3.0,1.3,3.7}, new double[]{1.0, 1.0, 2.0, 2.0, 3.0, 3.0}, new double[]{1, 2, 3, 4, 5, 4, 3,2,1}};
        final double[] scores = values;
        Integer[] indices = new Integer[values.length];
        for(int i = 0; i < values.length; i++){
        	indices[i] = i;
        }
        Arrays.sort(indices, new Comparator<Integer>() {
			@Override
			public int compare(Integer ind1, Integer ind2) {
				return Double.compare(scores[ind1], scores[ind2]);
			}
		});
        
        result[indices[0]] =  values.length;
        for(int i = 1; i < values.length; i++){
        	int index = indices[i];
        	int previous = indices[i - 1];        	
        	if(scores[previous] < scores[index]){	// precisely all previous elements are strictly smaller
        		result[index] = values.length - i;
        	} else{									// i.e. >= hence == hence the same result 
        		result[index] = result[previous];
        	}
        }
        return result;

    }


    public double getSpearmanRankCorrelation(double[] a, double[] b) {

        int n = a.length;
        // get the rankings
        double[] ra = getRanks(a);
        double[] rb = getRanks(b);
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
        // compute the rank
        double rank = 1 - (6 * sum_ds) / (n * (n * n - 1));

        return rank;
    }


    @Override
    public ClusError getErrorClone(ClusErrorList par) {
        // TODO Auto-generated method stub
        return null;
    }


	public boolean shouldBeLow() {//previously, this method was in ClusError and returned true
		return false;
	}

}
