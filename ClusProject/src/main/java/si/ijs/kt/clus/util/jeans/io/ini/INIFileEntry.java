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

import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.jeans.util.MStreamTokenizer;


public abstract class INIFileEntry extends INIFileNode {
 
    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;
    protected String m_Name;


    public INIFileEntry(String name) {
        super(name);
    }


    @Override
    public boolean isSectionGroup() {
        return false;
    }


    @Override
    public boolean isSection() {
        return false;
    }


    public abstract String getStringValue();


    public abstract void setValue(String value) throws IOException;


    public void build(MStreamTokenizer tokens) throws IOException {
        String line = tokens.readTillEol();
        if (line == null || line.equals("")) { throw new IOException("Expected value for entry '" + getName() + "'" + tokens.getFileNameForErrorMsg()); }
        setValue(line.trim());
    }


    @Override
    public void save(PrintWriter writer) throws IOException {
        writer.println(getName() + " = " + getStringValue());
    }
}
