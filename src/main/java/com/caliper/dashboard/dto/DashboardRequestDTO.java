package com.caliper.dashboard.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardRequestDTO {

	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("from_date")
	private Date fromDate;
	
	@JsonProperty("to_date")
	private Date toDate;
}
