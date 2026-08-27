package com.caliper.reporting.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class PostReportingDataPageResponse {
	
	@JsonProperty("post_reporting_data")
	public PostReportingResponse postDataResponseList;
	
    

}
