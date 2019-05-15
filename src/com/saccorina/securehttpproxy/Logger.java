package com.saccorina.securehttpproxy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper class for print log in console output.
 *
 * @author Matteo Rinaldini
 * @author Francesco Saccani
 */
public class Logger {

    private static Logger ourInstance = new Logger();

    public static Logger getInstance() {
        return ourInstance;
    }

    private Logger() {}

    /**
     * Log something in output console.
     *
     * @param log The message to log.
     */
    public void log(String log) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[" + formatter.format(now) + "] " + log);
    }

    /**
     * Print the details of an error in console.
     *
     * @param log The message to log.
     * @param throwable The exception thrown.
     */
    public void error(String log, Throwable throwable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();

        System.err.println("[" + formatter.format(now) + "] [ERROR] " + log + ": " + throwable.getMessage());
        throwable.printStackTrace();
    }

}
