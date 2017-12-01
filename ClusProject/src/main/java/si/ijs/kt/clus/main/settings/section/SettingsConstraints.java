package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;
import si.ijs.kt.clus.util.jeans.util.StringUtils;

public class SettingsConstraints extends SettingsBase {
    
    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    
    public SettingsConstraints(int position) {
        super(position);
    }


    /***********************************************************************
     * Section: Constraints *
     ***********************************************************************/

    private INIFileString m_SyntacticConstrFile;
    private INIFileNominalOrIntOrVector m_MaxSizeConstr;
    private INIFileNominalOrDoubleOrVector m_MaxErrorConstr;
    private INIFileInt m_TreeMaxDepth;

    public boolean hasConstraintFile() {
        return !StringUtils.unCaseCompare(m_SyntacticConstrFile.getValue(), NONE);
    }

    public int getTreeMaxDepth() {
        return m_TreeMaxDepth.getValue();
    }
    
    /**
     * For tree to rules procedure, we want to induce a tree without maximum
     * depth
     */
    public void setTreeMaxDepth(int value) {
        m_TreeMaxDepth.setValue(value);
    }
    

    public String getConstraintFile() {
        return m_SyntacticConstrFile.getValue();
    }
 

    public int getMaxSize() {
        return getSizeConstraintPruning(0);
    }


    public int getSizeConstraintPruning(int idx) {
        if (m_MaxSizeConstr.isNominal(idx)) {
            return -1;
        }
        else {
            return m_MaxSizeConstr.getInt(idx);
        }
    }


    public int getSizeConstraintPruningNumber() {
        int len = m_MaxSizeConstr.getVectorLength();
        if (len == 1 && m_MaxSizeConstr.getNominal() == INFINITY_VALUE)
            return 0;
        else
            return len;
    }


    public int[] getSizeConstraintPruningVector() {
        int size_nb = getSizeConstraintPruningNumber();
        int[] sizes = new int[size_nb];
        for (int i = 0; i < size_nb; i++) {
            sizes[i] = getSizeConstraintPruning(i);
        }
        return sizes;
    }


    public void setSizeConstraintPruning(int size) {
        m_MaxSizeConstr.setInt(size);
    }


    public double getMaxErrorConstraint(int idx) {
        if (m_MaxErrorConstr.isNominal(idx)) {
            return Double.POSITIVE_INFINITY;
        }
        else {
            return m_MaxErrorConstr.getDouble(idx);
        }
    }


    public int getMaxErrorConstraintNumber() {
        int len = m_MaxErrorConstr.getVectorLength();
        if (len == 1 && m_MaxErrorConstr.getDouble(0) == 0.0)
            return 0;
        else
            return len;
    }


    public double[] getMaxErrorConstraintVector() {
        int error_nb = getMaxErrorConstraintNumber();
        double[] max_error = new double[error_nb];
        for (int i = 0; i < error_nb; i++) {
            max_error[i] = getMaxErrorConstraint(i);
        }
        return max_error;
    }


    @Override
    public INIFileSection create() {

        INIFileSection constr = new INIFileSection("Constraints");
        constr.addNode(m_SyntacticConstrFile = new INIFileString("Syntactic", NONE));
        constr.addNode(m_MaxSizeConstr = new INIFileNominalOrIntOrVector("MaxSize", INFINITY));
        constr.addNode(m_MaxErrorConstr = new INIFileNominalOrDoubleOrVector("MaxError", INFINITY));
        constr.addNode(m_TreeMaxDepth = new INIFileInt("MaxDepth", -1));
        m_MaxSizeConstr.setNominal(0);
        m_MaxErrorConstr.setDouble(0);
        
        return constr;
        
    }

    @Override
    public void initNamedValues() {
        m_TreeMaxDepth.setNamedValue(-1, INFINITY_STRING);
    }
}
