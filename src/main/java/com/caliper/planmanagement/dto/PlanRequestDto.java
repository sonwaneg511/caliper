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
public class PlanRequestDto {
	@JsonProperty("service_ids")
	private List<String>serviceIds;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("start_date")
	private Date startDate;
	
	@JsonProperty("end_date")
	private Date endDate;
	
	@JsonProperty("payment_plan_request")
	private PaymentPlanRequest paymentPlanRequest;
	
	@JsonProperty("location_count")
	private Long locationCount;
	
}
