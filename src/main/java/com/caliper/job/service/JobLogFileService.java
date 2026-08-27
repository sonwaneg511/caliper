package com.caliper.job.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

/**
 * Service for managing job execution log files.
 *
 * Purpose: Handles writing logs to files and reading them back.
 *
 * Responsibilities:
 * - Create log file directories if they don't exist
 * - Write log content to files
 * - Read log content from files
 * - Generate unique file names for each execution
 * - Handle file operations safely
 */
@Service
@Slf4j
public class JobLogFileService {

    /**
     * Base directory for storing job execution logs.
     * Configured via application.properties: scheduler.logs.directory
     * Default: ./logs/jobs
     */
    @Value("${scheduler.logs.directory:./logs/jobs}")
    private String logsDirectory;

    /**
     * Date format for log file names.
     * Format: yyyy-MM-dd_HH-mm-ss
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Writes log content to a file and returns the file details.
     *
     * Steps:
     * 1. Create logs directory if it doesn't exist
     * 2. Generate unique file name based on job ID and timestamp
     * 3. Write log content to file
     * 4. Return file path, name, and size
     *
     * @param jobId The ID of the job
     * @param executionId The ID of the execution log entry
     * @param logContent The log content to write
     * @return LogFileInfo containing file path, name, and size
     * @throws IOException if file writing fails
     */
    public LogFileInfo writeLogToFile(String jobId, Long executionId, String logContent) throws IOException {
        // Step 1: Ensure logs directory exists
        Path logsDir = Paths.get(logsDirectory);
        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
            log.info("Created logs directory: {}", logsDir.toAbsolutePath());
        }

        // Step 2: Generate unique file name
        // Format: job-{jobId}-execution-{executionId}-{timestamp}.log
        String timestamp = java.time.LocalDateTime.now().format(DATE_FORMATTER);
        String fileName = String.format("job-%s-execution-%d-%s.log",
                jobId.substring(0, Math.min(8, jobId.length())), // Use first 8 chars of UUID
                executionId,
                timestamp);

        // Step 3: Create file path
        Path filePath = logsDir.resolve(fileName);

        // Step 4: Write log content to file
        Files.writeString(filePath, logContent, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        // Step 5: Get file size
        long fileSize = Files.size(filePath);

        log.debug("Log file written: {} (Size: {} bytes)", filePath, fileSize);

        // Step 6: Return file info
        return new LogFileInfo(
                filePath.toString(),           // Full path
                fileName,                      // File name
                fileSize                       // File size in bytes
        );
    }

    /**
     * Reads log content from a file.
     *
     * @param filePath The path to the log file
     * @return The log content as a string
     * @throws IOException if file reading fails or file doesn't exist
     */
    public String readLogFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("Log file not found: " + filePath);
        }

        return Files.readString(path);
    }

    /**
     * Checks if a log file exists.
     *
     * @param filePath The path to the log file
     * @return true if file exists, false otherwise
     */
    public boolean logFileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Deletes a log file.
     *
     * @param filePath The path to the log file
     * @return true if file was deleted, false if file didn't exist
     * @throws IOException if deletion fails
     */
    public boolean deleteLogFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return false;
        }

        Files.delete(path);
        log.debug("Log file deleted: {}", filePath);
        return true;
    }

    /**
     * Gets the base logs directory path.
     *
     * @return Absolute path to logs directory
     */
    public String getLogsDirectory() {
        return Paths.get(logsDirectory).toAbsolutePath().toString();
    }

    /**
     * Data class to hold log file information.
     */
    public static class LogFileInfo {
        private final String filePath;
        private final String fileName;
        private final long fileSizeBytes;

        public LogFileInfo(String filePath, String fileName, long fileSizeBytes) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.fileSizeBytes = fileSizeBytes;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public long getFileSizeBytes() {
            return fileSizeBytes;
        }
    }
}



