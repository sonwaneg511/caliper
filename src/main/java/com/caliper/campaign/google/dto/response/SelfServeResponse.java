package com.caliper.campaign.google.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SelfServeResponse {

	@JsonProperty("result")
	private String result;
	
	@JsonProperty("message")
    private String message;
	
	@JsonProperty("user_role")
	private String userRole;
	
	@JsonProperty("campaign_id")
	private Long campaignId;
	
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_FAILURE = "failure";
}