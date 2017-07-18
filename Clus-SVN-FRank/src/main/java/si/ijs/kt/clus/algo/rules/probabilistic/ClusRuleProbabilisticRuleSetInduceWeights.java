package si.ijs.kt.clus.algo.rules.probabilistic;

/**
 * Helper class for SLS algorithm weights. 
 * @author Martin Breskvar
 */
public class ClusRuleProbabilisticRuleSetInduceWeights
{
	/** default weights */
	public double WEIGHT_OBJECTIVE_SIZE = 1;
	public double WEIGHT_OBJECTIVE_CARDINALITY = 1;
	public double WEIGHT_OBJECTIVE_OVERLAP = 1;
	public double WEIGHT_OBJECTIVE_ACCURACY = 1;
	public double WEIGHT_OBJECTIVE_CORRECT_COVER = 1;
	public double WEIGHT_OBJECTIVE_INCORRECT_COVER = 1;

	/** turn on/off objectives */
	public boolean objectiveSizeEnabled = true;
	public boolean objectiveCardinalityEnabled = false;
	public boolean objectiveOverlapEnabled = false;
	public boolean objectiveAccuracyEnabled = true;
	public boolean objectiveCorrectCoverEnabled = false;
	public boolean objectiveIncorrectCoverEnabled = false;
	
	public boolean isEnabledObjectiveSize() { return objectiveSizeEnabled; }
	public boolean isEnabledObjectiveCardinality() { return objectiveCardinalityEnabled; }
	public boolean isEnabledObjectiveOverlap() { return objectiveOverlapEnabled; }
	public boolean isEnabledObjectiveAccuracy() { return objectiveAccuracyEnabled; }
	public boolean isEnabledObjectiveCorrectCover() { return objectiveCorrectCoverEnabled; }
	public boolean isEnabledObjectiveIncorrectCover() { return objectiveIncorrectCoverEnabled; }
	
	/** By Default Accuracy and Size are taken into account with weights 1.0 */
	public ClusRuleProbabilisticRuleSetInduceWeights()
	{
	}
	
	/** Return input weights */
	public ClusRuleProbabilisticRuleSetInduceWeights(double[] weights, boolean[] enabledObjectives) 
	{
		if (weights == null || weights.length != 6) throw new RuntimeException("Wrong weight vector size!");
		if (enabledObjectives == null || enabledObjectives.length != 6) throw new RuntimeException("Wrong enabled objectives vector size!");
		
		WEIGHT_OBJECTIVE_SIZE = weights[0];
		WEIGHT_OBJECTIVE_CARDINALITY = weights[1];
		WEIGHT_OBJECTIVE_OVERLAP = weights[2];
		WEIGHT_OBJECTIVE_ACCURACY = weights[3];
		WEIGHT_OBJECTIVE_CORRECT_COVER = weights[4];
		WEIGHT_OBJECTIVE_INCORRECT_COVER = weights[5];
		
		objectiveSizeEnabled = enabledObjectives[0];
		objectiveCardinalityEnabled = enabledObjectives[1];
		objectiveOverlapEnabled = enabledObjectives[2];
		objectiveAccuracyEnabled = enabledObjectives[3];
		objectiveCorrectCoverEnabled = enabledObjectives[4];
		objectiveIncorrectCoverEnabled = enabledObjectives[5];
	}
	
	public String getWeightsString() {
		String s =
				"\n\tACCURACY: " + WEIGHT_OBJECTIVE_ACCURACY
			 + "\n\tSIZE: " + WEIGHT_OBJECTIVE_SIZE
			 + "\n\tCARDINALITY: " + WEIGHT_OBJECTIVE_CARDINALITY
			 + "\n\tOVERLAP: " + WEIGHT_OBJECTIVE_OVERLAP
			 + "\n\tCORRECT COVER: " + WEIGHT_OBJECTIVE_CORRECT_COVER
			 + "\n\tINCORRECT COVER: " + WEIGHT_OBJECTIVE_CORRECT_COVER + "\n";
		 
		 
		return s;
		
	}
	public double[] getWeights() {
		
		return new double[] {
				WEIGHT_OBJECTIVE_SIZE,
				WEIGHT_OBJECTIVE_CARDINALITY,
				WEIGHT_OBJECTIVE_OVERLAP,
				WEIGHT_OBJECTIVE_ACCURACY,
				WEIGHT_OBJECTIVE_CORRECT_COVER,
				WEIGHT_OBJECTIVE_INCORRECT_COVER
				};
	}
	public void setWeights(double[] weights) {
		if (weights == null || weights.length != 6) throw new RuntimeException("Wrong weight vector size!");
		
		WEIGHT_OBJECTIVE_SIZE = weights[0];
		WEIGHT_OBJECTIVE_CARDINALITY = weights[1];
		WEIGHT_OBJECTIVE_OVERLAP = weights[2];
		WEIGHT_OBJECTIVE_ACCURACY = weights[3];
		WEIGHT_OBJECTIVE_CORRECT_COVER = weights[4];
		WEIGHT_OBJECTIVE_INCORRECT_COVER = weights[5];		
	}
	
	
	
}