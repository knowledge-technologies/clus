package sit.searchAlgorithm;

import java.util.Iterator;

import sit.TargetSet;
import sit.mtLearner.MTLearner;
import clus.data.type.ClusAttrType;

public class GreedySIT extends SearchAlgorithmImpl{

	protected MTLearner learner;

	public String getName() {
		return "GreedySIT";
	}

	public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) {


		TargetSet best_set = new TargetSet(mainTarget);
		double best_err = eval(best_set,mainTarget);
		System.out.println("Best set = "+best_set+" with correlation "+best_err);





		boolean improvement = true;
		while(improvement){

			improvement = false;
			double tmp_best_err = best_err;
			TargetSet tmp_best_set = best_set;
			System.out.println("Trying to improve this set:"+best_set);
			Iterator i = candidates.iterator();
			while(i.hasNext()){
				TargetSet test = (TargetSet) best_set.clone();
				test.add(i.next());
				System.out.println("Eval:"+test);
				double err = eval(test,mainTarget);
				if(err>tmp_best_err){
					tmp_best_err = err;
					tmp_best_set = test;
					improvement = true;
					System.out.println("-->improvement: "+err);
				}
			}

			best_err = tmp_best_err;
			best_set = tmp_best_set;
			System.out.println("Best set found:"+best_set+" correlation "+best_err);
		}



		return best_set;
	}



}
