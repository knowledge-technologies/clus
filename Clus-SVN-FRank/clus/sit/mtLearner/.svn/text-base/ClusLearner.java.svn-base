package sit.mtLearner;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import sit.TargetSet;
import clus.Clus;
import clus.algo.ClusInductionAlgorithmType;
import clus.algo.tdidt.ClusDecisionTree;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.main.ClusRun;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.model.ClusModelPredictor;

public class ClusLearner extends MTLearnerImpl {

	protected Clus m_Clus;
	protected ClusSchema m_Schema;

	public void init(RowData data, Settings sett) {
		m_Schema = data.getSchema().cloneSchema();
		RowData mydata = new RowData(data);
		mydata.setSchema(m_Schema);
		super.init(mydata, sett);
		m_Clus = new Clus();
		ClusInductionAlgorithmType clss = new ClusDecisionTree(m_Clus);
		try {
			m_Clus.initialize(mydata, m_Schema, sett, clss);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	protected RowData[] LearnModel(TargetSet targets, RowData train, RowData test) {
		try {
			ClusSchema schema = m_Clus.getSchema();
			schema.clearAttributeStatusClusteringAndTarget();
			Iterator targetIterator = targets.iterator();
			while (targetIterator.hasNext()) {
				ClusAttrType attr = (ClusAttrType)targetIterator.next();
				ClusAttrType clusAttr = schema.getAttrType(attr.getIndex());
				clusAttr.setStatus(ClusAttrType.STATUS_TARGET);
				clusAttr.setClustering(true);
			}


			schema.addIndices(ClusSchema.ROWS);


			for(int i = 0; i<train.getNbRows();i++){
				DataTuple tr = train.getTuple(i);


				tr.setWeight(0);
				for(int j= 0;j <test.getNbRows();j++){
					DataTuple te = test.getTuple(j);
					tr.setWeight(tr.getWeight()+1.0/(1+Math.pow(te.euclDistance(tr),1)));
				}

				tr.setWeight(tr.getWeight()/test.getNbRows());
				//tr.setWeight(1);
				//System.out.println(tr.getWeight());

			}


			ClusRun cr = m_Clus.train(train);
			ClusModel pruned = cr.getModel(ClusModel.PRUNED);
			/*
			PrintWriter p = new PrintWriter(new OutputStreamWriter(System.out));
			pruned.printModel(p);
			p.flush();
			*/
			RowData predictions = ClusModelPredictor.predict(pruned, test);
			RowData[] final_result = {test, predictions};
			return final_result;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public String getName() {
		return "ClusLearner";
	}
}






