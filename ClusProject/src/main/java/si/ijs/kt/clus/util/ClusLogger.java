
package si.ijs.kt.clus.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.*;
import si.ijs.kt.clus.main.settings.section.SettingsGeneral;


/**
 * This is the logging class. From anywhere in Clus use ClusLogger.<logging method> to log messages.
 * Default output stream is the console. If you want to log to file, create a logging.properties file in the same folder
 * as the Clus executable.
 * 
 * Read {@link http://www.vogella.com/tutorials/Logging/article.html} before changing anything.
 * 
 * @author martinb
 *
 */
public class ClusLogger {

    private static Logger m_MainLogger;


    /**
     * Initializes the logging for Clus.
     * 
     * @param sets
     */
    public static void initialize(SettingsGeneral sets) {

        // we define the default output format for all loggers
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$s] %5$s %n");

        String lpath = sets.getLoggingProperties();
        try {
            FileInputStream configFile = new FileInputStream(lpath);
            LogManager.getLogManager().readConfiguration(configFile);
        }
        catch (IOException ex) {
            // Could not open configuration file, logging only to console
            System.out.println(String.format("Logging properties file %s not found! Console output only.", lpath));
        }

        m_MainLogger = Logger.getLogger(ClusLogger.class.getSimpleName());
    }


    public static final void info(String msg) {
        m_MainLogger.info(msg);
    }


    public static final void severe(String msg) {
        m_MainLogger.severe(msg);
    }


    public static final void fine(String msg) {
        m_MainLogger.fine(msg);
    }


    public static final void finer(String msg) {
        m_MainLogger.finer(msg);
    }
}
