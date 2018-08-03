
package si.ijs.kt.clus.main.settings.section;

import java.util.Arrays;
import java.util.List;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileArray;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnumList;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsKNN extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsKNN(int position) {
        super(position, "kNN");
        
    }

    /***********************************************************************
     * Section: KNN *
     ***********************************************************************/
    private INIFileEnum<Distance> m_Distance;
    private INIFileNominalOrIntOrVector m_k;
    private INIFileEnum<SearchMethod> m_SearchMethod;
    private INIFileEnumList<DistanceWeights> m_distanceWeight;

    private INIFileString m_attributeWeight; // parsed in KnnModel for its very diverse uses

    private INIFileNominalOrIntOrVector m_ChosenInstancesTrain;
    private INIFileNominalOrIntOrVector m_ChosenInstancesTest;
    public static int[] DUMMY_INSTANCES = new int[] { -1 };

    private INIFileArray m_LoadNeighboursFiles;
    private INIFileString m_SaveNeighboursFile;
    private static String EMPTY_STRING = "";
    private static String[] EMPTY_STRING_ARRAY = new String[] {};

    private INIFileBool m_IsMlcKnn;
    private INIFileDouble m_MlcCountSmoother;
    
    private final static int[] DEFAULT_K = new int[] { 1, 3 };

    public enum Distance {
        Euclidean, Chebyshev, Manhattan
    };

    public enum SearchMethod {
        BruteForce, VPTree, KDTree, Oracle
    };

    public enum DistanceWeights {
        Constant, OneOverD, OneMinusD
    };


    public int[] getKNNk() {
        return m_k.getIntVector();
    }


    public Distance getDistance() {
        return m_Distance.getValue();
    }


    public List<DistanceWeights> getKNNDistanceWeights() {
        return m_distanceWeight.getValue();
    }


    public String getKNNDistanceWeightsString() {
        return m_distanceWeight.getStringValue();
    }


    public String getKNNAttrWeight() {
        return m_attributeWeight.getValue();
    }


    public void setKNNAttrWeight(String val) {
        m_attributeWeight.setValue(val);
    }


    public void setSectionKNNEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    public boolean isKNN() {
        return m_Section.isEnabled();
    }


    public SearchMethod getSearchMethod() {
        return m_SearchMethod.getValue();
    }


    public int[] getChosenIntancesTrain(int nbTrainInstances) {
        int[] inSettings = m_ChosenInstancesTrain.getIntVector();
        return getChosenInstances(nbTrainInstances, inSettings);
    }


    public int[] getChosenIntancesTest(int nbTestInstances) {
        int[] inSettings = m_ChosenInstancesTest.getIntVector();
        return getChosenInstances(nbTestInstances, inSettings);
    }


    public boolean shouldSaveNeighbours() {
        return !m_SaveNeighboursFile.getValue().equals(EMPTY_STRING);
    }


    public boolean shouldLoadNeighbours() {
        return m_LoadNeighboursFiles.getSize() > 0;
    }


    public String[] getLoadNeighboursFiles() {
        String[] files = new String[m_LoadNeighboursFiles.getSize()];
        for (int i = 0; i < files.length; i++) {
            files[i] = m_LoadNeighboursFiles.getStringAt(i);
        }
        return files;
    }


    public String getSaveNeighboursFile() {
        return m_SaveNeighboursFile.getValue();
    }


    public boolean mustNotComputeTrainingError(int nbTrainInstances) {
        return mustNotComputeError(nbTrainInstances, getChosenIntancesTrain(nbTrainInstances));
    }


    public boolean mustNotComputeTestError(int nbTestInstances) {
        return mustNotComputeError(nbTestInstances, getChosenIntancesTest(nbTestInstances));
    }
    
    public boolean isMlcKnn() {
    	return m_IsMlcKnn.getValue();
    }


    private static boolean mustNotComputeError(int nb_instances, int[] chosenInstances) {
        Arrays.sort(chosenInstances);
        int[] range = rangeInterval(nb_instances);
        return !Arrays.equals(chosenInstances, range);
    }


    private static int[] getChosenInstances(int nbInstances, int[] chosenInstances) {
        Arrays.sort(chosenInstances);
        if (Arrays.equals(chosenInstances, DUMMY_INSTANCES)) {
            return rangeInterval(nbInstances);
        }
        else {
            return chosenInstances;
        }
    }


    private static int[] rangeInterval(int upperBound) {
        int[] interval = new int[upperBound];
        for (int i = 0; i < upperBound; i++) {
            interval[i] = i;
        }
        return interval;
    }


    @Override
    public void create() {
        m_Section.addNode(m_k = new INIFileNominalOrIntOrVector("K", NONELIST));
        m_k.setIntVector(DEFAULT_K);

        m_Section.addNode(m_Distance = new INIFileEnum<>("Distance", Distance.Euclidean));
        m_Section.addNode(m_SearchMethod = new INIFileEnum<>("SearchMethod", SearchMethod.BruteForce));
        m_Section.addNode(m_distanceWeight = new INIFileEnumList<>("DistanceWeighting", DistanceWeights.Constant));
        m_Section.addNode(m_attributeWeight = new INIFileString("AttributeWeighting", "none"));

        m_Section.addNode(m_ChosenInstancesTrain = new INIFileNominalOrIntOrVector("ChosenInstancesTrain", NONELIST));
        m_ChosenInstancesTrain.setIntVector(DUMMY_INSTANCES);

        m_Section.addNode(m_ChosenInstancesTest = new INIFileNominalOrIntOrVector("ChosenInstancesTest", NONELIST));
        m_ChosenInstancesTest.setIntVector(DUMMY_INSTANCES);

        m_Section.addNode(m_LoadNeighboursFiles = new INIFileArray("LoadNeighboursFiles", EMPTY_STRING_ARRAY));
        m_Section.addNode(m_SaveNeighboursFile = new INIFileString("SaveNeighboursFile", EMPTY_STRING));
        
        m_Section.addNode(m_IsMlcKnn = new INIFileBool("IsMlcKnn", false));
        m_Section.addNode(m_MlcCountSmoother = new INIFileDouble("MlcCountSmoother", 1.0)); // Laplace by default...

        m_Section.setEnabled(false);
    }


    @Override
    public void initNamedValues() {
        

    }
}
