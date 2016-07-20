package sit.mtLearner;


import sit.TargetSet;
import jeans.util.IntervalCollection;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NumericAttrType;
import clus.main.Settings;
import clus.selection.XValMainSelection;
import clus.selection.XValRandomSelection;
import clus.selection.XValSelection;
import clus.util.ClusException;

/***
 *
 *
 * @author beau
 * Returns as prediction the average of all training instances
 *
 *
 *  */


public class AvgLearner extends MTLearnerImpl {

	/* ********************************
	 * Private implementation functions
	 **********************************/

	//the actual LearnModel function
	protected RowData[] LearnModel(TargetSet targets, RowData train, RowData test){
		ClusSchema schema = m_Data.getSchema();
		//schema.getNbNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
		NumericAttrType[] num = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);


		DataTuple result = new DataTuple(schema);//the results have the same schema, non-targets may be null

		for(int i=0;i<train.getNbRows();i++){

			DataTuple tuple = train.getTuple(i);

			for(int j=0;j<num.length;j++){
				double d = num[j].getNumeric(tuple);
				double temp = num[j].getNumeric(result) + d;
				num[j].setNumeric(result, temp);


			}
		}
		for(int j=0;j<num.length;j++){
			double temp = num[j].getNumeric(result);
			num[j].setNumeric(result, temp/train.getNbRows());


		}

		RowData predictions = new RowData(schema,test.getNbRows());
		for(int i=0;i<test.getNbRows();i++){
			predictions.setTuple(result,i);
		}

		RowData[] final_result ={test,predictions};
		return final_result;
	}

	public String getName() {
		return "AvgLearner";
	}

}
