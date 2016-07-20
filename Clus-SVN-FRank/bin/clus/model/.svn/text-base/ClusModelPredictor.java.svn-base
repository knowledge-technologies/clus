package clus.model;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusSchema;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;

public class ClusModelPredictor {

	public static RowData predict(ClusModel model, RowData test) throws ClusException {
		ClusSchema schema = test.getSchema();
		schema.attachModel(model);
		RowData predictions = new RowData(schema, test.getNbRows());
		for (int i = 0; i < test.getNbRows(); i++) {
			DataTuple prediction = new DataTuple(schema);
			ClusStatistic stat = model.predictWeighted(test.getTuple(i));
			stat.predictTuple(prediction);
			predictions.setTuple(prediction, i);
		}
		return predictions;
	}

	public static DataTuple predict(ClusModel model, DataTuple test) throws ClusException {
		ClusSchema schema = test.getSchema();
		schema.attachModel(model);
		DataTuple prediction = new DataTuple(schema);
		ClusStatistic stat = model.predictWeighted(test);
		stat.predictTuple(prediction);
		return prediction;
	}


}
