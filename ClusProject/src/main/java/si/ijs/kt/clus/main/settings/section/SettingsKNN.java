<<<<<<< HEAD

package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsKNN extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsKNN(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }

    /***********************************************************************
     * Section: KNN *
     ***********************************************************************/

    private INIFileSection m_SectionKNN;

    private final static int[] DEFAULT_K = new int[] { 1, 3 };
    private INIFileNominalOrIntOrVector m_k;

    private final static String[] DISTANCES = new String[] { "euclidean", "chebyshev", "manhattan" };
    public final static int DISTANCE_EUCLIDEAN = 0;
    public final static int DISTANCE_CHEBYSHEV = 1;
    public final static int DISTANCE_MANHATTAN = 2;
    private INIFileNominal m_distance;

    private final static String[] SEARCH_METHODS = new String[] { "BrutForce", "VP-tree", "KD-tree" };
    public final static int SEARCH_METHOD_BRUTE_FORCE = 0;
    public final static int SEARCH_METHOD_VP_TREE = 1;
    public final static int SEARCH_METHOD_KD_TREE = 2;
    private INIFileNominal m_searchMethod;

    public final static String[] DISTANCE_WEIGHTS = new String[] { "constant", "1/d", "1-d" };
    public static final int DISTANCE_WEIGHTING_CONSTANT = 0;
    public static final int DISTANCE_WEIGHTING_INVERSE = 1; // d^{-1}
    public static final int DISTANCE_WEIGHTING_MINUS = 2; // 1-d
    private INIFileNominalOrIntOrVector m_distanceWeight;

    private INIFileString m_attributeWeight; // parsed in KnnModel for its very diverse uses


    public int[] getKNNk() {
        return m_k.getIntVector();
    }


    public int getKNNDistance() {
        return m_distance.getValue();
    }


    public int[] getKNNDistanceWeight() {
        return m_distanceWeight.getNominalVector();
    }


    public String getKNNAttrWeight() {
        return m_attributeWeight.getValue();
    }


    public void setKNNAttrWeight(String val) {
        m_attributeWeight.setValue(val);
    }


    public void setSectionKNNEnabled(boolean enable) {
        m_SectionKNN.setEnabled(enable);
    }


    public boolean isKNN() {
        return m_SectionKNN.isEnabled();
    }


    public int getKNNMethod() {
        return m_searchMethod.getValue();
    }


    @Override
    public INIFileSection create() {
        m_SectionKNN = new INIFileSection("kNN");
        m_SectionKNN.addNode(m_k = new INIFileNominalOrIntOrVector("K", NONELIST));
        m_k.setIntVector(DEFAULT_K);

        m_SectionKNN.addNode(m_distance = new INIFileNominal("Distance", DISTANCES, DISTANCE_EUCLIDEAN));
        m_SectionKNN.addNode(m_searchMethod = new INIFileNominal("SearchMethod", SEARCH_METHODS, SEARCH_METHOD_BRUTE_FORCE));
        m_SectionKNN.addNode(m_distanceWeight = new INIFileNominalOrIntOrVector("DistanceWeighting", DISTANCE_WEIGHTS));
        m_distanceWeight.setNominal(DISTANCE_WEIGHTING_CONSTANT);

        m_SectionKNN.addNode(m_attributeWeight = new INIFileString("AttributeWeighting", "none"));
        m_SectionKNN.setEnabled(false);

        return m_SectionKNN;

    }


    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub

    }
}
=======

package si.ijs.kt.clus.main.settings.section;

import java.util.Arrays;

import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileArray;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsKNN extends SettingsBase {

    public SettingsKNN(int position) {
        super(position);
        // TODO Auto-generated constructor stub
    }

    /***********************************************************************
     * Section: KNN *
     ***********************************************************************/

    private INIFileSection m_SectionKNN;
    
    private final static int[] DEFAULT_K = new int[] {1, 3};
    private INIFileNominalOrIntOrVector m_k;
    
    public enum Distance {Euclidean, Chebyshev, Manhattan};
    private INIFileEnum<Distance> m_Distance;
    
    public enum SearchMethod {BruteForce, VPTree, KDTree, Oracle};
    private INIFileEnum<SearchMethod> m_SearchMethod;	
    
    public final static String[] DISTANCE_WEIGHTS = new String[]{"constant", "1/d", "1-d"};
    public static final int DISTANCE_WEIGHTING_CONSTANT = 0;
    public static final int DISTANCE_WEIGHTING_INVERSE = 1; // d^{-1}
    public static final int DISTANCE_WEIGHTING_MINUS = 2;   // 1-d
    private INIFileNominalOrIntOrVector m_distanceWeight;
    
    private INIFileString m_attributeWeight; // parsed in KnnModel for its very diverse uses
    
    private INIFileNominalOrIntOrVector m_ChosenInstancesTrain;
    private INIFileNominalOrIntOrVector m_ChosenInstancesTest;
    public static int[] DUMMY_INSTANCES = new int[] {-1};
    
    private INIFileArray m_LoadNeighboursFiles;
    private INIFileString m_SaveNeighboursFile;
    private static String EMPTY_STRING = "";
    private static String[] EMPTY_STRING_ARRAY = new String[] {};


    public int[] getKNNk() {
        return m_k.getIntVector();
    }

    public Distance getDistance() {
        return m_Distance.getChosenOption();
    }

    public int[] getKNNDistanceWeight() {
        return m_distanceWeight.getNominalVector();
    }

    public String getKNNAttrWeight() {
        return m_attributeWeight.getValue();
    }
    
    public void setKNNAttrWeight(String val) {
        m_attributeWeight.setValue(val);
    }

    public void setSectionKNNEnabled(boolean enable) {
        m_SectionKNN.setEnabled(enable);
    }


    public boolean isKNN() {
        return m_SectionKNN.isEnabled();
    }

    
    public SearchMethod getSearchMethod() {
    	return m_SearchMethod.getChosenOption();
    }

    
    public int[] getChosenIntancesTrain(int nbTrainInstances) {
    	int[] inSettings = m_ChosenInstancesTrain.getIntVector();
    	if(Arrays.equals(inSettings, DUMMY_INSTANCES)) {
    		return rangeInterval(nbTrainInstances);
    	} else {
    		return inSettings;
    	}
    }
    
    public int[] getChosenIntancesTest(int nbTestInstances) {
    	int[] inSettings = m_ChosenInstancesTest.getIntVector();
    	if(Arrays.equals(inSettings, DUMMY_INSTANCES)) {
    		return rangeInterval(nbTestInstances);
    	} else {
    		return inSettings;
    	}
    }
   
    public boolean shouldSaveNeighbours() {
    	return !m_SaveNeighboursFile.getValue().equals(EMPTY_STRING);
    }
    
    public boolean shouldLoadNeighbours() {
    	return m_LoadNeighboursFiles.getSize() > 0;
    }
    
    public String[] getLoadNeighboursFiles() {
    	String[] files = new String[m_LoadNeighboursFiles.getSize()];
    	for(int i = 0; i < files.length; i++) {
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
    
    private static boolean mustNotComputeError(int nb_instances, int[] chosenInstances) {
    	Arrays.sort(chosenInstances);
    	int[] range = new int[nb_instances];
    	for (int i = 0; i < nb_instances; i++) {
    		range[i] = i;
    	}
    	return !Arrays.equals(chosenInstances, range);
    }
    
    private int[] getChosenInstances(int nbInstances, int[] chosenInstances) {
    	if (Arrays.equals(chosenInstances, DUMMY_INSTANCES)) {
    		return rangeInterval(nbInstances);
    	} else {
    		return chosenInstances;
    	}
    }
    
    private int[] rangeInterval(int upperBound) {
    	int[] interval = new int[upperBound];
    	for (int i = 0; i < upperBound; i++) {
    		interval[i] = i;
    	}
    	return interval;
    }


    @Override
    public INIFileSection create() {
        m_SectionKNN = new INIFileSection("kNN");
        m_SectionKNN.addNode(m_k = new INIFileNominalOrIntOrVector("K", NONELIST));
        m_k.setIntVector(DEFAULT_K);
        
        m_SectionKNN.addNode(m_Distance = new INIFileEnum<Distance>("Distance", Distance.class, Distance.Euclidean));
        //m_SectionKNN.addNode(m_searchMethod = new INIFileNominal("SearchMethod", SEARCH_METHODS, SEARCH_METHOD_BRUTE_FORCE));
        m_SectionKNN.addNode(m_SearchMethod =  new INIFileEnum<SearchMethod>("SearchMethod", SearchMethod.class, SearchMethod.BruteForce));
        m_SectionKNN.addNode(m_distanceWeight = new INIFileNominalOrIntOrVector("DistanceWeighting", DISTANCE_WEIGHTS));        
        m_distanceWeight.setNominal(DISTANCE_WEIGHTING_CONSTANT);
        
        m_SectionKNN.addNode(m_attributeWeight = new INIFileString("AttributeWeighting", "none"));
        
        m_SectionKNN.addNode(m_ChosenInstancesTrain = new INIFileNominalOrIntOrVector("ChosenInstancesTrain", NONELIST));
        m_ChosenInstancesTrain.setIntVector(DUMMY_INSTANCES);
        m_SectionKNN.addNode(m_ChosenInstancesTest = new INIFileNominalOrIntOrVector("ChosenInstancesTest", NONELIST));
        m_ChosenInstancesTest.setIntVector(DUMMY_INSTANCES);
        m_SectionKNN.addNode(m_LoadNeighboursFiles = new INIFileArray("LoadNeighboursFiles", EMPTY_STRING_ARRAY));
        m_SectionKNN.addNode(m_SaveNeighboursFile = new INIFileString("SaveNeighboursFile", EMPTY_STRING));
        
        
        m_SectionKNN.setEnabled(false);

        return m_SectionKNN;

    }

    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub
        
    }
}
>>>>>>> refs/heads/matej
