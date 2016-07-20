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
import clus.model.modelio.tilde.*;

import jeans.util.*;

public class Tilde2PMML {

	static final String COPYRIGHT="Jan Struyf, Darek Krzywania";
	static final String DESCRIPTION="Predicting skidding for the SolEuNet Challenge";
	static final String APPLICATIONNAME="SolEuNet Challenge";
	static final String APPLICATIONVERSION="1.0";
	static final String XMLVERSION="1.0";
        static final String XMLENCODING="ISO-8859-1";

	public static ClusNode loadTildeTree(InputStream strm) throws IOException {
		TildeOutReader reader = new TildeOutReader(strm);
		reader.doParse();
		ClusNode root = reader.getTree();
		reader.close();
		return root;
	}

	public static void main(String[] args) {
		try {
			ClusNode root = loadTildeTree(new FileInputStream(args[0]));
			ClusNode currentNode;

			//output to pmmlcode.pmml
			File outFile = new File("pmmlcode.pmml");
			FileOutputStream outFileStream = new FileOutputStream(outFile);
			PrintWriter outStream = new PrintWriter(outFileStream);

			//header
	                String header = "<?xml version=\""+XMLVERSION+"\" encoding=\""+XMLENCODING+"\"?>\n<PMML>\n<Header copyright=\""+COPYRIGHT+"\" description=\""+DESCRIPTION+"\">\n<Application name=\""+APPLICATIONNAME+"\" version=\""+APPLICATIONVERSION+"\">\n</Header>\n";
			outStream.write(header);

			//start at the root and travers depth first
			currentNode = root;
			Vector itemSets= new Vector();
			depthFirstInit(currentNode, outStream, 0, itemSets, 0, 0, 0, 0);
//			depthFirstPrint(currentNode, outStream, 0, itemSets, 0, 0);

			//endfile
			outStream.write("</PMML>");

			//closing outStream
			outStream.close();

		}

		catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
		}

	} //main


	//depth-first traversal of the tree to initialize
	static void depthFirstInit(ClusNode $currentNode, PrintWriter $outStream, int tabs, Vector $itemSets, int
	$nbOfItems, int $nbOfItemSets, int $open, int $close) throws IOException {
	try{
		String test = $currentNode.getTest().toString();
		MStreamTokenizer tokens = MStreamTokenizer.createStringParser(test);
		tokens.setCharTokens(",[]():");

		while (tokens.hasMoreTokens()) {
			String name = tokens.readToken();

			Vector TempVector = new Vector();
			Itemset TempItemSet = new Itemset($nbOfItemSets, name, 0, TempVector);
			$nbOfItemSets++;

			int counter1 = tabs;

			while (counter1>0) {
			$outStream.write("\t");
			counter1--;
			}

			$outStream.write("<Node "+name+">");
			$open++;
			tokens.readChar('(');

			do {
			String arg = tokens.readToken();
			$outStream.write("  Arg "+arg);

			Item TempItem = new Item($nbOfItems, false, arg);
			$nbOfItems++;
			TempItemSet.addItemRef(TempItem);

			} while(tokens.isNextToken(','));

			$itemSets.add($nbOfItemSets-1,TempItemSet);

		tokens.readChar(')');
		tokens.readChar('?');
		tokens.isNextToken(',');

		}
		$outStream.write("\n");

		int newTab = tabs+1;

	       	for(int idx = 0; idx < $currentNode.getNbChildren(); idx++ ) {

		ClusNode $childNode= (ClusNode)$currentNode.getChild(idx);
		if (!$childNode.atBottomLevel()) depthFirstInit($childNode, $outStream, newTab, $itemSets,
		$nbOfItems, $nbOfItemSets, $open, $close);

			if (idx==$currentNode.getNbChildren()-1) {

			int counter2 = tabs;

			while (counter2>0){
			$outStream.write("\t");
			counter2--;
			}


			$outStream.write("</Node>\n");
			$close++;

			if ($open==$close)
			done($itemSets, $outStream);
			}

        	}
	} //try

	catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
	}

	} //depthFirstInit

	static void done (Vector ItemSets, PrintWriter outStream) {

	int counter=ItemSets.size();
	int pointer=0;

	while(counter>0) {

	((Itemset)ItemSets.elementAt(pointer)).print(outStream);

	counter--;
	pointer++;
	}

	}

	//***************************************************************************************************

/*	//depth-first traversal of the tree to print to PMML file
	static void depthFirstPrint(ClusNode $currentNode, PrintWriter $outStream, int tabs, Vector $itemSets, int
	$nbOfItems, int $nbOfItemSets) throws IOException {
	try{
		String test = $currentNode.getTest().toString();
		MStreamTokenizer tokens = MStreamTokenizer.createStringParser(test);
		tokens.setCharTokens(",[]():");

		while (tokens.hasMoreTokens()) {
			String name = tokens.readToken();

			Itemset TempItemSet = new Itemset($nbOfItemSets, name, 0, []);
			$nbOfItemSets++;

			int counter1 = tabs;

			while (counter1>0) {
			$outStream.write("\t");
			counter1--;
			}

			$outStream.write("<Node "+name+">");
			tokens.readChar('(');

			do {
			String arg = tokens.readToken();
			$outStream.write("  Arg "+arg);

			new Item($nbOfItems, false, arg);
			$nbOfItems++;

			} while(tokens.isNextToken(','));


		tokens.readChar(')');
		tokens.readChar('?');
		tokens.isNextToken(',');

		}
		$outStream.write("\n");

		int newTab = tabs+1;

	       	for(int idx = 0; idx < $currentNode.getNbChildren(); idx++ ) {

		ClusNode $childNode= (ClusNode)$currentNode.getChild(idx);
		if (!$childNode.atBottomLevel()) depthFirstPrint($childNode, $outStream, newTab, $itemSets,
		$nbOfItems, $nbOfItemSets);

			if (idx==$currentNode.getNbChildren()-1) {

			int counter2 = tabs;

			while (counter2>0){
			$outStream.write("\t");
			counter2--;
			}


			$outStream.write("</Node>\n");

			}

        	}
	} //try

	catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
	}

	} //depthFirstPrint
*/

} //class
