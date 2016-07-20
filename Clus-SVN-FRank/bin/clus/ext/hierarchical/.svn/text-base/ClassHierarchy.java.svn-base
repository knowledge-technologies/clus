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

package clus.ext.hierarchical;

import jeans.tree.*;
import jeans.util.array.StringTable;
import jeans.math.*;

import java.io.*;
import java.util.*;

import clus.main.*;
import clus.util.*;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.*;

public class ClassHierarchy implements Serializable {

	public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

	public final static int TEST = 0;
	public final static int ERROR = 1;

	public final static int TREE = 0;
	public final static int DAG = 1;

	protected int m_MaxDepth = 0;
	protected int m_HierType = TREE;
	protected ClassesTuple m_Eval;
	protected ArrayList m_ClassList = new ArrayList();
	protected HashMap m_ClassMap = new HashMap();
	protected ClassTerm m_Root;
	protected NumericAttrType[] m_DummyTypes;
	protected boolean m_IsLocked;
	protected transient double[] m_Weights;
	protected transient Hashtable m_ErrorWeights = new Hashtable();
	protected transient ClassesAttrType m_Type;

	public ClassHierarchy() {
	}

	public ClassHierarchy(ClassesAttrType type) {
		this(new ClassTerm());
		setType(type);
	}

	public ClassHierarchy(ClassTerm root) {
		m_Root = root;
	}

	public Settings getSettings() {
		return m_Type.getSettings();
	}

	public final void setType(ClassesAttrType type) {
		m_Type = type;
	}

	public final ClassesAttrType getType() {
		return m_Type;
	}

	public final void addClass(ClassesValue val) {
		if (!isLocked()) m_Root.addClass(val, 0, this);
	}

	public final void print(PrintWriter wrt) {
		m_Root.print(0, wrt, null, null);
	}

	public final void print(PrintWriter wrt, double[] counts, double[] weights) {
		m_Root.print(0, wrt, counts, weights);
	}

	public final void print(PrintWriter wrt, double[] counts) {
		m_Root.print(0, wrt, counts, m_Weights);
	}

	public final int getMaxDepth() {
		return m_Root.getMaxDepth();
	}

	public final ClassTerm getRoot() {
		return m_Root;
	}

	public final void initClassListRecursiveTree(ClassTerm term) {
		m_ClassList.add(term);
		term.sortChildrenByID();
		for (int i = 0; i < term.getNbChildren(); i++) {
			initClassListRecursiveTree((ClassTerm)term.getChild(i));
		}
	}

	public final void initClassListRecursiveDAG(ClassTerm term, HashSet set) {
		if (!set.contains(term.getID())) {
			// This is the first time we see this term
			m_ClassList.add(term);
			term.sortChildrenByID();
			for (int i = 0; i < term.getNbChildren(); i++) {
				initClassListRecursiveDAG((ClassTerm)term.getChild(i), set);
			}
			set.add(term.getID());
		}
	}

	public final void numberHierarchy() {
		m_Root.setIndex(-1);
		m_Root.sortChildrenByID();
		m_ClassList.clear();
		if (isDAG()) {
			// make sure each ID only appears once!
			HashSet set = new HashSet();
			for (int i = 0; i < m_Root.getNbChildren(); i++) {
				initClassListRecursiveDAG((ClassTerm)m_Root.getChild(i), set);
			}
		} else {
			for (int i = 0; i < m_Root.getNbChildren(); i++) {
				initClassListRecursiveTree((ClassTerm)m_Root.getChild(i));
			}
		}
		for (int i = 0; i < getTotal(); i++) {
			ClassTerm term = getTermAt(i);
			term.setIndex(i);
		}
		System.out.println("Hierarchy initialized: "+getTotal()+" nodes");
		// after this, the hierarchy must not change anymore
		setLocked(true);
	}

	void getAllParentChildTuplesRecursive(ClassTerm node, boolean[] visited, ArrayList parentchilds) {
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClassTerm child = (ClassTerm)node.getChild(i);
			parentchilds.add(node.getID()+"/"+child.getID());
			if (!visited[child.getIndex()]) {
				// If visited, then all tuples for subtree below child are already included
				visited[child.getIndex()] = true;
				getAllParentChildTuplesRecursive(child, visited, parentchilds);
			}
		}
	}

	public ArrayList getAllParentChildTuples() {
		ArrayList parentchilds = new ArrayList();
		boolean[] visited = new boolean[getTotal()];
		getAllParentChildTuplesRecursive(m_Root, visited, parentchilds);
		return parentchilds;
	}

	void getAllPathsRecursive(ClassTerm node, String crpath, boolean[] visited, ArrayList paths) {
		for (int i = 0; i < node.getNbChildren(); i++) {
			ClassTerm child = (ClassTerm)node.getChild(i);
			String new_path = node.getIndex() == -1 ? "" : crpath + "/";
			new_path = new_path + child.getID();
			paths.add(new_path);
			if (!visited[child.getIndex()]) {
				// If visited, then all paths for subtree below child are already included
				visited[child.getIndex()] = true;
				getAllPathsRecursive(child, new_path, visited, paths);
			}
		}
	}

	public ArrayList getAllPaths() {
		ArrayList paths = new ArrayList();
		boolean[] visited = new boolean[getTotal()];
		getAllPathsRecursive(m_Root, "", visited, paths);
		return paths;
	}

	public void addAllClasses(ClassesTuple tuple, boolean[] matrix) {
		int idx = 0;
		tuple.setSize(countOnes(matrix));
		for (int i = 0; i < getTotal(); i++) {
			if (matrix[i]) tuple.setItemAt(new ClassesValue(getTermAt(i), 1.0), idx++);
		}
	}

	public void fillBooleanMatrixMaj(double[] mean, boolean[] matrix, double treshold) {
		for (int i = 0; i < getTotal(); i++) {
			ClassTerm term = getTermAt(i);
			if (mean[term.getIndex()] >= treshold/100.0) matrix[term.getIndex()] = true;
		}
	}

	public static void removeParentNodesRec(ClassTerm node, boolean[] matrix) {
		if (matrix[node.getIndex()]) {
			ClassTerm parent = node.getCTParent();
			while (parent.getIndex() != -1 && matrix[parent.getIndex()]) {
				matrix[parent.getIndex()] = false;
				parent = parent.getCTParent();
			}
		}
		for (int i = 0; i < node.getNbChildren(); i++) {
			removeParentNodesRec((ClassTerm)node.getChild(i), matrix);
		}
	}

	public static void removeParentNodes(ClassTerm node, boolean[] matrix) {
		for (int i = 0; i < node.getNbChildren(); i++) {
			removeParentNodesRec((ClassTerm)node.getChild(i), matrix);
		}
	}

	public void removeParentNodesRecursive(ClassTerm term, boolean[] array) {
		for (int i = 0; i < term.getNbParents(); i++) {
			ClassTerm par = term.getParent(i);
			if (par.getIndex() != -1 && array[par.getIndex()]) {
				array[par.getIndex()] = false;
				removeParentNodesRecursive(par, array);
			}
		}
	}

	public void removeParentNodes(boolean[] array) {
		for (int i = 0; i < getTotal(); i++) {
			ClassTerm term = getTermAt(i);
			if (term.getIndex() != -1 && array[term.getIndex()]) {
				removeParentNodesRecursive(term, array);
			}
		}
	}

	public static int countOnes(boolean[] matrix) {
		int count = 0;
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i]) count++;
		}
		return count;
	}

	// Currently not used
	public ClassesTuple getBestTupleMajNoParents(double[] mean, double treshold) {
		boolean[] classes = new boolean[getTotal()];
		fillBooleanMatrixMaj(mean, classes, treshold);
		removeParentNodes(getRoot(), classes);
		ClassesTuple tuple = new ClassesTuple();
		addAllClasses(tuple, classes);
		return tuple;
	}

	public ClassesTuple getBestTupleMaj(double[] mean, double treshold) {
		boolean[] classes = new boolean[getTotal()];
		fillBooleanMatrixMaj(mean, classes, treshold);
		ClassesTuple tuple = new ClassesTuple();
		addAllClasses(tuple, classes);
		return tuple;
	}
	
	public ClassesTuple getTuple(boolean[] nodes) {
		ClassesTuple result = new ClassesTuple();
		addAllClasses(result, nodes);
		return result;
	}	
	
	public final CompleteTreeIterator getNoRootIter() {
		CompleteTreeIterator iter = new CompleteTreeIterator(m_Root);
		if (iter.hasMoreNodes()) iter.getNextNode();
		return iter;
	}

	public final CompleteTreeIterator getRootIter() {
		return new CompleteTreeIterator(m_Root);
	}

	public final double[] getWeights() {
		return m_Weights;
	}

	public final void calcWeights() {
		HierNodeWeights ws = new HierNodeWeights();
		int wtype = getSettings().getHierWType();
		double widec = getSettings().getHierWParam();
		ws.initExponentialDepthWeights(this, wtype, widec);
		m_Weights = ws.getWeights();
	}

	public final void calcMaxDepth() {
		m_MaxDepth = 0;
		for (int i = 0; i < getTotal(); i++) {
			ClassTerm term = getTermAt(i);
			m_MaxDepth = Math.max(m_MaxDepth, term.getMaxDepth());
		}
	}

	public final SingleStat getMeanBranch(boolean[] enabled) {
		SingleStat stat = new SingleStat();
		m_Root.getMeanBranch(enabled, stat);
		return stat;
	}

	public final int getTotal() {
		return m_ClassList.size();
	}

	public final int getDepth() {
		return m_MaxDepth;
	}

	public final int[] getClassesByLevel() {
		int[] res = new int[getDepth()+2];
		countClassesRecursive(m_Root, 0, res);
		return res;
	}

	public final void countClassesRecursive(ClassTerm root, int depth, int[] cls) {
		cls[depth]++;
		for (int i = 0; i < root.getNbChildren(); i++) {
			countClassesRecursive((ClassTerm)root.getChild(i), depth+1, cls);
		}
	}

	public final void initialize() {
		numberHierarchy();
		calcWeights();
		calcMaxDepth();
		ClusSchema schema = m_Type.getSchema();
		int maxIndex = schema.getNbAttributes();
		m_DummyTypes = new NumericAttrType[getTotal()];
		for (int i = 0; i < getTotal(); i++) {
			NumericAttrType type = new NumericAttrType("H"+i);
			type.setIndex(maxIndex++);
			type.setSchema(schema);
			m_DummyTypes[i] = type;
		}
	}

	public boolean[] removeInfrequentClasses(WHTDStatistic stat, double minfreq) {
		boolean[] removed = new boolean[getTotal()];
		ArrayList new_cls = new ArrayList();
		for (int i = 0; i < getTotal(); i++) {
			double mean = stat.getMean(i);
			if (mean == 0.0 || mean < minfreq) {
				ClassTerm trm = getTermAt(i);
				for (int j = 0; j < trm.getNbParents(); j++) {
					ClassTerm par = trm.getParent(j);
					par.removeChild(trm);
				}
				removed[trm.getIndex()] = true;
			} else {
				new_cls.add(getTermAt(i));
			}
		}
		m_ClassList.clear();
		m_ClassMap.clear();
		if (isDAG()) {
			for (int i = 0; i < new_cls.size(); i++) {
				ClassTerm trm = (ClassTerm)new_cls.get(i);
				m_ClassMap.put(trm.getID(), trm);
			}
		}
		return removed;
	}

	public final NumericAttrType[] getDummyAttrs() {
		return m_DummyTypes;
	}

	public final void showSummary() {
		int leaves = 0;
		int depth = getMaxDepth();
		System.out.println("Depth: "+depth);
		System.out.println("Nodes: "+getTotal());
		ClassTerm root = getRoot();
		int nb = root.getNbChildren();
		for (int i = 0; i < nb; i++) {
			ClassTerm chi = (ClassTerm)root.getChild(i);
			int nbl = chi.getNbLeaves();
			System.out.println("Child "+i+": "+chi.getID()+" "+nbl);
			leaves += nbl;
		}
		System.out.println("Leaves: "+leaves);
	}

	public final ClassTerm getClassTermTree(ClassesValue vl) throws ClusException {
		int pos = 0;
		int nb_level = vl.getNbLevels();
		ClassTerm subterm = m_Root;
		while (true) {
			if (pos >= nb_level) return subterm;
			String lookup = vl.getClassID(pos);
			if (lookup.equals("0")) {
				return subterm;
			} else {
				ClassTerm found = subterm.getByName(lookup);
				if (found == null) throw new ClusException("Classes value not in tree hierarchy: "+vl.toPathString()+" (lookup: "+lookup+", term: "+subterm.toPathString()+", subterms: "+subterm.getKeysVector()+")");
				subterm = found;
			}
			pos++;
		}
	}

	public final ClassTerm getClassTermDAG(ClassesValue vl) throws ClusException {
		//System.out.println("Meest specifieke klasse: "+vl.getMostSpecificClass());
		ClassTerm term = getClassTermByName(vl.getMostSpecificClass());
		if (term == null) throw new ClusException("Classes value not in DAG hierarchy: "+vl.toPathString());
		return term;
	}

	public final ClassTerm getClassTerm(ClassesValue vl) throws ClusException {
		if (isTree()) {
			return getClassTermTree(vl);
		} else {
			return getClassTermDAG(vl);
		}
	}

	public final int getClassIndex(ClassesValue vl) throws ClusException {
		return getClassTerm(vl).getIndex();
	}

	public final double getWeight(int idx) {
		return m_Weights[idx];
	}

	public final void setEvalClasses(ClassesTuple eval) {
		m_Eval = eval;
	}

	public final ClassesTuple getEvalClasses() {
		return m_Eval;
	}

	public final boolean[] getEvalClassesVector() {
		if (m_Eval == null) {
			boolean[] res = new boolean[getTotal()];
			Arrays.fill(res, true);
			return res;
		} else {
			return m_Eval.getVectorBoolean(this);
		}
	}

	public void addChildrenToRoot() {
		// terms without parents are children of the root
		Iterator iter = m_ClassMap.values().iterator();
		while (iter.hasNext()) {
			ClassTerm term = (ClassTerm)iter.next();
			if (term != m_Root && term.atTopLevel()) {
				m_Root.addChildCheckAndParent(term);
			}
		}
	}

	public void addParentChildTuple(String parent, String child) throws ClusException {
		ClassTerm parent_t = getClassTermByNameAddIfNotIn(parent);
		ClassTerm child_t  = getClassTermByNameAddIfNotIn(child);
		if (parent_t.getByName(child) != null) {
			throw new ClusException("Duplicate parent-child relation '"+parent+"' -> '"+child+"' in DAG definition in .arff file");
		}
		parent_t.addChildCheckAndParent(child_t);
	}

	public void loadDAG(String[] cls) throws IOException, ClusException {
		addClassTerm("root", getRoot());
		for (int i = 0; i < cls.length; i++) {
			String[] rel = cls[i].split("\\s*\\/\\s*");
			if (rel.length != 2) {
				//System.out.println(cls[i]);
				throw new ClusException("Illegal parent child tuple in .arff");
			}
			String parent = rel[0];
			String child  = rel[1];
			addParentChildTuple(parent, child);
		}
		addChildrenToRoot();
	}

	public void loadDAG(String fname) throws IOException, ClusException {
		String line = null;
		LineNumberReader rdr = new LineNumberReader(new FileReader(fname));
		while ((line = rdr.readLine()) != null) {
			line = line.trim();
			if (!line.equals("")) {
				String[] rel = line.split("\\s*\\,\\s*");
				if (rel.length != 2) {
					throw new ClusException("Illegal line '"+line+"' in DAG definition file: '"+fname+"'");
				}
				String parent = rel[0];
				String child  = rel[1];
				addParentChildTuple(parent, child);
			}
		}
		rdr.close();
		addChildrenToRoot();
	}

	public final static char DFS_WHITE = 0;
	public final static char DFS_GRAY = 1;
	public final static char DFS_BLACK = 2;

	public void findCycleRecursive(ClassTerm term, char[] visited, ClassTerm[] pi, boolean[] hasCycle) {
		visited[term.getIndex()] = DFS_GRAY;
		for (int i = 0; i < term.getNbChildren(); i++) {
			ClassTerm child = (ClassTerm)term.getChild(i);
			if (visited[child.getIndex()] == DFS_WHITE) {
				pi[child.getIndex()] = term;
				findCycleRecursive(child, visited, pi, hasCycle);
			} else if (visited[child.getIndex()] == DFS_GRAY) {
				System.out.println("Cycle: ");
				System.out.print("("+term.getID()+","+child.getID()+")");
				ClassTerm w = term;
				do {
					System.out.print("; ("+w.getID()+","+pi[w.getIndex()].getID()+")");
					w = pi[w.getIndex()];
				} while (w != child);
				System.out.println();
				hasCycle[0] = true;
			}
		}
		visited[term.getIndex()] = DFS_BLACK;
	}

	public void findCycle() {
		char[] visited = new char[getTotal()];
		ClassTerm[] pi = new ClassTerm[getTotal()];
		boolean[] hasCycle = new boolean[1];
		Arrays.fill(visited, DFS_WHITE);
		for (int i = 0; i < m_ClassList.size(); i++) {
			ClassTerm term = getTermAt(i);
			if (visited[term.getIndex()] == DFS_WHITE) {
				findCycleRecursive(term, visited, pi, hasCycle);
			}
		}
		if (hasCycle[0]) System.exit(-1);
	}

	public void writeTargets(RowData data, ClusSchema schema, String name) throws ClusException, IOException {
		double[] wis = getWeights();
		PrintWriter wrt = new PrintWriter(new FileWriter(name + ".weights"));
		wrt.print("weights(X) :- X = [");
		for (int i = 0; i < wis.length; i++) {
			if (i != 0) wrt.print(",");
			wrt.print(wis[i]);
		}
		wrt.println("].");
		wrt.println();
		ClassTerm[] terms = new ClassTerm[wis.length];
		CompleteTreeIterator iter = getRootIter();
		while (iter.hasMoreNodes()) {
			ClassTerm node = (ClassTerm)iter.getNextNode();
			if (node.getIndex() != -1) terms[node.getIndex()] = node;
		}
		for (int i = 0; i < wis.length; i++) {
			wrt.print("% class "+terms[i]+": ");
			wrt.println(wis[i]);
		}
		wrt.close();
		ClusAttrType[] keys = schema.getAllAttrUse(ClusAttrType.ATTR_USE_KEY);
		int sidx = getType().getArrayIndex();
		wrt = new PrintWriter(new FileWriter(name + ".targets"));
		for (int i = 0; i < data.getNbRows(); i++) {
			DataTuple tuple = data.getTuple(i);
			int pos = 0;
			for (int j = 0; j < keys.length; j++) {
				if (pos != 0) wrt.print(",");
				wrt.print(keys[j].getString(tuple));
				pos++;
			}
			ClassesTuple target = (ClassesTuple)tuple.getObjVal(sidx);
			double[] vec = target.getVectorNodeAndAncestors(this);
			wrt.print(",");
			wrt.print(target.toString());
			wrt.print(",[");
			for (int j = 0; j < vec.length; j++) {
				if (j != 0) wrt.print(",");
				wrt.print(vec[j]);
			}
			wrt.println("]");
		}
		wrt.close();
	}

	public void setLocked(boolean lock) {
		m_IsLocked = lock;
	}

	public boolean isLocked() {
		return m_IsLocked;
	}

	public void setHierType(int type) {
		m_HierType = type;
	}

	public boolean isTree() {
		return m_HierType == TREE;
	}

	public boolean isDAG() {
		return m_HierType == DAG;
	}

	public ClassTerm getClassTermByNameAddIfNotIn(String id) {
		ClassTerm found = getClassTermByName(id);
		if (found == null) {
			found = new ClassTerm(id);
			addClassTerm(id, found);
		}
		return found;
	}

	public ClassTerm getClassTermByName(String id) {
		return (ClassTerm)m_ClassMap.get(id);
	}

	public void addClassTerm(String id, ClassTerm term) {
		m_ClassMap.put(id, term);
	}

	public void addClassTerm(ClassTerm term) {
		m_ClassList.add(term);
	}

	public ClassTerm getTermAt(int i) {
		return (ClassTerm)m_ClassList.get(i);
	}

	public ClassesValue createValueByName(String name, StringTable table) throws ClusException {
		ClassesValue val = new ClassesValue(name, table);
		ClassTerm term = getClassTerm(val);
		val.setClassTerm(term);
		return val;
	}
}
