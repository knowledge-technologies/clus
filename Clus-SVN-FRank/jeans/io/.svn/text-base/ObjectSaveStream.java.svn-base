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
 * A class implementing an interface to the saving of objects to a file
 *
 * @author	Peter
 * @version	1.0
 */
public class ObjectSaveStream {

	private ZipOutputStream zip;
	private ObjectOutputStream stream;


    /**
     * Opens a file for saving objects to
     *
     * @param	fileName	the file name of the file to save to
     *
     * @pre     (fileName != null)
     *		and no file should be opened by the class
     *
     * @effect  the file specified by fileName is opened for writing
     *
     * @exception IOException
     *			if an error occurred while opening or creating the file
     */
	public ObjectSaveStream(OutputStream file) throws IOException {
		zip = new ZipOutputStream(file);
		ZipEntry entry = new ZipEntry("data");
		entry.setMethod(ZipEntry.DEFLATED);
		zip.putNextEntry(entry);
		stream = new ObjectOutputStream(zip);
	}

    /**
     * Closes a file previously opened for saving
     *
     * @pre     a file has been opened for saving by calling initiate(String)
     *
     * @effect  the currently open file is closed
     *
     * @exception IOException
     *			if an error occurred while closing the file
     */
	public void close() throws IOException {
		zip.closeEntry();
		stream.close();
	}

	public void closeEntry() throws IOException {
		zip.closeEntry();
	}

	public void writeObject(Object object) throws IOException {
		stream.writeObject(object);
	}
}
