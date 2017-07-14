package clus.ext.semisupervised;

import static clus.Clus.calcStdDevsForTheSet;
import clus.algo.ClusInductionAlgorithm;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NominalAttrType;
import clus.data.type.NumericAttrType;
import clus.error.Accuracy;
import clus.error.ClusError;
import clus.error.ClusErrorList;
import clus.error.PearsonCorrelation;
import clus.error.RMSError;
import clus.ext.ensembles.ClusForest;
import clus.ext.hierarchical.HierErrorMeasures;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.main.Settings;
import clus.model.ClusModel;
import clus.selection.RandomSelection;
import clus.selection.XValMainSelection;
import clus.selection.XValRandomSelection;
import clus.selection.XValSelection;
import clus.statistic.ClusStatistic;
import clus.util.ClusException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

/**
*
* @author jurical
*/
public abstract class ClusSemiSupervisedInduce extends ClusInductionAlgorithm {

   RowData m_UnlabeledData;
   RowData m_TrainingSet;
   double m_PercentageLabeled;
   ClusModel m_Model;

   public ClusSemiSupervisedInduce(ClusSchema schema, Settings sett) throws ClusException, IOException {
       super(schema, sett);
   }

   public ClusSemiSupervisedInduce(ClusInductionAlgorithm other) {
       super(other);
   }

   public void partitionData(ClusRun cr) throws IOException, ClusException {

       m_UnlabeledData = cr.getUnlabeledSet();
       m_TrainingSet = new RowData(cr.getStatManager().getSchema());

       
       //
       // Set training set and unlabeled set
       //
       if (m_UnlabeledData == null) {
    	   
    	   //if unlabeled data are given in a training set
    	   
    	   m_UnlabeledData = new RowData(cr.getStatManager().getSchema());
    	   RowData tempTrainingSet = (RowData) cr.getTrainingSet();
    	   
    	   if(tempTrainingSet.getNbUnlabeled() > 0) { //if unlabeled examples are in the training set
    		   for (int i = 0; i < tempTrainingSet.getNbRows(); i++) {
    			   if(tempTrainingSet.getTuple(i).isUnlabeled())
    				   m_UnlabeledData.add(tempTrainingSet.getTuple(i).deepCloneTuple());
    			   else
    				   m_TrainingSet.add(tempTrainingSet.getTuple(i).deepCloneTuple());
    		   }
    	   }
    	   else 
    	   {
	           System.out.println("UnlabeledData not set. Unlabeled examples will be selected from training set (Percentage labeled = " + m_PercentageLabeled + ")");
	
	           RandomSelection randomSelection = new RandomSelection(tempTrainingSet.getNbRows(), m_PercentageLabeled, cr.getStatManager().getSettings().getRandomSeed());

	           //add selected instances from training set to unlabeled set
	           //we have to remove from training set instances which will be in the unlabeled set, 
	           //because if we just set their weights to 0, they will anyway be selected in bagging process
	           for (int i = 0; i < tempTrainingSet.getNbRows(); i++) {
	               if (!randomSelection.isSelected(i)) {
	                   m_UnlabeledData.add(tempTrainingSet.getTuple(i).deepCloneTuple());
	                   //trainingSet.getTuple(i).setWeight(0);
	               } else {
	                   m_TrainingSet.add(tempTrainingSet.getTuple(i).deepCloneTuple());
	               }
	           }
    	   }
    	   
           //replace training set with the new one
           cr.setTrainingSet(m_TrainingSet);
           
       } else {
           m_TrainingSet = (RowData) cr.getTrainingSet();
       }
       
       setTestSet(cr);
   }

   public void setTestSet(ClusRun cr) throws IOException, ClusException {

       RowData testSet = cr.getTestSet();

       if (testSet == null) {
           System.out.println("Testing data not set. Semi-supervised learning will be evaluated on unlabeled data.");
           testSet = new RowData(cr.getStatManager().getSchema());

           for (int i = 0; i < m_UnlabeledData.getNbRows(); i++) {
               testSet.add(m_UnlabeledData.getTuple(i).deepCloneTuple());
           }
           cr.setTestSet(testSet.getIterator());
       }
   }
   
   /**
    * Calculate error for a given test set
    *
    * By Tomaž: This doesn't seem right, because MODE depends also on
    * clustering attributes. Here's a possible improvement.
    *
    * @param model
    * @param testSet
    * @param maxInstanceIndex only the first maxInstanceIndex will be taken
    * into account
    * @return
    */
   public ClusError calculateError(ClusModel model, RowData testSet, int maxInstanceIndex) {
       ClusError error = null;
       ClusErrorList ErrorList = new ClusErrorList();

//       if (ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION) {
//           error = new RMSError(ErrorList, this.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
//       }
//
//       if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY) {
//           error = new Accuracy(ErrorList, this.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
//       }
       if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
           error = new HierErrorMeasures(ErrorList, m_StatManager.getHier(), m_StatManager.getSettings().getRecallValues().getDoubleVector(), getSettings().getCompatibility(), Settings.HIERMEASURE_POOLED_AUPRC, m_StatManager.getSettings().isWriteCurves());
       } else {
           //Jurica - this does not work for HMC, because HMc is not num or nom
           NumericAttrType[] num = m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
           NominalAttrType[] nom = m_Schema.getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
           if (nom.length != 0) {
               error = new Accuracy(ErrorList, nom);
           } else if (num.length != 0) {
               error = new RMSError(ErrorList, num);
           }
       }

       ErrorList.addError(error);
       DataTuple tuple;

       for (int t = 0; t < maxInstanceIndex; t++) {

           tuple = testSet.getTuple(t);

           ClusStatistic pred = model.predictWeighted(tuple);
           ErrorList.addExample(tuple, pred);
       }

       return error;
   }

   public ClusError calculateError(RowData testSet) {
       return calculateError(m_Model, testSet, testSet.getNbRows());
   }

   /**
    * TODO: THIS SHOULD BE DELETED AND RE-IMPLEMENTED TO WORK DIRECTLY IN CLUS FOREST
    * CLASS Return Out of Bag Error Estimate of the ClusForest model. All types
    * of errors which are usually calculated for the current modeling task will
    * be calculated and returned in ClusErrorList.
    *
    * @param all_data data on which the * the raining set
    * @param maxInstanceIndex the OOB Error will be calculated for instances in
    * all_data with indices between 0 and maxInstanceIndex (exclusive). This is
    * used in self-training to calculate OOBError only on the originally
    * labeled examples.
    * @return OOB Error
    */
   public ClusError getOOBError(RowData all_data, int maxInstanceIndex) {

       ClusError error = null;
       ClusErrorList OOBErrorList = new ClusErrorList();

       if (!Settings.shouldEstimateOOB()) {
           return new Accuracy(OOBErrorList, this.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
       }

       //Pooled AUPRC (more is better)
       if (ClusStatManager.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
           error = new HierErrorMeasures(OOBErrorList, m_StatManager.getHier(), m_StatManager.getSettings().getRecallValues().getDoubleVector(), getSettings().getCompatibility(), Settings.HIERMEASURE_POOLED_AUPRC, m_StatManager.getSettings().isWriteCurves());
       }

       //RMSE (less is better)
       if (ClusStatManager.getMode() == ClusStatManager.MODE_REGRESSION) {
           error = new RMSError(OOBErrorList, this.getSchema().getNumericAttrUse(ClusAttrType.ATTR_USE_TARGET));
       }

       if (ClusStatManager.getMode() == ClusStatManager.MODE_CLASSIFY) {
           error = new Accuracy(OOBErrorList, this.getSchema().getNominalAttrUse(ClusAttrType.ATTR_USE_TARGET));
       }

       OOBErrorList.addError(error);
       DataTuple tuple;

       for (int t = 0; t < maxInstanceIndex; t++) {

           tuple = all_data.getTuple(t);

           //if example was not OOB, we simply skip it in calculation
           if (((ClusForest) this.m_Model).containsOOBForTuple(tuple)) {
               ClusStatistic pred = ((ClusForest) this.m_Model).predictWeightedOOB(tuple);
               OOBErrorList.addExample(tuple, pred);
           }
       }

       return error;
   }
}
