
package si.ijs.kt.clus.addon.sit.mtLearner;

import java.util.Iterator;

import si.ijs.kt.clus.Clus;
import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.algo.tdidt.ClusDecisionTree;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.Status;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelPredictor;


public class ClusLearner extends MTLearnerImpl {

    protected Clus m_Clus;
    protected ClusSchema m_Schema;


    @Override
    public void init(RowData data, Settings sett) {
        m_Schema = data.getSchema().cloneSchema();
        RowData mydata = new RowData(data);
        mydata.setSchema(m_Schema);
        super.init(mydata, sett);
        m_Clus = new Clus();
        ClusInductionAlgorithmType clss = new ClusDecisionTree(m_Clus);
        try {
            m_Clus.initialize(mydata, m_Schema, sett, clss);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    @Override
    protected RowData[] LearnModel(TargetSet targets, RowData train, RowData test) {
        try {
            ClusSchema schema = m_Clus.getSchema();
            schema.clearAttributeStatusClusteringAndTarget();
            Iterator targetIterator = targets.iterator();
            while (targetIterator.hasNext()) {
                ClusAttrType attr = (ClusAttrType) targetIterator.next();
                ClusAttrType clusAttr = schema.getAttrType(attr.getIndex());
                clusAttr.setStatus(Status.Target);
                clusAttr.setClustering(true);
            }

            schema.addIndices(ClusSchema.ROWS);

            for (int i = 0; i < train.getNbRows(); i++) {
                DataTuple tr = train.getTuple(i);

                tr.setWeight(0);
                for (int j = 0; j < test.getNbRows(); j++) {
                    DataTuple te = test.getTuple(j);
                    tr.setWeight(tr.getWeight() + 1.0 / (1 + Math.pow(te.euclDistance(tr), 1)));
                }

                tr.setWeight(tr.getWeight() / test.getNbRows());
                // tr.setWeight(1);
                // System.out.println(tr.getWeight());

            }

            ClusRun cr = m_Clus.train(train);
            ClusModel pruned = cr.getModel(ClusModel.PRUNED);
            /*
             * PrintWriter p = new PrintWriter(new OutputStreamWriter(System.out));
             * pruned.printModel(p);
             * p.flush();
             */
            RowData predictions = ClusModelPredictor.predict(pruned, test);
            RowData[] final_result = { test, predictions };
            return final_result;
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }


    @Override
    public String getName() {
        return "ClusLearner";
    }
}
