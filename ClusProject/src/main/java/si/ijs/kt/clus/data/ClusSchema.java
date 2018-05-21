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

package si.ijs.kt.clus.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import si.ijs.kt.clus.data.io.ClusView;
import si.ijs.kt.clus.data.rows.DataPreprocs;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.SparseDataTuple;
import si.ijs.kt.clus.data.type.BitwiseNominalAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.ClusAttrType.Status;
import si.ijs.kt.clus.data.type.ClusAttrType.ValueType;
import si.ijs.kt.clus.data.type.primitive.IndexAttrType;
import si.ijs.kt.clus.data.type.primitive.NominalAttrType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.data.type.primitive.SparseNumericAttrType;
import si.ijs.kt.clus.data.type.primitive.TimeSeriesAttrType;
import si.ijs.kt.clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.model.ClusModel;
import si.ijs.kt.clus.selection.XValDataSelection;
import si.ijs.kt.clus.selection.XValMainSelection;
import si.ijs.kt.clus.selection.XValRandomSelection;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.format.ClusFormat;
import si.ijs.kt.clus.util.io.DummySerializable;
import si.ijs.kt.clus.util.jeans.util.IntervalCollection;
import si.ijs.kt.clus.util.jeans.util.StringUtils;

public class ClusSchema implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int ROWS = 0;
	public final static int COLS = 1;

	protected boolean m_IsSparse;
	protected String m_Relation;
	protected int m_NbAttrs;
	protected int m_NbInts, m_NbDoubles, m_NbObjects;
	protected ArrayList<ClusAttrType> m_Attr = new ArrayList<ClusAttrType>();
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
	protected IntervalCollection m_GIS = IntervalCollection.EMPTY; // daniela
	protected IntervalCollection m_Key = IntervalCollection.EMPTY;
	protected int[] m_NbVt;
	protected int m_NbHMTRAttributes;
	private ClusHMTRHierarchy m_HMTRHierarchy;

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
		setTarget(new IntervalCollection(sett.getAttribute().getTarget()));
		setDisabled(new IntervalCollection(sett.getAttribute().getDisabled()));
		setClustering(new IntervalCollection(sett.getAttribute().getClustering()));
		setDescriptive(new IntervalCollection(sett.getAttribute().getDescriptive()));
		setGIS(new IntervalCollection(sett.getAttribute().getGIS())); // daniela
		setKey(new IntervalCollection(sett.getAttribute().getKey()));

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
	 * Methods for retrieving attribute types *
	 ***********************************************************************/

	public final int getNbAttributes() {
		return m_NbAttrs;
	}

	public final ClusAttrType getAttrType(int idx) {
		return m_Attr.get(idx);
	}

	public final ClusAttrType getAttrType(String name) {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType attr = m_Attr.get(j);
			if (name.equals(attr.getName()))
				return attr;
		}
		return null;
	}

	public final ClusAttrType[] getAllAttrUse(AttributeUseType attruse) {
		return m_AllAttrUse[attruse.getIndex()];
	}

	public final int getNbAllAttrUse(AttributeUseType attruse) {
		return m_AllAttrUse[attruse.getIndex()].length;
	}

	public TimeSeriesAttrType[] getTimeSeriesAttrUse(AttributeUseType attruse) {
		return m_TimeSeriesAttrUse[attruse.getIndex()];
	}

	public final NominalAttrType[] getNominalAttrUse(AttributeUseType attruse) {
		return m_NominalAttrUse[attruse.getIndex()];
	}

	public final int getNbNominalAttrUse(AttributeUseType attruse) {
		return m_NominalAttrUse[attruse.getIndex()].length;
	}

	/**
	 * Returns all the numeric attributes that are of the given type.
	 * 
	 * @param attruse
	 *            The use type of attributes as defined in ClusAttrType. For example
	 *            ATTR_USE_TARGET.
	 * @return An array of numeric attributes.
	 */
	public final NumericAttrType[] getNumericAttrUse(AttributeUseType attruse) {
		return m_NumericAttrUse[attruse.getIndex()];
	}

	public final int getNbNumericAttrUse(AttributeUseType attruse) {
		return m_NumericAttrUse[attruse.getIndex()].length;
	}

	public final ClusAttrType[] getDescriptiveAttributes() {
		return m_AllAttrUse[AttributeUseType.Descriptive.getIndex()];
	}

	public final ClusAttrType[] getKeyAttribute() {
		return m_AllAttrUse[AttributeUseType.Key.getIndex()];
	}

	public final int getNbDescriptiveAttributes() {
		return getNbAllAttrUse(AttributeUseType.Descriptive);
	}

	public final ClusAttrType[] getTargetAttributes() {
		return m_AllAttrUse[AttributeUseType.Target.getIndex()];
	}

	public final int getNbTargetAttributes() {
		return getNbAllAttrUse(AttributeUseType.Target);
	}

	public final int getNbNominalDescriptiveAttributes() {
		return getNbNominalAttrUse(AttributeUseType.Descriptive);
	}

	public final int getNbNumericDescriptiveAttributes() {
		return getNbNumericAttrUse(AttributeUseType.Descriptive);
	}

	public final int getNbNominalTargetAttributes() {
		return getNbNominalAttrUse(AttributeUseType.Target);
	}

	public final int getNbNumericTargetAttributes() {
		return getNbNumericAttrUse(AttributeUseType.Target);
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

	public final boolean hasAttributeType(AttributeUseType attruse, AttributeType attrtype) {
		ClusAttrType[] all = getAllAttrUse(attruse);
		for (int i = 0; i < all.length; i++) {
			if (all[i].getAttributeType().equals(attrtype))
				return true;
		}
		return false;
	}

	public ClusAttrType getLastNonDisabledType() {
		int nb = getNbAttributes() - 1;
		while (nb >= 0 && getAttrType(nb).isDisabled()) {
			nb--;
		}
		if (nb >= 0)
			return getAttrType(nb);
		else
			return null;
	}

	/***********************************************************************
	 * Methods for adding attributes to the schema *
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
	 * Methods concerning missing values *
	 ***********************************************************************/

	public final boolean hasMissing() {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType attr = m_Attr.get(j);
			if (attr.hasMissing())
				return true;
		}
		return false;
	}

	public final double getTotalInputNbMissing() {
		int nb_miss = 0;
		ClusAttrType[] attrs = getDescriptiveAttributes();
		for (int j = 0; j < attrs.length; j++) {
			ClusAttrType at = attrs[j];
			nb_miss += at.getNbMissing();
		}
		return nb_miss;
	}

	/***********************************************************************
	 * Methods for working with interval collections of attributes *
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

	public void setGIS(IntervalCollection m_gis) {
		m_GIS = m_gis;
	}

	public final void setKey(IntervalCollection coll) {
		m_Key = coll;
	}

	public final void updateAttributeUse() throws ClusException, IOException {
		boolean[] keys = new boolean[getNbAttributes() + 1];
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType type = getAttrType(i);
			if (type.getStatus().equals(Status.Key))
				keys[type.getIndex() + 1] = true;
		}
		m_Key.add(keys);
		if (m_Target.isDefault()) {
			// By default, the last non-disabled and non-key attribute is the target
			m_Target.clear();
			boolean[] bits = new boolean[getNbAttributes() + 1];
			m_Disabled.toBits(bits);
			int target = bits.length - 1;
			while (target >= 0 && (bits[target] || keys[target]))
				target--;
			if (target > 0)
				m_Target.addInterval(target, target);
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
			m_Descriptive.addInterval(1, getNbAttributes());
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
		checkRange(m_GIS, "GIS"); // daniela
		checkRange(m_Clustering, "clustering");
		checkRange(m_Descriptive, "descriptive");
		setStatusAll(Status.Normal);
		setStatus(m_Disabled, Status.Disabled, true);
		setStatus(m_Target, Status.Target, true);
		setStatus(m_Clustering, Status.ClusterNoTarget, false);
		setStatus(m_GIS, Status.GIS, true); // daniela
		setStatus(m_Key, Status.Key, true);
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
			if (!at.getStatus().equals(Status.Disabled) && !at.getStatus().equals(Status.Key)) {
				at.setStatus(Status.Normal);
			}
		}
		setClusteringAll(false);
	}

	public void initDescriptiveAttributes() {
		setDescriptiveAll(false);
		setDescriptive(m_Descriptive, true);
	}

	public final void checkRange(IntervalCollection coll, String type) throws ClusException {
		if (coll.getMinIndex() < 0)
			throw new ClusException("Range for " + type + " attributes goes below zero: '" + coll + "'");
		if (coll.getMaxIndex() > getNbAttributes())
			throw new ClusException("Range for " + type + " attributes: '" + coll + "' out of range (there are only "
					+ getNbAttributes() + " attributes)");
	}

	public final void setStatus(IntervalCollection coll, Status status, boolean force) {
		coll.reset();
		while (coll.hasMoreInts()) {
			ClusAttrType at = getAttrType(coll.nextInt() - 1);
			if (force || at.getStatus().equals(Status.Normal)) {
				at.setStatus(status);
			}
		}
	}

	public final void setStatusAll(Status status) {
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType at = getAttrType(i);
			at.setStatus(status);
		}
	}

	public final void setDescriptive(IntervalCollection coll, boolean descr) {
		coll.reset();
		while (coll.hasMoreInts()) {
			ClusAttrType at = getAttrType(coll.nextInt() - 1);
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
			ClusAttrType at = getAttrType(coll.nextInt() - 1);
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
			// m_NbTarNum = makeSpecialIndex(NumericAttrType.class,
			// ClusAttrType.STATUS_TARGET);
			// m_NbTarNom = makeSpecialIndex(NominalAttrType.class,
			// ClusAttrType.STATUS_TARGET);
			addColsIndex();
		} else {
			addRowsIndex();
		}
	}

	public final void showDebug() {
		System.out.println("Nb ints: " + getNbInts());
		System.out.println("Nb double: " + getNbDoubles());
		System.out.println("Nb obj: " + getNbObjects());
		System.out.println("Idx   Name                          Descr Status    Ref   Type             Sparse Missing");
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = m_Attr.get(j);
			System.out.print(StringUtils.printInt(j + 1, 6));
			System.out.print(StringUtils.printStrMax(at.getName(), 29));
			if (at.isDescriptive())
				System.out.print(" Yes   ");
			else
				System.out.print(" No    ");
			switch (at.getStatus()) {
			case Normal:
				System.out.print("          ");
				break;
			case Disabled:
				System.out.print("Disabled  ");
				break;
			case GIS:
				System.out.print("GIS    "); // daniela
				break;
			case Target:
				System.out.print("Target    ");
				break;
			case ClusterNoTarget:
				System.out.print("Cluster   ");
				break;
			case Key:
				System.out.print("Key       ");
				break;
			default:
				System.out.print("Error     ");
				break;
			}
			System.out.print(StringUtils.printInt(at.getArrayIndex(), 6));
			System.out.print(StringUtils.printStr(at.getTypeName(), 16));
			if (at instanceof NumericAttrType) {
				if (((NumericAttrType) at).isSparse())
					System.out.print(" Yes");
				else
					System.out.print(" No ");
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
			ClusAttrType attr = m_Attr.get(j);
			if (!attr.getStatus().equals(Status.Disabled))
				attr.setReader(start_stop);
		}
	}

	public final void getPreprocs(DataPreprocs pps, boolean single) {
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType attr = m_Attr.get(j);
			if (!attr.getStatus().equals(Status.Disabled))
				attr.getPreprocs(pps, single);
		}
	}

	public final int getMaxNbStats() {
		int max = 0;
		ClusAttrType[] descr = getAllAttrUse(AttributeUseType.Descriptive);
		for (int i = 0; i < descr.length; i++) {
			max = Math.max(descr[i].getMaxNbStats(), max);
		}
		return max;
	}

	public final XValMainSelection getXValSelection(ClusData data) throws ClusException {
		if (m_TSAttr == null) {
			return new XValRandomSelection(data.getNbRows(), getSettings().getData().getXValFolds());
		} else {
			return new XValDataSelection(m_TSAttr);
		}
	}

	public final void setTestSet(int id) {
		if (id != -1) {
			System.out.println("Setting test set ID: " + id);
			ClusAttrType type = m_Attr.get(id);
			m_Attr.set(id, m_TSAttr = new IndexAttrType(type.getName()));
		}
	}

	public final void attachModel(ClusModel model) throws ClusException {
		HashMap<String, ClusAttrType> table = buildAttributeHash();
		model.attachModel(table);
	}

	public final HashMap<String, ClusAttrType> buildAttributeHash() throws ClusException {
		HashMap<String, ClusAttrType> hash = new HashMap<String, ClusAttrType>();
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = m_Attr.get(j);
			if (hash.containsKey(at.getName())) {
				throw new ClusException("Duplicate attribute name: '" + at.getName() + "'");
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
			throw new ClusException("To few attributes in data set " + this_nb + " < " + other_nb);
		}
		for (int i = 0; i < other_nb; i++) {
			ClusAttrType this_type = getAttrType(i);
			ClusAttrType other_type = schema.getAttrType(i);
			if (!this_type.getName().equals(other_type.getName())) {
				throw new ClusException("Attribute names do not align: '" + other_type.getName()
						+ "' expected at position " + (i + 1) + " but found '" + this_type.getName() + "'");
			}
			if (this_type.getClass() != other_type.getClass()) {
				throw new ClusException("Attribute types do not match for '" + other_type.getName() + "' expected '"
						+ other_type.getClass().getName() + "' but found '" + this_type.getClass().getName() + "'");
			}
			this_type.initializeFrom(other_type);
		}
	}

	private void addColsIndex() {
		int idx = 0;
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = m_Attr.get(j);
			if (at.getStatus().equals(Status.Normal))
				at.setArrayIndex(idx++);
		}
	}

	public static ClusAttrType[] vectorToAttrArray(ArrayList list) {
		ClusAttrType[] res = new ClusAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (ClusAttrType) list.get(i);
		}
		return res;
	}

	public static NominalAttrType[] vectorToNominalAttrArray(ArrayList list) {
		NominalAttrType[] res = new NominalAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (NominalAttrType) list.get(i);
		}
		return res;
	}

	public static NumericAttrType[] vectorToNumericAttrArray(ArrayList list) {
		NumericAttrType[] res = new NumericAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (NumericAttrType) list.get(i);
		}
		return res;
	}

	public static TimeSeriesAttrType[] vectorToTimeSeriesAttrArray(ArrayList list) {
		TimeSeriesAttrType[] res = new TimeSeriesAttrType[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = (TimeSeriesAttrType) list.get(i);
		}
		return res;
	}

	public ArrayList collectAttributes(AttributeUseType attruse, AttributeType attrtype) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType type = getAttrType(i);
			if (attrtype == null || attrtype.equals(type.getAttributeType())) {
				switch (attruse) {
				case All:
					if (!type.getStatus().equals(Status.Disabled) && !type.getStatus().equals(Status.Key)) {
						result.add(type);
					}
					break;
				case Clustering:
					if (type.isClustering()) {
						result.add(type);
					}
					break;
				case Descriptive:
					if (type.isDescriptive()) {
						result.add(type);
					}
					break;
				case Target:
					if (type.isTarget()) {
						result.add(type);
					}
					break;
				case Key:
					if (type.isKey()) {
						result.add(type);
					}
					break;
				case GIS: // daniela
					if (type.isGIS()) {
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
				if (type.isDescriptive() && type.getAttributeType().equals(AttributeType.Numeric)) {
					SparseNumericAttrType nt = new SparseNumericAttrType((NumericAttrType) type);
					nt.setStatus(type.getStatus()); // ********************
					nt.setDescriptive(true);
					// nt.setClustering(true); //******************** //CV: added because of sparse
					// Pert(?), removed
					// because method ClusStatManager.check() gave error (2 types set in
					// hierarchical context)
					setAttrType(nt, i);
					nbSparse++;
				} else {
					nonSparse.add(type);
				}
			}
			m_NonSparse = vectorToAttrArray(nonSparse);
			if (getSettings().getGeneral().getVerbose() > 0)
				System.out.println("Number of sparse attributes: " + nbSparse);
			addRowsIndex();
			m_IsSparse = true;
		}
	}

	public void printInfo() {
		if (getSettings().getGeneral().getVerbose() >= 1) {
			System.out.println("Space required by nominal attributes: " + m_NbVt[ValueType.Int.getIndex()] * 4
					+ " bytes/tuple regular, " + m_NbVt[ValueType.BitwiseInt.getIndex()] * 4 + " bytes/tuple bitwise");
		}
	}

	protected void addRowsIndex() {
		// Allocate attributes to arrays m_Ints, m_Doubles, m_Objects
		m_NbVt = new int[ValueType.values().length];
		int bitPosition = 0; // for BitwiseNominalAttrType
		int nbBitwise = 0;
		for (int j = 0; j < m_NbAttrs; j++) {
			ClusAttrType at = m_Attr.get(j);
			ValueType vtype = at.getValueType();
			if (vtype.equals(ValueType.None) || at.getStatus().equals(Status.Disabled)) {
				at.setArrayIndex(-1);
			} else {
				if (!vtype.equals(ValueType.BitwiseInt)) {
					int sidx = m_NbVt[vtype.getIndex()]++;
					at.setArrayIndex(sidx);
				} else { // vtype == ClusAttrType.VALUE_TYPE_BITWISEINT
					nbBitwise++;
					BitwiseNominalAttrType bat = (BitwiseNominalAttrType) at;
					int nextBitPosition = bitPosition + bat.getNbBits();
					if (nextBitPosition > Integer.SIZE) {
						// too many bits needed to fit in current int
						m_NbVt[vtype.getIndex()]++;
						int sidx = m_NbVt[vtype.getIndex()];
						bat.setArrayIndex(sidx);
						bat.setBitPosition(0);
						bitPosition = bat.getNbBits();
					} else {
						int sidx = m_NbVt[vtype.getIndex()];
						bat.setArrayIndex(sidx);
						bat.setBitPosition(bitPosition);
						bitPosition = nextBitPosition;
					}
				}
			}
		}
		if (nbBitwise > 0)
			m_NbVt[ValueType.BitwiseInt.getIndex()]++;
		m_NbInts = Math.max(m_NbVt[ValueType.Int.getIndex()], m_NbVt[ValueType.BitwiseInt.getIndex()]); // only
																										// one of
																										// them
																										// will be
																										// different
																										// from 0
		m_NbDoubles = m_NbVt[ValueType.Double.getIndex()];
		m_NbObjects = m_NbVt[ValueType.Object.getIndex()];
		// Collect attributes into arrays m_Allattruse, m_Nominalattruse,
		// m_Numericattruse
		// Sorted in order that they occur in the .arff file (to be consistent with
		// weight vector order)
		int nb = AttributeUseType.values().length;
		m_AllAttrUse = new ClusAttrType[nb][];
		m_NominalAttrUse = new NominalAttrType[nb][];
		m_NumericAttrUse = new NumericAttrType[nb][];
		m_TimeSeriesAttrUse = new TimeSeriesAttrType[nb][];

		for (AttributeUseType attruse : AttributeUseType.values()) {
			m_AllAttrUse[attruse.getIndex()] = vectorToAttrArray(collectAttributes(attruse, null));
			m_NominalAttrUse[attruse.getIndex()] = vectorToNominalAttrArray(
					collectAttributes(attruse, AttributeType.Nominal));
			m_NumericAttrUse[attruse.getIndex()] = vectorToNumericAttrArray(
					collectAttributes(attruse, AttributeType.Numeric));
			m_TimeSeriesAttrUse[attruse.getIndex()] = vectorToTimeSeriesAttrArray(
					collectAttributes(attruse, AttributeType.TimeSeries));
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
			Status status = at.getStatus();
			if (status.equals(Status.Disabled)) {
				view.addAttribute(new DummySerializable());
			} else {
				view.addAttribute(at.createRowSerializable());
			}
		}
	}

	@Override
	public String toString() {
		int aidx = 0;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < getNbAttributes(); i++) {
			ClusAttrType type = getAttrType(i);
			if (!type.isDisabled()) {
				if (aidx != 0)
					buf.append(",");
				buf.append(type.getName());
				aidx++;
			}
		}
		return buf.toString();
	}

	public ArrayList<ClusAttrType> getAttr() {
		return m_Attr;
	}

	public void setHMTRHierarchy(ClusHMTRHierarchy HMTRHierarchy) {
		m_HMTRHierarchy = HMTRHierarchy;
	}

	public ClusHMTRHierarchy getHMTRHierarchy() {
		return m_HMTRHierarchy;
	}

	public int getNbHMTRAttributes() {
		return m_NbHMTRAttributes;
	}

	public void setNbHMTRAttributes(int nbHMTR) {
		this.m_NbHMTRAttributes = nbHMTR;
	}
}
