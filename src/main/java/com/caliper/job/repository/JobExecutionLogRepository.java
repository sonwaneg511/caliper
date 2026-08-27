package com.caliper.job.repository;

import com.caliper.job.entity.JobExecutionLog;
import com.caliper.job.entity.ScheduledJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for JobExecutionLog entity.
 *
 * Purpose: Provides database operations for job execution logs.
 * Includes custom queries for retrieving logs by job, date range, etc.
 */
@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {

    /**
     * Find all execution logs for a specific job
     * Ordered by most recent first
     *
     * @param job The scheduled job
     * @param pageable Pagination parameters (page number, size, sort)
     * @return Page of execution logs
     */
    Page<JobExecutionLog> findByJobOrderByStartedAtDesc(ScheduledJob job, Pageable pageable);

    /**
     * Find all execution logs for a specific job
     * Returns all logs without pagination (use with caution for large datasets)
     *
     * @param job The scheduled job
     * @return List of all execution logs for that job
     */
    List<JobExecutionLog> findByJobOrderByStartedAtDesc(ScheduledJob job);

    /**
     * Find execution logs within a date range
     * Useful for viewing logs for a specific time period
     *
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @param pageable Pagination parameters
     * @return Page of execution logs in that date range
     */
    @Query("SELECT log FROM JobExecutionLog log WHERE log.startedAt BETWEEN :startDate AND :endDate ORDER BY log.startedAt DESC")
    Page<JobExecutionLog> findByDateRange(@Param("startDate") Instant startDate,
                                          @Param("endDate") Instant endDate,
                                          Pageable pageable);

    /**
     * Find execution logs for a specific job within a date range
     * Combines job filter with date range filter
     *
     * @param job The scheduled job
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @param pageable Pagination parameters
     * @return Page of execution logs matching both criteria
     */
    @Query("SELECT log FROM JobExecutionLog log WHERE log.job = :job AND log.startedAt BETWEEN :startDate AND :endDate ORDER BY log.startedAt DESC")
    Page<JobExecutionLog> findByJobAndDateRange(@Param("job") ScheduledJob job,
                                                @Param("startDate") Instant startDate,
                                                @Param("endDate") Instant endDate,
                                                Pageable pageable);

    /**
     * Count total executions for a job
     * Useful for statistics
     *
     * @param job The scheduled job
     * @return Total number of executions
     */
    long countByJob(ScheduledJob job);

    /**
     * Count failed executions for a job
     * Useful for monitoring job health
     *
     * @param job The scheduled job
     * @return Number of failed executions
     */
    long countByJobAndStatus(ScheduledJob job, JobExecutionLog.ExecutionStatus status);
}



