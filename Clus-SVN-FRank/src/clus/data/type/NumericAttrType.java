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

package clus.data.type;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clus.data.cols.ColTarget;
import clus.data.cols.attribute.ClusAttribute;
import clus.data.cols.attribute.NumericTarget;
import clus.data.io.ClusReader;
import clus.data.rows.DataTuple;
import clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import clus.ext.hierarchicalmtr.ClusHMTRNode;
import clus.io.ClusSerializable;
import clus.main.Settings;
import clus.util.ClusException;


/**
 * Attribute of numeric (continuous) value.
 */
public class NumericAttrType extends ClusAttrType {

    public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

    public final static int THIS_TYPE = NUMERIC_ATR_TYPE;
    public final static String THIS_TYPE_NAME = "Numeric";

    public final static double MISSING = Double.POSITIVE_INFINITY;

    protected boolean m_Sparse;


    public NumericAttrType(String name) {
        super(name);
    }


    public ClusAttrType cloneType() {
        NumericAttrType at = new NumericAttrType(m_Name);
        cloneType(at);
        at.m_Sparse = m_Sparse;
        return at;
    }


    public boolean isSparse() {
        return m_Sparse;
    }


    public void setSparse(boolean sparse) {
        m_Sparse = sparse;
    }


    public int getTypeIndex() {
        return THIS_TYPE;
    }


    public String getTypeName() {
        return THIS_TYPE_NAME;
    }


    public int getValueType() {
        return VALUE_TYPE_DOUBLE;
    }


    /*
     * public boolean addToData(ColData data) {
     * if (!super.addToData(data)) {
     * data.addAttribute(new NumericAttribute(this));
     * }
     * return true;
     * }
     */
    public int getMaxNbStats() {
        // Positive statistic and missing value statistic
        return 2;
    }


    public String getString(DataTuple tuple) {
        double val = this.getNumeric(tuple);
        // FIXME - SOON - STATUS_KEY attribute :-)
        if (getStatus() == STATUS_KEY) {
            return String.valueOf((int) val);
        }
        else {
            return val == MISSING ? "?" : String.valueOf(val);
        }
    }


    public boolean isMissing(DataTuple tuple) {
        return tuple.m_Doubles[m_ArrayIndex] == MISSING;
    }


    public double getNumeric(DataTuple tuple) {
        return tuple.getDoubleVal(m_ArrayIndex);
    }


    public void setNumeric(DataTuple tuple, double value) {
        tuple.setDoubleVal(value, m_ArrayIndex);
    }

	@Override
	public void setToMissing(DataTuple tuple) {
		setNumeric(tuple, MISSING);
	}

    public int compareValue(DataTuple t1, DataTuple t2) {
        double v1 = t1.m_Doubles[m_ArrayIndex];
        double v2 = t2.m_Doubles[m_ArrayIndex];
        if (v1 == v2)
            return 0;
        return v1 > v2 ? 1 : -1;
    }


    public ClusAttribute createTargetAttr(ColTarget target) {
        return new NumericTarget(target, this, getArrayIndex());
    }


    public void writeARFFType(PrintWriter wrt) throws ClusException {
        wrt.print("numeric");
    }


    public ClusSerializable createRowSerializable() throws ClusException {
        return new MySerializable();
    }

    public class MySerializable extends ClusSerializable {

        public int m_NbZero, m_NbNeg, m_NbTotal;


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
            //if(Settings.VERBOSE > 0) System.out.println("CALCULATING HMTR AGGREGATE - row: "+(data.getRow()+1)+" name: "+name + ", value: " + val + " aggregation function is: "+Settings.HMTR_AGGS[getSettings().getHMTRAggregation().getValue()]);

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


        public boolean readHMTRAttribute(ClusReader data, DataTuple tuple, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy, String line) throws IOException {

            int key = schema.getKeyAttribute().length;
            int desc = schema.getDescriptiveAttributes().length;
            int dis = schema.getDisabled().getTotal();
            int tar = schema.getTargetAttributes().length;

            int pos = m_Index - key - dis - desc - tar + schema.getNbHMTR();

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

            ClusAttrType[] targets = tuple.getSchema().getTargetAttributes();

            int ind = m_Index - key - dis - desc;
            String name = targets[ind].getName();

            if (Double.isNaN(val))
                throw new IOException("Error reading HMTR aggregate from dump! Aggregation function is: " + Settings.HMTR_AGGS[getSettings().getHMTRAggregation().getValue()]);

            // if(Settings.VERBOSE > 0) System.out.println("READING HMTR AGGREGATE - row: "+(data.getRow()+1)+" name: "+name + ", value: " + val + " aggregation function is: "+Settings.HMTR_AGGS[getSettings().getHMTRAggregation().getValue()]);

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

                        String n = child.getName();
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
                    //            HMTR_AGG_SUM = 0;
                    //            HMTR_AGG_AVG = 1;
                    //            HMTR_AGG_MEDIAN = 2;
                    //            HMTR_AGG_MIN = 3;
                    //            HMTR_AGG_MAX = 4;
                    //            HMTR_AGG_AND = 5;
                    //            HMTR_AGG_OR = 6;
                    //            HMTR_AGG_COUNT = 7;
                    //            HMTR_AGG_VAR = 8;
                    //            HMTR_AGG_STDEV = 9;
                    //            HMTR_AGG_ZERO = 10
                    //            HMTR_AGG_ONE = 11
                    Collections.sort(values);
                    switch (getSettings().getHMTRAggregation().getValue()) {
                        case 0:
                            return sum;
                        case 1:
                            return sum / children.size();
                        case 2:
                            int middle = values.size() / 2;
                            if (values.size() % 2 == 1) {
                                return values.get(middle);
                            }
                            else {
                                return ((values.get(middle - 1) + values.get(middle)) / 2.0);
                            }
                        case 3:
                            return Collections.min(values);
                        case 4:
                            return Collections.max(values);
                        case 5:
                            for (int i = 0; i < values.size(); i++) {
                                double val = values.get(i);
                                if (!(val == 0) && !(val == 1))
                                    throw new IOException("Value " + val + " is not 1 or 0 while using AND or OR");
                                if (val == 0)
                                    return 0;
                            }
                            return 1;
                        case 6:
                            for (int i = 0; i < values.size(); i++) {
                                double val = values.get(i);
                                if (!(val == 0) && !(val == 1))
                                    throw new IOException("Value " + val + " is not 1 or 0 while using AND or OR");
                                if (val == 1)
                                    return 1;
                            }
                            return 0;
                        case 7:
                            return values.size();
                        case 8:
                            double sumDiffsSquared = 0.0;
                            double avg = sum / children.size();
                            for (int i = 0; i < values.size(); i++) {
                                double diff = values.get(i) - avg;
                                diff *= diff;
                                sumDiffsSquared += diff;
                            }
                            return sumDiffsSquared / (double) (values.size());
                        case 9:
                            double mean = sum / children.size();
                            double temp = 0;
                            for (int i = 0; i < values.size(); i++) {
                                double val = values.get(i);
                                double squrDiffToMean = Math.pow(val - mean, 2);
                                temp += squrDiffToMean;
                            }
                            double meanOfDiffs = (double) temp / (double) (values.size());
                            return Math.sqrt(meanOfDiffs);
                        case 10:
                            return 0;
                        case 11:
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
                    int i = target.m_ArrayIndex;
                    value = tuple.getDoubleVal(i);
                    break;
                }

            }

            return value;
        }


        public void term(ClusSchema schema) {
            // System.out.println("Attribute: "+getName()+" "+((double)100.0*m_NbZero/m_NbTotal));
            if (m_NbNeg == 0 && m_NbZero > m_NbTotal * 5 / 10) {
                setSparse(true);
            }
        }
    }


    public boolean isNumeric() {
        return true;
    }

}
