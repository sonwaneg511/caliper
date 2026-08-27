package com.caliper.planmanagement.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentPlanResponse {

	@JsonProperty("status")
	private int status;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("code")
	private long code;
	
	private Date timestamp;
}
