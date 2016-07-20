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
import java.applet.AppletContext;
import java.applet.Applet;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * Loads and holds a bunch of audio files whose locations are specified
 * relative to a fixed base URL.
 */
public class MediaInterface {

	private static MediaInterface instance = null;
	protected Applet applet = null;
	protected Class jarBaseClass = null;
	protected URL soundBase = null;
	protected String imageBase = null;
	protected String userDir = null;
	protected Hashtable table = new Hashtable();
	protected int javaVersion = 0;


	protected MediaInterface() {
	}

	public static MediaInterface getInstance() {
		if (instance == null)
			instance = new MediaInterface();
		return instance;
	}

	public int getJavaVersion() {
		if (javaVersion == 0) {
			int major = 0, minor = 0;
			String version = System.getProperty("java.version");
			StringTokenizer tokens = new StringTokenizer(version,".");
			try {
				if (tokens.hasMoreTokens())
					major = Integer.parseInt(tokens.nextToken());
				if (tokens.hasMoreTokens())
					minor = Integer.parseInt(tokens.nextToken());
			} catch (NumberFormatException e) {}
			javaVersion = major * 10000 + minor;
		}
		return javaVersion;
	}

	public boolean supportSounds() {
		return inApplet() || getJavaVersion() >= 10002;
	}

	public AudioClip loadAudioClip(String name) throws MalformedURLException {
		if (soundBase == null) initSoundBase();
		if (inApplet()) {
			System.out.println("Sound base: "+soundBase);
			System.out.println("Name: "+name);
			return applet.getAudioClip(soundBase, name);
		} else {
			if (getJavaVersion() >= 10002) {
				URL url = new URL(soundBase, name);
				return Applet.newAudioClip(url);
			} else {
				return null;
			}
		}
	}

	public InputStream openStream(String fname) throws IOException {
		try {
			if (inApplet()) {
				URL url = new URL(applet.getDocumentBase(), fname);
				return url.openStream();
			} else if (inJar()) {
				URL url = jarBaseClass.getResource(fname);
				return url.openStream();
			} else {
				return new FileInputStream(fname);
			}
		} catch (MalformedURLException e) {
			throw new IOException("Can't open URL: "+fname);
		}
	}

	public OutputStream makeStream(String fname) throws IOException {
		return new FileOutputStream(fname);
	}

	public Image loadImage(String name) {
		String fname = name;
		if (imageBase != null) fname = imageBase + File.separator + name;
		if (inApplet()) {
			return applet.getImage(applet.getCodeBase(), fname);
		} else if (inJar()) {
			URL url = jarBaseClass.getResource(fname);
			return Toolkit.getDefaultToolkit().getImage(url);
		} else {
			return Toolkit.getDefaultToolkit().getImage(fname);
		}
	}

	public void showImagePath(String name) {
		String fname = name;
		if (imageBase != null) fname = imageBase + File.separator + name;
		if (inApplet()) {
			System.out.println("[APPLET] " + fname);
		} else if (inJar()) {
			System.out.println("[JAR] " + fname);
		} else {
			System.out.println(fname);
		}
	}

	public void showRelativeURL(String relative, String target) throws IOException {
		URL url = null;
		try {
			url = getRelativeURL(relative);
		} catch (MalformedURLException e) {
			throw new IOException("Malformed URL: "+relative);
		}
		if (inApplet()) {
			AppletContext context = applet.getAppletContext();
			context.showDocument(url, target);
		} else {
			Runtime runtime = Runtime.getRuntime();
			String[] args = new String[2];
			args[0] = "start";
			args[1] = url.toExternalForm();
			try {
				runtime.exec(args);
			} catch (IOException e) {
				args[0] = "netscape";
				runtime.exec(args);
			}
		}
	}

	public URL getBaseURL() throws IOException, MalformedURLException {
		if (inApplet()) return applet.getCodeBase();
		else return new URL("file:" + getUserDir() + "/");
	}

	public URL getRelativeURL(String dir) throws IOException, MalformedURLException {
		if (dir == null) return getBaseURL();
		else return new URL(getBaseURL(), dir);
	}

	public String getUserDir() {
		if (userDir == null) userDir = System.getProperty("user.dir");
		return userDir;
	}

	public void setSoundDirectory(String dir) throws IOException, MalformedURLException {
		if (inApplet())	soundBase = getRelativeURL(dir+'/');
		else soundBase = getRelativeURL(dir+'/');
	}

	public void setImageDirectory(String dir) {
		imageBase = dir;
	}

	public void setDirectory(String dir) throws IOException, MalformedURLException {
		setSoundDirectory(dir);
		setImageDirectory(dir);
	}

	public void setJarBase(Class cl) {
		jarBaseClass = cl;
	}

	public boolean inJar() {
		return jarBaseClass != null;
	}

	public void setApplet(Applet applet) {
		this.applet = applet;
	}

	public boolean inApplet() {
		return applet != null;
	}

	protected void initSoundBase() {
		try {
			setSoundDirectory(null);
		} catch (IOException e) {}
	}
}
