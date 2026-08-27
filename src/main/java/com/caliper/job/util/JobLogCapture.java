package com.caliper.job.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Captures all output produced during a job run:
 * - System.out and System.err
 * - SLF4J/Logback logs (if Logback is available)
 *
 * startCapture(): redirects System.out/err and attaches a Logback appender.
 * stopCapture(): restores streams, detaches appender, and returns combined text.
 */
@Component
public class JobLogCapture {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private LogCaptureOutputStream logCaptureOut;
    private LogCaptureOutputStream logCaptureErr;
    private ListAppender<ILoggingEvent> logbackAppender;
    private Logger rootLogger;
    private boolean logbackAvailable = false;

    /**
     * Starts capturing logs.
     *
     * Steps:
     * 1. Save original System.out and System.err
     * 2. Create capture streams
     * 3. Redirect System.out and System.err to capture streams
     * 4. Try to add Logback appender to capture logger output (if Logback is available)
     */
    public void startCapture() {
        // Save original streams
        originalOut = System.out;
        originalErr = System.err;

        // Create capture streams
        logCaptureOut = new LogCaptureOutputStream();
        logCaptureErr = new LogCaptureOutputStream();

        // Redirect System.out and System.err
        System.setOut(new PrintStream(logCaptureOut, true));
        System.setErr(new PrintStream(logCaptureErr, true));

        // Try to capture Logback/SLF4J logs (if Logback is available)
        try {
            ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
            if (loggerFactory instanceof ch.qos.logback.classic.LoggerContext) {
                ch.qos.logback.classic.LoggerContext loggerContext =
                        (ch.qos.logback.classic.LoggerContext) loggerFactory;
                rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

                // Create and add appender to capture log events
                logbackAppender = new ch.qos.logback.core.read.ListAppender<>();
                ((ch.qos.logback.core.read.ListAppender<?>) logbackAppender).setContext(loggerContext);
                ((ch.qos.logback.core.read.ListAppender<?>) logbackAppender).start();
                ((ch.qos.logback.classic.Logger) rootLogger).addAppender(
                        (ch.qos.logback.core.Appender) logbackAppender);
                logbackAvailable = true;
            }
        } catch (Exception e) {
            // Logback not available or different logging framework - that's okay
            // We'll still capture System.out/System.err
            logbackAvailable = false;
        }
    }

    /**
     * Stops capturing logs and returns all captured content.
     *
     * Steps:
     * 1. Restore original System.out and System.err
     * 2. Remove Logback appender (if Logback was used)
     * 3. Combine all captured logs (System.out, System.err, Logger)
     * 4. Return combined log content
     *
     * @return Combined log content from all sources
     */
    public String stopCapture() {
        // Restore original streams
        if (originalOut != null) {
            System.setOut(originalOut);
        }
        if (originalErr != null) {
            System.setErr(originalErr);
        }

        // Remove Logback appender (if Logback was used)
        if (logbackAvailable && logbackAppender != null && rootLogger != null) {
            try {
                ((ch.qos.logback.classic.Logger) rootLogger).detachAppender(
                        (ch.qos.logback.core.Appender) logbackAppender);
                ((ch.qos.logback.core.read.ListAppender<?>) logbackAppender).stop();
            } catch (Exception e) {
                // Ignore errors during cleanup
            }
        }

        // Combine all logs
        StringBuilder combinedLogs = new StringBuilder();

        // Add System.out logs
        if (logCaptureOut != null) {
            String outLogs = logCaptureOut.getLogContent();
            if (!outLogs.isEmpty()) {
                combinedLogs.append("=== STDOUT ===\n");
                combinedLogs.append(outLogs);
                combinedLogs.append("\n");
            }
        }

        // Add System.err logs
        if (logCaptureErr != null) {
            String errLogs = logCaptureErr.getLogContent();
            if (!errLogs.isEmpty()) {
                combinedLogs.append("=== STDERR ===\n");
                combinedLogs.append(errLogs);
                combinedLogs.append("\n");
            }
        }

        // Add Logback/SLF4J logs (if Logback was used)
        if (logbackAvailable && logbackAppender != null) {
            try {
                @SuppressWarnings("unchecked")
                List<ch.qos.logback.classic.spi.ILoggingEvent> logEvents =
                        ((ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>)
                                logbackAppender).list;
                if (!logEvents.isEmpty()) {
                    combinedLogs.append("=== LOGGER OUTPUT ===\n");
                    String loggerLogs = logEvents.stream()
                            .map(event -> String.format("[%s] %s - %s",
                                    event.getLevel(),
                                    event.getLoggerName(),
                                    event.getFormattedMessage()))
                            .collect(Collectors.joining("\n"));
                    combinedLogs.append(loggerLogs);
                    combinedLogs.append("\n");
                }
            } catch (Exception e) {
                // Ignore errors when reading logback logs
            }
        }

        return combinedLogs.toString();
    }

    /**
     * Gets only the captured content without stopping capture.
     * Useful for checking logs during execution.
     *
     * @return Current captured log content
     */
    public String getCurrentLogs() {
        StringBuilder combinedLogs = new StringBuilder();

        if (logCaptureOut != null) {
            String outLogs = logCaptureOut.getLogContent();
            if (!outLogs.isEmpty()) {
                combinedLogs.append("=== STDOUT ===\n").append(outLogs).append("\n");
            }
        }

        if (logCaptureErr != null) {
            String errLogs = logCaptureErr.getLogContent();
            if (!errLogs.isEmpty()) {
                combinedLogs.append("=== STDERR ===\n").append(errLogs).append("\n");
            }
        }

        if (logbackAvailable && logbackAppender != null) {
            try {
                @SuppressWarnings("unchecked")
                List<ch.qos.logback.classic.spi.ILoggingEvent> logEvents =
                        ((ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>)
                                logbackAppender).list;
                if (!logEvents.isEmpty()) {
                    combinedLogs.append("=== LOGGER OUTPUT ===\n");
                    String loggerLogs = logEvents.stream()
                            .map(event -> String.format("[%s] %s - %s",
                                    event.getLevel(),
                                    event.getLoggerName(),
                                    event.getFormattedMessage()))
                            .collect(Collectors.joining("\n"));
                    combinedLogs.append(loggerLogs).append("\n");
                }
            } catch (Exception e) {
                // Ignore errors
            }
        }

        return combinedLogs.toString();
    }
}

