
package si.ijs.kt.clus.main.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import si.ijs.kt.clus.util.jeans.util.FileUtil;
import si.ijs.kt.clus.util.jeans.util.StringUtils;


public class SettingsGeneric {

    private SettingsOutput m_SettOutput;


    public SettingsGeneric(SettingsOutput settOutput) {
        m_SettOutput = settOutput;
    }

    /***********************************************************************
     * Generic information *
     ***********************************************************************/

    protected Date m_Date;
    protected String m_AppName;
    protected String m_DirName;
    protected String m_Suffix = "";

    public static boolean EXACT_TIME = false;


  

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

        /*
         * added July, 2014, Jurica Levatic, JSI
         * option to gzip prediction files: [Output] GzipPredictions = Yes
         */
        if (m_SettOutput.isGzipOutput()) {
            path += ".gz";
            try {
                return new PrintWriter(new OutputStreamWriter(
                        new GZIPOutputStream(new FileOutputStream(path))));
            }
            catch (IOException ex) {
                System.err.println(ex.toString());
            }
        }
        /* End added by Jurica */

        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), SettingsGeneral.CHARSET));
    }

}
