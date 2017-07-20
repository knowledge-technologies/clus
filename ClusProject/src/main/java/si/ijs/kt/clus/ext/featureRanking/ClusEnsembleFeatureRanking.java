package si.ijs.kt.clus.ext.featureRanking;

import java.util.ArrayList;
import java.util.HashMap;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.ensemble.container.NodeDepthPair;
import si.ijs.kt.clus.main.ClusRun;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.selection.OOBSelection;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.statistic.ComponentStatistic;
import si.ijs.kt.clus.util.ClusException;
import si.ijs.kt.clus.util.ClusRandomNonstatic;

public class ClusEnsembleFeatureRanking extends ClusFeatureRanking{
	public ClusEnsembleFeatureRanking(Settings sett){
		super(sett);
	}
	
	
    public HashMap<String, double[][]> calculateRFimportance(ClusModel model, ClusRun cr, OOBSelection oob_sel, ClusRandomNonstatic rnd, ClusStatManager mgr) throws ClusException, InterruptedException {
        HashMap<String, double[][]> partialImportances = new HashMap<String, double[][]>();

        ArrayList<String> attests = getInternalAttributesNames((ClusNode) model);

        RowData tdata = ((RowData) cr.getTrainingSet()).deepCloneData();
        double[][][] oob_errs = calcAverageErrors((RowData) tdata.selectFrom(oob_sel, rnd), model, mgr);
        for (int z = 0; z < attests.size(); z++) {// for the attributes that appear in the tree
            String current_attribute = attests.get(z);
            if (!partialImportances.containsKey(current_attribute)) {
            	double[][] impos = new double[oob_errs.length][];
            	for(int errInd = 0; errInd < oob_errs.length; errInd++){
            		int nbResultsPerError = oob_errs[errInd][0].length;
            		impos[errInd] = new double[nbResultsPerError];
            	} 
                partialImportances.put(current_attribute, impos);
            }
            double[] info = getAttributeInfo(current_attribute);
            double type = info[0];
            double position = info[1];
            double[][] importances = partialImportances.get(current_attribute);
            int permutationSeed = rnd.nextInt(ClusRandomNonstatic.RANDOM_SEED);
            RowData permuted = createRandomizedOOBdata(oob_sel, (RowData) tdata.selectFrom(oob_sel, rnd), (int) type, (int) position, permutationSeed);
            double[][][] permuted_oob_errs = calcAverageErrors((RowData) permuted, model, mgr);
            for (int i = 0; i < oob_errs.length; i++) {
            	for(int j = 0; j < oob_errs[i][0].length; j++){
            		double oobE = oob_errs[i][0][j];
            		double permOobE = permuted_oob_errs[i][0][j];
            		double sign = oob_errs[i][1][0];
            		importances[i][j] += (oobE != 0.0 || permOobE != 0.0) ? sign * (oobE - permOobE) / oobE : 0.0;
            		//importances[i] += (oob_errs[i][0] != 0.0 || permuted_oob_errs[i][0] != 0.0) ? oob_errs[i][1] * (oob_errs[i][0] - permuted_oob_errs[i][0]) / oob_errs[i][0] : 0.0;
            	}
            }
        }

        return partialImportances;
    }
    



    @Deprecated
    public void calculateGENIE3importance(ClusNode node, ClusRun cr) throws InterruptedException {
//        if (m_FimpTableHeader == null) {
//            setGenie3Description();
//        }
        if (!node.atBottomLevel()) {
            String attribute = node.getTest().getType().getName();
            double[] info = getAttributeInfo(attribute);
            info[2] += calculateGENI3value(node, cr);// variable importance
            putAttributeInfo(attribute, info);
            for (int i = 0; i < node.getNbChildren(); i++)
                calculateGENIE3importance((ClusNode) node.getChild(i), cr);
        } // if it is a leaf - do nothing
    }


    @Deprecated
    public double calculateGENI3value(ClusNode node, ClusRun cr) {
        ClusStatistic total = node.getClusteringStat();
        double total_variance = total.getSVarS(cr.getStatManager().getClusteringWeights());
        double summ_variances = 0.0;
        for (int j = 0; j < node.getNbChildren(); j++) {
            ClusNode child = (ClusNode) node.getChild(j);
            summ_variances += child.getClusteringStat().getSVarS(cr.getStatManager().getClusteringWeights());
        }
        return total_variance - summ_variances;
    }
    
    /**
     * An iterative version of {@link calculateGENIE3importance}, which does not update feature importances in place.
     * Rather, it returns the partial importances for all attributes. These are combined later.
     * 
     * @param root
     * @param weights
     * @return
     * @throws InterruptedException
     */
    public HashMap<String, double[][]> calculateGENIE3importanceIteratively(ClusNode root, ClusStatManager statManager) {
//        if (m_FimpTableHeader == null) {
//            setGenie3Description();
//        }
        ArrayList<NodeDepthPair> nodes = getInternalNodesWithDepth(root);
        HashMap<String, double[][]> partialImportances = new HashMap<String, double[][]>();
        int nbTargetComponents = 0;
        boolean perTargetRanking = statManager.getSettings().getEnsemble().shouldPerformRankingPerTarget(); // we set this option to false if !(root.getClusteringStat() instanceof ComponentStatistic) 
        if (perTargetRanking){ 
        	nbTargetComponents += ((ComponentStatistic) root.getClusteringStat()).getNbStatisticComponents();
        }
        for (NodeDepthPair pair : nodes) {
            String attribute = pair.getNode().getTest().getType().getName();
            if (!partialImportances.containsKey(attribute)) {
            	double[][] impos = new double[1][1 + nbTargetComponents];            	
                partialImportances.put(attribute, impos);
            }
            double[][] info = partialImportances.get(attribute);
            double[] gain = calculateGENI3value(pair.getNode(), statManager, nbTargetComponents);
            for(int i = 0; i < gain.length; i++){
            	info[0][i] += gain[i]; 
            }
        }
        return partialImportances;
    }
    
    public double[] calculateGENI3value(ClusNode node, ClusStatManager statManager, int nbTargetComponents) {
    	double[] gain = new double[1 + nbTargetComponents];
    	ClusStatistic total = node.getClusteringStat();
    	// overall
        double total_variance = total.getSVarS(statManager.getClusteringWeights());
        double summ_variances = 0.0;
        for (int j = 0; j < node.getNbChildren(); j++) {
            ClusNode child = (ClusNode) node.getChild(j);
            summ_variances += child.getClusteringStat().getSVarS(statManager.getClusteringWeights());
        }
        gain[0] = total_variance - summ_variances;
        // per target
        for(int i = 1; i < gain.length; i++){
        	double total_variance_comp = ((ComponentStatistic) total).getSVarS(i - 1);
        	double summ_variances_comp = 0.0;
            for (int j = 0; j < node.getNbChildren(); j++) {
                ClusNode child = (ClusNode) node.getChild(j);
                summ_variances += ((ComponentStatistic) child.getClusteringStat()).getSVarS(i - 1);
            }
        	gain[i] = total_variance_comp - summ_variances_comp;
        }
        return gain; 
    }
    
    /**
     * Recursively computes the symbolic importance of attributes, importance(attribute) = importance(attribute,
     * {@code node}), where
     * <p>
     * importance({@code attribute}, {@code node}) = (0.0 : 1.0 ? {@code node} has {@code attribute} as a test) +
     * sum_subnodes {@code weight} * importance({@code attribute}, subnode),
     * <p>
     * for all weights in {@code weights}.
     * 
     * @param node
     * @param weights
     * @param depth
     *        Depth of {@code node}, root's depth is 0
     * @throws InterruptedException
     */
    @Deprecated
    public void calculateSYMBOLICimportance(ClusNode node, double[] weights, double depth) throws InterruptedException {
        if (m_FimpTableHeader == null) {
            setSymbolicFimpHeader(weights);
        }

        if (!node.atBottomLevel()) {
            String attribute = node.getTest().getType().getName();
            double[] info = getAttributeInfo(attribute);
            for (int ranking = 0; ranking < weights.length; ranking++) {
                info[2 + ranking] += Math.pow(weights[ranking], depth);// variable importance
            }
            putAttributeInfo(attribute, info);
            for (int i = 0; i < node.getNbChildren(); i++)
                calculateSYMBOLICimportance((ClusNode) node.getChild(i), weights, depth + 1.0);
        } // if it is a leaf - do nothing
    }


    /**
     * An iterative version of {@link calculateSYMBOLICimportance}, which does not update feature importances in place.
     * Rather, it returns the partial importances for all attributes. These are combined later.
     * 
     * @param root
     * @param weights
     * @return
     * @throws InterruptedException
     */
    public synchronized HashMap<String, double[][]> calculateSYMBOLICimportanceIteratively(ClusNode root, double[] weights) throws InterruptedException {
//        if (m_FimpTableHeader == null) {
//            setSymbolicDescription(weights);
//        }
        ArrayList<NodeDepthPair> nodes = getInternalNodesWithDepth(root);
        // it would suffice to have String --> double[], but we need to allow for
        // double[][] in the Genie3 and RForest methods for feature ranking.
        HashMap<String, double[][]> partialImportances = new HashMap<String, double[][]>();

        for (NodeDepthPair pair : nodes) {
            String attribute = pair.getNode().getTest().getType().getName();
            if (!partialImportances.containsKey(attribute)) {
                partialImportances.put(attribute, new double[weights.length][1]);
            }
            double[][] info = partialImportances.get(attribute);
            for (int ranking = 0; ranking < weights.length; ranking++) {
                info[ranking][0] += Math.pow(weights[ranking], pair.getDepth());
            }
        }

        return partialImportances;

    }
    

    /**
     * Finds all internal nodes in the given tree. Depth first search is used to traverse the tree.
     * We return the nodes and their depths.
     * 
     * @param root
     *        The root of the tree
     * @return List of {@code NodeDepthPair} pairs
     */
    public ArrayList<NodeDepthPair> getInternalNodesWithDepth(ClusNode root) {
        ArrayList<NodeDepthPair> nodes = new ArrayList<NodeDepthPair>();

        ArrayList<NodeDepthPair> stack = new ArrayList<NodeDepthPair>();
        stack.add(new NodeDepthPair(root, 0.0));

        while (stack.size() > 0) {
            NodeDepthPair top = stack.remove(stack.size() - 1);
            ClusNode topNode = top.getNode();
            if (!topNode.atBottomLevel()) {
                nodes.add(top);
            }
            for (int i = 0; i < topNode.getNbChildren(); i++) {
                stack.add(new NodeDepthPair((ClusNode) topNode.getChild(i), top.getDepth() + 1.0));
            }
        }
        return nodes;
    }
    
    public void setRForestFimpHeader(ArrayList<String> names) {
        String rfHeader = "attributeName\t[" + String.join(",", names) + "]";
        setFimpHeader(rfHeader);
    }

    public void setGenie3FimpHeader(ArrayList<String> names) {
        setFimpHeader(fimpTableHeader(names));
    }


    public void setSymbolicFimpHeader(double[] weights) {
        String[] names = new String[weights.length];
        for(int i = 0; i < names.length; i++){
        	names[i] = "w=" + Double.toString(weights[i]);
        }
        String.join("", names);
        setFimpHeader(fimpTableHeader(names)); 
    }
    
    public void setEnsembleRankigDescription(int ensembleType, int rankingType, int nbTrees){
    	String[] description_parts = new String[]{String.format("Ensemble method: %s", getSettings().getEnsemble().getEnsembleTypeName(ensembleType)),
    											  String.format("Ranking method: %s", getSettings().getEnsemble().getRankingTypeName(rankingType)),
    											  String.format("Ensemble size: %d", nbTrees)};
    	setRankingDescription(String.join("\n", description_parts));
    }
    

}
