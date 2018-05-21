
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsPhylogeny extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsPhylogeny(int position) {
        super(position, "Phylogeny");
    }

    /***********************************************************************
     * Section: Phylogeny *
     ***********************************************************************/

    public enum PhylogenyDistanceMeasure {
        PDist, Edit, JC, Kimura, AminoKimura
    };

    public enum PhylogenySequence {
        DNA, Protein, Any
    };

    public enum PhylogenyCriterion {
        MinTotBranchLength, MaxAvgPWDistance, MaxMinPWDistance
    };

    private INIFileEnum<PhylogenyDistanceMeasure> m_PhylogenyDM;
    private INIFileEnum<PhylogenyCriterion> m_PhylogenyCriterion;
    private INIFileEnum<PhylogenySequence> m_PhylogenySequence;
    private INIFileString m_PhylogenyDistanceMatrix;
    private INIFileDouble m_PhylogenyEntropyVsRootStop;
    private INIFileDouble m_PhylogenyDistancesVsRootStop;
    private INIFileDouble m_PhylogenyEntropyVsParentStop;
    private INIFileDouble m_PhylogenyDistancesVsParentStop;


    public PhylogenySequence getPhylogenySequence() {
        return m_PhylogenySequence.getValue();
    }


    public PhylogenyDistanceMeasure getPhylogenyDM() {
        return m_PhylogenyDM.getValue();
    }


    public PhylogenyCriterion getPhylogenyCriterion() {
        return m_PhylogenyCriterion.getValue();
    }


    public String getPhylogenyDistanceMatrix() {
        return m_PhylogenyDistanceMatrix.getValue();
    }


    public boolean isSectionPhylogenyEnabled() {
        return m_Section.isEnabled();
    }


    public void setSectionPhylogenyEnabled(boolean enable) {
        m_Section.setEnabled(enable);
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
    public void create() {
        m_Section.addNode(m_PhylogenyDM = new INIFileEnum<>("DistanceMeasure", PhylogenyDistanceMeasure.PDist));
        m_Section.addNode(m_PhylogenyCriterion = new INIFileEnum<>("OptimizationCriterion", PhylogenyCriterion.MinTotBranchLength));
        m_Section.addNode(m_PhylogenySequence = new INIFileEnum<>("Sequence", PhylogenySequence.DNA));
        m_Section.addNode(m_PhylogenyDistanceMatrix = new INIFileString("DistanceMatrix", "dist"));
        m_Section.addNode(m_PhylogenyEntropyVsRootStop = new INIFileDouble("EntropyVsRootStopCriterion", 0));
        m_Section.addNode(m_PhylogenyDistancesVsRootStop = new INIFileDouble("SumPWDistancesVsRootStopCriterion", 0));
        m_Section.addNode(m_PhylogenyEntropyVsParentStop = new INIFileDouble("EntropyVsParentStopCriterion", 0));
        m_Section.addNode(m_PhylogenyDistancesVsParentStop = new INIFileDouble("SumPWDistancesVsParentStopCriterion", 0));

        m_Section.setEnabled(false);
    }
}
