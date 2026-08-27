package com.caliper.reporting.dto.response;

import java.util.List;

import com.caliper.post.dto.Response.PostDataResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InsightReportingTableDataResponse {
	
	@JsonProperty("insight_data")
	List<GMBInsightLocationResponse> gmbInsightLocationResponse;
	
    @JsonProperty("total_no_of_pages")
    public int totalNoOfPages;
    
    @JsonProperty("total_no_of_records")
    public Long totalNoOfRecords;

}
