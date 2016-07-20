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

package jeans.resource;

public class ResourceInfo {

	protected static boolean m_LibLoaded = false;

	public final static boolean isLibLoaded() {
		return m_LibLoaded;
	}

	public final static long getTime() {
		if (isLibLoaded()) return getCPUTime();
		else return System.currentTimeMillis();
	}

	public final static long getMemory() {
		if (isLibLoaded()) return getMemorySize();
		else return -1;
	}

	public native static long getCPUTime();

	public native static long getMemorySize();

	public final static void loadLibrary(boolean test) {
		try {
			System.loadLibrary("ResourceInfo");
			m_LibLoaded = true;
		} catch (UnsatisfiedLinkError e) {
			if (test) {
				System.out.println("Error loading resource info: "+e.getMessage());
				System.out.println("Value of java.library.path: "+System.getProperty("java.library.path"));
			}
		}
	}
}
