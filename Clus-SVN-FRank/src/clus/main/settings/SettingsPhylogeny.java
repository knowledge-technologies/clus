
package clus.main.settings;

import clus.util.jeans.io.ini.INIFileDouble;
import clus.util.jeans.io.ini.INIFileNominal;
import clus.util.jeans.io.ini.INIFileSection;
import clus.util.jeans.io.ini.INIFileString;


public class SettingsPhylogeny implements ISettings {

    /***********************************************************************
     * Section: Phylogeny *
     ***********************************************************************/

    private final String[] PHYLOGENY_DISTANCE_MEASURE = { "PDist", "Edit", "JC", "Kimura", "AminoKimura" };
    public final static int PHYLOGENY_DISTANCE_MEASURE_PDIST = 0;
    public final static int PHYLOGENY_DISTANCE_MEASURE_EDIT = 1;
    public final static int PHYLOGENY_DISTANCE_MEASURE_JC = 2;
    public final static int PHYLOGENY_DISTANCE_MEASURE_KIMURA = 3;
    public final static int PHYLOGENY_DISTANCE_MEASURE_AMINOKIMURA = 4;

    private final String[] PHYLOGENY_SEQUENCE = { "DNA", "Protein", "Any" };
    public final static int PHYLOGENY_SEQUENCE_DNA = 0;
    public final static int PHYLOGENY_SEQUENCE_AMINO = 1;
    public final static int PHYLOGENY_SEQUENCE_ANY = 2;

    private final String[] PHYLOGENY_CRITERION = { "MinTotBranchLength", "MaxAvgPWDistance", "MaxMinPWDistance" };
    public final static int PHYLOGENY_CRITERION_BRANCHLENGTHS = 0;
    public final static int PHYLOGENY_CRITERION_MAXAVGPWDIST = 1;
    public final static int PHYLOGENY_CRITERION_MAXMINPWDIST = 2;

    private INIFileSection m_SectionPhylogeny;
    private INIFileNominal m_PhylogenyDM;
    private INIFileNominal m_PhylogenyCriterion;
    private INIFileNominal m_PhylogenySequence;
    private INIFileString m_PhylogenyDistanceMatrix;
    private INIFileDouble m_PhylogenyEntropyVsRootStop;
    private INIFileDouble m_PhylogenyDistancesVsRootStop;
    private INIFileDouble m_PhylogenyEntropyVsParentStop;
    private INIFileDouble m_PhylogenyDistancesVsParentStop;


    public int getPhylogenySequence() {
        return m_PhylogenySequence.getValue();
    }


    public int getPhylogenyDM() {
        return m_PhylogenyDM.getValue();
    }


    public int getPhylogenyCriterion() {
        return m_PhylogenyCriterion.getValue();
    }


    public String getPhylogenyDistanceMatrix() {
        return m_PhylogenyDistanceMatrix.getValue();
    }


    public boolean isSectionPhylogenyEnabled() {
        return m_SectionPhylogeny.isEnabled();
    }


    public void setSectionPhylogenyEnabled(boolean enable) {
        m_SectionPhylogeny.setEnabled(enable);
    }


    public double getPhylogenyEntropyVsRootStop() {
        return m_PhylogenyEntropyVsRootStop.getValue();
    }


    public double getPhylogenyDistancesVsRootStop() {
        return m_PhylogenyDistancesVsRootStop.getValue();
    }


    public double getPhylogenyEntropyVsParentStop() {
        return m_PhylogenyEntropyVsParentStop.getValue();
    }


    public double getPhylogenyDistancesVsParentStop() {
        return m_PhylogenyDistancesVsParentStop.getValue();
    }


    @Override
    public INIFileSection create() {
        m_SectionPhylogeny = new INIFileSection("Phylogeny");
        m_SectionPhylogeny.addNode(m_PhylogenyDM = new INIFileNominal("DistanceMeasure", PHYLOGENY_DISTANCE_MEASURE, PHYLOGENY_DISTANCE_MEASURE_PDIST));
        m_SectionPhylogeny.addNode(m_PhylogenyCriterion = new INIFileNominal("OptimizationCriterion", PHYLOGENY_CRITERION, PHYLOGENY_CRITERION_BRANCHLENGTHS));
        m_SectionPhylogeny.addNode(m_PhylogenySequence = new INIFileNominal("Sequence", PHYLOGENY_SEQUENCE, PHYLOGENY_SEQUENCE_DNA));
        m_SectionPhylogeny.addNode(m_PhylogenyDistanceMatrix = new INIFileString("DistanceMatrix", "dist"));
        m_SectionPhylogeny.addNode(m_PhylogenyEntropyVsRootStop = new INIFileDouble("EntropyVsRootStopCriterion", 0));
        m_SectionPhylogeny.addNode(m_PhylogenyDistancesVsRootStop = new INIFileDouble("SumPWDistancesVsRootStopCriterion", 0));
        m_SectionPhylogeny.addNode(m_PhylogenyEntropyVsParentStop = new INIFileDouble("EntropyVsParentStopCriterion", 0));
        m_SectionPhylogeny.addNode(m_PhylogenyDistancesVsParentStop = new INIFileDouble("SumPWDistancesVsParentStopCriterion", 0));
        m_SectionPhylogeny.setEnabled(false);

        return m_SectionPhylogeny;
    }
}
