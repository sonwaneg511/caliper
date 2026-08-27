package com.caliper.job.dto.response;

import com.caliper.job.entity.JobExecutionLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for returning job execution log information in API responses.
 *
 * Purpose: Used when returning execution logs to the client.
 * Contains information about when a job ran and whether it succeeded or failed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionLogResponse {

    private Long id;
    private String jobId;
    private String jobName;
    private Instant startedAt;
    private Instant finishedAt;
    private JobExecutionLog.ExecutionStatus status;
    private String errorMessage;
    private Long durationMs;
    private String logFilePath;      // Path to the log file
    private String logFileName;      // Name of the log file
    private Long logFileSizeBytes;   // Size of the log file in bytes

    /**
     * Factory method to create JobExecutionLogResponse from JobExecutionLog entity
     * Converts entity to DTO for API response
     */
    public static JobExecutionLogResponse from(JobExecutionLog log) {
        return new JobExecutionLogResponse(
                log.getId(),
                log.getJob().getId(),
                log.getJob().getJobName(),
                log.getStartedAt(),
                log.getFinishedAt(),
                log.getStatus(),
                log.getErrorMessage(),
                log.getDurationMs(),
                log.getLogFilePath(),      // File path where logs are stored
                log.getLogFileName(),      // File name
                log.getLogFileSizeBytes()  // File size
        );
    }
}


