package clus.main.settings;

import clus.jeans.io.ini.INIFileBool;
import clus.jeans.io.ini.INIFileInt;
import clus.jeans.io.ini.INIFileSection;
import clus.jeans.io.ini.INIFileString;

public class SettingsKNNTree implements ISettings {
    /***********************************************************************
     * Section: KNN Trees *
     ***********************************************************************/

    INIFileSection m_SectionKNNT;
    public static INIFileInt kNNT_k;
    public static INIFileString kNNT_vectDist;
    public static INIFileBool kNNT_distWeighted;
    public static INIFileBool kNNT_normalized;
    public static INIFileBool kNNT_attrWeighted;


    public void setSectionKNNTEnabled(boolean enable) {
        m_SectionKNNT.setEnabled(enable);
    }


    @Override
    public INIFileSection create() {

        // m_SectionKNN = new INIFileSection("kNN");
        // m_SectionKNN.addNode(m_kNN_k = new INIFileString("k", "1,3"));
        // m_SectionKNN.addNode(m_kNNmethod = new INIFileNominal("SearchMethod", kNN_METHODS, 0));
        // m_SectionKNN.addNode(m_kNNdistance = new INIFileNominal("Distance", kNN_DISTANCES, 0));
        // m_SectionKNN.addNode(m_kNNdistanceWeight = new INIFileNominal("DistanceWeighting", kNN_DIST_WEIGHTS, 0));
        // m_SectionKNN.addNode(m_kNNattrWeight = new INIFileString("attributeWeighting","none"));

        m_SectionKNNT = new INIFileSection("kNNTree");
        m_SectionKNNT.addNode(kNNT_k = new INIFileInt("k", 3));
        m_SectionKNNT.addNode(kNNT_vectDist = new INIFileString("VectorDistance", "Euclidian"));
        m_SectionKNNT.addNode(kNNT_distWeighted = new INIFileBool("DistanceWeighted", false));
        m_SectionKNNT.addNode(kNNT_normalized = new INIFileBool("Normalizing", true));
        m_SectionKNNT.addNode(kNNT_attrWeighted = new INIFileBool("AttributeWeighted", false));
        m_SectionKNNT.setEnabled(false);
        
        
        return m_SectionKNNT;
    }


}
