package clus.main.settings;

import clus.util.jeans.io.ini.INIFileDouble;
import clus.util.jeans.io.ini.INIFileInt;
import clus.util.jeans.io.ini.INIFileSection;
import clus.util.jeans.io.range.DoubleRangeCheck;
import clus.util.jeans.io.range.IntRangeCheck;

public class SettingsOptionTree implements ISettings {
    /***********************************************************************
     * Section: Option tree
     ***********************************************************************/

    protected INIFileSection m_SectionOptionTree;
    protected INIFileDouble m_optionFactor;
    protected INIFileDouble m_optionDecayFactor;
    protected INIFileDouble m_optionEpsilon;
    protected INIFileInt m_optionMaxNumberOfOptionsPerNode;
    protected INIFileInt m_optionMaxDepthOfOptionNode;


    public boolean isSectionOptionEnabled() {
        return m_SectionOptionTree.isEnabled();
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
        m_SectionOptionTree.setEnabled(enable);
    }


    @Override
    public INIFileSection create() {
        m_SectionOptionTree = new INIFileSection("OptionTree");
        m_SectionOptionTree.addNode(m_optionDecayFactor = new INIFileDouble("DecayFactor", 0.9));
        m_SectionOptionTree.addNode(m_optionEpsilon = new INIFileDouble("Epsilon", 0.1));
        m_SectionOptionTree.addNode(m_optionMaxNumberOfOptionsPerNode = new INIFileInt("MaxNumberOfOptionsPerNode", 5)); /*
                                                                                                                          * Constant
                                                                                                                          * by
                                                                                                                          * Kohavi,
                                                                                                                          * Kunz
                                                                                                                          */
        m_SectionOptionTree.addNode(m_optionMaxDepthOfOptionNode = new INIFileInt("MaxDepthOfOptionNode", 3));
        m_optionMaxNumberOfOptionsPerNode.setValueCheck(new IntRangeCheck(2, Integer.MAX_VALUE));
        m_optionMaxDepthOfOptionNode.setValueCheck(new IntRangeCheck(1, Integer.MAX_VALUE));
        m_optionDecayFactor.setValueCheck(new DoubleRangeCheck(0.0, 1.0));
        m_optionEpsilon.setValueCheck(new DoubleRangeCheck(0.0, 1.0));
        m_SectionOptionTree.setEnabled(false);
        
        return m_SectionOptionTree;
        
    }

}
