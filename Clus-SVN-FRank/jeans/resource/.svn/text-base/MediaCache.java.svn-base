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

import java.util.*;
import java.awt.*;
import java.applet.AudioClip;
import java.net.MalformedURLException;

public class MediaCache {

	private Hashtable sounds = null;
	private Hashtable images = null;
	private boolean m_SoundEnabled = true;
	private boolean m_SoundLoaded = false;
	private static MediaCache instance = null;

	public static MediaCache getInstance() {
		if (instance == null)
			instance = new MediaCache();
		return instance;
	}

	private MediaCache() {
	}

	public void showImagePaths() {
		MediaInterface interf = MediaInterface.getInstance();
		for (Enumeration e = images.keys(); e.hasMoreElements(); ) {
			String name = (String)e.nextElement();
			interf.showImagePath(name);
		}
	}

	public Image getImage(String fname) {
		if (images == null) images = new Hashtable();
		if (images.containsKey(fname)) {
			return (Image)images.get(fname);
		} else {
			return loadImage(fname, fname);
		}
	}

	public Image loadImage(String fname, String key) {
		if (images == null) images = new Hashtable();
		Image image = MediaInterface.getInstance().loadImage(fname);
		images.put(key, image);
		return image;
	}

	public void setSoundEnabled(boolean ena) {
		m_SoundEnabled = ena;
	}

	public boolean isSoundEnabled() {
		return m_SoundEnabled;
	}

	public void setSoundsLoaded(boolean load) {
		m_SoundLoaded = load;
	}

	public boolean isSoundsLoaded() {
		return m_SoundLoaded;
	}

	public void playSound(String key) {
		if (isSoundEnabled()) getSound(key).play();
	}

	public AudioClip getSound(String key) {
		return (AudioClip)sounds.get(key);
	}

	public AudioClip loadSound(String fname, String key) throws MalformedURLException {
		if (sounds == null) sounds = new Hashtable();
		AudioClip sound = MediaInterface.getInstance().loadAudioClip(fname);
		if (sound != null) sounds.put(key, sound);
		return sound;
	}

}
