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

package clus.data.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusSchema;
import clus.ext.hierarchicalmtr.ClusHMTRHierarchy;
import clus.io.ClusSerializable;
import clus.main.settings.Settings;
import clus.util.ClusException;


public class ClusView {

    protected ArrayList<ClusSerializable> m_Attr = new ArrayList<ClusSerializable>();


    public int getNbAttributes() {
        return m_Attr.size();
    }


    public ClusSerializable getAttribute(int idx) {
        return (ClusSerializable) m_Attr.get(idx);
    }


    public void addAttribute(ClusSerializable attr) {
        m_Attr.add(attr);
    }


    public RowData readData(ClusReader reader, ClusSchema schema) throws IOException, ClusException {
        schema.setReader(true);
        ArrayList<DataTuple> items = new ArrayList<DataTuple>();
        DataTuple tuple = readDataTupleFirst(reader, schema);
        while (tuple != null) {
            items.add(tuple);
            tuple = readDataTupleNext(reader, schema);
        }
        for (int j = 0; j < m_Attr.size(); j++) {
            ClusSerializable attr = (ClusSerializable) m_Attr.get(j);
            attr.term(schema);
        }
        schema.setReader(false);
        return new RowData(items, schema);
    }


    public DataTuple readDataTupleFirst(ClusReader reader, ClusSchema schema) throws IOException, ClusException {
        if (!reader.hasMoreTokens())
            return null;
        boolean sparse = reader.isNextChar('{');
        if (sparse) {
            m_Attr.clear();
            schema.ensureSparse();
            schema.createNormalView(this);
        }
        return readDataTuple(reader, schema, sparse);
    }


    public DataTuple readDataTupleNext(ClusReader reader, ClusSchema schema) throws IOException {
        if (!reader.hasMoreTokens())
            return null;
        boolean sparse = reader.isNextChar('{');
        if (sparse && !schema.isSparse()) { throw new IOException("Sparse tuple found in a non-sparse data set (at row " + (reader.getRow() + 1) + ")"); }
        return readDataTuple(reader, schema, sparse);
    }


    public DataTuple readDataTuple(ClusReader reader, ClusSchema schema) throws IOException {
        if (!reader.hasMoreTokens())
            return null;
        boolean sparse = reader.isNextChar('{');
        return readDataTuple(reader, schema, sparse);
    }


    public DataTuple readDataTuple(ClusReader reader, ClusSchema schema, boolean sparse) throws IOException {
        DataTuple tuple = schema.createTuple();
        if (sparse) {
            while (!reader.isNextChar('}')) {
                int idx = reader.readIntIndex();
                if (idx < 1 || idx > m_Attr.size()) { throw new IOException("Error attribute index '" + idx + "' out of range [1," + m_Attr.size() + "] at row " + (reader.getRow() + 1)); }
                ClusSerializable attr = (ClusSerializable) m_Attr.get(idx - 1);
                if (!attr.read(reader, tuple)) { throw new IOException("Error reading attribute " + m_Attr + " at row " + (reader.getRow() + 1)); }
            }
        }
        else {
            if (m_Attr.size() > 0) {
                ClusSerializable attr_0 = (ClusSerializable) m_Attr.get(0);
                if (!attr_0.read(reader, tuple))
                    return null;
                for (int j = 1; j < m_Attr.size(); j++) {
                    ClusSerializable attr = (ClusSerializable) m_Attr.get(j);
                    if (!attr.read(reader, tuple)) { throw new IOException("Error reading attribute with index " + j + " (" + m_Attr.get(j) + "), at row " + (reader.getRow() + 1)); }
                }
            }
        }
        // Attribute read operations eat ',' after attribute field
        if (reader.isNextCharNoSpace('{')) {
            if (!reader.readNoSpace()) { throw new IOException("Error reading tuple weight at row " + (reader.getRow() + 1)); }
            tuple.setWeight(reader.getFloat());
            if (!reader.isNextChar('}')) { throw new IOException("Expected closing '}' after tuple weight at row " + (reader.getRow() + 1)); }
        }
        reader.readEol();
        return tuple;
    }


    //  ***************** HMTR ********************

    public RowData readDataHMTR(ClusReader reader, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy, Settings settings) throws IOException, ClusException {

        schema.setReader(true);
        ArrayList<DataTuple> items = new ArrayList<DataTuple>();

        FileWriter fr = null;
        BufferedWriter br = null;

        File file = Paths.get(settings.getData().getDataFile() + ".hmtr").toFile();

        String newLine = System.lineSeparator();

        boolean incorrectDump = true;

        try {

            String agg = "";
            String hier = "";
            String line = "";

            if (file.exists()) {

                FileInputStream inputStream = null;
                Scanner sc = null;
                try {
                    inputStream = new FileInputStream(file);
                    sc = new Scanner(inputStream, "UTF-8");

                    if (sc.hasNextLine())
                        agg = sc.nextLine();
                    if (sc.hasNextLine())
                        hier = sc.nextLine();
                    if (sc.hasNextLine())
                        sc.nextLine();

                    String aggFromS = settings.getHMTR().getHMTRAggregationName();
                    String hierFromS = settings.getHMTR().getHMTRHierarchyString();

                    if (agg.equals(aggFromS) && hier.equals(hierFromS)) {
                        incorrectDump = false;

                        schema.getSettings().getHMTR().setHMTRUsingDump(true);
                        //ClassHMTRHierarchy.setIsUsingDump(true);

                        if (sc.hasNextLine()) {
                            line = sc.nextLine();
                        }
                        else {
                            throw new IOException("Dump has different number of rows! Try deleting the dump file: " + file.getAbsolutePath());
                        }

                        if (line.equals("") || line.equals(" ") || line.equals("\t")) {
                            if (sc.hasNextLine()) {
                                line = sc.nextLine();
                            }
                            else {
                                throw new IOException("Dump has different number of rows! Try deleting the dump file: " + file.getAbsolutePath());
                            }
                        }

                        DataTuple tuple = readDataHMTRTupleFirst(reader, schema, hmtrHierarchy, line);

                        while (tuple != null) {
                            items.add(tuple);
                            String oldline = line;
                            if (sc.hasNextLine()) {
                                line = sc.nextLine();
                            }
                            if (line.equals("") || line.equals(" ") || line.equals("\t")) {
                                if (sc.hasNextLine()) {
                                    line = sc.nextLine();
                                }
                                else {
                                    throw new IOException("Dump has different number of rows! Try deleting the dump file: " + file.getAbsolutePath());
                                }
                            }
                            tuple = readDataHMTRTupleNext(reader, schema, hmtrHierarchy, line);
                            //                            if (tuple != null && line.equals(oldline)) {
                            //                                throw new IOException("Dump has different number of rows! Try deleting the dump file: " + file.getAbsolutePath());
                            //                                }
                        }

                    }

                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (sc != null) {
                        sc.close();
                    }
                }

            }

            if (incorrectDump) {
                fr = new FileWriter(file);
                br = new BufferedWriter(fr);

                br.write(settings.getHMTR().getHMTRAggregationName() + newLine +
                        settings.getHMTR().getHMTRHierarchyString() + newLine + newLine);

                DataTuple tuple = readDataHMTRTupleFirst(reader, schema, hmtrHierarchy, line);

                String toWrites[];
                String toWrite;

                while (tuple != null) {
                    toWrites = tuple.toString().split(",");
                    toWrite = "";

                    for (int i = toWrites.length - schema.getNbHMTR(); i < toWrites.length; i++) {
                        toWrite += "," + toWrites[i];
                    }
                    toWrite = toWrite.substring(1);

                    items.add(tuple);
                    br.write(toWrite + newLine);
                    tuple = readDataHMTRTupleNext(reader, schema, hmtrHierarchy, line);
                }

            }

        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int j = 0; j < m_Attr.size(); j++) {
            ClusSerializable attr = (ClusSerializable) m_Attr.get(j);
            attr.term(schema);
        }
        schema.setReader(false);

        return new RowData(items, schema);
    }


    public DataTuple readDataHMTRTupleFirst(ClusReader reader, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy, String line) throws IOException, ClusException {
        if (!reader.hasMoreTokens())
            return null;
        boolean sparse = reader.isNextChar('{');
        if (sparse) {
            m_Attr.clear();
            schema.ensureSparse();
            schema.createNormalView(this);
        }
        return readDataHMTRTuple(reader, schema, sparse, hmtrHierarchy, line);
    }


    public DataTuple readDataHMTRTupleNext(ClusReader reader, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy, String line) throws IOException {
        if (!reader.hasMoreTokens())
            return null;
        boolean sparse = reader.isNextChar('{');
        if (sparse && !schema.isSparse()) { throw new IOException("Sparse tuple found in a non-sparse data set (at row " + (reader.getRow() + 1) + ")"); }
        return readDataHMTRTuple(reader, schema, sparse, hmtrHierarchy, line);
    }


    public DataTuple readDataHMTRTuple(ClusReader reader, ClusSchema schema, ClusHMTRHierarchy hmtrHierarchy, String line) throws IOException {
        if (!reader.hasMoreTokens())
            return null;
        boolean sparse = reader.isNextChar('{');
        return readDataHMTRTuple(reader, schema, sparse, hmtrHierarchy, line);
    }


    public DataTuple readDataHMTRTuple(ClusReader reader, ClusSchema schema, boolean sparse, ClusHMTRHierarchy hmtrHierarchy, String line) throws IOException {
        DataTuple tuple = schema.createTuple();
        if (sparse) {
            while (!reader.isNextChar('}')) {
                int idx = reader.readIntIndex();
                if (idx < 1 || idx > m_Attr.size()) { throw new IOException("Error attribute index '" + idx + "' out of range [1," + m_Attr.size() + "] at row " + (reader.getRow() + 1)); }
                ClusSerializable attr = (ClusSerializable) m_Attr.get(idx - 1);
                if (!attr.read(reader, tuple)) { throw new IOException("Error reading attirbute " + m_Attr + " at row " + (reader.getRow() + 1)); }
            }
        }
        else {
            if (m_Attr.size() > 0) {
                ClusSerializable attr_0 = (ClusSerializable) m_Attr.get(0);
                if (!attr_0.read(reader, tuple))
                    return null;
                for (int j = 1; j < m_Attr.size() - schema.getNbHMTR(); j++) {
                    ClusSerializable attr = (ClusSerializable) m_Attr.get(j);
                    if (!attr.read(reader, tuple)) { throw new IOException("Error reading attirbute " + m_Attr + " at row " + (reader.getRow() + 1)); }
                }

                if (schema.getSettings().getHMTR().isHMTRUsingDump()) {
                    // read attributes from dump
                    for (int j = m_Attr.size() - schema.getNbHMTR(); j < m_Attr.size(); j++) {
                        ClusSerializable attr = (ClusSerializable) m_Attr.get(j);
                        if (!attr.readHMTRAttribute(reader, tuple, schema, hmtrHierarchy, line)) { throw new IOException("Error calculating Hierarchical MTR attribute " + m_Attr + " at row " + (reader.getRow() + 1)); }
                    }

                }
                else {

                    // do not read but calculate the Hierarchical MTR aggregate attributes
                    for (int j = m_Attr.size() - schema.getNbHMTR(); j < m_Attr.size(); j++) {
                        ClusSerializable attr = (ClusSerializable) m_Attr.get(j);
                        if (!attr.calculateHMTRAttribute(reader, tuple, schema, hmtrHierarchy)) { throw new IOException("Error calculating Hierarchical MTR attribute " + m_Attr + " at row " + (reader.getRow() + 1)); }
                    }

                }

            }
        }
        // Attribute read operations eat ',' after attribute field
        if (reader.isNextCharNoSpace('{')) {
            if (!reader.readNoSpace()) { throw new IOException("Error reading tuple weight at row " + (reader.getRow() + 1)); }
            tuple.setWeight(reader.getFloat());
            if (!reader.isNextChar('}')) { throw new IOException("Expected closing '}' after tuple weight at row " + (reader.getRow() + 1)); }
        }
        reader.readEol();
        return tuple;
    }

}
