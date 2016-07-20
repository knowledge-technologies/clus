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

package jeans.util.thread;

import java.util.*;
import java.awt.event.*;

public class MWorkerThread extends Thread {

	protected static MWorkerThread m_Instance;
	protected ArrayList m_Jobs = new ArrayList();
	protected boolean m_Running, m_Terminate;
	protected ActionListener m_Idle;

	public MWorkerThread() {
	}

	public void setIdleListener(ActionListener idle) {
		m_Idle = idle;
	}

	public static MWorkerThread getInstance() {
		if (m_Instance == null) m_Instance = new MWorkerThread();
		return m_Instance;
	}

	public synchronized void executeOverwrite(Runnable runner) {
		executeOverwrite(runner, true);
	}

	public synchronized void executeOverwrite(Runnable runner, boolean newThread) {
		if (m_Running) {
			Class cls = runner.getClass();
			for (int i = m_Jobs.size()-1; i >= 0; i--) {
				if (m_Jobs.get(i).getClass() == cls) m_Jobs.remove(i);
			}
		}
		execute(runner, newThread);
	}

	public synchronized void execute(Runnable runner) {
		execute(runner, true);
	}

	public synchronized void execute(Runnable runner, boolean newThread) {
		if (newThread) {
			if (m_Running) {
				m_Jobs.add(runner);
				if (m_Jobs.size() == 1) notify();
			} else {
				m_Jobs.add(runner);
				m_Running = true;
				start();
			}
		} else {
			doRunJob(runner);
		}
	}

	public synchronized Runnable getNextJob() throws InterruptedException {
		if (m_Jobs.size() == 0) {
			m_Idle.actionPerformed(null);
			wait();
		}
		Runnable job = (Runnable)m_Jobs.get(0);
		m_Jobs.remove(0);
		return job;
	}

	public void doRunJob(Runnable job) {
		try {
			job.run();
		} catch (Exception e) {
			System.out.println("Exception: "+e);
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while (!m_Terminate) {
				Runnable job = getNextJob();
				doRunJob(job);
			}
		} catch (InterruptedException e) {}
	}
}
