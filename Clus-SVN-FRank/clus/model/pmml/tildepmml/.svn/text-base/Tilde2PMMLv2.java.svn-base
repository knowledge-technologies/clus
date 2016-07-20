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

package clus.model.pmml.tildepmml;

import java.util.*;

import java.io.*;

import clus.algo.tdidt.*;
import clus.statistic.*;

import jeans.util.*;

import clus.model.modelio.tilde.*;

public class Tilde2PMMLv2 {

	static final String COPYRIGHT="Jan Struyf, Darek Krzywania";
	static final String DESCRIPTION="Predicting skidding for the SolEuNet Challenge";
	static final String APPLICATIONNAME="SolEuNet Challenge";
	static final String APPLICATIONVERSION="1.0";
	static final String XMLVERSION="1.0";
        static final String XMLENCODING="ISO-8859-1";

	static Vector itemSets=new Vector();
	static Vector items=new Vector();
	static HashMap itemsmap = new HashMap();
	static int nbOfItems=0;
	static int itemSetCount=0;
	static int selectClass = 0;


	public static ClusNode loadTildeTree(InputStream strm, int dim, String schema) throws IOException {
		TildeOutReader reader = new TildeOutReader(strm);
		reader.setDim(dim);
		reader.doParse();
		if (schema != null) reader.readTargetSchema(schema);
		ClusNode root = reader.getTree();
		reader.close();
		return root;
	}

	public static void main(String[] args) {
		try {
			int dim = 1;
			String schemaName = null;
			if (args.length >= 2) dim = Integer.parseInt(args[1]);
			if (args.length >= 3) selectClass = Integer.parseInt(args[2]);
			if (args.length >= 4) schemaName = args[3];
			ClusNode root = loadTildeTree(new FileInputStream(args[0]), dim, schemaName);

			//output to pmmlcode.pmml
			File outFile = new File(FileUtil.getName(args[0]) + ".pmml");
			FileOutputStream outFileStream = new FileOutputStream(outFile);
			PrintWriter outStream = new PrintWriter(outFileStream);

			//header
	                String header = "<?xml version=\""+XMLVERSION+"\" encoding=\""+XMLENCODING+"\"?>\n<PMML>\n<Header copyright=\""+COPYRIGHT+"\" description=\""+DESCRIPTION+"\">\n<Application name=\""+APPLICATIONNAME+"\" version=\""+APPLICATIONVERSION+"\">\n</Header>";
			outStream.println(header);

			depthFirstInit(root, outStream);
			printItems(itemSets, outStream);
			printItemsets(itemSets, outStream);
			depthFirstPrint(root, outStream, 0);

			//((Itemset)itemSets.elementAt(1)).print(outStream);

			//endfile
			outStream.println("</PMML>");

			//closing outStream
			outStream.close();
		}

		catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
		}

	} //main

	private static Item getOrAddItem(String name, boolean fieldOrValue) {
		Item TempItem = (Item)itemsmap.get(name);
		if (TempItem == null) {
			TempItem = new Item(nbOfItems, fieldOrValue, name);
			itemsmap.put(name, TempItem);
			items.add(TempItem);
			nbOfItems++;
		}
		return TempItem;
	}

	private static Itemset addItemSet(String name) {
		Itemset TempItemSet = new Itemset(itemSets.size(), name);
		itemSets.add(TempItemSet);
		return TempItemSet;
	}


//depth-first traversal of the tree to initialize
	private static void depthFirstInit(ClusNode $currentNode, PrintWriter $outStream) throws IOException {
		try{
			String test = $currentNode.getTest().toString();
			MStreamTokenizer tokens = MStreamTokenizer.createStringParser(test);
			tokens.setCharTokens(",[]():=><");
			System.out.println("Line = "+test);
			CompoundPredicate pred = new CompoundPredicate();
			$currentNode.setVisitor(pred);
			while (tokens.hasMoreTokens()) {
				String token1 = tokens.readToken();
				String token2 = tokens.readToken();
				System.out.println("token1 = "+token1);
				System.out.println("token2 = "+token2);

				if (token2.equals("(")) {

					Itemset TempItemSet = addItemSet(token1);
					do {
						String arg = tokens.readToken();
						Item TempItem = getOrAddItem(arg, false);
						TempItemSet.addItemRef(TempItem);
					} while(tokens.isNextToken(','));
					pred.addItemset(TempItemSet);
					tokens.readChar(')');

				} else {

					String name = "unknown";
					if (token2.equals("=")) {
						name="equals";
						tokens.readToken();
					}
					else if (token2.equals("<")) {
						name="lessThan";
					}
					else if (token2.equals(">")) {
						name="greaterThan";
					}

					Itemset TempItemSet = addItemSet(name);

					Item TempItem1 = getOrAddItem(token1, false);
					TempItemSet.addItemRef(TempItem1);

					String arg2 = tokens.readToken();
					Item TempItem2 = getOrAddItem(arg2, true);
					TempItemSet.addItemRef(TempItem2);
					pred.addItemset(TempItemSet);
				}

				if (!tokens.isNextToken(',')) {
					tokens.readChar('?');
				}

			} //no more tokens


		       	for(int idx = 0; idx < $currentNode.getNbChildren(); idx++ ) {
				ClusNode $childNode = (ClusNode)$currentNode.getChild(idx);
				if (!$childNode.atBottomLevel())
					depthFirstInit($childNode, $outStream);
	        	}

		} catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
		}

	} //depthFirstInit

	//***************************************************************************************************


	private static void printItems(Vector $itemSets, PrintWriter $outStream) {
		for (int i = 0; i < items.size(); i++) {
			((Item)items.elementAt(i)).print($outStream);
		}
	}


	//***************************************************************************************************


	private static void printItemsets(Vector $itemSets, PrintWriter $outStream) {
		for (int i = 0; i < $itemSets.size(); i++) {
			((Itemset)$itemSets.elementAt(i)).print($outStream);
		}
	}

	private static void printTabs(PrintWriter $outStream, int tabs) {
		for (int counter = 0; counter < tabs; counter++) {
			$outStream.print("\t");
		}
	}

	//***************************************************************************************************

	//depth-first traversal of the tree to print to PMML file

	private static void depthFirstPrint(ClusNode $currentNode, PrintWriter $outStream, int tabs) throws IOException {
		try{
			printTabs($outStream, tabs);
			ClassificationStat stat = (ClassificationStat)$currentNode.getClusteringStat();
			$outStream.println("<Node recordCount=\""+(int)stat.getTotalWeight()+"\" score=\""+stat.getPredictedClassName(selectClass)+"\">");

			for(int idx = 0; idx < stat.getNbClasses(selectClass); idx++ ) {
				printTabs($outStream, tabs);
				$outStream.println("<ScoreDistribution value=\""+stat.getClassName(selectClass,idx)+"\" recordCount=\""+(int)stat.getCount(selectClass,idx)+"\" />");
			}

			CompoundPredicate pred = (CompoundPredicate)$currentNode.getVisitor();
			pred.print($outStream, tabs);
			$outStream.println();

			int newTab = tabs+1;
			int nbChild = $currentNode.getNbChildren();
		       	for(int idx = 0; idx < nbChild; idx++) {
				ClusNode $childNode = (ClusNode)$currentNode.getChild(idx);

				if (!$childNode.atBottomLevel())
					depthFirstPrint($childNode, $outStream, newTab);

				if (idx == nbChild-1) {
					printTabs($outStream, tabs);
					$outStream.write("</Node>\n");
				}

        		} // end for

		} //try

		catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
		}
	}

} //class
