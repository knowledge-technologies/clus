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

import jeans.util.*;

import java.io.*;
import java.util.*;

import clus.io.DummySerializable;
import clus.main.*;
import clus.model.ClusModel;
import clus.data.ClusData;
import clus.data.io.ClusView;
import clus.data.rows.*;
import clus.util.*;
import clus.selection.*;

public class ClusSchema implements Serializable {

	public final static long serialVersionUID = 1L;

	public final static int ROWS = 0;
	public final static int COLS = 1;

	protected boolean m_IsSparse;
	protected String m_Relation;
	protected int m_NbAttrs;
	protected int m_NbInts, m_NbDoubles, m_NbObjects;
	protected ArrayList m_Attr = new ArrayList();
	protected ClusAttrType[][] m_AllAttrUse;
	protected NominalAttrType[][] m_NominalAttrUse;
	protected NumericAttrType[][] m_NumericAttrUse;
	protected TimeSeriesAttrType[][] m_TimeSeriesAttrUse;
	protected ClusAttrType[] m_NonSparse;
	protected Settings m_Settings;
	protected IndexAttrType m_TSAttr;
	protected IntervalCollection m_Target = IntervalCollection.EMPTY;
	protected IntervalCollection m_Disabled = IntervalCollection.EMPTY;
	protected IntervalCollection m_Clustering = IntervalCollection.EMPTY;
	protected IntervalCollection m_Descriptive = IntervalCollection.EMPTY;
	protected IntervalCollection m_Key = IntervalCollection.EMPTY;
	protected int[] m_NbVt;

	public ClusSchema(String name) {
		m_Relation = name;
	}

	public ClusSchema(String name, String descr) {
		m_Relation = name;
		addFromString(descr);
	}

	public void setSettings(Settings sett) {
		m_Settings = sett;
	}

	public void initializeSettings(Settings sett) throws ClusException, IOException {
		setSettings(sett);
		setTestSet(-1); /* Support ID for XVAL attribute later on? */
		setTarget(new IntervalCollection(sett.getTarget()));
		setDisabled(new IntervalCollection(sett.getDisabled()));
		setClustering(new IntervalCollection(sett.getClustering()));
		setDescriptive(new IntervalCollection(sett.getDescriptive()));
		setKey(new IntervalCollection(sett.getKey()));
		updateAttributeUse();
		addIndices(ClusSchema.ROWS);
	}

	public void initialize() throws ClusException, IOException {
		updateAttributeUse();
		addIndices(ClusSchema.ROWS);
	}

	public Settings getSettings() {
		return m_Settings;
	}

	public final String getRelationName() {
		return m_Relation;
	}

	public final void setRelationName(String name) {
		m_Relation = name;
	}

	public ClusSchema cloneSchema() {
		ClusSchema result = new ClusSchema(getRelationName());
		result.setSettings(getSettings());
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType attr = getAttrType(i);
			ClusAttrType copy = attr.cloneType();
			result.addAttrType(copy);
		}
		return result;
	}

/***********************************************************************
 * Methods for retrieving attribute types                              *
 ***********************************************************************/

	public final int getNbAttributes() {
		return m_NbAttrs;
	}

	public final ClusAttrType getAttrType(int idx) {
		return (ClusAttrType)m_Attr.get(idx);
	}

	public final ClusAttrType getAttrType(String name) {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType attr = (ClusAttrType)m_Attr.get(j);
			if (name.equals(attr.getName())) return attr;
		}
		return null;
	}

	public final ClusAttrType[] getAllAttrUse(int attruse) {
		return m_AllAttrUse[attruse];
	}

	public final int getNbAllAttrUse(int attruse) {
		return m_AllAttrUse[attruse].length;
	}

	public TimeSeriesAttrType[] getTimeSeriesAttrUse(int attruse) {
		return m_TimeSeriesAttrUse[attruse];
	}

	public final NominalAttrType[] getNominalAttrUse(int attruse) {
		return m_NominalAttrUse[attruse];
	}

	public final int getNbNominalAttrUse(int attruse) {
		return m_NominalAttrUse[attruse].length;
	}

	/**
	 * Returns all the numeric attributes that are of the given type.
	 * @param attruse The use type of attributes as defined in ClusAttrType. For example ATTR_USE_TARGET.
	 * @return An array of numeric attributes.
	 */
	public final NumericAttrType[] getNumericAttrUse(int attruse) {
		return m_NumericAttrUse[attruse];
	}

	public final int getNbNumericAttrUse(int attruse) {
		return m_NumericAttrUse[attruse].length;
	}

	public final ClusAttrType[] getDescriptiveAttributes() {
		return m_AllAttrUse[ClusAttrType.ATTR_USE_DESCRIPTIVE];
	}
	
	public final ClusAttrType[] getKeyAttribute() {
		return m_AllAttrUse[ClusAttrType.ATTR_USE_KEY];
	}

	public final int getNbDescriptiveAttributes() {
		return getNbAllAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
	}
	
	public final ClusAttrType[] getTargetAttributes() {
		return m_AllAttrUse[ClusAttrType.ATTR_USE_TARGET];
	}

	public final int getNbTargetAttributes() {
		return getNbAllAttrUse(ClusAttrType.ATTR_USE_TARGET);
	}

	public final int getNbNominalDescriptiveAttributes() {
		return getNbNominalAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
	}

	public final int getNbNumericDescriptiveAttributes() {
		return getNbNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
	}

	public final int getNbNominalTargetAttributes() {
		return getNbNominalAttrUse(ClusAttrType.ATTR_USE_TARGET);
	}

	public final int getNbNumericTargetAttributes() {
		return getNbNumericAttrUse(ClusAttrType.ATTR_USE_TARGET);
	}

	public final int getNbInts() {
		return m_NbInts;
	}

	public final int getNbDoubles() {
		return m_NbDoubles;
	}

	public final int getNbObjects() {
		return m_NbObjects;
	}

	public final boolean hasAttributeType(int attruse, int attrtype) {
		ClusAttrType[] all = getAllAttrUse(attruse);
		for (int i = 0; i < all.length; i++) {
			if (all[i].getTypeIndex() == attrtype) return true;
		}
		return false;
	}

	public ClusAttrType getLastNonDisabledType() {
		int nb = getNbAttributes()-1;
		while (nb >= 0 && getAttrType(nb).isDisabled()) {
			nb--;
		}
		if (nb >= 0) return getAttrType(nb);
		else return null;
	}

/***********************************************************************
 * Methods for adding attributes to the schema                         *
 ***********************************************************************/

	public final void addAttrType(ClusAttrType attr) {
		m_Attr.add(attr);
		attr.setIndex(m_NbAttrs++);
		attr.setSchema(this);
	}

	public final void setAttrType(ClusAttrType attr, int idx) {
		m_Attr.set(idx, attr);
		attr.setIndex(idx);
		attr.setSchema(this);
	}

	public final void addFromString(String descr) {
		StringTokenizer tokens = new StringTokenizer(descr, "[]");
		while (tokens.hasMoreTokens()) {
			String type = tokens.nextToken();
			String name = tokens.hasMoreTokens() ? tokens.nextToken() : "";
			if (type.equals("f")) {
				addAttrType(new NumericAttrType(name));
			}
		}
	}

/***********************************************************************
 * Methods concerning missing values                                   *
 ***********************************************************************/

	public final boolean hasMissing() {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType attr = (ClusAttrType)m_Attr.get(j);
			if (attr.hasMissing()) return true;
		}
		return false;
	}

	public final double getTotalInputNbMissing() {
		int nb_miss = 0;
		ClusAttrType[] attrs = getDescriptiveAttributes();
		for (int j = 0; j < attrs.length; j++) {
			ClusAttrType at = (ClusAttrType)attrs[j];
			nb_miss += at.getNbMissing();
		}
		return (double)nb_miss;
	}

/***********************************************************************
 * Methods for working with interval collections of attributes         *
 ***********************************************************************/

	public final IntervalCollection getTarget() {
		return m_Target;
	}

	public final IntervalCollection getDisabled() {
		return m_Disabled;
	}

	public final IntervalCollection getClustering() {
		return m_Clustering;
	}

	public final IntervalCollection getDescriptive() {
		return m_Descriptive;
	}

	public final void setTarget(IntervalCollection coll) {
		m_Target = coll;

	}

	public final void setDisabled(IntervalCollection coll) {
		m_Disabled = coll;
	}

	public final void setClustering(IntervalCollection coll) {
		m_Clustering = coll;
	}

	public final void setDescriptive(IntervalCollection coll) {
		m_Descriptive = coll;
	}

	public final void setKey(IntervalCollection coll) {
		m_Key = coll;
	}

	public final void updateAttributeUse() throws ClusException, IOException {
		boolean[] keys = new boolean[getNbAttributes()+1];
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType type = getAttrType(i);
			if (type.getStatus() == ClusAttrType.STATUS_KEY) keys[type.getIndex()+1] = true;
		}
		m_Key.add(keys);
		if (m_Target.isDefault()) {
			// By default, the last non-disabled and non-key attribute is the target
			m_Target.clear();
			boolean[] bits = new boolean[getNbAttributes()+1];
			m_Disabled.toBits(bits);
			int target = bits.length-1;
			while (target >= 0 && (bits[target] || keys[target])) target--;
			if (target > 0) m_Target.addInterval(target, target);
		} else {
			// Target and all other settings have precedence over disabled
			m_Disabled.subtract(m_Target);
		}
		if (m_Clustering.isDefault()) {
			// By default same as target attributes
			m_Clustering.copyFrom(m_Target);
		} else {
			m_Disabled.subtract(m_Clustering);
		}
		if (m_Descriptive.isDefault()) {
			// By default all attributes that are not target and not disabled
			m_Descriptive.clear();
			m_Descriptive.addInterval(1,getNbAttributes());
			m_Descriptive.subtract(m_Target);
			m_Descriptive.subtract(m_Disabled);
			m_Descriptive.subtract(m_Key);
		} else {
			m_Disabled.subtract(m_Descriptive);
		}
		m_Disabled.subtract(m_Key);
		checkRange(m_Key, "key");
		checkRange(m_Disabled, "disabled");
		checkRange(m_Target, "target");
		checkRange(m_Clustering, "clustering");
		checkRange(m_Descriptive, "descriptive");
		setStatusAll(ClusAttrType.STATUS_NORMAL);
		setStatus(m_Disabled, ClusAttrType.STATUS_DISABLED, true);
		setStatus(m_Target, ClusAttrType.STATUS_TARGET, true);
		setStatus(m_Clustering, ClusAttrType.STATUS_CLUSTER_NO_TARGET, false);
		setStatus(m_Key, ClusAttrType.STATUS_KEY, true);
		setDescriptiveAll(false);
		setDescriptive(m_Descriptive, true);
		setClusteringAll(false);
		setClustering(m_Clustering, true);
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType at = getAttrType(i);
			at.initializeBeforeLoadingData();
		}
	}

	public void clearAttributeStatusClusteringAndTarget() {
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType at = getAttrType(i);
			if (at.getStatus() != ClusAttrType.STATUS_DISABLED && at.getStatus() != ClusAttrType.STATUS_KEY) {
				at.setStatus(ClusAttrType.STATUS_NORMAL);
			}
		}
		setClusteringAll(false);
	}

	public void initDescriptiveAttributes() {
		setDescriptiveAll(false);
		setDescriptive(m_Descriptive, true);
	}

	public final void checkRange(IntervalCollection coll, String type) throws ClusException {
		if (coll.getMinIndex() < 0) throw new ClusException("Range for "+type+" attributes goes below zero: '"+coll+"'");
		if (coll.getMaxIndex() > getNbAttributes()) throw new ClusException("Range for "+type+" attributes: '"+coll+"' out of range (there are only "+getNbAttributes()+" attributes)");
	}

	public final void setStatus(IntervalCollection coll, int status, boolean force) {
		coll.reset();
		while (coll.hasMoreInts()) {
			ClusAttrType at = getAttrType(coll.nextInt()-1);
			if (force || at.getStatus() == ClusAttrType.STATUS_NORMAL) {
				at.setStatus(status);
			}
		}
	}

	public final void setStatusAll(int status) {
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType at = getAttrType(i);
			at.setStatus(status);
		}
	}

	public final void setDescriptive(IntervalCollection coll, boolean descr) {
		coll.reset();
		while (coll.hasMoreInts()) {
			ClusAttrType at = getAttrType(coll.nextInt()-1);
			at.setDescriptive(descr);
		}
	}

	public final void setDescriptiveAll(boolean descr) {
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType at = getAttrType(i);
			at.setDescriptive(descr);
		}
	}

	public final void setClustering(IntervalCollection coll, boolean clust) {
		coll.reset();
		while (coll.hasMoreInts()) {
			ClusAttrType at = getAttrType(coll.nextInt()-1);
			at.setClustering(clust);
		}
	}

	public final void setClusteringAll(boolean clust) {
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType at = getAttrType(i);
			at.setClustering(clust);
		}
	}

	public final void addIndices(int type) throws ClusException {
		if (type == COLS) {
// FIXME: COLS mode currently disabled :-(
//			m_NbTarNum = makeSpecialIndex(NumericAttrType.class, ClusAttrType.STATUS_TARGET);
//			m_NbTarNom = makeSpecialIndex(NominalAttrType.class, ClusAttrType.STATUS_TARGET);
			addColsIndex();
		} else {
			addRowsIndex();
		}
	}

	public final void showDebug() {
		System.out.println("Nb ints: "+getNbInts());
		System.out.println("Nb double: "+getNbDoubles());
		System.out.println("Nb obj: "+getNbObjects());
		System.out.println("Idx   Name                          Descr Status    Ref   Type             Sparse Missing");
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = (ClusAttrType)m_Attr.get(j);
			System.out.print(StringUtils.printInt(j+1, 6));
			System.out.print(StringUtils.printStrMax(at.getName(), 29));
			if (at.isDescriptive()) System.out.print(" Yes   ");
			else System.out.print(" No    ");
			switch (at.getStatus()) {
				case ClusAttrType.STATUS_NORMAL:
					System.out.print("          ");
					break;
				case ClusAttrType.STATUS_DISABLED:
					System.out.print("Disabled  ");
					break;
				case ClusAttrType.STATUS_TARGET:
					System.out.print("Target    ");
					break;
				case ClusAttrType.STATUS_CLUSTER_NO_TARGET:
					System.out.print("Cluster   ");
					break;
				case ClusAttrType.STATUS_KEY:
					System.out.print("Key       ");
					break;
				default:
					System.out.print("Error     ");
					break;
			}
			System.out.print(StringUtils.printInt(at.getArrayIndex(), 6));
			System.out.print(StringUtils.printStr(at.getTypeName(), 16));
			if (at instanceof NumericAttrType) {
				if (((NumericAttrType)at).isSparse()) System.out.print(" Yes");
				else System.out.print(" No ");
			} else {
				System.out.print(" ?  ");
			}
			System.out.print("    ");
			System.out.print(StringUtils.printStr(ClusFormat.TWO_AFTER_DOT.format(at.getNbMissing()), 8));
			System.out.println();
		}
	}

	// Used for enabling multi-score
	public final boolean isRegression() {
		return getNbNumericTargetAttributes() > 0;
	}

	public final void setReader(boolean start_stop) {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType attr = (ClusAttrType)m_Attr.get(j);
			if (attr.getStatus() != ClusAttrType.STATUS_DISABLED) attr.setReader(start_stop);
		}
	}

	public final void getPreprocs(DataPreprocs pps, boolean single) {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType attr = (ClusAttrType)m_Attr.get(j);
			if (attr.getStatus() != ClusAttrType.STATUS_DISABLED) attr.getPreprocs(pps, single);
		}
	}

	public final int getMaxNbStats() {
		int max = 0;
		ClusAttrType[] descr = getAllAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
		for (int i = 0; i < descr.length; i++) {
			max = Math.max(descr[i].getMaxNbStats(), max);
		}
		return max;
	}

	public final XValMainSelection getXValSelection(ClusData data) throws ClusException {
		if (m_TSAttr == null) {
			return new XValRandomSelection(data.getNbRows(), getSettings().getXValFolds());
		} else {
			return new XValDataSelection(m_TSAttr);
		}
	}

	public final void setTestSet(int id) {
		if (id != -1) {
			System.out.println("Setting test set ID: "+id);
			ClusAttrType type = (ClusAttrType)m_Attr.get(id);
			m_Attr.set(id, m_TSAttr = new IndexAttrType(type.getName()));
		}
	}

	public final void attachModel(ClusModel model) throws ClusException {
		HashMap table = buildAttributeHash();
		model.attachModel(table);
	}

	public final HashMap buildAttributeHash() throws ClusException {
		HashMap hash = new HashMap();
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = (ClusAttrType)m_Attr.get(j);
			if (hash.containsKey(at.getName())) {
				throw new ClusException("Duplicate attribute name: '"+at.getName()+"'");
			} else {
				hash.put(at.getName(), at);
			}
		}
		return hash;
	}

	public void initializeFrom(ClusSchema schema) throws ClusException {
		if (schema.isSparse())
			this.ensureSparse();

		int this_nb = getNbAttributes();
		int other_nb = schema.getNbAttributes();
		if (other_nb > this_nb) {
			throw new ClusException("To few attributes in data set "+this_nb+" < "+other_nb);
		}
		for (int i = 0; i < other_nb; i++) {
			ClusAttrType this_type = getAttrType(i);
			ClusAttrType other_type = schema.getAttrType(i);
			if (!this_type.getName().equals(other_type.getName())) {
				throw new ClusException("Attribute names do not align: '"+other_type.getName()+
						                "' expected at position "+(i+1)+" but found '"+this_type.getName()+"'");
			}
			if (this_type.getClass() != other_type.getClass()) {
				throw new ClusException("Attribute types do not match for '"+other_type.getName()+
		                                "' expected '"+other_type.getClass().getName()+"' but found '"+this_type.getClass().getName()+"'");
			}
			this_type.initializeFrom(other_type);
		}
	}

	private void addColsIndex() {
		int idx = 0;
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = (ClusAttrType)m_Attr.get(j);
			if (at.getStatus() == ClusAttrType.STATUS_NORMAL) at.setArrayIndex(idx++);
		}
	}

	public static ClusAttrType[] vectorToAttrArray(ArrayList list) {
		ClusAttrType[] res = new ClusAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (ClusAttrType)list.get(i);
		}
		return res;
	}

	public static NominalAttrType[] vectorToNominalAttrArray(ArrayList list) {
		NominalAttrType[] res = new NominalAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (NominalAttrType)list.get(i);
		}
		return res;
	}

	public static NumericAttrType[] vectorToNumericAttrArray(ArrayList list) {
		NumericAttrType[] res = new NumericAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (NumericAttrType)list.get(i);
		}
		return res;
	}
	public static TimeSeriesAttrType[] vectorToTimeSeriesAttrArray(ArrayList list){
		TimeSeriesAttrType[] res = new TimeSeriesAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (TimeSeriesAttrType)list.get(i);
		}
		return res;
	}

	public ArrayList collectAttributes(int attruse, int attrtype) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType type = getAttrType(i);
			if (attrtype == ClusAttrType.THIS_TYPE || attrtype == type.getTypeIndex()) {
				switch (attruse) {
					case ClusAttrType.ATTR_USE_ALL:
						if (type.getStatus() != ClusAttrType.STATUS_DISABLED && type.getStatus() != ClusAttrType.STATUS_KEY) {
							result.add(type);
						}
						break;
					case ClusAttrType.ATTR_USE_CLUSTERING:
						if (type.isClustering()) {
							result.add(type);
						}
						break;
					case ClusAttrType.ATTR_USE_DESCRIPTIVE:
						if (type.isDescriptive()) {
							result.add(type);
						}
						break;
					case ClusAttrType.ATTR_USE_TARGET:
						if (type.getStatus() == ClusAttrType.STATUS_TARGET) {
							result.add(type);
						}
						break;
					case ClusAttrType.ATTR_USE_KEY:
						if (type.getStatus() == ClusAttrType.STATUS_KEY) {
							result.add(type);
						}
						break;
				}
			}
		}
		return result;
	}

	public boolean isSparse() {
		return m_IsSparse;
	}
	
	public void setSparse() {
		m_IsSparse = true;
	}

	public ClusAttrType[] getNonSparseAttributes() {
		return m_NonSparse;
	}
	
	public void ensureSparse() {
		if (!m_IsSparse) {
			int nbSparse = 0;
			ArrayList<ClusAttrType> nonSparse = new ArrayList<ClusAttrType>(); 
			for (int i = 0; i < getNbAttributes(); i++) {
				ClusAttrType type = getAttrType(i);
				if (type.isDescriptive() && type.getTypeIndex() == NumericAttrType.THIS_TYPE) {
					SparseNumericAttrType nt = new SparseNumericAttrType((NumericAttrType)type);
					nt.setStatus(type.getStatus()); //********************
					nt.setDescriptive(true);
//					nt.setClustering(true); //******************** //CV: added because of sparse Pert(?), removed because method ClusStatManager.check() gave error (2 types set in hierarchical context)
					setAttrType(nt, i);
					nbSparse++;
				} else {
					nonSparse.add(type);
				}
			}
			m_NonSparse = vectorToAttrArray(nonSparse);
			System.out.println("Number of sparse attributes: "+nbSparse);
			addRowsIndex();
			m_IsSparse = true;
		}
	}

	public void printInfo() {
		if (getSettings().getVerbose() >= 1) {
			System.out.println("Space required by nominal attributes: " + m_NbVt[ClusAttrType.VALUE_TYPE_INT]*4 + " bytes/tuple regular, " + m_NbVt[ClusAttrType.VALUE_TYPE_BITWISEINT]*4+" bytes/tuple bitwise");
		}
	}

	protected void addRowsIndex() {
		// Allocate attributes to arrays m_Ints, m_Doubles, m_Objects
		m_NbVt = new int[ClusAttrType.NB_VALUE_TYPES];
		int bitPosition = 0; // for BitwiseNominalAttrType
		int nbBitwise = 0;
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = (ClusAttrType)m_Attr.get(j);
			int vtype = at.getValueType();
			if (vtype == ClusAttrType.VALUE_TYPE_NONE || at.getStatus() == ClusAttrType.STATUS_DISABLED) {
				at.setArrayIndex(-1);
			} else {
				if (vtype != ClusAttrType.VALUE_TYPE_BITWISEINT) {
					int sidx = m_NbVt[vtype]++;
					at.setArrayIndex(sidx);
				} else { //vtype == ClusAttrType.VALUE_TYPE_BITWISEINT
					nbBitwise++;
					BitwiseNominalAttrType bat = (BitwiseNominalAttrType)at;
					int nextBitPosition = bitPosition + bat.getNbBits();
					if (nextBitPosition > Integer.SIZE) {
						// too many bits needed to fit in current int
						m_NbVt[vtype]++;
						int sidx = m_NbVt[vtype];
						bat.setArrayIndex(sidx);
						bat.setBitPosition(0);
						bitPosition = bat.getNbBits();
					} else {
						int sidx = m_NbVt[vtype];
						bat.setArrayIndex(sidx);
						bat.setBitPosition(bitPosition);
						bitPosition = nextBitPosition;
					}
				}
			}
		}
		if (nbBitwise>0) m_NbVt[ClusAttrType.VALUE_TYPE_BITWISEINT]++;
		m_NbInts = Math.max(m_NbVt[ClusAttrType.VALUE_TYPE_INT], m_NbVt[ClusAttrType.VALUE_TYPE_BITWISEINT]); // only one of them will be different from 0
		m_NbDoubles = m_NbVt[ClusAttrType.VALUE_TYPE_DOUBLE];
		m_NbObjects = m_NbVt[ClusAttrType.VALUE_TYPE_OBJECT];
		// Collect attributes into arrays m_Allattruse, m_Nominalattruse, m_Numericattruse
		// Sorted in order that they occur in the .arff file (to be consistent with weight vector order)
		m_AllAttrUse = new ClusAttrType[ClusAttrType.NB_ATTR_USE][];
		m_NominalAttrUse = new NominalAttrType[ClusAttrType.NB_ATTR_USE][];
		m_NumericAttrUse = new NumericAttrType[ClusAttrType.NB_ATTR_USE][];
		m_TimeSeriesAttrUse = new TimeSeriesAttrType[ClusAttrType.NB_ATTR_USE][];
		for (int attruse = ClusAttrType.ATTR_USE_ALL; attruse <= ClusAttrType.ATTR_USE_KEY; attruse++) {
			m_AllAttrUse[attruse] = vectorToAttrArray(collectAttributes(attruse, ClusAttrType.THIS_TYPE));
			m_NominalAttrUse[attruse] = vectorToNominalAttrArray(collectAttributes(attruse, NominalAttrType.THIS_TYPE));
			m_NumericAttrUse[attruse] = vectorToNumericAttrArray(collectAttributes(attruse, NumericAttrType.THIS_TYPE));
			m_TimeSeriesAttrUse[attruse] = vectorToTimeSeriesAttrArray(collectAttributes(attruse, TimeSeriesAttrType.THIS_TYPE));
		}
	}

	public DataTuple createTuple() {
		return m_IsSparse ? new SparseDataTuple(this) : new DataTuple(this);
	}

	public ClusView createNormalView() throws ClusException {
		ClusView view = new ClusView();
		createNormalView(view);
		return view;
	}

	public void createNormalView(ClusView view) throws ClusException {
		int nb = getNbAttributes();
		for (int j = 0; j < nb; j++) {
			ClusAttrType at = getAttrType(j);
			int status = at.getStatus();
			if (status == ClusAttrType.STATUS_DISABLED) {
				view.addAttribute(new DummySerializable());
			} else {
				view.addAttribute(at.createRowSerializable());
			}
		}
	}

	public String toString() {
		int aidx = 0;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType type = getAttrType(i);
			if (!type.isDisabled()) {
					if (aidx != 0) buf.append(",");
					buf.append(type.getName());
					aidx++;
			}
		}
		return buf.toString();
	}
}
