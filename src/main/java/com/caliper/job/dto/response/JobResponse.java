package com.caliper.job.dto.response;

import com.caliper.job.entity.ScheduledJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for returning job information in API responses.
 *
 * Purpose: Used when returning job details to the client.
 * Contains all relevant information about a scheduled job.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private String id;
    private String className;
    private String jobName;
    private String groupName;
    private Map<String, String> jobParameters;
    private Integer minutes;
    private Instant oneTimeAt;
    private ScheduledJob.JobStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Factory method to create JobResponse from ScheduledJob entity
     * Converts entity to DTO for API response
     */
    public static JobResponse from(ScheduledJob job) {
        return new JobResponse(
                job.getId(),
                job.getClassName(),
                job.getJobName(),
                job.getGroupName(),
                job.getJobParameters(),
                job.getMinutes(),
                job.getOneTimeAt(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}


