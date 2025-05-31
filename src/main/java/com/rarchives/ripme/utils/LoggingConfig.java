package com.rarchives.ripme.utils;

import java.util.logging.*;

public class LoggingConfig {
    public static void setup() {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();

        for (Handler handler : handlers) {
            handler.setFormatter(new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$s] %3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                            new java.util.Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            });
        }
    }
}
