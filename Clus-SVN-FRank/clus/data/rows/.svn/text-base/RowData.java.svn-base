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

package clus.data.rows;

import jeans.resource.ResourceInfo;
import jeans.util.sort.*;
import jeans.util.compound.*;

import java.util.*;
import java.io.*;

import clus.util.*;
import clus.algo.tdidt.ClusNode;
import clus.data.ClusData;
import clus.data.io.ClusReader;
import clus.data.type.*;
import clus.model.test.*;
import clus.error.*;
import clus.selection.*;
import clus.statistic.*;

/**
 * Multiple rows (tuples) of data.
 * One row (DataTuple) is one instance of the data with target and description attributes.
 */
public class RowData extends ClusData implements MSortable, Serializable {

	public int m_Index;
	public ClusSchema m_Schema;
	public DataTuple[] m_Data;

	public RowData(ClusSchema schema) {
		m_Schema = schema;
	}

	public RowData(ClusSchema schema, int size) {
		m_Schema = schema;
		resizeEmpty(size);
	}

	public RowData(RowData data) {
		this(data.m_Data, data.getNbRows());
		m_Schema = data.m_Schema;
	}

	public RowData(Object[] data, int size) {
		m_Data = new DataTuple[size];
		System.arraycopy(data, 0, m_Data, 0, size);
		setNbRows(size);
	}

	public RowData(ArrayList list, ClusSchema schema) {
		m_Schema = schema;
		setFromList(list);
	}

	public void setFromList(ArrayList list) {
		m_Data = new DataTuple[list.size()];
		for (int i = 0; i < list.size(); i++) {
			m_Data[i] = (DataTuple)list.get(i);
		}
		setNbRows(list.size());
	}

	public String toString(){
		return toString("");
	}

	public String toString(String prefix){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getNbRows(); i++) {
			sb.append(prefix);
			sb.append(getTuple(i).toString()+"\n");
		}
		return sb.toString();
	}
	
	public String getSummary(){
		return getSummary("");
	}

	public String getSummary(String prefix){
		StringBuffer sb = new StringBuffer();
		double[] avg,min,max,stddev;
		DataTuple temp = getTuple(0);
		int nda= temp.getSchema().getNbNumericDescriptiveAttributes();
		avg = new double[nda];
		min = new double[nda];
		max = new double[nda];
		stddev = new double[nda];
		Arrays.fill(avg, 0);
		Arrays.fill(stddev, 0);
		Arrays.fill(min, Double.MAX_VALUE);
		Arrays.fill(max, Double.MIN_VALUE);
		int nbrows = getNbRows();
		for (int i = 0; i < nbrows ; i++) {
			temp = getTuple(i);
			ClusSchema schema = temp.getSchema();
			for (int j = 0; j < schema.getNbNumericDescriptiveAttributes(); j++) {
				ClusAttrType type = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE)[j];
				double tmpvalue = type.getNumeric(temp);
				if (tmpvalue>max[j]){
					max[j]=tmpvalue;
				}
				if (tmpvalue<min[j]){
					min[j]=tmpvalue;
				}
				avg[j]+=tmpvalue;
				stddev[j]+=tmpvalue*tmpvalue;
			}
		}
		for (int i=0;i<nda;i++){
			avg[i]/=nbrows;
			stddev[i]=(stddev[i]-nbrows*avg[i]*avg[i])/nbrows;
			stddev[i] = Math.sqrt(stddev[i]);
			min[i]=Math.round(min[i]*100)/100.0;
			max[i]=Math.round(max[i]*100)/100.0;
			avg[i]=Math.round(avg[i]*100)/100.0;
			stddev[i]=Math.round(stddev[i]*100)/100.0;
		}

		sb.append(prefix+"Min: "+Arrays.toString(min)+"\n");
		sb.append(prefix+"Max: "+Arrays.toString(max)+"\n");
		sb.append(prefix+"Avg: "+Arrays.toString(avg)+"\n");
		sb.append(prefix+"StdDev: "+Arrays.toString(stddev)+"\n");
		return sb.toString();
	}

	public ArrayList toArrayList() {
		ArrayList array = new ArrayList();
		addTo(array);
		return array;
	}

	public void addTo(ArrayList array) {
		for (int i = 0; i < getNbRows(); i++) {
			array.add(getTuple(i));
		}
	}

	public DataTuple createTuple() {
		return new DataTuple(m_Schema);
	}

	public static RowData readData(String fname, ClusSchema schema) throws ClusException, IOException {
		schema.addIndices(ClusSchema.ROWS);
		ClusReader reader = new ClusReader(fname, schema.getSettings());
		RowData data = schema.createNormalView().readData(reader, schema);
		reader.close();
		return data;
	}

	public ClusData cloneData() {
		RowData res = new RowData(m_Schema, m_NbRows);
		System.arraycopy(m_Data, 0, res.m_Data, 0, m_NbRows);
		return res;
	}

	public RowData shallowCloneData() {
		RowData res = new RowData(m_Schema, m_NbRows);
		for (int i = 0; i < m_NbRows; i++) {
			res.setTuple(m_Data[i].cloneTuple(), i);
		}
		return res;
	}

	public ClusData deepCloneData() {
		RowData res = new RowData(m_Schema, m_NbRows);
		for (int i = 0; i < m_NbRows; i++) {
			res.setTuple(m_Data[i].deepCloneTuple(), i);
		}
		return res;
	}

	public final ClusSchema getSchema() {
		return m_Schema;
	}

	public void setSchema(ClusSchema schema) {
		m_Schema = schema;
	}
	
	public void sortSparse(NumericAttrType at, RowDataSortHelper helper) {
		int nbmiss = 0, nbzero = 0, nbother = 0;
		helper.resize(m_NbRows+1);
		DataTuple[] missing = helper.missing;
		DataTuple[] zero = helper.zero;
		DoubleObject[] other = helper.other;
		for (int i = 0; i < m_NbRows; i++) {
			double data = at.getNumeric(m_Data[i]);
			if (data == 0.0) {
				zero[nbzero++] = m_Data[i];
			} else if (data == NumericAttrType.MISSING) {
				missing[nbmiss++] = m_Data[i];
			} else if (data > 0.0) {
				other[nbother++].set(data, m_Data[i]);
			} else {
				System.err.println("Sparse attribute has negative value!");
				System.exit(-1);
			}
		}
		// long start_time = ResourceInfo.getTime();
		// Arrays.sort(other, 0, nbother);
		MSorter.quickSort(helper, 0, nbother);
		// long done_time = ResourceInfo.getTime();
		// System.out.println("Sorting took: "+((double)(done_time-start_time)/1000.0)+" sec");		
		int pos = 0;
		for (int i = 0; i < nbmiss; i++) {
			m_Data[pos++] = missing[i];
		}
		for (int i = 0; i < nbother; i++) {
			m_Data[pos++] = (DataTuple)other[i].getObject();
		}
		for (int i = 0; i < nbzero; i++) {
			m_Data[pos++] = zero[i];
		}
	}

	public void sort(NumericAttrType at) {
		m_Index = at.getArrayIndex();
		MSorter.quickSort(this, 0, m_NbRows);
	}

	public double getDouble(int i) {
		return m_Data[i].getDoubleVal(m_Index);
	}

	public boolean compare(int i, int j) {
		return m_Data[i].getDoubleVal(m_Index) < m_Data[j].getDoubleVal(m_Index);
	}

	public void swap(int i, int j) {
		DataTuple temp = m_Data[i];
		m_Data[i] = m_Data[j];
		m_Data[j] = temp;
	}

	public DataTuple findTupleByKey(String key_value) {
		ClusAttrType[] key = getSchema().getAllAttrUse(ClusAttrType.ATTR_USE_KEY);
		if (key.length > 0) {
			ClusAttrType key_attr = key[0];
			for (int i = 0; i < getNbRows(); i++) {
				DataTuple tuple = getTuple(i);
				if (key_attr.getString(tuple).equals(key_value)) return tuple;
			}
		}
		return null;
	}

	// Does not change original distribution
	public ClusData selectFrom(ClusSelection sel) {
		int nbsel = sel.getNbSelected();
		RowData res = new RowData(m_Schema, nbsel);
		if (sel.supportsReplacement()) {
			for (int i = 0; i < nbsel; i++) {
				res.setTuple(m_Data[sel.getIndex(i)], i);
			}
		} else {
			int s_subset = 0;
			for (int i = 0; i < m_NbRows; i++) {
				if (sel.isSelected(i)) res.setTuple(m_Data[i], s_subset++);
			}
		}
		return res;
	}

	public ClusData select(ClusSelection sel) {
		int s_data = 0;
		int s_subset = 0;
		DataTuple[] old = m_Data;
		int nbsel = sel.getNbSelected();
		m_Data = new DataTuple[m_NbRows - nbsel];
		RowData res = new RowData(m_Schema, nbsel);
		for (int i = 0; i < m_NbRows; i++) {
			if (sel.isSelected(i)) res.setTuple(old[i], s_subset++);
			else setTuple(old[i], s_data++);
		}
		m_NbRows -= nbsel;
		return res;
	}

	public void update(ClusSelection sel) {
		int s_data = 0;
		DataTuple[] old = m_Data;
		int nbsel = sel.getNbSelected();
		m_Data = new DataTuple[nbsel];
		for (int i = 0; i < m_NbRows; i++) {
			if (sel.isSelected(i)) {
				DataTuple nt = old[i].multiplyWeight(sel.getWeight(i));
				setTuple(nt, s_data++);
			}
		}
		m_NbRows = nbsel;
	}

	public final double getSumWeights() {
		double sum = 0.0;
		for (int i = 0; i < m_NbRows; i++) {
			sum += m_Data[i].getWeight();
		}
		return sum;
	}

	public final boolean containsFold(DataTuple tuple, int[] folds) {
		for (int i = 0; i < folds.length; i++) {
			if (tuple.m_Folds[folds[i]] > 0) return true;
		}
		return false;
	}

	public final void optimize2(int[] folds) {
		int nbsel = 0;
		int s_data = 0;
		for (int i = 0; i < m_NbRows; i++) {
			if (containsFold(m_Data[i], folds)) nbsel++;
		}
		DataTuple[] old = m_Data;
		m_Data = new DataTuple[nbsel];
		for (int i = 0; i < m_NbRows; i++) {
			if (containsFold(old[i], folds)) {
				setTuple(old[i], s_data++);
			}
		}
		m_NbRows = nbsel;
	}

	public void insert(ClusData data, ClusSelection sel) {
		int s_data = 0;
		int s_subset = 0;
		DataTuple[] old = m_Data;
		RowData other = (RowData)data;
		resizeEmpty(m_NbRows + sel.getNbSelected());
		for (int i = 0; i < m_NbRows; i++) {
			if (sel.isSelected(i)) setTuple(other.getTuple(s_subset++), i);
			else setTuple(old[s_data++], i);
		}
	}

	public final RowData getFoldData(int fold) {
		int idx = 0;
		int nbsel = 0;
		// Count examples for fold
		for (int i = 0; i < m_NbRows; i++)
			if (m_Data[i].getIndex() != fold) nbsel++;
		// Select examples
		RowData res = new RowData(m_Schema, nbsel);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			if (tuple.getIndex() != fold) res.setTuple(tuple, idx++);
		}
		return res;
	}

	/**
	 * Only used in efficient XVal code
	 * TODO Could be a bug: changeWeight -> multiplyWeight
	 * @return
	 */
	public final RowData getFoldData2(int fold) {
		int idx = 0;
		int nbsel = 0;
		// Count examples for fold
		for (int i = 0; i < m_NbRows; i++)
			if (m_Data[i].m_Folds[fold] != 0) nbsel++;
		// Select examples
		RowData res = new RowData(m_Schema, nbsel);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			int factor = m_Data[i].m_Folds[fold];
			if (factor != 0) {
				DataTuple t2 = factor == 1 ? tuple : tuple.changeWeight(tuple.getWeight() * factor);
				res.setTuple(t2, idx++);
			}
		}
		return res;
	}

	public final RowData getOVFoldData(int fold) {
		int idx = 0;
		int nbsel = 0;
		// Count examples for fold
		for (int i = 0; i < m_NbRows; i++) {
			int efold = m_Data[i].m_Index;
			if (efold != -1) {
				if (efold != fold) nbsel++;
			} else {
				if (Arrays.binarySearch(m_Data[i].m_Folds, fold) >= 0) nbsel++;
			}
		}
		// Select examples
		RowData res = new RowData(m_Schema, nbsel);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			int efold = tuple.m_Index;
			if (efold != -1) {
				if (efold != fold) res.setTuple(tuple, idx++);
			} else {
				if (Arrays.binarySearch(m_Data[i].m_Folds, fold) >= 0) res.setTuple(tuple, idx++);
			}
		}
		return res;
	}

	public final boolean checkData() {
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			if (tuple.m_Index == -1 && tuple.m_Folds == null) return false;
			if (tuple.m_Index != -1 && tuple.m_Folds != null) return false;
		}
		return true;
	}

	public final DataTuple getTuple(int i) {
		return m_Data[i];
	}

	public final void setTuple(DataTuple tuple, int i) {
		m_Data[i] = tuple;
	}



	public final RowData applyWeighted(NodeTest test, int branch) {
		int nb = 0;
		for (int i = 0; i < m_NbRows; i++) {
			int pred = test.predictWeighted(m_Data[i]);
			if (pred == branch || pred == NodeTest.UNKNOWN) nb++;
		}
		int idx = 0;
		RowData res = new RowData(m_Schema, nb);
		double prop = test.getProportion(branch);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			int pred = test.predictWeighted(tuple);
			if (pred == branch) {
				res.setTuple(tuple, idx++);
			} else if (pred == NodeTest.UNKNOWN) {
				DataTuple ntuple = tuple.multiplyWeight(prop);
				res.setTuple(ntuple, idx++);
			}
		}
		return res;
	}

	public final RowData applyAllAlternativeTests(NodeTest orig, NodeTest[] tests, NodeTest[] opposites, int branch) {
		// remark: we assume there are only 2 branches
		ArrayList<DataTuple> al = new ArrayList<DataTuple>(); // will be used to create final RowData
		for (int i = 0; i < m_NbRows; i++) {
			int[] counts = new int[2]; // to count the number of yes and no predictions
			//original test
			int pred = orig.predictWeighted(m_Data[i]); // pred is 0 or 1
			counts[pred]++;
			// alternative tests
			for (int t=0; t<tests.length; t++)
			{
				pred = tests[t].predictWeighted(m_Data[i]); // pred is 0 or 1
				counts[pred]++;
			}
			// opposite alternatives tests
			for (int t=0; t<opposites.length; t++)
			{
				pred = opposites[t].predictWeighted(m_Data[i]); // pred is 0 or 1
				counts[(pred+1)%2]++; // 0->1 and 1->0
			}
			int finalpred;
			int totalcounts = counts[0]+counts[1];
			//counts[0] = yes branch, counts[1] = no branch
			if (counts[0] >= 0.5 * totalcounts) finalpred = 0;
			else finalpred = 1;
			if (finalpred == branch) {
				al.add(m_Data[i]);
			}
		}
		RowData res = new RowData(al, m_Schema);
		return res;
	}
	
	public final RowData apply(NodeTest test, int branch) {
		int nb = 0;
		for (int i = 0; i < m_NbRows; i++) {
			int pred = test.predictWeighted(m_Data[i]);
			if (pred == branch) nb++;
		}
		int idx = 0;
		RowData res = new RowData(m_Schema, nb);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			int pred = test.predictWeighted(tuple);
			if (pred == branch) res.setTuple(tuple, idx++);
		}
		return res;
	}
	
	public final RowData applyConstraint(ClusRuleConstraintInduceTest test, int branch) {
		boolean order = test.isSmallerThanTest();
		if(order)
			return applyConstraintTrue(test,branch);
		else
			return applyConstraintFalse(test,branch);
		
	}

	private RowData applyConstraintTrue(ClusRuleConstraintInduceTest test, int branch) {
		int nb = 0;
		for (int i = 0; i < m_NbRows; i++) {
			int pred = test.predictWeighted(m_Data[i]);
			if (pred != branch) nb++;
		}
		int idx = 0;
		RowData res = new RowData(m_Schema, nb);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			int pred = test.predictWeighted(tuple);
			if (pred != branch) res.setTuple(tuple, idx++);
		}
		return res;	
	}
	
	private RowData applyConstraintFalse(ClusRuleConstraintInduceTest test, int branch) {
		int nb = 0;
		for (int i = 0; i < m_NbRows; i++) {
			int pred = test.predictWeighted(m_Data[i]);
			if (pred == branch) nb++;
		}
		int idx = 0;
		RowData res = new RowData(m_Schema, nb);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			int pred = test.predictWeighted(tuple);
			if (pred == branch) res.setTuple(tuple, idx++);
		}
		return res;	
	}

	public final RowData applySoft(SoftTest test, int branch) {
		int nb = 0;
		for (int i = 0; i < m_NbRows; i++)
			nb += test.softPredictNb(m_Data[i], branch);
		int idx = 0;
		RowData res = new RowData(m_Schema, nb);
		for (int i = 0; i < m_NbRows; i++)
			idx = test.softPredict(res, m_Data[i], idx, branch);
		return res;
	}

	public final RowData applySoft2(SoftTest test, int branch) {
		int nb = 0;
		for (int i = 0; i < m_NbRows; i++)
			nb += test.softPredictNb2(m_Data[i], branch);
		int idx = 0;
		RowData res = new RowData(m_Schema, nb);
		for (int i = 0; i < m_NbRows; i++)
			idx = test.softPredict2(res, m_Data[i], idx, branch);
		return res;
	}

	public void resize(int nbrows) {
		m_Data = new DataTuple[nbrows];
		for (int i = 0; i < nbrows; i++) m_Data[i] = new DataTuple(m_Schema);
		m_NbRows = nbrows;
	}

	public void resizeEmpty(int nbrows) {
		m_Data = new DataTuple[nbrows];
		m_NbRows = nbrows;
	}

	public void showDebug(ClusSchema schema) {
		System.out.println("Data: "+m_NbRows+" Size: "+m_Data.length);
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = getTuple(i);
			if (tuple == null) {
				System.out.println("? ");
			} else {
				ClusAttrType at = schema.getAttrType(0);
				System.out.println(at.getString(tuple));
/*				if (tuple.m_Index == -1) {
					System.out.println(" Folds: "+MyIntArray.print(tuple.m_Folds));
				} else {
					System.out.println(" LO: "+tuple.m_Index);
				}*/
			}
		}
		System.out.println();
	}

	public void attach(ClusNode node) {
	}

	public void calcTotalStatBitVector(ClusStatistic stat) {
		stat.setSDataSize(getNbRows());
		calcTotalStat(stat);
		stat.optimizePreCalc(this);
	}

	public void calcTotalStat(ClusStatistic stat) {
		for (int i = 0; i < m_NbRows; i++) {
			stat.updateWeighted(m_Data[i], i);
		}
	}

	public final void calcPosAndMissStat(NodeTest test, int branch, ClusStatistic pos, ClusStatistic miss) {
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			int pred = test.predictWeighted(tuple);
			if (pred == branch) {
				pos.updateWeighted(m_Data[i], i);
			} else if (pred == NodeTest.UNKNOWN) {
				miss.updateWeighted(m_Data[i], i);
			}
		}
	}

	public final boolean isSoft() {
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			if (tuple.m_Index == -1) return true;
		}
		return false;
	}

	public final void calcXValTotalStat(ClusStatistic[] tot) {
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			tot[tuple.getIndex()].updateWeighted(tuple, i);
		}
	}

	public final void calcXValTotalStat(ClusStatistic[] tot, ClusStatistic[] extra) {
		for (int i = 0; i < extra.length; i++) extra[i].reset();
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			if (tuple.m_Index != -1) {
				tot[tuple.m_Index].updateWeighted(tuple, i);
			} else {
				int[] folds = tuple.m_Folds;
				for (int j = 0; j < folds.length; j++)
					extra[folds[j]].updateWeighted(tuple, i);
			}
		}
	}

	public void calcError(ClusNode node, ClusErrorList par) {
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = getTuple(i);
			ClusStatistic stat = node.predictWeighted(tuple);
			par.addExample(tuple, stat);
		}
	}

	public void preprocess(int pass, DataPreprocs pps) throws ClusException {
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = m_Data[i];
			pps.preproc(pass, tuple);
		}
	}

	public final void showTable() {
		for (int i = 0; i < m_Schema.getNbAttributes(); i++) {
			ClusAttrType type = m_Schema.getAttrType(i);
			if (i != 0) System.out.print(",");
			System.out.print(type.getName());
		}
		System.out.println();
		for (int i = 0; i < m_NbRows; i++) {
			DataTuple tuple = getTuple(i);
			for (int j = 0; j < m_Schema.getNbAttributes(); j++) {
				ClusAttrType type = m_Schema.getAttrType(j);
				if (j != 0) System.out.print(",");
				System.out.print(type.getString(tuple));
			}
			System.out.println();
		}
	}

	public double[] getNumeric(int idx) {
		return m_Data[idx].m_Doubles;
	}

	public int[] getNominal(int idx) {
		return m_Data[idx].m_Ints;
	}

	public MemoryTupleIterator getIterator() {
		return new MemoryTupleIterator(this);
	}

	public void addIndices() {
		for (int i = 0; i < m_NbRows; i++) {
			m_Data[i].setIndex(i);
		}
	}

	public boolean equals(RowData d){
		if (d.getNbRows() != getNbRows()) {
			return false;
		}
		if (!m_Schema.equals(m_Schema)) {
			return false;
		}
		for (int i = 0; i < getNbRows(); i++) {
			if (!d.getTuple(i).equals(getTuple(i))) return false;
		}
		return false;
	}

	public void add(DataTuple tuple) {
		setNbRows(getNbRows() + 1);
		DataTuple[] newdata;
		if(m_Data != null)
			newdata = Arrays.copyOf(m_Data, getNbRows());
		else
			newdata = new DataTuple[getNbRows()];
		newdata[getNbRows() - 1] = tuple.cloneTuple();
		m_Data = newdata;
	}
	
	public void addAll(RowData data1, RowData data2) {
		int size = data1.getNbRows() + data2.getNbRows();
		setNbRows(size);
		m_Data = new DataTuple[size];
		for (int i = 0; i < data1.getNbRows(); i++) {
			m_Data[i] = data1.getTuple(i).cloneTuple();
		}
		for (int i = 0; i < data2.getNbRows(); i++) {
			data2.getTuple(i).cloneTuple();
			m_Data[i+data1.getNbRows()] = data2.getTuple(i).cloneTuple();
		}
	}
	
	public void add(RowData data1) {
		DataTuple[] oldData = m_Data;
		int size = getNbRows() + data1.getNbRows();
		setNbRows(size);
		m_Data = new DataTuple[size];
		for (int i = 0; i < oldData.length; i++) {
			m_Data[i] = oldData[i].cloneTuple();
		}
		for (int i = 0; i < data1.getNbRows(); i++) {
			data1.getTuple(i).cloneTuple();
			m_Data[i+oldData.length] = data1.getTuple(i).cloneTuple();
		}
	}

	/**
	 * Create a random sample with replacement of this RowData.
	 * Uses the ClusRandom.RANDOM_SAMPLE random generator
	 * 
	 * 
	 * @param N The size of the random subset
	 * @return If N > 0:  a new RowData containing the random sample of size N
	 * 		   If N == 0: a copy of this RowData object (i.e. no sampling)
	 * @throws IllegalArgumentException if N < 0
	 */
	public RowData sample(int N) {
		if(N < 0) throw new IllegalArgumentException("N should be larger than or equal to zero");
		int nbRows = getNbRows();
		if(N == 0) return new RowData(this);
		ArrayList<DataTuple> res = new ArrayList<DataTuple>();
		// sample with replacement
		int i;
		for(int size = 0; size < N; size++) {
			i = ClusRandom.nextInt(ClusRandom.RANDOM_SAMPLE,nbRows);
			res.add(getTuple(i));
		}
		return new RowData(res, getSchema().cloneSchema());
	}
	
	
	public RowData sampleWeighted(Random random) {
		return sampleWeighted(random, getNbRows());
	}

	public RowData sampleWeighted(Random random, int N) {
		double[] weight_acc = new double[getNbRows()];
		weight_acc[0] = getTuple(0).getWeight();
		for (int i = 1; i < getNbRows(); i++) {
			DataTuple tuple = getTuple(i);
			weight_acc[i] = weight_acc[i-1] + tuple.getWeight();
		}
		double tot_w = weight_acc[getNbRows()-1];
		ArrayList res = new ArrayList();
		for (int i = 0; i < N; i++) {
			double rnd = random.nextDouble()*tot_w;
			// Index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1).
			// The insertion point is defined as the point at which the key would be inserted into the list:
			// the index of the first element greater than the key, or list.size(), if all elements in the list
			// are less than the specified key
			int loc = Arrays.binarySearch(weight_acc, rnd);
			if (loc < 0) {
				loc = -loc - 1;
			}
			DataTuple restuple = getTuple(loc).changeWeight(1.0);
			res.add(restuple);
		}
		return new RowData(res, getSchema());
	}
	
	// returns an array list with for a random target, for each target label a random example
	/*public ArrayList getPertTuples() {
		Random rnd = new Random();
		HashMap<Integer,ArrayList> classhash = new HashMap();
		
		ClusAttrType[] targets = getSchema().getTargetAttributes();
		// pick one of the random targets
		int rndtarget = rnd.nextInt(targets.length);
		ClusAttrType target = targets[rndtarget];
		int targettype = target.getValueType();
		if (targettype != ClusAttrType.VALUE_TYPE_INT) {
			System.out.println("Target not nominal: ClusAttrType " + targettype);
		}
		else {
			for (int i = 0; i < getNbRows(); i++) {
				DataTuple dt = getTuple(i);			
				int classid = target.getNominal(dt);
				
				if (classhash.containsKey(classid)) {
					((ArrayList)classhash.get(classid)).add(i);
				}
				else {
					ArrayList<Integer> al = new ArrayList<Integer>();
					al.add(i);
					classhash.put(classid,al);
				}
			}
		}
		Set<Integer> keys = classhash.keySet();
		Iterator<Integer> keysit = keys.iterator();
		//System.out.println("Printing " + getNbRows() + " tuples");
		ArrayList<Integer> result = new ArrayList<Integer>();
		while (keysit.hasNext())
		{
			Integer key = keysit.next();
			ArrayList tuples = (ArrayList) classhash.get(key);
			
			// pick random element of ArrayList
			
			int rndint = rnd.nextInt(tuples.size());
			result.add((Integer)tuples.get(rndint));
			
			//Object[] intarray = tuples.toArray();
			//System.out.print("Class " + key + ": ");
			//for (int j=0;j<intarray.length;j++) {
			//	System.out.print(intarray[j].toString()+" ");
			//}
			//System.out.println();
		}
		return result;
	}*/
	
}
