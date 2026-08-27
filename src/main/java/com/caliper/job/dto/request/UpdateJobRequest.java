package com.caliper.job.dto.request;

import com.caliper.job.entity.ScheduledJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateJobRequest {

    // UpdateJobRequest.java (for edit)
    private String jobName;
    private String groupName;
    private Map<String,String> jobParameters;
    private Integer minutes;
    private Instant oneTimeAt;
    private ScheduledJob.JobStatus status; // optional
}
