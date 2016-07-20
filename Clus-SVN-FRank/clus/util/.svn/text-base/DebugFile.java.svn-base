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

package clus.util;

import java.io.*;

public class DebugFile {

	protected static boolean m_TryCreate;
	protected static PrintWriter m_Writer;

	public static void log(String strg) {
		if (!m_TryCreate) makeWriter();
		if (m_Writer != null) m_Writer.println(strg);
	}

	public static void close() {
		if (m_Writer != null) m_Writer.close();
	}

	public static void exit() {
		close();
		System.exit(-1);
	}

	protected static PrintWriter makeWriter() {
		m_TryCreate = true;
		try {
			return new PrintWriter(new OutputStreamWriter(new FileOutputStream("debug.txt")));
		} catch (IOException e) {
			System.err.println("Error creating debug writer");
			return null;
		}
	}
}
