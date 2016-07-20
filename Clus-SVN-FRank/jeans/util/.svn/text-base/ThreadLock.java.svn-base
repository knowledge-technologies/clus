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

import java.io.PrintStream;

public class ThreadLock {

	private boolean lock = false;

	public synchronized boolean getState() {
		return lock;
	}

	public synchronized void setState(boolean state) {
		if (state) {
			lock = true;
		} else {
			release();
		}
	}

	public synchronized void lock() throws InterruptedException {
		lock = true;
		wait();
	}

	public synchronized void lock(long timeout) throws InterruptedException {
		lock = true;
		wait(timeout);
	}

	public synchronized void release() {
		if (lock) notify();
		lock = false;
	}

	public synchronized void entry() throws InterruptedException {
		boolean lockstate = lock;
		lock = true;
		if (lockstate) wait();
	}

	public synchronized boolean tryEntry() {
		boolean lockstate = lock;
		lock = true;
		return !lockstate;
	}

	public synchronized boolean testLock() throws InterruptedException {
		if (lock) {
			wait();
			return true;
		}
		return false;
	}

	public synchronized boolean testLockDebug(String strg, PrintStream output) throws InterruptedException {
		if (lock) {
			output.println(strg+" waits..");
			wait();
			output.println(strg+" resumes..");
			return true;
		}
		return false;
	}

}
