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
import java.util.*;

/**
 * A Helper Class for working with Files on the Persistent Storage.
 *
 * @author	Jeans
 * @version	1.0
 */
public class FileUtil {

    /**
     * Return the File SASystem Extension of a given File.
     *
     * @param f	The File.
     *
     * @return	The Extension of f.
     */
	public static String getExtension(File f) {
		return getExtension(f.getName());
	}

    /**
     * Return the current working Directory of the File SASystem.
     *
     * @return	The working Directory.
     */
	public static File getCurrentDir() {
		return new File(".");
	}

	public static String cmbPath(String path, String fname) {
		if (path.endsWith("/") || path.endsWith("\\")) {
			return normPath(path + fname);
		} else {
			return normPath(path + File.separator + fname);
		}
	}

	public static String normPath(String path) {
		if (File.separatorChar == '/') {
			return path.replace('\\', File.separatorChar);
		} else {
			return path.replace('/', File.separatorChar);
		}
	}

	public static String getCurrentDirectory() throws IOException {
		return getCurrentDir().getCanonicalPath();
	}

    /**
     * Add an Extension to a given File name.
     *
     * @param fname	The name of the File.
     * @param ext	The Extension.
     *
     * @return	The File name with the Extension.
     */
	public static String addExtension(String fname, String ext) {
		String myext = FileUtil.getExtension(fname);
		if (myext == null || (!myext.equals(ext))) return fname + "." + ext;
		else return fname;
	}

    /**
     * Return the File SASystem Extension of a given (by name) File.
     *
     * @param s	The name of the File.
     *
     * @return	The Extension of the File.
     */
	public static String getExtension(String s) {
		String ext = null;
		int i = s.lastIndexOf('.');
		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

    /**
     * Return the Name of a given File (without the Extension).
     *
     * @param s	The name of the File (possibly including an Extension).
     *
     * @return	The Name of the File.
     */
	public static String getName(String s) {
		String name = s;
		int i = s.lastIndexOf('.');
		if (i > 0 &&  i < s.length() - 1) {
			name = s.substring(0,i);
		}
		return name;
	}

	public static void delete(String fname) {
		File file = new File(fname);
		file.delete();
	}

	public static void mkdir(String fname) {
		File file = new File(fname);
		file.mkdirs();
	}

	public static boolean fileExists(String fname) {
		File file = new File(fname);
		return file.exists();
	}

	public static boolean isNewerOrEqual(String newFile, String oldFile) {
		File newF = new File(newFile);
		File oldF = new File(oldFile);
		if (!newF.exists()) return false;
		if (!oldF.exists()) return true;
		return newF.lastModified() >= oldF.lastModified();
	}

	public static String removePath(String s) {
		String name = s;
		int i = s.lastIndexOf(File.separatorChar);
		if (i >= 0 &&  i < s.length() - 1) {
			name = s.substring(i+1);
		}
		return name;
	}

	public static String getPath(String s) {
		int i = s.lastIndexOf(File.separatorChar);
		if (i >= 0 && i < s.length() - 1) {
			return trimDirSeparator(s.substring(0,i));
		}
		return null;
	}

	public static String trimDirSeparator(String s){
		int len = s.length();
		if (len > 0 && s.charAt(len-1) == File.separatorChar)
			return s.substring(0, len-1);
		return s;
	}

	public static String relativePath(String abs, String prefix) {
		int pos = abs.indexOf(prefix);
		int from = prefix.length()+1;
		if (pos == 0 && from < abs.length()) return abs.substring(from);
		return abs;
	}

	public static boolean isAbsolutePath(String fname) {
		if (fname.length() > 0) {
			if (fname.charAt(0) == File.separatorChar) return true;
			if (fname.length() >= 3 && fname.charAt(1) == ':' &&
			    (fname.charAt(2) == File.separatorChar || fname.charAt(2) == '/')) return true;
			return false;
		} else {
			return false;
		}
	}

    /**
     * Return a list of all the files in a given (by name) Directory.
     *
     * @param dirname	The name of the Directory.
     *
     * @return	The Names of all the Files in that Directory.
     *
     * @exception FileNotFoundException if the Directory in not found.
     */
	public static String[] dirList(String dirname) throws FileNotFoundException {
		File directory = new File(dirname);
		if (directory != null && directory.isDirectory()) {
			return directory.list();
		}
		throw new FileNotFoundException();
	}

	public static ArrayList recursiveFind(File dir, String pattern) throws IOException {
		ArrayList res = new ArrayList();
		if (dir.isDirectory()) recursiveFindAll(dir, pattern, res);
		return res;
	}

	public static void recursiveFindAll(File dir, String pattern, ArrayList res) throws IOException {
		String[] list = dir.list();
		if (list == null) return;
		for (int i = 0; i < list.length; i++) {
			String full = cmbPath(dir.getCanonicalPath(), list[i]);
			if (list[i].endsWith(pattern)) {
				res.add(full);
			}
			File file = new File(full);
			if (file.isDirectory()) recursiveFindAll(file, pattern, res);
		}
	}

    /**
     * Return a list of all the files in a given (by name) Directory, filtered by Extension.
     *
     * @param dirname	The name of the Directory.
     * @param extension	The Extension to filter the Files on.
     *
     * @return	The Names of all the Files in that Directory with the given Extension.
     *
     * @exception FileNotFoundException if the Directory in not found.
     */
	public static String[] dirList(String dirname, String extension) throws FileNotFoundException {
		File directory = new File(dirname);
		if (directory != null && directory.isDirectory()) {
			return directory.list(new ExtensionFilter(extension));
		}
		throw new FileNotFoundException();
	}

    /**
     * Reads a Text File and Returns a list of all the Lines of Text.
     *
     * @param filename	The Name of the File to read.
     *
     * @return The list of Text Lines.
     *
     * @exception FileNotFoundException if the File was not found in the File SASystem.
     * @exception IOException if an error occured while reading the File.
     */
	public static String[] readTextFile(String filename) throws FileNotFoundException, IOException {
		Vector lines = new Vector();
		BufferedReader in;
		String line;
		in = new BufferedReader(new FileReader(filename));
		do {
			line = in.readLine();
			if (line != null) lines.addElement(line);
		} while (line != null);
		in.close();
		String text[] = new String[lines.size()];
		int idx = 0;
		for (Enumeration e = lines.elements(); e.hasMoreElements() ;) {
			text[idx++] = (String)e.nextElement();
		}
		return text;
	}
}

