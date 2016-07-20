package sit.mtLearner;

import java.util.ArrayList;

import sit.TargetSet;
import clus.data.rows.RowData;

/**
 * This class stores a cache of TargetSets, testdata and the predictions for that testdata
 *
 * TODO: better search in stored results. Now O(n).
 *
 * @author beau
 *
 */
public class ResultsCache {

	protected ArrayList<TargetSet> m_TargetSets; //
	protected ArrayList<RowData> m_TestData;
	protected ArrayList<RowData> m_Predictions;


	public ResultsCache(){
		m_TargetSets = new ArrayList<TargetSet>();
		m_TestData = new ArrayList<RowData>();
		m_Predictions = new ArrayList<RowData>();
	}

	public void addResult(TargetSet targetset, RowData[] testpred ){
		/* Running out of heapspace on the large datasets...
		m_TargetSets.add(targetset);
		m_TestData.add(testpred[0]);
		m_Predictions.add(testpred[1]);
		*/
	}


	public RowData[] getResult(TargetSet targets, RowData test){
		//just loop over all stored results
		for(int i=0;i<m_TargetSets.size();i++){
			if(targets.equals(m_TargetSets.get(i))){
				//we found a matching targetset, now check the testdata
				if(test.equals(m_TestData.get(i))){
					//System.out.println("Cache hit for targetset "+targets);
					RowData[] result = {test,m_Predictions.get(i)};
					return result;
				}
			}
		}


		//if no cache-hit, just return null
		return null;
	}

}
