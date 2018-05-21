
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.range.DoubleRangeCheck;
import si.ijs.kt.clus.util.jeans.io.range.IntRangeCheck;


public class SettingsOptionTree extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsOptionTree(int position) {
        super(position, "OptionTree");
    }

    /***********************************************************************
     * Section: Option tree
     ***********************************************************************/

    protected INIFileDouble m_optionFactor;
    protected INIFileDouble m_optionDecayFactor;
    protected INIFileDouble m_optionEpsilon;
    protected INIFileInt m_optionMaxNumberOfOptionsPerNode;
    protected INIFileInt m_optionMaxDepthOfOptionNode;


    public boolean isSectionOptionEnabled() {
        return m_Section.isEnabled();
    }


    public double getOptionDecayFactor() {
        return m_optionDecayFactor.getValue();
    }


    public double getOptionEpsilon() {
        return m_optionEpsilon.getValue();
    }


    public int getOptionMaxNumberOfOptionsPerNode() {
        return m_optionMaxNumberOfOptionsPerNode.getValue();
    }


    public int getOptionMaxDepthOfOptionNode() {
        return m_optionMaxDepthOfOptionNode.getValue();
    }


    public void setSectionOptionEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    @Override
    public void create() {
        m_Section.addNode(m_optionDecayFactor = new INIFileDouble("DecayFactor", 0.9));
        m_optionDecayFactor.setValueCheck(new DoubleRangeCheck(0.0, 1.0));

        m_Section.addNode(m_optionEpsilon = new INIFileDouble("Epsilon", 0.1));
        m_optionEpsilon.setValueCheck(new DoubleRangeCheck(0.0, 1.0));

        /* Constant by Kohavi Kunz */
        m_Section.addNode(m_optionMaxNumberOfOptionsPerNode = new INIFileInt("MaxNumberOfOptionsPerNode", 5));
        m_optionMaxNumberOfOptionsPerNode.setValueCheck(new IntRangeCheck(2, Integer.MAX_VALUE));

        m_Section.addNode(m_optionMaxDepthOfOptionNode = new INIFileInt("MaxDepthOfOptionNode", 3));
        m_optionMaxDepthOfOptionNode.setValueCheck(new IntRangeCheck(1, Integer.MAX_VALUE));

        m_Section.setEnabled(false);
    }
}
