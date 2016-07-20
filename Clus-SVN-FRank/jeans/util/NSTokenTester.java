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

package jeans.util;

import java.io.*;

public class NSTokenTester {


	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Illegal number of arguments.");
			return;
		}
		System.out.println("Reading file: "+args[0]);
		try {
			NStreamTokenizer tokens = new NStreamTokenizer(new FileReader(args[0]));
			/*String token = "-------------";
			while (token != null) {
				System.out.println(token);
				token = tokens.readTillEol();
			}*/
			char ch = tokens.readChar();
			while (ch != NStreamTokenizer.CHAR_EOF) {
				System.out.print(ch);
				ch = tokens.readChar();
			}
			System.out.println();
			tokens.close();
		} catch (IOException e) {
			System.out.println("Error: "+e);
		}

	}


}
