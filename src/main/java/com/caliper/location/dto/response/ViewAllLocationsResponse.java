package com.caliper.location.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewAllLocationsResponse {

	private List<ViewAllLocations> allLocatons;
	
	@JsonProperty("total_no_of_pages")
	private int totalNoOfPages;

	@JsonProperty("total_no_of_records")
	private Long totalNoOfRecords;
}
