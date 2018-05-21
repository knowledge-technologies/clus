
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileStringOrDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileStringOrInt;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsData extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsData(int position) {
        super(position, "Data");
    }

    /***********************************************************************
     * Section: Data *
     ***********************************************************************/

    private INIFileString m_DataFile;
    private INIFileStringOrDouble m_TestSet;
    private INIFileStringOrDouble m_PruneSet;
    private INIFileStringOrInt m_PruneSetMax;
    /** How many folds are we having in xval OR gives a file that defines the used folds (in the data set) */
    private INIFileStringOrInt m_XValFolds;
    private INIFileBool m_RemoveMissingTarget;

    // Gradient descent optimization algorithm
    public enum NormalizeDataValues {
        None, Numeric
    };

    /** Possible values for normalizeData */
    //private final String[] NORMALIZE_DATA_VALUES = { "None", "Numeric" };
    /** Do not normalize data. DEFAULT */
    //public final static int NORMALIZE_DATA_NONE = 0;
    /** Normalize only numeric variables */
    //public final static int NORMALIZE_DATA_NUMERIC = 1;
    /** Normalize all the variables Not implemented */
    // public final static int NORMALIZE_DATA_ALL = 3;
    /** Do normalization for the data in the beginning. Done by dividing with the variance. */
    
    private INIFileEnum<NormalizeDataValues> m_NormalizeData;


    public String getDataFile() {
        return m_DataFile.getValue();
    }


    public boolean isNullFile() {
        return StringUtils.unCaseCompare(m_DataFile.getValue(), NONE);
    }


    public void updateDataFile(String fname) {
        if (isNullFile())
            m_DataFile.setValue(fname);
    }


    public String getTestFile() {
        return m_TestSet.getValue();
    }


    public boolean isNullTestFile() {
        return m_TestSet.isDoubleOrNull(NONE);
    }


    public String getPruneFile() {
        return m_PruneSet.getValue();
    }


    public boolean isNullPruneFile() {
        return m_PruneSet.isDoubleOrNull(NONE);
    }


    public double getTestProportion() {
        if (!m_TestSet.isDouble())
            return 0.0;
        return m_TestSet.getDoubleValue();
    }


    public double getPruneProportion() {
        if (!m_PruneSet.isDouble())
            return 0.0;
        return m_PruneSet.getDoubleValue();
    }


    public int getPruneSetMax() {
        if (m_PruneSetMax.isString(INFINITY_STRING))
            return Integer.MAX_VALUE;
        else
            return m_PruneSetMax.getIntValue();
    }


    public boolean isNullXValFile() {
        return m_XValFolds.isIntOrNull(NONE);
    }


    public boolean isLOOXVal() {
        return m_XValFolds.isString("LOO");
    }


    public String getXValFile() {
        return m_XValFolds.getValue();
    }


    public int getXValFolds() {
        return m_XValFolds.getIntValue();
    }


    public void setXValFolds(int folds) {
        m_XValFolds.setIntValue(folds);
    }


    public boolean isRemoveMissingTarget() {
        return m_RemoveMissingTarget.getValue();
    }


    /** Do we want to normalize the data */
    public NormalizeDataValues getNormalizeData() {
        return m_NormalizeData.getValue();
    }


    public void setPruneSetMaxEnabled(boolean value) {
        m_PruneSetMax.setEnabled(value);
    }


    public boolean isPruneSetString(String str) {
        return m_PruneSet.isString(str);
    }


    @Override
    public void create() {
        m_Section.addNode(m_DataFile = new INIFileString("File", NONE));
        m_Section.addNode(m_TestSet = new INIFileStringOrDouble("TestSet", NONE));
        m_Section.addNode(m_PruneSet = new INIFileStringOrDouble("PruneSet", NONE));
        m_Section.addNode(m_PruneSetMax = new INIFileStringOrInt("PruneSetMax", INFINITY_STRING));
        
        m_Section.addNode(m_XValFolds = new INIFileStringOrInt("XVal"));
        m_XValFolds.setIntValue(10);
        
        m_Section.addNode(m_RemoveMissingTarget = new INIFileBool("RemoveMissingTarget", false));
        m_Section.addNode(m_NormalizeData = new INIFileEnum<>("NormalizeData", NormalizeDataValues.None));
    }
}
