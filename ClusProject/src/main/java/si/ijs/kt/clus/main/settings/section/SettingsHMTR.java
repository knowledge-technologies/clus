
package si.ijs.kt.clus.main.settings.section;

import java.util.ArrayList;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import si.ijs.kt.clus.ext.hierarchicalmtr.ClusHMTRNode;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.SettingsBase;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileDouble;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileEnum;
import si.ijs.kt.clus.util.jeans.io.ini.INIFileString;


public class SettingsHMTR extends SettingsBase {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    SettingsAttribute m_SettAttribute;
    SettingsGeneral m_SettGeneral;


    public SettingsHMTR(int position, SettingsAttribute settAttribute, SettingsGeneral settGeneral) {
        super(position, "HMTR");

        m_SettAttribute = settAttribute;
        m_SettGeneral = settGeneral;
    }

    public static final String HMTR_ERROR_POSTFIX = "HMTR";

    /***********************************************************************
     * Section: Hierarchical multi-target regression *
     ***********************************************************************/

    public enum HierarchyTypesHMTR {
        Disabled, Tree, DAG
    };

    public enum HierarchyDistanceHMTR {
        WeightedEuclidean, Jaccard
    };

    public enum HierarchyAggregationsHMTR {
        SUM, AVG, MEDIAN, MIN, MAX, AND, OR, COUNT, VAR, STDEV, ZERO, ONE
    };

    protected static INIFileEnum<HierarchyTypesHMTR> m_HMTRType;
    private INIFileEnum<HierarchyDistanceHMTR> m_HMTRDistance;
    private INIFileEnum<HierarchyAggregationsHMTR> m_HMTRAggregation;
    private INIFileString m_HMTRHierarchyString;
    private INIFileDouble m_HMTRHierarchyWeight;
    private boolean m_HMTRUsingDump = false;


    public void setHMTRUsingDump(boolean val) {
        m_HMTRUsingDump = val;
    }


    public boolean isHMTRUsingDump() {
        return m_HMTRUsingDump;
    }


    public INIFileDouble getHMTRHierarchyWeight() {
        return m_HMTRHierarchyWeight;
    }


    public void setSectionHMTREnabled(boolean enable) {
        m_Section.setEnabled(enable);
    }


    public boolean isSectionHMTREnabled() {
        return m_Section.isEnabled();
    }


    public boolean isHMTREnabled() {
        return !getHMTRType().equals(HierarchyTypesHMTR.Disabled);
    }


    public HierarchyTypesHMTR getHMTRType() {
        return m_HMTRType.getValue();
    }


    public INIFileEnum<HierarchyDistanceHMTR> getHMTRDistance() {
        return m_HMTRDistance;
    }


    public HierarchyAggregationsHMTR getHMTRAggregation() {
        return m_HMTRAggregation.getValue();
    }


    public String getHMTRAggregationName() {
        return m_HMTRAggregation.getStringValue();
    }


    public String getHMTRHierarchyString() {
        return m_HMTRHierarchyString.getStringValue();
    }


    @Override
    public void create() {
        m_Section.addNode(m_HMTRType = new INIFileEnum<>("Type", HierarchyTypesHMTR.Disabled));
        m_Section.addNode(m_HMTRDistance = new INIFileEnum<>("Distance", HierarchyDistanceHMTR.WeightedEuclidean));
        m_Section.addNode(m_HMTRAggregation = new INIFileEnum<>("Aggregation", HierarchyAggregationsHMTR.SUM));
        m_Section.addNode(m_HMTRHierarchyString = new INIFileString("Hierarchy"));
        m_Section.addNode(m_HMTRHierarchyWeight = new INIFileDouble("Weight"));

        m_Section.setEnabled(false);
    }


    public void addHMTRTargets(ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy) {

        if (this.isSectionHMTREnabled()) {

            ArrayList<ClusAttrType> attributes = schema.getAttr();
            ArrayList<String> attrNames = new ArrayList<String>();

            attributes.stream().forEach(a -> attrNames.add(a.getName()));

            ArrayList<String> aggregates = new ArrayList<String>();
            for (ClusHMTRNode node : hmtrHierarchy.getNodes()) {
                if (!attrNames.contains(node.getName())) {
                    node.setAggregate(true);
                    aggregates.add(node.getName());
                    schema.addAttrType(new NumericAttrType(node.getName()));
                }
            }

            if (m_SettGeneral.getVerbose() > 0) {
                System.out.print("Aggregate attributes (a.k.a. not present in the database): ");
                ClusLogger.info(String.join(", ", aggregates));
                ClusLogger.info();
            }

            int nb = schema.getNbAttributes();

            String targets = m_SettAttribute.getTarget();
            int nbAggregates = aggregates.size();
            targets += "," + (nb - nbAggregates + 1) + "-" + nb;

            m_SettAttribute.setTarget(targets);
            schema.setNbHMTRAttributes(nbAggregates);
        }
    }
}
