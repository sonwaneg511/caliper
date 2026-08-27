package com.caliper.job.repository;


import com.caliper.job.entity.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ScheduledJob entity.
 *
 * Purpose: Provides database operations (CRUD) for scheduled jobs.
 * Spring Data JPA automatically implements these methods.
 *
 * Usage: Injected into service classes to perform database operations.
 */
@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, String> {

    /**
     * Find all jobs by group name
     * Useful for filtering jobs by module/feature
     *
     * @param groupName The group name to search for
     * @return List of jobs in that group
     */
    List<ScheduledJob> findByGroupName(String groupName);

    /**
     * Find all jobs by status
     * Useful for finding all paused jobs, running jobs, etc.
     *
     * @param status The status to filter by
     * @return List of jobs with that status
     */
    List<ScheduledJob> findByStatus(ScheduledJob.JobStatus status);

    /**
     * Find a job by its name
     * Useful for checking if a job name already exists
     *
     * @param jobName The job name to search for
     * @return Optional containing the job if found
     */
    Optional<ScheduledJob> findByJobName(String jobName);

    /**
     * Find all jobs by group name and status
     * Combines both filters for more specific queries
     *
     * @param groupName The group name
     * @param status The status
     * @return List of matching jobs
     */
    List<ScheduledJob> findByGroupNameAndStatus(String groupName, ScheduledJob.JobStatus status);
}



