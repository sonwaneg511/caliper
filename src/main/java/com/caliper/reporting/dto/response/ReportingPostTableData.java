package com.caliper.reporting.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class ReportingPostTableData {
	
	@JsonProperty("post_data")
	public List<PostReportingData> postData;
	@JsonProperty("total_no_of_pages")
    public int totalNoOfPages;
    @JsonProperty("total_no_of_records")
    public Long totalNoOfRecords;

}
