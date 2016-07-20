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

package jeans.graph.swing;

import java.io.File;
import java.util.*;
import javax.swing.filechooser.*;

import jeans.util.*;

public class MExtensionFilter extends FileFilter {

	protected String m_Descr;
	protected ArrayList m_List = new ArrayList();

	public MExtensionFilter(String descr) {
		m_Descr = descr;
	}

	public void addExtension(String ext) {
		m_List.add(ext);
	}

	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		String extension = FileUtil.getExtension(f);
		if (extension != null) {
			extension = extension.toLowerCase();
			for (int i = 0; i < m_List.size(); i++) {
				String crext = (String)m_List.get(i);
				if (extension.equals(crext)) return true;
			}
		}
	        return false;
	}

	public String getDescription() {
		return m_Descr;
	}
}
