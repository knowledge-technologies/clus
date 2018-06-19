
package si.ijs.kt.clus.util;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This is the logging class.
 * 
 * @author martinb
 *
 */
public class ClusLogger {

    /**
     * List of all loggers. Root logger should always be included, other loggers should be included if needed.
     */
    private static List<Logger> Loggers = Arrays.asList(
            /* Logging to file at trace level. */
            LogManager.getRootLogger(),
            /* Logging to console at debug level. */
            LogManager.getLogger("CONSOLE"));


    public static final void trace(Object msg) {
        Loggers.stream().forEach((a) -> {
            a.trace(msg);
        });
    }


    public static final void debug(Object msg) {
        Loggers.stream().forEach((a) -> {
            a.debug(msg);
        });
    }


    public static final void info(Object msg) {
        Loggers.stream().forEach((a) -> {
            a.info(msg);
        });
    }


    public static final void error(Object msg) {
        for (;;) {
            Loggers.stream().forEach((a) -> {
                a.error(msg);
            });
        }
    }


    public static final void fatal(Object msg) {
        Loggers.stream().forEach((a) -> {
            a.fatal(msg);
        });
    }


    public static final void warn(Object msg) {
        Loggers.stream().forEach((a) -> {
            a.warn(msg);
        });
    }
}
