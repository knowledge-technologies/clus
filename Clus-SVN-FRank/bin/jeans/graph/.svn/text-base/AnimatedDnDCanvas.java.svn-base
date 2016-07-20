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

package jeans.graph;

import java.awt.*;

import jeans.graph.DragNDropCanvas;
import jeans.util.ThreadLock;

public abstract class AnimatedDnDCanvas extends DragNDropCanvas {

	public final static int ANIMATE_MODE_THREAD = 0;
	public final static int ANIMATE_MODE_EVENT = 1;

	public final static int HINT_NONE = 0;
	public final static int HINT_ALL = 1;
	public final static int HINT_ANIMATED = 2;

	private Image m_back_img = null;
	private Graphics m_back_grp = null;
	private boolean m_back_update = false;
	private Animator m_animator = null;
	private boolean m_animate_enabled = false;
	private boolean m_paused = false;
	private int m_animate_mode = ANIMATE_MODE_THREAD;
	private int m_draw_hint = HINT_ALL;
	private int m_count = 0;

	public AnimatedDnDCanvas(int wd, int hi) {
		super(wd,hi);
	}

	public boolean isAnimated() {
		return m_animate_enabled;
	}

	public synchronized void setPaused(boolean paused) {
		if (paused == m_paused) return;
		if (isAnimated() && m_animate_mode == ANIMATE_MODE_THREAD) {
			setAnimatorState(!paused);
		}
		m_paused = paused;
	}

	public synchronized void setAnimateMode(int mode) {
		if (m_animate_mode != mode) {
			if (m_animate_mode == ANIMATE_MODE_THREAD && m_animator != null) {
				m_animator.setEnabled(true);
				m_animator = null;
			}
			m_animate_mode = mode;
		}
	}

	public synchronized void startStopAnimator(boolean start) {
		if (m_animate_enabled == start) return;
		if (!m_paused && m_animate_mode == ANIMATE_MODE_THREAD) setAnimatorState(start);
		m_animate_enabled = start;
	}

	public synchronized void update(Graphics g) {
		reSize(getSize());
		if (isAnimated()) {
			if (m_draw_hint == HINT_ALL || m_back_update) {
				m_back_update = false;
				if (m_back_img == null) {
					m_back_img = createImage(bufSiz.width,bufSiz.height);
					m_back_grp = m_back_img.getGraphics();
				}
				paintBackground(m_back_grp, bufSiz);
			}
			if (m_draw_hint == HINT_ALL || m_draw_hint == HINT_ANIMATED) {
				bufGrp.drawImage(m_back_img, 0, 0, this);
				paintAnimated(bufGrp, bufSiz);
			}
		} else {
			if (m_draw_hint == HINT_ALL) paintBackground(bufGrp, bufSiz);
		}
		toScreen(g, bufSiz);
		m_draw_hint = HINT_NONE;
	}

	public synchronized void redraw() {
		m_draw_hint = HINT_ALL;
		m_back_update = true;
		repaint();
	}

	public synchronized void redrawAnimated() {
		m_draw_hint = HINT_ANIMATED;
		repaint();
	}

	public abstract void paintBackground(Graphics g, Dimension d);

	public abstract void paintAnimated(Graphics g, Dimension d);

	public void clockTick() {
		redrawAnimated();
		m_count++;
	}

	public synchronized int getAnimateCount() {
		if (m_paused) {
			return 0;
		} else {
			return m_count;
		}
	}

	public void reSize(Dimension d) {
	     if ((bufGrp == null) || (d.width != bufSiz.width) || (d.height != bufSiz.height)) {
			if (d.width == 0 || d.height == 0) return;
			bufSiz = d;
			bufImg = createImage(d.width,d.height);
			bufGrp = bufImg.getGraphics();
			if (isAnimated()) {
				m_back_img = createImage(d.width,d.height);
				m_back_grp = m_back_img.getGraphics();
			} else {
				m_back_img = null;
				m_back_update = true;
			}
			m_draw_hint = HINT_ALL;
	      }
	}

	public void paintIt(Graphics g, Dimension d) {
		//Not used anymore
	}

	private void setAnimatorState(boolean start) {
		if (m_animator != null) {
			m_animator.setEnabled(start);
		} else {
			if (start) {
				m_animator = new Animator();
				m_animator.start();
			}
		}
	}

	private class Animator extends Thread {

		private ThreadLock m_enable = new ThreadLock();

		public boolean isEnabled() {
			return !m_enable.getState();
		}

		public void setEnabled(boolean enable) {
			m_enable.setState(!enable);
		}

		public void run() {
			try {
				while (m_animator != null) {
					Thread.sleep(500);
					m_enable.testLock();
					clockTick();
				}
			} catch (InterruptedException e) {}
			m_animator = null;
		}

	}

}
