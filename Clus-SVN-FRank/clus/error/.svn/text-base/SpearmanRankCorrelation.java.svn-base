package clus.error;

import java.util.ArrayList;

import clus.data.rows.DataTuple;
import clus.data.type.NumericAttrType;


/**
 * This class computes the average spearman rank correlation over all target attributes.
 * Ties are not taken into account.
 *
 *
 * The spearman rank correlation is a measure for how well the rankings of the real values
 * correspond to the rankings of the predicted values.
 *
 * @author beau
 *
 */
public class SpearmanRankCorrelation extends ClusNumericError {

	protected ArrayList<Double> RankCorrelations = new ArrayList<Double>();;

	public SpearmanRankCorrelation(final ClusErrorList par, final NumericAttrType[] num) {
		super(par, num);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addExample(final double[] real, final double[] predicted) {
		//calculate the rank correlation
		double rank = getSpearmanRankCorrelation(real,predicted);
		//add rannk to ranklist
		RankCorrelations.add(rank);
	}

	public void addExample(DataTuple real, DataTuple pred) {
		double[] double_real = new double[m_Dim];
		double[] double_pred = new double[m_Dim];
		for (int i = 0; i < m_Dim; i++) {
				double_real[i] = getAttr(i).getNumeric(real);
				double_pred[i] = getAttr(i).getNumeric(pred);

		}
		addExample(double_real,double_pred);
	}

	@Override
	public double getModelErrorComponent(int i) {
		System.err.println("SpearmanRankCorrelation does not have multiple components (it's a measure over all dimensions)");
		return 0.0;
	}


	/**
	 * Gives the average (=arithmetic mean) spearman rank correlation over all examples.
	 * @return average spearman rank correlation
	 */
	public double getAvgRankCorr(){
		double total = 0;
		for(int i = 0;i<RankCorrelations.size();i++){
			total += RankCorrelations.get(i);
		}
		return total/RankCorrelations.size();
	}

	/**
	 * Gives the average (=arithmetic mean) spearman rank correlation over all examples.
	 * @return harmonic mean of spearman rank correlations for each example
	 */
	public double getHarmonicAvgRankCorr(){
		double total = 0;
		for(int i = 0;i<RankCorrelations.size();i++){
			total += 1/RankCorrelations.get(i);
		}
		return RankCorrelations.size()/total;
	}

	/**
	 * Gives the variance of the arithmetic mean of the rank correlation over all examples
	 * @return variance of the average rank correlation
	 */
	public double getRankCorrVariance(){
		double avg = getAvgRankCorr();
		double total = 0;
		for(int i = 0;i<RankCorrelations.size();i++){
			total += (RankCorrelations.get(i)-avg)*(RankCorrelations.get(i)-avg);
		}
		return total/RankCorrelations.size();
	}

	/**
	 * Gives the variance of the harmonic mean of the rank correlation over all examples
	 * @return variance of the average rank correlation
	 */
	public double getHarmonicRankCorrVariance(){
		double avg = getHarmonicAvgRankCorr();
		double total = 0;
		for(int i = 0;i<RankCorrelations.size();i++){
			total += (RankCorrelations.get(i)-avg)*(RankCorrelations.get(i)-avg);
		}
		return total/RankCorrelations.size();
	}


	@Override
	public String getName() {
		return "Spearman Rank Correlation";
	}


	/**
	 * Computes the rank of each value in the given array
	 * Current implementation is O(n^2) with n = values.length.
	 * I think an optimal implementation would be O(log n) using quicksort
	 *
	 * @param values
	 * @return an array with the corresponding ranking
	 */
	private double[] getRanks(double[] values){
		double[] result = new double[values.length];
		//brute force! O(n*n) should be re-implemented
		int rank = values.length;

		for(int v=0;v<values.length;v++){
			for(int i = 0; i<values.length;i++){
				if(values[i]<values[v]){
					rank--;
				}
			}
			result[v]=rank;
			rank = values.length;
		}



		return result;

	}

	public double getSpearmanRankCorrelation(double[] a, double[] b){

		int n = a.length;
		//get the rankings
		double[] ra = getRanks(a);
		double[] rb = getRanks(b);
		//substract rankings
		double[] d = new double[n];
		for(int i = 0;i<n;i++){
			d[i] = ra[i]-rb[i];
		}

		//sum the squares of d
		double sum_ds = 0;
		for(int i = 0;i<n;i++){
			sum_ds  += d[i]*d[i];
		}
		//compute the rank
		double rank =  1-(6*sum_ds)/(n*(n*n-1));

		return rank;
	}

	@Override
	public ClusError getErrorClone(ClusErrorList par) {
		// TODO Auto-generated method stub
		return null;
	}

}
