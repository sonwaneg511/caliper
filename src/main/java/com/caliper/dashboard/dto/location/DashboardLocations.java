package com.caliper.dashboard.dto.location;

import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("location")
public class DashboardLocations {

    private Long totalLocations;
    private Long gmbLocations;
    private Long metaLocations;
    private Long micrositeLocations;
    private Long auditScore;
}