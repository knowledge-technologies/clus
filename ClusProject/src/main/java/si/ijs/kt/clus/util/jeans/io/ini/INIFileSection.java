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

package si.ijs.kt.clus.util.jeans.io.ini;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.jeans.util.MStreamTokenizer;


public class INIFileSection extends INIFileNode {

    public final static long serialVersionUID = 1;

    protected Hashtable<String, INIFileNode> m_hEntries = new Hashtable<String, INIFileNode>();
    protected Vector<INIFileNode> m_hEntryList = new Vector<INIFileNode>();
    protected int m_Index = 1; // index of this section in the INI file



    public INIFileSection(String name) {
        super(name);
    }


    public INIFileSection() {
        this("");
    }


    @Override
    public boolean isSectionGroup() {
        return false;
    }


    @Override
    public boolean isSection() {
        return true;
    }


    @Override
    public INIFileNode cloneNode() {
        INIFileSection sec = new INIFileSection(getName());
        for (Enumeration<INIFileNode> e = getNodes(); e.hasMoreElements();) {
            INIFileNode node = e.nextElement();
            sec.addNode(node.cloneNode());
        }
        return sec;
    }


    public int getNbNodes() {
        return m_hEntryList.size();
    }


    public Enumeration<INIFileNode> getNodes() {
        return m_hEntryList.elements();
    }


    public INIFileEntry getEntry(String name) {
        return (INIFileEntry) m_hEntries.get(name);
    }


    public INIFileNode getNode(String name) {
        return m_hEntries.get(name);
    }


    public void addNode(INIFileNode entry) {
        m_hEntries.put(entry.getName(), entry);
        m_hEntryList.addElement(entry);
        entry.setParent(this);
    }


    public INIFileNode getPathNode(String path, Class type) {
        INIFileNode node = getPathNode(path);
        if (node != null && node.getClass() == type)
            return node;
        else
            return null;
    }


    public INIFileNode getPathNode(String path, int type) {
        INIFileNode node = getPathNode(path);
        if (node == null)
            return null;
        switch (type) {
            case INIFile.TYPE_STRING_ARRAY:
            case INIFile.TYPE_DOUBLE_ARRAY:
                if (node instanceof INIFileArray && ((INIFileArray) node).getType() == type)
                    return node;
                break;
        }
        return null;
    }


    public INIFileNode getPathNode(String path) {
        String nextNode = null, subNode = null;
        if (path.equals(""))
            return this;
        int idx = path.indexOf('.');
        if (idx == -1) {
            nextNode = path;
            subNode = "";
        }
        else {
            nextNode = path.substring(0, idx);
            subNode = path.substring(idx + 1);
        }
        INIFileNode node = getNode(nextNode);
        if (node != null) {
            if (node.isSection())
                return ((INIFileSection) node).getPathNode(subNode);
            else
                return node;
        }
        else {
            ClusLogger.info("Can't find node: " + nextNode);
            for (Enumeration<INIFileNode> e = getNodes(); e.hasMoreElements();) {
                INIFileNode entry = e.nextElement();
                ClusLogger.info("   " + entry.getName());
            }
            return null;
        }
    }


    public void doLoad(String sectionName, String groupName, MStreamTokenizer tokens) throws IOException {
        INIFileNode node = getNode(groupName);
        if (node != null && node instanceof INIFileSectionGroup) {
            INIFileSectionGroup group = (INIFileSectionGroup) node;
            INIFileSection sec = (INIFileSection) group.getPrototype().cloneNode();
            sec.setName(sectionName);
            group.addSection(sec);
            sec.load(tokens);
        }
        else {
            throw new IOException("Error in the settings file. Don't know about group '" + groupName + "' at line: " + tokens.getLine());
        }
    }


    public void doLoad(String name, MStreamTokenizer tokens) throws IOException {
        INIFileNode node = getNode(name);
        if (node != null && node instanceof INIFileSection) {
            INIFileSection section = (INIFileSection) node;
            section.load(tokens);
        }
        else {
            throw new IOException("Error in the settings file. Don't know about section '" + name + "' at line: " + tokens.getLine());
        }
    }


    public void load(MStreamTokenizer tokens) throws IOException {
        setEnabled(true);
        while (true) {
            String token = tokens.getToken();
            if (token == null)
                return;
            if (token.equals("[")) {
                tokens.pushBackToken(token);
                return;
            }
            if (token.equals("<")) {
                if (getDepth() >= 1) {
                    tokens.pushBackToken(token);
                    return;
                }
                // Read subsection
                int saveline = tokens.getLine();
                String name = tokens.readTillEol();
                // Kill trailing '>'
                int idx1 = name.indexOf('>');
                if (idx1 == -1)
                    throw new IOException("Error in the settings file. Character '>' expected at line: " + saveline);
                // Check for sectiongroup ','
                int idx2 = name.indexOf(',');
                if (idx2 != -1) {
                    String groupName = name.substring(0, idx2).trim();
                    String sectionName = name.substring(idx2 + 1, idx1).trim();
                    doLoad(sectionName, groupName, tokens);
                }
                else {
                    doLoad(name.substring(0, idx1).trim(), tokens);
                }
            }
            else {
                // Read single item
                String name = token.trim();
                tokens.readChar('=');
                // Get type name
                INIFileNode entry = getNode(name);
                if (entry != null && entry instanceof INIFileEntry) {
                    ((INIFileEntry) entry).build(tokens);
                }
                else {
                    throw new IOException("Error in the settings file. Don't know about entry '" + name + "' at line: " + tokens.getLine());
                }
            }
        }
    }


    public void save(String group, PrintWriter writer) throws IOException {
        int depth = getDepth();
        if (!isEnabled()) { return; }
        if (group == null) {
            if (depth == 0)
                writer.println("[" + getName() + "]");
            else
                writer.println("<" + getName() + ">");
        }
        else {
            if (depth == 0)
                writer.println("[" + group + ", " + getName() + "]");
            else
                writer.println("<" + group + ", " + getName() + ">");
        }
        for (Enumeration<INIFileNode> e = getNodes(); e.hasMoreElements();) {
            INIFileNode entry = e.nextElement();
            if (entry.isEnabled())
                entry.save(writer);
        }
    }


    @Override
    public void save(PrintWriter writer) throws IOException {
        save(null, writer);
    }
}
