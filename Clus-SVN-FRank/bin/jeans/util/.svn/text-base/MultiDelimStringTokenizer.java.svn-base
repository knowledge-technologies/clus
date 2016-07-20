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

public class MultiDelimStringTokenizer {

	public final static String[] $example_delims =
		{"&nbsp;", "&", ";"};

	protected String[] $delims;
	protected String $line;
	protected int[] $delimpos;
	protected int[] $delimlen;
	protected int $pos;
	protected int $len;
	protected int $lastdelim = -1;

	MultiDelimStringTokenizer(String[] delims) {
		$delims = delims;
		$delimpos = new int[$delims.length];
		$delimlen = new int[$delims.length];
		for (int i = 0; i < $delims.length; i++)
			$delimlen[i] = $delims[i].length();
	}

	public void setLine(String line) {
		$pos = 0;
		$line = line;
		$len = line.length();
		int delim = -1;
		while ((delim = checkDelim()) != -1) {
			$lastdelim = delim;
		}
	}

	public boolean hasMoreTokens() {
		return $pos < $len;
	}

	public String nextToken() {
		int delim = -1;
		boolean notdelim = true;
		StringBuffer token = new StringBuffer();
		while ($pos < $len && notdelim) {
			token.append((char)$line.charAt($pos++));
			while ((delim = checkDelim()) != -1) {
				notdelim = false;
				$lastdelim = delim;
			}
		}
		return token.toString();
	}

	public int lastDelim() {
		return $lastdelim;
	}

	public int checkDelim() {
		int ch = 0;
		int mypos = $pos;
		int nbdelims = $delims.length;
		int is_delim = -1;
		for (int i = 0; i < $delims.length; i++) $delimpos[i] = 0;
		while (nbdelims > 0 && mypos <= $len) {
			if (mypos < $len) {
				ch = $line.charAt(mypos++);
			} else {
				mypos++;
				is_delim = 0;
				$pos = mypos;
			}
			for (int i = 0; i < $delims.length; i++) {
				int delps = $delimpos[i];
				if (delps != -1) {
					int delch = $delims[i].charAt(delps);
					if (delch == ch) {
						delps = ++$delimpos[i];
						if (delps >= $delimlen[i]) {
							is_delim = i+1;
							$pos = mypos;
							$delimpos[i] = -1;
							nbdelims--;
						}
					} else {
						$delimpos[i] = -1;
						nbdelims--;
					}
				}
			}
		}
		return is_delim;
	}

	public static void main(String[] args) {
		try {
			String line = null;
			MultiDelimStringTokenizer token = new MultiDelimStringTokenizer($example_delims);
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(args[0])));
			while ((line = reader.readLine()) != null) {
				token.setLine(line);
				while (token.hasMoreTokens()) {
					System.out.println(token.nextToken());
				}
			}
			reader.close();
		} catch (NullPointerException e) {
			System.out.println("You must supply a file name!");
		} catch (IOException e) {
			System.out.println("IO Error: "+e);
		}
	}
}
