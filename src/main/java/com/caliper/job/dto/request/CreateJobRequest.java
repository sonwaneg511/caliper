package com.caliper.job.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {

    // CreateJobRequest.java
    private String className;
    private String jobName;
    private String groupName;
    private Map<String,String> jobParameters;
    private Integer minutes;    // 1–1440
    private Instant oneTimeAt;  // mutually exclusive with minutes
}
