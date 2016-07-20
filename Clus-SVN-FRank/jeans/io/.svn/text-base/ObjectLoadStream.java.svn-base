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

package jeans.io;

import java.io.*;
import java.util.zip.*;

/**
 * A class implementing an interface to the loading of objects from a file
 *
 * @author	Jan, Peter
 * @version	1.0
 */
public class ObjectLoadStream extends ObjectInputStream {


    /**
     * Opens a file for loading objects from
     *
     * @param	fileName	the file name of the file to load from
     *
     * @pre     (fileName != null) and
     *		the file exists
     *		and no file should be opened by the class
     *
     * @effect  the file specified by fileName is opened for reading
     *
     * @exception IOException
     *			if an error occurred while opening the file
     */
	public ObjectLoadStream(InputStream file) throws IOException {
		super(makeStream(file));
	}

	private static ZipInputStream makeStream(InputStream file) throws IOException {
		ZipInputStream zip = new ZipInputStream(file);
		zip.getNextEntry();
		return zip;
	}
}
