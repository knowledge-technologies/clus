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

import jeans.util.thread.MCallback;
import jeans.util.thread.MWorkerThread;
import jeans.util.sort.MVectorSorter;
import jeans.util.sort.MStringSorter;

import java.util.*;
import java.io.*;

public abstract class MFileSystem {

	public final static int OP_RELOAD = 0;
	public final static int OP_CHDIR = 1;

	protected MCallback m_hCallback;
	protected MWorkerThread m_hWorker;
	protected Vector m_hDirectories[] = new Vector[2];
	protected Vector m_hFiles[] = new Vector[2];
	protected int m_iCurr;
	protected MStringSorter m_hStringSorter = new MStringSorter();
	protected MFileSorter m_hFileSorter = new MFileSorter();

	public MFileSystem() {
		for (int i=0; i < 2; i++) {
			m_hDirectories[i] = new Vector();
			m_hFiles[i] = new Vector();
		}
	}

	public int getNbDirectories() {
		return m_hDirectories[m_iCurr].size();
	}

	public String getDirectoryAt(int idx) {
		Vector curr = m_hDirectories[m_iCurr];
		if (idx < curr.size()) return (String)curr.elementAt(idx);
		else return "";
	}

	public int getNbFiles() {
		return m_hFiles[m_iCurr].size();
	}

	public MFileEntry getFileAt(int idx) {
		Vector curr = m_hFiles[m_iCurr];
		if (idx < curr.size()) return (MFileEntry)curr.elementAt(idx);
		else return null;
	}

	public MFileEntry getEntry(String name) {
		Vector curr = m_hFiles[m_iCurr];
		for (int i = 0; i < curr.size(); i++) {
			MFileEntry ent = (MFileEntry)curr.elementAt(i);
			if (ent.getName().equals(name)) return ent;
		}
		return null;
	}

	public void clearLists(int idx) {
		m_hFiles[idx].removeAllElements();
		m_hDirectories[idx].removeAllElements();
	}

	public void setCallback(MCallback call) {
		m_hCallback = call;
	}

	public void setWorker(MWorkerThread worker) {
		m_hWorker = worker;
	}

	public void toggleCurr() {
		clearLists(m_iCurr);
		m_iCurr = 1-m_iCurr;
	}

	public int getCurrent() {
		return m_iCurr;
	}

	public int getBuffer() {
		return 1-m_iCurr;
	}

	public void sortFiles(int idx) {
		MVectorSorter.quickSort(m_hFiles[idx], m_hFileSorter);
	}

	public void sortDirectories(int idx) {
		MVectorSorter.quickSort(m_hDirectories[idx], m_hStringSorter);
	}

	public boolean reload(int type) {
		MyWorker job = new MyWorker(null, type, OP_RELOAD);
		if (m_hWorker == null) m_hWorker = MWorkerThread.getInstance();
		m_hWorker.execute(job);
		return true;
	}

	public boolean chdir(String dir, int type) {
		MyWorker job = new MyWorker(dir, type, OP_CHDIR);
		if (m_hWorker == null) m_hWorker = MWorkerThread.getInstance();
		m_hWorker.execute(job);
		return true;
	}

	public abstract InputStream openFile(MFileEntry ent) throws FileNotFoundException, IOException;

	public abstract OutputStream openOutputStream(String name) throws IOException;

	public abstract void doReload();

	public abstract void doChdir(String str);

	public class MyWorker implements Runnable {

		protected Object m_hArg;
		protected int m_iType, m_iOperator;

		public MyWorker(Object arg, int type, int operator) {
			m_hArg = arg;
			m_iType = type;
			m_iOperator = operator;
		}

		public void run() {
			Object result = null;
			switch (m_iOperator) {
				case OP_RELOAD:
					doReload();
					break;
				case OP_CHDIR:
					doChdir((String)m_hArg);
					break;

			}
			if (m_hCallback != null) m_hCallback.callBack(result, m_iType);
		}
	}

}
