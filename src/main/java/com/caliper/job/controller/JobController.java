package com.caliper.job.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.job.dto.request.CreateJobRequest;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.caliper.job.dto.response.JobExecutionLogResponse;
import com.caliper.job.dto.response.JobResponse;
import com.caliper.job.entity.JobExecutionLog;
import com.caliper.job.entity.ScheduledJob;
import com.caliper.job.service.JobSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing scheduled jobs.
 *
 * Purpose: Provides HTTP endpoints for:
 * - Creating new scheduled jobs
 * - Viewing all scheduled jobs
 * - Viewing a specific job
 * - Pausing a job
 * - Deleting a job
 * - Viewing execution logs for a job
 *
 * Base URL: /api/jobs
 */
@RestController
@RequestMapping("/api/jobs")
@Slf4j
public class JobController {

    private final JobSchedulerService jobSchedulerService;

    public JobController(JobSchedulerService jobSchedulerService) {
        this.jobSchedulerService = jobSchedulerService;
    }

    /**
     * POST /api/jobs
     *
     * Creates and schedules a new job.
     *
     * Request Body Example (Recurring):
     * {
     *   "className": "com.caliper.jobs.ReportJob",
     *   "jobName": "Daily Report",
     *   "groupName": "reports",
     *   "jobParameters": {
     *     "client": "acme",
     *     "method": "runReport"
     *   },
     *   "minutes": 60
     * }
     *
     * Request Body Example (One-time):
     * {
     *   "className": "com.caliper.jobs.ReportJob",
     *   "jobName": "One-time Report",
     *   "groupName": "reports",
     *   "jobParameters": {
     *     "client": "acme"
     *   },
     *   "oneTimeAt": "2025-01-15T10:00:00Z"
     * }
     *
     * @param request The job creation request
     * @return The created job with HTTP 200 OK
     */
    @PostMapping("/create")
    public ResponseEntity<JobResponse> createJob(@RequestBody CreateJobRequest request) {
        log.info("Received request to create job: {}", request.getJobName());
        ScheduledJob job = jobSchedulerService.schedule(request);
        return ResponseEntity.ok(JobResponse.from(job));
    }

    /**
     * GET /api/jobs
     *
     * Retrieves all scheduled jobs.
     *
     * @return List of all jobs with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        log.info("Retrieving all scheduled jobs");
        List<ScheduledJob> jobs = jobSchedulerService.getAllJobs();
        List<JobResponse> responses = jobs.stream()
                .map(JobResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/jobs/{id}
     *
     * Retrieves a specific job by ID.
     *
     * @param id The job ID
     * @return The job with HTTP 200 OK, or HTTP 404 NOT FOUND if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String id) {
        log.info("Retrieving job: {}", id);
        ScheduledJob job = jobSchedulerService.getJob(id);
        if (job == null) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }
        return ResponseEntity.ok(JobResponse.from(job));
    }

    // Trigger an immediate run (one-off) for a job
    @PostMapping("/{id}/run")
    public ResponseEntity<Void> runNow(@PathVariable String id) {
        log.info("Forcing run for job: {}", id);
        ScheduledJob job = jobSchedulerService.getJob(id);
        if (job == null) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }
        // Fire-and-forget run; it will create a JobExecutionLog entry
        jobSchedulerService.runJob(id);
        return ResponseEntity.accepted().build(); // 202 Accepted
    }

    /**
     * POST /api/jobs/{id}/pause
     *
     * Pauses a job by cancelling its future executions.
     *
     * Note: If the job is currently running, it will finish but won't run again.
     * The job remains in the database and can be resumed later (if you implement resume).
     *
     * @param id The job ID to pause
     * @return HTTP 204 NO CONTENT on success, or HTTP 404 NOT FOUND if job not found
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<Void> pauseJob(@PathVariable String id) {
        log.info("Pausing job: {}", id);
        try {
            jobSchedulerService.pause(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }
    }

    /**
     * DELETE /api/jobs/{id}
     *
     * Deletes a job by cancelling it and removing it from the database.
     *
     * Note: Execution logs are preserved for audit purposes.
     *
     * @param id The job ID to delete
     * @return HTTP 204 NO CONTENT on success, or HTTP 404 NOT FOUND if job not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable String id) {
        log.info("Deleting job: {}", id);
        try {
            jobSchedulerService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }
    }

    /**
     * GET /api/jobs/{id}/logs
     *
     * Retrieves all execution logs for a specific job.
     *
     * Returns logs ordered by most recent first.
     * Each log entry contains:
     * - When the job started and finished
     * - Whether it succeeded or failed
     * - Error message and stack trace (if failed)
     * - Execution duration
     * - Log file details (path, name, size)
     *
     * Note: This endpoint returns log metadata. To get the actual log content,
     * use GET /api/jobs/{id}/logs/{logId}/content
     *
     * @param id The job ID
     * @return List of execution logs with HTTP 200 OK, or HTTP 404 NOT FOUND if job not found
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<JobExecutionLogResponse>> getJobLogs(@PathVariable String id) {
        log.info("Retrieving logs for job: {}", id);
        try {
            List<JobExecutionLog> logs = jobSchedulerService.getJobLogs(id);
            List<JobExecutionLogResponse> responses = logs.stream()
                    .map(JobExecutionLogResponse::from)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }
    }

    /**
     * GET /api/jobs/{id}/logs/{logId}/content
     *
     * Retrieves the actual log content from the log file for a specific execution.
     *
     * This endpoint reads the log file from disk and returns its content.
     * Use this after getting the log list from GET /api/jobs/{id}/logs to view
     * the detailed execution logs.
     *
     * @param id The job ID
     * @param logId The execution log ID
     * @return Log content with HTTP 200 OK, or HTTP 404 NOT FOUND if log not found
     */
    @GetMapping("/{id}/logs/{logId}/content")
    public ResponseEntity<String> getLogContent(
            @PathVariable String id,
            @PathVariable Long logId) {
        log.info("Retrieving log content for job: {}, log: {}", id, logId);
        ScheduledJob job = jobSchedulerService.getJob(id);
        if (job == null) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }

        List<JobExecutionLog> logs = jobSchedulerService.getJobLogs(id);
        JobExecutionLog executionLog = logs.stream()
                .filter(log -> log.getId().equals(logId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Log not found: " + logId));

        String logContent = jobSchedulerService.readLogContent(executionLog);
        if (logContent == null) {
            throw new ResourceNotFoundException("Log file not available for log: " + logId);
        }

        return ResponseEntity.ok(logContent);
    }
}


