package clus.selection;

import clus.util.ClusRandom;
import clus.util.ClusRandomNonstatic;

public class BaggingSelectionSemiSupervised extends BaggingSelection {
	
	/**
     * Creates a new bagging selection from dataset consisting of labeled and unlabeled examples. Labeled and unlabeled examples are sampled separately
     * to avoid having only unlabeled examples in bagging selection. Dataset is assumed to be sorted so that the labeled examples come first, while unlabeled second. 
     * @param nbrows the total number of instances
     * @param labeledStart starting position of labeled examples (usually 0)
     * @param labeledNo the number of labeled examples to be sampled
     * @param unlabeledstart starting position of unlabeled examples in dataset
     * @param unlabeledNo  the number of unlabeled examples to be sampled 
     */
    public BaggingSelectionSemiSupervised(int nbrows, int labeledNo, int unlabeledNo, ClusRandomNonstatic rnd) {
        super(nbrows);

        m_Counts = new int[nbrows];
        
        //select labeled
        if(rnd == null) {
	        for (int i = 0; i < labeledNo; i++) {
	            m_Counts[ClusRandom.nextInt(ClusRandom.RANDOM_SELECTION, labeledNo)]++;
	        }
	        
	        //select unlabeled
	        for (int i = 0; i < unlabeledNo; i++) {
	            m_Counts[labeledNo + ClusRandom.nextInt(ClusRandom.RANDOM_SELECTION, unlabeledNo)]++;
	        }
        } else {
        	for (int i = 0; i < labeledNo; i++) {
	            m_Counts[rnd.nextInt(ClusRandom.RANDOM_SELECTION, labeledNo)]++;
	        }
	        
	        //select unlabeled
	        for (int i = 0; i < unlabeledNo; i++) {
	            m_Counts[labeledNo + rnd.nextInt(ClusRandom.RANDOM_SELECTION, unlabeledNo)]++;
	        }
        }
        
        for (int i = 0; i < nbrows; i++) {
            if (m_Counts[i] != 0) {
                m_NbSel++;
            }
        }

    }

}
