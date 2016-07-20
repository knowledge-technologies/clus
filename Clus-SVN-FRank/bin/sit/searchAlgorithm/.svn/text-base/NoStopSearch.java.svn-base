package sit.searchAlgorithm;

import java.util.Iterator;

import sit.TargetSet;
import clus.data.type.ClusAttrType;
import clus.main.Settings;

public class NoStopSearch extends SearchAlgorithmImpl{

	public String getName() {
		return "NoStop";
	}

	public TargetSet search(ClusAttrType mainTarget, TargetSet candidates) {


		TargetSet best_set = new TargetSet(mainTarget);
		double best_err = eval(best_set,mainTarget);

		System.out.println("Best set = "+best_set+" MSE "+(best_err-1)*-1);


		TargetSet overal_best_set = new TargetSet(mainTarget);
		double overal_best_err = eval(best_set,mainTarget);




		boolean c = true;
		while(c){

			double tmp_best_err = Double.MAX_VALUE*-1;
			TargetSet tmp_best_set = best_set;
			System.out.println("Trying to improve this set:"+best_set);
			Iterator i = candidates.iterator();
			while(i.hasNext()){
				TargetSet test = (TargetSet) best_set.clone();

				ClusAttrType cat = (ClusAttrType) i.next();
				if(!test.contains(cat)){
					test.add(cat);

					double err = eval(test,mainTarget);
					System.out.println("Eval:"+test +"->"+(err-1)*-1);




					if(err>tmp_best_err){// && test.size() != best_set.size()){
						tmp_best_err = err;
						tmp_best_set = test;
						System.out.println("-->improvement ");
					}
				}
			}

			best_err = tmp_best_err;
			best_set = tmp_best_set;

			if(best_err>overal_best_err){
				overal_best_err = best_err;
				overal_best_set = best_set;
				System.out.println("-->OVERAL improvement");
			}else{
				System.out.println("-->NO overal improvement...");
			}
			if(tmp_best_set.size()==candidates.size()){
				c = false;
			}
			System.out.println("Best set found:"+best_set+" correlation "+best_err);
		}

		System.out.println("Overal best set found:"+overal_best_set+" correlation "+overal_best_err);

		return overal_best_set;
	}



}
