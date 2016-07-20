package clus.error;

import java.io.IOException;

import jeans.util.IntervalCollection;

import clus.data.type.ClusSchema;
import clus.main.ClusOutput;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModelInfo;
import clus.util.ClusException;
import clus.util.ClusFormat;

public class ClusErrorOutput extends ClusOutput {


	public ClusErrorOutput(String fname, ClusSchema schema, Settings sett)
	throws IOException {
		super(fname, schema, sett);
	}

	public ClusErrorOutput(String fname, Settings sett)
	throws IOException {
		super(fname, null,sett);
	}

	public ClusErrorOutput(ClusSchema schema, Settings sett) throws IOException {
		super(schema, sett);
	}

	public void writeHeader() throws IOException {
		m_Writer.println("@relation experiment");
		m_Writer.println("@attribute Dataset {"+m_Sett.getDataFile()+"}");
		m_Writer.println("@attribute Run numeric");
		m_Writer.println("@attribute Fold {01,02,03,04,05,06,07,08,09,10}");//TODO automaticly adjust to nr of folds, 10 is the default
		m_Writer.println("@attribute Algo string");

		m_Writer.println("@attribute Targets string");
		m_Writer.println("@attribute Descriptive string");
		m_Writer.println("@attribute MainTarget numeric");


		//the errors
		m_Writer.println("@attribute RMSE_Default numeric");
		m_Writer.println("@attribute RMSE_Original numeric");
		m_Writer.println("@attribute RMSE_Pruned numeric");

		m_Writer.println("@attribute WRMSE_Default numeric");
		m_Writer.println("@attribute WRMSE_Original numeric");
		m_Writer.println("@attribute WRMSE_Pruned numeric");

		//the modelsizes
		m_Writer.println("@attribute OriginalModelSize numeric");
		m_Writer.println("@attribute PrunedModelSize numeric");

		//support targets
//		m_Writer.println("@attribute SupportTargets string");
		m_Writer.println("@attribute nrSupportTargets numeric");
		m_Writer.println("@attribute inductionTime numeric");



		m_Writer.println("@data");

		m_Writer.flush();
	}


	public void writeOutput(ClusRun cr, boolean detail, boolean outputtrain, double[] clusteringWeights) throws IOException, ClusException {
		//m_Writer.println("@attribute Dataset {"+m_Sett.getAppName()+"}");
		m_Writer.print(m_Sett.getDataFile()+",");
		//m_Writer.println("@attribute Run numeric");
		m_Writer.print(m_Sett.getRandomSeed()+",");
		//m_Writer.println("@attribute Fold {1,2,3,4,5,6,7,8,9,10}");//TODO automaticly adjust to nr of folds, 10 is the default
		m_Writer.print(cr.getIndexString()+",");
		//m_Writer.println("@attribute Algo string");
		IntervalCollection targets = new IntervalCollection(m_Sett.getTarget());
		int nrTargets = targets.getMinIndex() - targets.getMaxIndex() + 1;
		//m_Writer.println("@attribute PruningMethod string");
		m_Writer.print(m_Sett.getPruningMethodName()+",");
		//m_Writer.println("@attribute Targets string");
		m_Writer.print(m_Sett.getTarget()+",");
		//m_Writer.println("@attribute Descriptive string")
		m_Writer.print(m_Sett.getDescriptive()+",");
		//m_Writer.println("@attribute MainTarget numeric");
		m_Writer.print(m_Sett.getMainTarget());
		//m_Writer.println("@attribute PrunedModelSize numeric");
		if (m_Sett.getMainTarget().equals("Default")) {
			// part below caused number parsing error during cross-validation
			return;
		}
		int mt =new Integer(m_Sett.getMainTarget());
		//IntervalCollection targets = new IntervalCollection(m_Sett.getTarget());
		int mt_idx = mt-targets.getMinIndex();
/*
		if(mt == targets.getMinIndex()){
			mt_idx = 0;
		}else{
			mt_idx = 1;
		}
	*/
		ClusErrorList tr_err = cr.getTestError();
		if (tr_err != null) {
			for (int i = 1; i < 3; i++) {//the models
				for (int j = 0; j < 3; j++) {//the errors
					ClusModelInfo inf = cr.getModelInfo(j);
					ClusErrorList parent = inf.getError(ClusModelInfo.TEST_ERR);

					ClusError err2 = parent.getError(i);
					//System.out.print(inf.getName()+": ");
					//System.out.println(err2.getName());
					m_Writer.print(","+err2.getModelErrorComponent(mt_idx));
				}
			}
			for (int j = 1; j < 3; j++) {
				ClusModelInfo inf = cr.getModelInfo(j);
				m_Writer.print(","+inf.getModelSize());
			}
		}
		//m_Writer.println("@attribute SupportTargets string");

		int supportTargetCounter = -1;
	//	m_Writer.print(",[");
		for(int j = 0;j<clusteringWeights.length;j++){
			if(clusteringWeights[j] == 1){
		//	m_Writer.print((j+1)+"_");
			supportTargetCounter++;
			}
		}
	//	m_Writer.print("]");
		//m_Writer.println("@attribute nrSupportTargets numeric");
		m_Writer.print(","+supportTargetCounter);
		//induction time
		m_Writer.print(","+ClusFormat.FOUR_AFTER_DOT.format((double)cr.getInductionTime()/1000.0));

		m_Writer.println();
		m_Writer.flush();
	}
}
