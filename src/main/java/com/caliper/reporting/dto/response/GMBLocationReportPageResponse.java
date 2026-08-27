package com.caliper.reporting.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GMBLocationReportPageResponse {

	@JsonProperty("gmb_location_data")
	private List<GMBLocationReportResponse> gmbLocationReportResponse;

	@JsonProperty("total_no_of_pages")
	private int totalNoOfPages;

	@JsonProperty("total_no_of_records")
	private Long totalNoOfRecords;

}
