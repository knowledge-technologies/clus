package si.ijs.kt.clus.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import si.ijs.kt.clus.main.settings.section.SettingsGeneral;

/**
 * This is the logging class. From anywhere in Clus use ClusLogger.{@literal <}logging
 * method{@literal >} to log messages. Default output stream is the console. If you want to
 * log to file, create a logging.properties file in the same folder as the Clus
 * executable.
 * 
 * @see <a href="http://www.vogella.com/tutorials/Logging/article.html">Read before changing anything.</a>
 * 
 * @author martinb
 *
 */
public class ClusLogger {

	private static Logger m_MainLogger = ClusLogger.initialize_simple();

	/**
	 * Initializes the logging for Clus.
	 * 
	 * @param sets
	 */
	public static void initialize(SettingsGeneral sets) {
		String lpath;
		if (sets == null) {
			lpath = "does not exist";
		} else {
			lpath = sets.getLoggingProperties();
		}

		// we define the default output format for all loggers
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$s] %5$s %n");
		try {
			FileInputStream configFile = new FileInputStream(lpath);
			LogManager.getLogManager().readConfiguration(configFile);
		} catch (IOException ex) {
			// Could not open configuration file, logging only to console
			System.err.println(String.format("Logging properties file %s not found! Console output only.", lpath));
		}

		m_MainLogger = Logger.getLogger("");
		
		Handler[] handlers = m_MainLogger.getHandlers();
		for (Handler h : handlers) {
			m_MainLogger.removeHandler(h);
		}
//		if (handlers[0] instanceof ConsoleHandler) {
//			handlers[0].setLevel(Level.ALL);
			
//		}

		m_MainLogger.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
		m_MainLogger.setLevel(Level.ALL);
	}

	private static Logger initialize_simple() {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$s] %5$s %n");
		Logger l = Logger.getLogger("");
		l.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
		Handler[] handlers = l.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
			handlers[0].setLevel(Level.ALL);
		}

		l.setLevel(Level.ALL);
		return l;
	}
	
	private static void flush() {
		for (Handler h : m_MainLogger.getHandlers()) {
        	h.flush();
        }
	}

    public static final void info(String msg) {
        m_MainLogger.info(msg);
        flush();
    }
    
    public static final void info(double d) {
        m_MainLogger.info(Double.toString(d));
    }

    public static final void info() {
        m_MainLogger.info("");
    }

    public static final void severe(String msg) {
        m_MainLogger.severe(msg);
        flush();
    }


    public static final void fine(String msg) {
        m_MainLogger.fine(msg);
        flush();
    }
    
    public static final void fine() {
    	m_MainLogger.fine("");
    }


    public static final void finer(String msg) {
        m_MainLogger.finer(msg);
        flush();
    }
}
