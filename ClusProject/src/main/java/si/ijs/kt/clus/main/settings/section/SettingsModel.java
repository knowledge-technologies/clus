
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileInt;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsModel extends SettingsBase {

    /***********************************************************************
     * Section: Model *
     ***********************************************************************/

    protected INIFileDouble m_MinW;
    protected INIFileDouble m_MinKnownW;
    protected INIFileInt m_MinNbEx;
    protected INIFileString m_TuneFolds;
//    protected INIFileNominalOrDoubleOrVector m_ClassWeight;
    protected INIFileBool m_NominalSubsetTests;


    public SettingsModel(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }


    public double getMinimalWeight() {
        return m_MinW.getValue();
    }


    public double getMinimalKnownWeight() {
        return m_MinKnownW.getValue();
    }


    public int getMinimalNbExamples() {
        return m_MinNbEx.getValue();
    }


    public void setMinimalWeight(double val) {
        m_MinW.setValue(val);
    }


    public String getTuneFolds() {
        return m_TuneFolds.getValue();
    }


//    public double[] getClassWeight() {
//        return m_ClassWeight.getDoubleVector();
//    }


    public boolean isNominalSubsetTests() {
        return m_NominalSubsetTests.getValue();
    }


    @Override
    public INIFileSection create() {
        INIFileSection model = new INIFileSection("Model");
        model.addNode(m_MinW = new INIFileDouble("MinimalWeight", 2.0));
        model.addNode(m_MinNbEx = new INIFileInt("MinimalNumberExamples", 0));
        model.addNode(m_MinKnownW = new INIFileDouble("MinimalKnownWeight", 0));
        model.addNode(m_TuneFolds = new INIFileString("ParamTuneNumberFolds", "10"));
//        model.addNode(m_ClassWeight = new INIFileNominalOrDoubleOrVector("ClassWeights", EMPTY));
        model.addNode(m_NominalSubsetTests = new INIFileBool("NominalSubsetTests", true));
        
        return model;
    }


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub

    }

}
