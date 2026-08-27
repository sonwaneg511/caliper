package com.caliper.reporting.dto.request;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GMBLocationInisghtRequest {

	@JsonProperty("client_id")
	public String clientId;
	@JsonProperty("user_id")
	public String userId;
	
	@JsonFormat(pattern  = "yyyy-MM-dd")
	@JsonProperty("start_date")
	public Date startDate;
	
	@JsonProperty("end_date")
	@JsonFormat(pattern  = "yyyy-MM-dd")
	public Date endDate;
	@JsonProperty("dealer_id")
	public List<String> dealerIds;
}
