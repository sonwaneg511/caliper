package com.caliper.job.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Entity class representing a scheduled job in the database.
 *
 * Purpose: This table stores all scheduled jobs that users create via the API.
 * Each job can be either:
 * - Recurring: runs every N minutes (1-1440 minutes = up to 24 hours)
 * - One-time: runs once at a specific time
 *
 * The status field tracks the current state of the job:
 * - SCHEDULED: Job is active and will run
 * - PAUSED: Job execution is paused (can be resumed)
 * - RUNNING: Job is currently executing
 * - COMPLETED: One-time job has finished
 * - CANCELLED: Job was deleted/cancelled
 * - ERROR: Job failed during execution
 */
@Entity
@Table(name = "scheduled_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledJob {
    /**
     * Unique identifier for the job (UUID)
     * Primary key in the database
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Fully qualified class name that implements ExecutableJob interface
     * Example: "com.caliper.jobs.ReportJob"
     * This class will be instantiated and executed when the job runs
     */
    @Column(name = "class_name", nullable = false, length = 500)
    private String className;

    /**
     * Human-readable name for the job
     * Used for identification and display purposes
     */
    @Column(name = "job_name", nullable = false, length = 255)
    private String jobName;

    /**
     * Group name to categorize jobs
     * Useful for organizing jobs by module/feature
     * Example: "analytics", "reports", "notifications"
     */
    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    /**
     * Parameters to pass to the job when it executes
     * Stored as JSON in the database
     * Example: {"client": "acme", "method": "runReport", "date": "2025-01-05"}
     */
    @Column(name = "job_parameters", columnDefinition = "TEXT")
    @Convert(converter = MapToStringConverter.class)
    private Map<String, String> jobParameters;

    /**
     * For recurring jobs: number of minutes between executions
     * Range: 1-1440 (1 minute to 24 hours)
     * NULL for one-time jobs
     */
    @Column(name = "minutes")
    private Integer minutes;

    /**
     * For one-time jobs: UTC timestamp when the job should run
     * NULL for recurring jobs
     */
    @Column(name = "one_time_at")
    private Instant oneTimeAt;

    /**
     * Current status of the job
     * Tracks lifecycle: SCHEDULED -> RUNNING -> SCHEDULED/COMPLETED/ERROR
     * Can be set to PAUSED or CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status;

    /**
     * Timestamp when the job was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the job was last updated
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Enumeration of possible job statuses
     */
    public enum JobStatus {
        SCHEDULED,  // Job is scheduled and will run
        PAUSED,     // Job execution is paused
        RUNNING,     // Job is currently executing
        COMPLETED,   // One-time job has finished successfully
        CANCELLED,   // Job was cancelled/deleted
        ERROR        // Job encountered an error during execution
    }

    /**
     * Converter to store Map<String, String> as JSON string in database.
     *
     * Purpose: Converts Map to JSON string when saving to database,
     * and converts JSON string back to Map when reading from database.
     *
     * Uses Jackson ObjectMapper for proper JSON serialization/deserialization.
     */
    @Converter
    public static class MapToStringConverter implements AttributeConverter<Map<String, String>, String> {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        /**
         * Converts Map to JSON string for database storage.
         *
         * @param attribute The Map to convert
         * @return JSON string representation
         */
        @Override
        public String convertToDatabaseColumn(Map<String, String> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return "{}";
            }
            try {
                // Convert Map to JSON string using Jackson
                return objectMapper.writeValueAsString(attribute);
            } catch (Exception e) {
                // Fallback to empty JSON if serialization fails
                return "{}";
            }
        }

        /**
         * Converts JSON string from database back to Map.
         *
         * @param dbData The JSON string from database
         * @return Map representation
         */
        @Override
        public Map<String, String> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty() || dbData.equals("{}")) {
                return new HashMap<>();
            }
            try {
                // Convert JSON string to Map using Jackson
                TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
                return objectMapper.readValue(dbData, typeRef);
            } catch (Exception e) {
                // Return empty map if deserialization fails
                return new HashMap<>();
            }
        }
    }


}
