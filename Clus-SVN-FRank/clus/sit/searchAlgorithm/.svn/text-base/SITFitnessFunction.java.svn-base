package sit.searchAlgorithm;

import java.util.ArrayList;

import org.jgap.Chromosome;
import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NumericAttrType;
import clus.error.ClusErrorList;
import clus.error.PearsonCorrelation;
import clus.error.SpearmanRankCorrelation;
import clus.util.ClusRandom;

import sit.Evaluator;
import sit.TargetSet;
import sit.mtLearner.MTLearner;


/**
 * Translates a chromosome to a targetset and evaluates it against the MTLearner
 * @author beau
 *
 */
public class SITFitnessFunction extends FitnessFunction{

	protected ClusAttrType mainTarget;
	protected TargetSet candidates;
	protected MTLearner learner;

	public SITFitnessFunction(ClusAttrType mainTarget,MTLearner learner,TargetSet candidates){
		this.mainTarget = mainTarget;
		this.learner = learner;
		this.candidates = candidates;
	}

	protected double evaluate(IChromosome chromyTheChromoson) {
		TargetSet tset = GeneticSearch.getTargetSet(this.candidates, (Chromosome) chromyTheChromoson);

		int errorIdx = tset.getIndex(mainTarget);
		if(errorIdx==-1){
			//System.out.println("main target not in targetset");
			return 0;
		}


		//predict a few folds
		int nbFolds = 25;
		learner.initXVal(25);
		//learn a model for each fold
		ArrayList<RowData[]> folds = new ArrayList<RowData[]>();
		for(int f = 0;f<nbFolds;f++){
			folds.add(learner.LearnModel(tset,f));
		}
		//return 1.0/tset.size();
		//tset.add(mainTarget);
		double error = 10-Evaluator.getRelativeError(folds,mainTarget.getIndex());

		//System.out.println(tset);
		//System.out.println(10-error);
		return error;
	}

//	private double calcError(ArrayList<RowData[]> folds, int errorIdx){
//
//		RowData[] temp = (RowData[]) folds.get(0);
//		ClusSchema schema = temp[0].getSchema();
//		ClusErrorList parent = new ClusErrorList();
//		NumericAttrType[] num = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_ALL);
//		PearsonCorrelation error = new PearsonCorrelation(parent, num);
//		//SpearmanRankCorrelation error = new SpearmanRankCorrelation(parent,num);
//		parent.addError(error);
//
//		for(int f=0;f<folds.size();f++){
//			RowData[] fold = folds.get(f);
//
//			for(int t=0;t<fold[0].getNbRows();t++){
//				DataTuple tuple_real = fold[0].getTuple(t);
//				DataTuple tuple_prediction = fold[1].getTuple(t);
//				parent.addExample(tuple_real, tuple_prediction);
//
//			}
//		}
//		if(errorIdx==-1){
//			return 0;
//		}
//
//		return error.getModelErrorComponent(errorIdx)+1.0;
//
//		//return error.getModelErrorComponent(0)+1;
//	}
}
