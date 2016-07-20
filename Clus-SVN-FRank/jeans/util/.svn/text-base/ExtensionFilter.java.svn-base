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

/**
  * A Filter Class to filter Files by Extension.
  */
public class ExtensionFilter implements FilenameFilter {

     /* The Extension*/
	private String extension;

     /**
      * Construct a new Extension Filter.
      *
      * @param	extension	the Extension.
      */
	public ExtensionFilter(String extension) {
		this.extension = extension;
	}

     /**
      * Test if a given File is accepted by this Filter.
      *
      * @param	dir	the Directory in which the File is located.
      * @param	dir	the Name of the File.
      *
      * @returns true if the given File is accepted.
      */
	public boolean accept(File dir, String name) {
		String fileext = FileUtil.getExtension(name);
		return (fileext != null) && fileext.equals(extension);
	}
}
