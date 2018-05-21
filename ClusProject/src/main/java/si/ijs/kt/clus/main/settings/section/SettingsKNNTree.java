
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsKNNTree extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsKNNTree(int position) {
        super(position, "kNNTree");
    }

    /***********************************************************************
     * Section: KNN Trees *
     ***********************************************************************/

    @SuppressWarnings("unused")
    private INIFileInt kNNT_k;
    @SuppressWarnings("unused")
    private INIFileString kNNT_vectDist;
    @SuppressWarnings("unused")
    private INIFileBool kNNT_distWeighted;
    private INIFileBool kNNT_normalized;
    @SuppressWarnings("unused")
    private INIFileBool kNNT_attrWeighted;


    public boolean getKNNTNormalized() {
        return kNNT_normalized.getValue();
    }


    public void setSectionKNNTEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    @Override
    public void create() {
        m_Section.addNode(kNNT_k = new INIFileInt("k", 3));
        m_Section.addNode(kNNT_vectDist = new INIFileString("VectorDistance", "Euclidian"));
        m_Section.addNode(kNNT_distWeighted = new INIFileBool("DistanceWeighted", false));
        m_Section.addNode(kNNT_normalized = new INIFileBool("Normalizing", true));
        m_Section.addNode(kNNT_attrWeighted = new INIFileBool("AttributeWeighted", false));

        m_Section.setEnabled(false);
    }
}
