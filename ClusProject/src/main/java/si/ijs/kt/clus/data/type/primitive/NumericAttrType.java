/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.data.type.primitive;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import si.ijs.kt.clus.algo.kNN.distance.valentin.NumericStatistic;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.cols.ColTarget;
import si.ijs.kt.clus.data.cols.attribute.ClusAttribute;
import si.ijs.kt.clus.data.cols.attribute.NumericTarget;
import si.ijs.kt.clus.data.io.ClusReader;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import si.ijs.kt.clus.ext.hierarchicalmtr.ClusHMTRNode;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.io.ClusSerializable;


/**
 * Attribute of numeric (continuous) value.
 */
public class NumericAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public final static double MISSING = Double.POSITIVE_INFINITY;

    protected boolean m_Sparse;
    private NumericStatistic m_StatNumeric;


    public NumericAttrType(String name) {
        super(name);
    }


    // new for knn
    public void setStatistic(NumericStatistic stat) {
        m_StatNumeric = stat;
    }


    public NumericStatistic getStatistic() {
        return m_StatNumeric;
    }


    @Override
    public ClusAttrType cloneType() {
        NumericAttrType at = new NumericAttrType(m_Name);
        cloneType(at);
        at.m_Sparse = m_Sparse;
        return at;
    }


    @Override
    public boolean isSparse() {
        return m_Sparse;
    }


    public void setSparse(boolean sparse) {
        m_Sparse = sparse;
    }


    @Override
    public AttributeType getAttributeType() {
        return AttributeType.Numeric;
    }


    @Override
    public String getTypeName() {
        return getAttributeType().getName();
    }


    @Override
    public ValueType getValueType() {
        return ValueType.Double;
    }


    /*
     * public boolean addToData(ColData data) {
     * if (!super.addToData(data)) {
     * data.addAttribute(new NumericAttribute(this));
     * }
     * return true;
     * }
     */
    @Override
    public int getMaxNbStats() {
        // Positive statistic and missing value statistic
        return 2;
    }


    @Override
    public String getString(DataTuple tuple) {
        double val = this.getNumeric(tuple);
        // FIXME - SOON - STATUS_KEY attribute :-)
        if (getStatus().equals(Status.Key)) {
            return String.valueOf((int) val);
        }
        else {
            return val == MISSING ? "?" : String.valueOf(val);
        }
    }


    @Override
    public boolean isMissing(DataTuple tuple) {
        return tuple.getDoubleVal(m_ArrayIndex) == MISSING;
    }


    @Override
    public double getNumeric(DataTuple tuple) {
        return tuple.getDoubleVal(m_ArrayIndex);
    }


    public void setNumeric(DataTuple tuple, double value) {
        tuple.setDoubleVal(value, m_ArrayIndex);
    }


    @Override
    public int compareValue(DataTuple t1, DataTuple t2) {
        double v1 = t1.getDoubleVal(m_ArrayIndex);
        double v2 = t2.getDoubleVal(m_ArrayIndex);
        if (v1 == v2)
            return 0;
        return v1 > v2 ? 1 : -1;
    }


    @Override
    public ClusAttribute createTargetAttr(ColTarget target) {
        return new NumericTarget(target, this, getArrayIndex());
    }


    @Override
    public void writeARFFType(PrintWriter wrt) throws ClusException {
        wrt.print("numeric");
    }


    @Override
    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }


    @Override
    public void setToMissing(DataTuple tuple) {
        setNumeric(tuple, MISSING);
    }

    public class MySerializable extends ClusSerializable {

        public int m_NbZero, m_NbNeg, m_NbTotal;


        @Override
        public boolean read(ClusReader data, DataTuple tuple) throws IOException {
            if (!data.readNoSpace())
                return false;
            double val = data.getFloat();
            tuple.setDoubleVal(val, getArrayIndex());
            if (val == MISSING) {
                incNbMissing();
                m_NbZero++;
            }
            if (val == 0.0) {
                m_NbZero++;
            }
            else if (val < 0.0) {
                m_NbNeg++;
            }
            m_NbTotal++;
            return true;
        }


        @Override
        public boolean calculateHMTRAttribute(ClusReader data, DataTuple tuple, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy) throws IOException {

            int key = schema.getKeyAttribute().length;
            int desc = schema.getDescriptiveAttributes().length;
            int dis = schema.getDisabled().getTotal();

            int nbNominalTargets = schema.getNbNominalTargetAttributes();
            if (nbNominalTargets > 0)
                throw new IOException("Nominal attributes used as targets in Hierarchical Multi-Target Regression!");

            ClusAttrType[] targets = tuple.getSchema().getTargetAttributes();

            double val = Double.NaN;

            int ind = m_Index - key - dis - desc;
            String name = targets[ind].getName();

            List<ClusHMTRNode> nodes = hmtrHierarchy.getNodes();

            val = calcHMTR(nodes, name, tuple);

            if (Double.isNaN(val)) {
                System.err.println("Either error calculating aggregate or certain value is missing");
                val = MISSING;
            }
            // if(getSettings().getGeneral().getVerbose() > 0) System.out.println("CALCULATING HMTR AGGREGATE - row:
            // "+(data.getRow()+1)+" name: "+name + ", value: " + val + " aggregation function is:
            // "+Settings.HMTR_AGGS[getSettings().getHMTRAggregation().getValue()]);

            tuple.setDoubleVal(val, getArrayIndex());
            if (val == MISSING) {
                incNbMissing();
                m_NbZero++;
            }
            if (val == 0.0) {
                m_NbZero++;
            }
            else if (val < 0.0) {
                m_NbNeg++;
            }
            m_NbTotal++;
            return true;
        }


        @Override
        public boolean readHMTRAttribute(ClusReader data, DataTuple tuple, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy, String line) throws IOException {

            int key = schema.getKeyAttribute().length;
            int desc = schema.getDescriptiveAttributes().length;
            int dis = schema.getDisabled().getTotal();
            int tar = schema.getTargetAttributes().length;

            int pos = m_Index - key - dis - desc - tar + schema.getNbHMTRAttributes();

            String[] lineElements = line.split(",");

            if (data.getRow() == 232) {
                System.out.println();
            }

            double val = MISSING;
            if (!lineElements[pos].contains("?")) {
                val = Double.parseDouble(lineElements[pos]);
            }
            else {
                val = MISSING;
            }

            //ClusAttrType[] targets = tuple.getSchema().getTargetAttributes();

            // int ind = m_Index - key - dis - desc;
            // String name = targets[ind].getName();

            if (Double.isNaN(val))
                throw new IOException("Error reading HMTR aggregate from dump! Aggregation function is: " + getSettings().getHMTR().getHMTRAggregationName());

            // if(getSettings().getGeneral().getVerbose() > 0) System.out.println("READING HMTR AGGREGATE - row:
            // "+(data.getRow()+1)+" name: "+name + ", value: " + val + " aggregation function is:
            // "+Settings.HMTR_AGGS[getSettings().getHMTRAggregation().getValue()]);

            tuple.setDoubleVal(val, getArrayIndex());
            if (val == MISSING) {
                incNbMissing();
                m_NbZero++;
            }
            if (val == 0.0) {
                m_NbZero++;
            }
            else if (val < 0.0) {
                m_NbNeg++;
            }
            m_NbTotal++;
            return true;
        }


        private double calcHMTR(List<ClusHMTRNode> nodes, String name, DataTuple tuple) throws IOException {

            for (ClusHMTRNode node : nodes) {
                if (node.getName().equals(name)) {
                    if (!node.isAggregate())
                        throw new IOException("Attribute " + node.getName() + " is  not aggregate!");

                    List<ClusHMTRNode> children = node.getChildren(); // children of the node

                    double sum = 0;
                    List<Double> values = new ArrayList<Double>();

                    for (ClusHMTRNode child : children) {

                        // String n = child.getName();
                        if (child.isAggregate()) {
                            double res = calcHMTR(nodes, child.getName(), tuple);
                            sum += res;
                            values.add(res);
                        }
                        else {
                            double res = getHMTRValue(tuple, child.getName());
                            sum += res;
                            values.add(res);
                        }
                    }

                    Collections.sort(values);
                    switch (getSettings().getHMTR().getHMTRAggregation()) {
                        case SUM:
                            return sum;
                        case AVG:
                            return sum / children.size();
                        case MEDIAN:
                            int middle = values.size() / 2;
                            if (values.size() % 2 == 1) {
                                return values.get(middle);
                            }
                            else {
                                return ((values.get(middle - 1) + values.get(middle)) / 2.0);
                            }
                        case MIN:
                            return Collections.min(values);
                        case MAX:
                            return Collections.max(values);
                        case AND:
                            for (int i = 0; i < values.size(); i++) {
                                double val = values.get(i);
                                if (!(val == 0) && !(val == 1))
                                    throw new IOException("Value " + val + " is not 1 or 0 while using AND or OR");
                                if (val == 0)
                                    return 0;
                            }
                            return 1;
                        case OR:
                            for (int i = 0; i < values.size(); i++) {
                                double val = values.get(i);
                                if (!(val == 0) && !(val == 1))
                                    throw new IOException("Value " + val + " is not 1 or 0 while using AND or OR");
                                if (val == 1)
                                    return 1;
                            }
                            return 0;
                        case COUNT:
                            return values.size();
                        case VAR:
                            double sumDiffsSquared = 0.0;
                            double avg = sum / children.size();
                            for (int i = 0; i < values.size(); i++) {
                                double diff = values.get(i) - avg;
                                diff *= diff;
                                sumDiffsSquared += diff;
                            }
                            return sumDiffsSquared / (values.size());
                        case STDEV:
                            double mean = sum / children.size();
                            double temp = 0;
                            for (int i = 0; i < values.size(); i++) {
                                double val = values.get(i);
                                double squrDiffToMean = Math.pow(val - mean, 2);
                                temp += squrDiffToMean;
                            }
                            double meanOfDiffs = temp / (values.size());
                            return Math.sqrt(meanOfDiffs);
                        case ZERO:
                            return 0;
                        case ONE:
                            return 1;
                    }
                }
            }
            return Double.NaN;
        }


        public double getHMTRValue(DataTuple tuple, String name) {

            double value = Double.NaN;

            ClusAttrType[] targets = tuple.getSchema().getTargetAttributes();

            for (ClusAttrType target : targets) {
                if (target.getName().equals(name)) {
                    int i = target.getArrayIndex();
                    value = tuple.getDoubleVal(i);
                    break;
                }

            }

            return value;
        }


        @Override
        public void term(ClusSchema schema) {
            // System.out.println("Attribute: "+getName()+" "+((double)100.0*m_NbZero/m_NbTotal));
            if (m_NbNeg == 0 && m_NbZero > m_NbTotal * 5 / 10) {
                setSparse(true);
            }
        }
    }


    @Override
    public boolean isNumeric() {
        return true;
    }

}
