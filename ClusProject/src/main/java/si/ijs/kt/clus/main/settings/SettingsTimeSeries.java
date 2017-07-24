
package si.ijs.kt.clus.main.settings;

import si.ijs.kt.clus.util.jeans.io.ini.INIFileNominal;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileSection;


public class SettingsTimeSeries implements SettingsBase {

    /***********************************************************************
     * Section: Time series *
     ***********************************************************************/

    private final String[] TIME_SERIES_DISTANCE_MEASURE = { "DTW", "QDM", "TSC" };
    public final static int TIME_SERIES_DISTANCE_MEASURE_DTW = 0;
    public final static int TIME_SERIES_DISTANCE_MEASURE_QDM = 1;
    public final static int TIME_SERIES_DISTANCE_MEASURE_TSC = 2;

    private final String[] TIME_SERIES_PROTOTYPE_COMPLEXITY = { "N2", "LOG", "LINEAR", "NPAIRS", "TEST" };
    public final static int TIME_SERIES_PROTOTYPE_COMPLEXITY_N2 = 0;
    public final static int TIME_SERIES_PROTOTYPE_COMPLEXITY_LOG = 1;
    public final static int TIME_SERIES_PROTOTYPE_COMPLEXITY_LINEAR = 2;
    public final static int TIME_SERIES_PROTOTYPE_COMPLEXITY_NPAIRS = 3;
    public final static int TIME_SERIES_PROTOTYPE_COMPLEXITY_TEST = 4;

    private INIFileSection m_SectionTimeSeries;
    private INIFileNominal m_TimeSeriesDistance;
    private INIFileNominal m_TimeSeriesHeuristicSampling;


    public boolean isSectionTimeSeriesEnabled() {
        return m_SectionTimeSeries.isEnabled();
    }


    public void setSectionTimeSeriesEnabled(boolean enable) {
        m_SectionTimeSeries.setEnabled(enable);
    }


    public boolean isTimeSeriesProtoComlexityExact() {
        if (m_TimeSeriesHeuristicSampling.getValue() == 0) {
            return true;
        }
        else {
            return false;
        }
    }


    public int getTimeSeriesDistance() {
        return m_TimeSeriesDistance.getValue();
    }

    public void setTimeSeriesDistance(int value) {
        m_TimeSeriesDistance.setSingleValue(value);
    }


   
    public int getTimeSeriesHeuristicSampling() {
        return m_TimeSeriesHeuristicSampling.getValue();
    }


    public boolean checkTimeSeriesDistance(String value) {
        return m_TimeSeriesDistance.getStringSingle().equals(value);
    }
    
    
    @Override
    public INIFileSection create() {
        m_SectionTimeSeries = new INIFileSection("TimeSeries");
        m_SectionTimeSeries.addNode(m_TimeSeriesDistance = new INIFileNominal("DistanceMeasure", TIME_SERIES_DISTANCE_MEASURE, TIME_SERIES_DISTANCE_MEASURE_DTW));
        m_SectionTimeSeries.addNode(m_TimeSeriesHeuristicSampling = new INIFileNominal("PrototypeComlexity", TIME_SERIES_PROTOTYPE_COMPLEXITY, TIME_SERIES_PROTOTYPE_COMPLEXITY_N2));
        
        
        setSectionTimeSeriesEnabled(false); // disabled by default

        return m_SectionTimeSeries;
    }

}
