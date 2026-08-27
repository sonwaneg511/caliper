package com.caliper.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity class representing execution logs for scheduled jobs.
 *
 * Purpose: This table stores a log entry every time a scheduled job runs.
 * It tracks:
 * - When the job started and finished
 * - Whether it succeeded or failed
 * - Error messages if it failed
 * - Execution duration
 *
 * This allows users to view the history and status of all job executions.
 */
@Entity
@Table(name = "job_execution_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionLog {

    /**
     * Unique identifier for the log entry
     * Auto-generated primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Foreign key reference to the scheduled job
     * Links this log entry to the job that executed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private ScheduledJob job;

    /**
     * Timestamp when the job execution started
     */
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    /**
     * Timestamp when the job execution finished
     * NULL if the job is still running or was interrupted
     */
    @Column(name = "finished_at")
    private Instant finishedAt;

    /**
     * Status of this execution
     * SUCCESS: Job completed successfully
     * FAILED: Job threw an exception
     * RUNNING: Job is still executing (should be rare in logs)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExecutionStatus status;

    /**
     * Error message if the execution failed
     * NULL if execution was successful
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Stack trace if the execution failed
     * NULL if execution was successful
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * Duration of execution in milliseconds
     * Calculated as: finishedAt - startedAt
     * NULL if job hasn't finished yet
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * File path where the log output is stored.
     *
     * Instead of storing log content directly in the database, logs are written to files
     * and this field stores the relative or absolute path to the log file.
     *
     * Example: "logs/jobs/job-12345-execution-67890.log"
     *
     * The actual log content can be retrieved by reading this file.
     */
    @Column(name = "log_file_path", length = 500)
    private String logFilePath;

    /**
     * File name of the log file.
     *
     * Example: "job-12345-execution-67890.log"
     */
    @Column(name = "log_file_name", length = 255)
    private String logFileName;

    /**
     * Size of the log file in bytes.
     * Useful for displaying file size to users.
     */
    @Column(name = "log_file_size_bytes")
    private Long logFileSizeBytes;

    /**
     * Enumeration of execution statuses
     */
    public enum ExecutionStatus {
        SUCCESS,  // Job executed successfully
        FAILED,   // Job threw an exception
        RUNNING   // Job is currently executing (rare in logs)
    }

    /**
     * Helper method to calculate duration
     * Called after job finishes to set durationMs
     */
    public void calculateDuration() {
        if (finishedAt != null && startedAt != null) {
            this.durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();
        }
    }
}

