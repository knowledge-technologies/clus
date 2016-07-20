package sit.searchAlgorithm;




import java.util.ArrayList;
import sit.Evaluator;
import sit.TargetSet;
import sit.mtLearner.MTLearner;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.main.Settings;

/**
 * Abstract implementation of the SearchAlgo interface.
 * Provides some basic functions needed by most implementations.
 * @author beau
 *
 */
public abstract class SearchAlgorithmImpl implements SearchAlgorithm{

	protected MTLearner learner;
	protected Settings m_Sett;

	public void setMTLearner(MTLearner learner) {
		this.learner = learner;
	}

	public void setSettings(Settings s) {
		this.m_Sett = s;
	}


	protected double eval(TargetSet tset, ClusAttrType mainTarget){
		//create a few folds
		//int nbFolds = 23;
		int nbFolds = learner.initLOOXVal();

		//learn a model for each fold
		ArrayList<RowData[]> folds = new ArrayList<RowData[]>();
		for(int f = 0;f<nbFolds;f++){
			folds.add(learner.LearnModel(tset,f));
		}

			String error = m_Sett.getError();
			if(error.equals("MSE")){
				//System.out.println("using mse");
				return 1-Evaluator.getMSE(folds,mainTarget.getArrayIndex());
			}
			if(error.equals("MisclassificationError")){
				return 1-Evaluator.getMisclassificationError(folds,mainTarget.getArrayIndex());
			}


		return Evaluator.getPearsonCorrelation(folds,mainTarget.getArrayIndex());
	}


}
