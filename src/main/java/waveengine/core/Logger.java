package waveengine.core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static Interface loggerImp;
    public static Interface getLogger() {
        if (loggerImp == null) {
            loggerImp = System.err::println;
        }
        return loggerImp;
    }

    public static void setLogger(Interface loggerImp) {
        Logger.loggerImp = loggerImp;
    }

    public interface Interface {

        void logProvider(String out);

        default void log(String msg) {
            logProvider(getTime() + " [LOG  ] " + msg);
        }

        default void logError(String msg) {
            logProvider(getTime() + " [ERROR] " + msg);
        }

        default void logInfo(String msg) {
            logProvider(getTime() + " [INFO ] " + msg);
        }

        default void logWarning(String msg) {
            logProvider(getTime() + " [WARN ] " + msg);
        }

        default String getTime() {
            SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss::SS");
            Date now = new Date();
            return sdfDate.format(now);
        }
    }
}
