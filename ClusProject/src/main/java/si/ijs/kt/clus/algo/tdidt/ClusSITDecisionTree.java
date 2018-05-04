
package si.ijs.kt.clus.algo.tdidt;

import java.util.Random;

import si.ijs.kt.clus.algo.ClusInductionAlgorithmType;
import si.ijs.kt.clus.data.ClusData;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.error.Accuracy;
import si.ijs.kt.clus.error.PearsonCorrelation;
import si.ijs.kt.clus.error.common.ClusError;
import si.ijs.kt.clus.error.common.ClusErrorList;
import si.ijs.kt.clus.error.common.ClusErrorOutput;
import si.ijs.kt.clus.error.hmlc.HierClassWiseAccuracy;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.ClusSummary;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsSIT;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.model.ClusModelInfo;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.selection.XValSelection;
import si.ijs.kt.clus.util.ResourceInfo;
import si.ijs.kt.clus.util.jeans.util.IntervalCollection;


public class ClusSITDecisionTree extends ClusDecisionTree {

    protected ClusInductionAlgorithmType m_Class;


    public ClusSITDecisionTree(ClusInductionAlgorithmType clss) {
        super(clss.getClus());
        m_Class = clss;
    }


    @Override
    public void printInfo() {
        System.out.println("---------SIT---------");
        System.out.println("Heuristic: " + getStatManager().getHeuristicName());
    }


    public ClusErrorList createTuneError(ClusStatManager mgr) {
        ClusErrorList parent = new ClusErrorList();
        if (mgr.getMode() == ClusStatManager.MODE_HIERARCHICAL) {
            parent.addError(new HierClassWiseAccuracy(parent, mgr.getHier()));
            return parent;
        }
        NumericAttrType[] num = mgr.getSchema().getNumericAttrUse(AttributeUseType.Target);
        NominalAttrType[] nom = mgr.getSchema().getNominalAttrUse(AttributeUseType.Target);
        if (nom.length != 0) {
            parent.addError(new Accuracy(parent, nom));
        }
        if (num.length != 0) {
            parent.addError(new PearsonCorrelation(parent, num));
            // parent.addError(new RMSError(parent, num));
        }
        return parent;
    }


    private final void showFold(int i, XValMainSelection sel) {
        if (i != 0)
            System.out.print(" ");
        System.out.print(String.valueOf(i + 1));

        System.out.flush();
    }


    public ClusError doParamXVal(ClusData trset, ClusData pruneset) throws Exception {
        int prevVerb = getSettings().getGeneral().enableVerbose(0);
        ClusStatManager mgr = getStatManager();
        ClusSummary summ = new ClusSummary();
        summ.setTestError(createTuneError(mgr));
        // Next does not always use same partition!
        // Random random = ClusRandom.getRandom(ClusRandom.RANDOM_PARAM_TUNE);
        Random random = new Random(0);

        int nbfolds = 10;// Integer.parseInt(getSettings().getTuneFolds());
        XValMainSelection sel = new XValRandomSelection(trset.getNbRows(), nbfolds, random);

        for (int i = 0; i < nbfolds; i++) {
            showFold(i, sel);
            XValSelection msel = new XValSelection(sel, i);
            ClusRun cr = m_Clus.partitionDataBasic(trset, msel, pruneset, summ, i + 1);
            ClusModel pruned = m_Class.induceSingle(cr);
            cr.addModelInfo(ClusModel.PRUNED).setModel(pruned);
            m_Clus.calcError(cr, summ, null);

        }
        ClusModelInfo mi = summ.getModelInfo(ClusModel.PRUNED);
        getSettings().getGeneral().enableVerbose(prevVerb);

        ClusErrorList err_list = mi.getTestError();
        ClusError err = err_list.getFirstError();
        System.out.println();

        return err;
    }


    /**
     * @param main_target
     *        the only weight not set to 0
     *        Set weights to 0, except for the main_target
     */
    private void resetWeights(int main_target) {
        resetWeights();
        getStatManager().getClusteringWeights().m_Weights[main_target] = 1;
    }


    /**
     * Set weights to 0
     */
    private void resetWeights() {
        ClusStatManager mgr = getStatManager();
        double[] weights = mgr.getClusteringWeights().m_Weights;

        for (int i = 0; i < weights.length; i++) {
            weights[i] = 0;
        }

    }


    private double addBestSupportTasks(double[] weights, int emc, int[] support_range, ClusData trset, ClusData pruneset) throws Exception {

        ClusStatManager mgr = getStatManager();
        // variables for holding the current best found weights/err
        double[] best_weights = weights.clone();

        ClusError err = doParamXVal(trset, pruneset);
        double best_err = err.getModelErrorComponent(emc);
        System.out.print("Current best Target error: " + best_err + " for targets ");
        for (int j = 0; j < weights.length; j++) {
            if (best_weights[j] == 1) {
                System.out.print((j + 1) + " ");
            }
        }
        System.out.println();

        for (int i = support_range[0]; i <= support_range[1]; i++) {
            mgr.getClusteringWeights().m_Weights = weights.clone();

            if (mgr.getClusteringWeights().m_Weights[i] != 1) {
                mgr.getClusteringWeights().m_Weights[i] = 1;

                // Testing new weight vector
                System.out.print("Testing targets: ");
                for (int j = 0; j < weights.length; j++) {
                    if (mgr.getClusteringWeights().m_Weights[j] == 1) {
                        System.out.print((j + 1) + " ");
                    }
                }
                System.out.println();
                err = doParamXVal(trset, pruneset);
                System.out.println("Correlation: " + err.getModelErrorComponent(emc));
                if (err.getModelErrorComponent(emc) > best_err) {
                    best_err = err.getModelErrorComponent(emc);
                    best_weights = mgr.getClusteringWeights().m_Weights.clone();
                }
                System.out.println();

            }
        }

        System.out.println("Best error: " + best_err);
        System.out.print("Best targets:");
        for (int j = 0; j < weights.length; j++) {
            if (best_weights[j] == 1) {
                System.out.print((j + 1) + " ");
            }
        }
        System.out.println();
        // set the weights to the best weights found
        mgr.getClusteringWeights().m_Weights = best_weights;

        return best_err;
    }


    private double substractBestSupportTasks(double[] weights, int emc, int[] support_range, ClusData trset, ClusData pruneset) throws Exception {

        ClusStatManager mgr = getStatManager();
        // variables for holding the current best found weights/err
        double[] best_weights = weights.clone();

        ClusError err = doParamXVal(trset, pruneset);
        double best_err = err.getModelErrorComponent(emc);
        System.out.print("Current best Target error: " + best_err + " for targets ");
        for (int j = 0; j < weights.length; j++) {
            if (best_weights[j] == 1) {
                System.out.print((j + 1) + " ");
            }
        }
        System.out.println();

        for (int i = support_range[0]; i <= support_range[1]; i++) {
            mgr.getClusteringWeights().m_Weights = weights.clone();

            if (mgr.getClusteringWeights().m_Weights[i] != 0) {
                mgr.getClusteringWeights().m_Weights[i] = 0;

                // Testing new weight vector
                System.out.print("Testing targets: ");
                for (int j = 0; j < weights.length; j++) {
                    if (mgr.getClusteringWeights().m_Weights[j] == 1) {
                        System.out.print((j + 1) + " ");
                    }
                }
                System.out.println();
                err = doParamXVal(trset, pruneset);
                System.out.println("Correlation: " + err.getModelErrorComponent(emc));
                if (err.getModelErrorComponent(emc) > best_err) {
                    best_err = err.getModelErrorComponent(emc);
                    best_weights = mgr.getClusteringWeights().m_Weights.clone();
                }
                System.out.println();

            }
        }

        System.out.println("Best error: " + best_err);
        System.out.print("Best targets:");
        for (int j = 0; j < weights.length; j++) {
            if (best_weights[j] == 1) {
                System.out.print((j + 1) + " ");
            }
        }

        // set the weights to the best weights found
        mgr.getClusteringWeights().m_Weights = best_weights;

        return best_err;
    }


    public void findBestSupportTasks(ClusData trset, ClusData pruneset) throws Exception {
        ClusStatManager mgr = getStatManager();
        Settings settings = mgr.getSettings();
        int main_target = new Integer(settings.getSIT().getMainTarget()) - 1;//// we try to optimize for this target. Index! 0 =
                                                                    //// target 1

        // only non-interrupted ranges work for now, eg NOT "1-10,12-15"
        IntervalCollection targets = new IntervalCollection(getSettings().getAttribute().getTarget());

        int support_range[] = { targets.getMinIndex() - 1, targets.getMaxIndex() - 1 };// try finding optimal support
                                                                                       // attribute in this range,
                                                                                       // should be equal to "targets"
        int emc = main_target - support_range[0];// error model component of the main target
        boolean recursive = settings.getSIT().getRecursive();

        // Optimizing for target 1:
        // set all weights to 0, except the main_target
        resetWeights(main_target);
        double[] weights = mgr.getClusteringWeights().m_Weights;
        double best_err = addBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);

        if (recursive) {
            System.out.println("\n---recursive sit---");
            weights = mgr.getClusteringWeights().m_Weights;
            double new_err = addBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
            while (new_err > best_err) {
                best_err = new_err;
                weights = mgr.getClusteringWeights().m_Weights;
                new_err = addBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
            }
        }

        System.out.println();

    }


    public void twoSidedSit(ClusData trset, ClusData pruneset) throws Exception {
        ClusStatManager mgr = getStatManager();
        SettingsSIT settings = mgr.getSettings().getSIT();
        int main_target = new Integer(settings.getMainTarget()) - 1;//// we try to optimize for this target. Index! 0 =
                                                                    //// target 1

        // only non-interrupted ranges work for now, eg NOT "1-10,12-15"
        IntervalCollection targets = new IntervalCollection(getSettings().getAttribute().getTarget());

        int support_range[] = { targets.getMinIndex() - 1, targets.getMaxIndex() - 1 };// try finding optimal support
                                                                                       // attribute in this range,
                                                                                       // should be equal to "targets"
        int emc = main_target - support_range[0];// error model component of the main target
        boolean recursive = settings.getRecursive();

        // estimate ST-error
        // set all weights to 0, except the main_target
        resetWeights(main_target);
        double[] weights = mgr.getClusteringWeights().m_Weights;

        ClusError err = doParamXVal(trset, pruneset);
        double ST_err = err.getModelErrorComponent(emc);
        double best_err = ST_err;

        System.out.println("Estimated ST error: " + ST_err);

        // estimate MT-error
        // set all candidate targets to 1
        for (int i = support_range[0]; i <= support_range[1]; i++) {
            mgr.getClusteringWeights().m_Weights[i] = 1;
        }
        err = doParamXVal(trset, pruneset);
        double MT_err = err.getModelErrorComponent(emc);
        System.out.println("Estimated MT error: " + MT_err);

        if (MT_err > ST_err) {
            best_err = MT_err;
            System.out.println("\n---recursive sub sit---");
            weights = mgr.getClusteringWeights().m_Weights;
            double new_err = substractBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
            while (new_err > best_err) {
                best_err = new_err;
                weights = mgr.getClusteringWeights().m_Weights;
                new_err = substractBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
            }
        }
        else {
            System.out.println("\n---recursive add sit---");
            resetWeights(main_target);
            weights = mgr.getClusteringWeights().m_Weights;
            double new_err = addBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
            while (new_err > best_err) {
                best_err = new_err;
                weights = mgr.getClusteringWeights().m_Weights;
                new_err = addBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
            }
        }

        System.out.println();

    }


    public void superSit(ClusData trset, ClusData pruneset) throws Exception {
        ClusStatManager mgr = getStatManager();
        Settings settings = mgr.getSettings();
        int main_target = new Integer(settings.getSIT().getMainTarget()) - 1;//// we try to optimize for this target. Index! 0 =
                                                                    //// target 1

        // only non-interrupted ranges work for now, eg NOT "1-10,12-15"
        IntervalCollection targets = new IntervalCollection(settings.getAttribute().getTarget());

        int support_range[] = { targets.getMinIndex() - 1, targets.getMaxIndex() - 1 };// try finding optimal support
                                                                                       // attribute in this range,
                                                                                       // should be equal to "targets"
        int emc = main_target - support_range[0];// error model component of the main target
        boolean recursive = settings.getSIT().getRecursive();

        // estimate ST-error
        // set all weights to 0, except the main_target
        resetWeights(main_target);
        double[] weights = mgr.getClusteringWeights().m_Weights;

        ClusError err = doParamXVal(trset, pruneset);
        double ST_err = err.getModelErrorComponent(emc);
        double best_err = ST_err;

        System.out.println("Estimated ST error: " + ST_err);

        double[] starting_weights = weights.clone();
        // estimate starting set
        // for all candidate targets to 1
        for (int i = support_range[0]; i <= support_range[1]; i++) {
            mgr.getClusteringWeights().m_Weights[i] = 1;
            err = doParamXVal(trset, pruneset);
            double MT_err = err.getModelErrorComponent(emc);
            if (MT_err > ST_err) {
                starting_weights[i] = 1;
                System.out.println("Adding target " + (i + 1) + " to starting set");
            }
        }
        mgr.getClusteringWeights().m_Weights = starting_weights;

        System.out.print("Starting from targets ");
        for (int j = 0; j < weights.length; j++) {
            if (starting_weights[j] == 1) {
                System.out.print((j + 1) + " ");
            }
        }

        System.out.println("\n---recursive sit---");

        weights = starting_weights;
        double new_err = addBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
        new_err = substractBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
        while (new_err > best_err) {
            best_err = new_err;

            weights = mgr.getClusteringWeights().m_Weights;
            new_err = addBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
            new_err = substractBestSupportTasks(weights.clone(), emc, support_range, trset, pruneset);
        }
        System.out.println();
    }


    public void sweepSit(ClusData trset, ClusData pruneset) throws Exception {
        ClusStatManager mgr = getStatManager();
        Settings settings = mgr.getSettings();
        int main_target = new Integer(settings.getSIT().getMainTarget()) - 1;//// we try to optimize for this target. Index! 0 =
                                                                    //// target 1

        // only non-interrupted ranges work for now, eg NOT "1-10,12-15"
        IntervalCollection targets = new IntervalCollection(settings.getAttribute().getTarget());

        int support_range[] = { targets.getMinIndex() - 1, targets.getMaxIndex() - 1 };// try finding optimal support
                                                                                       // attribute in this range,
                                                                                       // should be equal to "targets"
        int emc = main_target - support_range[0];// error model component of the main target
        boolean recursive = settings.getSIT().getRecursive();

        // estimate ST-error
        // set all weights to 0, except the main_target
        resetWeights(main_target);
        double[] weights = mgr.getClusteringWeights().m_Weights;

        ClusError err = doParamXVal(trset, pruneset);
        double best_err = err.getModelErrorComponent(emc);

        double[] selected_weights = weights.clone();
        boolean improved = true;

        System.out.print("Set before sweeping: ");
        for (int j = 0; j < weights.length; j++) {
            if (selected_weights[j] == 1) {
                System.out.print((j + 1) + " ");
            }
        }
        System.out.println();
        while (improved) {
            improved = false;
            // add sweep
            for (int i = support_range[0]; i <= support_range[1]; i++) {
                if (mgr.getClusteringWeights().m_Weights[i] == 0) {
                    mgr.getClusteringWeights().m_Weights[i] = 1;
                    err = doParamXVal(trset, pruneset);
                    double MT_err = err.getModelErrorComponent(emc);
                    if (MT_err > best_err) {
                        best_err = MT_err;
                        selected_weights[i] = 1;

                        System.out.print("Adding target " + (i + 1) + " to selected set: ");
                        for (int j = 0; j < weights.length; j++) {
                            if (selected_weights[j] == 1) {
                                System.out.print((j + 1) + " ");
                            }
                        }
                        System.out.println();

                        mgr.getClusteringWeights().m_Weights = selected_weights.clone();
                        improved = true;
                    }
                    else {
                        mgr.getClusteringWeights().m_Weights = selected_weights.clone();
                    }
                }
                else {
                    mgr.getClusteringWeights().m_Weights[i] = 0;
                    err = doParamXVal(trset, pruneset);
                    double MT_err = err.getModelErrorComponent(emc);
                    if (MT_err > best_err) {
                        best_err = MT_err;
                        selected_weights[i] = 0;

                        System.out.print("Removing target " + (i + 1) + " from selected set: ");
                        for (int j = 0; j < weights.length; j++) {
                            if (selected_weights[j] == 1) {
                                System.out.print((j + 1) + " ");
                            }
                        }
                        System.out.println();

                        mgr.getClusteringWeights().m_Weights = selected_weights.clone();
                        improved = true;
                    }
                    else {
                        mgr.getClusteringWeights().m_Weights = selected_weights.clone();
                    }
                }
            }
        }

        System.out.print("Final targets ");
        for (int j = 0; j < weights.length; j++) {
            if (selected_weights[j] == 1) {
                System.out.print((j + 1) + " ");
            }
        }

        System.out.println();

    }


    public void exhaustiveSearch(ClusRun cr) throws Exception {
        ClusStatManager mgr = getStatManager();
        Settings settings = mgr.getSettings();
        // int main_target = new Integer(settings.getMainTarget())-1;////we try to optimize for this target. Index! 0 =
        // target 1

        // only non-interrupted ranges work for now, eg NOT "1-10,12-15"
        IntervalCollection targets = new IntervalCollection(settings.getAttribute().getTarget());

        int support_range[] = { targets.getMinIndex() - 1, targets.getMaxIndex() - 1 };// try finding optimal support
                                                                                       // attribute in this range,
                                                                                       // should be equal to "targets"
                                                                                       // int emc = main_target - support_range[0];//error model component of the main target
        boolean recursive = settings.getSIT().getRecursive();

        resetWeights();
        double[] weights = mgr.getClusteringWeights().m_Weights;// .clone();

        ClusErrorOutput errOutput = new ClusErrorOutput(settings.getGeneric().getAppName() + ".err", settings);

        // generate all subsets for n targets
        int n = support_range[1] - support_range[0] + 1;
        for (int B = 0; B < (1 << n); ++B) {
            for (int b = 0; b < n; ++b) {
                if ((B & (1 << b)) > 0) {
                    weights[b + support_range[0]] = 1;
                }
                else {
                    weights[b + support_range[0]] = 0;
                }
            }
            System.out.println();
            for (int j = 0; j < weights.length; j++) {
                if (weights[j] == 1) {
                    System.out.print((j + 1) + " ");
                }
            }

            errOutput.writeOutput(cr, false, false, weights);
        }

        ClusError err = doParamXVal(cr.getTrainingSet(), cr.getPruneSet());
        double best_err = err.getModelErrorComponent(0);
    }


    @Override
    public void induceAll(ClusRun cr) throws Exception {
        ClusStatManager mgr = getStatManager();
        Settings settings = mgr.getSettings();

        long start_time = ResourceInfo.getTime();

        long done_time = ResourceInfo.getTime();

        // Induce final model
        System.out.println("----------Building final model------------");
        m_Class.induceAll(cr);
        // overwrite InductionTime -> otherwise the induction time is only the time of inducing the final model
        cr.setInductionTime(done_time - start_time);
    }
}
