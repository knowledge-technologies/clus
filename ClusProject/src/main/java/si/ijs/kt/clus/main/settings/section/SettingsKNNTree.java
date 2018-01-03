
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsKNNTree extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsKNNTree(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }

    /***********************************************************************
     * Section: KNN Trees *
     ***********************************************************************/

    INIFileSection m_SectionKNNT;
    private INIFileInt kNNT_k;
    private INIFileString kNNT_vectDist;
    private INIFileBool kNNT_distWeighted;
    private INIFileBool kNNT_normalized;
    private INIFileBool kNNT_attrWeighted;


    public boolean getKNNTNormalized() {
        return kNNT_normalized.getValue();
    }


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


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub

    }

}
