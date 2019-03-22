
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileBool;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrDoubleOrVector;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominalOrIntOrVector;
import si.ijs.kt.clus.util.jeans.math.MathUtil;


public class SettingsRelief extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsRelief(int position) {
        super(position, "Relief");
    }

    /***********************************************************************
     * Section: Relief *
     ***********************************************************************/

    public static final int RELIEF_NEIGHBOUR_DEFAULT = 10;
    public static final int RELIEF_ITERATIONS_DEFAULT = -1;
    private INIFileNominalOrIntOrVector m_ReliefNbNeighbours;
    private INIFileNominalOrDoubleOrVector m_ReliefNbIterations;
    private INIFileBool m_ReliefShouldHaveNeighbourWeighting;
    private INIFileDouble m_ReliefWeightingSigma;
    private INIFileNominalOrIntOrVector m_ChosenInstances;
    public static int[] DUMMY_INSTANCES = new int[] { -1 };

    // If new types are introduced use the same names as in SettingsMLC class
    public enum MultilabelDistance {
        HammingLoss, MLAccuracy, MLFOne, SubsetAccuracy
    };

    private INIFileEnum<MultilabelDistance> m_MultilabelDistance;

    public enum ReliefStatisticsType {
    	DistanceClassic, Variance, DistanceSimplified, Steepness
    }
    private INIFileEnum<ReliefStatisticsType> mReliefStatistics;
    

    public void setSectionReliefEnabled(boolean value) {
        m_Section.setEnabled(value);
    }


    public boolean isRelief() {
        return m_Section.isEnabled();
    }


    public int[] getReliefNbNeighboursValue() {
        return m_ReliefNbNeighbours.getIntVector();
    }


    /**
     * Returns a list, containing the numbers of the iterations.
     * If this setting is given as a (list of) proportion(s) of the instances
     * in the training set, we convert it into absolute values.
     * <p>
     * If the only value is 1, i.e., 1.0, we assume that this means
     * all (100%) instances.
     * 
     * @param nbInstances
     *        The number of instances in the training set.

     */
    public int[] getReliefNbIterationsValue(int nbInstances) {
        double[] values;
        if (m_ReliefNbIterations.isVector()) {
            values = m_ReliefNbIterations.getDoubleVector();
        }
        else {
            values = new double[] { m_ReliefNbIterations.getDouble() };
        }
        boolean[] types = new boolean[] { true, true }; // {is double, is integer}
        boolean containsOne = false;
        for (double value : values) {
            if (Math.abs(1.0 - value) >= MathUtil.C1E_9) { // value != 1
                int roundedValue = (int) Math.round(value);
                int wrong_type = Math.abs(roundedValue - value) >= MathUtil.C1E_9 ? 1 : 0;
                types[wrong_type] = false;
            }
            else {
                containsOne = true;
            }
        }
        int[] iterations = new int[values.length];
        if (values.length == 1 && containsOne) {
            System.err.print(String.format("Warning! Ambiguous setting: %s = 1.0", m_ReliefNbIterations.getName()));
            System.err.println(" Assuming 1.0 means 100%.");
        }
        if (!(types[0] || types[1])) {
            throw new RuntimeException(String.format("The values for the setting %s must be of the same type!", m_ReliefNbIterations.getName()));
        }
        else if (types[0]) {
            for (int i = 0; i < values.length; i++) {
                double value = values[i];
                if (0.0 <= value && value <= 1.0) {
                    iterations[i] = (int) Math.round(value * nbInstances);
                }
                else {
                    throw new RuntimeException("The value is not in the interval [0, 1]!");
                }
            }
        }
        else {
            for (int i = 0; i < values.length; i++) {
                iterations[i] = (int) Math.round(values[i]);
            }
        }
        return iterations;
    }


    public boolean getReliefWeightNeighbours() {
        return m_ReliefShouldHaveNeighbourWeighting.getValue();
    }


    public double getReliefWeightingSigma() {
        return m_ReliefWeightingSigma.getValue();
    }


    public int[] getChosenIntances() {
        return m_ChosenInstances.getIntVector();
    }


    public MultilabelDistance getMultilabelDistance() {
        return m_MultilabelDistance.getValue();
    }
    
    public ReliefStatisticsType getReliefStatisticsType() {
    	return mReliefStatistics.getValue();
    }


    @Override
    public void create() {
        m_Section.addNode(m_ReliefNbNeighbours = new INIFileNominalOrIntOrVector("Neighbours", NONELIST));
        m_ReliefNbNeighbours.setInt(RELIEF_NEIGHBOUR_DEFAULT);

        m_Section.addNode(m_ReliefNbIterations = new INIFileNominalOrDoubleOrVector("Iterations", NONELIST));
//        m_ReliefNbIterations.setNominal(RELIEF_ITERATIONS_DEFAULT);
		m_ReliefNbIterations.setDouble(RELIEF_ITERATIONS_DEFAULT);
        m_Section.addNode(m_ReliefShouldHaveNeighbourWeighting = new INIFileBool("WeightNeighbours", false));
        /* following Weka, the authors do not give any suggestions */
        m_Section.addNode(m_ReliefWeightingSigma = new INIFileDouble("WeightingSigma", 0.5));
        m_Section.addNode(m_ChosenInstances = new INIFileNominalOrIntOrVector("ChosenInstances", NONELIST));
        m_ChosenInstances.setIntVector(DUMMY_INSTANCES);
        m_Section.addNode(m_MultilabelDistance = new INIFileEnum<MultilabelDistance>("MultilabelDistance", MultilabelDistance.HammingLoss));
        m_Section.addNode(mReliefStatistics = new INIFileEnum<ReliefStatisticsType>("ReliefStatisticsType", ReliefStatisticsType.DistanceClassic));
        m_Section.setEnabled(false);
    }
}
