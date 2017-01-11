package sit.mtLearner;
import sit.TargetSet;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.main.Settings;

public interface MTLearner {

	/**
	 * Initialize the MTLearner
	 * @param data The dataset
	 * @param sett The settings file
	 */
	public void init(RowData data,Settings sett);


	/**
	 * Some model can return predictions for targets that are not included in the targetset.
	 * This function allows to define the main target for which predictions should be included.
	 */
	public void setMainTarget(ClusAttrType target);


	/**
	 * Learns a model for fold foldNr and returns the predictions for the remaining data
	 * @param targets The targets used in the MT model
	 * @param foldNr The fold to learn a model for
	 * @return predictions for the remaining data (data-fold) and the remaining data
	 */
	public RowData[] LearnModel(TargetSet targets,int foldNr);


	/**
	 * Learns a model for the complete trainingset and returns predictions for the testset
	 * @param targets The targets used in the MT model
	 * @return predictions on the testset and the testset
	 * @throws throws an exception if the testset is not set by setTestData();
	 */
	public RowData[] LearnModel(TargetSet targets) throws Exception;

	public void setTestData(RowData test);

	public void initXVal(int nrFolds);

	public String getName();

	/**
	 *
	 * @return the number of folds
	 */
	public int initLOOXVal();



}
