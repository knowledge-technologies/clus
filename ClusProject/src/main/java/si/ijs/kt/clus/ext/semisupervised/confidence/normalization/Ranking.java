package si.ijs.kt.clus.ext.semisupervised.confidence.normalization;

import java.util.Arrays;
import java.util.Map;

import si.ijs.kt.clus.ext.semisupervised.utils.IndiceValuePair;

/**
 * On the basis of the given per-target confidence scores, provides ranking
 * based confidence scores: per-target scores are ranked, independently for
 * each target
 * @author jurical
 */
public class Ranking implements Normalization {

    int nbTargetAttributes;
    boolean lessIsBetter = true;

    public Ranking(int nbTargetAttributes) {
        this.nbTargetAttributes = nbTargetAttributes;
    }

    @Override
    public void normalize(Map<Integer, double[]> perTargetScores) {

        IndiceValuePair[][] indValPairs = new IndiceValuePair[nbTargetAttributes][perTargetScores.size()];
        double[] instancePerTargetScores;
        int nbExamples = 0;

        //initialize array for sorting
        for (Integer key : perTargetScores.keySet()) {
            instancePerTargetScores = perTargetScores.get(key);

            for (int i = 0; i < instancePerTargetScores.length; i++) {
                indValPairs[i][nbExamples] = new IndiceValuePair(key, instancePerTargetScores[i]);
            }

            nbExamples++;
        }

        //sort
        for (int i = 0; i < indValPairs.length; i++) {
            //ClusLogger.info("Orig:" + Helper.arrayToString(indValPairs[i], ","));
            Arrays.sort(indValPairs[i]);
            //ClusLogger.info("Sorted:" + Helper.arrayToString(indValPairs[i], ","));
        }

        double maxRank = indValPairs[0].length - 1; //we get the highest rank, i.e., the number of instances

        for (int i = 0; i < indValPairs[0].length; i++) { //examples           
            for (int j = 0; j < indValPairs.length; j++) { //targets
                //store ranks, divide with maxRank to ensure that the scores are in [0,1]
                //subtract from 1 to ensure that high rank (i.e., 1,2,3,...) corresponds to high reliability
                // FIXME: if examples have the same value, rank should be divided among them, now it's not
                if(lessIsBetter)
                    perTargetScores.get(indValPairs[j][i].getIndice())[j] = 1 - i / maxRank;
                else
                    perTargetScores.get(indValPairs[j][i].getIndice())[j] = i / maxRank;
            }
            
        }

        //free memory
        indValPairs = null;
    }

    @Override
    public void setIsLessBetter(boolean lessIsBetter) {
        this.lessIsBetter = lessIsBetter;
    }
}