package waveengine.core;

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
        void log(String msg);
    }
}
