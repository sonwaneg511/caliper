package com.caliper.reporting.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GMBLocationReportRequest {

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("page_no")
	private int pageNo;

	@JsonProperty("search")
	private String search;

}
