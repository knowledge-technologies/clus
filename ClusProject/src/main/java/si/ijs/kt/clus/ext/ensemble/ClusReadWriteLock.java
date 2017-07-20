/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.ext.ensemble;

import java.io.Serializable;

public class ClusReadWriteLock implements Serializable {

	private int m_NbReaders = 0;
    private int m_NbWriters = 0;
    private int m_NbWriteRequests = 0;


    public synchronized void readingLock() {
        while (m_NbWriters > 0 || m_NbWriteRequests > 0) {
            try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
        }
        m_NbReaders++;
    }


    public synchronized void readingUnlock() {
        m_NbReaders--;
        notifyAll();
    }


    public synchronized void writingLock() {
        m_NbWriteRequests++;
        while (m_NbReaders > 0 || m_NbWriters > 0) {
            try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
        }
        m_NbWriteRequests--;
        m_NbWriters++;
    }


    public synchronized void writingUnlock() {
        m_NbWriters--;
        notifyAll();
    }
}
