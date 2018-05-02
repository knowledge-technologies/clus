
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.main.settings.section.SettingsTree.Heuristic;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsBeamSearch extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsBeamSearch(int position) {
        super(position);
    }

    /***********************************************************************
     * Section: Beam search *
     ***********************************************************************/
    INIFileSection m_SectionBeam;

    public static int BEAM_WIDTH;
    public static double SIZE_PENALTY;
    public static double BEAM_SIMILARITY;
    public static boolean BEAM_SYNT_DIST_CONSTR;

    protected INIFileDouble m_SizePenalty;
    protected INIFileInt m_BeamWidth;
    protected INIFileInt m_BeamBestN;
    protected INIFileInt m_TreeMaxSize;
    protected INIFileEnum<Heuristic> m_BeamAttrHeuristic;
    protected INIFileBool m_FastBS;
    protected INIFileBool m_BeamPostPrune;
    protected INIFileBool m_BMRemoveEqualHeur;
    protected INIFileDouble m_BeamSimilarity;
    protected INIFileBool m_BSortTrainParameter;
    protected INIFileBool m_BeamToForest;
    protected INIFileString m_BeamSyntacticConstrFile;


    public void setSectionBeamEnabled(boolean enable) {
        m_SectionBeam.setEnabled(enable);
    }


    public int getBeamWidth() {
        return m_BeamWidth.getValue();
    }


    public double getSizePenalty() {
        return m_SizePenalty.getValue();
    }


    public boolean isBeamSearchMode() {
        return m_SectionBeam.isEnabled();
    }


    public int getBeamBestN() {
        return m_BeamBestN.getValue();
    }


    public int getBeamTreeMaxSize() {
        return m_TreeMaxSize.getValue();
    }


    public boolean getBeamRemoveEqualHeur() {
        return m_BMRemoveEqualHeur.getValue();
    }


    public boolean getBeamSortOnTrainParameter() {
        return m_BSortTrainParameter.getValue();
    }


    public double getBeamSimilarity() {
        return m_BeamSimilarity.getValue();
    }


    public boolean isBeamPostPrune() {
        return m_BeamPostPrune.getValue();
    }


    public Heuristic getBeamAttrHeuristic() {
        return m_BeamAttrHeuristic.getValue();
    }


    public boolean hasBeamConstraintFile() {
        return !StringUtils.unCaseCompare(m_BeamSyntacticConstrFile.getValue(), NONE);
    }


    public String getBeamConstraintFile() {
        return m_BeamSyntacticConstrFile.getValue();
    }


    public boolean isBeamToForest() {
        return m_BeamToForest.getValue();
    }


    public boolean isFastBS() {
        return m_FastBS.getValue();
    }


    @Override
    public INIFileSection create() {

        m_SectionBeam = new INIFileSection("Beam");
        m_SectionBeam.addNode(m_SizePenalty = new INIFileDouble("SizePenalty", 0.1));
        m_SectionBeam.addNode(m_BeamWidth = new INIFileInt("BeamWidth", 10));
        m_SectionBeam.addNode(m_BeamBestN = new INIFileInt("BeamBestN", 5));
        m_SectionBeam.addNode(m_TreeMaxSize = new INIFileInt("MaxSize", -1));
        m_SectionBeam.addNode(m_BeamAttrHeuristic = new INIFileEnum<>("AttributeHeuristic", Heuristic.Default));
        m_SectionBeam.addNode(m_FastBS = new INIFileBool("FastSearch", true));
        m_SectionBeam.addNode(m_BeamPostPrune = new INIFileBool("PostPrune", false));
        m_SectionBeam.addNode(m_BMRemoveEqualHeur = new INIFileBool("RemoveEqualHeur", false));
        m_SectionBeam.addNode(m_BeamSimilarity = new INIFileDouble("BeamSimilarity", 0.0));
        m_SectionBeam.addNode(m_BSortTrainParameter = new INIFileBool("BeamSortOnTrainParameteres", false));
        m_SectionBeam.addNode(m_BeamSyntacticConstrFile = new INIFileString("DistSyntacticConstr", NONE));
        m_SectionBeam.addNode(m_BeamToForest = new INIFileBool("BeamToForest", false));
        m_SectionBeam.setEnabled(false);

        return m_SectionBeam;

    }


    @Override
    public void initNamedValues() {
        m_TreeMaxSize.setNamedValue(-1, "Infinity");
    }
}
