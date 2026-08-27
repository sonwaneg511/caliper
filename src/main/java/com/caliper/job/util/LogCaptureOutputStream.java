package com.caliper.job.util;


import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Custom OutputStream that captures all output written to it.
 *
 * Purpose: This class captures System.out and System.err output during job execution
 * so we can store it in the database for later viewing.
 *
 * How it works:
 * 1. Wraps a ByteArrayOutputStream to capture bytes
 * 2. Converts captured bytes to String
 * 3. Provides methods to get the captured log content
 * 4. Can be used to redirect System.out/System.err temporarily
 *
 * Usage:
 * LogCaptureOutputStream logCapture = new LogCaptureOutputStream();
 * PrintStream originalOut = System.out;
 * System.setOut(new PrintStream(logCapture));
 * // ... execute job ...
 * String logs = logCapture.getLogContent();
 * System.setOut(originalOut);
 */

@Component
public class LogCaptureOutputStream extends ByteArrayOutputStream {

    /**
     * Gets the captured log content as a String.
     *
     * @return All captured output as a string
     */
    public String getLogContent() {
        try {
            return this.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // Should never happen with UTF-8
            return this.toString();
        }
    }

    /**
     * Clears the captured content.
     * Useful for resetting before a new execution.
     */
    public void clear() {
        this.reset();
    }
}



