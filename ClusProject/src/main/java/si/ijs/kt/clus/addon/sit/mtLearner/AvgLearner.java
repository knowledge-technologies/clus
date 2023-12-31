
package si.ijs.kt.clus.addon.sit.mtLearner;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;


/***
 *
 *
 * @author beau
 *         Returns as prediction the average of all training instances
 *
 *
 */

public class AvgLearner extends MTLearnerImpl {

    /*
     * ********************************
     * Private implementation functions
     **********************************/

    // the actual LearnModel function
    @Override
    protected RowData[] LearnModel(TargetSet targets, RowData train, RowData test) {
        ClusSchema schema = m_Data.getSchema();
        // schema.getNbNumericAttrUse(AttributeUseType.Target);
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.Target);

        DataTuple result = new DataTuple(schema);// the results have the same schema, non-targets may be null

        for (int i = 0; i < train.getNbRows(); i++) {

            DataTuple tuple = train.getTuple(i);

            for (int j = 0; j < num.length; j++) {
                double d = num[j].getNumeric(tuple);
                double temp = num[j].getNumeric(result) + d;
                num[j].setNumeric(result, temp);

            }
        }
        for (int j = 0; j < num.length; j++) {
            double temp = num[j].getNumeric(result);
            num[j].setNumeric(result, temp / train.getNbRows());

        }

        RowData predictions = new RowData(schema, test.getNbRows());
        for (int i = 0; i < test.getNbRows(); i++) {
            predictions.setTuple(result, i);
        }

        RowData[] final_result = { test, predictions };
        return final_result;
    }


    @Override
    public String getName() {
        return "AvgLearner";
    }

}
