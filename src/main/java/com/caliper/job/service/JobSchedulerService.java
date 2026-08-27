package com.caliper.job.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Map;


import com.caliper.job.dto.request.CreateJobRequest;
import com.caliper.utils.exception.customException.InvalidRequestException;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.caliper.job.entity.JobExecutionLog;
import com.caliper.job.entity.ScheduledJob;
import com.caliper.job.repository.JobExecutionLogRepository;
import com.caliper.job.repository.ScheduledJobRepository;
import com.caliper.job.runtime.ExecutableJob;
import com.caliper.job.service.JobLogFileService.LogFileInfo;
import com.caliper.job.util.JobLogCapture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Service class that manages scheduled jobs.
 *
 * Purpose: This is the core business logic for:
 * - Creating and scheduling new jobs
 * - Pausing jobs (cancelling future executions)
 * - Deleting jobs (cancelling and removing from database)
 * - Executing jobs when their scheduled time arrives
 * - Logging job executions
 *
 * Flow:
 * 1. Client calls POST /api/jobs with CreateJobRequest
 * 2. Controller calls schedule() method
 * 3. Service validates request, creates ScheduledJob entity, saves to database
 * 4. Service schedules the job with Spring TaskScheduler
 * 5. When time arrives, TaskScheduler calls runJob() method
 * 6. runJob() loads the class, instantiates it, calls run() method
 * 7. Execution is logged in JobExecutionLog table
 */
@Service
@Slf4j
public class JobSchedulerService {

    // Spring's TaskScheduler - handles scheduling tasks
    private final TaskScheduler taskScheduler;

    // Repository for database operations on scheduled jobs
    private final ScheduledJobRepository jobRepository;

    // Repository for database operations on execution logs
    private final JobExecutionLogRepository logRepository;

    // Service for managing log files
    private final JobLogFileService logFileService;

    // Map to track scheduled futures (needed for pause/delete operations)
    // Key: job ID, Value: ScheduledFuture that can be cancelled
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    @Autowired
    private JobLogCapture logCapture;

    public JobSchedulerService(
            TaskScheduler taskScheduler,
            ScheduledJobRepository jobRepository,
            JobExecutionLogRepository logRepository,
            JobLogFileService logFileService) {
        this.taskScheduler = taskScheduler;
        this.jobRepository = jobRepository;
        this.logRepository = logRepository;
        this.logFileService = logFileService;
    }

    /**
     * Creates and schedules a new job.
     *
     * Steps:
     * 1. Validate the request (required fields, valid minutes range, etc.)
     * 2. Create ScheduledJob entity from request
     * 3. Save job to database
     * 4. Schedule the job with TaskScheduler (recurring or one-time)
     * 5. Store the ScheduledFuture so we can cancel it later
     * 6. Return the saved job
     *
     * @param request The job creation request from client
     * @return The created and scheduled job
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ScheduledJob schedule(CreateJobRequest request) {
        log.info("Scheduling new job: {}", request.getJobName());

        // Step 1: Validate request
        validateRequest(request);

        // Step 2: Create entity from request
        ScheduledJob job = createJobFromRequest(request);

        // Step 3: Save to database
        job = jobRepository.save(job);
        log.info("Job saved to database with ID: {}", job.getId());

        // Step 4: Schedule the job
        ScheduledFuture<?> future = scheduleJobExecution(job);

        // Step 5: Store future for later cancellation
        scheduledFutures.put(job.getId(), future);

        log.info("Job scheduled successfully. Job ID: {}, Type: {}",
                job.getId(),
                job.getMinutes() != null ? "Recurring" : "One-time");

        return job;
    }

    /**
     * Pauses a job by cancelling its scheduled executions.
     *
     * Steps:
     * 1. Find the job in database
     * 2. Cancel the ScheduledFuture (stops future executions)
     * 3. Remove future from map
     * 4. Update job status to PAUSED
     * 5. Save updated job to database
     *
     * Note: If job is currently running, it will finish but won't run again.
     *
     * @param jobId The ID of the job to pause
     * @throws IllegalArgumentException if job not found
     */
    @Transactional
    public void pause(String jobId) {
        log.info("Pausing job: {}", jobId);

        // Step 1: Find job in database
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // Step 2: Cancel the scheduled future
        ScheduledFuture<?> future = scheduledFutures.remove(jobId);
        if (future != null) {
            // cancel(false) means: don't interrupt if currently running
            boolean cancelled = future.cancel(false);
            log.info("Job future cancelled: {}", cancelled);
        }

        // Step 3: Update status
        job.setStatus(ScheduledJob.JobStatus.PAUSED);
        job.setUpdatedAt(Instant.now());

        // Step 4: Save to database
        jobRepository.save(job);

        log.info("Job paused successfully: {}", jobId);
    }

    /**
     * Deletes a job by cancelling it and removing from database.
     *
     * Steps:
     * 1. Pause the job (cancels future executions)
     * 2. Update status to CANCELLED
     * 3. Delete from database
     *
     * Note: Execution logs are preserved (not deleted) for audit purposes.
     *
     * @param jobId The ID of the job to delete
     * @throws IllegalArgumentException if job not found
     */
    @Transactional
    public void delete(String jobId) {
        log.info("Deleting job: {}", jobId);

        // Step 1: Find job
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // Step 2: Cancel future executions
        ScheduledFuture<?> future = scheduledFutures.remove(jobId);
        if (future != null) {
            future.cancel(false);
        }

        // Step 3: Update status before deletion (optional, for audit)
        job.setStatus(ScheduledJob.JobStatus.CANCELLED);
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);

        // Step 4: Delete from database
        jobRepository.delete(job);

        log.info("Job deleted successfully: {}", jobId);
    }

    /**
     * Retrieves a job by ID.
     *
     * @param jobId The ID of the job
     * @return The job if found, null otherwise
     */
    public ScheduledJob getJob(String jobId) {
        return jobRepository.findById(jobId).orElse(null);
    }

    /**
     * Retrieves all scheduled jobs.
     *
     * @return List of all jobs in database
     */
    public List<ScheduledJob> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * Retrieves all execution logs for a specific job.
     *
     * @param jobId The ID of the job
     * @return List of execution logs, ordered by most recent first
     */
    public List<JobExecutionLog> getJobLogs(String jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        return logRepository.findByJobOrderByStartedAtDesc(job);
    }

    /**
     * Reads log content from file for a specific execution log.
     *
     * @param executionLog The execution log entry
     * @return The log content as a string, or null if file doesn't exist or can't be read
     */
    public String readLogContent(JobExecutionLog executionLog) {
        if (executionLog.getLogFilePath() == null || executionLog.getLogFilePath().isEmpty()) {
            return null;
        }

        try {
            return logFileService.readLogFromFile(executionLog.getLogFilePath());
        } catch (IOException e) {
            log.error("Failed to read log file: {}", executionLog.getLogFilePath(), e);
            return null;
        }
    }

    /**
     * Executes a job when its scheduled time arrives.
     *
     * This method is called by Spring TaskScheduler when:
     * - A recurring job's interval elapses
     * - A one-time job's scheduled time arrives
     *
     * Steps:
     * 1. Load job from database
     * 2. Create execution log entry
     * 3. Update job status to RUNNING
     * 4. Load the job class using reflection
     * 5. Instantiate the class
     * 6. Verify it implements ExecutableJob interface
     * 7. Call the run() method with job parameters
     * 8. Log success or failure
     * 9. Update job status
     *
     * @param jobId The ID of the job to execute
     */
    @Transactional
    public void runJob(String jobId) {
        ScheduledJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("Job not found for execution: {}", jobId);
            return;
        }

        // Skip if job is paused or cancelled
        if (job.getStatus() == ScheduledJob.JobStatus.PAUSED ||
                job.getStatus() == ScheduledJob.JobStatus.CANCELLED) {
            log.info("Skipping execution of {} job: {}", job.getStatus(), jobId);
            return;
        }

        // Create execution log entry
        JobExecutionLog executionLog = new JobExecutionLog();
        executionLog.setJob(job);
        executionLog.setStartedAt(Instant.now());
        executionLog.setStatus(JobExecutionLog.ExecutionStatus.RUNNING);
        executionLog = logRepository.save(executionLog);

        // Update job status to RUNNING
        job.setStatus(ScheduledJob.JobStatus.RUNNING);
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);

        log.info("Executing job: {} (ID: {})", job.getJobName(), jobId);

        // Create log capture utility to capture all output during job execution
        // This will capture: System.out, System.err, and SLF4J/Logback logger output
        //JobLogCapture logCapture = new JobLogCapture();
        String capturedLogs = "";

        try {
            // Start capturing logs BEFORE executing the job
            // This will redirect System.out/System.err and capture logger output
            logCapture.startCapture();

            // Step 4-6: Load class, instantiate, verify interface
            Class<?> jobClass = Class.forName(job.getClassName());
            Object instance = jobClass.getDeclaredConstructor().newInstance();

            if (!(instance instanceof ExecutableJob executableJob)) {
                throw new InvalidRequestException(
                        "Class does not implement ExecutableJob interface: " + job.getClassName());
            }

            // Step 7: Execute the job
            // All output from the job (System.out.println, log.info, log.error, etc.)
            // will be captured by logCapture
            executableJob.run(job.getJobParameters());

            // Stop capturing logs and get all captured content
            capturedLogs = logCapture.stopCapture();

            // Step 8: Write logs to file and store file details in database
            try {
                LogFileInfo fileInfo = logFileService.writeLogToFile(
                        jobId,
                        executionLog.getId(),
                        capturedLogs
                );

                // Store file details in database instead of log content
                executionLog.setLogFilePath(fileInfo.getFilePath());
                executionLog.setLogFileName(fileInfo.getFileName());
                executionLog.setLogFileSizeBytes(fileInfo.getFileSizeBytes());
            } catch (IOException e) {
                log.error("Failed to write log file for job execution: {}", executionLog.getId(), e);
                // Continue without file info - at least save the execution log
            }

            executionLog.setFinishedAt(Instant.now());
            executionLog.setStatus(JobExecutionLog.ExecutionStatus.SUCCESS);
            executionLog.calculateDuration();
            logRepository.save(executionLog);

            // Step 9: Update job status
            // For one-time jobs, mark as COMPLETED
            // For recurring jobs, mark as SCHEDULED (ready for next run)
            if (job.getMinutes() == null) {
                job.setStatus(ScheduledJob.JobStatus.COMPLETED);
            } else {
                job.setStatus(ScheduledJob.JobStatus.SCHEDULED);
            }
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);

            log.info("Job executed successfully: {} (Duration: {} ms)",
                    job.getJobName(),
                    executionLog.getDurationMs());

        } catch (Exception e) {
            // Stop capturing logs even if exception occurred
            // This ensures we capture any error logs before the exception
            try {
                capturedLogs = logCapture.stopCapture();
            } catch (Exception captureException) {
                // If stopping capture fails, at least try to get current logs
                capturedLogs = logCapture.getCurrentLogs();
            }

            // Log failure
            log.error("Job execution failed: {} (ID: {})", job.getJobName(), jobId, e);

            // Append exception details to captured logs
            StringBuilder fullLogs = new StringBuilder();
            if (!capturedLogs.isEmpty()) {
                fullLogs.append(capturedLogs);
                fullLogs.append("\n");
            }
            fullLogs.append("=== EXCEPTION DETAILS ===\n");
            fullLogs.append("Exception: ").append(e.getClass().getName()).append("\n");
            fullLogs.append("Message: ").append(e.getMessage()).append("\n");
            fullLogs.append("Stack Trace:\n").append(getStackTrace(e));

            // Write logs to file and store file details in database
            try {
                LogFileInfo fileInfo = logFileService.writeLogToFile(
                        jobId,
                        executionLog.getId(),
                        fullLogs.toString()
                );

                // Store file details in database
                executionLog.setLogFilePath(fileInfo.getFilePath());
                executionLog.setLogFileName(fileInfo.getFileName());
                executionLog.setLogFileSizeBytes(fileInfo.getFileSizeBytes());
            } catch (IOException ioException) {
                log.error("Failed to write log file for failed job execution: {}", executionLog.getId(), ioException);
                // Continue without file info
            }

            executionLog.setFinishedAt(Instant.now());
            executionLog.setStatus(JobExecutionLog.ExecutionStatus.FAILED);
            executionLog.setErrorMessage(e.getMessage());
            executionLog.setStackTrace(getStackTrace(e));
            executionLog.calculateDuration();
            logRepository.save(executionLog);

            // Update job status to ERROR
            job.setStatus(ScheduledJob.JobStatus.ERROR);
            job.setUpdatedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    /**
     * Validates the create job request.
     *
     * Checks:
     * - className, jobName, groupName are not empty
     * - Either minutes OR oneTimeAt is provided (not both, not neither)
     * - minutes is between 1 and 1440 (1 minute to 24 hours)
     *
     * @param request The request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRequest(CreateJobRequest request) {
        if (!StringUtils.hasText(request.getClassName())) {
            throw new InvalidRequestException("className is required");
        }
        if (!StringUtils.hasText(request.getJobName())) {
            throw new InvalidRequestException("jobName is required");
        }
        if (!StringUtils.hasText(request.getGroupName())) {
            throw new InvalidRequestException("groupName is required");
        }

        boolean hasMinutes = request.getMinutes() != null && request.getMinutes() > 0;
        boolean hasOneTimeAt = request.getOneTimeAt() != null;

        if (!hasMinutes && !hasOneTimeAt) {
            throw new InvalidRequestException(
                    "Either 'minutes' (for recurring) or 'oneTimeAt' (for one-time) must be provided");
        }

        if (hasMinutes && hasOneTimeAt) {
            throw new InvalidRequestException(
                    "Cannot provide both 'minutes' and 'oneTimeAt'. Choose one.");
        }

        if (hasMinutes && (request.getMinutes() < 1 || request.getMinutes() > 1440)) {
            throw new InvalidRequestException(
                    "minutes must be between 1 and 1440 (1 minute to 24 hours)");
        }

        if (hasOneTimeAt && request.getOneTimeAt().isBefore(Instant.now())) {
            throw new InvalidRequestException(
                    "oneTimeAt must be in the future");
        }
    }

    /**
     * Creates a ScheduledJob entity from the request.
     *
     * @param request The create request
     * @return New ScheduledJob entity (not yet saved)
     */
    private ScheduledJob createJobFromRequest(CreateJobRequest request) {
        ScheduledJob job = new ScheduledJob();
        job.setId(UUID.randomUUID().toString());
        job.setClassName(request.getClassName());
        job.setJobName(request.getJobName());
        job.setGroupName(request.getGroupName());
        job.setJobParameters(request.getJobParameters() != null ? request.getJobParameters() : Map.of());
        job.setMinutes(request.getMinutes());
        job.setOneTimeAt(request.getOneTimeAt());
        job.setStatus(ScheduledJob.JobStatus.SCHEDULED);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        return job;
    }

    /**
     * Schedules the job execution with Spring TaskScheduler.
     *
     * For recurring jobs: Uses scheduleAtFixedRate to run every N minutes
     * For one-time jobs: Uses schedule to run at a specific instant
     *
     * @param job The job to schedule
     * @return ScheduledFuture that can be used to cancel the job
     */
    private ScheduledFuture<?> scheduleJobExecution(ScheduledJob job) {
        if (job.getMinutes() != null) {
            // Recurring job: run every N minutes
            log.info("Scheduling recurring job: every {} minutes", job.getMinutes());
            return taskScheduler.scheduleAtFixedRate(
                    () -> runJob(job.getId()),
                    Duration.ofMinutes(job.getMinutes())
            );
        } else {
            // One-time job: run at specific time
            log.info("Scheduling one-time job: at {}", job.getOneTimeAt());
            return taskScheduler.schedule(
                    () -> runJob(job.getId()),
                    job.getOneTimeAt()
            );
        }
    }

    /**
     * Helper method to convert exception to stack trace string.
     *
     * @param e The exception
     * @return Stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}


