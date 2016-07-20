package clus.heuristic;

import java.io.IOException;
import java.util.ArrayList;

import clus.algo.tdidt.ClusNode;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;

import org.apache.commons.math.special.Gamma;


public class EncodingCost {

	protected ArrayList<RowData> data;
	protected ArrayList<Integer> m_ClusterIds;
	protected ArrayList<ClusNode> nodes;
	protected ClusAttrType[] attributes;
	protected int m_nbSequences;
	
	//Blocks9 model
	protected double[] m_MixtureValues = {0.178091, 0.056591, 0.0960191, 0.0781233, 0.0834977, 0.0904123, 0.114468, 0.0682132, 0.234585}; 
	protected double[][] m_AlphaValues = { 	
			{1.18065,  0.270671,  0.039848,  0.017576,  0.016415,  0.014268,  0.131916,  0.012391,  0.022599,  0.020358,  0.030727,  0.015315,  0.048298, 0.053803,  0.020662,  0.023612, 0.216147, 0.147226, 0.065438, 0.003758, 0.009621},
			{1.35583, 0.021465, 0.0103, 0.011741, 0.010883, 0.385651, 0.016416, 0.076196, 0.035329, 0.013921, 0.093517, 0.022034, 0.028593, 0.013086, 0.023011, 0.018866, 0.029156, 0.018153, 0.0361, 0.07177, 0.419641},
			{6.66436, 0.561459, 0.045448, 0.438366, 0.764167, 0.087364, 0.259114, 0.21494, 0.145928, 0.762204, 0.24732, 0.118662, 0.441564, 0.174822, 0.53084, 0.465529, 0.583402, 0.445586, 0.22705, 0.02951, 0.12109},
			{2.08141, 0.070143, 0.01114, 0.019479, 0.094657, 0.013162, 0.048038, 0.077, 0.032939, 0.576639, 0.072293, 0.02824, 0.080372, 0.037661, 0.185037, 0.506783, 0.073732, 0.071587, 0.042532, 0.011254, 0.028723},
			{2.08101, 0.041103, 0.014794, 0.00561, 0.010216, 0.153602, 0.007797, 0.007175, 0.299635, 0.010849, 0.999446, 0.210189, 0.006127, 0.013021, 0.019798, 0.014509, 0.012049, 0.035799, 0.180085, 0.012744, 0.026466},
			{2.56819, 0.115607, 0.037381, 0.012414, 0.018179, 0.051778, 0.017255, 0.004911, 0.796882, 0.017074, 0.285858, 0.075811, 0.014548, 0.015092, 0.011382, 0.012696, 0.027535, 0.088333, 0.94434, 0.004373, 0.016741},
			{1.76606, 0.093461, 0.004737, 0.387252, 0.347841, 0.010822, 0.105877, 0.049776, 0.014963, 0.094276, 0.027761, 0.01004, 0.187869, 0.050018, 0.110039, 0.038668, 0.119471, 0.065802, 0.02543, 0.003215, 0.018742},
			{4.98768, 0.452171, 0.114613, 0.06246, 0.115702, 0.284246, 0.140204, 0.100358, 0.55023, 0.143995, 0.700649, 0.27658, 0.118569, 0.09747, 0.126673, 0.143634, 0.278983, 0.358482, 0.66175, 0.061533, 0.199373},
			{0.0995, 0.005193, 0.004039, 0.006722, 0.006121, 0.003468, 0.016931, 0.003647, 0.002184, 0.005019, 0.00599, 0.001473, 0.004158, 0.009055, 0.00363, 0.006583, 0.003172, 0.00369, 0.002967, 0.002772, 0.002686}
};
	
	
	//Recode4 model
	protected double[] m_MixtureValuesRecode4 = {0.383048,0.174635,0.0852555,0.0594628,0.0406419,0.0398597,0.0297682,0.0280806,0.0242095,0.019244,0.0178775,0.0174343,0.0168758,0.0139663,0.0123711,0.0123012,0.0111316,0.00698083,0.00365577,0.00320122};
	protected double[][] m_AlphaValuesRecode4 = {
	    { 4.04735, 0.335796, 0.0276254, 0.270793, 0.348046, 0.0863198,
	      0.225768, 0.115656, 0.120617, 0.395388, 0.21713, 0.0692591,
	      0.22952, 0.159096, 0.25475, 0.307135, 0.275135, 0.265742,
	      0.182309, 0.043852, 0.117411 },
	    { 3.5659, 0.273194, 0.0647217, 0.0365618, 0.0448084, 0.266448,
	      0.0796421, 0.039374, 0.522913, 0.0461325, 0.864337, 0.183843,
	      0.0445531, 0.0609869, 0.050193, 0.0545329, 0.0736139, 0.136326,
	      0.533931, 0.057054, 0.132732 },
	    { 0.803228, 0.05911, 0.0127852, 0.0785758, 0.0363021, 0.0289509,
	      0.160842, 0.0281857, 0.0130422, 0.0203385, 0.0267507, 0.00397971,
	      0.0430017, 0.0689402, 0.0127304, 0.026, 0.0605953, 0.0423799,
	      0.0340264, 0.0125806, 0.0341105 },
	    { 9.08397, 2.29698, 0.265451, 0.155901, 0.171394, 0.3867,
	      0.869853, 0.102854, 0.514941, 0.0695931, 0.759599, 0.234631,
	      0.14661, 0.177732, 0.127139, 0.135953, 0.74977, 0.457832,
	      1.05026, 0.0986083, 0.312162 },
	    { 53.6176, 3.26115, 0.745759, 3.18917, 2.67227, 4.15961,
	      4.55029, 3.01435, 2.39119, 0.70697, 4.38827, 1.29158,
	      3.68427, 1.80724, 1.47, 1.6602, 3.67109, 2.30334,
	      2.75308, 2.05938, 3.8384 },
	    { 105.708, 12.1186, 0.472681, 3.87403, 6.5048, 3.46117,
	      2.97363, 1.67069, 8.55045, 9.64583, 13.6136, 3.75321,
	      2.19455, 1.68259, 4.18394, 10.7077, 2.96826, 3.09571,
	      8.1825, 1.72721, 4.32637 },
	    { 76.7013, 10.2085, 1.7407, 2.52751, 1.66695, 4.64628,
	      2.93189, 2.32366, 7.7074, 1.63543, 12.4467, 3.3191,
	      2.36645, 0.915427, 2.74109, 1.49944, 2.30864, 3.94574,
	      7.72884, 0.865209, 3.17628 },
	    { 34.3245, 1.69321, 0.561402, 0.242201, 0.163758, 1.3284,
	      0.426017, 0.217733, 9.19979, 0.365122, 3.00267, 0.470859,
	      0.200578, 0.454379, 0.166355, 0.285794, 0.566137, 1.17851,
	      12.8947, 0.182001, 0.724889 },
	    { 13.549, 0.699016, 0.123918, 0.564493, 0.441809, 0.265585,
	      0.455207, 0.157372, 0.45222, 0.315356, 0.285096, 0.11236,
	      0.700959, 0.384523, 0.341402, 0.19505, 3.04705, 3.86283,
	      0.771455, 0.0747102, 0.298617 },
	    { 27.5029, 2.69506, 0.0984665, 0.999399, 0.702161, 0.18828,
	      14.7923, 0.204418, 0.341519, 0.895261, 0.604879, 0.264405,
	      1.20003, 0.526937, 0.485106, 0.528848, 1.54988, 0.759294,
	      0.220308, 0.11594, 0.330426 },
	    { 40.1814, 3.07208, 0.179581, 1.59068, 1.1802, 1.04447,
	      1.82765, 0.299515, 1.5785, 2.07133, 2.48759, 0.1886,
	      1.17791, 13.8668, 0.831189, 1.07904, 2.36316, 1.88465,
	      2.35323, 0.274462, 0.83075 },
	    { 17.5374, 1.41269, 0.00565739, 3.37209, 5.65739, 0.244139,
	      0.61415, 0.121496, 0.154197, 0.767153, 0.494589, 0.155884,
	      0.280603, 0.263088, 1.48884, 0.353594, 1.00131, 0.538288,
	      0.33596, 0.0728146, 0.20351 },
	    { 86.0527, 3.33374, 1.15604, 1.93175, 1.30643, 4.97546,
	      21.2633, 2.00541, 9.73505, 1.95251, 5.17488, 3.25167,
	      2.10234, 3.3081, 1.36967, 2.43658, 2.02859, 5.17926,
	      7.52289, 1.61701, 4.402 },
	    { 58.4428, 2.53998, 0.64182, 6.22299, 1.7898, 1.14222,
	      9.70006, 1.57777, 0.358056, 2.07334, 1.25521, 0.410731,
	      11.0338, 3.16446, 1.38371, 1.5993, 9.83971, 1.71336,
	      0.41867, 0.428225, 1.14954 },
	    { 157.364, 25.0469, 0.0287555, 14.1297, 23.3767, 0.557453,
	      5.28742, 2.81354, 1.20677, 28.7555, 4.133, 0.151853,
	      6.64437, 2.01447, 14.4893, 12.8818, 10.9112, 3.32116,
	      1.0243, 0.0487539, 0.54082 },
	    { 37.4997, 1.50426, 0.106938, 15.2516, 2.40265, 0.329304,
	      1.80122, 0.505069, 0.674739, 1.40725, 0.728632, 0.237147,
	      5.72791, 0.73936, 0.725283, 0.628562, 2.17878, 1.44031,
	      0.811792, 0.0152516, 0.283625 },
	    { 17.3228, 0.485358, 0.120765, 0.132386, 0.22325, 2.74084,
	      0.290926, 0.536354, 0.448205, 0.240466, 0.81734, 0.216503,
	      0.223577, 0.141958, 0.247041, 0.367824, 0.470015, 0.441281,
	      0.803322, 0.635744, 7.73963 },
	    { 16.9772, 0.422519, 0.162589, 0.063178, 0.232174, 8.8294,
	      0.198806, 0.135336, 0.589954, 0.127259, 1.13028, 0.213526,
	      0.0760594, 0.140012, 0.15649, 0.0125594, 0.33088, 0.331744,
	      0.624835, 0.909754, 2.28988 },
	    { 20.8417, 0.350372, 0.113692, 0.0720512, 0.210516, 1.2529,
	      0.258305, 0.13086, 0.43595, 0.15333, 1.31274, 0.32149,
	      0.0944224, 0.0966389, 0.0963917, 0.356382, 0.206486, 0.167836,
	      0.582403, 13.55, 1.07893 },
	    { 29.9957, 0.161616, 28.1472, 0.0281472, 0.0281472, 0.26712,
	      0.0281472, 0.0281472, 0.220459, 0.0281472, 0.0527309, 0.0281472,
	      0.0440255, 0.0281472, 0.0281472, 0.0281472, 0.152965, 0.0886627,
	      0.456531, 0.0947103, 0.0563106 }
	};
	
	protected double[] m_ZAlpha;
	public long[] m_Duration = new long[3];
	protected double[][] m_LogGammaAlpha;
	protected double[][] m_LogPMatrix;
	
	public EncodingCost(ArrayList<ClusNode> listNodes, ArrayList<RowData> subsets, ClusAttrType[] attrs){
		data = subsets;
		nodes = listNodes;
		attributes = attrs;
		
		// we do it here because we do not need to calculate it again every time
		m_ZAlpha = new double[m_AlphaValues.length];
		
		for(int k=0;k<m_AlphaValues.length;k++){
			// here we use 1 as the start and 20 as the end
			// remember that the first index (0 - zero) contains the sum of the vector
			m_ZAlpha[k] = functionZ(m_AlphaValues[k],1,20);
		}		
	}
	
	public EncodingCost(ArrayList<RowData> subsets, ClusAttrType[] attrs){
		data = subsets;
		attributes = attrs;
		
		// we do it here because we do not need to calculate it again every time
		m_ZAlpha = new double[m_AlphaValues.length];
		
		for(int k=0;k<m_AlphaValues.length;k++){
			// here we use 1 as the start and 20 as the end
			// remember that the first index (0 - zero) contains the sum of the vector
			m_ZAlpha[k] = functionZ(m_AlphaValues[k],1,20);
		}		
	}
	
	public EncodingCost(){
		// we do it here because we do not need to calculate it again every time
		m_ZAlpha = new double[m_AlphaValues.length];
		
		for(int k=0;k<m_AlphaValues.length;k++){
			// here we use 1 as the start and 20 as the end
			// remember that the first index (0 - zero) contains the sum of the vector
			m_ZAlpha[k] = functionZ(m_AlphaValues[k],1,20);
		}
		calculateLogGammaAlphaValues();
	}
	
	public void setNbSequences(int nbSeq){
		m_nbSequences = nbSeq;
	}
	
	public void initializeLogPMatrix(int nbnodes) {
		System.out.print("Initializing matrix with "+nbnodes+" lines and "+attributes.length+" columns\n");
		m_LogPMatrix = new double[nbnodes][attributes.length];
	}
	
	public void calculateLogGammaAlphaValues() {
		m_LogGammaAlpha = new double[m_AlphaValues.length][m_AlphaValues[0].length];
		for (int j=0;j<m_LogGammaAlpha.length;j++) {
			for (int i=0;i<m_LogGammaAlpha[0].length;i++) {
				m_LogGammaAlpha[j][i] = Gamma.logGamma(m_AlphaValues[j][i]);
			}
		}
	}
	
	public void printDuration() {
		System.out.print("Timings: ");
		for (int i=0; i<m_Duration.length; i++){
			System.out.print(m_Duration[i] + " ");
		}
		System.out.println();
	}
	
	public void setClusters(ArrayList<RowData> clusters, ArrayList<Integer> clusterIds) {
		data = clusters;
		m_ClusterIds = clusterIds;
	}
	
	public void setNodeClusters(ArrayList<ClusNode> listNodes,ArrayList<RowData> clusters) {
		data = clusters;
		nodes = listNodes;
	}
	
	public void generateAndSetClusterIDs(ArrayList<ClusNode> listNodes) {
		ArrayList<Integer> clusterIds = new ArrayList<Integer>();
		for(int i=0;i<listNodes.size();i++){
			clusterIds.add(new Integer(listNodes.get(i).getID()-1));
			m_ClusterIds = clusterIds;
		}
	}
	
	public void setAttributes(ClusAttrType[] attrs) {
		attributes = attrs;
	}
	
	
	public int getNbClusters(){
		return data.size();
	}	

	
	public int printAlphaValues(){
		// Iterating over rows
		for(int i=0;i<m_AlphaValues.length;i++){
			// Iterating over columns
			for(int j=0;j<m_AlphaValues[i].length;j++){
				System.out.print(m_AlphaValues[i][j]+"\t");
			}
			System.out.print("\n");
		}
		return 0;
	}
	
	private int printMatrix(int[][] frequency ){
		// Iterating over rows
		for(int i=0;i<frequency.length;i++){
			// Iterating over columns
			for(int j=0;j<frequency[i].length;j++){
				System.out.print(frequency[i][j]+"\t");
			}
			System.out.print("\n");
		}
		return 0;
	}
	
	// This is currently being used to generate the output
	private int printInstanceLabels(int nbSubsets){
		
		for(int i=0;i<nbSubsets;i++){
			int nbRows = data.get(i).getNbRows();
			for(int r=0;r<nbRows;r++){
				String key = data.get(i).getSchema().getKeyAttribute()[0].getString(data.get(i).getTuple(r));
				System.out.print(key+" ");
				
				// Eduardo's code
				//String[] parts = data.get(i).getTuple(r).toString().split(",");
				//System.out.print(parts[0]+" ");
			}
			System.out.print("\n");
		}
		return 0;
	}

	
	// Generate the vector Ncs, which contains the number of occurrence of each amino acid
	// Gaps are not included in this counting
	protected int[][] calculateFrequency(int position,int nbSubsets){
		int[][] frequency = new int[nbSubsets][21];  
		for(int i=0;i<nbSubsets;i++){
			
			int nbRows = data.get(i).getNbRows();
			for(int r=0;r<nbRows;r++){
				
				int index = attributes[position].getNominal(data.get(i).getTuple(r));
				if(index<20){
					frequency[i][index+1]++;
					frequency[i][0]++;
				}
				// else gap
				
				
				/*// Eduardo's code
				String[] parts = data.get(i).getTuple(r).toString().split(",");
				int index2=returnIndexAlphaMatrix(parts[position+1].charAt(0));
				if(index2!=0){
					frequency[i][index2]++;
					frequency[i][0]++;
				}*/
				
			}
		}
		return frequency;
	}
	
	
	// Returns value for the gamma function,
	// which is the exponential of the library function logGamma
	public double functionGamma(double x){
		return Math.exp(Gamma.logGamma(x));
	}
	
	// returns value for the function Z defined in the SCI-PHY encoding cost function
	// it allows to specify the range of the vector that is going to be given as input
	// to the function
	protected double functionZ(double[] vector, int start, int end){
		double valueFunction=1;
		double sum=0;
		for(int i=start;i<=end;i++){
			valueFunction=valueFunction*functionGamma(vector[i]);	
			sum=sum+vector[i];
		}
		valueFunction = valueFunction/functionGamma(sum);
		return valueFunction;
	}

	
	// This function uses the log properties to deal with the large values 
	// outputted by the gamma function 
	protected double functionZAlternative(double[] vector){
		double valueFunction=1;
		double sum=0;
		double logpart1=0;
		// log(prod(X))=log(X1)+log(X2)+.........+log(Xn)	
		for(int i=0;i<vector.length;i++){
			logpart1=logpart1+Gamma.logGamma(vector[i]);	
			sum=sum+vector[i];
		}
		double logpart2 = Gamma.logGamma(sum);
		valueFunction = Math.exp(logpart1-logpart2);
		return valueFunction;
	}
	
	
	// This function does not return the value of the function Z
	// but instead it returns the value of log(function Z)
	protected double functionLogZ(double[] vector){
		double valueFunction;
		double sum=0;
		double logpart1=0;
		// log(prod(X))=log(X1)+log(X2)+.........+log(Xn)	
		for(int i=0;i<vector.length;i++){
			logpart1=logpart1+Gamma.logGamma(vector[i]);	
			sum=sum+vector[i];
		}	
		double logpart2 = Gamma.logGamma(sum);
		valueFunction = logpart1-logpart2;
		return valueFunction;
	}
	
	
	// This function adds a line of the alpha matrix with the vector ncs
	// and returns a vector containing the sum of these vectors
	// Observe that the returned vector has only 20 positions
	protected double[] addAlphaVectorAndFrequencyvector(double[] alphaVector, int[] frequencyVector){	
		double[] addedVector = new double[20];
		for(int i=1;i<21;i++){
			addedVector[i-1]=frequencyVector[i]+alphaVector[i];			
		}
		return addedVector;
	}
	
	
	public double getEncodingCostValueWithNormalizer(){
		int nbAttr = attributes.length;	
		int nbSubsets = data.size();
		double encodingCostValue= m_nbSequences*(Math.log(nbSubsets)/Math.log(2));
		double totalLogProb = 0;
		// iterating over all columns
		for(int c=0;c<nbAttr;c++){
			// we are going to produce a matrix with the frequency of occurrence of each amino acid
			// the columns are going to be the amino acids
			// the lines are going to be the subsets
			int[][] frequency = calculateFrequency(c,nbSubsets);
			//printMatrix(frequency);
			double totalLogProbCol = 0;
			// iterating over all subsets
			for(int s=0;s<nbSubsets;s++){
				double logProb;	
				if (m_LogPMatrix[m_ClusterIds.get(s).intValue()][c] != 0.0) {
					logProb = m_LogPMatrix[m_ClusterIds.get(s).intValue()][c];
				}
				else {
					// calculate probability
					double[] logP = new double[m_MixtureValues.length];
					double normalizer=Double.NEGATIVE_INFINITY;
					
					for(int j=0;j<m_MixtureValues.length;j++){
						double tmp=0;
						for (int i=1;i<m_AlphaValues[0].length;i++) {
							if (frequency[s][i]>0) {								
								tmp += (Gamma.logGamma(m_AlphaValues[j][i]+frequency[s][i]) - m_LogGammaAlpha[j][i]);
							}
						}
						double[] sumVectors = addAlphaVectorAndFrequencyvector(m_AlphaValues[j], frequency[s]);
						double sum=0;
						for (int x=0;x<sumVectors.length;x++) {
							sum+=sumVectors[x];
						}
						double tmp2 = m_LogGammaAlpha[j][0] - Gamma.logGamma(sum);
						
						logP[j] = Math.log(m_MixtureValues[j])+tmp+tmp2;
		
						// Calculating normalizer
						if(logP[j] > normalizer){
							normalizer=logP[j];
						}	
					}
					
					double sumAllPJ=0;
					for(int j=0;j<m_MixtureValues.length;j++){
						double convertingBackValue = Math.exp(logP[j]-normalizer);
						sumAllPJ = sumAllPJ+convertingBackValue;
					}
					
					// log base 2
					logProb = (Math.log(sumAllPJ)/Math.log(2))+normalizer;					

					// log base e
					//logProb = Math.log(sumAllPJ)+normalizer;
					
					m_LogPMatrix[m_ClusterIds.get(s).intValue()][c] = logProb;
				}
				totalLogProbCol += logProb;
			}
			totalLogProb += totalLogProbCol;
		}
		encodingCostValue = encodingCostValue - totalLogProb;
		return encodingCostValue;
	}
	
	
	public double getEncodingCostValue(){
		return getEncodingCostValueWithNormalizer();
	}
	

}
