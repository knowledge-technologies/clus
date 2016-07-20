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

import java.applet.AudioClip;
import java.applet.Applet;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.*;

/**
 * Loads and holds a bunch of audio files whose locations are specified
 * relative to a fixed base URL.
 */
public class SoundList {

	Applet applet;
	URL baseURL;
	Hashtable table = new Hashtable();

	private static SoundList instance = null;

	public static SoundList getInstance() {
		if (instance == null)
			instance = new SoundList();
		return instance;
	}

	private SoundList() {
	}

	public void setApplet(Applet applet) {
		this.applet = applet;
	}

	public void setDirectory(URL codeBase, String dir) {
		try {
			baseURL = new URL(codeBase, dir);
		} catch (MalformedURLException e) {}
	}

	public void getDirectory(String dir) {
		try {
			URL crDir = new File(".").toURL();
			baseURL = new URL(crDir, dir);
		} catch (MalformedURLException e) {
		}
	}

	public void startLoading(String relativeURL) throws MalformedURLException {
		AudioClip audioClip = null;
		if (applet == null) {
			audioClip = Applet.newAudioClip(new URL(baseURL, relativeURL));
		} else {
			audioClip = applet.getAudioClip(baseURL, relativeURL);
		}
		if (audioClip == null) {
			System.out.println("Error loading audio clip: "+relativeURL);
		}
		putClip(audioClip, relativeURL);
	}

	public AudioClip getClip(String relativeURL) {
		return (AudioClip)table.get(relativeURL);
	}

	private void putClip(AudioClip clip, String relativeURL) {
		table.put(relativeURL, clip);
	}
}
