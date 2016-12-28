/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package clus.data.type;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import clus.ext.hierarchicalmtr.ClassHMTRHierarchy;
import clus.ext.hierarchicalmtr.ClassHMTRNode;
import clus.io.*;
import clus.main.Settings;
import clus.util.*;
import clus.data.rows.*;
import clus.data.cols.*;
import clus.data.cols.attribute.*;
import clus.data.io.ClusReader;
import jeans.util.IntervalCollection;

/**
 * Attribute of numeric (continuous) value.
 */
public class NumericAttrType extends ClusAttrType {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int THIS_TYPE = 1;
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

/*	public boolean addToData(ColData data) {
		if (!super.addToData(data)) {
			data.addAttribute(new NumericAttribute(this));
		}
		return true;
	}
*/
	public int getMaxNbStats() {
		// Positive statistic and missing value statistic
		return 2;
	}

	public String getString(DataTuple tuple) {
		double val = this.getNumeric(tuple);
// FIXME - SOON - STATUS_KEY attribute :-)
		if (getStatus() == STATUS_KEY) {
			return String.valueOf((int)val);
		} else {
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

	public int compareValue(DataTuple t1, DataTuple t2) {
		double v1 = t1.m_Doubles[m_ArrayIndex];
		double v2 = t2.m_Doubles[m_ArrayIndex];
		if (v1 == v2) return 0;
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
			if (!data.readNoSpace()) return false;
			double val = data.getFloat();
			tuple.setDoubleVal(val, getArrayIndex());
			if (val == MISSING) {
				incNbMissing();
				m_NbZero++;
			}
			if (val == 0.0) {
				m_NbZero++;
			} else if (val < 0.0) {
				m_NbNeg++;
			}
			m_NbTotal++;
			return true;
		}

		public boolean calculateHMTRAttribute(ClusReader data, DataTuple tuple, ClusSchema schema, ClassHMTRHierarchy hmtrHierarchy) throws IOException {


			int nbHMTRTargets = schema.getNbHierarchicalMTR();
            IntervalCollection key = schema.m_Key;

           	int nbAttributes = schema.getNbAttributes()-schema.getNbNominalDescriptiveAttributes();
            if(!key.isEmpty()) nbAttributes--;


			int nbNominalTargets = schema.getNbNominalTargetAttributes();
			if (nbNominalTargets>0) throw new IOException("Nominal attributes used as targets in Hierarchical Multi-Target Regression!");

            int NbAll = schema.getNbAttributes();

            int ai = getArrayIndex();
            double value;
            String name;

            ClusAttrType[] targets = tuple.getSchema().getTargetAttributes();


//			for (int i = NbAll-nbAttributes; i<nbAttributes;i++){
//
//               value = tuple.getDoubleVal(i);
//               name = targets[i-(schema.getNbAttributes()-nbAttributes)].getName();
//
//                System.out.println();
//
//            }


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

            double val = Double.NaN;

            List<ClassHMTRNode> nodes = hmtrHierarchy.getNodes();

            name = targets[ai-(schema.getNbAttributes()-nbAttributes)].getName();

            for (ClassHMTRNode node : nodes){

                if(node.getName().equals(name)){
                    if(!node.isAggregate())throw new IOException("Attribute "+node.getName()+" is  not aggregate!");



                    System.out.println(name);
                }

            }


		    switch (getSettings().getHMTRAggregation().getValue()){

                case 0:



                    val = 999.0;
                    break;
		        case 1: val = 999.0; break;
                case 2: val = 999.0; break;
                case 3: val = 999.0; break;
                case 4: val = 999.0; break;
                case 5: val = 999.0; break;
                case 6: val = 999.0; break;
                case 7: val = 999.0; break;
                case 8: val = 999.0; break;
                case 9: val = 999.0; break;
            }

            if (Double.isNaN(val)) throw new IOException("Error calculating HMTR aggregate! Aggregation function is: "+getSettings().getHMTRAggregation().getValue());

            System.out.println("CALCULATING HMTR AGGREGATE: " + val + " aggregation function is: "+getSettings().getHMTRAggregation().getValue());

			tuple.setDoubleVal(val, getArrayIndex());
			if (val == MISSING) {
				incNbMissing();
				m_NbZero++;
			}
			if (val == 0.0) {
				m_NbZero++;
			} else if (val < 0.0) {
				m_NbNeg++;
			}
			m_NbTotal++;
			return true;
		}

		public void term(ClusSchema schema) {
			// System.out.println("Attribute: "+getName()+" "+((double)100.0*m_NbZero/m_NbTotal));
			if (m_NbNeg == 0 && m_NbZero > m_NbTotal*5/10) {
				setSparse(true);
			}
		}
	}

}

