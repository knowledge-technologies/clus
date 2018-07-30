
package si.ijs.kt.clus.ext.ensemble.ros;

import java.util.HashMap;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.ensemble.ClusEnsembleInduce;
import si.ijs.kt.clus.main.settings.section.SettingsEnsemble.EnsembleROSAlgorithmType;
import si.ijs.kt.clus.util.ClusRandom;


public class ClusROSHelpers {

    // /**
    // * Method for generating ROS ensemble subspaces
    // *
    // * @param ClusSchema
    // * @param sizeOfSubspace
    // * Size of subspace that is visible to base learner
    // * @param ensembleSize
    // * Number of base learners in the ensemble
    // * @return ClusEnsembleROSInfo object with created subspaces (first subspace always covers all targets)
    // */
    // private static ClusEnsembleROSInfo generateROSEnsembleSubspaces(ClusSchema schema, int sizeOfSubspace, int
    // ensembleSize, SettingsEnsemble sett) {
    // int cnt = ensembleSize;
    // int[] enabled;
    // ArrayList<int[]> subspaces = new ArrayList<int[]>();
    //
    // boolean isRandom = sizeOfSubspace <= 0;
    // int subspaceCount = sizeOfSubspace;
    //
    // // find indices of target and clustering attributes
    // int[] targetIDs = new int[schema.getNbTargetAttributes()];
    // int[] clusteringIDs = new int[schema.getNbAllAttrUse(AttributeUseType.Clustering)];
    //
    // ClusAttrType[] targets = schema.getTargetAttributes();
    // ClusAttrType[] clustering = schema.getAllAttrUse(AttributeUseType.Clustering);
    //
    // for (int t = 0; t < targetIDs.length; t++)
    // targetIDs[t] = targets[t].getIndex();
    // for (int t = 0; t < clusteringIDs.length; t++)
    // clusteringIDs[t] = clustering[t].getIndex();
    //
    // // create subspaces
    // while (cnt > 1) {
    // enabled = new int[schema.getNbAttributes()];
    //
    // // enable all attributes
    // Arrays.fill(enabled, 1);
    //
    // // disable all targets
    // for (int i = 0; i < targets.length; i++)
    // enabled[targets[i].getIndex()] = 0;
    //
    // // if number of randomly selected targets should also be randomized
    // if (isRandom) {
    // /* use a separate randomizer for randomized target subspace size selection */
    // subspaceCount = ClusRandom.nextInt(ClusRandom.RANDOM_ENSEMBLE_ROS_SUBSPACE_SIZE_SELECTION, 1, targets.length);
    //
    // }
    //
    // /*
    // * Randomly select targets.
    // * inject ClusRandom.RANDOM_ENSEMBLE_TARGET_SUBSPACING randomizer
    // */
    // ClusAttrType[] selected = ClusEnsembleInduce.selectRandomSubspaces(targets, subspaceCount,
    // ClusRandom.RANDOM_ENSEMBLE_ROS, null);
    //
    // // enable selected targets
    // for (int i = 0; i < selected.length; i++)
    // enabled[selected[i].getIndex()] = 1;
    //
    // // safety check: check if at least one target attr is enabled
    // int sum = 0;
    // for (int a = 0; a < targetIDs.length; a++)
    // sum += enabled[targetIDs[a]];
    // if (sum > 0) {
    // // check if at least one clustering attr is enabled
    // sum = 0;
    // for (int a = 0; a < clusteringIDs.length; a++)
    // sum += enabled[clusteringIDs[a]];
    // if (sum > 0) {
    // subspaces.add(enabled); // subspace meets the criteria, add it to the list
    // cnt--;
    // }
    // }
    // }
    //
    // // create one MT model for all targets and insert it at the beginning of the array
    // enabled = new int[schema.getNbAttributes()];
    // Arrays.fill(enabled, 1); // enable all attributes
    // subspaces.add(0, enabled);
    //
    // return new ClusEnsembleROSInfo(schema, subspaces, sett.getEnsembleROSAlgorithmType(),
    // sett.getEnsembleROSVotingType(), -1);
    // }
    //
    //
    // /**
    // * Preparation for ROS ensembles
    // */
    // public static ClusEnsembleROSInfo prepareROSEnsembleInfoFixed(Settings settMain, ClusSchema schema) {
    // SettingsEnsemble sett = settMain.getEnsemble();
    //
    // int subspaceSize = sett.calculateNbRandomAttrSelected(schema, RandomAttributeTypeSelection.Clustering);
    //
    // int[] sizes = sett.getNbBaggingSets().getIntVectorSorted();
    // int maxEnsembleSize = sizes[sizes.length - 1];
    //
    // if (settMain.getGeneral().getVerbose() > 0)
    // ClusLogger.info(String.format("ROS: creating %s target subspaces.", maxEnsembleSize));
    //
    // return generateROSEnsembleSubspaces(schema, subspaceSize, maxEnsembleSize, sett);
    // }

    public static HashMap<Integer, Integer> generateSubspace(ClusSchema schema, int sizeOfSubspace, EnsembleROSAlgorithmType rosAT, int bagNumber) {
        if (rosAT.equals(EnsembleROSAlgorithmType.FixedSubspaces) && bagNumber == 0) {
            // first bag contains all attributes
            return populateMap(schema.getClusteringAttributes(), schema);
        }
        else {
            // randomly select subspace
            return generateSubspace(schema, sizeOfSubspace);
        }
    }


    private static HashMap<Integer, Integer> generateSubspace(ClusSchema schema, int sizeOfSubspace) {
        boolean isRandom = sizeOfSubspace <= 0;
        int subspaceCount = sizeOfSubspace;

        ClusAttrType[] clustering = schema.getClusteringAttributes();

        if (isRandom) {
            // if number of randomly selected targets should also be randomized
            // use a separate randomizer for randomized target subspace size selection
            subspaceCount = ClusRandom.nextInt(ClusRandom.RANDOM_ENSEMBLE_ROS_SUBSPACE_SIZE_SELECTION, 1, clustering.length);
        }

        // Randomly select targets with ClusRandom.RANDOM_ENSEMBLE_TARGET_SUBSPACING randomizer
        ClusAttrType[] selected = ClusEnsembleInduce.selectRandomSubspaces(clustering, subspaceCount, ClusRandom.RANDOM_ENSEMBLE_ROS, null);

        return populateMap(selected, schema);
    }


    private static HashMap<Integer, Integer> populateMap(ClusAttrType[] selected, ClusSchema schema) {

        HashMap<Integer, Integer> selectedClusteringAttributes = new HashMap<>();
        ClusAttrType[] all = schema.getClusteringAttributes();

        for (int i = 0; i < all.length; i++) {
            ClusAttrType a = all[i];
            for (ClusAttrType t : selected) {
                if (a.getIndex() == t.getIndex()) {
                    selectedClusteringAttributes.put(i, t.getIndex());
                }
            }
        }

        return selectedClusteringAttributes;
    }
}
