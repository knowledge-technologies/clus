package clus.main.settings;

import java.util.ArrayList;

import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NumericAttrType;
import clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import clus.ext.hierarchicalmtr.ClusHMTRNode;
import clus.jeans.io.ini.INIFileDouble;
import clus.jeans.io.ini.INIFileNominal;
import clus.jeans.io.ini.INIFileSection;
import clus.jeans.io.ini.INIFileString;

public class SettingsHMTR implements ISettings {

    SettingsAttribute m_SettAttribute;
    SettingsGeneric m_SettGeneric;
    
    public SettingsHMTR(SettingsAttribute settAttribute, SettingsGeneric settGeneric) {
        m_SettAttribute = settAttribute;
        m_SettGeneric = settGeneric;
    }
    
    
    /***********************************************************************
     * Section: Hierarchical multi-target regression *
     ***********************************************************************/

    private final String[] HMTR_HIERTYPES = { "Disabled", "Tree", "DAG" };
    public final static int HMTR_HIERTYPE_NONE = 0; // disabled
    public final static int HMTR_HIERTYPE_TREE = 1;
    public final static int HMTR_HIERTYPE_DAG = 2;

    private final String[] HMTR_HIERDIST = { "WeightedEuclidean", "Jaccard" };
    public final static int HMTR_HIERDIST_WEIGHTED_EUCLIDEAN = 0;
    public final static int HMTR_HIERDIST_JACCARD = 1;

    private final String[] HMTR_AGGS = { "SUM", "AVG", "MEDIAN", "MIN", "MAX", "AND", "OR", "COUNT", "VAR", "STDEV", "ZERO", "ONE" };
    public final static int HMTR_AGG_SUM = 0;
    public final static int HMTR_AGG_AVG = 1;
    public final static int HMTR_AGG_MEDIAN = 2;
    public final static int HMTR_AGG_MIN = 3;
    public final static int HMTR_AGG_MAX = 4;
    public final static int HMTR_AGG_AND = 5;
    public final static int HMTR_AGG_OR = 6;
    public final static int HMTR_AGG_COUNT = 7;
    public final static int HMTR_AGG_VAR = 8;
    public final static int HMTR_AGG_STDEV = 9;
    public final static int HMTR_AGG_ZERO = 10;
    public final static int HMTR_AGG_ONE = 11;

    private INIFileSection m_SectionHMTR;
    protected static INIFileNominal m_HMTRType;
    private INIFileNominal m_HMTRDistance;
    private INIFileNominal m_HMTRAggregation;
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
        m_SectionHMTR.setEnabled(enable);
    }


    public boolean isSectionHMTREnabled() {
        return m_SectionHMTR.isEnabled();
    }


    public boolean isHMTREnabled() {
        return getHMTRType() != HMTR_HIERTYPE_NONE;
    }


    public int getHMTRType() {
        return m_HMTRType.getValue();
    }


    public INIFileNominal getHMTRDistance() {
        return m_HMTRDistance;
    }


    public INIFileNominal getHMTRAggregation() {
        return m_HMTRAggregation;
    }


    public INIFileString getHMTRHierarchyString() {
        return m_HMTRHierarchyString;
    }


    @Override
    public INIFileSection create() {

        m_SectionHMTR = new INIFileSection("HMTR");
        m_SectionHMTR.addNode(m_HMTRType = new INIFileNominal("Type", HMTR_HIERTYPES, HMTR_HIERTYPE_NONE));
        m_SectionHMTR.addNode(m_HMTRDistance = new INIFileNominal("Distance", HMTR_HIERDIST, HMTR_HIERDIST_WEIGHTED_EUCLIDEAN));
        m_SectionHMTR.addNode(m_HMTRAggregation = new INIFileNominal("Aggregation", HMTR_AGGS, HMTR_AGG_SUM));
        m_SectionHMTR.addNode(m_HMTRHierarchyString = new INIFileString("Hierarchy"));
        m_SectionHMTR.addNode(m_HMTRHierarchyWeight = new INIFileDouble("Weight"));
        m_SectionHMTR.setEnabled(false);
        
        return m_SectionHMTR;
    }



    public void addHMTRTargets(ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy) {

        if (this.isSectionHMTREnabled()) {

            ArrayList<ClusAttrType> attributes = schema.getAttr();

            ArrayList<String> attrNames = new ArrayList<String>();

            for (ClusAttrType attribute : attributes) {

                attrNames.add(attribute.getName());

            }

            if (m_SettGeneric.getVerbose() > 0)
                System.out.print("Aggregate attributes (a.k.a. not present in the database): ");

            String comma = "";
            int numAggregates = 0;

            for (ClusHMTRNode node : hmtrHierarchy.getNodes()) {
                if (!attrNames.contains(node.getName())) {

                    node.setAggregate(true);
                    System.out.print(comma + node.getName());
                    comma = ", ";
                    schema.addAttrType(new NumericAttrType(node.getName()));
                    numAggregates++;

                }
            }

            System.out.println();
            int nb = schema.getNbAttributes();

            String targets = m_SettAttribute.getTarget();

            for (int i = numAggregates; i > 0; i--) {
                targets += "," + (nb - i + 1);
            }
            
            m_SettAttribute.setTarget(targets);
            schema.setNbHMTR(numAggregates);
        }
    }
    
}
