
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;


public class SettingsTimeSeries extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public SettingsTimeSeries(int position) {
        super(position, "TimeSeries");
    }

    /***********************************************************************
     * Section: Time series *
     ***********************************************************************/

    public enum TimeSeriesDistanceMeasure {
        DTW, QDM, TSC
    };

    public enum TimeSeriesPrototypeComplexity {
        N2, Log, Linear, NPairs, Test
    };

    private INIFileEnum<TimeSeriesDistanceMeasure> m_TimeSeriesDistance;
    private INIFileEnum<TimeSeriesPrototypeComplexity> m_TimeSeriesHeuristicSampling;


    public boolean isSectionTimeSeriesEnabled() {
        return m_Section.isEnabled();
    }


    public void setSectionTimeSeriesEnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    public boolean isTimeSeriesProtoComlexityExact() {
        return m_TimeSeriesHeuristicSampling.getValue().equals(TimeSeriesPrototypeComplexity.N2);
    }


    public TimeSeriesDistanceMeasure getTimeSeriesDistance() {
        return m_TimeSeriesDistance.getValue();
    }


    public void setTimeSeriesDistance(TimeSeriesDistanceMeasure value) {
        m_TimeSeriesDistance.setValue(value);
    }


    public TimeSeriesPrototypeComplexity getTimeSeriesHeuristicSampling() {
        return m_TimeSeriesHeuristicSampling.getValue();
    }


    public boolean checkTimeSeriesDistance(TimeSeriesDistanceMeasure value) {
        return m_TimeSeriesDistance.getValue().equals(value);
    }


    @Override
    public void create() {
        m_Section.addNode(m_TimeSeriesDistance = new INIFileEnum<TimeSeriesDistanceMeasure>("DistanceMeasure", TimeSeriesDistanceMeasure.DTW));
        m_Section.addNode(m_TimeSeriesHeuristicSampling = new INIFileEnum<>("PrototypeComplexity", TimeSeriesPrototypeComplexity.N2));

        setSectionTimeSeriesEnabled(false); // disabled by default
    }
}
