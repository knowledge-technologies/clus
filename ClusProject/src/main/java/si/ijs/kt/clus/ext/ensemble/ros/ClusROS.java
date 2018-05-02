
package si.ijs.kt.clus.ext.ensemble.ros;

import java.util.ArrayList;
import java.util.Arrays;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleMethod;
import si.ijs.kt.clus.util.ClusRandom;


public class ClusROS {

    /**
     * Method for generating ROS ensemble subspaces
     * 
     * @param ClusSchema
     * @param sizeOfSubspace
     *        Size of subspace that is visible to base learner
     * @param ensembleSize
     *        Number of base learners in the ensemble
     * @return ClusEnsembleROSInfo object with created subspaces (first subspace always covers all targets)
     */
    private static ClusEnsembleROSInfo generateROSEnsembleSubspaces(ClusSchema schema, int sizeOfSubspace, int ensembleSize) {
        int cnt = ensembleSize;
        int[] enabled;
        ArrayList<int[]> subspaces = new ArrayList<int[]>();

        boolean isRandom = sizeOfSubspace <= 0;
        int subspaceCount = sizeOfSubspace;

        // find indices of target and clustering attributes
        int[] targetIDs = new int[schema.getNbTargetAttributes()];
        int[] clusteringIDs = new int[schema.getNbAllAttrUse(AttributeUseType.Clustering)];

        ClusAttrType[] targets = schema.getTargetAttributes();
        ClusAttrType[] clustering = schema.getAllAttrUse(AttributeUseType.Clustering);

        for (int t = 0; t < targetIDs.length; t++)
            targetIDs[t] = targets[t].getIndex();
        for (int t = 0; t < clusteringIDs.length; t++)
            clusteringIDs[t] = clustering[t].getIndex();

        // create subspaces
        while (cnt > 1) {
            enabled = new int[schema.getNbAttributes()];

            // enable all attributes
            Arrays.fill(enabled, 1);

            // disable all targets
            for (int i = 0; i < targets.length; i++)
                enabled[targets[i].getIndex()] = 0;

            // if number of randomly selected targets should also be randomized
            if (isRandom)
                subspaceCount = ClusRandom.nextInt(ClusRandom.RANDOM_ENSEMBLE_ROS_SUBSPACE_SIZE_SELECTION, 1, targets.length); // use a separate randomizer for randomized target subspace size selection

            // randomly select targets
            ClusAttrType[] selected = ClusEnsembleInduce.selectRandomSubspaces(targets, subspaceCount, ClusRandom.RANDOM_ENSEMBLE_ROS, null); // inject ClusRandom.RANDOM_ENSEMBLE_TARGET_SUBSPACING randomizer

            // enable selected targets
            for (int i = 0; i < selected.length; i++)
                enabled[selected[i].getIndex()] = 1;

            // safety check: check if at least one target attr is enabled
            int sum = 0;
            for (int a = 0; a < targetIDs.length; a++)
                sum += enabled[targetIDs[a]];
            if (sum > 0) {
                // check if at least one clustering attr is enabled
                sum = 0;
                for (int a = 0; a < clusteringIDs.length; a++)
                    sum += enabled[clusteringIDs[a]];
                if (sum > 0) {
                    subspaces.add(enabled); // subspace meets the criteria, add it to the list
                    cnt--;
                }
            }
        }

        // create one MT model for all targets and insert it at the beginning of the array
        enabled = new int[schema.getNbAttributes()];
        Arrays.fill(enabled, 1); // enable all attributes
        subspaces.add(0, enabled);

        return new ClusEnsembleROSInfo(schema, subspaces);
    }


    /**
     * Preparation for ROS ensembles
     */
    public static ClusEnsembleROSInfo prepareROSEnsembleInfo(Settings settMain, ClusSchema schema) {
        SettingsEnsemble sett = settMain.getEnsemble();

        EnsembleMethod method = sett.getEnsembleMethod();
        if (method != EnsembleMethod.Bagging
                && method != EnsembleMethod.RForest
                && method != EnsembleMethod.RSubspaces
                && method != EnsembleMethod.ExtraTrees)

            throw new RuntimeException("ROS extension is not implemented for the selected ensemble method!");

        int subspaceSize = sett.calculateNbRandomAttrSelected(schema, 2);

        int[] sizes = sett.getNbBaggingSets().getIntVectorSorted();
        int maxEnsembleSize = sizes[sizes.length - 1];

        if (settMain.getGeneral().getVerbose() > 0)
            System.out.println(String.format("ROS: creating %s target subspaces.", maxEnsembleSize));

        return generateROSEnsembleSubspaces(schema, subspaceSize, maxEnsembleSize);
    }
}
