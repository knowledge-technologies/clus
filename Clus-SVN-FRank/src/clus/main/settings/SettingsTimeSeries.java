package clus.main.settings;

import clus.jeans.io.ini.INIFileNominal;
import clus.jeans.io.ini.INIFileSection;

public class SettingsTimeSeries implements ISettings {

    /***********************************************************************
     * Section: Time series *
     ***********************************************************************/

    private final String[] TIME_SERIES_DISTANCE_MEASURE = { "DTW", "QDM", "TSC" };

    public final static int TIME_SERIES_DISTANCE_MEASURE_DTW = 0;
    public final static int TIME_SERIES_DISTANCE_MEASURE_QDM = 1;
    public final static int TIME_SERIES_DISTANCE_MEASURE_TSC = 2;

    private final String[] TIME_SERIES_PROTOTYPE_COMPLEXITY = { "N2", "LOG", "LINEAR", "NPAIRS", "TEST" };

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


    public int getTimeSeriesHeuristicSampling() {
        return m_TimeSeriesHeuristicSampling.getValue();
    }


    @Override
    public INIFileSection create() {
        m_SectionTimeSeries = new INIFileSection("TimeSeries");
//      m_SectionTimeSeries.addNode(m_TimeSeriesDistance = new INIFileNominal("DistanceMeasure", TIME_SERIES_DISTANCE_MEASURE, 0));
//      m_SectionTimeSeries.addNode(m_TimeSeriesHeuristicSampling = new INIFileNominal("PrototypeComlexity", TIME_SERIES_PROTOTYPE_COMPLEXITY, 0));
//      m_SectionTimeSeries.setEnabled(false);
        
        return m_SectionTimeSeries;
    }

}
