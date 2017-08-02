
package si.ijs.kt.clus.main.settings.section;

import si.ijs.kt.clus.main.settings.SettingsBase;
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
    private INIFileString kNN_k;
    private INIFileString kNN_distance;
    private INIFileString kNN_method;
    private INIFileString kNN_distanceWeight;
    private INIFileString kNN_attrWeight;


    public String getKNNk() {
        return kNN_k.getValue();
    }

    public String getKNNDistance() {
        return kNN_distance.getValue();
    }

    public String getKNNDistanceWeight() {
        return kNN_distanceWeight.getValue();
    }

    public String getKNNAttrWeight() {
        return kNN_attrWeight.getValue();
    }
    
    public void setKNNAttrWeight(String val) {
        kNN_attrWeight.setValue(val);
    }

    public void setSectionKNNEnabled(boolean enable) {
        m_SectionKNN.setEnabled(enable);
    }


    public boolean isKNN() {
        return m_SectionKNN.isEnabled();
    }


    public String getKNNMethod() {
        return this.kNN_method.getStringValue();
    }

    // INIFileSection m_SectionKNN;
    // public final static String[] kNN_METHODS = {"VP-tree", "KD-tree", "BrutForce"};
    // public final static int kNN_VP = 0;
    // public final static int kNN_KD = 1;
    // public final static int kNN_BF = 2;
    //
    // public final static String[] kNN_DISTANCES = {"Euclidean", "Manhattan", "Chebyshev"};
    // public final static int kNN_EUCLIDEAN = 0;
    // public final static int kNN_MANHATTAN = 1;
    // public final static int kNN_CHEBYSHEV = 2;
    //
    // public final static String[] kNN_DIST_WEIGHTS = {"None", "1/d", "1-d"};
    // public final static int kNN_DIST_WEIGHT_NONE = 0;
    // public final static int kNN_DIST_WEIGHT_INVERSE = 1;
    // public final static int kNN_DIST_WEIGHT_SUBSTRACT = 2;
    //
    // public static INIFileString m_kNN_k;
    // public static INIFileNominal m_kNNdistance;
    // public static INIFileNominal m_kNNmethod;
    // public static INIFileNominal m_kNNdistanceWeight;
    // public static INIFileString m_kNNattrWeight;
    //
    // public void setSectionKNNEnabled(boolean enable) {
    // m_SectionKNN.setEnabled(enable);
    // }
    //
    // public void setKNNNbNeighbors(String value){
    // m_kNN_k.setValue(value);
    // }
    //
    // public String getKnnNbNeighbors(){
    // return m_kNN_k.getStringValue();
    // }
    //
    //
    // public void setKNNMethod(String value){
    // m_kNNmethod.setValue(value);
    // }
    //
    // public int getKnnMethod(){
    // return m_kNNmethod.getValue();
    // }
    //
    // public String getKnnMethodName(){
    // return m_kNNmethod.getStringValue();
    // }
    //
    // public boolean isKNN(){
    // return m_SectionKNN.isEnabled();
    // }
    //
    // public void setkNNDistance(String value){
    // m_kNNdistance.setValue(value);
    // }
    //
    // public int getkNNDistance(){
    // return m_kNNdistance.getValue();
    // }
    //
    // public void setkNNDistanceWeighting(String value){
    // m_kNNdistanceWeight.setValue(value);
    // }
    //
    // public String getkNNDistanceName(){
    // return m_kNNdistance.getStringValue();
    // }
    //
    // public int getkNNDistanceWeighting(){
    // return m_kNNdistanceWeight.getValue();
    // }
    //
    // public void setkNNAttributeWeighting(String value){
    // m_kNNattrWeight.setValue(value);
    // }
    //
    // public String getkNNAttributeWeighting(){
    // return m_kNNattrWeight.getValue();
    // }


    @Override
    public INIFileSection create() {
        //         // m_SectionKNN = new INIFileSection("kNN");
        // m_SectionKNN.addNode(kNN_k = new INIFileInt("k", 3));
        // m_SectionKNN.addNode(kNN_vectDist = new INIFileString("VectorDistance", "Euclidian"));
        // m_SectionKNN.addNode(kNN_distWeighted = new INIFileBool("DistanceWeighted", false));
        // m_SectionKNN.addNode(kNN_normalized = new INIFileBool("Normalizing", true));
        // m_SectionKNN.addNode(kNN_attrWeighted = new INIFileBool("AttributeWeighted", false));
        // m_SectionKNN.setEnabled(false);

        m_SectionKNN = new INIFileSection("kNN");
        m_SectionKNN.addNode(kNN_k = new INIFileString("k", "1,3"));
        m_SectionKNN.addNode(kNN_method = new INIFileString("method", "kd-tree"));
        m_SectionKNN.addNode(kNN_distance = new INIFileString("distance", "euclidean"));
        m_SectionKNN.addNode(kNN_distanceWeight = new INIFileString("distanceWeighting", "none"));
        m_SectionKNN.addNode(kNN_attrWeight = new INIFileString("attributeWeighting", "none"));
        m_SectionKNN.setEnabled(false);

        return m_SectionKNN;

    }

    @Override
    public void initNamedValues() {
        // TODO Auto-generated method stub
        
    }
}
