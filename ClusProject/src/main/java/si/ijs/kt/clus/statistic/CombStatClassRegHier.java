
package si.ijs.kt.clus.statistic;

import java.util.ArrayList;

import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.ext.hierarchical.WHTDStatistic;
import si.ijs.kt.clus.main.ClusStatManager;
import si.ijs.kt.clus.main.settings.Settings;


/**
 *
 * @author jurica
 */
public class CombStatClassRegHier extends CombStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    protected WHTDStatistic m_HierStat;


    /**
     * Constructor for this class.
     */
    public CombStatClassRegHier(ClusStatManager statManager, RegressionStat reg, ClassificationStat cls, WHTDStatistic hier) {
        super(statManager, reg, cls);
        m_HierStat = hier;
    }


    public CombStatClassRegHier(ClusStatManager statManager, NumericAttrType[] num, NominalAttrType[] nom, WHTDStatistic hier) {
        super(statManager, num, nom);
        m_HierStat = hier;
    }


    @Override
    public ClusStatistic cloneStat() {
        return new CombStatClassRegHier(m_StatManager, (RegressionStat) m_RegStat.cloneStat(), (ClassificationStat) m_ClassStat.cloneStat(), (WHTDStatistic) m_HierStat.cloneStat());
    }


    @Override
    public ClusStatistic cloneSimple() {
        return new CombStatClassRegHier(m_StatManager, (RegressionStat) m_RegStat.cloneSimple(), (ClassificationStat) m_ClassStat.cloneSimple(), (WHTDStatistic) m_HierStat.cloneSimple());
    }


    public WHTDStatistic getHierarchicalStat() {
        return m_HierStat;
    }


    @Override
    public void setParentStat(ClusStatistic parent) {
        CombStatClassRegHier cstat = (CombStatClassRegHier) parent;

        getClassificationStat().setParentStat(cstat.getClassificationStat());
        getRegressionStat().setParentStat(cstat.getRegressionStat());
        getHierarchicalStat().setParentStat(cstat.getHierarchicalStat());
    }


    @Override
    public ClusStatistic getParentStat() {
        return new CombStatClassRegHier(m_StatManager, (RegressionStat) m_RegStat.getParentStat(), (ClassificationStat) m_ClassStat.getParentStat(), (WHTDStatistic) m_HierStat.getParentStat());
    }


    @Override
    public void setTrainingStat(ClusStatistic train) {
        CombStatClassRegHier ctrain = (CombStatClassRegHier) train;
        m_RegStat.setTrainingStat(ctrain.getRegressionStat());
        m_ClassStat.setTrainingStat(ctrain.getClassificationStat());
        m_HierStat.setTrainingStat(ctrain.getHierarchicalStat());
    }


    @Override
    public void updateWeighted(DataTuple tuple, double weight) {
        m_RegStat.updateWeighted(tuple, weight);
        m_ClassStat.updateWeighted(tuple, weight);
        m_HierStat.updateWeighted(tuple, weight);
        m_SumWeight += weight;
    }


    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        m_RegStat.updateWeighted(tuple, tuple.getWeight());
        m_ClassStat.updateWeighted(tuple, tuple.getWeight());
        m_HierStat.updateWeighted(tuple, tuple.getWeight());
        m_SumWeight += tuple.getWeight();
    }


    @Override
    public void calcMean() {
        m_RegStat.calcMean();
        m_ClassStat.calcMean();
        m_HierStat.calcMean();
    }


    @Override
    public double getSVarS(ClusAttributeWeights scale) {
        int nbClusteringNom = m_ClassStat.getNbNominalAttributes();
        int nbClusteringNum = m_RegStat.getNbNumericAttributes();
        int nbTargetHier = m_HierStat.getNbAttributes();

        //This was with the old equation for variance in RegressionStat
        //        if(nbClusteringNom == 0)
        //            return (m_RegStat.getSVarS(scale) * nbClusteringNum + m_HierStat.getSVarS(scale) * nbTargetHier * (m_SumWeight / m_HierStat.getWeightLabeled())) / (nbClusteringNum + 1); 
        //        if(nbClusteringNum == 0)
        //            return (m_ClassStat.getSVarS(scale) * nbClusteringNom + m_HierStat.getSVarS(scale) * nbTargetHier * (m_SumWeight / m_HierStat.getWeightLabeled())) / (nbClusteringNom + 1);
        //    
        //        return (m_ClassStat.getSVarS(scale) * nbClusteringNom + m_RegStat.getSVarS(scale) * nbClusteringNum + m_HierStat.getSVarS(scale) * nbTargetHier * (m_SumWeight / m_HierStat.getWeightLabeled())) / (nbClusteringNom + nbClusteringNum + 1);

        //Note, in ClusStatManager, normalization weights of hierarchical classes are initialized with sum of hierarchical weights, therefore, here we do not need to divide with #classes, but with +1 since hierarchy is considered as one target attribute
        if (nbClusteringNom == 0)
            return (m_RegStat.getSVarS(scale) * nbClusteringNum + m_HierStat.getSVarS(scale) * nbTargetHier) / (nbClusteringNum + 1);
        if (nbClusteringNum == 0)
            return (m_ClassStat.getSVarS(scale) * nbClusteringNom + m_HierStat.getSVarS(scale) * nbTargetHier) / (nbClusteringNom + 1);

        return (m_ClassStat.getSVarS(scale) * nbClusteringNom + m_RegStat.getSVarS(scale) * nbClusteringNum + m_HierStat.getSVarS(scale) * nbTargetHier) / (nbClusteringNom + nbClusteringNum + 1);
    }


    @Override
    public double getSVarSDiff(ClusAttributeWeights scale, ClusStatistic other) {
        int nbClusteringNom = m_ClassStat.getNbNominalAttributes();
        int nbClusteringNum = m_RegStat.getNbNumericAttributes();
        int nbTargetHier = m_HierStat.getNbAttributes();
        ClassificationStat ocls = ((CombStat) other).getClassificationStat();
        RegressionStat oreg = ((CombStat) other).getRegressionStat();
        WHTDStatistic ohier = ((CombStatClassRegHier) other).getHierarchicalStat();

        //This was with the old equation for variance in RegressionStat
        //        if(nbClusteringNom == 0)
        //            return (m_RegStat.getSVarSDiff(scale, oreg) * nbClusteringNum + m_HierStat.getSVarSDiff(scale, ohier) * nbTargetHier * (other.m_SumWeight / ohier.getWeightLabeled()))  / (nbClusteringNum + 1);
        //        if(nbClusteringNum == 0)
        //            return (m_ClassStat.getSVarSDiff(scale, ocls) * nbClusteringNom + m_HierStat.getSVarSDiff(scale, ohier) * nbTargetHier * (other.m_SumWeight / ohier.getWeightLabeled()))  / (nbClusteringNom + nbClusteringNum + 1);
        //
        //        return (m_ClassStat.getSVarSDiff(scale, ocls) * nbClusteringNom + m_RegStat.getSVarSDiff(scale, oreg) * nbClusteringNum + m_HierStat.getSVarSDiff(scale, ohier) * nbTargetHier * (other.m_SumWeight / ohier.getWeightLabeled()))  / (nbClusteringNom + nbClusteringNum + 1);

        //Note, in ClusStatManager, normalization weights of hierarchical classes are initialized with sum of hierarchical weights, therefore, here we do not need to divide with #classes, but with +1 since hierarchy is considered as one target attribute
        if (nbClusteringNom == 0)
            return (m_RegStat.getSVarSDiff(scale, oreg) * nbClusteringNum + m_HierStat.getSVarSDiff(scale, ohier) * nbTargetHier) / (nbClusteringNum + 1);
        if (nbClusteringNum == 0)
            return (m_ClassStat.getSVarSDiff(scale, ocls) * nbClusteringNom + m_HierStat.getSVarSDiff(scale, ohier) * nbTargetHier) / (nbClusteringNom + nbClusteringNum + 1);

        return (m_ClassStat.getSVarSDiff(scale, ocls) * nbClusteringNom + m_RegStat.getSVarSDiff(scale, oreg) * nbClusteringNum + m_HierStat.getSVarSDiff(scale, ohier) * nbTargetHier) / (nbClusteringNom + nbClusteringNum + 1); // FIXME: implement nicer

    }


    @Override
    public String getString(StatisticPrintInfo info) {
        return "ClassificationStat: " + m_ClassStat.getString() + " RegressionStat: " + m_RegStat.toString() + " HierarchicalStat:" + m_HierStat.getString();
    }


    @Override
    public String getPredictedClassName(int idx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public String getArrayOfStatistic() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void reset() {
        m_RegStat.reset();
        m_ClassStat.reset();
        m_HierStat.reset();
        m_SumWeight = 0.0;
    }


    @Override
    public void copy(ClusStatistic other) {
        CombStatClassRegHier or = (CombStatClassRegHier) other;
        m_SumWeight = or.m_SumWeight;
        m_StatManager = or.m_StatManager;
        m_RegStat.copy(or.m_RegStat);
        m_ClassStat.copy(or.m_ClassStat);
        m_HierStat.copy(or.m_HierStat);
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        CombStatClassRegHier or = (CombStatClassRegHier) other;
        m_RegStat.addPrediction(or.m_RegStat, weight);
        m_ClassStat.addPrediction(or.m_ClassStat, weight);
        m_HierStat.addPrediction(or.m_HierStat, weight);
    }


    @Override
    public void add(ClusStatistic other) {
        CombStatClassRegHier or = (CombStatClassRegHier) other;
        m_RegStat.add(or.m_RegStat);
        m_ClassStat.add(or.m_ClassStat);
        m_HierStat.add(or.m_HierStat);
        m_SumWeight += or.m_SumWeight;
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        CombStatClassRegHier or = (CombStatClassRegHier) other;
        m_RegStat.subtractFromThis(or.m_RegStat);
        m_ClassStat.subtractFromThis(or.m_ClassStat);
        m_HierStat.subtractFromThis(or.m_HierStat);
        m_SumWeight -= or.m_SumWeight;
    }


    @Override
    public void subtractFromOther(ClusStatistic other) {
        CombStatClassRegHier or = (CombStatClassRegHier) other;
        m_RegStat.subtractFromOther(or.m_RegStat);
        m_ClassStat.subtractFromOther(or.m_ClassStat);
        m_HierStat.subtractFromOther(or.m_HierStat);
        m_SumWeight = or.m_SumWeight - m_SumWeight;
    }


    @Override
    public void vote(ArrayList votes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public boolean samePrediction(ClusStatistic other) {
        CombStatClassRegHier cstat = (CombStatClassRegHier) other;

        return getClassificationStat().samePrediction(cstat.getClassificationStat()) && getRegressionStat().samePrediction(cstat.getRegressionStat()) && getHierarchicalStat().samePrediction(cstat.getHierarchicalStat());
    }


    /**
     * Provides sum of weights of target attributes
     *
     * @return sum of weights of target attributes, or NaN if statistic doesn't
     *         contain target attributes (i.e., unsupervised learning is performed)
     */
    @Override
    public double getTargetSumWeights() {
        return /* m_ClassStat.getTargetSumWeights() + m_RegStat.getTargetSumWeights() */ +m_HierStat.getTargetSumWeights(); //the number of labeled examples
    }
}
