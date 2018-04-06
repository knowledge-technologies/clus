
package si.ijs.kt.clus.statistic;

import java.text.NumberFormat;

import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.jeans.list.BitList;


public class GeneticDistanceStat extends BitVectorStat {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public int m_NbTarget;
    public NominalAttrType[] m_Attrs;


    /**
     * Constructor for this class.
     * 
     * @param nomAtts
     *        array of nominal attributes
     */
    public GeneticDistanceStat(Settings sett, NominalAttrType[] nomAtts) {
        super(sett);

        m_NbTarget = nomAtts.length;
        m_Attrs = nomAtts;
    }


    public BitList getBits() {
        return m_Bits;
    }


    @Override
    public void updateWeighted(DataTuple tuple, int idx) {
        m_SumWeight += tuple.getWeight();
        m_Bits.setBit(idx);
        m_Modified = true;
    }


    // returns the index of the index-th tuple in the statistic (i.e. with bit 1)
    public int getTupleIndex(int index) {
        int size = getBits().size();
        int nbones = 0;
        int i = 0;
        while (nbones <= index && i < size) {
            if (getBits().getBit(i)) {
                nbones++;
            }
            i++;
        }
        if (nbones == index + 1) {
            return i - 1;
        }
        else {
            System.err.println("error in getTuple (GeneticDistanceStat), requesting tuple" + index);
            return -1;
        }
    }


    @Override
    public void reset() {
        m_SumWeight = 0.0;
        m_Bits.reset();
        m_Modified = true;
    }


    @Override
    public GeneticDistanceStat cloneStat() {
        GeneticDistanceStat stat = new GeneticDistanceStat(this.m_Settings, m_Attrs);
        stat.cloneFrom(this);
        return stat;
    }


    public void cloneFrom(GeneticDistanceStat other) {
        int nb = other.m_Bits.size();
        m_NbTarget = other.m_NbTarget;
        m_Attrs = other.m_Attrs;
        if (nb > 0) {
            setSDataSize(nb);
        }
    }


    @Override
    public void copy(ClusStatistic other) {
        GeneticDistanceStat or = (GeneticDistanceStat) other;
        m_SumWeight = or.m_SumWeight;
        m_Bits.copy(or.m_Bits);
        m_Modified = or.m_Modified;
        m_NbTarget = or.m_NbTarget;
        m_Attrs = or.m_Attrs;
    }


    @Override
    public void addPrediction(ClusStatistic other, double weight) {
        GeneticDistanceStat or = (GeneticDistanceStat) other;
        m_SumWeight += weight * or.m_SumWeight;
    }


    @Override
    public void add(ClusStatistic other) {
        GeneticDistanceStat or = (GeneticDistanceStat) other;
        m_SumWeight += or.m_SumWeight;
        m_Bits.add(or.m_Bits);
        m_Modified = true;
    }


    @Override
    public void addScaled(double scale, ClusStatistic other) {
        GeneticDistanceStat or = (GeneticDistanceStat) other;
        m_SumWeight += scale * or.m_SumWeight;
    }


    public void subtractFromThis(BitList bits) {
        m_SumWeight -= bits.getNbOnes();
        m_Bits.subtractFromThis(bits);
        m_Modified = true;
    }


    @Override
    public void subtractFromThis(ClusStatistic other) {
        GeneticDistanceStat or = (GeneticDistanceStat) other;
        m_SumWeight -= or.m_SumWeight;
        m_Bits.subtractFromThis(or.m_Bits);
        m_Modified = true;
    }


    public void copyAndSubtractFromThis(ClusStatistic stattocopy, ClusStatistic stattosubtract) {
        GeneticDistanceStat tocopy = (GeneticDistanceStat) stattocopy;
        GeneticDistanceStat tosubtract = (GeneticDistanceStat) stattosubtract;

        m_SumWeight = tocopy.m_SumWeight - tosubtract.m_SumWeight;
        m_Bits.copyAndSubtractFromThis(tocopy.m_Bits, tosubtract.m_Bits);
        m_NbTarget = tocopy.m_NbTarget;
        m_Attrs = tocopy.m_Attrs;
        m_Modified = true;
    }


    @Override
    public void subtractFromOther(ClusStatistic other) {
        GeneticDistanceStat or = (GeneticDistanceStat) other;
        m_SumWeight = or.m_SumWeight - m_SumWeight;
        m_Bits.subtractFromOther(or.m_Bits);
        m_Modified = true;
    }


    @Override
    public int[] getNominalPred() {
        System.out.println("getNominalPred: not implemented for GeneticDistanceStat");
        return null;
    }


    @Override
    public String getString(StatisticPrintInfo info) {
        StringBuffer buf = new StringBuffer();
        NumberFormat fr = ClusFormat.SIX_AFTER_DOT;
        buf.append("LEAF : ");
        buf.append(fr.format(m_SumWeight));
        buf.append(" sequence(s)");
        return buf.toString();
    }


    // some methods called to calculate predictions or errors, which we don't need

    @Override
    public void calcMean() {
    }


    @Override
    public double getCount(int idx, int cls) {
        return 0.0;
    }


    // FIXME: discuss this with Celine
    // getClassificationStat() should not return something that is not a ClassificationStat
    // better to disable this type of error measure in Phylo setting
    // public ClassificationStat getClassificationStat() {
    // return this;
    // }

    @Override
    public int getNbStatisticComponents() {
        throw new RuntimeException(getClass().getName() + "getNbStatisticComponents(): not implemented");
    }
}
