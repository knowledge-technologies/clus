package si.ijs.kt.clus.distance.primitive.relief;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.ext.hierarchical.ClassHierarchy;
import si.ijs.kt.clus.ext.hierarchical.ClassTerm;
import si.ijs.kt.clus.ext.hierarchical.ClassesAttrType;
import si.ijs.kt.clus.ext.hierarchical.ClassesTuple;
import si.ijs.kt.clus.util.jeans.math.MathUtil;

public class HierarchicalMultiLabelDistance {
	/** Hash map for hierarchical attributes: {attributeName: hierarchy, ... } */
    private HashMap<String, ClassHierarchy> m_Hierarchies;
    /** The weights, used in Weighted Euclidean distance, for each hierarchical attribute */
    private HashMap<String, Double> m_HierarUpperBounds;
    
    
    public HierarchicalMultiLabelDistance(){
    	m_Hierarchies = new HashMap<String, ClassHierarchy>();
    	m_HierarUpperBounds = new HashMap<String, Double>();
    }
    
    public void processAttribute(ClassesAttrType attr, RowData data){
    	String attrName = attr.getName();
    	m_Hierarchies.put(attrName, attr.getHier());
    	boolean[][] presentAndFinal = getPresentTerms(attr.getHier(), data);
        double upperDistanceBound = upperBound(attr, presentAndFinal);
        m_HierarUpperBounds.put(attrName, upperDistanceBound);
    }
    
    /**
     * Calculates the weighted Euclidean distance between the two classes in the hierarchy, where the weight of
     * a label is {@code m_HierarWeight^depth}. The distance is at the end normalised by the upper bound
     * for this hierarchy.
     * 
     * @param t1
     *        First tuple
     * @param t2
     *        Second tuple
     * @param attr
     *        The dimension in which the distance between {@code t1} and {@code t2} is computed.
     * @return Normalised weighted Euclidean distance.
     */
    public double calculateDist(DataTuple t1, DataTuple t2, ClassesAttrType attr) {
        String name = attr.getName();
        ClassHierarchy hier = m_Hierarchies.get(name);
        int sidx = hier.getType().getArrayIndex();
        ClassesTuple tp1 = (ClassesTuple) t1.getObjVal(sidx);
        ClassesTuple tp2 = (ClassesTuple) t2.getObjVal(sidx);
        HashSet<Integer> symmetricDifferenceOfLabelSets = new HashSet<Integer>();

        // add labels for t1
        for (int j = 0; j < tp1.getNbClasses(); j++) {
            int labelIndex = tp1.getClass(j).getIndex();
            symmetricDifferenceOfLabelSets.add(labelIndex);
        }
        // remove the intersection labels of t1 and t2, and add labels(t2) - labels(t1)
        for (int j = 0; j < tp2.getNbClasses(); j++) {
            int labelIndex = tp2.getClass(j).getIndex();
            if (symmetricDifferenceOfLabelSets.contains(labelIndex)) {
                symmetricDifferenceOfLabelSets.remove(labelIndex);
            }
            else {
                symmetricDifferenceOfLabelSets.add(labelIndex);
            }
        }
        double dist = weightedEuclideanInt(hier, symmetricDifferenceOfLabelSets);
        return dist / m_HierarUpperBounds.get(name);
    }
    
    /**
     * Computes the depth of each term in the hierarchy, which can be DAG or tree-shaped.
     * 
     * @param attr
     *        The attribute, which the hierarchy belongs to.
     */
    private void computeDepthsOfTerms(ClassHierarchy hier) {
        ArrayList<ClassTerm> toProcess = new ArrayList<ClassTerm>();
        HashMap<ClassTerm, Integer> numberOfProcessedParents = new HashMap<ClassTerm, Integer>();
        for (int i = 0; i < hier.getTotal(); i++) {
            ClassTerm term = hier.getTermAt(i);
            numberOfProcessedParents.put(term, term.getNbParents());
            if (numberOfProcessedParents.get(term) == 0) {
                toProcess.add(term);
            }
        }
        toProcess.add(hier.getRoot());
        while (toProcess.size() > 0) {
            ClassTerm term = toProcess.remove(toProcess.size() - 1);
            // compute the depth
            if (term.getNbParents() == 0) {
                term.setDepth(-1.0); // root
            }
            else {
                double avgDepth = 0.0;
                for (int i = 0; i < term.getNbParents(); i++) {
                    avgDepth += term.getParent(i).getDepth();
                }
                avgDepth /= term.getNbParents();
                term.setDepth(avgDepth + 1.0);
            }
            // see the children
            for (int i = 0; i < term.getNbChildren(); i++) {
                ClassTerm child = (ClassTerm) term.getChild(i);
                int nbParentsToProcess = numberOfProcessedParents.get(child);
                if (nbParentsToProcess == 1) {
                    toProcess.add(child);
                }
                numberOfProcessedParents.put(child, nbParentsToProcess - 1);
            }
        }

    }
    
    /**
     * Finds the indices of the classes in the hierarchy that at least one example belongs to. 
     * @param hier
     * @param data
     * @return answer[class index] = true iff at least one example belongs to class.
     */
    private boolean[][] getPresentTerms(ClassHierarchy hier, RowData data){
    	int hierarchySize = hier.getTotal();
    	boolean[] present = new boolean[hierarchySize];
    	boolean[] finalClasses = new boolean[hierarchySize];
    	int nbFinal = 0;
    	int sidx = hier.getType().getArrayIndex();
    	boolean isTree = hier.isTree();
    	// we can break the loop if all classes are present, since in that case,
    	// the upper bound for d(x1, x2) is achieved in two leaves unless hierarchy is a line
    	// root ---> a1 ---> a2 ---> ... ---> an ... Better safe than sorry ... We will not break the loop.
    	for(int i = 0; i < data.getNbRows(); i++){ //  && nbPresent < hierarchySize
    		ClassesTuple tp = (ClassesTuple) data.getTuple(i).getObjVal(sidx);
    		int nbClasses = tp.getNbClasses();
    		int lastClassIndex = -123;
    		if(isTree){
    			lastClassIndex = tp.getClass(nbClasses - 1).getIndex();
    		} else{
    			lastClassIndex = mostSpecificTermIndex(tp);
    		}
    		if(!finalClasses[lastClassIndex]){
    			finalClasses[lastClassIndex] = true;
    			nbFinal++;
    		}
    		if(present[lastClassIndex]){ // if DAG, the leaf may not be the last in the list
    			continue;  // we have already processed an example that covers all classes of the current
    		}
    		for(int j = 0; j < nbClasses; j++){
    			int ind = tp.getClass(j).getIndex();
    			if (! present[ind]){
    				present[ind] = true;
    				
    			}
    		}
    	}
    	return new boolean[][]{present, finalClasses, new boolean[]{nbFinal > 1}};
    }
    
    /**
     * If the hierarchy {@code hier} is tree-shaped, this efficiently (in linear time) computes the maximal distance {@code d(x, y)} over the vectors
     * {@code x} and {@code y} that correspond to the labels in the hierarchy and are present in the data.<br>
     * If the hierarchy is only DAG, then the time complexity is O(squared number of different labels in data).
     * 
     * @param attr
     *        Hierarchical attribute under consideration
     * @param presentAndFinalClasses An array of 3 arrays:
     * <ul>
     * <li>[..., is the i-th label present in the data, ...] </li>
     * <li>[..., is the i-th label the most specific for some example in the data, ...] </li>
     * <li>[is the number of different most specific labels in the data at least two]</li>
     * </ul>
     * @return {@code max d(x, y)} over the label sets x and y in the dataset.
     */
    private double upperBound(ClassesAttrType attr, boolean[][] presentAndFinalClasses) {
    	boolean atLeastTwoClasses = presentAndFinalClasses[2][0];
    	if(atLeastTwoClasses){
	        ClassHierarchy hier = m_Hierarchies.get(attr.getName());
	        if(hier.isTree()){
	        	return upperBoundTree(hier, presentAndFinalClasses[0], presentAndFinalClasses[1]);
	        } else {
	        	return upperBoundDAG(hier, presentAndFinalClasses[0], presentAndFinalClasses[1]);
	        } 
    	} else{
        	return 1.0; // does not matter what is the normalisation constant, the distance will be always 0
        }

        
    }
    
    /**
     * Finds the upper bound of the weighted Euclidean distance for a given data. Covers the following cases:<br>
     * <ul>
     * <li> any type of tree hierarchy (e.g. line) </li>
     * <li> at least two terms of the hierarchy must be present in the data</li>
     * </ul>
     * @param hier The hierarchy
     * @param presentClasses true at that components that correspond to terms that are present in the data
     * @param finalClasses true at that components that correspond to the deepest term for some example in the data
     * @return
     */
    private double upperBoundTree(ClassHierarchy hier, boolean[] presentClasses, boolean[] finalClasses){
    	computeDepthsOfTerms(hier);
    	double bound = 0.0;
        ClassTerm root = hier.getRoot();
        
        ArrayList<ClassTerm> extremeTerms = findDistantTerms(hier, root, presentClasses, finalClasses);  // must have length two!
        bound = weightedEuclidean(hier, extremeTerms.get(0), extremeTerms.get(1));        
        return bound;
    }
    
    /**
     * We iterate over all pairs of different final classes and return the maximal distance achieved.
     * @param hier
     * @param presentClasses
     * @param finalClasses
     * @return
     */
    private double upperBoundDAG(ClassHierarchy hier, boolean[] presentClasses, boolean[] finalClasses){
    	ArrayList<Integer> finalClassesOnly = new ArrayList<Integer>();
    	for(int termInd = 0; termInd < finalClasses.length; termInd++){
    		if(finalClasses[termInd]){
    			finalClassesOnly.add(termInd);
    		}
    	}
    	double bound = 0.0;
    	for(int i = 0; i < finalClassesOnly.size(); i++){
    		ClassTerm term1 = hier.getTermAt((finalClassesOnly.get(i)));
    		for(int j = i + 1; j < finalClassesOnly.size(); j++){
    			ClassTerm term2 = hier.getTermAt((finalClassesOnly.get(j)));
    			double dist = weightedEuclidean(hier, term1, term2);
    			if(dist > bound + MathUtil.C1E_9){
    				bound = dist;
    			}
    		}
    	}	
    	return bound;
    }
    
    /**
     * Recursively finds the two most distant terms of the hierarchy that represent the final classes in the data, it two such terms exist. 
     * @param root The current root of the hierarchy.
     * @param presentClasses see upperBound
     * @param finalClasses see upperBound
     * @return
     */
    private ArrayList<ClassTerm> findDistantTerms(ClassHierarchy hier, ClassTerm root, boolean[] presentClasses, boolean[] finalClasses){
    	ArrayList<ClassTerm> extremeTerms = new ArrayList<ClassTerm>();
    	int rootInd = root.getIndex();
    	if(rootInd >= 0 && !presentClasses[rootInd]){
    		// the part of the hierarchy rooted here, is not in the data --> empty list
    		return extremeTerms;
    	}
    	ArrayList<ArrayList<ClassTerm>> childrenResults = new ArrayList<ArrayList<ClassTerm>>();
    	int nbCh = root.getNbChildren();
    	for(int i = 0; i < nbCh; i++){
    		ArrayList<ClassTerm> resultCh = findDistantTerms(hier, (ClassTerm) root.getChild(i), presentClasses, finalClasses);
    		if(resultCh.size() > 0){
    			childrenResults.add(resultCh);
    		}
    	}
    	
    	boolean rootIsApproprite = rootInd >= 0 && finalClasses[rootInd];
    	int nbChildrenCandidates = childrenResults.size();
    	// find the indices of <= 2 children that have the deepest first term
		int notAnIndex = -1234;
		// double[] depths = new double[]{-10.0,-10.0};  // initial value does not matter
		int[] inds = new int[]{notAnIndex, notAnIndex}; // something that is not an index (-1 not good choice because of the root term)
		for(int i = 0; i < nbChildrenCandidates; i++){
			double thisDepth = childrenResults.get(i).get(0).getDepth();
			int whereTo = 0;			
			while(whereTo < inds.length){				
				if (inds[whereTo] == notAnIndex || thisDepth > childrenResults.get(inds[whereTo]).get(0).getDepth() + MathUtil.C1E_6){
					break;
				}
				whereTo++;
			}
			// move everything worse than the current one place to right: if whereTo = the last position or even worse, nothing happens
			for(int worseItemInd = inds.length - 2; worseItemInd >= whereTo ; worseItemInd--){
				inds[worseItemInd + 1] = inds[worseItemInd];
			}
			if (whereTo < inds.length){
				inds[whereTo] = i;
				}    		
    	}
    	
    	if (nbChildrenCandidates == 0){
    		// empty + possibly the root
    		if(rootIsApproprite){
    			extremeTerms.add(root);
    		}
    	} else if (nbChildrenCandidates == 1){
    		extremeTerms.add(childrenResults.get(0).get(0)); // always in
    		if(childrenResults.get(0).size() == 1 && rootIsApproprite){  // add root, if root is final
    			extremeTerms.add(root); 
    		} else if(childrenResults.get(0).size() == 2 && rootIsApproprite){ 	// add the other child term or root
    			double distChildRootComb = weightedEuclidean(hier, childrenResults.get(0).get(0), root);
    			double distChildComb = weightedEuclidean(hier, childrenResults.get(0).get(0), childrenResults.get(0).get(1));
    			if(distChildRootComb > distChildComb){
    				extremeTerms.add(root);
    			} else{
    				extremeTerms.add(childrenResults.get(0).get(1));
    			}
    		} else if(childrenResults.get(0).size() == 2){ // add the other child term
    			extremeTerms.add(childrenResults.get(0).get(1));
    		}
    	} else{ // root cannot be an option, either two deepest 1st components or one of the children
    		double maxDistDiffChildren = weightedEuclidean(hier, childrenResults.get(inds[0]).get(0),
    															 childrenResults.get(inds[1]).get(0));
    		double maxInnerDistChild = 0.0;
    		int optimal = -1;
    		for(int i = 0; i < nbChildrenCandidates; i++){
    			if (childrenResults.get(i).size() > 1){
	    			double dist = weightedEuclidean(hier, childrenResults.get(i).get(0), childrenResults.get(i).get(1));
	    			if(dist > maxInnerDistChild + MathUtil.C1E_9){
	    				optimal = i;
	    				maxInnerDistChild = dist;
	    			}
    			}
    		}
    		if(maxDistDiffChildren > maxInnerDistChild){
    			extremeTerms.add(childrenResults.get(inds[0]).get(0));
    			extremeTerms.add(childrenResults.get(inds[1]).get(0));
    		} else{
    			extremeTerms.addAll(childrenResults.get(optimal));
    		}
    	}
    	return extremeTerms;
    }

    private double weightedEuclideanInt(ClassHierarchy hier, HashSet<Integer> symmetricDifferenceOfLabelSets){
    	double dist = 0.0;
        for (int labelIndex : symmetricDifferenceOfLabelSets) {
            dist += hier.getWeight(labelIndex);
        }
        return dist;
    }
    
    private double weightedEuclideanTerm(ClassHierarchy hier, HashSet<ClassTerm> symmetricDifferenceOfLabelSets){
    	double dist = 0.0;
        for (ClassTerm term : symmetricDifferenceOfLabelSets) {
            dist += hier.getWeight(term.getIndex());
        }
        return dist;
    }
    
    private double weightedEuclidean(ClassHierarchy hier, ClassTerm finalTerm1, ClassTerm finalTerm2){
    	return weightedEuclideanTerm(hier, MathUtil.symmetricDifference(finalTerm1.getAllAncestors(true), finalTerm2.getAllAncestors(true)));
    }
    
    /**
     * Finds the most specific label among the ones given in ClassesTuple. 
     * @param tp
     * @return
     */
    private static int mostSpecificTermIndex(ClassesTuple tp){
    	int ind = 0;
    	HashMap<Integer, Integer> termIndices = new HashMap<Integer, Integer>();
    	int nbClasses = tp.getNbClasses();
    	for(int i = 0; i < nbClasses; i++){
    		termIndices.put(tp.getClass(i).getIndex(), i);
    	}
    	boolean continueSearch = true;
    	while (continueSearch){
    		continueSearch = false;
    		ClassTerm term = tp.getClass(ind).getTerm();
    		if (term.atBottomLevel()){
    			return term.getIndex();
    		}
    		for(int i = 0; i < term.getNbChildren(); i++){
    			int j = ((ClassTerm) term.getChild(i)).getIndex();
    			if(termIndices.containsKey(j)){
    				ind = termIndices.get(j);
    				continueSearch = true;
    				break;
    			}
    		}
    	}
    	return tp.getClass(ind).getIndex();
    }
    
    @Deprecated
    /**
     * The weight, used in Weighted Euclidean distance.
     * 
     * @param hier
     * @return The the biggest among the weights of the labels that are smaller than 1.0.
     */
    private double getWeight(ClassHierarchy hier) {
        double[] weights = hier.getWeights();
        double maxW = 0.0;
        for (double w : weights) {
            if (w < 1.0 - MathUtil.C1E_6 && w > maxW) {
                maxW = w;
            }
        }
        return maxW;
    }
    
    @Deprecated
    /**
     * Finds the maximal depth of the subtree with a given root. Works also for DAGs.
     * 
     * @param root
     *        The root of the subtree/subDAG.
     * @return
     */
    private double maxDepthSubtree(ClassTerm root) {
        if (root.getNbChildren() == 0) {
            return root.getDepth();
        }
        else {
            double maxDepth = 0.0;
            for (int child = 0; child < root.getNbChildren(); child++) {
                maxDepth = Math.max(maxDepth, maxDepthSubtree((ClassTerm) root.getChild(child)));
            }
            return maxDepth;
        }
    }
}
