
package si.ijs.kt.clus.addon.sit;

import java.util.ArrayList;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.MSError;
import si.ijs.kt.clus.error.MisclassificationError;
import si.ijs.kt.clus.error.PearsonCorrelation;
import si.ijs.kt.clus.error.RMSError;
import si.ijs.kt.clus.error.RelativeError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.util.exception.ClusException;


/**
 * Functions to evaluate predictions
 * 
 * @author beau
 *
 */
public final class Evaluator {

    public final static double getPearsonCorrelation(final ArrayList<RowData[]> folds, final int errorIdx) throws ClusException {
        RowData[] temp = folds.get(0);
        ClusSchema schema = temp[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        PearsonCorrelation error = new PearsonCorrelation(parent, num);
        parent.addError(error);
        for (int f = 0; f < folds.size(); f++) {
            RowData[] fold = folds.get(f);

            for (int t = 0; t < fold[0].getNbRows(); t++) {
                DataTuple tuple_real = fold[0].getTuple(t);
                DataTuple tuple_prediction = fold[1].getTuple(t);
                parent.addExample(tuple_real, tuple_prediction);

            }
        }
        if (errorIdx == -1) {
            // ClusLogger.info("main target not in targetset");
            return 0;
        }

        return error.getModelErrorComponent(errorIdx);

    }


    public final static double getPearsonCorrelation(RowData[] data, final int errorIdx) throws ClusException {
        if (errorIdx == -1) { return 0; }
        ClusSchema schema = data[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        PearsonCorrelation error = new PearsonCorrelation(parent, num);
        parent.addError(error);
        for (int t = 0; t < data[0].getNbRows(); t++) {
            DataTuple tuple_real = data[0].getTuple(t);
            DataTuple tuple_prediction = data[1].getTuple(t);
            parent.addExample(tuple_real, tuple_prediction);

        }

        return error.getModelErrorComponent(errorIdx);

    }


    public final static double getMSE(RowData[] data, final int errorIdx) throws ClusException {
        if (errorIdx == -1) { return 0; }
        ClusSchema schema = data[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        MSError error = new MSError(parent, num);
        parent.addError(error);
        for (int t = 0; t < data[0].getNbRows(); t++) {
            DataTuple tuple_real = data[0].getTuple(t);
            DataTuple tuple_prediction = data[1].getTuple(t);
            parent.addExample(tuple_real, tuple_prediction);

        }
        double err = error.getModelErrorComponent(errorIdx);

        return err;

    }


    public final static MSError getMSE(RowData[] data) throws ClusException {

        ClusSchema schema = data[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        MSError error = new MSError(parent, num);
        parent.addError(error);
        for (int t = 0; t < data[0].getNbRows(); t++) {
            DataTuple tuple_real = data[0].getTuple(t);
            DataTuple tuple_prediction = data[1].getTuple(t);
            parent.addExample(tuple_real, tuple_prediction);

        }

        return error;
    }


    public final static double getMSE(final ArrayList<RowData[]> folds, final int errorIdx) throws ClusException {
        RowData[] temp = folds.get(0);
        ClusSchema schema = temp[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        MSError error = new MSError(parent, num);
        parent.addError(error);
        for (int f = 0; f < folds.size(); f++) {
            RowData[] fold = folds.get(f);

            for (int t = 0; t < fold[0].getNbRows(); t++) {
                DataTuple tuple_real = fold[0].getTuple(t);
                DataTuple tuple_prediction = fold[1].getTuple(t);
                parent.addExample(tuple_real, tuple_prediction);

            }
        }
        if (errorIdx == -1) {
            // ClusLogger.info("main target not in targetset");
            return 0;
        }
        return error.getModelErrorComponent(errorIdx);

    }


    public final static double getMisclassificationError(final ArrayList<RowData[]> folds, final int errorIdx) throws ClusException {
        RowData[] temp = folds.get(0);
        ClusSchema schema = temp[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NominalAttrType[] nom = schema.getNominalAttrUse(AttributeUseType.All);
        MisclassificationError error = new MisclassificationError(parent, nom);
        parent.addError(error);
        for (int f = 0; f < folds.size(); f++) {
            RowData[] fold = folds.get(f);

            for (int t = 0; t < fold[0].getNbRows(); t++) {
                DataTuple tuple_real = fold[0].getTuple(t);
                DataTuple tuple_prediction = fold[1].getTuple(t);
                parent.addExample(tuple_real, tuple_prediction);

            }
        }
        if (errorIdx == -1) {
            // ClusLogger.info("main target not in targetset");
            return 0;
        }
        return error.getModelErrorComponent(errorIdx);

    }


    public final static double getMisclassificationError(RowData[] data, final int errorIdx) throws ClusException {
        if (errorIdx == -1) { return 0; }
        ClusSchema schema = data[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NominalAttrType[] nom = schema.getNominalAttrUse(AttributeUseType.All);
        MisclassificationError error = new MisclassificationError(parent, nom);
        parent.addError(error);
        for (int t = 0; t < data[0].getNbRows(); t++) {
            DataTuple tuple_real = data[0].getTuple(t);
            DataTuple tuple_prediction = data[1].getTuple(t);
            parent.addExample(tuple_real, tuple_prediction);

        }
        double err = error.getModelErrorComponent(errorIdx);
        return err;

    }


    public final static double getRelativeError(final ArrayList<RowData[]> folds, final int errorIdx) throws ClusException {
        RowData[] temp = folds.get(0);
        ClusSchema schema = temp[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        RelativeError error = new RelativeError(parent, num);
        parent.addError(error);
        for (int f = 0; f < folds.size(); f++) {
            RowData[] fold = folds.get(f);

            for (int t = 0; t < fold[0].getNbRows(); t++) {
                DataTuple tuple_real = fold[0].getTuple(t);
                DataTuple tuple_prediction = fold[1].getTuple(t);
                parent.addExample(tuple_real, tuple_prediction);

            }
        }
        if (errorIdx == -1) {
            // ClusLogger.info("main target not in targetset");
            return 0;
        }
        return error.getModelErrorComponent(errorIdx);

    }


    public final static double getRelativeError(RowData[] data, final int errorIdx) throws ClusException {
        if (errorIdx == -1) { return 0; }
        ClusSchema schema = data[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        RelativeError error = new RelativeError(parent, num);
        parent.addError(error);
        for (int t = 0; t < data[0].getNbRows(); t++) {
            DataTuple tuple_real = data[0].getTuple(t);
            DataTuple tuple_prediction = data[1].getTuple(t);
            parent.addExample(tuple_real, tuple_prediction);

        }
        double err = error.getModelErrorComponent(errorIdx);
        return err;

    }


    public final static double getRMSE(RowData[] data, final int errorIdx) throws ClusException {
        if (errorIdx == -1) { return 0; }
        ClusSchema schema = data[0].getSchema();
        ClusErrorList parent = new ClusErrorList();
        NumericAttrType[] num = schema.getNumericAttrUse(AttributeUseType.All);
        RMSError error = new RMSError(parent, num);
        parent.addError(error);
        for (int t = 0; t < data[0].getNbRows(); t++) {
            DataTuple tuple_real = data[0].getTuple(t);
            DataTuple tuple_prediction = data[1].getTuple(t);
            parent.addExample(tuple_real, tuple_prediction);

        }
        double err = error.getModelErrorComponent(errorIdx);
        return err;

    }

}
