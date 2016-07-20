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

package clus.tools.hier;

import clus.ext.hierarchical.*;

import java.util.*;

import clus.util.*;
import jeans.math.*;
import jeans.util.*;
import jeans.io.*;

public class GenerateData {

	public static double MEAN_BRANCH = 2.75;
	public static int    MAX_BRANCH = 3;
	public static int    MAX_DEPTH = 4;
	public static int    MAX_CLASS = 1;
	public static int    NB_DATA = 10000;
	public static double MAX_ERROR = 0.001;

	public static ClassTerm createSimpleHierarchy() {
		ClassTerm term = new ClassTerm();
		createSimpleHierarchy(term, 0);
		return term;
	}

	public static void createSimpleHierarchy(ClassTerm term, int depth) {
		if (depth >= MAX_DEPTH) return;
		for (int i = 0; i < MAX_BRANCH ; i++) {
			ClassTerm child = new ClassTerm(String.valueOf(i+1));
			child.addParent(child);
			term.addChild(child);
			createSimpleHierarchy(child, depth+1);
		}
	}

	public static ClassTerm createInitHierarchy() {
		ClassTerm term = new ClassTerm();
		createInitHier(term, 0);
		return term;
	}

	public static void createInitHier(ClassTerm term, int depth) {
		if (depth >= MAX_DEPTH) return;
		int arity = ClusRandom.nextInt(ClusRandom.RANDOM_CREATE_DATA, (int)Math.floor(2*MEAN_BRANCH));
		for (int i = 0; i < arity; i++) {
			ClassTerm child = new ClassTerm(String.valueOf(i+1));
			child.addParent(term);
			term.addChild(child);
			createInitHier(child, depth+1);
		}
	}

	public static ClassTerm createHierarchy() {
		boolean done = false;
		boolean increase = true;
		int count = 0;
		ClassTerm root = createInitHierarchy();
		MyArray terms = new MyArray();
		addAll(root, terms);
		MyArray sel_from = new MyArray();
	    SingleStat stat = new SingleStat();
		while (!done) {
			int nb_possible = 0;
			sel_from.removeAllElements();
			if (increase) {
				for (int i = 0; i < terms.size(); i++) {
					ClassTerm trm = (ClassTerm)terms.elementAt(i);
					if (trm.getLevel() < MAX_DEPTH) {
						nb_possible++;
						sel_from.addElement(trm);
					}
				}
				int to_expand = ClusRandom.nextInt(ClusRandom.RANDOM_CREATE_DATA, nb_possible);
				ClassTerm myexp = (ClassTerm)sel_from.elementAt(to_expand);
				int name = myexp.getNbChildren();
				ClassTerm child = new ClassTerm(String.valueOf(name+1));
				child.addParent(myexp);
				myexp.addChild(child);
				terms.addElement(child);
			} else {
				for (int i = 0; i < terms.size(); i++) {
					ClassTerm trm = (ClassTerm)terms.elementAt(i);
					if (trm.getNbChildren() > 0) {
						nb_possible++;
						sel_from.addElement(trm);
					}

				}
				int to_decrease = ClusRandom.nextInt(ClusRandom.RANDOM_CREATE_DATA, nb_possible);
				ClassTerm mydecr = (ClassTerm)sel_from.elementAt(to_decrease);
				int nbch = mydecr.getNbChildren();
				int to_remove = ClusRandom.nextInt(ClusRandom.RANDOM_CREATE_DATA, nbch);
				mydecr.removeChild(to_remove);
				mydecr.numberChildren();
				terms.removeAllElements();
				addAll(root, terms);

			}
			boolean depth_constraint = false;
			for (int i = 0; i < terms.size(); i++) {
				ClassTerm trm = (ClassTerm)terms.elementAt(i);
				if (trm.getLevel() == MAX_DEPTH) depth_constraint = true;
			}
			stat.reset();
			root.getMeanBranch(null, stat);
			if (depth_constraint) {
				if (Math.abs(stat.getMean() - MEAN_BRANCH) < MAX_ERROR) {
					done = true;
				} else {
					if (stat.getMean() < MEAN_BRANCH) increase = true;
					else increase = false;
				}
			} else {
				increase = true;
			}
			if ((count % 100) == 0) {
				System.out.println("Current: "+stat.getMean()+" "+terms.size());
			}
			count++;
		}
		return root;
	}

	public static void addAll(ClassTerm root, MyArray cls) {
		cls.addElement(root);
		for (int i = 0; i < root.getNbChildren(); i++) {
			addAll((ClassTerm)root.getChild(i), cls);
		}
	}

	public static void addClasses(ClassTerm root, MyArray cls) {
		if (root.atBottomLevel()) cls.addElement(root);
		for (int i = 0; i < root.getNbChildren(); i++) {
			addClasses((ClassTerm)root.getChild(i), cls);
		}
	}

	public static void addAttributes(ClassTerm root, MyArray cls) {
		if (!root.atBottomLevel()) cls.addElement(root);
		for (int i = 0; i < root.getNbChildren(); i++) {
			addAttributes((ClassTerm)root.getChild(i), cls);
		}
	}

	public static void calcMeanDepth(ClassTerm root, SingleStat stat) {
		stat.addFloat((double)root.getLevel());
		for (int i = 0; i < root.getNbChildren(); i++) {
			calcMeanDepth((ClassTerm)root.getChild(i), stat);
		}
	}

	public static void main(String[] args) {
		// MEAN_BRANCH = Double.parseDouble(args[0]);
		MAX_BRANCH  = Integer.parseInt(args[0]);
		MAX_DEPTH   = Integer.parseInt(args[1]);
		MAX_CLASS   = Integer.parseInt(args[2]);
		int RND     = Integer.parseInt(args[3]);

		System.out.println("Branch: "+MAX_BRANCH);
		System.out.println("Depth:  "+MAX_DEPTH);
		System.out.println("Class:  "+MAX_CLASS);
		System.out.println("Random: "+RND);

		ClusRandom.initialize(RND);
		ClassTerm root = createSimpleHierarchy();

		HierIO io = new HierIO();
		System.out.println("Hierarchy:");
		io.writeHierarchy(root, ClusFormat.OUT_WRITER);

	    	SingleStat hb = new SingleStat();
		root.getMeanBranch(null, hb);
		System.out.println("Mean branching factor: "+hb);

		MyArray cls = new MyArray();
		addClasses(root, cls);

		MyArray attr = new MyArray();
		addAttributes(root, attr);

		MyFile file = new MyFile("artificial.arff");
		file.log("@relation artificial");
		file.log();

		for (int i = 0; i < attr.size(); i++) {
			ClassTerm at = (ClassTerm)attr.elementAt(i);
			for (int j = 0; j < at.getNbChildren(); j++) {
				file.log("@attribute\t"+at+"_"+j+"\t{0,1}");
			}
		}
		file.log("@attribute\ttarget\tclasses");

		file.log();
		file.log("@data");

		ClassHierarchy hier = new ClassHierarchy(root);
		hier.numberHierarchy();
		MyArray target = new MyArray();
		boolean[] include = new boolean[hier.getTotal()];

		int nb_class = 0;
		SingleStat meb = new SingleStat();
		for (int i = 0; i < NB_DATA; i++) {
			StringBuffer buf = new StringBuffer();

			Arrays.fill(include, false);
			target.removeAllElements();
			int nb_classes = ClusRandom.nextInt(ClusRandom.RANDOM_CREATE_DATA, MAX_CLASS)+1;
			for (int j = 0; j < nb_classes; j++) {
				boolean found = false;
				ClassTerm mcls = null;
				while (!found) {
					int idx = ClusRandom.nextInt(ClusRandom.RANDOM_CREATE_DATA, cls.size());
					mcls = (ClassTerm)cls.elementAt(idx);
					if (!include[mcls.getIndex()]) found = true;
				}
//				mcls.toBoolVector(include);
				target.addElement(mcls);
			}

			nb_class += nb_classes;
			meb.addMean(hier.getMeanBranch(include));

			for (int j = 0; j < attr.size(); j++) {
				ClassTerm at = (ClassTerm)attr.elementAt(j);
				if (include[at.getIndex()]) {
					int nb_match = 0;
					for (int k = 0; k < at.getNbChildren(); k++) {
						ClassTerm child = (ClassTerm)at.getChild(k);
						if (include[child.getIndex()]) {
							nb_match++;
							buf.append('1');
						} else {
							buf.append('0');
						}
						buf.append(',');
					}
				} else {
					for (int k = 0; k < at.getNbChildren(); k++) {
						int value = ClusRandom.nextInt(ClusRandom.RANDOM_CREATE_DATA, 2);
						buf.append(value);
						buf.append(',');
					}
				}
			}
			for (int j = 0; j < target.size(); j++) {
				if (j != 0) buf.append('@');
				buf.append(target.elementAt(j));
			}
			file.log(buf.toString());
		}
		file.close();

		SingleStat md = new SingleStat();
		calcMeanDepth(root, md);

		MyFile info = new MyFile("hier.info");
		info.log("Setting Mean Branch: "+MEAN_BRANCH);
		info.log("Hier Branch: "+hb);
		info.log("Hier Max Depth: "+MAX_DEPTH);
		info.log("Hier Mean Depth: "+md);
		info.log("Hier Nodes: "+attr.size());
		info.log("Hier Leaves: "+cls.size());
		info.log("Example Max Class: "+MAX_CLASS);
		double mean_cls = (double)nb_class/NB_DATA;
		info.log("Example Mean Class: "+mean_cls);
		info.log("Example Branch: "+meb);
		info.log();
		io.writeHierarchy(root, info.getWriter());
		info.close();
	}
}

