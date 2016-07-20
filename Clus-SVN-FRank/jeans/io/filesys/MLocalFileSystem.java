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

package jeans.io.filesys;

import java.io.*;

public class MLocalFileSystem extends MFileSystem {

	protected File m_hDir = new File(".");
	protected boolean m_bIsRoot;

	protected void doLoad() {
		int other = getBuffer();
		if (m_bIsRoot) {
			File[] files = File.listRoots();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				m_hDirectories[other].addElement(file.getAbsolutePath());
			}
		} else {
			File[] files = m_hDir.listFiles();
			m_hDirectories[other].addElement("..");
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					m_hDirectories[other].addElement(file.getName());
				} else {
					MFileEntry entry = new MFileEntry(file.getName(), file.length(), file.lastModified());
					m_hFiles[other].addElement(entry);
				}
			}
		}
		sortDirectories(other);
		sortFiles(other);
		toggleCurr();
	}

	public InputStream openFile(MFileEntry ent) throws FileNotFoundException, IOException {
		String path = m_hDir.getCanonicalPath();
		return new FileInputStream(path + File.separator + ent.getName());
	}

	public OutputStream openOutputStream(String name) throws IOException {
		String path = m_hDir.getCanonicalPath();
		return new FileOutputStream(path + File.separator + name);
	}

	public void doReload() {
		doLoad();
	}

	public void doChdir(String str) {
		try {
			String newPath = null;
			String sep = File.separator;
			String path = m_bIsRoot ? "" : m_hDir.getCanonicalPath();
			if (str.equals("..")) {
				if (m_bIsRoot) {
					return;
				} else {
					int idx = path.lastIndexOf(sep);
					if (idx != -1) {
						int len = path.length();
						if (len <= 3 && len >= 2 && path.charAt(1) == ':') {
							m_bIsRoot = true;
							System.out.println("CD: ROOT");
							doLoad();
							return;
						}
						newPath = path.substring(0, idx);
					} else {
						m_bIsRoot = true;
						System.out.println("CD: ROOT");
						doLoad();
					}
				}
			} else {
				int idx = path.lastIndexOf(sep);
				int delta = path.length()-sep.length();
				if (idx != -1 && idx == delta) newPath = path + str;
				else newPath = path + File.separator + str;
			}
			if (newPath != null) {
				File newDir = new File(newPath + sep);
				if (newDir.exists()) {
					m_bIsRoot = false;
					m_hDir = newDir;
					System.out.println("CD: " + newPath + sep);
					doLoad();
				}
			}
		} catch (IOException e) {}
	}
}
