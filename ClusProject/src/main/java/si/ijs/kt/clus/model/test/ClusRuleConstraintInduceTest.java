
package si.ijs.kt.clus.model.test;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.main.settings.Settings;


public class ClusRuleConstraintInduceTest extends NodeTest {

    protected double m_Bound;
    protected NumericAttrType m_Type;
    // true if test is of form "< x"
    protected boolean smallerThan;


    public ClusRuleConstraintInduceTest(ClusAttrType attr, double bound, boolean test) {
        m_Type = (NumericAttrType) attr;
        m_Bound = bound;
        setArity(2);
        smallerThan = test;
    }


    public boolean isSmallerThanTest() {
        return smallerThan;
    }

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;


    public boolean isInverseNumeric() {
        return false;
    }


    public final int getAttrIndex() {
        return m_Type.getArrayIndex();
    }


    public final NumericAttrType getNumType() {
        return m_Type;
    }


    public final double getBound() {
        return m_Bound;
    }


    public final void setBound(double bound) {
        m_Bound = bound;
    }


    @Override
    public ClusAttrType getType() {
        return m_Type;
    }


    @Override
    public void setType(ClusAttrType type) {
        m_Type = (NumericAttrType) type;
    }


    @Override
    public String getString() {
        String value = m_Bound != Double.NEGATIVE_INFINITY ? String.valueOf(m_Bound) : "?";
        if (smallerThan)
            return m_Type.getName() + " < " + value;
        else
            return m_Type.getName() + " >= " + value;
    }


    @Override
    public String getPythonString() {
        return getString();
    }


    @Override
    public boolean hasConstants() {
        return m_Bound != Double.NEGATIVE_INFINITY;
    }


    @Override
    public boolean equals(NodeTest test) {
        if (m_Type != test.getType())
            return false;
        ClusRuleConstraintInduceTest ntest = (ClusRuleConstraintInduceTest) test;
        return m_Bound == ntest.m_Bound && ntest.smallerThan == smallerThan;
    }


    @Override
    public int hashCode() {
        long v = Double.doubleToLongBits(m_Bound);
        return m_Type.getIndex() + (int) (v ^ (v >>> 32));
    }


    @Override
    public int numericPredictWeighted(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return hasUnknownBranch() ? ClusNode.UNK : UNKNOWN;
        }
        else {
            return ((value < m_Bound && smallerThan) || (value >= m_Bound && !smallerThan)) ? ClusNode.YES : ClusNode.NO; // editeuh
        }
    }


    @Override
    public int predictWeighted(DataTuple tuple) {
        double val = m_Type.getNumeric(tuple);
        return numericPredictWeighted(val);
    }
}
