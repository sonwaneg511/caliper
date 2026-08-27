package com.caliper.job.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class for the scheduler.
 *
 * Purpose: Configures Spring's TaskScheduler bean that will be used
 * to schedule and execute jobs.
 *
 * ThreadPoolTaskScheduler provides:
 * - Thread pool for executing scheduled tasks
 * - Ability to schedule tasks at fixed intervals or specific times
 * - Thread management and lifecycle
 */
@Configuration
public class SchedulerConfig {

    /**
     * Creates and configures the TaskScheduler bean.
     *
     * ThreadPoolTaskScheduler:
     * - Uses a thread pool to execute scheduled tasks
     * - poolSize: Number of threads in the pool (10 by default)
     * - threadNamePrefix: Prefix for thread names (helps with debugging)
     * - waitForTasksToCompleteOnShutdown: Wait for running tasks to finish on shutdown
     * - awaitTerminationSeconds: How long to wait for tasks to complete
     *
     * @return Configured TaskScheduler instance
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Set the number of threads in the pool
        // Increase this if you have many concurrent jobs
        scheduler.setPoolSize(10);

        // Set thread name prefix for easier debugging
        scheduler.setThreadNamePrefix("scheduled-caliper-job-");

        // Wait for tasks to complete on shutdown
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        // Wait up to 60 seconds for tasks to complete
        //scheduler.setAwaitTerminationSeconds(60);

        // Initialize the scheduler
        scheduler.initialize();

        return scheduler;
    }
}


