package clus.main.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;


import clus.jeans.util.FileUtil;
import clus.jeans.util.StringUtils;

public class SettingsGeneric {

    /***********************************************************************
     * Generic information *
     ***********************************************************************/

    protected Date m_Date;
    protected String m_AppName;
    protected String m_DirName;
    protected String m_Suffix = "";

    public static int VERBOSE = 1; // TODO: migrate to Log4J
    public static boolean EXACT_TIME = false;


    public int getVerbose() {
        return VERBOSE;
    }
    public int enableVerbose(int talk) {
        
        int prev = VERBOSE;
        VERBOSE = talk;
        return prev;
    }

    
    public Date getDate() {
        return m_Date;
    }


    public void setDate(Date date) {
        m_Date = date;
    }


    public String getAppName() {
        return m_AppName;
    }


    public String getAppNameWithSuffix() {
        return m_AppName + m_Suffix;
    }


    public void setSuffix(String suffix) {
        m_Suffix = suffix;
    }


    public void setAppName(String file) {
        file = StringUtils.removeSuffix(file, ".gz");
        file = StringUtils.removeSuffix(file, ".arff");
        file = StringUtils.removeSuffix(file, ".s");
        file = StringUtils.removeSuffix(file, ".");
        m_AppName = FileUtil.removePath(file);
        m_DirName = FileUtil.getPath(file);
    }
    
    
    public String getFileAbsolute(String fname) {
        if (m_DirName == null) {
            return fname;
        }
        else {
            if (FileUtil.isAbsolutePath(fname)) {
                return fname;
            }
            else {
                return m_DirName + File.separator + fname;
            }
        }
    }


    public PrintWriter getFileAbsoluteWriter(String fname) throws FileNotFoundException {
        String path = getFileAbsolute(fname);
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), SettingsGeneral.CHARSET));
    }

}
